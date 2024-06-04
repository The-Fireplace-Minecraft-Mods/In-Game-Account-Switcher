/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2024 VidTu
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

package ru.vidtu.ias.utils;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Various IAS utils.
 *
 * @author VidTu
 */
public final class IUtils {
    /**
     * Ports to try binding to.
     * Not everything is allowed, this should be edited in MS Entra manually.
     */
    private static final int @NotNull [] TRY_BIND_PORTS = {59125, 59126, 59127, 59128, 59129, 59130, 59131,
            59132, 59133, 59134, 59135, 1234, 1235, 1236, 1237, 80, 8080, 19364, 19365, 19366, 27930,
            27931, 27932, 27933, 27934, 42069};

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private IUtils() {
        throw new AssertionError("No instances.");
    }

    /**
     * Gets whether the user should be warned about the name.
     *
     * @param name Target name
     * @return Warning key, {@code null} if none
     */
    @Contract(pure = true)
    @Nullable
    public static String warnKey(@NotNull String name) {
        // Blank.
        if (name.isBlank()) return "ias.nick.blank";

        // Length.
        int length = name.length();
        if (length < 3) return "ias.nick.short";
        if (length > 16) return "ias.nick.long";

        // Chars.
        for (int i = 0; i < length; i++) {
            int c = name.codePointAt(i);
            if (c == '_' || c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') continue;
            return "ias.nick.chars";
        }

        // Valid.
        return null;
    }

    /**
     * Tests the exceptions from the causal chain.
     *
     * @param root   Causal chain root exception
     * @param tester Exception tester
     * @return {@code true} if the predicate has matched any exception in the causal chain, {@code false} if not, causal loop detected, or causal stack is too big
     */
    @Contract(pure = true)
    public static boolean anyInCausalChain(@Nullable Throwable root, @NotNull Predicate<Throwable> tester) {
        // Causal loop detection set.
        Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<>(8));

        // 256 is the (arbitrarily-chosen) limit for causal stack.
        for (int i = 0; i < 256; i++) {
            // Break if null (reached the end) or loop detected.
            if (root == null || !dejaVu.add(root)) break;

            // Return if tested exception.
            if (tester.test(root)) return true;

            // Next cause.
            root = root.getCause();
        }

        // Not found. (or reached the 256 limit)
        return false;
    }

    /**
     * Gets whether the HTTP server provided by Sun exists in this Java implementation.
     * <p>
     * The server authentication method is the most convenient, but the server is located in
     * the {@code com.sun} package and in the internal module, so it might not be present in all Java
     * implementations and/or inbound firewall configurations. Because of this, we also add an
     * alternative authentication method, which is less convenient, but should be supported on
     * all Java platforms and inbound firewall configs.
     *
     * @return The presence of the HTTP server
     * @implNote This method might block for the first call
     */
    @CheckReturnValue
    public static boolean canUseSunServer() {
        return SunServerAvailability.AVAILABLE;
    }

    /**
     * Gets the ports to try and bind to.
     *
     * @return A copy of ports that can be tried to be bound to
     */
    @Contract(pure = true)
    public static int @NotNull [] tryBindPorts() {
        return TRY_BIND_PORTS.clone();
    }

    /**
     * Lazily-initialized holder for value returned by {@link #canUseSunServer()}.
     */
    private static final class SunServerAvailability {
        /**
         * Whether the Sun server is available.
         */
        private static final boolean AVAILABLE;

        static {
            // Lazy init.
            Logger logger = LoggerFactory.getLogger("IAS/IUtils/SunServerAvailability");
            logger.info("IAS: Testing Sun HTTP server availability...");
            boolean available;
            try {
                // Check for class presence.
                Class.forName("com.sun.net.httpserver.HttpServer");
                logger.info("IAS: Sun HTTP server class found, testing firewall by binding TCP server.");

                // Create the socket.
                try (ServerSocket server = new ServerSocket();
                     Socket client = new Socket()) {
                    // Set up timeouts.
                    server.setSoTimeout(2000);

                    // Try to bind it.
                    bindToSupportedPort(server);
                    logger.info("IAS: TCP server bound, connecting...");

                    // Try to connect.
                    int port = server.getLocalPort();
                    client.setSoTimeout(2000);
                    client.setTcpNoDelay(true);
                    client.connect(new InetSocketAddress(port), 2000);
                    logger.info("IAS: Connected to TCP server, trying to exchange data bidirectionally.");

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
                            throw new IllegalStateException("S2C data doesn't match, sent " + HexFormat.of().formatHex(data) + ", got " + HexFormat.of().formatHex(read));
                        }

                        // Client-to-server.
                        random.nextBytes(data);
                        clientOut.write(data);
                        clientOut.flush();
                        read = serverIn.readNBytes(256);

                        // Something in the way.
                        if (!Arrays.equals(data, read)) {
                            throw new IllegalStateException("C2S data doesn't match, sent " + HexFormat.of().formatHex(data) + ", got " + HexFormat.of().formatHex(read));
                        }
                    }

                    // Success.
                    logger.info("IAS: Exchanged TCP data, setting Sun server as available...");
                    available = true;
                }
            } catch (Throwable t) {
                // Log.
                logger.warn("IAS: Sun server is not available or is not accessible.", t);
                available = false;
            }
            AVAILABLE = available;
        }

        /**
         * Bind the server to any supported port.
         *
         * @param socket Socket to bind
         * @throws RuntimeException If unable to bind
         */
        private static void bindToSupportedPort(@NotNull ServerSocket socket) {
            // Any thrown exceptions.
            List<RuntimeException> thrown = new LinkedList<>();

            // Note that this port range MUST be declared in Microsoft valid
            // redirect URIs, so using any port won't work. I did register some
            // ports in the UI.
            for (int port : TRY_BIND_PORTS) {
                try {
                    // Try to bind.
                    socket.bind(new InetSocketAddress(port), 0);
                    return;
                } catch (Throwable t) {
                    // Add to thrown exceptions.
                    thrown.add(new RuntimeException("Unable to bind: " + port, t));
                }
            }

            // Rethrow all errors.
            RuntimeException holder = new RuntimeException("Unable to bind to any port.");
            thrown.forEach(holder::addSuppressed);
            throw holder;
        }

        /**
         * An instance of this class cannot be created.
         *
         * @throws AssertionError Always
         */
        @Contract(value = "-> fail", pure = true)
        private SunServerAvailability() {
            throw new AssertionError("No instances.");
        }
    }
}
