/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.vash.vate.socket.proxy;

import net.sourceforge.jsocks.socks.ProxyMessage;
import net.sourceforge.jsocks.socks.Socks4Message;
import net.sourceforge.jsocks.socks.Socks5Message;
import net.sourceforge.jsocks.socks.SocksException;
import net.sourceforge.jsocks.socks.SocksServerSocket;
import net.sourceforge.jsocks.socks.SocksSocket;
import net.sourceforge.jsocks.socks.UDPRelayServer;
import net.sourceforge.jsocks.socks.server.ServerAuthenticator;
import net.sourceforge.jsocks.socks.Proxy;
//import java.util.Random;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.net.Socket;
//import org.apache.commons.lang.RandomStringUtils;
//import org.apache.log4j.Logger;
import java.util.concurrent.ExecutorService;

import org.vash.vate.VT;
import org.vash.vate.socket.remote.VTRemoteSocketAdapter;
import org.vash.vate.socket.remote.VTRemoteSocketFactory;

/**
 * SOCKS4 and SOCKS5 proxy, handles both protocols simultaniously. Implements
 * all SOCKS commands, including UDP relaying.
 * <p>
 * In order to use it you will need to implement ServerAuthenticator interface.
 * There is an implementation of this interface which does no authentication
 * ServerAuthenticatorNone, but it is very dangerous to use, as it will give
 * access to your local network to anybody in the world. One should never use
 * this authentication scheme unless one have pretty good reason to do so. There
 * is a couple of other authentication schemes in socks.server package.
 * 
 * @see socks.server.ServerAuthenticator
 */
public class VTSocksProxyServer implements Runnable {

	ServerAuthenticator auth;
	ProxyMessage msg = null;

	Socket sock = null, remote_sock = null;
	ServerSocket ss = null;
	UDPRelayServer relayServer = null;
	InputStream in, remote_in;
	OutputStream out, remote_out;

	int mode;
	static final int START_MODE = 0;
	static final int ACCEPT_MODE = 1;
	static final int PIPE_MODE = 2;
	static final int ABORT_MODE = 3;

	static final int DEFAULT_BUF_SIZE = VT.VT_STANDARD_BUFFER_SIZE_BYTES;

	Thread pipe_thread1, pipe_thread2;
	long lastReadTime;

	private int idleTimeout = 90000; // 90 seconds
	private int acceptTimeout = 90000; // 90 seconds

	// private static final Logger LOG = Logger.getLogger(ProxyServer.class);

	private Proxy proxy;

	private int BUF_SIZE = DEFAULT_BUF_SIZE;
	
	private boolean disabled_udp_relay = false;
	private boolean disabled_bind = false;
	
	private VTProxy connect_proxy;
	private VTRemoteSocketFactory socket_factory;
	private VTRemoteSocketFactory datagram_factory;
	private int connectTimeout;
	
	private ExecutorService executorService;
	
	private String bind;

	// private String connectionId;

	// Public Constructors
	/////////////////////

//	/**
//	 * Creates a proxy server with given Authentication scheme.
//	 * 
//	 * @param auth
//	 *            Authentication scheme to be used.
//	 */
//	public VTSocksProxyServer(ServerAuthenticator auth) {
//		this.auth = auth;
//		// this.connectionId = newConnectionId();
//	}
	
	 public VTSocksProxyServer(ServerAuthenticator auth, ExecutorService executorService) {
	    this.executorService = executorService;
	    this.auth = auth;
	  }

	 public VTSocksProxyServer(ServerAuthenticator auth, ExecutorService executorService, boolean disabled_bind, boolean disabled_udp_relay, VTProxy connect_proxy, VTRemoteSocketFactory socket_factory, int connectTimeout) {
	    this.executorService = executorService;
	    this.auth = auth;
	    this.disabled_bind = disabled_bind;
	    this.disabled_udp_relay = disabled_udp_relay;
	    this.connect_proxy = connect_proxy;
	    this.socket_factory = socket_factory;
	    this.connectTimeout = connectTimeout;
	  }
	// Other constructors
	////////////////////

	public VTSocksProxyServer(ServerAuthenticator auth, Socket socket, ExecutorService executorService) {
	  this.executorService = executorService;
		this.auth = auth;
		this.sock = socket;
		// this.connectionId = connectionId;
		mode = START_MODE;
	}
	
