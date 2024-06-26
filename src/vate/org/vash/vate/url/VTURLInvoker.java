package org.vash.vate.url;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.vash.vate.VT;
import org.vash.vate.tls.VTTLSVerificationDisabler;

public class VTURLInvoker
{
  static
  {
    try
    {
      VTTLSVerificationDisabler.install();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public static void close(URLConnection connection, OutputStream connectionOutputStream, InputStream connectionInputStream)
  {
    if (connection != null)
    {
      try
      {
        if (connectionOutputStream != null)
        {
          connectionOutputStream.close();
        }
      }
      catch (Throwable t)
      {
        
      }
      
      try
      {
        if (connectionInputStream != null)
        {
          connectionInputStream.close();
        }
      }
      catch (Throwable t)
      {
        
      }
      
      try
      {
        if (connection instanceof HttpURLConnection)
        {
          ((HttpURLConnection) connection).disconnect();
          return;
        }
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public static VTURLResult invokeURL(String urlString, Proxy proxy, int connectTimeout, int dataTimeout, Map<String, String> requestHeaders, String requestMethod, InputStream requestInputStream, OutputStream resultOutputStream)
  {
    //System.setProperty("http.keepAlive", "false");
    final byte[] readBuffer = new byte[VT.VT_STANDARD_BUFFER_SIZE_BYTES];
    VTURLResult urlResult = new VTURLResult(-1, null, null, true);
    int readed = 1;
    URLConnection urlConnection = null;
    HttpURLConnection httpConnection = null;
    //Throwable error = null;
    OutputStream connectionOutputStream = null;
    InputStream connectionInputStream = null;
    InputStream connectionErrorStream = null;
    boolean failed = false;
    
    try
    {
      URL url = new URL(urlString);
      if (proxy != null)
      {
        urlConnection = url.openConnection(proxy);
      }
      else
      {
        urlConnection = url.openConnection();
      }
      urlConnection.setConnectTimeout(connectTimeout);
      urlConnection.setReadTimeout(dataTimeout);
      urlConnection.setAllowUserInteraction(false);
      urlConnection.setUseCaches(false);
      //urlConnection.setIfModifiedSince(0);
      urlConnection.setDoInput(true);
      
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
      
      if (requestInputStream != null)
      {
        try
        {
          urlConnection.setDoOutput(true);
          if (httpConnection != null)
          {
            httpConnection.setChunkedStreamingMode(0);
          }
          connectionOutputStream = urlConnection.getOutputStream();
          while ((readed = requestInputStream.read(readBuffer)) > 0)
          {
            connectionOutputStream.write(readBuffer, 0, readed);
            connectionOutputStream.flush();
          }
          connectionOutputStream.close();
        }
        catch (Throwable e)
        {
          
        }
      }
      else
      {
        urlConnection.setDoOutput(false);
      }
      
      int code = -1;
      String response = null;
      
      if (httpConnection != null)
      {
        try
        {
          code = httpConnection.getResponseCode();
        }
        catch (Throwable t)
        {
          
        }
        
        try
        {
          response = httpConnection.getResponseMessage();
        }
        catch (Throwable t)
        {
          
        }
      }
      
//      try
//      {
//        urlConnection.connect();
//      }
//      catch (Throwable e)
//      {
//        error = e;
//      }
      
      try
      {
        InputStream inputStream = urlConnection.getInputStream();
        if (inputStream != null)
        {
          connectionInputStream = new BufferedInputStream(inputStream, VT.VT_STANDARD_BUFFER_SIZE_BYTES);
        }
      }
      catch (Throwable t)
      {
        
      }
      
      if (connectionInputStream != null)
      {
        try
        {
          while ((readed = connectionInputStream.read(readBuffer)) > 0)
          {
            if (resultOutputStream != null)
            {
              resultOutputStream.write(readBuffer, 0, readed);
              resultOutputStream.flush();
            }
          }
          connectionInputStream.close();
        }
        catch (Throwable t)
        {
          
        }
      }
      else
      {
        failed = true;
        if (httpConnection != null)
        {
          try
          {
            connectionErrorStream = httpConnection.getErrorStream();
            if (connectionErrorStream != null)
            {
              connectionInputStream = new BufferedInputStream(connectionErrorStream, VT.VT_STANDARD_BUFFER_SIZE_BYTES);
              while ((readed = connectionInputStream.read(readBuffer)) > 0)
              {
                resultOutputStream.write(readBuffer, 0, readed);
                resultOutputStream.flush();
              }
              connectionInputStream.close();
            }
          }
          catch (Throwable e)
          {
            
          }
        }
      }
      
      Map<String, List<String>> headers = urlConnection.getHeaderFields();
      
      urlResult = new VTURLResult(code, response, headers, failed);
    }
    catch (Throwable e)
    {
      urlResult = new VTURLResult(-1, null, null, true);
    }
    finally
    {
      if (urlConnection != null)
      {
        close(urlConnection, connectionOutputStream, connectionInputStream);
      }
    }
    return urlResult;
  }
}