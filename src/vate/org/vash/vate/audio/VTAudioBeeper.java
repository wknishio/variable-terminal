package org.vash.vate.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import org.vash.vate.VT;

public class VTAudioBeeper
{
  private static final int SAMPLE_SIZE_IN_BITS = 16;
  private static final int SAMPLE_RATE = 16000;
  
  private static final byte[] createSinWaveBuffer(int freq, int ms, double vol)
  {
    int samples = (int)((ms * SAMPLE_RATE) / 1000d);
    int sampleBytes = SAMPLE_SIZE_IN_BITS / 8;
    byte[] output = new byte[samples * sampleBytes];
    double factor = ((Math.PI * 2) * freq) / SAMPLE_RATE;
    double sine;
    for (int i = 0; i < samples; i++)
    {
      sine = Math.sin(factor * i);
      short sample = (short)(Short.MAX_VALUE * sine * vol);
      output[(i * sampleBytes)] = (byte)((sample & 0xFF));
      output[(i * sampleBytes) + 1] = (byte)((sample & 0xFF00) >> 8);
    }
    return output;
  }
  
  //private static final DrainSourceDataline drainer = new DrainSourceDataline();
  
  private static final class DrainSourceDataline implements Runnable
  {
    private SourceDataLine sdl;
    private byte[] data;
    private int lineBufferSize = 0;
    //private int chunkSize;
    
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
        //t.printStackTrace();
      }
      
      try
      {
        sdl.close();
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      
      sdl = null;
    }
    
    public void run()
    {
      try
      {
        int written = 0;
        int remaining = data.length;
        int cycle = 0;
        
        sdl.start();
        
        //long start = System.currentTimeMillis();
        
        while (remaining > 0)
        {
          cycle = sdl.write(data, written, Math.min(lineBufferSize, remaining));
          written += cycle;
          remaining -= cycle;
        }
        
        sdl.drain();
        
        //long end = System.currentTimeMillis();
        //System.out.println("beep:" + (end - start));
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      
      close();
    }
  }
    
  private static synchronized final boolean toneBlocking(int hz, int msecs, double vol)
  {
    try
    {
      SourceDataLine sdl = openSourceDataLine();
      if (sdl == null)
      {
        return false;
      }
      float level = sdl.getLevel();
      //System.out.println("level:" + level);
      if (level == -1)
      {
        
      }
      else
      {
        
      }
      byte[] buf = createSinWaveBuffer(hz, msecs, vol);
      DrainSourceDataline drainer = new DrainSourceDataline();
      drainer.configure(sdl);
      drainer.setData(buf);
      drainer.run();
      return true;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return false;
  }
  
  private static synchronized final boolean toneThreaded(int hz, int msecs, double vol)
  {
    try
    {
      SourceDataLine sdl = openSourceDataLine();
      if (sdl == null)
      {
        return false;
      }
      float level = sdl.getLevel();
      //System.out.println("level:" + level);
      if (level == -1)
      {
        
      }
      else
      {
        
      }
      byte[] buf = createSinWaveBuffer(hz, msecs, vol);
      DrainSourceDataline drainer = new DrainSourceDataline();
      drainer.configure(sdl);
      drainer.setData(buf);
      drainer.start();
      return true;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return false;
  }
  
  private static final SourceDataLine openSourceDataLine()
  {
    SourceDataLine sdl = null;
    try
    {
      AudioFormat af = new AudioFormat((float) SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, 1, true, false);
      sdl = AudioSystem.getSourceDataLine(af);
      //sdl.open();
      sdl.open(af, (int) ((af.getSampleRate() / 1000) * (af.getSampleSizeInBits() / 8)) * af.getChannels() * VT.VT_AUDIO_LINE_PLAYBACK_BUFFER_MILLISECONDS);
      return sdl;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
  
  public static synchronized final boolean beep(int freq, int dur, boolean block)
  {
    if (block)
    {
      return toneBlocking(freq, dur, 0.5);
    }
    else
    {
      return toneThreaded(freq, dur, 0.5);
    }
  }
}
