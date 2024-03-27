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
  // private TargetDataLine targetDataLine;
  // private SourceDataLine sourceDataLine;
  // private Thread captureThread;
  
  private VTAudioCapturer capture;
  private VTAudioPlayer play;
  
  public VTAudioSystem(ExecutorService executor)
  {
    capture = new VTAudioCapturer(this, executor);
    play = new VTAudioPlayer(this, executor);
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