package org.vate.audio;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;
import org.concentus.OpusException;
import org.concentus.OpusMode;
import org.concentus.OpusSignal;
import org.vate.VT;
import org.vate.stream.endian.VTLittleEndianOutputStream;
import org.xiph.speex.SpeexEncoder;

public class VTAudioCapturer
{
  private volatile boolean running = false;
  private ExecutorService threads;
  private AudioFormat audioFormat;
  private Map<String, VTAudioCapturerThread> lines = Collections.synchronizedMap(new HashMap<String, VTAudioCapturerThread>());
  private VTAudioSystem system;

  public VTAudioCapturer(VTAudioSystem system, ExecutorService threads)
  {
    this.system = system;
    this.threads = threads;
  }

  public boolean initialize(AudioFormat audioFormat)
  {
    int sampleRate = (int) audioFormat.getSampleRate();
    if (sampleRate != 8000 && sampleRate != 16000 && sampleRate != 32000 && sampleRate != 24000 && sampleRate != 48000)
    {
      return false;
    }
    this.audioFormat = audioFormat;
    return true;
  }

  private TargetDataLine searchTargetDataLine(AudioFormat audioFormat, Mixer.Info info, int bufferedMilliseconds)
  {
    TargetDataLine line = null;
    if (info == null)
    {
      try
      {
        line = AudioSystem.getTargetDataLine(audioFormat);
        // line.open(audioFormat);
        line.open(audioFormat, (int) ((audioFormat.getSampleRate() / 1000) * (audioFormat.getSampleSizeInBits() / 8)) * audioFormat.getChannels() * bufferedMilliseconds);
        return line;
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
      }
      Mixer.Info[] mixers = AudioSystem.getMixerInfo();
      for (Mixer.Info mixer : mixers)
      {
        try
        {
          line = AudioSystem.getTargetDataLine(audioFormat, mixer);
          // line.open(audioFormat);
          line.open(audioFormat, (int) ((audioFormat.getSampleRate() / 1000) * (audioFormat.getSampleSizeInBits() / 8)) * audioFormat.getChannels() * bufferedMilliseconds);
          return line;
        }
        catch (Throwable e)
        {
          // e.printStackTrace();
        }
      }
    }
    else
    {
      try
      {
        line = AudioSystem.getTargetDataLine(audioFormat, info);
        // line.open(audioFormat);
        line.open(audioFormat, (int) ((audioFormat.getSampleRate() / 1000) * (audioFormat.getSampleSizeInBits() / 8)) * audioFormat.getChannels() * bufferedMilliseconds);
        return line;
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
      }
    }
    return line;
  }

  private class VTAudioCapturerThread implements Runnable
  {
    private int encodedFrameSize = 0;
    private int decodedFrameSize = 0;
    private final byte[] inputBuffer;
    // private short[] inputBufferShort;
    private final byte[] outputBuffer;
    private final Queue<VTLittleEndianOutputStream> streams;
    private final String id;
    private TargetDataLine line;
    // private final VTLittleEndianOutputStream out;
    private SpeexEncoder speex;
    private OpusEncoder opus;
    private int frameSize;
    private int lineBufferSize;
    private int codec;
    // private int availableFrames;
    // private int maxFrameSize;
    // private int currentFrameSize;

