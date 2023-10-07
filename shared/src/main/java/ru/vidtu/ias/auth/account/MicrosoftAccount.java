package ru.vidtu.ias.auth.account;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.auth.AuthData;
import ru.vidtu.ias.auth.ms.AuthStage;
import ru.vidtu.ias.auth.ms.MSAuth;
import ru.vidtu.ias.utils.GSONUtils;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Microsoft account instance.
 *
 * @author VidTu
 */
public final class MicrosoftAccount implements Account {
    private UUID uuid;
    private String name;
    private String access;
    private String refresh;

    /**
     * Creates a new Microsoft account.
     *
     * @param uuid    Account UUID
     * @param name    Account name
     * @param access  Account Minecraft access (MA) token
     * @param refresh Account Microsoft refresh (MSR) token
     */
    public MicrosoftAccount(@NotNull UUID uuid, @NotNull String name,
                            @NotNull String access, @NotNull String refresh) {
        this.uuid = uuid;
        this.name = name;
        this.access = access;
        this.refresh = refresh;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public UUID uuid() {
        return uuid;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String name() {
        return name;
    }

    /**
     * Gets the Minecraft access (MA) token of this account.
     *
     * @return Account access token
     */
    @Contract(pure = true)
    @NotNull
    public String access() {
        return access;
    }

    /**
     * Gets the Microsoft refresh (MSR) token of this account.
     *
     * @return Account refresh token
     */
    @Contract(pure = true)
    @NotNull
    public String refresh() {
        return refresh;
    }

    @Contract(value = "_ -> new")
    @Override
    @CheckReturnValue
    @NotNull
    public CompletableFuture<AuthData> login(@NotNull Consumer<AuthStage> progress) {
        return CompletableFuture.supplyAsync(() -> {
            IAS.log().info("Logging as {}/{}", uuid, name);
            progress.accept(AuthStage.INITIALIZING);
            return new MSAuth(IAS.userAgent(), IAS.clientId(), IAS.redirectUri(), Duration.ofSeconds(15L), IAS.executor());
        }, IAS.executor()).thenComposeAsync(auth -> {
            IAS.log().info("Getting profile via MA... (stored)");
            progress.accept(AuthStage.MA_TO_PROFILE);
            return auth.minecraftToProfile(access).thenApplyAsync(profile -> {
                IAS.log().info("Success (stored): {}", profile);
                progress.accept(AuthStage.FINISHING);
                this.uuid = profile.uuid();
                this.name = profile.name();
                return new AuthData(name, uuid, access, AuthData.MSA);
            }, IAS.executor()).exceptionallyComposeAsync(original -> {
                IAS.log().warn("MA token is (probably) expired. Refreshing...");
                IAS.log().info("Refreshing MSA via MSR...");
                progress.accept(AuthStage.MSR_TO_MS);
                return auth.refreshToTokens(refresh).thenComposeAsync(ms -> {
                    this.refresh = ms.refresh();
                    IAS.log().info("Converting MSA to XBL...");
                    progress.accept(AuthStage.MSA_TO_XBL);
                    return auth.accessToXbox(ms.access());
                }, IAS.executor()).thenComposeAsync(xbl -> {
                    IAS.log().info("Converting XBL to XSTS...");
                    progress.accept(AuthStage.XBL_TO_XSTS);
                    return auth.xboxToSecureToken(xbl.token(), xbl.hash());
                }, IAS.executor()).thenComposeAsync(xsts -> {
                    IAS.log().info("Converting XSTS to MA...");
                    progress.accept(AuthStage.XSTS_TO_MA);
                    return auth.secureTokenToMinecraft(xsts.token(), xsts.hash());
                }, IAS.executor()).thenComposeAsync(access -> {
                    this.access = access;
                    IAS.log().info("Getting profile via MA...");
                    progress.accept(AuthStage.MA_TO_PROFILE);
                    return auth.minecraftToProfile(access).thenApply(profile -> new AuthData(profile.name(), profile.uuid(), access, AuthData.MSA));
                }, IAS.executor()).exceptionallyAsync(th -> {
                    RuntimeException ex = new RuntimeException("Unable to refresh account: " + uuid + "/" + name, th);
                    ex.addSuppressed(original);
                    throw ex;
                });
            }, IAS.executor());
        }, IAS.executor());
    }

    /**
     * GSON serializer and deserializer for {@link MicrosoftAccount}.
     *
     * @author VidTu
     */
    public static final class Adapter implements JsonSerializer<MicrosoftAccount>, JsonDeserializer<MicrosoftAccount> {
        @Contract(value = "_, _, _ -> new", pure = true)
        @Override
        @NotNull
        public JsonElement serialize(MicrosoftAccount account, Type type, JsonSerializationContext ctx) {
            JsonObject json = new JsonObject();
            json.addProperty("uuid", account.uuid.toString());
            json.addProperty("name", account.name);
            json.addProperty("access", account.access);
            json.addProperty("refresh", account.refresh);
            return json;
        }

        @Contract(value = "!null, _, _ -> new; null, _, _ -> fail", pure = true)
        @Override
        @NotNull
        public MicrosoftAccount deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            try {
                JsonObject json = element.getAsJsonObject();
                String uuidValue = GSONUtils.getStringOrThrow(json, "uuid");
                String name = GSONUtils.getStringOrThrow(json, "name");
                String access = GSONUtils.getStringOrThrow(json, "access");
                String refresh = GSONUtils.getStringOrThrow(json, "refresh");
                UUID uuid = UUID.fromString(uuidValue);
                return new MicrosoftAccount(uuid, name, access, refresh);
            } catch (Throwable t) {
                throw new JsonParseException("Unable to parse Microsoft account: " + element, t);
            }
        }
    }
}
