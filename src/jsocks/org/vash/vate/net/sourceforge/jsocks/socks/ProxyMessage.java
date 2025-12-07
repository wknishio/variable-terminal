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
package org.vash.vate.net.sourceforge.jsocks.socks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Abstract class which describes SOCKS4/5 response/request.
 */
public abstract class ProxyMessage {
	/** Host as an IP address */
	public InetAddress ip = null;
	/** SOCKS version, or version of the response for SOCKS4 */
	public int version;
	/** Port field of the request/response */
	public int port;
	/** Request/response code as an int */
	public int command;
	/** Host as string. */
	public String host = null;
	/** User field for SOCKS4 request messages */
	public String user = null;
	/** Connection ID */
	private String connectionId = "N/A";

	ProxyMessage(int command, InetAddress ip, int port) {
		this.command = command;
		this.ip = ip;
		this.port = port;
	}

	ProxyMessage() {
	}

	/**
	 * Initialises Message from the stream. Reads server response from given stream.
	 * 
	 * @param in
	 *            Input stream to read response from.
	 * @throws SocksException
	 *             If server response code is not SOCKS_SUCCESS(0), or if any error
	 *             with protocol occurs.
	 * @throws IOException
	 *             If any error happens with I/O.
	 */
	public abstract void read(InputStream in) throws SocksException, IOException;

	/**
	 * Initialises Message from the stream. Reads server response or client request
	 * from given stream.
	 * 
	 * @param in
	 *            Input stream to read response from.
	 * @param clinetMode
	 *            If true read server response, else read client request.
	 * @throws SocksException
	 *             If server response code is not SOCKS_SUCCESS(0) and reading in
	 *             client mode, or if any error with protocol occurs.
	 * @throws IOException
	 *             If any error happens with I/O.
	 */
	public abstract void read(InputStream in, boolean client_mode) throws SocksException, IOException;

	/**
	 * Writes the message to the stream.
	 * 
	 * @param out
	 *            Output stream to which message should be written.
	 */
	public abstract void write(OutputStream out) throws SocksException, IOException;

	/**
	 * Get the Address field of this message as InetAddress object.
	 * 
	 * @return Host address or null, if one can't be determined.
	 */
	public InetAddress getInetAddress() throws UnknownHostException {
		return ip;
	}

	/**
	 * Get string representaion of this message.
	 * 
	 * @return string representation of this message.
	 */
	public String toString() {
		return "Proxy Message:\n" + "Version:" + version + "\n" + "Command:" + command + "\n" + "IP:     " + ip + "\n"
				+ "Port:   " + port + "\n" + "User:   " + user + "\n";
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	// Package methods
	//////////////////

	static final String bytes2IPV4(byte[] addr, int offset) {
		String hostName = "" + (addr[offset] & 0xFF);
		for (int i = offset + 1; i < offset + 4; ++i)
			hostName += "." + (addr[i] & 0xFF);
		return hostName;
	}

	static final String bytes2IPV6(byte[] addr, int offset) {
		String hostName = "" + bytesToHex(addr, offset, 2);
		for (int i = offset + 2; i < offset + 16; i += 2) {
			hostName += ":" + bytesToHex(addr, offset, 2);
		}
		return hostName;
	}

	private static final char[] hexArray = "0123456789abcdef".toCharArray();

	public static String bytesToHex(byte[] bytes, int offset, int length) {
		char[] hexChars = new char[length * 2];
		for (int j = 0; j < length; j++) {
			int v = bytes[offset + j] & 0xFF;
			hexChars[offset + (j * 2)] = hexArray[v >>> 4];
			hexChars[offset + (j * 2) + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
