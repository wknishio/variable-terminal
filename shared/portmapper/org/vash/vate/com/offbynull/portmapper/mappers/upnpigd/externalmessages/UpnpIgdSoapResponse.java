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
package org.vash.vate.com.offbynull.portmapper.mappers.upnpigd.externalmessages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.vash.vate.com.offbynull.portmapper.helpers.TextUtils;
import org.vash.vate.org.apache.commons.lang3.StringEscapeUtils;
import org.vash.vate.org.apache.commons.lang3.StringUtils;
import org.vash.vate.org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP device query request.
 *
 * @author Kasra Faghihi
 */
public abstract class UpnpIgdSoapResponse extends UpnpIgdHttpResponse {

    private Map<String, String> arguments;
    
    UpnpIgdSoapResponse(String expectedResponseAction, Set<String> expectedArguments, byte[] buffer) {
        super(buffer);
        
        Validate.notNull(expectedArguments);
        Validate.noNullElements(expectedArguments);
//        validateResponseCode();

        String content = getContent();

//        if (!isResponseSuccessful()) {
//            throw new IllegalArgumentException("Response contains fault: " + content);
//        }
        
        //System.out.println("UpnpIgdSoapResponse.content:" + content);
        //System.out.println("UpnpIgdSoapResponse.expectedResponseAction:" + expectedResponseAction);
        String responseBlock = TextUtils.findFirstBlock(content,
                ///*<soapprefix*/":" + expectedResponseAction,
                //*</soapprefix:*/":" + expectedResponseAction, true);
        		":" + expectedResponseAction,
        		":" + expectedResponseAction, true);
        //System.out.println("UpnpIgdSoapResponse.responseBlock:" + responseBlock);
        if (responseBlock == null)
        {
        	//for empty xml responses
        	responseBlock = TextUtils.findFirstBlock(content,
                    ///*<soapprefix*/":" + expectedResponseAction,
                    //*</soapprefix:*/":" + expectedResponseAction, true);
            		":" + expectedResponseAction,
            		"/>", true);
        }
        Validate.isTrue(responseBlock != null);
        
        Map<String, String> args = new HashMap<String, String>();
        for (String key : expectedArguments) {
            // A really hacky way of finding args -- reason why the whole tag isn't used is because the soap prefix in the tag isn't
            // consistent... we'd have to do more hacky parsing to figure out what it is
            String value = TextUtils.findFirstBlock(responseBlock, key + ">", key + ">", true);
            value = StringUtils.substringBeforeLast(value, "<");
            if (value != null) {
                value = StringEscapeUtils.unescapeXml(value).trim();
                args.put(key, value);
            }
        }

        arguments = Collections.unmodifiableMap(args);
        
        //<?xml version="1.0"?>
        //
        //<soap:Envelope
        //xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
        //soap:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
        //<soap:Body>
        //  <soap:Fault>
        //  ...
        //  </soap:Fault>
        //</soap:Body>
        //</soap:Envelope>

        //<?xml version="1.0"?>
        //
        //<soap:Envelope
        //xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
        //soap:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
        //<soap:Body>
        //  <soap:Fault>
        //  ...
        //  </soap:Fault>
        //</soap:Body>
        //</soap:Envelope>
    }
    
    final String getArgumentIgnoreCase(String key) {
        for (Map.Entry<String, String> header : arguments.entrySet()) {
            if (header.getKey().equalsIgnoreCase(key)) {
                return header.getValue().trim();
            }
        }
        return null;
    }

    // CHECKSTYLE:OFF:DesignForExtension
    
    public String toString() {
        return "UpnpIgdSoapResponse{super=" + super.toString() + "arguments=" + arguments + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
    // CHECKSTYLE:ON:DesignForExtension
}
