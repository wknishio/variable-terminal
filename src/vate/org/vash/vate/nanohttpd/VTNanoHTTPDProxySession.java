package org.vash.vate.nanohttpd;

import java.io.BufferedReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.vash.vate.VT;
import org.vash.vate.parser.VTConfigurationProperties;
import org.vash.vate.socket.VTProxy;

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
//  public static void main(String[] args)
//  {
//    System.out.println("md5=" + DigestUtils.md5Hex("coisa"));
//  }
  
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
  public static final String HTTP_PAYLOAD_TOO_LARGE = "413 Payload Too Large";
  
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
          if (read > 0)
          {
            rlen += read;
            splitbyte = findHeaderEndSafe(header, rlen);
            if (splitbyte > 0)
            {
              foundHeaderEnd = true;
              break;
            }
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
        //uee.printStackTrace();
      }
    }
    
    /**
     * Adds given line to the header.
     */
    public void addHeader( String name, String value )
    {
      headers.put( name, value );
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
    public Properties headers = new VTConfigurationProperties();
    
    //public boolean keepConnection = false;
  }
  
  public VTNanoHTTPDProxySession( Socket s, InputStream in, String username, String password, VTProxy proxy, boolean digestAuthentication)
  {
    mySocket = s;
    myIn = in;
    this.username = username;
    this.password = password;
    this.proxy = proxy;
    this.digestAuthentication = digestAuthentication;
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
      final int bufsize = VT.VT_STANDARD_BUFFER_SIZE_BYTES;
      byte[] buf = new byte[bufsize];
      int splitbyte = 0;
      int rlen = 0;
      {
        int read = 1;
        while (read > 0 && rlen < bufsize)
        {
          read = in.read(buf, rlen, bufsize - rlen);
          if (read > 0)
          {
            rlen += read;
            splitbyte = findHeaderEnd(buf, rlen);
            if (splitbyte > 0)
            {
              foundHeaderEnd = true;
              break;
            }
          }
        }
      }
      if (rlen == 0)
      {
        sendError( HTTP_BADREQUEST, "BAD REQUEST: Empty request." );
      }
      
      if (!foundHeaderEnd)
      {
        sendError(HTTP_PAYLOAD_TOO_LARGE, "PAYLOAD TOO LARGE: Malformed request or request headers too large.");
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
        sendError( HTTP_BADREQUEST, "BAD REQUEST: Missing line terminator in request." );
      }
      
      String method = pre.getProperty("method");
      String uri = pre.getProperty("uri");
      
      if (method == null)
      {
        sendError( HTTP_BADREQUEST, "BAD REQUEST: Missing method in request." );
      }
      
      if (uri == null)
      {
        sendError( HTTP_BADREQUEST, "BAD REQUEST: Missing URI in request." );
      }
      
      if (size == 0)
      {
        size = 0x7FFFFFFFFFFFFFFFL;
      }
      
      // Write the part of body already read to ByteArrayOutputStream b
      ByteArrayOutputStream h = new ByteArrayOutputStream();
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      
      if (splitbyte < rlen)
      {
        b.write(buf, splitbyte, rlen-splitbyte);
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
      
      // this is a http proxy so maybe theres no need to read all body data
      
      // Now read all the body and write it to f
      //buf = new byte[VT.VT_STANDARD_BUFFER_SIZE_BYTES];
      while ( rlen >= 0 && size > 0 )
      {
        rlen = in.read(buf, 0, (int) Math.min(VT.VT_STANDARD_BUFFER_SIZE_BYTES, size));
        if (rlen > 0)
        {
          size -= rlen;
          b.write(buf, 0, rlen);
        }
      }
      
      // Get the raw body as a byte []
      byte[] bodyData = b.toByteArray();
      byte[] headerData = h.toByteArray();
      
      // Ok, now do the serve()
      serve(uri, method, pre, headers, headerData, bodyData, username, password, mySocket, in, proxy );
    }
    catch ( InterruptedException ie )
    {
      // Thrown by sendError, ignore and exit the thread.
    }
    catch ( Throwable e )
    {
      //e.printStackTrace();
      try
      {
        sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Exception: " + e.getMessage());
      }
      catch ( Throwable t ) 
      {
        
      }
    }
    finally
    {
      try
      {
        //mySocket.getOutputStream().close();
        //mySocket.close();
      }
      catch ( Throwable t ) 
      {
        
      }
    }
  }
  
  public void serve(String uri, String method, Properties pre, Properties headers, byte[] headerData, byte[] bodyData, String username, String password, Socket clientSocket, InputStream clientInput, VTProxy connectProxy) throws IOException, URISyntaxException, InterruptedException
  {
    if (digestAuthentication)
    {
      int result = checkProxyAuthenticatedDigest(headers, method, username, password, "Proxy");
      if (result != 0)
      {
        requireProxyAuthenticationDigest("Proxy", generateNonce("Proxy", username, password), result == -2);
      }
    }
    else
    {
      if (!checkProxyAuthenticatedBasic(headers, username, password))
      {
        requireProxyAuthenticationBasic("Proxy");
        return;
      }
    }
    
    if (method.equalsIgnoreCase("CONNECT"))
    {
      serveConnectRequest(uri, method, pre, headers, headerData, bodyData, clientSocket, clientInput, connectProxy);
    }
    else
    {
      servePipeRequest(uri, method, pre, headers, headerData, bodyData, clientSocket, clientInput, connectProxy);
    }
  }
  
  private boolean checkProxyAuthenticatedBasic(Properties headers, String username, String password) throws UnsupportedEncodingException
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
  
  private void requireProxyAuthenticationBasic(String realm) throws UnsupportedEncodingException, InterruptedException
  {
    Response resp = new Response();
    if (realm != null && realm.length() > 0)
    {
      resp.headers.put("Proxy-Authenticate", "Basic realm=\"" + realm + "\"");
    }
    else
    {
      resp.headers.put("Proxy-Authenticate", "Basic");
    }
    resp.status = HTTP_PROXY_AUTHENTICATION_REQUIRED;
    sendError(resp.status, MIME_PLAINTEXT, resp.headers, null);
  }
  
  private int checkProxyAuthenticatedDigest(Properties headers, String method, String username, String password, String realm) throws UnsupportedEncodingException
  {
    if (username == null || password == null)
    {
      return 0;
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
      return validateProxyAuthorizationDigest(proxyAuthorization, method, username, password, realm);
    }
    return -1;
  }
  
  private int validateProxyAuthorizationDigest(String proxyAuthorization, String method, String username, String password, String realm) throws UnsupportedEncodingException
  {
    //System.out.println("proxyAuthorization=" + proxyAuthorization);
    if (proxyAuthorization == null)
    {
      return -1;
    }
    if (!proxyAuthorization.startsWith("Digest "))
    {
      return -1;
    }
    Map<String, String> values = parseHeader(proxyAuthorization);
    
    String userName = values.get("username");
    String realmName = values.get("realm");
    String nonce = values.get("nonce");
    String nc = values.get("nc");
    String cnonce = values.get("cnonce");
    String qop = values.get("qop");
    String uri = values.get("uri");
    String response = values.get("response");
    
    String a2 = method + ":" + uri;
    String md5a2 = DigestUtils.md5Hex(a2.getBytes("ISO-8859-1"));
    
    String a1 = userName + ":" + realmName + ":" + password;
    String md5a1 = DigestUtils.md5Hex(a1.getBytes("ISO-8859-1"));
    
    String serverDigestValue = md5a1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + md5a2;
    
    String serverDigest = DigestUtils.md5Hex(serverDigestValue.getBytes("ISO-8859-1"));
    String clientDigest = response;
    
    //System.out.println("serverDigest=" + serverDigest);
    
    if (serverDigest.equals(clientDigest))
    {
      return 0;
//      Long timestamp = VALID_DIGEST_NONCES.get(nOnce);
//      if (timestamp != null)
//      {
//        if (timestamp >= System.currentTimeMillis())
//        {
//          return 0;
//        }
//        else
//        {
//          VALID_DIGEST_NONCES.remove(nOnce);
//          return -2;
//        }
//      }
//      else
//      {
//        return -2;
//      }
    }
    
    return -1;
  }
  
  private void requireProxyAuthenticationDigest(String realm, String nonce, boolean stale) throws UnsupportedEncodingException, InterruptedException
  {
    Response resp = new Response();
    resp.headers.put("Proxy-Authenticate", "Digest realm=\"" + realm + "\", "
        +  "qop=\"auth\", nonce=\"" + nonce + "\", " + "opaque=\""
        + DigestUtils.md5Hex(nonce.getBytes("ISO-8859-1")) + "\"" + (stale ? ", stale=\"true\"" : ""));
    resp.status = HTTP_PROXY_AUTHENTICATION_REQUIRED;
    sendError(resp.status, MIME_PLAINTEXT, resp.headers, null);
  }
  
  private void serveConnectRequest(String uri, String method, Properties pre, Properties headers, byte[] headerData, byte[] bodyData, Socket clientSocket, InputStream clientInput, VTProxy connectProxy) throws URISyntaxException, IOException, InterruptedException
  {
    String host = "";
    int port = 80;
    
    try
    {
      int idxPort = uri.lastIndexOf(':');
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
    
    //Socket remoteSocket = new Socket(host, port);
    Socket remoteSocket = VTProxy.connect(host, port, connectProxy, null);
    remoteSocket.setTcpNoDelay(true);
    remoteSocket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    
    InputStream remoteInput = remoteSocket.getInputStream();
    OutputStream remoteOutput = remoteSocket.getOutputStream();
    OutputStream clientOutput = clientSocket.getOutputStream();
    
    sendResponse(new Response().status, "");
    
    pipeSockets(clientSocket, remoteSocket, clientInput, clientOutput, remoteInput, remoteOutput);
  }
  
  private void servePipeRequest(String uri, String method, Properties pre, Properties headers, byte[] headerData, byte[] bodyData, Socket clientSocket, InputStream clientInput, VTProxy connectProxy) throws IOException, URISyntaxException, InterruptedException
  {
    ByteArrayOutputStream requestData = new ByteArrayOutputStream();
    for (Object headerName : headers.keySet().toArray(new Object[] {}))
    {
      if (headerName != null)
      {
        if (headerName.toString().equalsIgnoreCase("Proxy-Authorization"))
        {
          headers.remove(headerName);
        }
        if (headerName.toString().equalsIgnoreCase("Proxy-Connection"))
        {
          if (!searchHeaderIgnoreCase("Connection", headers))
          {
            headers.put("Connection", headers.get(headerName));
          }
          headers.remove(headerName);
        }
      }
    }
    
    String host = "";
    int port = 80;
    String path = "/";
    
    //System.out.println("method=[" + method + "]");
    //System.out.println("uri=[" + uri + "]");
    //System.out.println("body=[" + new String(body) + "]");
    
//    Pattern pattern = Pattern.compile("[a-zA-Z0-9+.-]+://");
//    Matcher matcher = pattern.matcher(uri);
//    
//    if (!matcher.find())
//    {
//      uri = "http://" + uri;
//    }
    
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
    }
    catch (URISyntaxException e)
    {
      sendError( HTTP_BADREQUEST, "BAD REQUEST: Malformed absolute form URI in request." );
      //e.printStackTrace();
    }
    
    String line = (method + " " + path + " HTTP/1.1\r\n");
    requestData.write(line.getBytes("ISO-8859-1"));
    
    for (Entry<Object, Object> entry : headers.entrySet())
    {
      Object key = entry.getKey();
      Object value = entry.getValue();
      if (key != null && value != null)
      {
        requestData.write((entry.getKey() + ": " + entry.getValue() + "\r\n").getBytes("ISO-8859-1"));
      }
    }
    
    requestData.write("\r\n".getBytes("ISO-8859-1"));
    requestData.write(bodyData);
    
    //Socket remoteSocket = new Socket(host, port);
    Socket remoteSocket = VTProxy.connect(host, port, connectProxy, null);
    remoteSocket.setTcpNoDelay(true);
    remoteSocket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    
    InputStream remoteInput = remoteSocket.getInputStream();
    OutputStream remoteOutput = remoteSocket.getOutputStream();
    OutputStream clientOutput = clientSocket.getOutputStream();
    
    remoteOutput.write(requestData.toByteArray());
    remoteOutput.flush();
    //System.out.println("pipe=" + uri);
    pipeSockets(clientSocket, remoteSocket, clientInput, clientOutput, remoteInput, remoteOutput);
    //System.out.println("finished=" + uri);
  }
  
  private void pipeSockets(Socket clientSocket, Socket remoteSocket, InputStream clientInput, OutputStream clientOutput, InputStream remoteInput, OutputStream remoteOutput) throws InterruptedException
  {
    SocketPipe firstPipe = new SocketPipe(clientSocket, remoteSocket, remoteInput, clientOutput);
    SocketPipe secondPipe = new SocketPipe(remoteSocket, clientSocket, clientInput, remoteOutput);
    
    Thread firstThread = new Thread(firstPipe);
    firstThread.setName(firstPipe.getClass().getSimpleName());
    firstThread.setDaemon(true);
    
    Thread secondThread = new Thread(secondPipe);
    secondThread.setName(secondPipe.getClass().getSimpleName());
    secondThread.setDaemon(true);
    
    firstPipe.setAnother(secondThread);
    secondPipe.setAnother(firstThread);
    
    firstThread.start();
    secondThread.start();
    
    firstThread.join();
    secondThread.join();
  }
  
  protected static String removeQuotes(String quotedString, boolean quotesRequired)
  {
    //support both quoted and non-quoted
    if (quotedString.length() > 0 && quotedString.charAt(0) != '"' && !quotesRequired)
    {
      return quotedString;
    }
    else if (quotedString.length() > 2)
    {
      return quotedString.substring(1, quotedString.length() - 1);
    }
    else
    {
      return new String();
    }
  }
  
  protected static String removeQuotes(String quotedString)
  {
    return removeQuotes(quotedString, false);
  }
  
  protected String generateNonce(String realm, String username, String password) throws UnsupportedEncodingException
  {
    long currentTime = System.currentTimeMillis();
    
    String nonceValue = username + ":" + password + ":" + currentTime + ":" + realm;
    nonceValue = DigestUtils.md5Hex(nonceValue.getBytes("ISO-8859-1"));
    
    //VALID_DIGEST_NONCES.put(nOnceValue, currentTime + (1000 * 300));
    return nonceValue;
  }
  
  private boolean searchHeaderIgnoreCase(String searchedHeader, Properties headers)
  {
    for (Object headerName : headers.keySet().toArray(new Object[] {}))
    {
      if (headerName.toString().equalsIgnoreCase(searchedHeader))
      {
        return true;
      }
    }
    return false;
  }
  
  private Map<String, String> parseHeader(String headerString)
  {
    // seperte out the part of the string which tells you which Auth scheme is it
    String headerStringWithoutScheme = headerString.substring(headerString.indexOf(" ") + 1).trim();
    LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
    String keyValueArray[] = headerStringWithoutScheme.split(",");
    for (String keyval : keyValueArray)
    {
      if (keyval.contains("="))
      {
        String key = keyval.substring(0, keyval.indexOf("="));
        String value = keyval.substring(keyval.indexOf("=") + 1);
        values.put(key.trim(), value.replaceAll("\"", "").trim());
      }
    }
    return values;
  }
  
  private class SocketPipe implements Runnable
  {
    private Socket close1;
    private Socket close2;
    private Thread another;
    //private Socket second;
    private InputStream source;
    private OutputStream destination;
    private final byte[] buffer = new byte[VT.VT_STANDARD_BUFFER_SIZE_BYTES];
    private int readed;
    //private boolean inClosed;
    //private boolean outClosed;
    
    private SocketPipe(Socket close1, Socket close2, InputStream source, OutputStream destination)
    {
      this.close1 = close1;
      this.close2 = close2;
      this.source = source;
      this.destination = destination;
    }
    
    public void setAnother(Thread another)
    {
      this.another = another;
    }
    
    public void run()
    {
      try
      {
        readed = 1;
        while (readed > 0)
        {
          readed = source.read(buffer, 0, buffer.length);
          if (readed > 0)
          {
            destination.write(buffer, 0, readed);
            destination.flush();
          }
        }
      }
      catch (Throwable t)
      {
        
      }
      finally
      {
        try
        {
          close1.close();
        }
        catch (Throwable t)
        {
          
        }
        
        try
        {
          close2.close();
        }
        catch (Throwable t)
        {
          
        }
        
        try
        {
          another.interrupt();
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
        sendError( HTTP_BADREQUEST, "BAD REQUEST: Missing method in request." );
      
      String method = "";
      while (method.length() <= 0)
      {
        method = st.nextToken();
      }
      pre.put("method", method);
      
      if ( !st.hasMoreTokens())
        sendError( HTTP_BADREQUEST, "BAD REQUEST: Missing URI in request." );
      
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
    sendResponse( status, MIME_PLAINTEXT, null, new ByteArrayInputStream( msg.getBytes("ISO-8859-1")));
    throw new InterruptedException(status);
  }
  
  private void sendError( String status, String mime, Properties header, InputStream data ) throws InterruptedException, UnsupportedEncodingException
  {
    sendResponse( status, mime, header, data);
    throw new InterruptedException(status);
  }
  
  private void sendResponse( String status, String msg ) throws UnsupportedEncodingException
  {
    sendResponse( status, MIME_PLAINTEXT, null, new ByteArrayInputStream( msg.getBytes("ISO-8859-1")));
  }
  
  /**
   * Sends given response to the socket.
   */
  private void sendResponse( String status, String mime, Properties header, InputStream data )
  {
    try
    {
      if ( status == null )
        throw new Error( "sendResponse(): Status can't be null." );
      
      OutputStream out = mySocket.getOutputStream();
      PrintWriter pw = new PrintWriter( new OutputStreamWriter(out, "ISO-8859-1") );
      pw.print("HTTP/1.1 " + status + " \r\n");
      
      if ( mime != null && mime.length() > 0)
        pw.print("Content-Type: " + mime + "\r\n");
      
      if ( header == null || header.getProperty( "Date" ) == null )
        pw.print( "Date: " + gmtFrmt.format( new Date()) + "\r\n");
      
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
        byte[] buff = new byte[VT.VT_STANDARD_BUFFER_SIZE_BYTES];
        while (pending>0)
        {
          int read = data.read( buff, 0, Math.min(VT.VT_STANDARD_BUFFER_SIZE_BYTES, pending));
          if (read <= 0)  break;
          out.write( buff, 0, read );
          pending -= read;
        }
      }
      out.flush();
      
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
  private VTProxy proxy;
  private boolean digestAuthentication;
  //private static final Map<String, Long> VALID_DIGEST_NONCES = new LinkedHashMap<String, Long>();
  
  private static java.text.SimpleDateFormat gmtFrmt;
  static
  {
    gmtFrmt = new java.text.SimpleDateFormat( "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
    gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
  }
  /**
   * The distribution licence
   */
  public static final String LICENCE =
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
