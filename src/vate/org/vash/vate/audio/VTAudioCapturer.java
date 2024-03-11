package org.vash.vate.audio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.vash.vate.VT;
import org.vash.vate.stream.endian.VTLittleEndianByteArrayInputOutputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.filter.VTBufferedOutputStream;
import org.xiph.speex.SpeexEncoder;

public class VTAudioCapturer
{
  private boolean running = false;
  private ExecutorService threads;
  private AudioFormat audioFormat;
//  private Map<String, VTAudioCapturerThread> lines = Collections.synchronizedMap(new LinkedHashMap<String, VTAudioCapturerThread>());
  private Map<String, VTAudioCapturerThread> lines = new LinkedHashMap<String, VTAudioCapturerThread>();
  private VTAudioSystem system;
  private List<Runnable> scheduled = new ArrayList<Runnable>();
  
  public VTAudioCapturer(VTAudioSystem system, ExecutorService threads)
  {
    this.system = system;
    this.threads = threads;
  }
  
  public boolean initialize(AudioFormat audioFormat)
  {
    int sampleRate = (int) audioFormat.getSampleRate();
    if (sampleRate != 8000 && sampleRate != 12000 && sampleRate != 16000 && sampleRate != 32000 && sampleRate != 24000 && sampleRate != 48000)
    {
      return false;
    }
    this.audioFormat = audioFormat;
    return true;
  }
  
