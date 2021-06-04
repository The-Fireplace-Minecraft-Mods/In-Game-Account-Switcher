package ru.vidtu.iasfork.msauth;

public interface MSAuthHandler {
	public void setState(String s);
	public void error(Throwable t);
	public void success(String name, String uuid, String token, String refresh);
	public void cancellble(boolean b);
}
