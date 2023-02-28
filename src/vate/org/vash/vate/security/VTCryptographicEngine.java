package org.vash.vate.security;

import java.io.InputStream;
import java.io.OutputStream;
//import java.security.InvalidAlgorithmParameterException;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;

//import javax.crypto.Cipher;
//import javax.crypto.CipherInputStream;
//import javax.crypto.CipherOutputStream;
//import javax.crypto.NoSuchPaddingException;
//import javax.crypto.spec.IvParameterSpec;
//import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.StreamCipher;
//import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.engines.ChaChaEngine;
import org.bouncycastle.crypto.engines.Grain128Engine;
import org.bouncycastle.crypto.engines.HC256Engine;
import org.bouncycastle.crypto.engines.ISAACEngine;
//import org.bouncycastle.crypto.engines.VMPCEngine;
import org.bouncycastle.crypto.engines.VMPCKSA3Engine;
//import org.bouncycastle.crypto.engines.Zuc128Engine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;
//import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.vash.vate.VT;

public class VTCryptographicEngine
{
  private VTBlake3MessageDigest blake3Digest;
  
  //private volatile Cipher encryptionCipher;
  //private volatile Cipher decryptionCipher;
  private volatile StreamCipher encryptionCipherBC;
  private volatile StreamCipher decryptionCipherBC;
  
  public VTCryptographicEngine()
  {
    this.blake3Digest = new VTBlake3MessageDigest();
  }
  
  public void initializeClientEngine(int encryptionType, byte[]... encryptionKeys)
  {
    //encryptionCipher = null;
    //decryptionCipher = null;
    encryptionCipherBC = null;
    decryptionCipherBC = null;
    
    blake3Digest.reset();
    for (byte[] data : encryptionKeys)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    byte[] first = blake3Digest.digest(64);
    blake3Digest.reset();
    blake3Digest.update(first);
    for (byte[] data : encryptionKeys)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    byte[] second = blake3Digest.digest(64);
    
    if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
    {
      
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
    {
      encryptionCipherBC = new VMPCKSA3Engine();
      decryptionCipherBC = new VMPCKSA3Engine();
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(512, first, second, encryptionKeys), 0, 512);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(512, second, first, encryptionKeys), 0, 512);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(512, first, second, encryptionKeys), 0, 512);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(512, second, first, encryptionKeys), 0, 512);
      encryptionCipherBC.init(true, encryptionIvParameterSpec);
      decryptionCipherBC.init(false, decryptionIvParameterSpec);
      //encryptionCipherBC = new RC4Engine();
      //decryptionCipherBC = new RC4Engine();
      //KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(256, first, second, encryptionKeys), 0, 256);
      //KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(256, second, first, encryptionKeys), 0, 256);
      //encryptionCipherBC.init(true, encryptionKeySpec);
      //decryptionCipherBC.init(false, decryptionKeySpec);
    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
