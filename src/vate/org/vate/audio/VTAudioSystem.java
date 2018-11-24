package org.vate.audio;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;

public class VTAudioSystem
{
	// private TargetDataLine targetDataLine;
	// private SourceDataLine sourceDataLine;
	// private Thread captureThread;
	
	private VTAudioCapturer capture;
	private VTAudioPlayer play;
	
	public VTAudioSystem(ExecutorService threads)
	{
		capture = new VTAudioCapturer(this, threads);
		play = new VTAudioPlayer(this, threads);
	}
	
	public boolean addAudioPlay(InputStream in, Mixer.Info info, int lineMilliseconds, int codec, int frameMilliseconds)
	{
		return play.addInputStream(in, info, lineMilliseconds, codec, frameMilliseconds);
	}
	
	public boolean addAudioCapture(OutputStream out, Mixer.Info info, int lineMilliseconds, int codec, int frameMilliseconds)
	{
		return capture.addOutputStream(out, info, lineMilliseconds, codec, frameMilliseconds);
	}
	
	public boolean initialize(AudioFormat audioFormat)
	{
		capture.setRunning(true);
		play.setRunning(true);
		return capture.initialize(audioFormat) && play.initialize(audioFormat);
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