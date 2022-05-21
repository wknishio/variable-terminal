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
package net.sbbi.upnp.samples;

/*import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import net.sbbi.upnp.jmx.UPNPMBeanDevice;
import net.sbbi.upnp.jmx.upnp.UPNPConnectorServer;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;*/

/**
 * Sample class to expose JRE 1.5 JVM MBeans as UPNP devices
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class JDK5ManagmentAsUpnp {

	public static void main(String[] args) {

		/*
		 * Runnable runnable = new Runnable() {
		 * 
		 * public void run() { UPNPMBeanDevice jdk5Device = new UPNPMBeanDevice(
		 * "JDK5Management", 1, "SBBI", "JDK 5 UPNP JMX Interface", "UPNPLIB", null );
		 * UPNPMBeanDevice jdk5Device2 = new UPNPMBeanDevice( "JDK5Management", 1,
		 * "SBBI", "JDK 5 UPNP JMX Classloader Interface", "UPNPLIB", "BEAN2" );
		 * 
		 * try { MBeanServer defaultServer = ManagementFactory.getPlatformMBeanServer();
		 * 
		 * JMXServiceURL url = new JMXServiceURL( "service:jmx:upnp://192.168.0.3:8080"
		 * ); Map env = new HashMap(); env.put(
		 * JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES, "net.sbbi.upnp.jmx" );
		 * env.put( UPNPConnectorServer.EXPOSE_MBEANS_AS_UPNP_DEVICES, Boolean.TRUE );
		 * env.put( UPNPConnectorServer.EXPOSE_EXISTING_MBEANS_AS_UPNP_DEVICES,
		 * Boolean.TRUE );
		 * 
		 * JMXConnectorServer connectorServer =
		 * JMXConnectorServerFactory.newJMXConnectorServer( url, env, null ); String
		 * upnpConnectorBeanName =
		 * "Connectors:protocol=upnp,host=192.168.0.3,port=8080"; ObjectName
		 * cntorServerName = ObjectName.getInstance( upnpConnectorBeanName );
		 * defaultServer.registerMBean( connectorServer, cntorServerName );
		 * connectorServer.start();
		 * 
		 * RuntimeMXBean mbean = ManagementFactory.getRuntimeMXBean(); ObjectName name =
		 * new ObjectName( ManagementFactory.RUNTIME_MXBEAN_NAME );
		 * jdk5Device.addService( mbean, name, defaultServer, "RUNTIME_MXBEAN",
		 * "RUNTIME_MXBEAN_TYPE", 1 ); ThreadMXBean mbeanThread =
		 * ManagementFactory.getThreadMXBean(); name = new ObjectName(
		 * ManagementFactory.THREAD_MXBEAN_NAME ); jdk5Device.addService( mbeanThread,
		 * name, defaultServer, "THREAD_MXBEAN_NAME", "THREAD_MXBEAN_NAME_TYPE", 1 );
		 * 
		 * MemoryMXBean mbeanMem = ManagementFactory.getMemoryMXBean(); name = new
		 * ObjectName( ManagementFactory.MEMORY_MXBEAN_NAME ); jdk5Device.addService(
		 * mbeanMem, name, defaultServer, "MEMORY_MXBEAN_NAME",
		 * "MEMORY_MXBEAN_NAME_TYPE", 1 );
		 * 
		 * ClassLoadingMXBean clBean = ManagementFactory.getClassLoadingMXBean(); name =
		 * new ObjectName( ManagementFactory.CLASS_LOADING_MXBEAN_NAME );
		 * jdk5Device2.addService( clBean, name, defaultServer,
		 * "CLASS_LOADING_MXBEAN_NAME", "CLASS_LOADING_MXBEAN_NAME_TYPE", 1 );
		 * 
		 * jdk5Device.addChildMBean( jdk5Device2 ); System.out.println(
		 * "devices created" ); jdk5Device.start(); System.out.println(
		 * "devices started" ); } catch ( Exception ex ) { ex.printStackTrace(); return;
		 * } while ( true ) { try { Thread.sleep( 1000 ); } catch ( InterruptedException
		 * intEx ) { Thread.currentThread().interrupt(); break; } } try {
		 * jdk5Device.stop(); System.out.println( "devices stopped" ); } catch (
		 * Exception ex ) { ex.printStackTrace(); } } };
		 * 
		 * Thread thrd = new Thread( runnable ); thrd.setDaemon( false ); thrd.start();
		 */

	}
}
