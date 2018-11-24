package org.vate.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.vate.VT;

public class VTCryptographicEngine
{
	private Cipher encryptionCipher;
	private Cipher decryptionCipher;
	private MessageDigest sha256Digester;
	
	public VTCryptographicEngine(MessageDigest sha256Digester)
	{
		this.sha256Digester = sha256Digester;
	}
	
	public void initializeClientEngine(int encryptionType, byte[]... encryptionKeys) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
	{
		if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
		{
			encryptionCipher = null;
			decryptionCipher = null;
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
		{
			sha256Digester.reset();
			encryptionCipher = Cipher.getInstance("RC4");
			decryptionCipher = Cipher.getInstance("RC4");
			for (byte[] data : encryptionKeys)
			{
				if (data != null && data.length > 0)
				{
					sha256Digester.update(data);
				}
			}
			byte[] digestedKey = sha256Digester.digest();
			SecretKeySpec decryptionKeySpec = new SecretKeySpec(digestedKey, 0, 16, "RC4");
			SecretKeySpec encryptionKeySpec = new SecretKeySpec(digestedKey, 16, 16, "RC4");
			encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKeySpec);
			decryptionCipher.init(Cipher.DECRYPT_MODE, decryptionKeySpec);
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
		{
			sha256Digester.reset();
			encryptionCipher = Cipher.getInstance("AES/CTR/NoPadding");
			decryptionCipher = Cipher.getInstance("AES/CTR/NoPadding");
			for (byte[] data : encryptionKeys)
			{
				if (data != null && data.length > 0)
				{
					sha256Digester.update(data);
				}
			}
			byte[] digestedKey = sha256Digester.digest();
			SecretKeySpec decryptionKeySpec = new SecretKeySpec(digestedKey, 0, 16, "AES");
			SecretKeySpec encryptionKeySpec = new SecretKeySpec(digestedKey, 16, 16, "AES");
			byte[] digestedIv = sha256Digester.digest(digestedKey);
			IvParameterSpec decryptionIvParameterSpec = new IvParameterSpec(digestedIv, 0, 16);
			IvParameterSpec encryptionIvParameterSpec = new IvParameterSpec(digestedIv, 16, 16);
			encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKeySpec, encryptionIvParameterSpec);
			decryptionCipher.init(Cipher.DECRYPT_MODE, decryptionKeySpec, decryptionIvParameterSpec);
		}
	}
	
	public void initializeServerEngine(int encryptionType, byte[]... encryptionKeys) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
	{
		if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
		{
			encryptionCipher = null;
			decryptionCipher = null;
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
		{
			sha256Digester.reset();
			encryptionCipher = Cipher.getInstance("RC4");
			decryptionCipher = Cipher.getInstance("RC4");
			for (byte[] data : encryptionKeys)
			{
				if (data != null && data.length > 0)
				{
					sha256Digester.update(data);
				}
			}
			byte[] digestedKey = sha256Digester.digest();
			SecretKeySpec encryptionKeySpec = new SecretKeySpec(digestedKey, 0, 16, "RC4");
			SecretKeySpec decryptionKeySpec = new SecretKeySpec(digestedKey, 16, 16, "RC4");
			encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKeySpec);
			decryptionCipher.init(Cipher.DECRYPT_MODE, decryptionKeySpec);
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
		{
			sha256Digester.reset();
			encryptionCipher = Cipher.getInstance("AES/CTR/NoPadding");
			decryptionCipher = Cipher.getInstance("AES/CTR/NoPadding");
			for (byte[] data : encryptionKeys)
			{
				if (data != null && data.length > 0)
				{
					sha256Digester.update(data);
				}
			}
			byte[] digestedKey = sha256Digester.digest();
			SecretKeySpec encryptionKeySpec = new SecretKeySpec(digestedKey, 0, 16, "AES");
			SecretKeySpec decryptionKeySpec = new SecretKeySpec(digestedKey, 16, 16, "AES");
			byte[] digestedIv = sha256Digester.digest(digestedKey);
			IvParameterSpec encryptionIvParameterSpec = new IvParameterSpec(digestedIv, 0, 16);
			IvParameterSpec decryptionIvParameterSpec = new IvParameterSpec(digestedIv, 16, 16);
			encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKeySpec, encryptionIvParameterSpec);
			decryptionCipher.init(Cipher.DECRYPT_MODE, decryptionKeySpec, decryptionIvParameterSpec);
		}
	}
	
	public InputStream getDecryptedInputStream(InputStream encrypted)
	{
		if (decryptionCipher == null)
		{
			return encrypted;
		}
		return new CipherInputStream(encrypted, decryptionCipher);
	}
	
	public OutputStream getEncryptedOutputStream(OutputStream decrypted)
	{
		if (encryptionCipher == null)
		{
			return decrypted;
		}
		return new CipherOutputStream(decrypted, encryptionCipher);
	}
}