package ru.vidtu.iasfork.msauth;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

public class PostRequest {
	public HttpURLConnection conn;
	public PostRequest(String uri) throws Exception {
		conn = (HttpURLConnection) new URL(uri).openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
	}
	
	public PostRequest header(String key, String value) {
		conn.setRequestProperty(key, value);
		return this;
	}
	
	public void post(String s) throws IOException {
		byte[] out = s.getBytes(StandardCharsets.UTF_8);
		conn.connect();
		OutputStream os = conn.getOutputStream();
		os.write(out);
		os.flush();
		os.close();
	}
	
	public void post(Map<Object, Object> map) throws Exception {
		StringJoiner sj = new StringJoiner("&");
		for (Entry<Object, Object> entry : map.entrySet())
			sj.add(URLEncoder.encode(entry.getKey().toString(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
		post(sj.toString());
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