	public VTSocksProxyServer(ServerAuthenticator auth, Socket socket, ExecutorService executorService, boolean disabled_bind, boolean disabled_udp_relay, VTProxy connect_proxy, VTRemoteSocketFactory socket_factory, int connectTimeout, String bind) {
    this.executorService = executorService;
    this.auth = auth;
    this.sock = socket;
    this.disabled_bind = disabled_bind;
    this.disabled_udp_relay = disabled_udp_relay;
    this.connect_proxy = connect_proxy;
    this.socket_factory = socket_factory;
    this.connectTimeout = connectTimeout;
    this.bind = bind;
    // this.connectionId = connectionId;
    mode = START_MODE;
  }
	
	public void setDatagramSocketFactory(VTRemoteSocketFactory socketFactory)
	{
	  datagram_factory = socketFactory;
	}

	// Public methods
	/////////////////

	public void setPipeBufferSize(int bufSize) {
		BUF_SIZE = bufSize;
	}

	/**
	 * Set proxy.
	 * <p>
	 * Allows Proxy chaining so that one Proxy server is connected to another and so
	 * on. If proxy supports SOCKSv4, then only some SOCKSv5 requests can be
	 * handled, UDP would not work, however CONNECT and BIND will be translated.
	 * 
	 * @param p
	 *            Proxy which should be used to handle user requests.
	 */
	public void setProxy(Proxy p) {
		proxy = p;
		//UDPRelayServer.proxy = proxy;
	}

	/**
	 * Get proxy.
	 * 
	 * @return Proxy wich is used to handle user requests.
	 */
	public Proxy getProxy() {
		return proxy;
	}

	/**
	 * Sets the timeout for connections, how long shoud server wait for data to
	 * arrive before dropping the connection.<br>
	 * Zero timeout implies infinity.<br>
	 * Default timeout is 3 minutes.
	 */
	public void setIdleTimeout(int timeout) {
		idleTimeout = timeout;
	}

	/**
	 * Sets the timeout for BIND command, how long the server should wait for the
	 * incoming connection.<br>
	 * Zero timeout implies infinity.<br>
	 * Default timeout is 3 minutes.
	 */
	public void setAcceptTimeout(int timeout) {
		acceptTimeout = timeout;
	}

	/**
	 * Sets the timeout for UDPRelay server.<br>
	 * Zero timeout implies infinity.<br>
	 * Default timeout is 3 minutes.
	 */
	public static void setUDPTimeout(int timeout) {
	  UDPRelayServer.setTimeout(timeout);
	}

	/**
	 * Sets the size of the datagrams used in the UDPRelayServer.<br>
	 * Default size is 64K, a bit more than maximum possible size of the datagram.
	 */
	public static void setDatagramSize(int size) {
	  UDPRelayServer.setDatagramSize(size);
	}

	/**
	 * Start the Proxy server at given port.<br>
	 * This methods blocks.
	 */
	public void start(int port) {
		start(port, 5, null, null);
	}
	
	public void start(int port, int backlog) {
    start(port, backlog, null, null);
  }

	/**
	 * Create a server with the specified port, listen backlog, and local IP address
	 * to bind to. The localIP argument can be used on a multi-homed host for a
	 * ServerSocket that will only accept connect requests to one of its addresses.
	 * If localIP is null, it will default accepting connections on any/all local
	 * addresses. The port must be between 0 and 65535, inclusive. <br>
	 * This methods blocks.
	 */
	public void start(int port, int backlog, String host, String bind) {
	  this.bind = bind;
		try {
		  if (host != null)
		  {
		    ss = new ServerSocket(port, backlog, InetAddress.getByName(host));
		  }
		  else
		  {
		    ss = new ServerSocket(port, backlog, null);
		  }
			//ss = new ServerSocket();
			//ss.setReuseAddress(true);
			//ss.bind(new InetSocketAddress(localIP, port), backlog);
			//ss.bind(new InetSocketAddress(port));
			// LOG.info("Starting SOCKS Proxy on:" +
			// ss.getInetAddress().getHostAddress() + ":" + ss.getLocalPort());
			//ss.setReceiveBufferSize(VT.VT_NETWORK_PACKET_BUFFER_SIZE - 1);
			while (true) {
				Socket s = ss.accept();
				//s.setSendBufferSize(VT.VT_NETWORK_PACKET_BUFFER_SIZE - 1);
				s.setTcpNoDelay(true);
	      //s.setSendBufferSize(1024 * 64);
	      //s.setReceiveBufferSize(1024 * 64);
				//s.setSoLinger(true, 5);
				//s.setReuseAddress(true);
				s.setKeepAlive(true);
				//s.setSoTimeout(90000);
				//s.setSoLinger(true, 0);
				// String connectionId = newConnectionId();
				// LOG.info(connectionId + " Accepted from:" +
				// s.getInetAddress().getHostName() + ":" +s.getPort());
				VTSocksProxyServer ps = new VTSocksProxyServer(auth, s, executorService);
				//(new Thread(ps)).start();
				executorService.execute(ps);
			}
		} catch (IOException ioe) {
			// ioe.printStackTrace();
		}
	}

