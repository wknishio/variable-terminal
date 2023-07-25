package org.vash.vate.network.url;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.vash.vate.VT;
import org.vash.vate.network.tls.TLSVerificationDisabler;

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
  
  private Collection<URLConnection> connections = Collections.synchronizedCollection(new ArrayList<URLConnection>());
  
  public void close()
  {
    try
    {
      for (URLConnection connection : connections.toArray(new URLConnection[] { }))
      {
        close(connection);
      }
      connections.clear();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void close(URLConnection connection)
  {
    if (connection != null)
    {
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
      
      try
      {
        OutputStream output = connection.getOutputStream();
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
        InputStream input = connection.getInputStream();
        if (input != null)
        {
          input.close();
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
    VTURLResult urlResult = new VTURLResult(-1, null, null, null);
    int readed = 1;
    URLConnection connection = null;
    HttpURLConnection httpConnection = null;
    try
    {
      URL url = new URL(urlString);
      connection = url.openConnection(proxy);
      connections.add(connection);
      connection.setConnectTimeout(connectTimeout);
      connection.setReadTimeout(dataTimeout);
      connection.setAllowUserInteraction(false);
      connection.setUseCaches(false);
      connection.setDoInput(true);
      connection.setDoOutput(true);
      if (connection instanceof HttpURLConnection)
      {
        httpConnection = (HttpURLConnection) connection;
        if (requestMethod != null)
        {
          httpConnection.setRequestMethod(requestMethod);
        }
      }
      if (requestHeaders != null)
      {
        for (Entry<String, String> header : requestHeaders.entrySet())
        {
          connection.setRequestProperty(header.getKey(), header.getValue());
        }
      }
      
      if (outputInputStream != null)
      {
        try
        {
          OutputStream outputOutputStream = connection.getOutputStream();
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
      int code = -1;
      String response = null;
      connection.connect();
      if (httpConnection != null)
      {
        code = httpConnection.getResponseCode();
        response = httpConnection.getResponseMessage();
      }
      Map<String, List<String>> headers = connection.getHeaderFields();
      InputStream resultInputStream = connection.getInputStream();
      while ((readed = resultInputStream.read(readBuffer)) > 0)
      {
        resultOutputStream.write(readBuffer, 0, readed);
        resultOutputStream.flush();
      }
      urlResult = new VTURLResult(code, response, headers, null);
    }
    catch (Throwable e)
    {
      urlResult = new VTURLResult(-1, null, null, e);
    }
    finally
    {
      if (connection != null)
      {
        close(connection);
        connections.remove(connection);
      }
    }
    return urlResult;
  }
}