package org.vash.vate.security;

import java.io.InputStream;
import java.io.OutputStream;

import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.ChaChaEngine;
import org.bouncycastle.crypto.engines.HC256Engine;
import org.bouncycastle.crypto.engines.ISAACEngine;
import org.bouncycastle.crypto.engines.VMPCKSA3Engine;
import org.bouncycastle.crypto.engines.Zuc256Engine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.vash.vate.VT;

public class VTCryptographicEngine
{
  private final VTBlake3MessageDigest blake3Digest;
  
  private StreamCipher encryptionCipherBC;
  private StreamCipher decryptionCipherBC;
  
  public VTCryptographicEngine()
  {
    this.blake3Digest = new VTBlake3MessageDigest();
  }
  
  public void initializeClientEngine(final int encryptionType, final byte[]... encryptionKeys)
  {
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
    byte[] first = blake3Digest.digest(VT.VT_SECURITY_SEED_SIZE_BYTES);
    blake3Digest.reset();
    blake3Digest.update(first);
    for (byte[] data : encryptionKeys)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    byte[] second = blake3Digest.digest(VT.VT_SECURITY_SEED_SIZE_BYTES);
    
    if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
    {
      
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_VMPC)
    {
      encryptionCipherBC = new VMPCKSA3Engine();
      decryptionCipherBC = new VMPCKSA3Engine();
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(768, first, second, encryptionKeys), 0, 768);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(768, second, first, encryptionKeys), 0, 768);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(768, first, second, encryptionKeys), 0, 768);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(768, second, first, encryptionKeys), 0, 768);
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
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ZUC256)
    {
      encryptionCipherBC = new Zuc256Engine();
      decryptionCipherBC = new Zuc256Engine();
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 25);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 25);
      encryptionCipherBC.init(true, encryptionIvParameterSpec);
      decryptionCipherBC.init(false, decryptionIvParameterSpec);
    }
    else
    {
      
    }
  }
  
  public void initializeServerEngine(final int encryptionType, final byte[]... encryptionKeys)
  {
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
    byte[] first = blake3Digest.digest(VT.VT_SECURITY_SEED_SIZE_BYTES);
    blake3Digest.reset();
    blake3Digest.update(first);
    for (byte[] data : encryptionKeys)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    byte[] second = blake3Digest.digest(VT.VT_SECURITY_SEED_SIZE_BYTES);
    
    if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
    {
      
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_VMPC)
    {
      encryptionCipherBC = new VMPCKSA3Engine();
      decryptionCipherBC = new VMPCKSA3Engine();
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(768, first, second, encryptionKeys), 0, 768);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(768, second, first, encryptionKeys), 0, 768);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(768, first, second, encryptionKeys), 0, 768);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(768, second, first, encryptionKeys), 0, 768);
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
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ZUC256)
    {
      encryptionCipherBC = new Zuc256Engine();
      decryptionCipherBC = new Zuc256Engine();
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 25);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 25);
      encryptionCipherBC.init(true, encryptionIvParameterSpec);
      decryptionCipherBC.init(false, decryptionIvParameterSpec);
    }
    else
    {
      
    }
  }
  
  private byte[] generateKeyBLAKE3(final int size, final byte[] first, final byte[] second, final byte[]... extra)
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
  
  private byte[] generateIVBLAKE3(final int size, final byte[] first, final byte[] second, final byte[]... extra)
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
  
  public InputStream getDecryptedInputStream(final InputStream encrypted)
  {
    if (decryptionCipherBC != null)
    {
      return new VTStreamCipherInputStream(encrypted, decryptionCipherBC);
    }
    return encrypted;
  }
  
  public OutputStream getEncryptedOutputStream(final OutputStream decrypted)
  {
    if (encryptionCipherBC != null)
    {
      return new VTStreamCipherOutputStream(decrypted, encryptionCipherBC);
    }
    return decrypted;
  }
}