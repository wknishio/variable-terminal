package org.vash.nanohttpd;

import java.io.BufferedReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.vash.vate.VT;
import org.vash.vate.parser.VTConfigurationProperties;

/**
 * A simple, tiny, nicely embeddable HTTP 1.0 (partially 1.1) server in Java
 *
 * <p> NanoHTTPD version 1.27,
 * Copyright &copy; 2001,2005-2013 Jarno Elonen (elonen@iki.fi, http://iki.fi/elonen/)
 * and Copyright &copy; 2010 Konstantinos Togias (info@ktogias.gr, http://ktogias.gr)
 *
 * <p><b>Features + limitations: </b><ul>
 *
 *    <li> Only one Java file </li>
 *    <li> Java 1.1 compatible </li>
 *    <li> Released as open source, Modified BSD licence </li>
 *    <li> No fixed config files, logging, authorization etc. (Implement yourself if you need them.) </li>
 *    <li> Supports parameter parsing of GET and POST methods (+ rudimentary PUT support in 1.25) </li>
 *    <li> Supports both dynamic content and file serving </li>
 *    <li> Supports file upload (since version 1.2, 2010) </li>
 *    <li> Supports partial content (streaming)</li>
 *    <li> Supports ETags</li>
 *    <li> Never caches anything </li>
 *    <li> Doesn't limit bandwidth, request time or simultaneous connections </li>
 *    <li> Default code serves files and shows all HTTP parameters and headers</li>
 *    <li> File server supports directory listing, index.html and index.htm</li>
 *    <li> File server supports partial content (streaming)</li>
 *    <li> File server supports ETags</li>
 *    <li> File server does the 301 redirection trick for directories without '/'</li>
 *    <li> File server supports simple skipping for files (continue download) </li>
 *    <li> File server serves also very long files without memory overhead </li>
 *    <li> Contains a built-in list of most common mime types </li>
 *    <li> All header names are converted lowercase so they don't vary between browsers/clients </li>
 *
 * </ul>
 *
 * <p><b>Ways to use: </b><ul>
 *
 *    <li> Run as a standalone app, serves files and shows requests</li>
 *    <li> Subclass serve() and embed to your own program </li>
 *    <li> Call serveFile() from serve() with your own base directory </li>
 *
 * </ul>
 *
 * See the end of the source file for distribution license
 * (Modified BSD licence)
 */

public class VTNanoHTTPDProxySession implements Runnable
{
  public static final String
  HTTP_OK = "200 OK",
  HTTP_PARTIALCONTENT = "206 Partial Content",
  HTTP_RANGE_NOT_SATISFIABLE = "416 Requested Range Not Satisfiable",
  HTTP_REDIRECT = "301 Moved Permanently",
  HTTP_NOTMODIFIED = "304 Not Modified",
  HTTP_FORBIDDEN = "403 Forbidden",
  HTTP_NOTFOUND = "404 Not Found",
  HTTP_BADREQUEST = "400 Bad Request",
  HTTP_INTERNALERROR = "500 Internal Server Error",
  HTTP_NOTIMPLEMENTED = "501 Not Implemented",
  HTTP_REQUEST_TIMEOUT = "408 Request Timeout";

  public static final String HTTP_PROXY_AUTHENTICATION_REQUIRED = "407 Proxy Authentication Required";
  public static final String HTTP_ENTITY_TOO_LARGE = "413 Entity Too Large";
  
  private static Hashtable<String, String> theMimeTypes = new Hashtable<String, String>();
  static
  {
    StringTokenizer st = new StringTokenizer(
      "css    text/css "+
      "htm    text/html "+
      "html   text/html "+
      "xml    text/xml "+
      "txt    text/plain "+
      "asc    text/plain "+
      "gif    image/gif "+
      "jpg    image/jpeg "+
      "jpeg   image/jpeg "+
      "png    image/png "+
      "mp3    audio/mpeg "+
      "m3u    audio/mpeg-url " +
      "mp4    video/mp4 " +
      "ogv    video/ogg " +
      "flv    video/x-flv " +
      "mov    video/quicktime " +
      "swf    application/x-shockwave-flash " +
      "js   application/javascript "+
      "pdf    application/pdf "+
      "doc    application/msword "+
      "ogg    application/x-ogg "+
      "zip    application/octet-stream "+
      "exe    application/octet-stream "+
      "class    application/octet-stream " );
    while ( st.hasMoreTokens())
      theMimeTypes.put( st.nextToken(), st.nextToken());
  }
  
