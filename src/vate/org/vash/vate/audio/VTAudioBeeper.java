package org.vash.vate.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;

public class VTAudioBeeper
{
  //private static final int SAMPLE_SIZE_IN_BITS = 8;
  //private static final int SAMPLE_RATE = 8000;
  
  private static final byte[] createSinWaveBuffer(int sampleRate, int sampleSizeBits, int freq, int msecs, double vol)
  {
    int samples = (int) ((msecs * sampleRate) / 1000d);
    int sampleBytes = sampleSizeBits / 8;
    byte[] output = new byte[samples * sampleBytes];
    double factor = ((Math.PI * 2) * freq) / sampleRate;
    //final double period = (double) sampleRate / freq;
    //final double twice_pi = 2.0 * Math.PI;
    //double sine;
    if (sampleBytes == 2)
    {
      for (int i = 0; i < samples; i++)
      {
        //sine = Math.sin(factor * i);
        short sample = (short) ((Short.MAX_VALUE * Math.sin(factor * i)) * vol);
        output[(i * sampleBytes)] = (byte) ((sample & 0xFF));
        output[(i * sampleBytes) + 1] = (byte) ((sample & 0xFF00) >> 8);
      }
    }
    if (sampleBytes == 1)
    {
      for (int i = 0; i < samples; i++)
      {
        //sine = Math.sin(factor * i);
        output[i] = (byte) ((Byte.MAX_VALUE * Math.sin(factor * i)) * vol);
      }
    }
    return output;
  }
  
  // private static final DrainSourceDataline drainer = new
  // DrainSourceDataline();
  
  private static final class DrainSourceDataline implements Runnable
  {
    private SourceDataLine sdl;
    private byte[] data;
    private int lineBufferSize = 0;
    // private int chunkSize;
    
    private DrainSourceDataline()
    {
      
    }
    
    public void configure(SourceDataLine sdl)
    {
      this.sdl = sdl;
      this.lineBufferSize = sdl.getBufferSize();
    }
    
    public void setData(byte[] data)
    {
      this.data = data;
    }
    
    public void start()
    {
      Thread drain = new Thread(this);
      drain.setName(this.getClass().getSimpleName());
      drain.setDaemon(true);
      drain.start();
    }
    
    public void close()
    {
      try
      {
        sdl.stop();
      }
      catch (Throwable t)
      {
        // t.printStackTrace();
      }
      
      try
      {
        sdl.close();
      }
      catch (Throwable t)
      {
        // t.printStackTrace();
      }
      
      sdl = null;
    }
    
    public void run()
    {
      try
      {
        int written = 0;
        int remaining = data.length;
        //int available = 0;
        int cycle = 1;
        
        sdl.start();
        
        // long start = System.currentTimeMillis();
        
        while (remaining > 0 && cycle > 0)
        {
          cycle = sdl.write(data, written, Math.min(lineBufferSize, remaining));
          written += cycle;
          remaining -= cycle;
        }
        
        sdl.drain();
        
        // long end = System.currentTimeMillis();
        // System.out.println("beep:" + (end - start));
      }
      catch (Throwable t)
      {
        // t.printStackTrace();
      }
      
      close();
    }
  }
  
  private static final boolean toneBlocking(int sampleRate, int sampleSizeBits, int freq, int msecs, double vol)
  {
    try
    {
      SourceDataLine sdl = openSourceDataLine(sampleRate, sampleSizeBits);
      if (sdl == null)
      {
        return false;
      }
      float level = sdl.getLevel();
      // System.out.println("level:" + level);
      if (level == -1)
      {
        
      }
      else
      {
        
      }
      byte[] buf = createSinWaveBuffer(sampleRate, sampleSizeBits, freq, msecs, vol);
      DrainSourceDataline drainer = new DrainSourceDataline();
      drainer.configure(sdl);
      drainer.setData(buf);
      drainer.run();
      return true;
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
    }
    return false;
  }
  
  private static final boolean toneThreaded(int sampleRate, int sampleSizeBits, int freq, int msecs, double vol)
  {
    try
    {
      SourceDataLine sdl = openSourceDataLine(sampleRate, sampleSizeBits);
      if (sdl == null)
      {
        return false;
      }
      float level = sdl.getLevel();
      // System.out.println("level:" + level);
      if (level == -1)
      {
        
      }
      else
      {
        
      }
      byte[] buf = createSinWaveBuffer(sampleRate, sampleSizeBits, freq, msecs, vol);
      DrainSourceDataline drainer = new DrainSourceDataline();
      drainer.configure(sdl);
      drainer.setData(buf);
      drainer.start();
      return true;
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
    }
    return false;
  }
  