  public TargetDataLine searchTargetDataLine(AudioFormat audioFormat, Mixer.Info info, int bufferedMilliseconds)
  {
    TargetDataLine line = null;
    if (info == null)
    {
      try
      {
        line = AudioSystem.getTargetDataLine(audioFormat);
        // line.open();
        if (bufferedMilliseconds > 0)
        {
          line.open(audioFormat, (int) ((audioFormat.getSampleRate() / 1000) * (audioFormat.getSampleSizeInBits() / 8)) * audioFormat.getChannels() * bufferedMilliseconds);
        }
        else
        {
          line.open(audioFormat);
        }
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
          // line.open();
          // line.open(audioFormat);
          if (bufferedMilliseconds > 0)
          {
            line.open(audioFormat, (int) ((audioFormat.getSampleRate() / 1000) * (audioFormat.getSampleSizeInBits() / 8)) * audioFormat.getChannels() * bufferedMilliseconds);
          }
          else
          {
            line.open(audioFormat);
          }
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
        // line.open();
        // line.open(audioFormat);
        if (bufferedMilliseconds > 0)
        {
          line.open(audioFormat, (int) ((audioFormat.getSampleRate() / 1000) * (audioFormat.getSampleSizeInBits() / 8)) * audioFormat.getChannels() * bufferedMilliseconds);
        }
        else
        {
          line.open(audioFormat);
        }
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
    private VTLittleEndianByteArrayInputOutputStream frameStream = new VTLittleEndianByteArrayInputOutputStream(VT.VT_STANDARD_BUFFER_SIZE_BYTES);
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
      this.lineBufferSize = line.getBufferSize();
      // System.out.println("capture.line.getBufferSize:" +
      // line.getBufferSize());
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
        this.speex.getEncoder().setQuality(8);
        this.speex.getEncoder().setComplexity(5);
        this.speex.getEncoder().setVbr(false);
        this.speex.getEncoder().setVad(false);
        this.speex.getEncoder().setDtx(false);
        try
        {
          this.opus = new OpusEncoder(sampleRate, audioFormat.getChannels(), OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY);
          this.opus.setForceMode(OpusMode.MODE_CELT_ONLY);
          this.opus.setSignalType(OpusSignal.OPUS_SIGNAL_MUSIC);
          this.opus.setEnableAnalysis(false);
          this.opus.setUseVBR(false);
          this.opus.setUseDTX(false);
          this.opus.setComplexity(9);
          this.opus.setBitrate(16000);
        }
        catch (Throwable t)
        {
          
        }
      }
      else if (sampleRate == 12000)
      {
        this.codec = VT.VT_AUDIO_CODEC_OPUS;
        try
        {
          this.opus = new OpusEncoder(sampleRate, audioFormat.getChannels(), OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY);
          this.opus.setForceMode(OpusMode.MODE_CELT_ONLY);
          this.opus.setSignalType(OpusSignal.OPUS_SIGNAL_MUSIC);
          this.opus.setEnableAnalysis(false);
          this.opus.setUseVBR(false);
          this.opus.setUseDTX(false);
          this.opus.setComplexity(9);
          this.opus.setBitrate(24000);
        }
        catch (Throwable t)
        {
          
        }
      }
      else if (sampleRate == 16000)
      {
        this.speex.init(1, 7, sampleRate, audioFormat.getChannels());
        this.speex.getEncoder().setQuality(8);
        this.speex.getEncoder().setComplexity(5);
        this.speex.getEncoder().setVbr(false);
        this.speex.getEncoder().setVad(false);
        this.speex.getEncoder().setDtx(false);
        try
        {
          this.opus = new OpusEncoder(sampleRate, audioFormat.getChannels(), OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY);
          this.opus.setForceMode(OpusMode.MODE_CELT_ONLY);
          this.opus.setSignalType(OpusSignal.OPUS_SIGNAL_MUSIC);
          this.opus.setEnableAnalysis(false);
          this.opus.setUseVBR(false);
          this.opus.setUseDTX(false);
          this.opus.setComplexity(9);
          this.opus.setBitrate(32000);
        }
        catch (Throwable t)
        {
          
        }
      }
      else if (sampleRate == 24000)
      {
        this.codec = VT.VT_AUDIO_CODEC_OPUS;
        try
        {
          this.opus = new OpusEncoder(sampleRate, audioFormat.getChannels(), OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY);
          this.opus.setForceMode(OpusMode.MODE_CELT_ONLY);
          this.opus.setSignalType(OpusSignal.OPUS_SIGNAL_MUSIC);
          this.opus.setEnableAnalysis(false);
          this.opus.setUseVBR(false);
          this.opus.setUseDTX(false);
          this.opus.setComplexity(9);
          this.opus.setBitrate(32000);
        }
        catch (Throwable t)
        {
          
        }
      }
      else if (sampleRate == 32000)
      {
        this.codec = VT.VT_AUDIO_CODEC_SPEEX;
        this.speex.init(2, 7, sampleRate, audioFormat.getChannels());
        this.speex.getEncoder().setQuality(8);
        this.speex.getEncoder().setComplexity(5);
        this.speex.getEncoder().setVbr(false);
        this.speex.getEncoder().setVad(false);
        this.speex.getEncoder().setDtx(false);
      }
      else if (sampleRate == 48000)
      {
        this.codec = VT.VT_AUDIO_CODEC_OPUS;
        try
        {
          this.opus = new OpusEncoder(sampleRate, audioFormat.getChannels(), OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY);
          this.opus.setForceMode(OpusMode.MODE_CELT_ONLY);
          this.opus.setSignalType(OpusSignal.OPUS_SIGNAL_MUSIC);
          this.opus.setEnableAnalysis(false);
          this.opus.setUseVBR(false);
          this.opus.setUseDTX(false);
          this.opus.setComplexity(9);
          this.opus.setBitrate(64000);
        }
        catch (Throwable t)
        {
          
        }
      }
      
      addOutput(out);
      // System.out.println("VTAudioCapturerThread started");
      if (running)
      {
        threads.execute(this);
      }
      else
      {
        scheduled.add(this);
      }
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
      lines.remove(id);
      if (lines.size() == 0)
      {
        running = false;
        system.stop();
      }
    }
    
    public final void loopOpus() throws OpusException, IOException
    {
      int offset = 0;
      int readSize = 0;
      while (running)
      {
        if (readSize == 0)
        {
          readSize = Math.max(VT.VT_AUDIO_LINE_CAPTURE_BUFFER_MILLISECONDS / (VT.VT_AUDIO_CODEC_FRAME_MILLISECONDS * 2), line.available() / frameSize) * frameSize;
        }
        else
        {
          readSize = Math.max(1, line.available() / frameSize) * frameSize;
        }
        decodedFrameSize = line.read(inputBuffer, 0, readSize);
        if (decodedFrameSize > 0 && streams.size() > 0)
        {
          frameStream.reset();
          for (offset = 0; offset < decodedFrameSize; offset += (frameSize))
          {
            encodedFrameSize = opus.encode(inputBuffer, offset, (frameSize), outputBuffer, 0, (frameSize));
            frameStream.writeUnsignedShort(encodedFrameSize);
            frameStream.write(outputBuffer, 0, encodedFrameSize);
          }
          for (VTLittleEndianOutputStream out : streams.toArray(new VTLittleEndianOutputStream[] {}))
          {
            try
            {
              out.write(frameStream.getBuffer(), 0, frameStream.getOutputCount());
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
    
    public final void loopSpeex() throws IOException
    {
      int offset = 0;
      int readSize = 0;
      while (running)
      {
        if (readSize == 0)
        {
          readSize = Math.max(VT.VT_AUDIO_LINE_CAPTURE_BUFFER_MILLISECONDS / (VT.VT_AUDIO_CODEC_FRAME_MILLISECONDS * 2), line.available() / frameSize) * frameSize;
        }
        else
        {
          readSize = Math.max(1, line.available() / frameSize) * frameSize;
        }
        decodedFrameSize = line.read(inputBuffer, 0, readSize);
        if (decodedFrameSize > 0 && streams.size() > 0)
        {
          frameStream.reset();
          for (offset = 0; offset < decodedFrameSize; offset += frameSize)
          {
            speex.processData(inputBuffer, offset, frameSize);
            encodedFrameSize = speex.getProcessedData(outputBuffer, 0);
            frameStream.writeUnsignedShort(encodedFrameSize);
            frameStream.write(outputBuffer, 0, encodedFrameSize);
          }
          for (VTLittleEndianOutputStream out : streams.toArray(new VTLittleEndianOutputStream[] {}))
          {
            try
            {
              out.write(frameStream.getBuffer(), 0, frameStream.getOutputCount());
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
    
    public void run()
    {
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
        //e.printStackTrace();
      }
      close();
    }
  }
  
  public boolean addOutputStream(OutputStream out, Mixer.Info info, TargetDataLine line, int codec, int frameMilliseconds)
  {
    // Mixer mixer = AudioSystem.getMixer(info);
    String id = "";
    if (info != null)
    {
      id = info.getName() + info.getDescription() + info.getVendor() + info.getVersion();
    }
    else
    {
      id = line.getLineInfo().toString();
    }
    VTAudioCapturerThread capturer = lines.get(id);
    if (capturer == null)
    {
      VTLittleEndianOutputStream stream = new VTLittleEndianOutputStream(new VTBufferedOutputStream(out, VT.VT_REDUCED_BUFFER_SIZE_BYTES, true));
      lines.put(id, new VTAudioCapturerThread(stream, line, id, codec, frameMilliseconds));
    }
    else
    {
      VTLittleEndianOutputStream stream = new VTLittleEndianOutputStream(new VTBufferedOutputStream(out, VT.VT_REDUCED_BUFFER_SIZE_BYTES, true));
      capturer.addOutput(stream);
    }
    return true;
  }
  
  public void close()
  {
    this.running = false;
    for (VTAudioCapturerThread capturerThread : lines.values().toArray(new VTAudioCapturerThread[] {}))
    {
      try
      {
        capturerThread.close();
      }
      catch (Throwable e)
      {
        
      }
    }
    lines.clear();
    scheduled.clear();
  }
  
  public void start()
  {
    if (!isRunning())
    {
      setRunning(true);
      if (scheduled.size() > 0)
      {
        for (Runnable runnable : scheduled)
        {
          threads.execute(runnable);
        }
        scheduled.clear();
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