	/**
	 * Creates new unique ID for this connection.
	 * 
	 * @return a random-enough ID.
	 */
	// private String newConnectionId() {
	// return "[" + RandomStringUtils.randomAlphanumeric(4) + "]";
	// }

	/**
	 * Stop server operation.It would be wise to interrupt thread running the server
	 * afterwards.
	 */
	public void stop() {
		try {
			if (ss != null)
				ss.close();
		} catch (IOException ioe) {
		}
	}

	// Runnable interface
	////////////////////
	public void run() {
		switch (mode) {
		case START_MODE:
			try {
				startSession();
			} catch (IOException ioe) {
				handleException(ioe);
				// ioe.printStackTrace();
			} finally {
				abort();
				if (auth != null)
					auth.endSession();
				// LOG.debug(connectionId + " Main thread(client->remote)stopped.");
			}
			break;
		case ACCEPT_MODE:
			try {
				doAccept();
				mode = PIPE_MODE;
				pipe_thread1.interrupt(); // Tell other thread that connection have
											// been accepted.
				pipe(remote_in, out);
			} catch (IOException ioe) {
				// log("Accept exception:"+ioe);
				handleException(ioe);
			} finally {
				abort();
				// LOG.debug(connectionId + " Accept thread(remote->client) stopped");
			}
			break;
		case PIPE_MODE:
			try {
				pipe(remote_in, out);
			} catch (IOException ioe) {
			} finally {
				abort();
				// LOG.debug(connectionId + " Support thread(remote->client) stopped");
			}
			break;
		case ABORT_MODE:
			break;
		default:
			// LOG.info(connectionId + " Unexpected MODE " + mode);
		}
	}

	// Private methods
	/////////////////
	private void startSession() throws IOException {
		//sock.setSoTimeout(idleTimeout);
	  sock.setKeepAlive(true);

		try {
			auth = auth.startSession(sock);
		} catch (IOException ioe) {
			// LOG.info(connectionId + " Auth exception", ioe);
			auth = null;
			return;
		}

		if (auth == null) { // Authentication failed
			// LOG.warn(connectionId + " Authentication failed");
			return;
		}

		in = auth.getInputStream();
		out = auth.getOutputStream();

		msg = readMsg(in);
		// Set the connection ID in the message.
		// msg.setConnectionId(connectionId);
		handleRequest(msg);
	}

	private void handleRequest(ProxyMessage msg) throws IOException {
		if (!auth.checkRequest(msg)) {
			ProxyMessage response = new Socks5Message(Proxy.SOCKS_NOT_ALLOWED_BY_RULESET);
			response.write(out);
			abort();
			throw new SocksException(Proxy.SOCKS_NOT_ALLOWED_BY_RULESET);
		}

		if (msg.ip == null) {
			if (msg instanceof Socks5Message)
			{
				//msg.ip = InetAddress.getByName(msg.host);
			}
			else
				throw new SocksException(Proxy.SOCKS_FAILURE);
		}
		// LOG.debug(connectionId + " " + msg);
    
    switch (msg.command) {
      case Proxy.SOCKS_CMD_CONNECT:
        onConnect(msg);
        break;
      case Proxy.SOCKS_CMD_BIND:
        if (!disabled_bind)
        {
          onBind(msg);
          break;
        }
      case Proxy.SOCKS_CMD_UDP_ASSOCIATE:
        if (!disabled_udp_relay)
        {
          onUDP(msg);
          break;
        }
      default:
        throw new SocksException(Proxy.SOCKS_CMD_NOT_SUPPORTED);
      }
    
		
	}

