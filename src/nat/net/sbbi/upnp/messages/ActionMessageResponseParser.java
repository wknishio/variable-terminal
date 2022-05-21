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
package net.sbbi.upnp.messages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;

import net.sbbi.upnp.services.*;

/**
 * Simple SAX handler for UPNP response message parsing, this message is in SOAP
 * format
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class ActionMessageResponseParser extends org.xml.sax.helpers.DefaultHandler {

	private final static Log log = LogFactory.getLog(ActionMessageResponseParser.class);

	private final static String SOAP_FAULT_EL = "Fault";

	private ServiceAction serviceAction;
	private String bodyElementName;
	private boolean faultResponse = false;
	private UPNPResponseException msgEx;

	private boolean readFaultCode = false;
	private boolean readFaultString = false;
	private boolean readErrorCode = false;
	private boolean readErrorDescription = false;
	private boolean parseOutputParams = false;
	private ActionResponse result;
	private ServiceActionArgument parsedResultOutArg;

	private StringBuffer parsedResultBuilder = new StringBuffer();

	protected ActionMessageResponseParser(ServiceAction serviceAction) {
		this.serviceAction = serviceAction;
		bodyElementName = serviceAction.getName() + "Response";
	}

	protected UPNPResponseException getUPNPResponseException() {
		return msgEx;
	}

	protected ActionResponse getActionResponse() {
		return result;
	}

	public void characters(char[] ch, int start, int length) {
		if (parseOutputParams) {
			if (parsedResultOutArg != null) {
				parsedResultBuilder.append(ch, start, length);
			}
		} else if (readFaultCode) {
			msgEx.faultCode = new String(ch, start, length);
			readFaultCode = false;
		} else if (readFaultString) {
			msgEx.faultString = new String(ch, start, length);
			readFaultString = false;
		} else if (readErrorCode) {
			String code = new String(ch, start, length);
			try {
				msgEx.detailErrorCode = Integer.parseInt(code);
			} catch (Throwable ex) {
				log.debug("Error during returned error code " + code + " parsing");
			}
			readErrorCode = false;
		} else if (readErrorDescription) {
			msgEx.detailErrorDescription = new String(ch, start, length);
			readErrorDescription = false;
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (parseOutputParams) {
			ServiceActionArgument arg = serviceAction.getActionArgument(localName);
			if (arg != null && arg.getDirection() == ServiceActionArgument.DIRECTION_OUT) {
				parsedResultOutArg = arg;
				result.addResult(parsedResultOutArg, null);
			} else {
				parsedResultOutArg = null;
			}
		} else if (faultResponse) {
			if (localName.equals("faultcode")) {
				readFaultCode = true;
			} else if (localName.equals("faultstring")) {
				readFaultString = true;
			} else if (localName.equals("errorCode")) {
				readErrorCode = true;
			} else if (localName.equals("errorDescription")) {
				readErrorDescription = true;
			}
		} else if (localName.equals(SOAP_FAULT_EL)) {
			msgEx = new UPNPResponseException();
			faultResponse = true;
		} else if (localName.equals(bodyElementName)) {
			parseOutputParams = true;
			result = new ActionResponse();
		}
	}

	public void endElement(String uri, String localName, String qName) {
		if (parsedResultOutArg != null && parsedResultOutArg.getName().equals(localName)) {
			result.addResult(parsedResultOutArg, parsedResultBuilder.toString());
			parsedResultOutArg = null;
			parsedResultBuilder = new StringBuffer();
		} else if (localName.equals(bodyElementName)) {
			parseOutputParams = false;
		}
	}
}
