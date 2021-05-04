package ru.vidtu.iasfork.msauth;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GetRequest {
	public HttpURLConnection conn;
	public GetRequest(String uri) throws Exception {
		conn = (HttpURLConnection) new URL(uri).openConnection();
		conn.setRequestMethod("GET");
	}
	
	public GetRequest header(String key, String value) {
		conn.addRequestProperty(key, value);
		return this;
	}
	
	public void get() throws IOException {
		conn.connect();
	}
	
	public int response() throws IOException {
		return conn.getResponseCode();
	}
	
	public String body() throws IOException {
		StringBuilder sb = new StringBuilder();
		Reader r = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
		int i;
		while ((i = r.read()) >= 0) {
			sb.append((char)i);
		}
		r.close();
		return sb.toString();
	}
}
