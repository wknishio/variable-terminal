/*
 * Copyright 2013-2016, Kasra Faghihi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.offbynull.portmapper.helpers;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
//import java.util.Arrays;
import org.apache.commons.lang3.Validate;
import org.vash.vate.compatibility.VTArrays;

/**
 * Network-related utility class.
 * @author Kasra Faghihi
 */
public final class NetworkUtils {

    /**
     * An address that represents an all zero IPv6 address (::).
     */
    public static final InetAddress ZERO_IPV6;
    /**
     * An address that represents an all zero IPv4 address (0.0.0.0).
     */
    public static final InetAddress ZERO_IPV4;
    static {
        try {
            ZERO_IPV6 = InetAddress.getByName("::");
            ZERO_IPV4 = InetAddress.getByName("0.0.0.0");
        } catch (UnknownHostException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private NetworkUtils() {
        // do nothing
    }

    /**
     * Convert host to {@link InetAddress}. Equivalent to calling {@link InetAddress#getByName(java.lang.String) } but throws an unchecked
     * exception on problem.
     * @param host host to convert
     * @return address converted address
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if host couldn't be converted to an {@link InetAddress}
     */
    public static InetAddress toAddress(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException uhe) {
            throw new IllegalArgumentException(uhe);
        }
    }

    /**
     * Convert host and port to {@link InetSocketAddress}. Equivalent to calling
     * {@link InetSocketAddress#InetSocketAddress(java.net.InetAddress, int) } but throws an unchecked exception on problem.
     * @param host host to convert
     * @param port port of host
     * @return address converted address
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if host couldn't be converted to an {@link InetSocketAddress}
     */
    public static InetSocketAddress toSocketAddress(String host, int port) {
        try {
            return new InetSocketAddress(InetAddress.getByName(host), port);
        } catch (UnknownHostException uhe) {
            throw new IllegalArgumentException(uhe);
        }
    }

    /**
     * Gets the address as an IPv6 address string. If {@code address} is a {@link Inet4Address}, it'll be converted to an IPv4 mapped to
     * IPv5 address. If {@code address} is a {@link Inet6Address}, it'll return {@code address.getHostAddress()}.
     * <p>
     * This method is required because Java prevents you from creating an 'IPv4 mapped to IPv6' address as an {@link Inet6Address}.
     * @param address address to convert to string
     * @return address as an IPv6 address string
     */
    public static String toIpv6AddressString(InetAddress address) {
        if (address instanceof Inet4Address) {
            byte[] bytes = address.getAddress();
            int block1 = ((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff);
            int block2 = ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff);
            return "::ffff:" + Integer.toHexString(block1) + ":" + Integer.toHexString(block2);
        } else if (address instanceof Inet6Address) {
            return address.getHostAddress();
        } else {
            throw new IllegalStateException(); // should never happen
        }
    }
    
    /**
     * Convert a IP address to an IPv6 address and dump as a byte array. If the input is IPv4 it'll be converted to an IPv4-to-IPv6 address.
     * Otherwise, the IPv6 address will be dumped as-is.
     * @param address address to convert to a ipv6 byte array
     * @return ipv6 byte array
     * @throws NullPointerException if any argument is {@code null}
     */
    public static byte[] convertAddressToIpv6Bytes(InetAddress address) {
        Validate.notNull(address);
        
        byte[] addrArr = address.getAddress();
        switch (addrArr.length) {
            case 4: {
                // convert ipv4 address to ipv4-mapped ipv6 address
                byte[] newAddrArr = new byte[16];
                newAddrArr[10] = (byte) 0xff;
                newAddrArr[11] = (byte) 0xff;
                System.arraycopy(addrArr, 0, newAddrArr, 12, 4);
                
                return newAddrArr;
            }
            case 16: {
                return addrArr;
            }
            default:
                throw new IllegalStateException(); // should never happen
        }
    }
    
    /**
     * Convert a byte array to an IP address. Equivalent to doing ...
     * <pre>
     * try {
     *     return = InetAddress.getByAddress(buffer);
     * } catch (UnknownHostException uhe) {
     *     throw new IllegalStateException(uhe); // should never happen
     * }
     * </pre>
     * @param buffer buffer to convert
     * @return IP address
     * @throws IllegalArgumentException if could not be converted to an IP address (almost always because {@code buffer.length} is not 4 or
     * 16)
     */
    public static InetAddress convertBytesToAddress(byte[] buffer) {
        return NetworkUtils.convertBytesToAddress(buffer, 0, buffer.length);
    }

    /**
     * Convert a byte array to an IP address. Equivalent to doing ...
     * <pre>
     * byte[] addr = Arrays.copyOfRange(buffer, offset, offset + length);
     * try {
     *     return = InetAddress.getByAddress(addr);
     * } catch (UnknownHostException uhe) {
     *     throw new IllegalStateException(uhe); // should never happen
     * }
     * </pre>
     * @param buffer buffer containing the IP to convert
     * @param offset offset IP bytes start at
     * @param length length of the IP bytes
     * @return IP address
     * @throws IllegalArgumentException if could not be converted to an IP address (almost always because {@code length} is not 4 or 16)
     */
    public static InetAddress convertBytesToAddress(byte[] buffer, int offset, int length) {
        byte[] addr = VTArrays.copyOfRange(buffer, offset, offset + length);
        try {
            InetAddress ret = InetAddress.getByAddress(addr);
            return ret;
        } catch (UnknownHostException uhe) {
            throw new IllegalArgumentException(uhe); // should never happen
        }
    }
}
