package org.vash.vate.audio;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class VTAudioSystem
{
  public static final AudioFormat VT_AUDIO_FORMAT_DEFAULT;
  public static final AudioFormat VT_AUDIO_FORMAT_8000;
  public static final AudioFormat VT_AUDIO_FORMAT_16000;
  public static final AudioFormat VT_AUDIO_FORMAT_24000;
  public static final AudioFormat VT_AUDIO_FORMAT_32000;
  public static final AudioFormat VT_AUDIO_FORMAT_48000;
  
  static
  {
    int sampleSizeInBits = 16;
    int channels = 1;
    boolean signed = true;
    boolean bigEndian = false;
    VT_AUDIO_FORMAT_8000 = new AudioFormat(8000, sampleSizeInBits, channels, signed, bigEndian);
    VT_AUDIO_FORMAT_16000 = new AudioFormat(16000, sampleSizeInBits, channels, signed, bigEndian);
    VT_AUDIO_FORMAT_24000 = new AudioFormat(24000, sampleSizeInBits, channels, signed, bigEndian);
    VT_AUDIO_FORMAT_32000 = new AudioFormat(32000, sampleSizeInBits, channels, signed, bigEndian);
    VT_AUDIO_FORMAT_48000 = new AudioFormat(48000, sampleSizeInBits, channels, signed, bigEndian);
    VT_AUDIO_FORMAT_DEFAULT = VT_AUDIO_FORMAT_16000;
  }
  
  private VTAudioCapturer capture;
  private VTAudioPlayer play;
  
  public VTAudioSystem(ExecutorService executorService)
  {
    capture = new VTAudioCapturer(this, executorService);
    play = new VTAudioPlayer(this, executorService);
  }
  
  public SourceDataLine searchSourceDataLine(AudioFormat audioFormat, Mixer.Info info, int bufferedMilliseconds)
  {
    return play.searchSourceDataLine(audioFormat, info, bufferedMilliseconds);
  }
  
  public TargetDataLine searchTargetDataLine(AudioFormat audioFormat, Mixer.Info info, int bufferedMilliseconds)
  {
    return capture.searchTargetDataLine(audioFormat, info, bufferedMilliseconds);
  }
  
  public boolean addAudioPlay(InputStream in, Mixer.Info info, SourceDataLine line, int codec, int frameMilliseconds)
  {
    return play.addInputStream(in, info, line, codec, frameMilliseconds);
  }
  
  public boolean addAudioCapture(OutputStream out, Mixer.Info info, TargetDataLine line, int codec, int frameMilliseconds)
  {
    return capture.addOutputStream(out, info, line, codec, frameMilliseconds);
  }
  
  public boolean initialize(AudioFormat audioFormat)
  {
    // capture.setRunning(true);
    // play.setRunning(true);
    return capture.initialize(audioFormat) && play.initialize(audioFormat);
  }
  
  public void start()
  {
    capture.start();
    play.start();
  }
  
  public void stop()
  {
    capture.setRunning(false);
    play.setRunning(false);
    capture.close();
    play.close();
  }
  
  public boolean isRunning()
  {
    return capture.isRunning() && play.isRunning();
  }
}