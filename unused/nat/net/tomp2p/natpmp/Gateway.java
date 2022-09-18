/*******************************************************************************
 * Copyright 2012 Thomas Bocek
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package net.tomp2p.natpmp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Gateway {
	public static InetAddress[] getIPs() {
		Pattern ipv4Pattern = Pattern.compile(
				"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
		Pattern ipv6Pattern = Pattern.compile(
				"((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:)))(%.+)?");
		HashSet<InetAddress> set = new HashSet<InetAddress>();
		// Try to determine the gateway.
		try {
			// Run netstat. This gets the table of routes.
			Process proc = Runtime.getRuntime().exec("netstat -rn");

			InputStream inputstream = proc.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

			// Parse the result.
			String line;
			while ((line = bufferedreader.readLine()) != null) {
				if (line.indexOf("default") >= 0 || line.indexOf("0.0.0.0") >= 0 || line.indexOf(" :: ") >= 0
						|| line.indexOf("::0") >= 0 || line.indexOf("0:0:0:0:0:0:0:0") >= 0
						|| line.indexOf("00:00:00:00:00:00:00:00") >= 0
						|| line.indexOf("0000:0000:0000:0000:0000:0000:0000:0000") >= 0) {
					// this is a line with a gateway IP, search for the first good entry.
					Matcher m4 = ipv4Pattern.matcher(line);
					Matcher m6 = ipv6Pattern.matcher(line);

					int start = 0;
					while (m4.find(start)) {
						String tmp = m4.group();
						if (tmp.indexOf("0.0.0.0") < 0) {
							set.add(InetAddress.getByName(tmp));
						}
						start = m4.end() + 1;
					}

					start = 0;
					while (m6.find(start)) {
						String tmp = m6.group();
						if (!tmp.equals("::") || tmp.indexOf("::0") < 0 || tmp.indexOf("0:0:0:0:0:0:0:0") < 0
								|| tmp.indexOf("00:00:00:00:00:00:00:00") < 0
								|| tmp.indexOf("0000:0000:0000:0000:0000:0000:0000:0000") < 0) {
							set.add(InetAddress.getByName(tmp));
						}
						start = m6.end() + 1;
					}
				}
			}
		} catch (IOException ex) {

		}
		return set.toArray(new InetAddress[] {});
	}

	
//	public static void main(String[] args)
//	{
//		System.out.println(Arrays.toString(Gateway.getIPs()));
//	}
	
}