//    {
//      encryptionCipherBC = new SICBlockCipher(new AESFastEngine());
//      decryptionCipherBC = new SICBlockCipher(new AESFastEngine());
//      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
//      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
//      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 16);
//      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 16);
//      encryptionCipherBC.init(true, encryptionIvParameterSpec);
//      decryptionCipherBC.init(false, decryptionIvParameterSpec);
//    }
    //else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_BLOWFISH)
    //{
      //encryptionCipher = Cipher.getInstance("Blowfish/CTR/NoPadding");
      //decryptionCipher = Cipher.getInstance("Blowfish/CTR/NoPadding");
      //SecretKeySpec decryptionKeySpec = new SecretKeySpec(digestedKey, 0, 16, "Blowfish");
      //SecretKeySpec encryptionKeySpec = new SecretKeySpec(digestedKey, 16, 16, "Blowfish");
      //IvParameterSpec decryptionIvParameterSpec = new IvParameterSpec(digestedIv, 0, 8);
      //IvParameterSpec encryptionIvParameterSpec = new IvParameterSpec(digestedIv, 16, 8);
      //encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKeySpec, encryptionIvParameterSpec);
      //decryptionCipher.init(Cipher.DECRYPT_MODE, decryptionKeySpec, decryptionIvParameterSpec);
    //}
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_SALSA)
    {
      encryptionCipherBC = new ChaChaEngine(16);
      decryptionCipherBC = new ChaChaEngine(16);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 8);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 8);
      encryptionCipherBC.init(true, encryptionIvParameterSpec);
      decryptionCipherBC.init(false, decryptionIvParameterSpec);
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_HC256)
    {
      encryptionCipherBC = new HC256Engine();
      decryptionCipherBC = new HC256Engine();
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 32);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 32);
      encryptionCipherBC.init(true, encryptionIvParameterSpec);
      decryptionCipherBC.init(false, decryptionIvParameterSpec);
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ISAAC)
    {
      encryptionCipherBC = new ISAACEngine();
      decryptionCipherBC = new ISAACEngine();
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(1024, first, second, encryptionKeys), 0, 1024);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(1024, second, first, encryptionKeys), 0, 1024);
      encryptionCipherBC.init(true, encryptionKeySpec);
      decryptionCipherBC.init(false, decryptionKeySpec);
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
    {
      encryptionCipherBC = new Grain128Engine();
      decryptionCipherBC = new Grain128Engine();
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 16);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 16);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 12);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 12);
      encryptionCipherBC.init(true, encryptionIvParameterSpec);
      decryptionCipherBC.init(false, decryptionIvParameterSpec);
    }
    else
    {
      
    }
  }
  
  public void initializeServerEngine(int encryptionType, byte[]... encryptionKeys)
  {
    //encryptionCipher = null;
    //decryptionCipher = null;
    encryptionCipherBC = null;
    decryptionCipherBC = null;
    
    blake3Digest.reset();
    for (byte[] data : encryptionKeys)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    byte[] first = blake3Digest.digest(64);
    blake3Digest.reset();
    blake3Digest.update(first);
    for (byte[] data : encryptionKeys)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    byte[] second = blake3Digest.digest(64);
    
    if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
    {
      
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
    {
      encryptionCipherBC = new VMPCKSA3Engine();
      decryptionCipherBC = new VMPCKSA3Engine();
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(512, first, second, encryptionKeys), 0, 512);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(512, second, first, encryptionKeys), 0, 512);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(512, first, second, encryptionKeys), 0, 512);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(512, second, first, encryptionKeys), 0, 512);
      encryptionCipherBC.init(true, encryptionIvParameterSpec);
      decryptionCipherBC.init(false, decryptionIvParameterSpec);
      //encryptionCipherBC = new RC4Engine();
      //decryptionCipherBC = new RC4Engine();
      //KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(256, first, second, encryptionKeys), 0, 256);
      //KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(256, second, first, encryptionKeys), 0, 256);
      //encryptionCipherBC.init(true, encryptionKeySpec);
      //decryptionCipherBC.init(false, decryptionKeySpec);
    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
