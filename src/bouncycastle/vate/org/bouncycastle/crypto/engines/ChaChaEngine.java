package vate.org.bouncycastle.crypto.engines;

import vate.org.bouncycastle.crypto.CipherParameters;
import vate.org.bouncycastle.crypto.SkippingStreamCipher;
import vate.org.bouncycastle.crypto.params.KeyParameter;
import vate.org.bouncycastle.crypto.params.ParametersWithIV;
import vate.org.bouncycastle.util.Integers;
import vate.org.bouncycastle.util.Pack;
import vate.org.bouncycastle.util.Strings;

/**
 * Implementation of Daniel J. Bernstein's ChaCha stream cipher.
 */
public class ChaChaEngine implements SkippingStreamCipher
{
  public final static int DEFAULT_ROUNDS = 20;

  /** Constants */
  private final static int STATE_SIZE = 16; // 16, 32 bit ints = 64 bytes

  private final static int[] TAU_SIGMA = Pack.littleEndianToInt(Strings.toByteArray("expand 16-byte k" + "expand 32-byte k"), 0, 8);

  protected final void packTauOrSigma(int keyLength, int[] state, int stateOffset)
  {
      int tsOff = (keyLength - 16) / 4;
      state[stateOffset    ] = TAU_SIGMA[tsOff    ];
      state[stateOffset + 1] = TAU_SIGMA[tsOff + 1];
      state[stateOffset + 2] = TAU_SIGMA[tsOff + 2];
      state[stateOffset + 3] = TAU_SIGMA[tsOff + 3];
  }

  /** @deprecated */
  protected final static byte[]
      sigma = Strings.toByteArray("expand 32-byte k"),
      tau   = Strings.toByteArray("expand 16-byte k");

  protected final int rounds;

  /*
   * variables to hold the state of the engine
   * during encryption and decryption
   */
  private int         index = 0;
  protected final int[]     engineState = new int[STATE_SIZE]; // state
  protected final int[]     x = new int[STATE_SIZE] ; // internal buffer
  private final byte[]      keyStream   = new byte[STATE_SIZE * 4]; // expanded state, 64 bytes
    /**
     * Creates a 20 rounds ChaCha engine.
     */
    public ChaChaEngine()
    {
      this(DEFAULT_ROUNDS);
    }

    /**
     * Creates a ChaCha engine with a specific number of rounds.
     * @param rounds the number of rounds (must be an even number).
     */
    public ChaChaEngine(int rounds)
    {
      if (rounds <= 0 || (rounds & 1) != 0)
      {
          throw new IllegalArgumentException("'rounds' must be a positive, even number");
      }
      this.rounds = rounds;
    }
    
    public final void init(
        boolean             forEncryption, 
        CipherParameters     params)
    {
        /* 
        * Salsa20 encryption and decryption is completely
        * symmetrical, so the 'forEncryption' is 
        * irrelevant. (Like 90% of stream ciphers)
        */

        if (!(params instanceof ParametersWithIV))
        {
            throw new IllegalArgumentException(getAlgorithmName() + " Init parameters must include an IV");
        }

        ParametersWithIV ivParams = (ParametersWithIV) params;

        byte[] iv = ivParams.getIV();
        if (iv == null || iv.length != getNonceSize())
        {
            throw new IllegalArgumentException(getAlgorithmName() + " requires exactly " + getNonceSize()
                    + " bytes of IV");
        }

        CipherParameters keyParam = ivParams.getParameters();
        if (keyParam == null)
        {
//            if (!initialised)
//            {
//                throw new IllegalStateException(getAlgorithmName() + " KeyParameter can not be null for first initialisation");
//            }

            setKey(null, iv);
        }
        else if (keyParam instanceof KeyParameter)
        {
            setKey(((KeyParameter)keyParam).getKey(), iv);
        }
        else
        {
            throw new IllegalArgumentException(getAlgorithmName() + " Init parameters must contain a KeyParameter (or null for re-init)");
        }

        reset();

        //initialised = true;
    }
    
    public final void reset()
    {
        index = 0;
        resetLimitCounter();
        resetCounter();

        generateKeyStream(keyStream);
    }
    
    public String getAlgorithmName()
    {
        return "ChaCha" + rounds;
    }
    
    protected static final int getNonceSize()
    {
        return 8;
    }

