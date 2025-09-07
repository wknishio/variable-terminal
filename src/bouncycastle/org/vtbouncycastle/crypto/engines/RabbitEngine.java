package org.vtbouncycastle.crypto.engines;

import java.util.Arrays;

import org.vtbouncycastle.crypto.CipherParameters;
import org.vtbouncycastle.crypto.DataLengthException;
import org.vtbouncycastle.crypto.StreamCipher;
import org.vtbouncycastle.crypto.params.KeyParameter;
import org.vtbouncycastle.crypto.params.ParametersWithIV;
import org.vtbouncycastle.util.Pack;

/**
 * Tested against the actual RFC.
 * 
 * @author Chase (Robert Maupin)
 * @see {@link http://tools.ietf.org/rfc/rfc4503.txt}
 */
public class RabbitEngine implements StreamCipher
{
	private static final int KEYSTREAM_LENGTH = 16;
	private static final int[] A = new int[]
	{
    0x4D34D34D,
    0xD34D34D3,
    0x34D34D34,
    0x4D34D34D,
    0xD34D34D3,
    0x34D34D34,
    0x4D34D34D,
    0xD34D34D3
	};

	private final int[] X = new int[8];
	private final int[] C = new int[8];
	private int B;
	private int I = 16;
	//private byte[] keystream;

	public RabbitEngine()
	{
		B = 0;
	}
	
  private final int[] G = new int[8];
  private final byte[] S = new byte[16];
	
	byte[] nextBlock()
	{
    nextState();
    
    int x = X[0] ^ X[5] >>> 16;
    S[0] = (byte) x;
    S[1] = (byte)(x >> 8);
    
    x = X[0] >>> 16 ^ X[3];
    S[2] = (byte) x;
    S[3] = (byte)(x >> 8);
    
    x = X[2] ^ X[7] >>> 16;
    S[4] = (byte) x;
    S[5] = (byte)(x >> 8);
    
    x = X[2] >> 16 ^ X[5];
    S[6] = (byte) x;
    S[7] = (byte)(x >> 8);
    
    x = X[4] ^ X[1] >>> 16;
    S[8] = (byte) x;
    S[9] = (byte)(x >> 8);
    
    x = X[4] >>> 16 ^ X[7];
    S[10] = (byte) x;
    S[11] = (byte)(x >> 8);
    
    x = X[6] ^ X[3] >>> 16;
    S[12] = (byte) x;
    S[13] = (byte)(x >> 8);
    
    x = X[6] >>> 16 ^ X[1];
    S[14] = (byte) x;
    S[15] = (byte)(x >> 8);
    
    return S;
	}

	/**
	 * Clears all internal data. You must set the key again to use this cypher.
	 */
	public void reset()
	{
		B = 0;
		I = 16;
		//keystream = null;
		Arrays.fill(X, 0);
		Arrays.fill(C, 0);
		Arrays.fill(S, (byte)0);
	}

	/**
	 * @param IV
	 *            An array of 8 bytes
	 */
	public void setupIV(final byte[] IV)
	{
		short[] sIV = new short[IV.length>>1];
		for(int i=0;i<sIV.length;++i) {
		  sIV[i] = (short)((IV[(i << 1) + 1] << 8) | IV[i << 1]);
		}
		Pack.littleEndianToShort(IV, 0, sIV, 0, 4);
		setupIV(sIV);
	}
	
	/**
	 * @param iv
	 *            array of 4 short values
	 */
	public void setupIV(final short[] iv) {
		/* unroll */
		C[0] ^= iv[1] << 16 | iv[0] & 0xFFFF;
		C[1] ^= iv[3] << 16 | iv[1] & 0xFFFF;
		C[2] ^= iv[3] << 16 | iv[2] & 0xFFFF;
		C[3] ^= iv[2] << 16 | iv[0] & 0xFFFF;
		C[4] ^= iv[1] << 16 | iv[0] & 0xFFFF;
		C[5] ^= iv[3] << 16 | iv[1] & 0xFFFF;
		C[6] ^= iv[3] << 16 | iv[2] & 0xFFFF;
		C[7] ^= iv[2] << 16 | iv[0] & 0xFFFF;
		
		nextState();
		nextState();
		nextState();
		nextState();
	}

	/**
	 * @param key
	 *            An array of 16 bytes
	 */
	public void setupKey(final byte[] key)
	{
		short[] sKey = new short[key.length>>1];
		Pack.littleEndianToShort(key, 0, sKey, 0, 8);
		setupKey(sKey);
	}
	
