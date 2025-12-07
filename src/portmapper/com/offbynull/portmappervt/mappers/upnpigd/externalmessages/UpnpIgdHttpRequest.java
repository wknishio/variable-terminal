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
package com.offbynull.portmappervt.mappers.upnpigd.externalmessages;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3vt.Validate;
import org.vash.vate.compatibility.VTObjects;

/**
 * Represents a UPnP-IGD request. Note that these messages aren't bound to any specific protocol. Some will be sent over UDP broadcast and
 * others will be sent via TCP (HTTP).
 * @author Kasra Faghihi
 */
public abstract class UpnpIgdHttpRequest {

    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String TERMINATOR = "\r\n";
//    private static final String HEADER_SPLIT_POINT = TERMINATOR + TERMINATOR;

    private final String method;
    private final String location;
    private final Map<String, String> headers;
    private final String content;

    UpnpIgdHttpRequest(String method, String location, Map<String, String> headers, String content) {
        Validate.notNull(method);
        Validate.notNull(location);
        Validate.notNull(headers);
        Validate.noNullElements(headers.keySet());
        Validate.noNullElements(headers.values());
//        Validate.notNull(content); // content may be null

        // content len calculated on dump
        for (String header : headers.keySet()) {
            if (header.equalsIgnoreCase("Content-Length")) {
                throw new IllegalArgumentException();
            }
        }

        this.method = method;
        this.location = location;
        this.headers = new LinkedHashMap<String, String>(headers);
        this.content = content;
    }

    /**
     * Dump out the UPnP-IGD request as a buffer.
     * @return UPnP-IGD packet/buffer
     */
    public final byte[] dump() {
        StringBuilder sb = new StringBuilder();

        sb.append(method).append(' ').append(location).append(' ').append(HTTP_VERSION).append(TERMINATOR);
        for (Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(TERMINATOR);
        }
        
        if (content != null) {
            byte[] contentBytes = {};
			try
			{
				contentBytes = content.getBytes(Charset.forName("US-ASCII").toString());
			}
			catch (UnsupportedEncodingException e)
			{
				
			}
         
            sb.append("Content-Length: ").append(contentBytes.length).append(TERMINATOR);
            sb.append(TERMINATOR); // split
            byte[] headerBytes = {};
			try
			{
				headerBytes = sb.toString().getBytes(Charset.forName("US-ASCII").toString());
			}
			catch (UnsupportedEncodingException e)
			{
				
			}
            
            byte[] finalBytes = new byte[contentBytes.length + headerBytes.length];
            System.arraycopy(headerBytes, 0, finalBytes, 0, headerBytes.length);
            System.arraycopy(contentBytes, 0, finalBytes, headerBytes.length, contentBytes.length);
            
            return finalBytes;
        } else {
            sb.append(TERMINATOR); // split
            try
			{
				return sb.toString().getBytes(Charset.forName("US-ASCII").toString());
			}
			catch (UnsupportedEncodingException e)
			{
				return "".getBytes();
			}
        }
    }

    // CHECKSTYLE:OFF:DesignForExtension
    
    public String toString() {
        return "UpnpIgdHttpRequest{" + "method=" + method + ", location=" + location + ", headers=" + headers + ", content=" + content
                + '}';
    }
    
    
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + VTObjects.hashCode(this.method);
        hash = 11 * hash + VTObjects.hashCode(this.location);
        hash = 11 * hash + VTObjects.hashCode(this.headers);
        hash = 11 * hash + VTObjects.hashCode(this.content);
        return hash;
    }

    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UpnpIgdHttpRequest other = (UpnpIgdHttpRequest) obj;
        if (!VTObjects.equals(this.method, other.method)) {
            return false;
        }
        if (!VTObjects.equals(this.location, other.location)) {
            return false;
        }
        if (!VTObjects.equals(this.content, other.content)) {
            return false;
        }
        if (!VTObjects.equals(this.headers, other.headers)) {
            return false;
        }
        return true;
    }
    // CHECKSTYLE:ON:DesignForExtension
}
