package ru.vidtu.ias.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

/**
 * Perform basic HTTP methods like GET or POST. 
 * @author VidTu
 */
public class Request {
	/**Current HTTP conenction.*/
	public HttpURLConnection conn;
	
	/**
	 * Initialize HTTP connection with <code>5000</code> timeouts.
	 * @param url HTTP connection URL
	 * @throws IOException If we're unable to initialize your connection
	 * @throws MalformedURLException If your connection URL is malformed
	 */
	public Request(String url) throws MalformedURLException, IOException {
		conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(5000);
	}
	
	/**
	 * Add HTTP header to the current connection.
	 * @param key Header name
	 * @param value Header value
	 */
	public void header(String key, String value) {
		conn.setRequestProperty(key, value);
	}
	
	/**
	 * Switch to <code>POST</code> mode and send your string encoded in <code>UTF-8</code>,
	 * @param s String for sending
	 * @throws IOException If we're unable to send your data
	 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST
	 */
	public void post(String s) throws IOException {
		conn.setRequestMethod("POST");
		byte[] out = s.getBytes(StandardCharsets.UTF_8);
		try (OutputStream os = conn.getOutputStream()) {
			os.write(out);
		}
	}
	
	/**
	 * Switch to <code>POST</code> mode and send your data in <i>key=value&key2=value2&key3=value3</i> format
	 * encoded in <code>UTF-8</code> and <code>URL encoding</code>.
	 * @param s String for sending
	 * @throws IOException If we're unable to send your data
	 * @see #post(String)
	 */
	public void post(Map<Object, Object> map) throws IOException {
		StringJoiner sj = new StringJoiner("&");
		for (Entry<Object, Object> entry : map.entrySet())
			sj.add(URLEncoder.encode(entry.getKey().toString(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
		post(sj.toString());
	}
	
	/**
	 * Switch to <code>GET</code> mode.
	 * @throws ProtocolException If we're unable to get
	 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/GET
	 */
	public void get() throws ProtocolException {
		conn.setRequestMethod("GET");
	}
	
	/**
	 * Get HTTP response code encoded as <code>UTF-8</code>.
	 * @return HTTP response code
	 * @throws IOException If we're unable to get response code
	 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
	 */
	public int response() throws IOException {
		return conn.getResponseCode();
	}
	
	/**
	 * Get HTTP response body.
	 * @return HTTP response body
	 * @throws IOException If we're unable to get response body
	 */
	public String body() throws IOException {
		StringBuilder sb = new StringBuilder();
		try (Reader r = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
			int i;
			while ((i = r.read()) >= 0) {
				sb.append((char)i);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Get HTTP response error body encoded as <code>UTF-8</code>.
	 * @return HTTP response error body
	 * @throws IOException If we're unable to get response error body
	 */
	public String error() throws IOException {
		StringBuilder sb = new StringBuilder();
		try (Reader r = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)) {
			int i;
			while ((i = r.read()) >= 0) {
				sb.append((char)i);
			}
		}
		return sb.toString();
	}
}