	private void handleException(IOException ioe) {
		// If we couldn't read the request, return;
		if (msg == null)
			return;
		// If have been aborted by other thread
		if (mode == ABORT_MODE)
			return;
		// If the request was successfully completed, but exception happened later
		if (mode == PIPE_MODE)
			return;

		int error_code = Proxy.SOCKS_FAILURE;

		if (ioe instanceof SocksException)
			error_code = ((SocksException) ioe).errCode;
		else if (ioe instanceof NoRouteToHostException)
			error_code = Proxy.SOCKS_HOST_UNREACHABLE;
		else if (ioe instanceof ConnectException)
			error_code = Proxy.SOCKS_CONNECTION_REFUSED;
		else if (ioe instanceof InterruptedIOException)
			error_code = Proxy.SOCKS_TTL_EXPIRE;

		if (error_code > Proxy.SOCKS_ADDR_NOT_SUPPORTED || error_code < 0) {
			error_code = Proxy.SOCKS_FAILURE;
		}

		sendErrorMessage(error_code);
	}

  private void onConnect(ProxyMessage msg) throws IOException {
		Socket socket = null;
		ProxyMessage response = null;
		

		if (proxy == null) {
		  if (connect_proxy == null)
		  {
		    if (msg.ip != null)
        {
          socket = VTProxy.connect(bind, msg.ip.getHostAddress(), msg.port, connectTimeout, null, connect_proxy);
        }
        else
        {
          socket = VTProxy.connect(bind, msg.host, msg.port, connectTimeout, null, connect_proxy);
        }
		    socket.setTcpNoDelay(true);
		    socket.setKeepAlive(true);
	      //s.setSoTimeout(90000);
		  }
		  else
		  {
		    if (msg.ip != null)
        {
		      socket = VTProxy.connect(bind, msg.ip.getHostAddress(), msg.port, connectTimeout, socket_factory == null ? null : new VTRemoteSocketAdapter(socket_factory), connect_proxy);
        }
		    else
		    {
		      socket = VTProxy.connect(bind, msg.host, msg.port, connectTimeout, socket_factory == null ? null : new VTRemoteSocketAdapter(socket_factory), connect_proxy);
		    }
		    socket.setTcpNoDelay(true);
		    socket.setKeepAlive(true);
        //s.setSoTimeout(90000);
		  }
			
		} else {
		  if (msg.ip != null)
		  {
		    socket = new SocksSocket(proxy, msg.ip, msg.port, connectTimeout);
		  }
		  else
		  {
		    socket = new SocksSocket(proxy, msg.host, msg.port, connectTimeout);
		  }
			
		  socket.setTcpNoDelay(true);
		  socket.setKeepAlive(true);
			//s.setSoTimeout(90000);
		}
		// LOG.info(connectionId + " Connected to " + s.getInetAddress() + ":" +
		// s.getPort());

		if (msg instanceof Socks5Message) {
			response = new Socks5Message(Proxy.SOCKS_SUCCESS, socket.getLocalAddress(), socket.getLocalPort());
		} else {
			response = new Socks4Message(Socks4Message.REPLY_OK, socket.getLocalAddress(), socket.getLocalPort());

		}
		response.write(out);
		startPipe(socket);
	}

