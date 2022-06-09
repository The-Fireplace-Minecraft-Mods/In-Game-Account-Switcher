package ru.vidtu.ias.utils;

import the_fireplace.ias.IAS;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * Fix for older Java 8 builds. (Distributed by Mojang)
 *
 * @author VidTu
 */
public class OldJavaFix {
    private static SSLContext context;
    public static SSLContext getFixedContext() {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream in = OldJavaFix.class.getResourceAsStream("/iasjavafix.jks")) {
                ks.load(in, "iasjavafix".toCharArray());
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), new SecureRandom());
        } catch (Throwable t) {
            IAS.LOG.error("Unable to fix old Java build.", t);
        }
        return context;
    }
}
