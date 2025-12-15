package org.vash.vate.security;

import java.io.InputStream;
import java.io.OutputStream;

import org.vash.vate.VTSystem;
import org.vash.vate.org.bouncycastle.crypto.StreamCipher;
import org.vash.vate.org.bouncycastle.crypto.engines.ChaChaEngine;
//import org.vash.vate.org.bouncycastle.crypto.engines.Grain128Engine;
import org.vash.vate.org.bouncycastle.crypto.engines.HC256Engine;
//import org.vash.vate.org.bouncycastle.crypto.engines.RabbitEngine;
import org.vash.vate.org.bouncycastle.crypto.engines.ThreefishEngine;
import org.vash.vate.org.bouncycastle.crypto.engines.Zuc256Engine;
import org.vash.vate.org.bouncycastle.crypto.modes.SICBlockCipher;
import org.vash.vate.org.bouncycastle.crypto.params.KeyParameter;
import org.vash.vate.org.bouncycastle.crypto.params.ParametersWithIV;
import org.vash.vate.org.bouncycastle.crypto.params.TweakableBlockCipherParameters;

public class VTCryptographicEngine
{
  private final VTBlake3MessageDigest blake3Digest;
  
  private StreamCipher encryptionStreamCipher;
  private StreamCipher decryptionStreamCipher;
  
  public VTCryptographicEngine()
  {
    this.blake3Digest = new VTBlake3MessageDigest();
  }
  
  public void initializeClientEngine(final int encryptionType, final byte[]... encryptionKeys)
  {
    encryptionStreamCipher = null;
    decryptionStreamCipher = null;
    
    blake3Digest.reset();
    for (byte[] data : encryptionKeys)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    byte[] first = blake3Digest.digest(VTSystem.VT_SECURITY_SEED_SIZE_BYTES);
    blake3Digest.reset();
    blake3Digest.update(first);
    for (byte[] data : encryptionKeys)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    byte[] second = blake3Digest.digest(VTSystem.VT_SECURITY_SEED_SIZE_BYTES);
    
    if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
    {
      
    }
//    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_VMPC)
//    {
//      encryptionCipherBC = new VMPCKSA3Engine();
//      decryptionCipherBC = new VMPCKSA3Engine();
//      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(768, first, second, encryptionKeys), 0, 768);
//      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(768, second, first, encryptionKeys), 0, 768);
//      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(768, first, second, encryptionKeys), 0, 768);
//      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(768, second, first, encryptionKeys), 0, 768);
//      encryptionCipherBC.init(true, encryptionIvParameterSpec);
//      decryptionCipherBC.init(false, decryptionIvParameterSpec);
//    }
//    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_ISAAC)
//    {
//      encryptionStreamCipher = new ISAACEngine();
//      decryptionStreamCipher = new ISAACEngine();
//      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(1024, first, second, encryptionKeys), 0, 1024);
//      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(1024, second, first, encryptionKeys), 0, 1024);
//      encryptionStreamCipher.init(true, encryptionKeySpec);
//      decryptionStreamCipher.init(false, decryptionKeySpec);
//    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_SALSA)
    {
      encryptionStreamCipher = new ChaChaEngine(16);
      decryptionStreamCipher = new ChaChaEngine(16);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(8, first, second, encryptionKeys), 0, 8);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(8, second, first, encryptionKeys), 0, 8);
      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_HC)
    {
      encryptionStreamCipher = new HC256Engine();
      decryptionStreamCipher = new HC256Engine();
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 32);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 32);
      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
    }
