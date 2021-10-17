package ru.vidtu.ias.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.AuthException;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.utils.Auth;
import the_fireplace.ias.IAS;

/**
 * Screen for adding Microsoft accounts.
 * @author VidTu
 */
public class MSAuthScreen extends Screen {
	public static final String[] symbols = new String[]{"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
			"_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ _ _ ▃ ▄ ▅ ▆ ▇ █",
			"_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
			"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _", "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
			"▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "█ ▇ ▆ ▅ ▄ ▃ _ _ _ _ _", "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
			"▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _"};
	public final Screen prev;
	private HttpServer srv;
	private int tick;
	private Text state = new TranslatableText("ias.msauth.checkbrowser");
	private List<OrderedText> error;
	private Consumer<Account> handler;
	
	public MSAuthScreen(Screen prev, Consumer<Account> handler) {
		super(new TranslatableText("ias.msauth.title"));
		this.prev = prev;
		this.handler = handler;
		String done = "<html><body><h1>" + I18n.translate("ias.msauth.canclosenow") + "</h1></body></html>";
		new Thread(() -> {
			try {
				srv = HttpServer.create(new InetSocketAddress(59125), 0);
	        	srv.createContext("/", new HttpHandler() {
					public void handle(HttpExchange ex) throws IOException {
						try {
							ex.getResponseHeaders().add("Location", "http://localhost:59125/end");
							ex.sendResponseHeaders(302, -1);
							new Thread(() -> auth(ex.getRequestURI().getQuery()), "IAS MS Auth Thread").start();
						} catch (Throwable t) {
							IAS.LOG.warn("Unable to process request 'auth' on MS auth server", t);
							try {
								if (srv != null) srv.stop(0);
							} catch (Throwable th) {
								IAS.LOG.warn("Unable to stop fail-requested MS auth server", th);
							}
						}
					}
				});
	        	srv.createContext("/end", new HttpHandler() {
					public void handle(HttpExchange ex) throws IOException {
						try {
							byte[] b = done.getBytes(StandardCharsets.UTF_8);
							ex.getResponseHeaders().put("Content-Type", Arrays.asList("text/html; charset=UTF-8"));
							ex.sendResponseHeaders(200, b.length);
							OutputStream os = ex.getResponseBody();
							os.write(b);
							os.flush();
							os.close();
							try {
								if (srv != null) srv.stop(0);
							} catch (Throwable th) {
								IAS.LOG.warn("Unable to stop MS auth server", th);
							}
						} catch (Throwable t) {
							IAS.LOG.warn("Unable to process request 'end' on MS auth server", t);
							try {
								if (srv != null) srv.stop(0);
							} catch (Throwable th) {
								IAS.LOG.warn("Unable to stop fail-requested MS auth server", th);
							}
						}
					}
				});
	        	srv.start();
	        	Util.getOperatingSystem().open("https://login.live.com/oauth20_authorize.srf" +
                        "?client_id=54fd49e4-2103-4044-9603-2b028c814ec3" +
                        "&response_type=code" +
                        "&scope=XboxLive.signin%20XboxLive.offline_access" +
                        "&redirect_uri=http://localhost:59125" +
                        "&prompt=consent");
			} catch (Throwable t) {
				IAS.LOG.warn("Unable to start MS auth server", t);
				try {
					if (srv != null) srv.stop(0);
				} catch (Throwable th) {
					IAS.LOG.warn("Unable to stop fail-started MS auth server", th);
				}
				error(t);
			}
		}, "IAS MS Auth Server Thread").start();
	}
	
	private void auth(String query) {
		try {
			state = new TranslatableText("ias.msauth.progress");
			if (query == null) throw new NullPointerException("query=null");
			if (query.equals("error=access_denied&error_description=The user has denied access to the scope requested by the client application."))
				throw new AuthException(new TranslatableText("ias.msauth.error.revoked"));
			if (!query.startsWith("code=")) throw new IllegalStateException("query=" + query);
			Pair<String, String> authRefreshTokens = Auth.authCode2Token(query.replace("code=", ""));
			String refreshToken = authRefreshTokens.getRight();
			String xblToken = Auth.authXBL(authRefreshTokens.getLeft()); //authToken
			Pair<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
			String accessToken = Auth.authMinecraft(xstsTokenUserhash.getRight(), xstsTokenUserhash.getLeft());
			Auth.checkGameOwnership(accessToken);
			Pair<UUID, String> profile = Auth.getProfile(accessToken);
			if (Config.accounts.stream().anyMatch(acc -> acc.alias().equalsIgnoreCase(profile.getRight())))
				throw new AuthException(new TranslatableText("ias.auth.alreadyexists"));
			client.execute(() -> {
				if (client.currentScreen == this) {
					handler.accept(new MicrosoftAccount(profile.getRight(), accessToken, refreshToken, profile.getLeft()));
					client.setScreen(prev);
				}
			});
		} catch (Throwable t) {
			IAS.LOG.warn("Unable to auth thru MS", t);
			error(t);
		}
	}
	
	public void error(Throwable t) {
		client.execute(() -> {
			if (t instanceof AuthException) {
				error = textRenderer.wrapLines(((AuthException)t).getText(), width - 20);
			} else {
				error = textRenderer.wrapLines(new TranslatableText("ias.auth.unknown", t.toString()), width - 20);
			}
		});
	}

	@Override
	public void init() {
		addDrawableChild(new ButtonWidget(this.width / 2 - 75, this.height - 28, 150, 20, new TranslatableText("gui.cancel"), btn -> client.setScreen(prev)));
	}
	
	@Override
	public void tick() {
		tick++;
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Override
	public void removed() {
		try {
			if (srv != null) srv.stop(0);
		} catch (Throwable t) {
			IAS.LOG.warn("Unable to stop MS auth server", t);
		}
		super.removed();
	}
	
	@Override
	public boolean keyPressed(int key, int oldkey, int mods) {
		if (key == GLFW.GLFW_KEY_ESCAPE) {
			client.setScreen(prev);
			return true;
		}
		return super.keyPressed(key, oldkey, mods);
	}
	
	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float delta) {
		renderBackground(ms);
		drawCenteredText(ms, textRenderer, this.title, this.width / 2, 7, -1);
		if (error != null) {
			for (int i = 0; i < error.size(); i++) {
				textRenderer.drawWithShadow(ms, error.get(i), this.width / 2 - textRenderer.getWidth(error.get(i)) / 2, height / 2 - 5 + i * 10 - error.size() * 5, 0xFFFF0000);
				if (i > 6) break; //Exceptions can be very large.
			}
		} else {
			drawCenteredText(ms, textRenderer, state, width / 2, height / 2 - 10, -1);
			drawCenteredText(ms, textRenderer, symbols[tick % symbols.length], width / 2, height / 2, 0xFFFF9900);
		}
		super.render(ms, mouseX, mouseY, delta);
	}
}
