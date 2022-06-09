package ru.vidtu.ias.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

/**
 * Perform basic HTTP methods like GET or POST.
 *
 * @author VidTu
 */
public class Request {
    /**
     * Current HTTP conenction.
     */
    public HttpURLConnection connection;

    /**
     * Initialize HTTP connection with <code>5000</code> timeouts.
     *
     * @param url HTTP connection URL
     * @throws IOException           If we're unable to initialize your connection
     * @throws MalformedURLException If your connection URL is malformed
     */
    public Request(String url) throws MalformedURLException, IOException {
        connection = (HttpURLConnection) new URL(url).openConnection();
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection secureConnection = (HttpsURLConnection) connection;
            secureConnection.setSSLSocketFactory(OldJavaFix.getFixedContext().getSocketFactory());
        }
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
    }

    /**
     * Set an HTTP header for the current connection.
     *
     * @param key   Header name
     * @param value Header value
     */
    public void header(String key, String value) {
        connection.setRequestProperty(key, value);
    }

    /**
     * Switch to the <code>POST</code> mode and send the string encoded in <code>UTF-8</code>.
     *
     * @param data Data for sending
     * @throws IOException If we're unable to send the data
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">Reference</a>
     */
    public void post(String data) throws IOException {
        connection.setRequestMethod("POST");
        try (OutputStream os = connection.getOutputStream()) {
            os.write(data.getBytes(StandardCharsets.UTF_8));
        }
    }

	/**
	 * Switch to the <code>POST</code> mode and send the data in <i>key=value&key2=value2&key3=value3</i> format
	 * encoded in <code>UTF-8</code> and <code>URL encoding</code>.
	 *
	 * @param map Data for encoding and sending
	 * @throws IOException If we're unable to send the data
	 * @see #post(String)
	 */
    public void post(Map<Object, Object> map) throws IOException {
        StringJoiner joiner = new StringJoiner("&");
        for (Entry<Object, Object> entry : map.entrySet())
            joiner.add(URLEncoder.encode(entry.getKey().toString(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
        post(joiner.toString());
    }

    /**
     * Switch to <code>GET</code> mode.
     *
     * @throws ProtocolException If we're unable to get
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/GET">Reference</a>
     */
    public void get() throws ProtocolException {
        connection.setRequestMethod("GET");
    }

    /**
     * Get the HTTP response code.
     *
     * @return HTTP response code
     * @throws IOException If we're unable to get response code
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">Reference</a>
     */
    public int response() throws IOException {
        return connection.getResponseCode();
    }

    /**
     * Get the HTTP response body.
     *
     * @return HTTP response body
     * @throws IOException If we're unable to get response body
     */
    public String body() throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Reader r = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            int i;
            while ((i = r.read()) >= 0) builder.append((char) i);
        }
        return builder.toString();
    }

    /**
     * Get HTTP response error body encoded as <code>UTF-8</code>.
     *
     * @return HTTP response error body
     * @throws IOException If we're unable to get response error body
     */
    public String error() throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Reader r = new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8)) {
            int i;
            while ((i = r.read()) >= 0) builder.append((char) i);
        }
        return builder.toString();
    }
}
