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
import org.vate.VT;
import org.vate.network.ssl.SSLVerificationDisabler;
import org.vate.stream.array.VTByteArrayOutputStream;

public class VTURLInvoker
{
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
	
	public VTURLData getURLData(String urlString, byte[] outputData, int outputOffset, int outputLength) throws Exception
	{
		return getURLData(urlString, Proxy.NO_PROXY, outputData, outputOffset, outputLength, null, null);
	}
	
	public VTURLData getURLData(String urlString, Proxy proxy, byte[] outputData, int outputOffset, int outputLength, Map<String, String> requestHeaders, String requestMethod)
	{
		final byte[] readBuffer = new byte[VT.VT_DATA_BUFFER_SIZE];
		VTByteArrayOutputStream dataBuffer = new VTByteArrayOutputStream();
		
		VTURLData urlData = null;
		dataBuffer.reset();
		int readed = 0;
		URLConnection urlConnection = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		HttpURLConnection httpConnection = null;
		try
		{
			URL url = new URL(urlString);
			urlConnection = url.openConnection(proxy);
			urlConnection.setDefaultUseCaches(false);
			if (urlConnection instanceof HttpURLConnection)
			{
				httpConnection = (HttpURLConnection) urlConnection;
				if (requestMethod != null)
				{
					httpConnection.setRequestMethod(requestMethod);
				}
			}
			if (requestHeaders != null)
			{
				for (Entry<String, String> header : requestHeaders.entrySet())
				{
					urlConnection.setRequestProperty(header.getKey(), header.getValue());
				}
			}
			urlConnection.setDoInput(true);
			if (outputData != null && outputData.length > 0 && outputLength > 0)
			{
				urlConnection.setDoOutput(true);
				outputStream = urlConnection.getOutputStream();
				outputStream.write(outputData, outputOffset, outputLength);
				outputStream.flush();
			}
			inputStream = urlConnection.getInputStream();
			while ((readed = inputStream.read(readBuffer)) >= 0)
			{
				dataBuffer.write(readBuffer, 0, readed);
			}
			int code = -1;
			String response = null;
			byte[] data = dataBuffer.toByteArray();
			Map<String, List<String>> headers = urlConnection.getHeaderFields();
			if (httpConnection != null)
			{
				code = httpConnection.getResponseCode();
				response = httpConnection.getResponseMessage();
			}
			urlData = new VTURLData(code, data, response, headers);
			return urlData;
		}
		catch (Throwable e)
		{
			
		}
		finally
		{
			if (urlConnection != null)
			{
				try
				{
					if (urlConnection instanceof HttpURLConnection)
					{
						((HttpURLConnection) urlConnection).disconnect();
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
		return urlData;
	}
}