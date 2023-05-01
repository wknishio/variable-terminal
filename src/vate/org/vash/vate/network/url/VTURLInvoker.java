package org.vash.vate.network.url;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
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
  
  private Collection<URLConnection> connections = Collections.synchronizedCollection(new LinkedList<URLConnection>());
  
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
    }
  }
  
  public VTURLResult invokeURL(String urlString, OutputStream resultOutputStream)
  {
    return invokeURL(urlString, Proxy.NO_PROXY, null, null, null, resultOutputStream);
  }
  
  public VTURLResult invokeURL(String urlString, InputStream outputInputStream, OutputStream resultOutputStream)
  {
    return invokeURL(urlString, Proxy.NO_PROXY, null, null, outputInputStream, resultOutputStream);
  }
  
  @SuppressWarnings("all")
  public VTURLResult invokeURL(String urlString, Proxy proxy, Map<String, String> requestHeaders, String requestMethod, InputStream outputInputStream, OutputStream resultOutputStream)
  {
    final byte[] readBuffer = new byte[VT.VT_BUFFER_STANDARD_SIZE_BYTES];
    // VTByteArrayOutputStream dataBuffer = new VTByteArrayOutputStream();
    VTURLResult urlResult = new VTURLResult(-1, null, null, null);
    // dataBuffer.reset();
    int readed = 0;
    // URLConnection urlConnection = null;
    // InputStream inputStream = null;
    // OutputStream outputStream = null;
    URLConnection.setDefaultAllowUserInteraction(false);
    URLConnection connection = null;
    HttpURLConnection httpConnection = null;
    try
    {
      URL url = new URL(urlString);
      connection = url.openConnection(proxy);
      if (connection != null)
      {
        connections.add(connection);
      }
      connection.setDefaultUseCaches(false);
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
      connection.setDoInput(true);
      if (outputInputStream != null)
      {
        connection.setDoOutput(true);
        try
        {
          OutputStream outputOutputStream = connection.getOutputStream();
          while ((readed = outputInputStream.read(readBuffer)) >= 0)
          {
            outputOutputStream.write(readBuffer);
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
//      for (Entry<String, List<String>> entry : headers.entrySet())
//      {
//        resultOutputStream.write((entry.getKey() + ":").getBytes());
//        for (String value : entry.getValue())
//        {
//          resultOutputStream.write((" " + value).getBytes());
//        }
//        resultOutputStream.write("\r\n".getBytes());
//        resultOutputStream.flush();
//      }
      InputStream resultInputStream = connection.getInputStream();
      while ((readed = resultInputStream.read(readBuffer)) >= 0)
      {
        resultOutputStream.write(readBuffer);
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