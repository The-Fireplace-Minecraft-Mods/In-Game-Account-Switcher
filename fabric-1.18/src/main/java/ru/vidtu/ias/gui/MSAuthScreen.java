package ru.vidtu.ias.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.AuthException;
import ru.vidtu.ias.account.MicrosoftAccount;
import ru.vidtu.ias.utils.Auth;
import the_fireplace.ias.IAS;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Screen for adding Microsoft accounts.
 *
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
    private String state = I18n.get("ias.msauth.checkbrowser");
    private List<FormattedCharSequence> error;
    private Consumer<Account> handler;

    public MSAuthScreen(Screen prev, Consumer<Account> handler) {
        super(new TranslatableComponent("ias.msauth.title"));
        this.prev = prev;
        this.handler = handler;
        String done = "<html><body><h1>" + I18n.get("ias.msauth.canclosenow") + "</h1></body></html>";
        IAS.EXECUTOR.execute(() -> {
            try {
                srv = HttpServer.create(new InetSocketAddress(59125), 0);
                srv.createContext("/", ex -> {
                    try {
                        byte[] b = done.getBytes(StandardCharsets.UTF_8);
                        ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                        ex.sendResponseHeaders(200, b.length);
                        try (OutputStream os = ex.getResponseBody()) {
                            os.write(b);
                        }
                        if (srv != null) srv.stop(0);
                        auth(ex.getRequestURI().getQuery());
                    } catch (Throwable t) {
                        IAS.LOG.warn("Unable to process request 'auth' on MS auth server", t);
                        if (srv != null) srv.stop(0);
                    }
                });
                srv.start();
                Util.getPlatform().openUri("https://login.live.com/oauth20_authorize.srf" +
                        "?client_id=54fd49e4-2103-4044-9603-2b028c814ec3" +
                        "&response_type=code" +
                        "&scope=XboxLive.signin%20XboxLive.offline_access" +
                        "&redirect_uri=http://localhost:59125" +
                        "&prompt=select_account");
            } catch (Throwable t) {
                IAS.LOG.warn("Unable to start MS auth server", t);
                try {
                    if (srv != null) srv.stop(0);
                } catch (Throwable th) {
                    IAS.LOG.warn("Unable to stop fail-started MS auth server", th);
                }
                error(t);
            }
        });
    }

    private void auth(String query) {
        try {
            IAS.LOG.info("Authenticating...");
            state = I18n.get("ias.msauth.progress");
            if (query == null) throw new NullPointerException("query=null");
            if (query.equals("error=access_denied&error_description=The user has denied access to the scope requested by the client application."))
                throw new AuthException(new TranslatableComponent("ias.msauth.error.revoked"));
            if (!query.startsWith("code=")) throw new IllegalStateException("query=" + query);
            IAS.LOG.info("Step: code -> token.");
            Pair<String, String> authRefreshTokens = Auth.authCode2Token(query.replace("code=", ""));
            String refreshToken = authRefreshTokens.getRight();
            IAS.LOG.info("Step: authXBL.");
            String xblToken = Auth.authXBL(authRefreshTokens.getLeft()); //authToken
            IAS.LOG.info("Step: authXSTS.");
            Pair<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
            IAS.LOG.info("Step: authMinecraft.");
            String accessToken = Auth.authMinecraft(xstsTokenUserhash.getRight(), xstsTokenUserhash.getLeft());
            IAS.LOG.info("Step: checkGameOwnership.");
            Auth.checkGameOwnership(accessToken);
            IAS.LOG.info("Step: getProfile.");
            Pair<UUID, String> profile = Auth.getProfile(accessToken);
            minecraft.execute(() -> {
                if (minecraft.screen == this) {
                    handler.accept(new MicrosoftAccount(profile.getRight(), accessToken, refreshToken, profile.getLeft()));
                    minecraft.setScreen(prev);
                }
            });
        } catch (Throwable t) {
            IAS.LOG.warn("Unable to auth via Microsoft.", t);
            error(t);
        }
    }

    public void error(Throwable t) {
        minecraft.execute(() -> {
            if (t instanceof AuthException) {
                error = font.split(((AuthException) t).getComponent(), width - 20);
            } else {
                error = font.split(new TranslatableComponent("ias.auth.unknown", t.toString()), width - 20);
            }
        });
    }

    @Override
    public void init() {
        addRenderableWidget(new Button(this.width / 2 - 75, this.height - 28, 150, 20, new TranslatableComponent("gui.cancel"), btn -> minecraft.setScreen(prev)));
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
            minecraft.setScreen(prev);
            return true;
        }
        return super.keyPressed(key, oldkey, mods);
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float delta) {
        renderBackground(ms);
        drawCenteredString(ms, font, this.title, this.width / 2, 7, -1);
        if (error != null) {
            for (int i = 0; i < error.size(); i++) {
                font.draw(ms, error.get(i), this.width / 2 - font.width(error.get(i)) / 2, height / 2 - 5 + i * 10 - error.size() * 5, 0xFFFF0000);
                if (i > 6) break; //Exceptions can be very large.
            }
        } else {
            drawCenteredString(ms, font, state, width / 2, height / 2 - 10, -1);
            drawCenteredString(ms, font, symbols[tick % symbols.length], width / 2, height / 2, 0xFFFF9900);
        }
        super.render(ms, mouseX, mouseY, delta);
    }
}