//    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_GRAIN)
//    {
//      encryptionStreamCipher = new Grain128Engine();
//      decryptionStreamCipher = new Grain128Engine();
//      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(16, first, second, encryptionKeys), 0, 16);
//      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(16, second, first, encryptionKeys), 0, 16);
//      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(12, first, second, encryptionKeys), 0, 12);
//      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(12, second, first, encryptionKeys), 0, 12);
//      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
//      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
//    }
//    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_RABBIT)
//    {
//      encryptionStreamCipher = new RabbitEngine();
//      decryptionStreamCipher = new RabbitEngine();
//      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(16, first, second, encryptionKeys), 0, 16);
//      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(16, second, first, encryptionKeys), 0, 16);
//      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(8, first, second, encryptionKeys), 0, 8);
//      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(8, second, first, encryptionKeys), 0, 8);
//      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
//      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
//    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_ZUC)
    {
      encryptionStreamCipher = new Zuc256Engine();
      decryptionStreamCipher = new Zuc256Engine();
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(25, first, second, encryptionKeys), 0, 25);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(25, second, first, encryptionKeys), 0, 25);
      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_THREEFISH)
    {
      encryptionStreamCipher = new SICBlockCipher(new ThreefishEngine(256));
      decryptionStreamCipher = new SICBlockCipher(new ThreefishEngine(256));
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(new TweakableBlockCipherParameters(decryptionKeySpec, generateIVBLAKE3(16, first, second, encryptionKeys)), generateIVBLAKE3(32, first, second, encryptionKeys), 0, 32);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(new TweakableBlockCipherParameters(encryptionKeySpec, generateIVBLAKE3(16, second, first, encryptionKeys)), generateIVBLAKE3(32, second, first, encryptionKeys), 0, 32);
      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
    }
    else
    {
      
    }
  }
  
  public void initializeServerEngine(final int encryptionType, final byte[]... encryptionKeys)
  {
    encryptionStreamCipher = null;
    decryptionStreamCipher = null;
    
    blake3Digest.reset();
    for (byte[] data : encryptionKeys)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    byte[] first = blake3Digest.digest(VTSystem.VT_SECURITY_SEED_SIZE_BYTES);
    blake3Digest.reset();
    blake3Digest.update(first);
    for (byte[] data : encryptionKeys)
    {
      if (data != null && data.length > 0)
      {
        blake3Digest.update(data);
      }
    }
    byte[] second = blake3Digest.digest(VTSystem.VT_SECURITY_SEED_SIZE_BYTES);
    
    if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
    {
      
    }
//    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_VMPC)
//    {
//      encryptionCipherBC = new VMPCKSA3Engine();
//      decryptionCipherBC = new VMPCKSA3Engine();
//      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(768, first, second, encryptionKeys), 0, 768);
//      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(768, second, first, encryptionKeys), 0, 768);
//      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(768, first, second, encryptionKeys), 0, 768);
//      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(768, second, first, encryptionKeys), 0, 768);
//      encryptionCipherBC.init(true, encryptionIvParameterSpec);
//      decryptionCipherBC.init(false, decryptionIvParameterSpec);
//    }
//    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_ISAAC)
//    {
//      encryptionStreamCipher = new ISAACEngine();
//      decryptionStreamCipher = new ISAACEngine();
//      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(1024, first, second, encryptionKeys), 0, 1024);
//      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(1024, second, first, encryptionKeys), 0, 1024);
//      encryptionStreamCipher.init(true, encryptionKeySpec);
//      decryptionStreamCipher.init(false, decryptionKeySpec);
//    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_SALSA)
    {
      encryptionStreamCipher = new ChaChaEngine(16);
      decryptionStreamCipher = new ChaChaEngine(16);
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(8, first, second, encryptionKeys), 0, 8);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(8, second, first, encryptionKeys), 0, 8);
      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_HC)
    {
      encryptionStreamCipher = new HC256Engine();
      decryptionStreamCipher = new HC256Engine();
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(32, first, second, encryptionKeys), 0, 32);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(32, second, first, encryptionKeys), 0, 32);
      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
    }
//    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_GRAIN)
//    {
//      encryptionStreamCipher = new Grain128Engine();
//      decryptionStreamCipher = new Grain128Engine();
//      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(16, first, second, encryptionKeys), 0, 16);
//      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(16, second, first, encryptionKeys), 0, 16);
//      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(12, first, second, encryptionKeys), 0, 12);
//      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(12, second, first, encryptionKeys), 0, 12);
//      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
//      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
//    }
//    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_RABBIT)
//    {
//      encryptionStreamCipher = new RabbitEngine();
//      decryptionStreamCipher = new RabbitEngine();
//      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(16, first, second, encryptionKeys), 0, 16);
//      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(16, second, first, encryptionKeys), 0, 16);
//      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(8, first, second, encryptionKeys), 0, 8);
//      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(8, second, first, encryptionKeys), 0, 8);
//      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
//      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
//    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_ZUC)
    {
      encryptionStreamCipher = new Zuc256Engine();
      decryptionStreamCipher = new Zuc256Engine();
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(encryptionKeySpec, generateIVBLAKE3(25, first, second, encryptionKeys), 0, 25);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(decryptionKeySpec, generateIVBLAKE3(25, second, first, encryptionKeys), 0, 25);
      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_THREEFISH)
    {
      encryptionStreamCipher = new SICBlockCipher(new ThreefishEngine(256));
      decryptionStreamCipher = new SICBlockCipher(new ThreefishEngine(256));
      KeyParameter encryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, first, second, encryptionKeys), 0, 32);
      KeyParameter decryptionKeySpec = new KeyParameter(generateKeyBLAKE3(32, second, first, encryptionKeys), 0, 32);
      ParametersWithIV encryptionIvParameterSpec = new ParametersWithIV(new TweakableBlockCipherParameters(encryptionKeySpec, generateIVBLAKE3(16, first, second, encryptionKeys)), generateIVBLAKE3(32, first, second, encryptionKeys), 0, 32);
      ParametersWithIV decryptionIvParameterSpec = new ParametersWithIV(new TweakableBlockCipherParameters(decryptionKeySpec, generateIVBLAKE3(16, second, first, encryptionKeys)), generateIVBLAKE3(32, second, first, encryptionKeys), 0, 32);
      encryptionStreamCipher.init(true, encryptionIvParameterSpec);
      decryptionStreamCipher.init(false, decryptionIvParameterSpec);
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
  
  public InputStream getDecryptedInputStream(final InputStream encrypted, final int bufferSize)
  {
    if (decryptionStreamCipher != null)
    {
      return new VTStreamCipherInputStream(encrypted, decryptionStreamCipher, bufferSize);
    }
    return encrypted;
  }
  
  public OutputStream getEncryptedOutputStream(final OutputStream decrypted, final int bufferSize)
  {
    if (encryptionStreamCipher != null)
    {
      return new VTStreamCipherOutputStream(decrypted, encryptionStreamCipher, bufferSize);
    }
    return decrypted;
  }
}