  private static int findHeaderEndSafe(final byte[] buf, int rlen)
  {
    int splitbyte = 0;
    while (splitbyte + 3 < rlen)
    {
      if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n')
        return splitbyte + 4;
      splitbyte++;
    }
    return 0;
  }
  
  public static int readHeaderSafe(InputStream in, byte[] header, final int bufsize)
  {
    try
    {
      boolean foundHeaderEnd = false;
      int splitbyte = 0;
      int rlen = 0;
      {
        int read = 1;
        while (read > 0 && rlen < bufsize)
        {
          read = in.read(header, rlen, bufsize - rlen);
          rlen += read;
          splitbyte = findHeaderEndSafe(header, rlen);
          if (splitbyte > 0)
          {
            foundHeaderEnd = true;
            break;
          }
        }
      }
      if (foundHeaderEnd)
      {
        return rlen;
      }
    }
    catch (Throwable t)
    {
      
    }
    return -1;
  }
  
  public static final String
  MIME_PLAINTEXT = "text/plain",
  MIME_HTML = "text/html",
  MIME_DEFAULT_BINARY = "application/octet-stream",
  MIME_XML = "text/xml";
  
  public class Response
  {
    /**
     * Default constructor: response = HTTP_OK, data = mime = 'null'
     */
    public Response()
    {
      this.status = HTTP_OK;
    }

    /**
     * Basic constructor.
     */
    public Response( String status, String mimeType, InputStream data )
    {
      this.status = status;
      this.mimeType = mimeType;
      this.data = data;
    }

    /**
     * Convenience method that makes an InputStream out of
     * given text.
     */
    public Response( String status, String mimeType, String txt )
    {
      this.status = status;
      this.mimeType = mimeType;
      try
      {
        this.data = new ByteArrayInputStream( txt.getBytes("ISO-8859-1"));
      }
      catch ( java.io.UnsupportedEncodingException uee )
      {
        uee.printStackTrace();
      }
    }

    /**
     * Adds given line to the header.
     */
    public void addHeader( String name, String value )
    {
      header.put( name, value );
    }

    /**
     * HTTP status code after processing, e.g. "200 OK", HTTP_OK
     */
    public String status;

    /**
     * MIME type of content, e.g. "text/html"
     */
    public String mimeType;

    /**
     * Data of the response, may be null.
     */
    public InputStream data;

    /**
     * Headers for the HTTP response. Use addHeader()
     * to add lines.
     */
    public Properties header = new VTConfigurationProperties();
    
    public boolean keepConnection = false;
  }
  
  public VTNanoHTTPDProxySession( Socket s, InputStream in, String username, String password)
  {
    mySocket = s;
    myIn = in;
    this.username = username;
    this.password = password;
    //Thread t = new Thread( this );
    //t.setDaemon( true );
    //t.start();
  }
  
