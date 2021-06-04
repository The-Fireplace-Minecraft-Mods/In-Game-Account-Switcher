package ru.vidtu.iasfork.msauth;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

public class Request {
	public HttpURLConnection conn;
	public Request(String url) throws Exception {
		conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
	}
	
	public Request header(String key, String value) {
		conn.setRequestProperty(key, value);
		return this;
	}
	
	public Request post(String s) throws IOException {
		conn.setRequestMethod("POST");
		byte[] out = s.getBytes(StandardCharsets.UTF_8);
		conn.connect();
		OutputStream os = conn.getOutputStream();
		os.write(out);
		os.flush();
		os.close();
		return this;
	}
	
	public Request post(Map<Object, Object> map) throws Exception {
		StringJoiner sj = new StringJoiner("&");
		for (Entry<Object, Object> entry : map.entrySet())
			sj.add(URLEncoder.encode(entry.getKey().toString(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
		post(sj.toString());
		return this;
	}
	
	public Request get() throws ProtocolException {
		conn.setRequestMethod("GET");
		return this;
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
