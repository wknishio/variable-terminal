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
package net.sbbi.upnp.jmx;

import java.util.*;

/**
 * A class to parse an HTTP request message.
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class HttpRequest {

	private String httpCommand;
	private String httpCommandArg;
	private Map fields;
	private String body;

	/**
	 * Constructor of the http request, will try to parse the raw request data
	 * 
	 * @param rawHttpRequest
	 *            the raw request data
	 */
	public HttpRequest(String rawHttpRequest) {
		if (rawHttpRequest.trim().length() == 0) {
			throw new IllegalArgumentException("Empty HTTP request message");
		}
		boolean bodyParsing = false;
		StringBuffer bodyParsed = new StringBuffer();
		fields = new HashMap();
		String[] lines = rawHttpRequest.split("\\r\\n");
		String header = lines[0].trim();
		int space = header.indexOf(" ");
		if (space != -1) {
			httpCommand = header.substring(0, space);
			int space2 = header.indexOf(" ", space + 1);
			if (space2 != -1) {
				httpCommandArg = header.substring(space + 1, space2);
			}
		}

		for (int i = 1; i < lines.length; i++) {

			String line = lines[i];
			if (line.length() == 0) {
				// line break before body
				bodyParsing = true;
			} else if (bodyParsing) {
				// we parse the message body
				bodyParsed.append(line).append("\r\n");
			} else {
				// we parse the header
				if (line.length() > 0) {
					int delim = line.indexOf(':');
					if (delim != -1) {
						String key = line.substring(0, delim).toUpperCase();
						String value = line.substring(delim + 1).trim();
						fields.put(key, value);
					}
				}
			}
		}
		if (bodyParsing) {
			body = bodyParsed.toString();
		}
	}

	public String getHttpCommand() {
		return httpCommand;
	}

	public String getHttpCommandArg() {
		return httpCommandArg;
	}

	public String getBody() {
		return body;
	}

	public String getHTTPFieldElement(String fieldName, String elementName) throws IllegalArgumentException {
		String fieldNameValue = getHTTPHeaderField(fieldName);
		if (fieldName != null) {

			StringTokenizer tokenizer = new StringTokenizer(fieldNameValue.trim(), ",");
			while (tokenizer.countTokens() > 0) {
				String nextToken = tokenizer.nextToken().trim();
				if (nextToken.startsWith(elementName)) {
					int index = nextToken.indexOf("=");
					if (index != -1) {
						return nextToken.substring(index + 1);
					}
				}
			}
		}
		return null;
	}

	public String getHTTPHeaderField(String fieldName) throws IllegalArgumentException {
		return (String) fields.get(fieldName.toUpperCase());
	}

}
