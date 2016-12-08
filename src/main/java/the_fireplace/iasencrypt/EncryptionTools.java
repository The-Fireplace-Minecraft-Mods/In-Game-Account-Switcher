package the_fireplace.iasencrypt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @author The_Fireplace
 * Thanks, ante.sabo on StackOverflow, for pointing me in the right direction
 */
public class EncryptionTools {
	public static final String DEFAULT_ENCODING="UTF-8"; 
	static BASE64Encoder enc=new BASE64Encoder();
	static BASE64Decoder dec=new BASE64Decoder();

	public static String encode(String text, String password){
		try {
			String rez = enc.encode( text.getBytes( DEFAULT_ENCODING ) );
			return rez;         
		}
		catch ( UnsupportedEncodingException e ) {
			return null;
		}
	}

	public static String decode(String text, String password){

		try {
			return new String(dec.decodeBuffer( text ),DEFAULT_ENCODING);
		}
		catch ( IOException e ) {
			return null;
		}

	}
	
	public static String generatePassword() {
		return "superSecureAndRandomPassword!!!";
	}
}