    protected final void advanceCounter(long diff)
    {
        int hi = (int)(diff >>> 32);
        int lo = (int)diff;

        if (hi > 0)
        {
            engineState[13] += hi;
        }

        int oldState = engineState[12];

        engineState[12] += lo;

        if (oldState != 0 && engineState[12] < oldState)
        {
            engineState[13]++;
        }
    }

    protected final void advanceCounter()
    {
        if (++engineState[12] == 0)
        {
            ++engineState[13];
        }
    }

    protected final void retreatCounter(long diff)
    {
        int hi = (int)(diff >>> 32);
        int lo = (int)diff;

        if (hi != 0)
        {
            if ((engineState[13] & 0xffffffffL) >= (hi & 0xffffffffL))
            {
                engineState[13] -= hi;
            }
//            else
//            {
//                throw new IllegalStateException("attempt to reduce counter past zero.");
//            }
        }

        if ((engineState[12] & 0xffffffffL) >= (lo & 0xffffffffL))
        {
            engineState[12] -= lo;
        }
        else
        {
            if (engineState[13] != 0)
            {
                --engineState[13];
                engineState[12] -= lo;
            }
//            else
//            {
//                throw new IllegalStateException("attempt to reduce counter past zero.");
//            }
        }
    }

    protected final void retreatCounter()
    {
//        if (engineState[12] == 0 && engineState[13] == 0)
//        {
//            throw new IllegalStateException("attempt to reduce counter past zero.");
//        }

        if (--engineState[12] == -1)
        {
            --engineState[13];
        }
    }

    protected final long getCounter()
    {
        return ((long)engineState[13] << 32) | (engineState[12] & 0xffffffffL);
    }

    protected final void resetCounter()
    {
        engineState[12] = engineState[13] = 0;
    }
    
    private final void resetLimitCounter()
    {
//        cW0 = 0;
//        cW1 = 0;
//        cW2 = 0;
    }
    
    public final long skip(long numberOfBytes)
    {
        if (numberOfBytes >= 0)
        {
            long remaining = numberOfBytes;

            if (remaining >= 64)
            {
                long count = remaining / 64;

                advanceCounter(count);

                remaining -= count * 64;
            }

            int oldIndex = index;

            index = (index + (int)remaining) & 63;

            if (index < oldIndex)
            {
                advanceCounter();
            }
        }
        else
        {
            long remaining = -numberOfBytes;

            if (remaining >= 64)
            {
                long count = remaining / 64;

                retreatCounter(count);

                remaining -= count * 64;
            }

            for (long i = 0; i < remaining; i++)
            {
                if (index == 0)
                {
                    retreatCounter();
                }

                index = (index - 1) & 63;
            }
        }

        generateKeyStream(keyStream);

        return numberOfBytes;
    }

    public final long seekTo(long position)
    {
        reset();

        return skip(position);
    }

    public final long getPosition()
    {
        return getCounter() * 64 + index;
    }

    protected final void setKey(byte[] keyBytes, byte[] ivBytes)
    {
        if (keyBytes != null)
        {
            if ((keyBytes.length != 16) && (keyBytes.length != 32))
            {
                throw new IllegalArgumentException(getAlgorithmName() + " requires 128 bit or 256 bit key");
            }

            packTauOrSigma(keyBytes.length, engineState, 0);

            // Key
            Pack.littleEndianToInt(keyBytes, 0, engineState, 4, 4);
            Pack.littleEndianToInt(keyBytes, keyBytes.length - 16, engineState, 8, 4);
        }

        // IV
        Pack.littleEndianToInt(ivBytes, 0, engineState, 14, 2);
    }

    protected final void generateKeyStream(byte[] output)
    {
        chachaCore(rounds, engineState, x);
        Pack.intToLittleEndian(x, output, 0);
    }
    
    public final byte returnByte(byte in)
    {
        byte out = (byte)(keyStream[index]^in);
        index = (index + 1) & 63;

        if (index == 0)
        {
            advanceCounter();
            generateKeyStream(keyStream);
        }

        return out;
    }

