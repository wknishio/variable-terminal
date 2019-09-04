package org.vate.network.url;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.vate.stream.array.VTByteArrayOutputStream;

public class VTURLInvoker
{
	private int readed;
	private final byte[] readBuffer = new byte[1024 * 64];
	private VTByteArrayOutputStream dataBuffer = new VTByteArrayOutputStream();
	
	public byte[] getURLData(String URL, byte[] outputData, int outputOffset, int outputLength) throws Exception
	{
		return getURLData(URL, Proxy.NO_PROXY, outputData, outputOffset, outputLength);
	}
	
	public byte[] getURLData(String urlString, Proxy proxy, byte[] outputData, int outputOffset, int outputLength)
	{
		dataBuffer.reset();
		readed = 0;
		URLConnection connection = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try
		{
			URL url = new URL(urlString);
			connection = url.openConnection(proxy);
			connection.setDoInput(true);
			if (outputData != null && outputData.length > 0 && outputLength > 0)
			{
				connection.setDoOutput(true);
				outputStream = connection.getOutputStream();
				outputStream.write(outputData, outputOffset, outputLength);
				outputStream.flush();
			}
			inputStream = connection.getInputStream();
			while ((readed = inputStream.read(readBuffer)) >= 0)
			{
				dataBuffer.write(readBuffer, 0, readed);
			}
		}
		catch (Throwable e)
		{
			
		}
		finally
		{
			if (connection != null)
			{
				try
				{
					if (connection instanceof HttpURLConnection)
					{
						((HttpURLConnection) connection).disconnect();
					}
				}
				catch (Throwable e)
				{
					
				}
			}
			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch (Throwable e)
				{
					
				}
			}
			if (outputStream != null)
			{
				try
				{
					outputStream.close();
				}
				catch (Throwable e)
				{
					
				}
			}
		}
		return dataBuffer.toByteArray();
	}
}