  public void run()
  {
    try
    {
      //InputStream is = mySocket.getInputStream();
      InputStream in = myIn;
      if ( in == null) return;

      // Read the first 16384 bytes.
      // The full header should fit in here.
      // Apache's default header limit is 8KB.
      // Do NOT assume that a single read will get the entire header at once
      boolean foundHeaderEnd = false;
      final int bufsize = 16384;
      byte[] buf = new byte[bufsize];
      int splitbyte = 0;
      int rlen = 0;
      {
        int read = 1;
        while (read > 0 && rlen < bufsize)
        {
          read = in.read(buf, rlen, bufsize - rlen);
          rlen += read;
          splitbyte = findHeaderEnd(buf, rlen);
          if (splitbyte > 0)
          {
            foundHeaderEnd = true;
            break;
          }
        }
      }
      
      if (!foundHeaderEnd)
      {
        sendError(HTTP_ENTITY_TOO_LARGE, "ENTITY TOO LARGE: Request Too Large.");
      }

      // Create a BufferedReader for parsing the header.
      ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
      BufferedReader hin = new BufferedReader( new InputStreamReader( hbis, "ISO-8859-1" ));
      Properties pre = new VTConfigurationProperties();
      Properties parms = new VTConfigurationProperties();
      Properties headers = new VTConfigurationProperties();
      //Properties files = new VTConfigurationProperties();

      // Decode the header into parms and header java properties
      long size = decodeHeader(hin, pre, parms, headers);
      
      if (size == -1)
      {
        sendError( HTTP_BADREQUEST, "BAD REQUEST: Found Unexpected EOF." );
      }
      
      String method = pre.getProperty("method");
      String uri = pre.getProperty("uri");
      //String request = pre.getProperty("request");

      if (size == 0)
      {
        size = 0x7FFFFFFFFFFFFFFFL;
      }

      // Write the part of body already read to ByteArrayOutputStream f
      ByteArrayOutputStream h = new ByteArrayOutputStream();
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      if (splitbyte < rlen)
      {
        int already = rlen-splitbyte;
        b.write(buf, splitbyte, already);
      }

      h.write(buf, 0, splitbyte);
      // While Firefox sends on the first read all the data fitting
      // our buffer, Chrome and Opera send only the headers even if
      // there is data for the body. We do some magic here to find
      // out whether we have already consumed part of body, if we
      // have reached the end of the data to be sent or we should
      // expect the first byte of the body at the next read.
      if (splitbyte < rlen)
        size -= rlen-splitbyte;
      else if (splitbyte==0 || size == 0x7FFFFFFFFFFFFFFFl)
        size = 0;

      // Now read all the body and write it to f
      buf = new byte[16384];
      while ( rlen >= 0 && size > 0 )
      {
        rlen = in.read(buf, 0, (int) Math.min(16384, size));
        size -= rlen;
        if (rlen > 0)
        {
          b.write(buf, 0, rlen);
        }
      }
      // Get the raw body as a byte []
      byte[] body = b.toByteArray();
      byte[] header = h.toByteArray();
      
      
      // Ok, now do the serve()
      serve(uri, method, pre, headers, header, body, username, password, mySocket, in );
    }
    catch ( Exception e )
    {
      try
      {
        sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Exception: " + e.getMessage());
      }
      catch ( Throwable t ) 
      {
        
      }
    }
  }
  
  public void serve(String uri, String method, Properties pre, Properties headers, byte[] header, byte[] body, String username, String password, Socket clientSocket, InputStream clientInput) throws IOException, URISyntaxException, InterruptedException
  {
    if (!checkProxyAuthenticatedBasic(headers, username, password))
    {
      requireProxyAuthenticationBasic(uri);
      return;
    }
    if (method.equalsIgnoreCase("CONNECT"))
    {
      serveConnectRequest(uri, method, pre, headers, header, body, clientSocket, clientInput);
    }
    else
    {
      servePipeRequest(uri, method, pre, headers, header, body, clientSocket, clientInput);
    }
  }
  
  public boolean checkProxyAuthenticatedBasic(Properties headers, String username, String password) throws UnsupportedEncodingException
  {
    if (username == null || password == null)
    {
      return true;
    }
    String proxyAuthorization = null;
    for (Object headerName : headers.keySet())
    {
      if (headerName != null && headerName.toString().equalsIgnoreCase("Proxy-Authorization"))
      {
        proxyAuthorization = headers.getProperty(headerName.toString());
      }
    }
    if (proxyAuthorization != null)
    {
      String expected = "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes("ISO-8859-1"));
      return proxyAuthorization.equals(expected);
    }
    return false;
  }
  
  public void requireProxyAuthenticationBasic(String uri) throws UnsupportedEncodingException, InterruptedException
  {
    Response resp = new Response();
    resp.header.put("Proxy-Authenticate", "Basic");
    resp.status = HTTP_PROXY_AUTHENTICATION_REQUIRED;
    sendError(resp.status, null, resp.header, null);
  }
  
  public void serveConnectRequest(String uri, String method, Properties pre, Properties headers, byte[] header, byte[] body, Socket clientSocket, InputStream clientInput) throws URISyntaxException, IOException, InterruptedException
  {
    Response resp = new Response();
    resp.keepConnection = true;
    
    String host = "";
    int port = 80;
    
    try
    {
      int idxPort = uri.indexOf(':');
      if (idxPort >= 0)
      {
        host = uri.substring(0, idxPort);
        port = Integer.valueOf(uri.substring(idxPort + 1));
      }
      else
      {
        host = uri;
      }
    }
    catch (Throwable t)
    {
      
    }
    
    InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
    
    Socket remoteSocket = new Socket();
    remoteSocket.connect(remoteAddress);
    remoteSocket.setTcpNoDelay(true);
    //remoteSocket.setSoLinger(true, 5);
    remoteSocket.setSoTimeout(VT.VT_TIMEOUT_NETWORK_CONNECTION_MILLISECONDS);
    
    InputStream remoteInput = remoteSocket.getInputStream();
    OutputStream remoteOutput = remoteSocket.getOutputStream();
    OutputStream clientOutput = clientSocket.getOutputStream();
    
    sendResponse(resp.status, "");
    
    pipeSockets(clientSocket, remoteSocket, clientInput, clientOutput, remoteInput, remoteOutput);
  }
  