//    {
//      encryptionCipherBC = new SICBlockCipher(new AESFastEngine());
//      decryptionCipherBC = new SICBlockCipher(new AESFastEngine());
//      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
//      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
//      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 16);
//      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 16);
//      encryptionCipherBC.init(true, encryptionIvParameterSpec);
//      decryptionCipherBC.init(false, decryptionIvParameterSpec);
//    }
    //else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_BLOWFISH)
    //{
      //encryptionCipher = Cipher.getInstance("Blowfish/CTR/NoPadding");
      //decryptionCipher = Cipher.getInstance("Blowfish/CTR/NoPadding");
      //byte[] digestedKey = sha256Digester.digest();
      //SecretKeySpec encryptionKeySpec = new SecretKeySpec(digestedKey, 0, 16, "Blowfish");
      //SecretKeySpec decryptionKeySpec = new SecretKeySpec(digestedKey, 16, 16, "Blowfish");
      //byte[] digestedIv = sha256Digester.digest(digestedKey);
      //IvParameterSpec encryptionIvParameterSpec = new IvParameterSpec(digestedIv, 0, 8);
      //IvParameterSpec decryptionIvParameterSpec = new IvParameterSpec(digestedIv, 16, 8);
      //encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKeySpec, encryptionIvParameterSpec);
      //decryptionCipher.init(Cipher.DECRYPT_MODE, decryptionKeySpec, decryptionIvParameterSpec);
    //}
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_SALSA)
    {
      encryptionCipherBC = new ChaChaEngine(16);
      decryptionCipherBC = new ChaChaEngine(16);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 8);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 8);
      encryptionCipherBC.init(true, encryptionIvParameterSpec);
      decryptionCipherBC.init(false, decryptionIvParameterSpec);
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_HC256)
    {
      encryptionCipherBC = new HC256Engine();
      decryptionCipherBC = new HC256Engine();
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 32);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 32);
      encryptionCipherBC.init(true, encryptionIvParameterSpec);
      decryptionCipherBC.init(false, decryptionIvParameterSpec);
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ISAAC)
    {
      encryptionCipherBC = new ISAACEngine();
      decryptionCipherBC = new ISAACEngine();
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(1024, first, second, encryptionKeys), 0, 1024);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(1024, second, first, encryptionKeys), 0, 1024);
      encryptionCipherBC.init(true, encryptionKeySpec);
      decryptionCipherBC.init(false, decryptionKeySpec);
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
    {
      encryptionCipherBC = new Grain128Engine();
      decryptionCipherBC = new Grain128Engine();
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 16);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 16);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 12);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 12);
      encryptionCipherBC.init(true, encryptionIvParameterSpec);
      decryptionCipherBC.init(false, decryptionIvParameterSpec);
    }
    else
    {
      
    }
  }
  
//  private byte[] generateKeyBLAKE3256(byte[] first, byte[] second, byte[]... extra)
//  {
//    blake3Digester.reset();
//    blake3Digester.update(first);
//    blake3Digester.update(second);
//    for (byte[] data : extra)
//    {
//      if (data != null && data.length > 0)
//      {
//        blake3Digester.update(data);
//      }
//    }
//    blake3Digester.update(second);
//    blake3Digester.update(first);
//    return blake3Digester.digest(32);
//  }
//  
//  private byte[] generateIVBLAKE3256(byte[] first, byte[] second, byte[]... extra)
//  {
//    blake3Digester.reset();
//    blake3Digester.update(first);
//    for (byte[] data : extra)
//    {
//      if (data != null && data.length > 0)
//      {
//        blake3Digester.update(data);
//      }
//    }
//    blake3Digester.update(second);
//    return blake3Digester.digest(32);
//  }
  
  private byte[] generateKeyBLAKE3(int size, byte[] first, byte[] second, byte[]... extra)
  {
    blake3Digest.reset();
    blake3Digest.update(first);
    blake3Digest.update(second);
    for (byte[] data : extra)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    blake3Digest.update(second);
    blake3Digest.update(first);
    return blake3Digest.digest(size);
  }
  
  private byte[] generateIVBLAKE3(int size, byte[] first, byte[] second, byte[]... extra)
  {
    blake3Digest.reset();
    blake3Digest.update(second);
    blake3Digest.update(first);
    for (byte[] data : extra)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    blake3Digest.update(first);
    blake3Digest.update(second);
    return blake3Digest.digest(size);
  }
  
  public InputStream getDecryptedInputStream(InputStream encrypted)
  {
    if (decryptionCipherBC != null)
    {
      return new CipherInputStream(encrypted, decryptionCipherBC, VT.VT_STANDARD_DATA_BUFFER_SIZE);
    }
    return encrypted;
    //if (decryptionCipher == null)
    //{
      //return encrypted;
    //}
    //return new CipherInputStream(encrypted, decryptionCipher);
  }
  
  public OutputStream getEncryptedOutputStream(OutputStream decrypted)
  {
    if (encryptionCipherBC != null)
    {
      return new CipherOutputStream(decrypted, encryptionCipherBC);
    }
    return decrypted;
    //if (encryptionCipher == null)
    //{
      //return decrypted;
    //}
    //return new CipherOutputStream(decrypted, encryptionCipher);
  }
}