	/**
	 * @param key
	 *            An array of 8 short values
	 */
	public void setupKey(final short[] key) {
		/* unroll */
		X[0] = key[1] << 16 | key[0] & 0xFFFF;
		X[1] = key[6] << 16 | key[5] & 0xFFFF;
		X[2] = key[3] << 16 | key[2] & 0xFFFF;
		X[3] = key[0] << 16 | key[7] & 0xFFFF;
		X[4] = key[5] << 16 | key[4] & 0xFFFF;
		X[5] = key[2] << 16 | key[1] & 0xFFFF;
		X[6] = key[7] << 16 | key[6] & 0xFFFF;
		X[7] = key[4] << 16 | key[3] & 0xFFFF;
		/* unroll */
		C[0] = key[4] << 16 | key[5] & 0xFFFF;
		C[1] = key[1] << 16 | key[2] & 0xFFFF;
		C[2] = key[6] << 16 | key[7] & 0xFFFF;
		C[3] = key[3] << 16 | key[4] & 0xFFFF;
		C[4] = key[0] << 16 | key[1] & 0xFFFF;
		C[5] = key[5] << 16 | key[6] & 0xFFFF;
		C[6] = key[2] << 16 | key[3] & 0xFFFF;
		C[7] = key[7] << 16 | key[0] & 0xFFFF;
		nextState();
		nextState();
		nextState();
		nextState();
		/* unroll */
		C[0] ^= X[4];
		C[1] ^= X[5];
		C[2] ^= X[6];
		C[3] ^= X[7];
		C[4] ^= X[0];
		C[5] ^= X[1];
		C[6] ^= X[2];
		C[7] ^= X[3];
	}

  public void init(boolean forEncryption, CipherParameters params) throws IllegalArgumentException
  {
    CipherParameters keyParam = params;
    byte[] key = null;
    byte[] iv = null;

    if (params instanceof ParametersWithIV)
    {
        iv = ((ParametersWithIV)params).getIV();
        keyParam = ((ParametersWithIV)params).getParameters();
    }
    else
    {
        iv = new byte[0];
    }
    
    if (keyParam instanceof KeyParameter)
    {
        key = ((KeyParameter)keyParam).getKey();
    }
    
    setupKey(key);
    setupIV(iv);
    init();
  }
  
  private void init()
  {
    I = 16;
  }

  public String getAlgorithmName()
  {
    return "Rabbit";
  }

  public byte returnByte(byte in)
  {
    return in ^= getByte();
  }

  public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff) throws DataLengthException
  {
    for (int i = 0; i < len; i++)
    {
        out[outOff + i] = (byte)(in[inOff + i] ^ getByte());
    }
    return len;
  }
  
  private byte getByte()
  {
    if (I == KEYSTREAM_LENGTH)
    {
      nextBlock();
      I = 0;
    }
    return S[I++];
  }
  
  private void nextState()
  {
    long temp;
    for (int i = 0; i < 8; i++)
    {
        temp = (C[i] & 0xFFFFFFFFl) + (A[i] & 0xFFFFFFFFl) + B;
        B = (int) (temp >>> 32);
        C[i] = (int) (temp & 0xFFFFFFFFl);
    }
    
    for (int i = 0; i < 8; i++)
    {
        G[i] = g(X[i], C[i]);
    }
    
    X[0] = G[0] + rotateLeft(G[7], 16) + rotateLeft(G[6], 16);
    X[1] = G[1] + rotateLeft(G[0],  8) + G[7];
    X[2] = G[2] + rotateLeft(G[1], 16) + rotateLeft(G[0], 16);
    X[3] = G[3] + rotateLeft(G[2],  8) + G[1];
    X[4] = G[4] + rotateLeft(G[3], 16) + rotateLeft(G[2], 16);
    X[5] = G[5] + rotateLeft(G[4],  8) + G[3];
    X[6] = G[6] + rotateLeft(G[5], 16) + rotateLeft(G[4], 16);
    X[7] = G[7] + rotateLeft(G[6],  8) + G[5];
  }

  private static int g(int u, int v)
  {
      long square = u + v & 0xFFFFFFFFl;
      square *= square;
      return (int)(square ^ square >>> 32);
  }

/**
 * Left circular bit shift
 * @param value
 * @param shift
 * @return
 */
  private static int rotateLeft(int value, int shift)
  {
      return value << shift | value >>> (32 - shift);
  }
}
