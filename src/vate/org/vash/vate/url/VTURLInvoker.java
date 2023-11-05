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
import org.vash.vate.tls.TLSVerificationDisabler;

public class VTURLInvoker
{
  static
  {
    try
    {
      TLSVerificationDisabler.install();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void close(URLConnection connection, OutputStream output, InputStream input)
  {
    if (connection != null)
    {
      try
      {
        if (output != null)
        {
          output.close();
        }
      }
      catch (Throwable t)
      {
        
      }
      
      try
      {
        if (input != null)
        {
          input.close();
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
  
  public VTURLResult invokeURL(String urlString, int connectTimeout, int dataTimeout, OutputStream resultOutputStream)
  {
    return invokeURL(urlString, connectTimeout, dataTimeout, Proxy.NO_PROXY, null, null, null, resultOutputStream);
  }
  
  public VTURLResult invokeURL(String urlString, int connectTimeout, int dataTimeout, InputStream outputInputStream, OutputStream resultOutputStream)
  {
    return invokeURL(urlString, connectTimeout, dataTimeout, Proxy.NO_PROXY, null, null, outputInputStream, resultOutputStream);
  }
  
  public VTURLResult invokeURL(String urlString, int connectTimeout, int dataTimeout, String requestMethod, InputStream outputInputStream, OutputStream resultOutputStream)
  {
    return invokeURL(urlString, connectTimeout, dataTimeout, Proxy.NO_PROXY, null, requestMethod, outputInputStream, resultOutputStream);
  }
  
  public VTURLResult invokeURL(String urlString, int connectTimeout, int dataTimeout, Map<String, String> requestHeaders, InputStream outputInputStream, OutputStream resultOutputStream)
  {
    return invokeURL(urlString, connectTimeout, dataTimeout, Proxy.NO_PROXY, requestHeaders, null, outputInputStream, resultOutputStream);
  }
  
  public VTURLResult invokeURL(String urlString, int connectTimeout, int dataTimeout, Map<String, String> requestHeaders, String requestMethod, InputStream outputInputStream, OutputStream resultOutputStream)
  {
    return invokeURL(urlString, connectTimeout, dataTimeout, Proxy.NO_PROXY, requestHeaders, requestMethod, outputInputStream, resultOutputStream);
  }
  
  public VTURLResult invokeURL(String urlString, int connectTimeout, int dataTimeout, Proxy proxy, Map<String, String> requestHeaders, String requestMethod, InputStream outputInputStream, OutputStream resultOutputStream)
  {
    //System.setProperty("http.keepAlive", "false");
    final byte[] readBuffer = new byte[VT.VT_STANDARD_BUFFER_SIZE_BYTES];
    VTURLResult urlResult = new VTURLResult(-1, null, null, true);
    int readed = 1;
    URLConnection urlConnection = null;
    HttpURLConnection httpConnection = null;
    //Throwable error = null;
    OutputStream outputOutputStream = null;
    InputStream resultInputStream = null;
    boolean error = false;
    
    try
    {
      URL url = new URL(urlString);
      urlConnection = url.openConnection(proxy);
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
      
      if (outputInputStream != null)
      {
        try
        {
          urlConnection.setDoOutput(true);
          if (httpConnection != null)
          {
            httpConnection.setChunkedStreamingMode(0);
          }
          outputOutputStream = urlConnection.getOutputStream();
          while ((readed = outputInputStream.read(readBuffer)) > 0)
          {
            outputOutputStream.write(readBuffer, 0, readed);
            outputOutputStream.flush();
          }
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
        resultInputStream = new BufferedInputStream(urlConnection.getInputStream(), VT.VT_STANDARD_BUFFER_SIZE_BYTES);
      }
      catch (Throwable t)
      {
        error = true;
        if (httpConnection != null)
        {
          try
          {
            InputStream errorStream = httpConnection.getErrorStream();
            if (errorStream != null)
            {
              resultInputStream = new BufferedInputStream(errorStream, VT.VT_STANDARD_BUFFER_SIZE_BYTES);
            }
          }
          catch (Throwable e)
          {
            
          }
        }
      }
      
      if (resultInputStream != null)
      {
        try
        {
          while ((readed = resultInputStream.read(readBuffer)) > 0)
          {
            resultOutputStream.write(readBuffer, 0, readed);
            resultOutputStream.flush();
          }
        }
        catch (Throwable t)
        {
          
        }
      }
      
      Map<String, List<String>> headers = urlConnection.getHeaderFields();
      
      if (httpConnection != null)
      {
        try
        {
          code = httpConnection.getResponseCode();
        }
        catch (Throwable t)
        {
          error = true;
        }
        
        try
        {
          response = httpConnection.getResponseMessage();
        }
        catch (Throwable t)
        {
          error = true;
          response = t.getMessage();
        }
      }
      
      urlResult = new VTURLResult(code, response, headers, error);
    }
    catch (Throwable e)
    {
      urlResult = new VTURLResult(-1, null, null, true);
    }
    finally
    {
      if (urlConnection != null)
      {
        close(urlConnection, outputOutputStream, resultInputStream);
      }
    }
    return urlResult;
  }
}