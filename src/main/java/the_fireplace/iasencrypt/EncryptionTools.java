package the_fireplace.iasencrypt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Thanks, ante.sabo on StackOverflow, for pointing me in the right direction
 * 
 * @author The_Fireplace
 * @author BrainStone
 */
public final class EncryptionTools {
	public static final String DEFAULT_ENCODING = "UTF-8";
	private static BASE64Encoder enc = new BASE64Encoder();
	private static BASE64Decoder dec = new BASE64Decoder();
	private static MessageDigest sha512 = getSha512Hasher();
	private static KeyGenerator keyGen = getAESGenerator();
	private static String secretSalt = "{$secret_salt}";

	public static String encode(String text, String password) {
		try {
			byte[] data = text.getBytes(DEFAULT_ENCODING);
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password));

			return enc.encode(cipher.doFinal(data));
		} catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | IOException
				| NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String decode(String text, String password) {
		try {
			byte[] data = dec.decodeBuffer(text);
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password));

			return new String(cipher.doFinal(data), DEFAULT_ENCODING);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IOException
				| IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String generatePassword() {
		keyGen.init(512);
		return enc.encode(keyGen.generateKey().getEncoded());
	}

	private static MessageDigest getSha512Hasher() {
		try {
			return MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static KeyGenerator getAESGenerator() {
		try {
			return KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static SecretKeySpec getSecretKey(String password) {
		try {
			password = secretSalt + password + secretSalt;
			byte[] key = Arrays.copyOf(sha512.digest(password.getBytes(DEFAULT_ENCODING)), 16);

			return new SecretKeySpec(key, "AES");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
