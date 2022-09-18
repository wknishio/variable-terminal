/*******************************************************************************
 * ============================================================================
 *                 The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *     include the following  acknowledgment: "This product includes software
 *     developed by SuperBonBon Industries (http://www.sbbi.net/)."
 *     Alternately, this acknowledgment may appear in the software itself, if
 *     and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "UPNPLib" and "SuperBonBon Industries" must not be
 *     used to endorse or promote products derived from this software without
 *     prior written permission. For written permission, please contact
 *     info@sbbi.net.
 *
 * 5. Products  derived from this software may not be called 
 *     "SuperBonBon Industries", nor may "SBBI" appear in their name, 
 *     without prior written permission of SuperBonBon Industries.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT,INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software  consists of voluntary contributions made by many individuals
 * on behalf of SuperBonBon Industries. For more information on 
 * SuperBonBon Industries, please see <http://www.sbbi.net/>.
 *******************************************************************************/
package net.sbbi.upnp;

import java.io.*;
import java.net.*;
import java.util.*;

import net.sbbi.upnp.devices.*;
import org.apache.commons.logging.*;

/**
 * Class to discover an UPNP device on the network.</br>
 * A multicast socket will be created to discover devices, the binding port for
 * this socket is set to 1901, if this is causing a problem you can use the
 * net.sbbi.upnp.Discovery.bindPort system property to specify another port. The
 * discovery methods only accept matching device description and broadcast
 * message response IP to avoid a security flaw with the protocol. If you are
 * not happy with such behaviour you can set the net.sbbi.upnp.ddos.matchip
 * system property to false to avoid this check.
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class Discovery {

	private final static Log log = LogFactory.getLog(Discovery.class);

	public final static String ROOT_DEVICES = "upnp:rootdevice";
	public final static String ALL_DEVICES = "ssdp:all";

	public static final int DEFAULT_MX = 3;
	public static final int DEFAULT_TTL = 4;
	public static final int DEFAULT_TIMEOUT = 1500;
	public static final String DEFAULT_SEARCH = ALL_DEVICES;
	public static final int DEFAULT_SSDP_SEARCH_PORT = 1901;

	public final static String SSDP_IPV4 = "239.255.255.250";
	public final static String SSDP_IPV6 = "ff05::c";
	public final static int SSDP_PORT = 1900;

	/**
	 * Devices discovering on all network interfaces with default values, all root
	 * devices will be searched
	 * 
	 * @return an array of UPNP Root device or null if nothing found with the
	 *         default timeout. Null does NOT means that no UPNP device is available
	 *         on the network. It only means that for this default timeout no
	 *         devices responded or that effectively no devices are available at
	 *         all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover() throws IOException {
		return discover(DEFAULT_TIMEOUT, DEFAULT_TTL, DEFAULT_MX, DEFAULT_SEARCH);
	}

	/**
	 * Devices discovering on all network interfaces with a given root device to
	 * search
	 * 
	 * @param searchTarget
	 *            the device URI to search
	 * @return an array of UPNP Root device that matches the search or null if
	 *         nothing found with the default timeout. Null does NOT means that no
	 *         UPNP device is available on the network. It only means that for this
	 *         given timeout no devices responded or that effectively no devices are
	 *         available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(String searchTarget) throws IOException {
		return discover(DEFAULT_TIMEOUT, DEFAULT_TTL, DEFAULT_MX, searchTarget);
	}

	/**
	 * Devices discovering on all network interfaces with a given timeout and a
	 * given root device to search
	 * 
	 * @param timeOut
	 *            the time allowed for a device to give a response
	 * @param searchTarget
	 *            the device URI to search
	 * @return an array of UPNP Root device that matches the search or null if
	 *         nothing found with the given timeout. Null does NOT means that no
	 *         UPNP device is available on the network. It only means that for this
	 *         given timeout no devices responded or that effectively no devices are
	 *         available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(int timeOut, String searchTarget) throws IOException {
		return discover(timeOut, DEFAULT_TTL, DEFAULT_MX, searchTarget);
	}

	/**
	 * Devices discovering on all network interfaces with a given timeout and a
	 * given root device to search, as well as a ttl and mx param
	 * 
	 * @param timeOut
	 *            the timeout for the a device to give a reponse
	 * @param ttl
	 *            the UDP socket packets time to live
	 * @param mx
	 *            discovery message mx http header field value
	 * @param searchTarget
	 *            the device URI to search
	 * @return an array of UPNP Root device that matches the search or null if
	 *         nothing found within the given timeout. Null return does NOT means
	 *         that no UPNP device is available on the network. It only means that
	 *         for this given timeout no devices responded or that effectively no
	 *         devices are available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(int timeOut, int ttl, int mx, String searchTarget) throws IOException {
		return discoverDevices(timeOut, ttl, mx, searchTarget, null);
	}

	/**
	 * Devices discovering with a given timeout and a given root device to search on
	 * an given network interface, as well as a ttl and mx param
	 * 
	 * @param timeOut
	 *            the timeout for the a device to give a reponse
	 * @param ttl
	 *            the UDP socket packets time to live
	 * @param mx
	 *            discovery message mx http header field value
	 * @param searchTarget
	 *            the device URI to search
	 * @param ni
	 *            the networkInterface where to search devices, null to lookup all
	 *            interfaces
	 * @return an array of UPNP Root device that matches the search or null if
	 *         nothing found within the given timeout. Null return does NOT means
	 *         that no UPNP device is available on the network. It only means that
	 *         for this given timeout no devices responded or that effectively no
	 *         devices are available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(int timeOut, int ttl, int mx, String searchTarget, NetworkInterface ni)
			throws IOException {
		return discoverDevices(timeOut, ttl, mx, searchTarget, ni);
	}

	private static UPNPRootDevice[] discoverDevices(int timeOut, int ttl, int mx, String searchTarget,
			NetworkInterface ni) throws IOException {
		if (searchTarget == null || searchTarget.trim().length() == 0) {
			throw new IllegalArgumentException("Illegal searchTarget");
		}

		final Map devices = new HashMap();

		DiscoveryResultsHandler handler = new DiscoveryResultsHandler() {

			public void discoveredDevice(String usn, String udn, String nt, String maxAge, URL location,
					String firmware, InetAddress sourceAddress) {
				synchronized (devices) {
					if (!devices.containsKey(usn)) {
						try {
							UPNPRootDevice device = new UPNPRootDevice(location, maxAge, firmware, usn, udn,
									sourceAddress);
							devices.put(usn, device);
						} catch (Exception ex) {
							log.debug("Error occured during upnp root device object creation from location " + location,
									ex);
						}
					}
				}
			}
		};
		List listeners = new ArrayList();
		// DiscoveryListener.getInstance().registerResultsHandler( handler, searchTarget
		// );
		if (ni == null) {
			for (Enumeration e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
				NetworkInterface intf = (NetworkInterface) e.nextElement();
				for (Enumeration adrs = intf.getInetAddresses(); adrs.hasMoreElements();) {
					InetAddress adr = (InetAddress) adrs.nextElement();
					if (adr instanceof Inet4Address && !adr.isLoopbackAddress()) {
						DiscoveryListener listener = new DiscoveryListener(adr);
						listeners.add(listener);
						// System.out.println("Using IPv4 " + intf.getDisplayName() + " " +
						// adr.toString());
						try {
							listener.registerResultsHandler(handler, searchTarget);
							sendSearchMessage((Inet4Address) adr, ttl, mx, searchTarget);
						} catch (Throwable t) {
							// t.printStackTrace();
						}
						// try {
						// Thread.sleep( timeOut );
						// } catch ( InterruptedException ex ) {
						// don't care
						// }
						// listener.unRegisterResultsHandler(handler, searchTarget);
					} else if (adr instanceof Inet6Address && !adr.isLoopbackAddress()) {
						DiscoveryListener listener = new DiscoveryListener(adr);
						listeners.add(listener);
						// System.out.println("Using IPv6 " + intf.getDisplayName() + " " +
						// adr.toString());
						try {
							listener.registerResultsHandler(handler, searchTarget);
							sendSearchMessage((Inet6Address) adr, ttl, mx, searchTarget);
						} catch (Throwable t) {
							// t.printStackTrace();
						}

						// try {
						// Thread.sleep( timeOut );
						// } catch ( InterruptedException ex ) {
						// don't care
						// }
						// listener.unRegisterResultsHandler(handler, searchTarget);
					}
				}
			}
		} else {
			for (Enumeration adrs = ni.getInetAddresses(); adrs.hasMoreElements();) {
				InetAddress adr = (InetAddress) adrs.nextElement();
				if (adr instanceof Inet4Address && !adr.isLoopbackAddress()) {
					DiscoveryListener listener = new DiscoveryListener(adr);
					listeners.add(listener);
					// System.out.println("Using IPv4 " + ni.getDisplayName() + " " +
					// adr.toString());
					try {
						listener.registerResultsHandler(handler, searchTarget);
						sendSearchMessage((Inet4Address) adr, ttl, mx, searchTarget);
					} catch (Throwable t) {
						// t.printStackTrace();
					}
					// try {
					// Thread.sleep( timeOut );
					// } catch ( InterruptedException ex ) {
					// don't care
					// }
					// listener.unRegisterResultsHandler(handler, searchTarget);
				} else if (adr instanceof Inet6Address && !adr.isLoopbackAddress()) {
					DiscoveryListener listener = new DiscoveryListener(adr);
					listeners.add(listener);
					// System.out.println("Using IPv6 " + ni.getDisplayName() + " " +
					// adr.toString());
					try {
						listener.registerResultsHandler(handler, searchTarget);
						sendSearchMessage((Inet6Address) adr, ttl, mx, searchTarget);
					} catch (Throwable t) {
						// t.printStackTrace();
					}

					// try {
					// Thread.sleep( timeOut );
					// } catch ( InterruptedException ex ) {
					// don't care
					// }
					// listener.unRegisterResultsHandler(handler, searchTarget);
				}
			}
		}

		try {
			Thread.sleep(timeOut);
		} catch (InterruptedException ex) {
			// don't care
		}

		// DiscoveryListener.getInstance().unRegisterResultsHandler( handler,
		// searchTarget );
		for (Iterator ite = listeners.iterator(); ite.hasNext();) {
			DiscoveryListener listener = (DiscoveryListener) ite.next();
			listener.unRegisterResultsHandler(handler, searchTarget);
		}
		listeners.clear();

		if (devices.size() == 0) {
			return null;
		}
		int j = 0;
		UPNPRootDevice[] rootDevices = new UPNPRootDevice[devices.size()];
		for (Iterator i = devices.values().iterator(); i.hasNext();) {
			rootDevices[j++] = (UPNPRootDevice) i.next();
			// System.out.println("device: " + rootDevices[j - 1].getURLBase());
		}

		return rootDevices;

	}

	/**
	 * Sends an SSDP search message on the network
	 * 
	 * @param src
	 *            the sender ip
	 * @param ttl
	 *            the time to live
	 * @param mx
	 *            the mx field
	 * @param searchTarget
	 *            the search target
	 * @throws IOException
	 *             if some IO errors occurs during search
	 */
	public static void sendSearchMessage(Inet4Address src, int ttl, int mx, String searchTarget) throws IOException {

		int bindPort = DEFAULT_SSDP_SEARCH_PORT;
		String port = System.getProperty("net.sbbi.upnp.Discovery.bindPort");
		if (port != null) {
			bindPort = Integer.parseInt(port);
		}
		InetSocketAddress adr = new InetSocketAddress(InetAddress.getByName(Discovery.SSDP_IPV4), Discovery.SSDP_PORT);

		java.net.MulticastSocket skt = new java.net.MulticastSocket(null);
		// System.out.println("sendSearchMessage new java.net.MulticastSocket" +
		// src.toString());
		skt.setReuseAddress(true);
		skt.setBroadcast(true);
		skt.bind(new InetSocketAddress(src, bindPort));
		skt.setTimeToLive(ttl);
		StringBuffer packet = new StringBuffer();
		packet.append("M-SEARCH * HTTP/1.1\r\n");
		packet.append("HOST: 239.255.255.250:1900\r\n");
		packet.append("MAN: \"ssdp:discover\"\r\n");
		packet.append("MX: ").append(mx).append("\r\n");
		packet.append("ST: ").append(searchTarget).append("\r\n").append("\r\n");
		if (log.isDebugEnabled())
			log.debug("Sending discovery message on 239.255.255.250:1900 multicast address form ip "
					+ src.getHostAddress() + ":\n" + packet.toString());
		String toSend = packet.toString();
		byte[] pk = toSend.getBytes();
		skt.send(new DatagramPacket(pk, pk.length, adr));
		skt.disconnect();
		skt.close();
		// System.out.println("sendSearchMessage close java.net.MulticastSocket" +
		// src.toString());
	}

	public static void sendSearchMessage(Inet6Address src, int ttl, int mx, String searchTarget) throws IOException {

		int bindPort = DEFAULT_SSDP_SEARCH_PORT;
		String port = System.getProperty("net.sbbi.upnp.Discovery.bindPort");
		if (port != null) {
			bindPort = Integer.parseInt(port);
		}
		InetSocketAddress adr = new InetSocketAddress(InetAddress.getByName(Discovery.SSDP_IPV6), Discovery.SSDP_PORT);

		java.net.MulticastSocket skt = new java.net.MulticastSocket(null);
		// System.out.println("sendSearchMessage new java.net.MulticastSocket" +
		// src.toString());
		skt.setReuseAddress(true);
		skt.setBroadcast(true);
		skt.bind(new InetSocketAddress(src, bindPort));
		skt.setTimeToLive(ttl);
		StringBuffer packet = new StringBuffer();
		packet.append("M-SEARCH * HTTP/1.1\r\n");
		packet.append("HOST: [ff05::c]:1900\r\n");
		packet.append("MAN: \"ssdp:discover\"\r\n");
		packet.append("MX: ").append(mx).append("\r\n");
		packet.append("ST: ").append(searchTarget).append("\r\n").append("\r\n");
		if (log.isDebugEnabled())
			log.debug("Sending discovery message on [ff05::c]:1900 multicast address form ip " + src.getHostAddress()
					+ ":\n" + packet.toString());
		String toSend = packet.toString();
		byte[] pk = toSend.getBytes();
		skt.send(new DatagramPacket(pk, pk.length, adr));
		skt.disconnect();
		skt.close();
		// System.out.println("sendSearchMessage close java.net.MulticastSocket" +
		// src.toString());
	}

}
