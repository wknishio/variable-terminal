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
package net.sourceforge.jsocks.socks;

/**
 * The Authentication interface provides for performing method specific
 * authentication for SOCKS5 connections.
 */
public interface Authentication {
	/**
	 * This method is called when SOCKS5 server have selected a particular
	 * authentication method, for whch an implementaion have been registered.
	 * 
	 * <p>
	 * This method should return an array {inputstream,outputstream
	 * [,UDPEncapsulation]}. The reason for that is that SOCKS5 protocol allows to
	 * have method specific encapsulation of data on the socket for purposes of
	 * integrity or security. And this encapsulation should be performed by those
	 * streams returned from the method. It is also possible to encapsulate
	 * datagrams. If authentication method supports such encapsulation an instance
	 * of the UDPEncapsulation interface should be returned as third element of the
	 * array, otherwise either null should be returned as third element, or array
	 * should contain only 2 elements.
	 * 
	 * @param methodId
	 *            Authentication method selected by the server.
	 * @param proxySocket
	 *            Socket used to conect to the proxy.
	 * @return Two or three element array containing Input/Output streams which
	 *         should be used on this connection. Third argument is optional and
	 *         should contain an instance of UDPEncapsulation. It should be provided
	 *         if the authentication method used requires any encapsulation to be
	 *         done on the datagrams.
	 */
	Object[] doSocksAuthentication(int methodId, java.net.Socket proxySocket) throws java.io.IOException;
}
