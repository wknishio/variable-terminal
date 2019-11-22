package org.vate.runtime;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VTRuntimeProcessOutputConsumer implements Runnable
{
	private static final int resultBufferSize = 1024 * 8;
	private boolean verbose;
	private volatile boolean running;
	private int readChars;
	private final char[] resultBuffer = new char[resultBufferSize];
	private InputStreamReader in;
	private BufferedWriter out;
	
	public VTRuntimeProcessOutputConsumer(InputStream in, BufferedWriter out, boolean verbose)
	{
		this.in = new InputStreamReader(in);
		this.out = out;
		this.verbose = verbose;
		this.running = true;
	}
	
	public void stop()
	{
		running = false;
		finalize();
	}
	
	public void finalize()
	{
		if (in != null)
		{
			try
			{
				in.close();
			}
			catch (Throwable e)
			{
				
			}
		}
	}
	
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		while (running)
		{
			try
			{
//				if (in.ready())
//				{
//					readChars = in.read(resultBuffer, 0, resultBufferSize);
//					if (readChars > 0 && running)
//					{
//						if (verbose)
//						{
//							out.write(resultBuffer, 0, readChars);
//							out.flush();
//						}
//					}
//					else
//					{
//						running = false;
//						break;
//					}
//				}
//				else
//				{
//					Thread.sleep(5);
//				}
				readChars = in.read(resultBuffer, 0, resultBufferSize);
				if (readChars > 0 && running)
				{
					if (verbose)
					{
						out.write(resultBuffer, 0, readChars);
						out.flush();
					}
				}
				else
				{
					running = false;
					break;
				}
			}
			catch (Throwable e)
			{
				running = false;
				break;
			}
		}
	}
}