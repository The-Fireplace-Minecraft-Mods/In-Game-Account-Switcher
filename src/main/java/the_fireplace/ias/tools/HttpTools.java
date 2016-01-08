package the_fireplace.ias.tools;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class HttpTools {
	public static boolean ping(String url){
		try{
			final URLConnection con = new URL(url).openConnection();
			con.connect();
			return true;
		}catch(IOException e){
			return false;
		}
	}
}
