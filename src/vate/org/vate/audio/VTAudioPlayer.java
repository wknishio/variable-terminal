package org.vate.audio;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.concentus.OpusDecoder;
import org.concentus.OpusException;
import org.vate.VT;
import org.vate.stream.endian.VTLittleEndianInputStream;
import org.xiph.speex.SpeexDecoder;

public class VTAudioPlayer
{
  private final Queue<VTLittleEndianInputStream> streams;
  // private final int maximumJitterLevel = 10;
  private volatile boolean running = false;
  private ExecutorService threads;
  private AudioFormat audioFormat;
  private VTAudioSystem system;

  public VTAudioPlayer(VTAudioSystem system, ExecutorService threads)
  {
    this.streams = new ConcurrentLinkedQueue<VTLittleEndianInputStream>();
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

  private SourceDataLine searchSourceDataLine(AudioFormat audioFormat, Mixer.Info info, int bufferedMilliseconds)
  {
    SourceDataLine line = null;

    if (info != null)
    {
      try
      {
        line = AudioSystem.getSourceDataLine(audioFormat, info);
        // line.open(audioFormat);
        line.open(audioFormat, (int) ((audioFormat.getSampleRate() / 1000) * (audioFormat.getSampleSizeInBits() / 8)) * audioFormat.getChannels() * bufferedMilliseconds);
        return line;
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
      }
    }
    else
    {
      try
      {
        line = AudioSystem.getSourceDataLine(audioFormat);
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
          line = AudioSystem.getSourceDataLine(audioFormat, mixer);
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
    return line;
  }

  private class VTAudioPlayerThread implements Runnable
  {
    // private int sampleRate;
    private int encodedFrameSize = 0;
    private int decodedFrameSize = 0;
    private final byte[] inputBuffer;
    private final byte[] outputBuffer;
    private final VTLittleEndianInputStream in;
    private SpeexDecoder speex;
    private OpusDecoder opus;
    private SourceDataLine line;
    private int frameSize;
    private int codec;
    // private int lineBufferSize;

    private VTAudioPlayerThread(VTLittleEndianInputStream in, SourceDataLine line, int codec, int frameMilliseconds)
    {
      this.codec = codec;
      this.in = in;
      this.line = line;
      // this.lineBufferSize = line.getBufferSize();
      int sampleRate = (int) audioFormat.getSampleRate();
      this.frameSize = ((sampleRate / 1000) * (audioFormat.getSampleSizeInBits() / 8)) * audioFormat.getChannels() * frameMilliseconds;
      this.inputBuffer = new byte[frameSize];
      this.outputBuffer = new byte[frameSize];

      this.speex = new SpeexDecoder();
      if (sampleRate == 8000)
      {
        this.speex.init(0, sampleRate, audioFormat.getChannels(), true);
        try
        {
          this.opus = new OpusDecoder(sampleRate, audioFormat.getChannels());
        }
        catch (Throwable t)
        {

        }
      }
      else if (sampleRate == 16000)
      {
        this.speex.init(1, sampleRate, audioFormat.getChannels(), true);
        try
        {
          this.opus = new OpusDecoder(sampleRate, audioFormat.getChannels());
        }
        catch (Throwable t)
        {

        }
      }
      else if (sampleRate == 32000)
      {
        this.codec = VT.VT_AUDIO_CODEC_SPEEX;
        this.speex.init(2, sampleRate, audioFormat.getChannels(), true);
      }
      else if (sampleRate == 24000)
      {
        this.codec = VT.VT_AUDIO_CODEC_OPUS;
        try
        {
          this.opus = new OpusDecoder(sampleRate, audioFormat.getChannels());
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
          this.opus = new OpusDecoder(sampleRate, audioFormat.getChannels());
        }
        catch (Throwable t)
        {

        }
      }
      else
      {

      }
    }

    public final void loopOpus() throws IOException, OpusException
    {
      while (running)
      {
        encodedFrameSize = in.readUnsignedShort();
        in.readFully(inputBuffer, 0, encodedFrameSize);
        opus.decode(inputBuffer, 0, encodedFrameSize, outputBuffer, 0, (frameSize), false);
        // opus.decode(inputBuffer, 0, encodedFrameSize, outputBuffer, 0, (frameSize >>
        // 1), false);
        line.write(outputBuffer, 0, (frameSize));
        // line.write(outputBuffer, 0, (frameSize >> 1));
      }
    }

    public final void loopSpeex() throws IOException
    {
      while (running)
      {
        encodedFrameSize = in.readUnsignedShort();
        in.readFully(inputBuffer, 0, encodedFrameSize);
        speex.processData(inputBuffer, 0, encodedFrameSize);
        decodedFrameSize = speex.getProcessedData(outputBuffer, 0);
        line.write(outputBuffer, 0, decodedFrameSize);
      }
    }

    public void run()
    {
      // System.out.println("started play");
      // System.out.println("play running:" + running);
      line.flush();
      line.start();
      // line.flush();
      // bufferedFrameSize = 0;
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
        // e.printStackTrace();
      }
      // running = false;
      try
      {
        in.close();
      }
      catch (Throwable e)
      {

      }
      streams.remove(in);
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
        line.close();
      }
      catch (Throwable e)
      {

      }
      if (streams.size() == 0)
      {
        running = false;
        system.stop();
      }
      // System.out.println("stopped play");
    }
  }

  public boolean addInputStream(InputStream in, Mixer.Info info, int lineMilliseconds, int codec, int frameMilliseconds)
  {
    SourceDataLine line = searchSourceDataLine(audioFormat, info, lineMilliseconds);
    if (line == null)
    {
      return false;
    }
    /*
     * try { int data = in.read(); if (data != 1) { return false; } } catch
     * (Throwable e) { //e.printStackTrace(); return false; }
     */
    VTLittleEndianInputStream stream = new VTLittleEndianInputStream(in);
    streams.add(stream);
    threads.execute(new VTAudioPlayerThread(stream, line, codec, frameMilliseconds));
    return true;
  }

  public void close()
  {
    this.running = false;
    for (VTLittleEndianInputStream in : streams)
    {
      try
      {
        in.close();
      }
      catch (Throwable e)
      {

      }
    }
    streams.clear();
  }

  public boolean isRunning()
  {
    return running;
  }

  public void setRunning(boolean running)
  {
    this.running = running;
  }
}