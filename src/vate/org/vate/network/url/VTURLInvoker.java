package org.vate.network.url;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.vate.network.ssl.SSLVerificationDisabler;
import org.vate.stream.array.VTByteArrayOutputStream;

public class VTURLInvoker
{
	private int readed;
	private final byte[] readBuffer = new byte[1024 * 64];
	private VTByteArrayOutputStream dataBuffer = new VTByteArrayOutputStream();
	
	static
	{
		try
		{
			SSLVerificationDisabler.install();
		}
		catch (Throwable t)
		{
			
		}
	}
	
	public VTURLData getURLData(String URL, byte[] outputData, int outputOffset, int outputLength) throws Exception
	{
		return getURLData(URL, Proxy.NO_PROXY, outputData, outputOffset, outputLength, null, null);
	}
	
	public VTURLData getURLData(String urlString, Proxy proxy, byte[] outputData, int outputOffset, int outputLength, Map<String, String> requestHeaders, String method)
	{
		VTURLData urldata = null;
		dataBuffer.reset();
		readed = 0;
		URLConnection connection = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		HttpURLConnection http = null;
		try
		{
			URL url = new URL(urlString);
			connection = url.openConnection(proxy);
			connection.setDefaultUseCaches(false);
			if (connection instanceof HttpURLConnection)
			{
				http = (HttpURLConnection)connection;
				if (method != null)
				{
					http.setRequestMethod(method);
				}
			}
			if (requestHeaders != null)
			{
				for (Entry<String, String> header : requestHeaders.entrySet())
				{
					connection.setRequestProperty(header.getKey(), header.getValue());
				}
			}
			connection.setDoInput(true);
			if (outputData != null && outputData.length > 0 && outputLength > 0)
			{
				connection.setDoOutput(true);
			}
			connection.connect();
			if (outputData != null && outputData.length > 0 && outputLength > 0)
			{
				outputStream = connection.getOutputStream();
				outputStream.write(outputData, outputOffset, outputLength);
				outputStream.flush();
			}
			inputStream = connection.getInputStream();
			while ((readed = inputStream.read(readBuffer)) >= 0)
			{
				dataBuffer.write(readBuffer, 0, readed);
			}
			
			int code = -1;
			String response = null;
			
			byte[] data = dataBuffer.toByteArray();
			Map<String, List<String>> headers = connection.getHeaderFields();
			
			if (http != null)
			{
				code = http.getResponseCode();
				response = http.getResponseMessage();
			}
			urldata = new VTURLData(code, data, response, headers);
			return urldata;
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
		return urldata;
	}
}