  private static final boolean toneBlocking(int sampleRate, int sampleSizeBits, int freq, int msecs, double vol, SourceDataLine sdl)
  {
    try
    {
      if (sdl == null)
      {
        return false;
      }
      float level = sdl.getLevel();
      // System.out.println("level:" + level);
      if (level == -1)
      {
        
      }
      else
      {
        
      }
      byte[] buf = createSinWaveBuffer(sampleRate, sampleSizeBits, freq, msecs, vol);
      DrainSourceDataline drainer = new DrainSourceDataline();
      drainer.configure(sdl);
      drainer.setData(buf);
      drainer.run();
      return true;
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
    }
    return false;
  }
  
  private static final boolean toneThreaded(int sampleRate, int sampleSizeBits, int freq, int msecs, double vol, SourceDataLine sdl)
  {
    try
    {
      if (sdl == null)
      {
        return false;
      }
      float level = sdl.getLevel();
      // System.out.println("level:" + level);
      if (level == -1)
      {
        
      }
      else
      {
        
      }
      byte[] buf = createSinWaveBuffer(sampleRate, sampleSizeBits, freq, msecs, vol);
      DrainSourceDataline drainer = new DrainSourceDataline();
      drainer.configure(sdl);
      drainer.setData(buf);
      drainer.start();
      return true;
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
    }
    return false;
  }
  
  public static final SourceDataLine openSourceDataLine(int sampleRate, int sampleSizeBits)
  {
    SourceDataLine sdl = null;
    try
    {
      AudioFormat af = new AudioFormat((float) sampleRate, sampleSizeBits, 1, true, false);
      sdl = AudioSystem.getSourceDataLine(af);
      //sdl.open(af, (int) ((af.getSampleRate() / 1000) * (af.getSampleSizeInBits() / 8)) * af.getChannels() * VT.VT_AUDIO_LINE_PLAYBACK_BUFFER_MILLISECONDS);
      sdl.open(af);
      return sdl;
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
    }
    return null;
  }
  
  public static final SourceDataLine openSourceDataLine(int sampleRate, int sampleSizeBits, Info mixerInfo)
  {
    SourceDataLine sdl = null;
    try
    {
      AudioFormat af = new AudioFormat((float) sampleRate, sampleSizeBits, 1, true, false);
      sdl = AudioSystem.getSourceDataLine(af, mixerInfo);
      //sdl.open(af, (int) ((af.getSampleRate() / 1000) * (af.getSampleSizeInBits() / 8)) * af.getChannels() * VT.VT_AUDIO_LINE_PLAYBACK_BUFFER_MILLISECONDS);
      sdl.open(af);
      return sdl;
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
    }
    return null;
  }
  
  public static final boolean beep(int sampleRate, int sampleSizeBits, int freq, int msecs, boolean block)
  {
    if (block)
    {
      return toneBlocking(sampleRate, sampleSizeBits, freq, msecs, 0.5);
    }
    else
    {
      return toneThreaded(sampleRate, sampleSizeBits, freq, msecs, 0.5);
    }
  }
  
  public static final boolean beep(int sampleRate, int sampleSizeBits, int freq, int msecs, boolean block, SourceDataLine sdl)
  {
    if (block)
    {
      return toneBlocking(sampleRate, sampleSizeBits, freq, msecs, 0.5, sdl);
    }
    else
    {
      return toneThreaded(sampleRate, sampleSizeBits, freq, msecs, 0.5, sdl);
    }
  }
  
  public static final boolean beep(int sampleRate, int sampleSizeBits, int freq, int msecs, double vol, boolean block)
  {
    if (block)
    {
      return toneBlocking(sampleRate, sampleSizeBits, freq, msecs, vol);
    }
    else
    {
      return toneThreaded(sampleRate, sampleSizeBits, freq, msecs, vol);
    }
  }
  
  public static final boolean beep(int sampleRate, int sampleSizeBits, int freq, int msecs, double vol, boolean block, SourceDataLine sdl)
  {
    if (block)
    {
      return toneBlocking(sampleRate, sampleSizeBits, freq, msecs, vol, sdl);
    }
    else
    {
      return toneThreaded(sampleRate, sampleSizeBits, freq, msecs, vol, sdl);
    }
  }
}