    private VTAudioCapturerThread(VTLittleEndianOutputStream out, TargetDataLine line, String id, int codec, int frameMilliseconds)
    {
      this.codec = codec;
      this.streams = new ConcurrentLinkedQueue<VTLittleEndianOutputStream>();
      // this.out = out;
      this.line = line;
      this.id = id;
      // System.out.println("line.getBufferSize:" + line.getBufferSize());
      this.lineBufferSize = line.getBufferSize();
      int sampleRate = (int) audioFormat.getSampleRate();
      this.frameSize = ((sampleRate / 1000) * (audioFormat.getSampleSizeInBits() / 8)) * audioFormat.getChannels() * frameMilliseconds;
      // this.maxFrameSize = minFrameSize * 3;
      this.inputBuffer = new byte[lineBufferSize];
      // this.inputBufferShort = new short[lineBufferSize / 2];
      this.outputBuffer = new byte[frameSize];
      this.speex = new SpeexEncoder();
      if (sampleRate == 8000)
      {
        this.speex.init(0, 7, sampleRate, audioFormat.getChannels());
        this.speex.getEncoder().setComplexity(3);
        try
        {
          opus = new OpusEncoder(sampleRate, audioFormat.getChannels(), OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY);
          opus.setForceMode(OpusMode.MODE_CELT_ONLY);
          opus.setSignalType(OpusSignal.OPUS_SIGNAL_MUSIC);
          opus.setEnableAnalysis(false);
          opus.setUseVBR(false);
          opus.setComplexity(5);
          opus.setBitrate(16000);
        }
        catch (Throwable t)
        {

        }
      }
      else if (sampleRate == 16000)
      {
        this.speex.init(1, 7, sampleRate, audioFormat.getChannels());
        this.speex.getEncoder().setComplexity(3);
        try
        {
          opus = new OpusEncoder(sampleRate, audioFormat.getChannels(), OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY);
          opus.setForceMode(OpusMode.MODE_CELT_ONLY);
          opus.setSignalType(OpusSignal.OPUS_SIGNAL_MUSIC);
          opus.setEnableAnalysis(false);
          opus.setUseVBR(false);
          opus.setComplexity(5);
          opus.setBitrate(32000);
        }
        catch (Throwable t)
        {

        }
      }
      else if (sampleRate == 32000)
      {
        this.codec = VT.VT_AUDIO_CODEC_SPEEX;
        this.speex.init(2, 7, sampleRate, audioFormat.getChannels());
        this.speex.getEncoder().setComplexity(3);
      }
      else if (sampleRate == 24000)
      {
        this.codec = VT.VT_AUDIO_CODEC_OPUS;
        try
        {
          opus = new OpusEncoder(sampleRate, audioFormat.getChannels(), OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY);
          opus.setForceMode(OpusMode.MODE_CELT_ONLY);
          opus.setSignalType(OpusSignal.OPUS_SIGNAL_MUSIC);
          opus.setEnableAnalysis(false);
          opus.setUseVBR(false);
          opus.setComplexity(5);
          opus.setBitrate(32000);
        }
        catch (Throwable t)
        {

        }
      }
      else if (sampleRate == 48000)
      {
        this.codec = VT.VT_AUDIO_CODEC_OPUS;
        try
        {
          opus = new OpusEncoder(sampleRate, audioFormat.getChannels(), OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY);
          opus.setForceMode(OpusMode.MODE_CELT_ONLY);
          opus.setSignalType(OpusSignal.OPUS_SIGNAL_MUSIC);
          opus.setEnableAnalysis(false);
          opus.setUseVBR(false);
          opus.setComplexity(5);
          opus.setBitrate(64000);
        }
        catch (Throwable t)
        {

        }
      }

      addOutput(out);
      // System.out.println("VTAudioCapturerThread started");
      threads.execute(this);
    }

    public boolean addOutput(VTLittleEndianOutputStream out)
    {
      return streams.add(out);
    }

    public void close()
    {
      for (VTLittleEndianOutputStream out : streams)
      {
        try
        {
          out.close();
        }
        catch (Throwable e)
        {

        }
      }
      streams.clear();
      if (line != null)
      {
        try
        {
          line.flush();
        }
        catch (Throwable e)
        {

        }
        try
        {
          line.stop();
        }
        catch (Throwable e)
        {

        }
        try
        {
          // line.stop();
          line.close();
        }
        catch (Throwable e)
        {
          // e.printStackTrace();
        }
      }
      synchronized (lines)
      {
        lines.remove(id);
        if (lines.size() == 0)
        {
          running = false;
          system.stop();
        }
      }
    }

