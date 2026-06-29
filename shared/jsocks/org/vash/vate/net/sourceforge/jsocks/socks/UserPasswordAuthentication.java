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

import java.io.UnsupportedEncodingException;

import org.vash.vate.compatibility.VTArrays;

/**
 * SOCKS5 User Password authentication scheme.
 */
public class UserPasswordAuthentication implements Authentication {

	/** SOCKS ID for User/Password authentication method */
	public final static int METHOD_ID = 2;

	String userName, password;
	byte[] request;

	/**
	 * Create an instance of UserPasswordAuthentication.
	 * 
	 * @param userName
	 *            User Name to send to SOCKS server.
	 * @param password
	 *            Password to send to SOCKS server.
	 * @throws UnsupportedEncodingException 
	 */
	public UserPasswordAuthentication(String userName, String password) throws UnsupportedEncodingException {
		this.userName = userName;
		this.password = password;
		formRequest();
	}

	/**
	 * Get the user name.
	 * 
	 * @return User name.
	 */
	public String getUser() {
		return userName;
	}

	/**
	 * Get password
	 * 
	 * @return Password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Does User/Password authentication as defined in rfc1929.
	 * 
	 * @return An array containnig in, out streams, or null if authentication fails.
	 */
	public Object[] doSocksAuthentication(int methodId, java.net.Socket proxySocket) throws java.io.IOException {

		if (methodId != METHOD_ID)
			return null;

		java.io.InputStream in = proxySocket.getInputStream();
		java.io.OutputStream out = proxySocket.getOutputStream();

		out.write(request);
		out.flush();
		int version = in.read();
		if (version < 0)
			return null; // Server closed connection
		int status = in.read();
		if (status != 0)
			return null; // Server closed connection, or auth failed.

		return new Object[] { in, out };
	}

	// Private methods
	//////////////////

	/** Convert UserName password in to binary form, ready to be send to server 
	 * @throws UnsupportedEncodingException */
	private void formRequest() throws UnsupportedEncodingException {
		byte[] user_bytes = userName.getBytes("ISO-8859-1");
		byte[] password_bytes = password.getBytes("ISO-8859-1");
		
		if (user_bytes.length > 255)
		{
		  user_bytes = VTArrays.copyOf(user_bytes, 255);
		}
		
		if (password_bytes.length > 255)
		{
		  password_bytes = VTArrays.copyOf(password_bytes, 255);
		}
		
		request = new byte[3 + user_bytes.length + password_bytes.length];
		request[0] = (byte) 1;
		request[1] = (byte) user_bytes.length;
		System.arraycopy(user_bytes, 0, request, 2, user_bytes.length);
		request[2 + user_bytes.length] = (byte) password_bytes.length;
		System.arraycopy(password_bytes, 0, request, 3 + user_bytes.length, password_bytes.length);
	}	
}