    public final int processBytes(
        byte[]     in, 
        int     inOff, 
        int     len, 
        byte[]     out, 
        int     outOff)
    {
        for (int i = 0; i < len; i++)
        {
            out[i + outOff] = (byte)(keyStream[index] ^ in[i + inOff]);
            index = (index + 1) & 63;

            if (index == 0)
            {
                advanceCounter();
                generateKeyStream(keyStream);
            }
        }

        return len;
    }
    /**
     * ChaCha function
     *
     * @param   input   input data
     */    
    public static final void chachaCore(int rounds, int[] input, int[] x)
    {
        int x00 = input[ 0];
        int x01 = input[ 1];
        int x02 = input[ 2];
        int x03 = input[ 3];
        int x04 = input[ 4];
        int x05 = input[ 5];
        int x06 = input[ 6];
        int x07 = input[ 7];
        int x08 = input[ 8];
        int x09 = input[ 9];
        int x10 = input[10];
        int x11 = input[11];
        int x12 = input[12];
        int x13 = input[13];
        int x14 = input[14];
        int x15 = input[15];

        for (int i = rounds; i > 0; i -= 2)
        {
            x00 += x04; x12 = Integers.rotateLeft(x12 ^ x00, 16);
            x08 += x12; x04 = Integers.rotateLeft(x04 ^ x08, 12);
            x00 += x04; x12 = Integers.rotateLeft(x12 ^ x00, 8);
            x08 += x12; x04 = Integers.rotateLeft(x04 ^ x08, 7);
            x01 += x05; x13 = Integers.rotateLeft(x13 ^ x01, 16);
            x09 += x13; x05 = Integers.rotateLeft(x05 ^ x09, 12);
            x01 += x05; x13 = Integers.rotateLeft(x13 ^ x01, 8);
            x09 += x13; x05 = Integers.rotateLeft(x05 ^ x09, 7);
            x02 += x06; x14 = Integers.rotateLeft(x14 ^ x02, 16);
            x10 += x14; x06 = Integers.rotateLeft(x06 ^ x10, 12);
            x02 += x06; x14 = Integers.rotateLeft(x14 ^ x02, 8);
            x10 += x14; x06 = Integers.rotateLeft(x06 ^ x10, 7);
            x03 += x07; x15 = Integers.rotateLeft(x15 ^ x03, 16);
            x11 += x15; x07 = Integers.rotateLeft(x07 ^ x11, 12);
            x03 += x07; x15 = Integers.rotateLeft(x15 ^ x03, 8);
            x11 += x15; x07 = Integers.rotateLeft(x07 ^ x11, 7);
            x00 += x05; x15 = Integers.rotateLeft(x15 ^ x00, 16);
            x10 += x15; x05 = Integers.rotateLeft(x05 ^ x10, 12);
            x00 += x05; x15 = Integers.rotateLeft(x15 ^ x00, 8);
            x10 += x15; x05 = Integers.rotateLeft(x05 ^ x10, 7);
            x01 += x06; x12 = Integers.rotateLeft(x12 ^ x01, 16);
            x11 += x12; x06 = Integers.rotateLeft(x06 ^ x11, 12);
            x01 += x06; x12 = Integers.rotateLeft(x12 ^ x01, 8);
            x11 += x12; x06 = Integers.rotateLeft(x06 ^ x11, 7);
            x02 += x07; x13 = Integers.rotateLeft(x13 ^ x02, 16);
            x08 += x13; x07 = Integers.rotateLeft(x07 ^ x08, 12);
            x02 += x07; x13 = Integers.rotateLeft(x13 ^ x02, 8);
            x08 += x13; x07 = Integers.rotateLeft(x07 ^ x08, 7);
            x03 += x04; x14 = Integers.rotateLeft(x14 ^ x03, 16);
            x09 += x14; x04 = Integers.rotateLeft(x04 ^ x09, 12);
            x03 += x04; x14 = Integers.rotateLeft(x14 ^ x03, 8);
            x09 += x14; x04 = Integers.rotateLeft(x04 ^ x09, 7);
        }

        x[ 0] = x00 + input[ 0];
        x[ 1] = x01 + input[ 1];
        x[ 2] = x02 + input[ 2];
        x[ 3] = x03 + input[ 3];
        x[ 4] = x04 + input[ 4];
        x[ 5] = x05 + input[ 5];
        x[ 6] = x06 + input[ 6];
        x[ 7] = x07 + input[ 7];
        x[ 8] = x08 + input[ 8];
        x[ 9] = x09 + input[ 9];
        x[10] = x10 + input[10];
        x[11] = x11 + input[11];
        x[12] = x12 + input[12];
        x[13] = x13 + input[13];
        x[14] = x14 + input[14];
        x[15] = x15 + input[15];
    }
}