	private void onBind(ProxyMessage msg) throws IOException {
		ProxyMessage response = null;

		if (proxy == null) {
			ss = new ServerSocket(0);
			// ss = new ServerSocket(msg.port);
		} else {
		  if (msg.ip != null)
		  {
		    ss = new SocksServerSocket(proxy, msg.ip, msg.port, connectTimeout);
		  }
		  else
		  {
		    ss = new SocksServerSocket(proxy, msg.host, msg.port, connectTimeout);
		  }
		}
		ss.setSoTimeout(acceptTimeout);

		// LOG.info(connectionId + " Trying accept on " + ss.getInetAddress() + ":" +
		// ss.getLocalPort());

		if (msg.version == 5)
			response = new Socks5Message(Proxy.SOCKS_SUCCESS, ss.getInetAddress(), ss.getLocalPort());
		else
			response = new Socks4Message(Socks4Message.REPLY_OK, ss.getInetAddress(), ss.getLocalPort());
		response.write(out);

		mode = ACCEPT_MODE;

		pipe_thread1 = Thread.currentThread();
		pipe_thread2 = new Thread(this);
		pipe_thread2.setDaemon(true);
		//pipe_thread2.start();
		executorService.execute(pipe_thread2);

		// Make timeout infinit.
		sock.setSoTimeout(0);
		int eof = 0;

		try {
			while ((eof = in.read()) >= 0) {
				if (mode != ACCEPT_MODE) {
					if (mode != PIPE_MODE)
						return;// Accept failed

					remote_out.write(eof);
					remote_out.flush();
					break;
				}
			}
		} catch (EOFException eofe) {
			// System.out.println("EOF exception");
			return;// Connection closed while we were trying to accept.
		} catch (InterruptedIOException iioe) {
			// Accept thread interrupted us.
			// System.out.println("Interrupted");
			if (mode != PIPE_MODE)
				return;// If accept thread was not successfull return.
		} finally {
			// System.out.println("Finnaly!");
		}

		if (eof < 0)// Connection closed while we were trying to accept;
			return;

		// Do not restore timeout, instead timeout is set on the
		// remote socket. It does not make any difference.

		pipe(in, remote_out);
	}

	private void onUDP(ProxyMessage msg) throws IOException {
		if (msg.ip.getHostAddress().equals("0.0.0.0") || msg.ip.getHostAddress().equals("::")
				|| msg.ip.getHostAddress().equals("::0") || msg.ip.getHostAddress().equals("0:0:0:0:0:0:0:0")
				|| msg.ip.getHostAddress().equals("00:00:00:00:00:00:00:00")
				|| msg.ip.getHostAddress().equals("0000:0000:0000:0000:0000:0000:0000:0000"))

			msg.ip = sock.getInetAddress();
		//System.out.println("onUDP()");
		// LOG.info(connectionId + " Creating UDP relay server for " + msg.ip +
		// ":" + msg.port);
		if (datagram_factory != null)
		{
		  if (socket_factory != null)
		  {
		    relayServer = new UDPRelayServer(msg.ip, msg.port, Thread.currentThread(), sock, auth, proxy, connectTimeout, null, socket_factory.createSocket("", 0, 300000));
		  }
		  else
		  {
	      relayServer = new UDPRelayServer(msg.ip, msg.port, Thread.currentThread(), sock, auth, proxy, connectTimeout, datagram_factory.createSocket("", 0, 300000));
		  }
		}
		else
		{
		  if (socket_factory != null)
      {
		    relayServer = new UDPRelayServer(msg.ip, msg.port, Thread.currentThread(), sock, auth, proxy, connectTimeout, null, socket_factory.createSocket("", 0, 300000));
      }
		  else
		  {
		    relayServer = new UDPRelayServer(msg.ip, msg.port, Thread.currentThread(), sock, auth, proxy, connectTimeout);
		  }
		}
		

		ProxyMessage response;

		response = new Socks5Message(Proxy.SOCKS_SUCCESS, relayServer.relayIP, relayServer.relayPort);

		response.write(out);

		relayServer.start();

		// Make timeout infinit.
		sock.setSoTimeout(0);
		try {
			while (in.read() >= 0)
				/* do nothing */;
		} catch (EOFException eofe) {
		}
	}

	// Private methods
	//////////////////