    public final void loopOpus() throws OpusException
    {
      int offset = 0;
      while (running)
      {
        decodedFrameSize = line.read(inputBuffer, 0, Math.max(1, line.available() / (frameSize)) * (frameSize));
        if (decodedFrameSize > 0 && streams.size() > 0)
        {
          for (offset = 0; offset < decodedFrameSize; offset += (frameSize))
          {
            // encodedFrameSize = opus.encode(inputBuffer, offset, (frameSize >> 1),
            // outputBuffer, 0, (frameSize));
            encodedFrameSize = opus.encode(inputBuffer, offset, (frameSize), outputBuffer, 0, (frameSize));
            for (VTLittleEndianOutputStream out : streams)
            {
              try
              {
                out.writeUnsignedShort(encodedFrameSize);
                out.write(outputBuffer, 0, encodedFrameSize);
                out.flush();
              }
              catch (Throwable e)
              {
                // e.printStackTrace();
                try
                {
                  out.close();
                }
                catch (Throwable e1)
                {
                  // e1.printStackTrace();
                }
                streams.remove(out);
                if (streams.size() == 0)
                {
                  close();
                }
              }
            }
          }
        }
      }
    }

    public final void loopSpeex()
    {
      int offset = 0;
      while (running)
      {
        decodedFrameSize = line.read(inputBuffer, 0, Math.max(1, line.available() / frameSize) * frameSize);
        if (decodedFrameSize > 0 && streams.size() > 0)
        {
          for (offset = 0; offset < decodedFrameSize; offset += frameSize)
          {
            speex.processData(inputBuffer, offset, frameSize);
            encodedFrameSize = speex.getProcessedData(outputBuffer, 0);
            for (VTLittleEndianOutputStream out : streams)
            {
              try
              {
                out.writeUnsignedShort(encodedFrameSize);
                out.write(outputBuffer, 0, encodedFrameSize);
                out.flush();
              }
              catch (Throwable e)
              {
                // e.printStackTrace();
                try
                {
                  out.close();
                }
                catch (Throwable e1)
                {
                  // e1.printStackTrace();
                }
                streams.remove(out);
                if (streams.size() == 0)
                {
                  close();
                }
              }
            }
          }
        }
      }
    }

    public void run()
    {
      // System.out.println("started capture");
      // System.out.println("started running:" + running);
      line.flush();
      line.start();

      try
      {
        if (codec == VT.VT_AUDIO_CODEC_OPUS)
        {
          loopOpus();
        }
        else
        {
          loopSpeex();
        }
      }
      catch (Throwable e)
      {
        // running = false;
        close();
        // e.printStackTrace();
      }
      close();
    }

  }

  public boolean addOutputStream(OutputStream out, Mixer.Info info, int lineMilliseconds, int codec, int frameMilliseconds)
  {
    // Mixer mixer = AudioSystem.getMixer(info);
    String id = "";
    if (info != null)
    {
      id = info.getName() + info.getDescription() + info.getVendor() + info.getVersion();
    }
    else
    {

    }
    if (lines.get(id) == null)
    {
      TargetDataLine line = searchTargetDataLine(audioFormat, info, lineMilliseconds);
      if (line == null)
      {
        return false;
      }
      VTLittleEndianOutputStream stream = new VTLittleEndianOutputStream(new BufferedOutputStream(out, 512));
      lines.put(id, new VTAudioCapturerThread(stream, line, id, codec, frameMilliseconds));
    }
    else
    {
      VTLittleEndianOutputStream stream = new VTLittleEndianOutputStream(new BufferedOutputStream(out, 512));
      lines.get(id).addOutput(stream);
    }
    return true;
  }

  public void close()
  {
    this.running = false;
    synchronized (lines)
    {
      for (Entry<String, VTAudioCapturerThread> entry : lines.entrySet())
      {
        try
        {
          entry.getValue().close();
        }
        catch (Throwable e)
        {

        }
      }
    }
  }

  public void setRunning(boolean running)
  {
    this.running = running;
  }

  public boolean isRunning()
  {
    return running;
  }
}