  public void servePipeRequest(String uri, String method, Properties pre, Properties headers, byte[] header, byte[] body, Socket clientSocket, InputStream clientInput) throws IOException, URISyntaxException, InterruptedException
  {
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    for (Object headerName : headers.keySet().toArray(new Object[] {}))
    {
      if (headerName != null && headerName.toString().equalsIgnoreCase("Proxy-Authorization"))
      {
        headers.remove(headerName);
      }
    }
    
    String host = "";
    int port = 80;
    String path = "/";
    
    try
    {
      URI url = new URI(uri);
      host = url.getHost();
      port = url.getPort();
      if (port == -1)
      {
        port = 80;
      }
      path = url.getPath();
      String line = (method + " " + path + " HTTP/1.1\r\n");
      data.write(line.getBytes("ISO-8859-1"));
    }
    catch (URISyntaxException e)
    {
      int idxPort = uri.indexOf(':');
      int idxPath = uri.indexOf('/');
      if (idxPath >= 0)
      {
        String hostPort = uri.substring(0, idxPath);
        path = uri.substring(idxPath + 1);
        idxPort = hostPort.indexOf(':');
        if (idxPort >= 0)
        {
          host = hostPort.substring(0, idxPort);
          port = Integer.valueOf(uri.substring(idxPort + 1));
        }
        String line = (method + " " + path + " HTTP/1.1\r\n");
        data.write(line.getBytes("ISO-8859-1"));
      }
      else if (idxPort >= 0)
      {
        host = uri.substring(0, idxPort);
        port = Integer.valueOf(uri.substring(idxPort + 1));
        String line = (method + " " + path + " HTTP/1.1\r\n");
        data.write(line.getBytes("ISO-8859-1"));
      }
      else
      {
        String line = (pre.get("request").toString() + "\r\n");
        data.write(line.getBytes("ISO-8859-1"));
      }
    }
    
    for (Entry<Object, Object> entry : headers.entrySet())
    {
      Object key = entry.getKey();
      Object value = entry.getValue();
      if (key != null && value != null)
      {
        data.write((entry.getKey() + ": " + entry.getValue() + "\r\n").getBytes("ISO-8859-1"));
      }
    }
    
    data.write("\r\n".getBytes("ISO-8859-1"));
    data.write(body);
    
    InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
    
    Socket remoteSocket = new Socket();
    remoteSocket.connect(remoteAddress);
    remoteSocket.setTcpNoDelay(true);
    //remoteSocket.setSoLinger(true, 5);
    remoteSocket.setSoTimeout(VT.VT_TIMEOUT_NETWORK_CONNECTION_MILLISECONDS);
    
    InputStream remoteInput = remoteSocket.getInputStream();
    OutputStream remoteOutput = remoteSocket.getOutputStream();
    OutputStream clientOutput = clientSocket.getOutputStream();
    
    byte[] request = data.toByteArray();
    
    remoteOutput.write(request);
    remoteOutput.flush();
    
    pipeSockets(clientSocket, remoteSocket, clientInput, clientOutput, remoteInput, remoteOutput);
  }
  
  public void pipeSockets(Socket clientSocket, Socket remoteSocket, InputStream clientInput, OutputStream clientOutput, InputStream remoteInput, OutputStream remoteOutput) throws InterruptedException
  {
    SocketPipe firstPipe = new SocketPipe(clientSocket, remoteSocket, clientInput, remoteOutput);
    SocketPipe secondPipe = new SocketPipe(remoteSocket, clientSocket, remoteInput, clientOutput);
    
    Thread pipeThread = new Thread(firstPipe);
    pipeThread.setName(firstPipe.getClass().getSimpleName());
    pipeThread.setDaemon(true);
    pipeThread.start();
    
    secondPipe.run();
    pipeThread.join();
  }
  