	@SuppressWarnings("unused")
  private void doAccept() throws IOException {
		Socket s;
		long startTime = System.currentTimeMillis();
		// System.out.println("ProxyServer doAccept()");
		// System.out.println("ProxyServer port: " + ss.getLocalPort());
		while (true) {
			//ss.setReuseAddress(true);
		  //ss.setReceiveBufferSize(VT.VT_NETWORK_PACKET_BUFFER_SIZE - 1);
			s = ss.accept();
			//s.setSendBufferSize(VT.VT_NETWORK_PACKET_BUFFER_SIZE - 1);
			s.setTcpNoDelay(true);
      //s.setSendBufferSize(1024 * 64);
      //s.setReceiveBufferSize(1024 * 64);
			//s.setSoLinger(true, 5);
			//s.setReuseAddress(true);
			s.setKeepAlive(true);
			//s.setSoTimeout(90000);
			//s.setSoLinger(true, 0);
			// if(s.getInetAddress().equals(msg.ip)){
			if (s != null) {
				// got the connection from the right host
				// Close listenning socket.
				ss.close();
				break;
			} else if (ss instanceof SocksServerSocket) {
				// We can't accept more then one connection
				if (s != null) {
					s.close();
				}
				ss.close();
				throw new SocksException(Proxy.SOCKS_FAILURE);
			} else {
				if (acceptTimeout != 0) { // If timeout is not infinit
					int newTimeout = acceptTimeout - (int) (System.currentTimeMillis() - startTime);
					if (newTimeout <= 0)
						throw new InterruptedIOException("In doAccept()");
					ss.setSoTimeout(newTimeout);
				}
				if (s != null) {
					s.close(); // Drop all connections from other hosts
				}
			}
		}

		// Accepted connection
		remote_sock = s;
		remote_in = s.getInputStream();
		remote_out = s.getOutputStream();

		// Set timeout
		remote_sock.setKeepAlive(true);
		//remote_sock.setSoTimeout(idleTimeout);

		// LOG.info(connectionId + " Accepted from "+ s.getInetAddress() + ":" +
		// s.getPort());

		ProxyMessage response;

		if (msg.version == 5)
			response = new Socks5Message(Proxy.SOCKS_SUCCESS, s.getInetAddress(), s.getPort());
		else
			response = new Socks4Message(Socks4Message.REPLY_OK, s.getInetAddress(), s.getPort());
		response.write(out);
	}

	private ProxyMessage readMsg(InputStream in) throws IOException {
		PushbackInputStream push_in;
		if (in instanceof PushbackInputStream)
			push_in = (PushbackInputStream) in;
		else
			push_in = new PushbackInputStream(in);

		int version = push_in.read();
		push_in.unread(version);

		ProxyMessage msg;

		if (version == 5) {
			msg = new Socks5Message(push_in, false);
		} else if (version == 4) {
			msg = new Socks4Message(push_in, false);
		} else {
			throw new SocksException(Proxy.SOCKS_FAILURE);
		}
		return msg;
	}

	private void startPipe(Socket remoteSocket) {
		mode = PIPE_MODE;
		remote_sock = remoteSocket;
		try {
			remote_in = remoteSocket.getInputStream();
			remote_out = remoteSocket.getOutputStream();
			pipe_thread1 = Thread.currentThread();
			pipe_thread2 = new Thread(this);
			pipe_thread2.setDaemon(true);
			//pipe_thread2.start();
			executorService.execute(pipe_thread2);
			pipe(in, remote_out);
		} catch (IOException ioe) {
		}
	}

	private void sendErrorMessage(int error_code) {
		ProxyMessage err_msg;
		if (msg instanceof Socks4Message)
			err_msg = new Socks4Message(Socks4Message.REPLY_REJECTED);
		else
			err_msg = new Socks5Message(error_code);
		try {
			err_msg.write(out);
		} catch (IOException ioe) {
		}
	}

	private synchronized void abort() {
		if (mode == ABORT_MODE)
			return;
		mode = ABORT_MODE;
		try {
			// LOG.info(connectionId + " Closing connection.");
			if (remote_sock != null)
				remote_sock.close();
			if (sock != null)
				sock.close();
			if (relayServer != null)
				relayServer.stop();
			if (ss != null)
				ss.close();
			if (pipe_thread1 != null)
				pipe_thread1.interrupt();
			if (pipe_thread2 != null)
				pipe_thread2.interrupt();
		} catch (IOException ioe) {
		}
	}

	private void pipe(InputStream in, OutputStream out) throws IOException {
		lastReadTime = System.currentTimeMillis();
		final byte[] buf = new byte[BUF_SIZE];
		int len = 0;
		while (len >= 0) {
			try {
				if (len != 0) {
					out.write(buf, 0, len);
					out.flush();
				}
				len = in.read(buf);
				lastReadTime = System.currentTimeMillis();
			} catch (InterruptedIOException iioe) {
				if (idleTimeout == 0)
					return;// Other thread interrupted us.
				long timeSinceRead = System.currentTimeMillis() - lastReadTime;
				if (timeSinceRead >= idleTimeout - 1000) // -1s for adjustment.
					return;
				len = 0;

			}
		}
	}

	static final String command_names[] = { "CONNECT", "BIND", "UDP_ASSOCIATE" };

	static final String command2String(int cmd) {
		if (cmd > 0 && cmd < 4)
			return command_names[cmd - 1];
		else
			return "Unknown Command " + cmd;
	}
	
	
}
