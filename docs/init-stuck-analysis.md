# IAS `Initializing` 卡住问题分析（针对 commit `241e565`）

## 结论（先说结果）

你这次在 `MicrosoftAccount.login` 链路里给 `MSAuth.*` 请求加的 `orTimeout(...)` 与重试，方向是对的，但**它只覆盖了微软鉴权链的一部分**，并没有覆盖真正更可能把整个 IAS 异步线程卡死的路径。

IAS 当前核心异步执行器是**单线程**（`newSingleThreadScheduledExecutor`），所以只要有一个任务在这个线程里阻塞，后续登录任务就会排队，UI 会长期停在最早显示的阶段（常见就是 `Initializing`）。

## 你这次修改里存在的问题

1. `orTimeout` 叠加在本身已有 HTTP timeout 的请求上，收益有限。
   - `MSAuth` 的请求本来就有 `HttpRequest.timeout(IAS.TIMEOUT)`（默认 15s）。
   - 新增 `orTimeout(5s/10s)` 会更快超时，但并不能解决“**不是这些请求导致的阻塞**”问题。

2. 重试逻辑里 `Thread.sleep(1000)` 放在 IAS 单线程异步链上，会额外阻塞同一个执行器。
   - 虽然只睡 1 秒，但在单线程下会阻塞所有排队任务。

3. `exceptionallyComposeAsync(timeoutErr -> retry...)` 没有区分异常类型：
   - 当前写法对所有异常都重试一次（包括 4xx 逻辑错误），可能掩盖真实错误并增加等待。

## 为什么仍然会卡在 `Initializing`

更关键的可疑点在账号切换后段（不是你这次加 timeout 的位置）：

- `IASMinecraft.account(...)` 在 `CompletableFuture.runAsync(..., IAS.executor())` 中执行。
- 里面调用 `services.sessionService().fetchProfile(data.uuid(), true)` 是同步调用，可能在网络/会话异常时长期阻塞。
- 由于 IAS executor 是单线程，只要这里卡住，后续任何登录链（包括你刚加了 timeout 的微软 token 刷新链）都拿不到线程执行。
- 新发起登录会先同步设置阶段为 `Initializing`，随后异步链无法推进，于是界面就像“卡在 Initializing”。

这也解释了你观察到的场景：

- 休眠恢复后网络栈/DNS/连接状态异常；或
- token 过期触发更多网络请求；或
- 出现“无法加入服务器”后再切号。

这些都可能让前一次任务在单线程执行器里阻塞，继而拖死后续任务。

## 建议修复方向（优先级顺序）

1. **把可能阻塞很久的调用移出 IAS 单线程执行器**（优先）
   - 特别是 `fetchProfile(...)` / `fetchProperties()` 这类同步网络调用。
   - 可用独立线程池，或改成可控 timeout 的异步流程。

2. **保留你新增的 timeout，但删除链路中的 `Thread.sleep`**
   - 改为 `CompletableFuture.delayedExecutor(...)` 做非阻塞延迟重试。

3. **重试要按异常类型筛选**
   - 仅对连接超时、DNS 失败等 transient 错误重试。
   - 4xx/参数错误等直接失败并展示友好错误。

4. **给“登录流程总时长”再加一层兜底 timeout**
   - 防止某个未覆盖节点永远不返回。

5. **增强日志定位**
   - 在进入/退出 `IASMinecraft.account` 的关键点打耗时日志，便于确认是不是 `fetchProfile` 卡住。

