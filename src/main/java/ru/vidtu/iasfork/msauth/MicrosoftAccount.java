package ru.vidtu.iasfork.msauth;

import java.io.Serializable;

/**
 * @author VidTu
 * @deprecated Inconvenient. Used only for conversion from old accounts to new accounts.
 */
@Deprecated
public class MicrosoftAccount implements Serializable {
	private static final long serialVersionUID = 5836857834701515666L;
	public String username;
	public String accessToken;
	public String refreshToken;
}