  private class SocketPipe implements Runnable
  {
    private Socket first;
    private Socket second;
    private InputStream source;
    private OutputStream destination;
    private final byte[] buffer = new byte[16384];
    private int readed;
    
    private SocketPipe(Socket first, Socket second, InputStream source, OutputStream destination)
    {
      this.first = first;
      this.second = second;
      this.source = source;
      this.destination = destination;
    }

    public void run()
    {
      try
      {
        readed = 1;
        while (readed > 0 && first.isConnected() && !first.isClosed() && second.isConnected() && !second.isClosed())
        {
          readed = source.read(buffer, 0, buffer.length);
          destination.write(buffer, 0, readed);
          destination.flush();
        }
      }
      catch (Throwable t)
      {
        
      }
      finally
      {
        try
        {
          first.close();
        }
        catch (Throwable t)
        {
          
        }
        try
        {
          second.close();
        }
        catch (Throwable t)
        {
          
        }
      }
    }
  }

  /**
   * Decodes the sent headers and loads the data into
   * java Properties' key - value pairs
   * @throws UnsupportedEncodingException 
  **/
  private long decodeHeader(BufferedReader in, Properties pre, Properties parms, Properties headers)
    throws InterruptedException, UnsupportedEncodingException
  {
    long contentLength = 0;
    try {
      // Read the request line
      String inLine = in.readLine();
      if (inLine == null) return -1;
      pre.put("request", inLine);
      StringTokenizer st = new StringTokenizer( inLine );
      if ( !st.hasMoreTokens())
        sendError( HTTP_BADREQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html" );

      String method = "";
      while (method.length() <= 0)
      {
        method = st.nextToken();
      }
      pre.put("method", method);

      if ( !st.hasMoreTokens())
        sendError( HTTP_BADREQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html" );

      String uri = st.nextToken();

      // Decode parameters from the URI
      int qmi = uri.indexOf( '?' );
      if ( qmi >= 0 )
      {
        decodeParms( uri.substring( qmi+1 ), parms );
        uri = decodePercent( uri.substring( 0, qmi ));
      }
      else uri = decodePercent(uri);

      // If there's another token, it's protocol version,
      // followed by HTTP headers. Ignore version but parse headers.
      // NOTE: this now forces header names lowercase since they are
      // case insensitive and vary by client.
      if ( st.hasMoreTokens())
      {
        String line = in.readLine();
        //pre.put("version", line);
        while ( line != null && line.trim().length() > 0 )
        {
          int p = line.indexOf( ':' );
          if ( p >= 0 )
          {
            String headerName = line.substring(0,p).trim();
            String headerValue = line.substring(p+1).trim();
            headers.put( headerName, headerValue );
            if (headerName.equalsIgnoreCase("Content-Length"))
            {
              try
              {
                contentLength = Long.parseLong(headerValue);
              }
              catch (Throwable t)
              {
                
              }
            }
          }
            
          line = in.readLine();
        }
      }

      pre.put("uri", uri);
    }
    catch ( IOException ioe )
    {
      sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
    }
    return contentLength;
  }

  /**
   * Find byte index separating header from body.
   * It must be the last byte of the first two sequential new lines.
  **/
  private int findHeaderEnd(final byte[] buf, int rlen)
  {
    int splitbyte = 0;
    while (splitbyte + 3 < rlen)
    {
      if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n')
        return splitbyte + 4;
      splitbyte++;
    }
    return 0;
  }

  /**
   * Decodes the percent encoding scheme. <br/>
   * For example: "an+example%20string" -> "an example string"
   * @throws UnsupportedEncodingException 
   */
  private String decodePercent( String str ) throws InterruptedException, UnsupportedEncodingException
  {
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      for( int i=0; i<str.length(); i++ )
      {
        char c = str.charAt( i );
        switch ( c )
        {
          case '+':
            baos.write( (int)' ' );
            break;
          case '%':
            baos.write(Integer.parseInt( str.substring(i+1,i+3), 16 ));
            i += 2;
            break;
          default:
            baos.write( (int)c );
            break;
        }
      }

      return new String( baos.toByteArray(), "UTF-8");
    }
    catch( Exception e )
    {
      sendError( HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding." );
      return null;
    }
  }

  /**
   * Decodes parameters in percent-encoded URI-format
   * ( e.g. "name=Jack%20Daniels&pass=Single%20Malt" ) and
   * adds them to given Properties. NOTE: this doesn't support multiple
   * identical keys due to the simplicity of Properties -- if you need multiples,
   * you might want to replace the Properties with a Hashtable of Vectors or such.
   * @throws UnsupportedEncodingException 
   */
  private void decodeParms( String parms, Properties p )
    throws InterruptedException, UnsupportedEncodingException
  {
    if ( parms == null )
      return;

    StringTokenizer st = new StringTokenizer( parms, "&" );
    while ( st.hasMoreTokens())
    {
      String e = st.nextToken();
      int sep = e.indexOf( '=' );
      if ( sep >= 0 )
        p.put( decodePercent( e.substring( 0, sep )).trim(),
             decodePercent( e.substring( sep+1 )));
      else
        p.put( decodePercent( e ).trim(), "" );
    }
  }

  private void sendError( String status, String msg ) throws InterruptedException, UnsupportedEncodingException
  {
    sendResponse( status, MIME_PLAINTEXT, null, new ByteArrayInputStream( msg.getBytes("ISO-8859-1")), false);
    throw new InterruptedException();
  }
  
  private void sendError( String status, String mime, Properties header, InputStream data ) throws InterruptedException, UnsupportedEncodingException
  {
    sendResponse( status, mime, header, data, false);
    throw new InterruptedException();
  }
  
  private void sendResponse( String status, String msg ) throws UnsupportedEncodingException
  {
    sendResponse( status, null, null, new ByteArrayInputStream( msg.getBytes("ISO-8859-1")), true);
  }

  /**
   * Sends given response to the socket.
   */
  private void sendResponse( String status, String mime, Properties header, InputStream data, boolean keepConnection )
  {
    try
    {
      if ( status == null )
        throw new Error( "sendResponse(): Status can't be null." );

      OutputStream out = mySocket.getOutputStream();
      PrintWriter pw = new PrintWriter( out );
      pw.print("HTTP/1.1 " + status + " \r\n");

      if ( mime != null && mime.length() > 0)
        pw.print("Content-Type: " + mime + "\r\n");

      //if ( header == null || header.getProperty( "Date" ) == null )
        //pw.print( "Date: " + gmtFrmt.format( new Date()) + "\r\n");

      if ( header != null )
      {
        Enumeration<?> e = header.keys();
        while ( e.hasMoreElements())
        {
          String key = (String)e.nextElement();
          String value = header.getProperty( key );
          pw.print( key + ": " + value + "\r\n");
        }
      }

      pw.print("\r\n");
      pw.flush();

      if ( data != null )
      {
        int pending = data.available(); // This is to support partial sends, see serveFile()
        byte[] buff = new byte[16384];
        while (pending>0)
        {
          int read = data.read( buff, 0, ( (pending>16384) ?  16384 : pending ));
          if (read <= 0)  break;
          out.write( buff, 0, read );
          pending -= read;
        }
      }
      out.flush();
      
      if (!keepConnection)
      {
        //out.close();
        mySocket.close();
      }
      if ( data != null )
      {
        data.close();
      }
    }
    catch( IOException ioe )
    {
      // Couldn't write? No can do.
      try { mySocket.close(); } catch( Throwable t ) {}
    }
  }

  private Socket mySocket;
  private InputStream myIn;
  private String username;
  private String password;
  
  /**
   * The distribution licence
   */
  private static final String LICENCE =
    "Copyright (C) 2001,2005-2013 by Jarno Elonen <elonen@iki.fi>\n"+
    "and Copyright (C) 2010 by Konstantinos Togias <info@ktogias.gr>\n"+
    "\n"+
    "Redistribution and use in source and binary forms, with or without\n"+
    "modification, are permitted provided that the following conditions\n"+
    "are met:\n"+
    "\n"+
    "Redistributions of source code must retain the above copyright notice,\n"+
    "this list of conditions and the following disclaimer. Redistributions in\n"+
    "binary form must reproduce the above copyright notice, this list of\n"+
    "conditions and the following disclaimer in the documentation and/or other\n"+
    "materials provided with the distribution. The name of the author may not\n"+
    "be used to endorse or promote products derived from this software without\n"+
    "specific prior written permission. \n"+
    " \n"+
    "THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\n"+
    "IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n"+
    "OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n"+
    "IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\n"+
    "INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n"+
    "NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\n"+
    "DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\n"+
    "THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n"+
    "(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n"+
    "OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
}

