/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2025 VidTu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package ru.vidtu.ias.config;

import com.google.common.base.Suppliers;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ias.IAS;
import ru.vidtu.ias.auth.microsoft.MSAuth;
import ru.vidtu.ias.platform.IStonecutter;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Internal Sun HTTP server mode.
 * <p>
 * The server authentication method is the most convenient, but the server is located in
 * the {@code com.sun} package and in the internal module, so it might not be present in all Java
 * implementations and/or inbound firewall configurations. Because of this, we also add an
 * alternative authentication method, which is less convenient, but should be supported on
 * all Java platforms and inbound firewall configs.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see IConfig#server
 */
@ApiStatus.Internal
@NullMarked
public enum ServerMode {
    /**
     * Always use HTTP server, never use Microsoft device auth.
     */
    ALWAYS(Suppliers.ofInstance(true)),

    /**
     * Use HTTP server if available, otherwise use Microsoft device auth.
     */
    AVAILABLE(Suppliers.memoize(() -> {
        Logger logger = LogManager.getLogger("IAS/ServerMode");
        long start = System.nanoTime();
        try {
            // Log.
            logger.info(IAS.IAS_MARKER, "IAS: Testing Sun HTTP server availability...");

            // Check for class presence, log. (**DEBUG**)
            Class.forName("com.sun.net.httpserver.HttpServer");
            logger.debug(IAS.IAS_MARKER, "IAS: Sun HTTP server class found, testing firewall by binding TCP server.");

            // Create the socket.
            try (ServerSocket server = new ServerSocket();
                 Socket client = new Socket()) {
                // Set up timeouts.
                server.setSoTimeout(2000);

                // Try to bind it, log. (**DEBUG**)
                bindToSupportedPort(server);
                logger.debug(IAS.IAS_MARKER, "IAS: TCP server bound, connecting... (server: {}, client: {})", server, client);

                // Try to connect, log. (**DEBUG**)
                int port = server.getLocalPort();
                client.setSoTimeout(2000);
                client.setTcpNoDelay(true);
                client.connect(new InetSocketAddress(port), 2000);
                logger.debug(IAS.IAS_MARKER, "IAS: Connected to TCP server, trying to exchange data bidirectionally. (server: {}, client: {}, port: {})", server, client, port);

                // Try to exchange some random data.
                try (Socket accepted = server.accept();
                     InputStream clientIn = client.getInputStream();
                     OutputStream clientOut = client.getOutputStream();
                     InputStream serverIn = accepted.getInputStream();
                     OutputStream serverOut = accepted.getOutputStream()) {

                    // Server-to-client.
                    Random random = new SecureRandom();
                    byte[] data = new byte[256];
                    random.nextBytes(data);
                    serverOut.write(data);
                    serverOut.flush();
                    byte[] read = clientIn.readNBytes(256);

                    // Something in the way.
                    if (!Arrays.equals(data, read)) {
                        throw new IllegalStateException("S2C data doesn't match. (sentL " + Base64.getEncoder().encodeToString(data) + ", got " + Base64.getEncoder().encodeToString(read) + ')');
                    }

                    // Client-to-server.
                    random.nextBytes(data);
                    clientOut.write(data);
                    clientOut.flush();
                    read = serverIn.readNBytes(256);

                    // Something in the way.
                    if (!Arrays.equals(data, read)) {
                        throw new IllegalStateException("C2S data doesn't match. (sent: " + Base64.getEncoder().encodeToString(data) + ", got: " + Base64.getEncoder().encodeToString(read) + ')');
                    }
                }

                // Success, log.
                logger.info(IAS.IAS_MARKER, "IAS: Sun HTTP server is available. ({} ms)", (System.nanoTime() - start) / 1_000_000L);
                return true;
            }
        } catch (Throwable t) {
            // Log.
            if (logger.isDebugEnabled(IAS.IAS_MARKER)) {
                logger.warn(IAS.IAS_MARKER, "IAS: Sun HTTP server is not available or is not accessible. ({} ms)", (System.nanoTime() - start) / 1_000_000L, t);
            } else {
                logger.warn(IAS.IAS_MARKER, "IAS: Sun HTTP server is not available or is not accessible. Enable debug logging to see the stacktrace. ({} ms)", (System.nanoTime() - start) / 1_000_000L);
            }
            return false;
        }
    })),

    /**
     * Never use HTTP server, always use Microsoft device auth.
     */
    NEVER(Suppliers.ofInstance(false));

    /**
     * Lazy supplier that determines whether the Sun HTTP server should be used.
     */
    private final Supplier<Boolean> useSunServer;

    /**
     * Mode button label.
     */
    private final Component label;

    /**
     * Mode button tip.
     */
    private final Component tip;

    /**
     * Creates a new mode.
     *
     * @param useSunServer Lazy supplier that determines whether the Sun HTTP server should be used
     */
    @Contract(pure = true)
    ServerMode(Supplier<Boolean> useSunServer) {
        // Validate.
        assert useSunServer != null : "Parameter 'useSunServer' is null. (mode: " + this + ')';

        // Assign.
        this.useSunServer = useSunServer;

        // Create the translation key.
        String key = ("ias.server." + this.name().toLowerCase(Locale.ROOT));

        // Create the components.
        this.label = IStonecutter.translate("options.generic_value", IStonecutter.translate("ias.server"), IStonecutter.translate(key.intern()));
        this.tip = IStonecutter.translate((key + ".tip").intern());
    }

    /**
     * Gets whether the Sun HTTP server should be used. This method is <b>BLOCKING</b>.
     *
     * @return Whether the
     */
    @Blocking
    @CheckReturnValue
    public boolean useSunServer() {
        return this.useSunServer.get();
    }

    /**
     * Gets the button label for this mode.
     *
     * @return Mode button label
     * @see #tip()
     * @see IScreen
     */
    @Contract(pure = true)
    Component label() {
        return this.label;
    }

    /**
     * Gets the button tooltip for this mode.
     *
     * @return Mode button tip
     * @see #label()
     * @see IScreen
     */
    @Contract(pure = true)
    Component tip() {
        return this.tip;
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "IAS/ServerMode{" +
                "name='" + this.name() + '\'' +
                ", ordinal=" + this.ordinal() +
                ", label=" + this.label +
                ", tip=" + this.tip +
                '}';
    }

    /**
     * Tries to bind the server socket to any supported port.
     *
     * @param socket Socket to bind
     * @throws RuntimeException If unable to bind to any port
     */
    private static void bindToSupportedPort(ServerSocket socket) {
        // Any thrown exceptions.
        List<RuntimeException> suppressed = new LinkedList<>();

        // Note that this port range MUST be declared in Microsoft valid
        // redirect URIs, so using any port won't work. I did register some
        // ports in the UI.
        for (int port : MSAuth.TRY_BIND_PORTS) {
            try {
                // Try to bind.
                socket.bind(new InetSocketAddress(port), 0);
                return;
            } catch (Throwable t) {
                // Add to thrown exceptions.
                suppressed.add(new RuntimeException("Unable to bind to port: " + port, t));
            }
        }

        // Rethrow all errors.
        RuntimeException holder = new RuntimeException("Unable to bind to any port.");
        suppressed.forEach(holder::addSuppressed);
        throw holder;
    }
}
