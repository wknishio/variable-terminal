/*
 * Copyright 2017 Guilherme Chaguri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guichaguri.minimalftp.handler;

import com.guichaguri.minimalftp.FTPConnection;
import com.guichaguri.minimalftp.FTPServer;
import com.guichaguri.minimalftp.Utils;
import com.guichaguri.minimalftp.api.IUserAuthenticator;
import com.guichaguri.minimalftp.api.CommandInfo.Command;
import com.guichaguri.minimalftp.api.IUserAuthenticator.AuthException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Handles special connection-based commands
 * @author Guilherme Chaguri
 */
public class FTPConnectionHandler {

    protected final FTPConnection con;

    protected InetAddress address = null;
    protected boolean authenticated = false;
    protected String username = null;

    protected boolean passive = false;
    protected ServerSocket passiveServer = null;
    protected String activeHost = null;
    protected int activePort = 0;

    protected boolean ascii = true;
    protected boolean secureData = false;
    protected boolean stop = false;

    public FTPConnectionHandler(FTPConnection connection) {
        this.con = connection;
    }

    public boolean shouldStop() {
        return stop;
    }

    public boolean isAsciiMode() {
        return ascii;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getUsername() {
        return username;
    }

    public Socket createDataSocket() throws IOException {
        if(passive && passiveServer != null) {
            return passiveServer.accept();
        } else if(secureData) {
            SSLSocketFactory factory = con.getServer().getSSLContext().getSocketFactory();
            SSLSocket socket = (SSLSocket)factory.createSocket(activeHost, activePort);
            socket.setUseClientMode(false);
            return socket;
        } else {
            return new Socket(activeHost, activePort);
        }
    }

    public void onConnected() throws IOException {
        IUserAuthenticator<?> auth = con.getServer().getAuthenticator();

        if(!auth.needsUsername(con)) {
            if(authenticate(auth, null)) {
                con.sendResponse(230, "Ready!");
            } else {
                con.sendResponse(421, "Authentication failed");
                con.close();
            }
        } else {
            con.sendResponse(220, "Waiting for authentication...");
        }
    }

    public void onDisconnected() throws IOException {
        if(passiveServer != null) {
            Utils.closeQuietly(passiveServer);
            passiveServer = null;
        }
    }

    public void registerCommands() {
        boolean passiveAvailable = con.getServer().getPassiveModeEnabled();

        con.registerCommand("NOOP", "NOOP", noop(), false); // Ping
        con.registerCommand("HELP", "HELP <command>", help(), false); // Command Help
        con.registerCommand("QUIT", "QUIT", quit(), false); // Quit
        con.registerCommand("REIN", "REIN", rein(), false); // Logout
        con.registerCommand("USER", "USER <username>", user(), false); // Set Username
        con.registerCommand("PASS", "PASS <password>", pass(), false); // Set Password
        con.registerCommand("ACCT", "ACCT <info>", acct(), false); // Account Info
        con.registerCommand("SYST", "SYST", syst()); // System Information
        if (passiveAvailable) con.registerCommand("PASV", "PASV", pasv()); // Passive Mode
        con.registerCommand("PORT", "PORT <address>", port()); // Active Mode
        con.registerCommand("TYPE", "TYPE <type>", type()); // Binary Flag
        con.registerCommand("STRU", "STRU <type>", stru()); // Structure Type
        con.registerCommand("MODE", "MODE <mode>", mode()); // Change Mode
        con.registerCommand("STAT", "STAT", stat()); // Statistics

        con.registerCommand("AUTH", "AUTH <mechanism>", auth(), false); // Security Mechanism (RFC 2228)
        con.registerCommand("PBSZ", "PBSZ <size>", pbsz(), false); // Protection Buffer Size (RFC 2228)
        con.registerCommand("PROT", "PROT <level>", prot(), false); // Data Channel Protection Level (RFC 2228)

        if (passiveAvailable) con.registerCommand("LPSV", "LPSV", lpsv()); // Long Passive Mode (RFC 1639) (Obsolete)
        con.registerCommand("LPRT", "LPRT <address>", lprt()); // Long Active Mode (RFC 1639) (Obsolete)

        if (passiveAvailable) con.registerCommand("EPSV", "EPSV", epsv()); // Extended Passive Mode (RFC 2428)
        con.registerCommand("EPRT", "EPRT <address>", eprt()); // Extended Active Mode (RFC 2428)

        con.registerCommand("HOST", "HOST <address>", host(), false); // Custom Virtual Hosts (RFC 7151)

        con.registerFeature("base"); // Base Commands (RFC 5797)
        con.registerFeature("secu"); // Security Commands (RFC 5797)
        con.registerFeature("hist"); // Obsolete Commands (RFC 5797)
        con.registerFeature("nat6"); // Extended Passive/Active Commands (RFC 5797)
        con.registerFeature("TYPE A;AN;AT;AC;L;I"); // Supported Types (RFC 5797)
        con.registerFeature("AUTH TLS"); // SSL/TLS support (RFC 4217)
        con.registerFeature("PBSZ"); // Protection Buffer Size (RFC 2228)
        con.registerFeature("PROT"); // Protection Level (RFC 2228)
        if (passiveAvailable) con.registerFeature("EPSV"); // Extended Passive Mode (RFC 2428)
        con.registerFeature("EPRT"); // Extended Active Mode (RFC 2428)
        con.registerFeature("HOST"); // Custom Virtual Hosts (RFC 7151)
    }

    protected Command noop()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          con.sendResponse(200, "OK");
        }
      };
    }

    protected Command help()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String[] cmd = parms.split("\\s+");
          if(cmd.length < 1) {
            con.sendResponse(501, "Missing parameters");
          }
          String command = cmd[0].toUpperCase();
          String help;
          if(cmd.length > 1 && command.equals("SITE")) {
              help = "SITE " + con.getSiteHelpMessage(cmd[1].toUpperCase());
          } else {
              help = con.getHelpMessage(command);
          }
          con.sendResponse(214, help);
        }
      };
        
    }

    protected Command type()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String type = parms;
          type = type.toUpperCase();
          if(type.startsWith("A")) {
              ascii = true;
          } else if(type.startsWith("L") || type.startsWith("I")) {
              ascii = false;
          } else {
              con.sendResponse(500, "Unknown type " + type);
              return;
          }
          con.sendResponse(200, "Type set to " + type);
        }
      };
    }

    protected Command stru()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String structure = parms;
          if(structure.equalsIgnoreCase("F")) {
              con.sendResponse(200, "The structure type was set to file");
          } else {
              con.sendResponse(504, "Unsupported structure type");
          }
        }
      };
    }

    protected Command mode()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String mode = parms;
          if(mode.equalsIgnoreCase("S")) {
              con.sendResponse(200, "The mode was set to stream");
          } else {
              con.sendResponse(504, "Unsupported mode");
          }
        }
      };
    }

    protected Command host()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String host = parms;
          if(authenticated) {
              con.sendResponse(503, "The user is already authenticated");
              return;
          }
          try {
              IUserAuthenticator<?> auth = con.getServer().getAuthenticator();
              InetAddress hostAddress = InetAddress.getByName(host);
              if(auth.acceptsHost(con, hostAddress)) {
                  address = hostAddress;
                  con.sendResponse(220, "Host accepted");
              } else {
                  address = null;
                  con.sendResponse(504, "Host denied");
              }
          } catch(UnknownHostException ex) {
              con.sendResponse(501, "Invalid host");
          }
        }
      };
        
    }

    protected Command user()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String _username = parms;
          if(authenticated) {
              con.sendResponse(230, "Logged in!");
              return;
          }
          username = _username;
          IUserAuthenticator<?> auth = con.getServer().getAuthenticator();
          if(auth.needsPassword(con, username, address)) {
              // Requests a password for the authentication
              con.sendResponse(331, "Needs a password");
          } else {
              // Tries to authenticate using the given username
              boolean success = authenticate(auth, null);

              if(success) {
                  con.sendResponse(230, "Logged in!");
              } else {
                  con.sendResponse(530, "Authentication failed");
                  con.close();
              }
          }
        }
      };
    }

    protected Command pass()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String password = parms;
          if(authenticated) {
              con.sendResponse(230, "Logged in!");
              return;
          }
          // Tries to authenticate using the given username and password
          boolean success = authenticate(con.getServer().getAuthenticator(), password);
          if(success) {
              con.sendResponse(230, "Logged in!");
          } else {
              con.sendResponse(530, "Authentication failed");
              con.close();
          }
        }
      };
    }

    protected Command acct()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          //String info = parms;
          if(authenticated) {
              con.sendResponse(230, "Logged in!");
              return;
          }
          // Many clients don't even support this command, it's not needed in most cases
          // A simple "username and password" combination is the most common system in the internet anyway
          // The authenticator can also handle special formatted usernames, if really needed (for instance: "username|account")
          // Although this is pretty simple to implement, I would have to store the password
          // in a field instead of directly sending it to the authenticator. I prefer to keep
          // things the way they are for security reasons.
          con.sendResponse(530, "Account information is not supported");
        }
      };
    }

    protected Command syst()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          con.sendResponse(215, "UNIX Type: L8"); // Generic System Info
        }
      };
    }

    protected Command rein()
    {
      return new Command()
      {
        public void run(String parms) throws IOException {
          authenticated = false;
          username = null;
          address = null;
          con.sendResponse(220, "Ready for a new user");
        }
      };
    }

    protected Command quit()
    {
      return new Command()
      {
        public void run(String parms) throws IOException {
          con.sendResponse(221, "Closing connection...");
          stop = true;
        }
      };
    }

    protected Command pasv()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          FTPServer server = con.getServer();
          //passiveServer = Utils.createServer(0, 5, server.getAddress(), server.getSSLContext(), secureData);
          passiveServer = createPassiveServer(server);
          passive = true;

          String host = passiveServer.getInetAddress().getHostAddress();
          int port = passiveServer.getLocalPort();

          if (host.equals("0.0.0.0") || host.equals("::")
          || host.equals("::0") || host.equals("0:0:0:0:0:0:0:0")
          || host.equals("00:00:00:00:00:00:00:00")
          || host.equals("0000:0000:0000:0000:0000:0000:0000:0000"))
          {
              // Sends a valid address instead of a wildcard
              host = InetAddress.getLocalHost().getHostAddress();
          }

          String[] addr = host.split("\\.");

          String address = addr[0] + "," + addr[1] + "," + addr[2] + "," + addr[3];
          String addressPort = port / 256 + "," + port % 256;

          con.sendResponse(227, "Enabled Passive Mode (" + address + "," + addressPort + ")");
        }
      };
    }

    protected Command port()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String data = parms;
          String[] args = data.split(",");
          activeHost = args[0] + "." + args[1] + "." + args[2] + "." + args[3];
          activePort = Integer.parseInt(args[4]) * 256 + Integer.parseInt(args[5]);
          passive = false;
          if(passiveServer != null) {
              Utils.closeQuietly(passiveServer);
              passiveServer = null;
          }
          con.sendResponse(200, "Enabled Active Mode");
        }
      };
    }

    protected Command stat()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          con.sendResponse(211, "Sending the status...");
          //String ip = con.getAddress().getHostAddress();
          String user = username != null ? "as " + username : "anonymously";
          String type = ascii ? "ASCII" : "Binary";
          String data = "";
          //data += "Connected from " + ip + " (" + ip + ")\r\n";
          data += "Logged in " + user + "\r\n";
          data += "TYPE: " + type + ", STRUcture: File, MODE: Stream\r\n";
          data += "Total bytes transferred for session: " + con.getBytesTransferred() + "\r\n";
          con.sendData(data.getBytes("UTF-8"));
          con.sendResponse(211, "Status sent!");
        }
      };
    }

    protected Command auth()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String mechanism = parms;
          mechanism = mechanism.toUpperCase();

          if(mechanism.equals("TLS") || mechanism.equals("TLS-C") ||
              mechanism.equals("SSL") || mechanism.equals("TLS-P")) {
              // No need to distinguish between TLS and SSL, as the protocol self-negotiate its level

              SSLContext ssl = con.getServer().getSSLContext();

              if(ssl == null) {
                  con.sendResponse(431, "TLS/SSL is not available");
              } else if(con.isSSLEnabled()) {
                  con.sendResponse(503, "TLS/SSL is already enabled");
              } else {
                  con.sendResponse(234, "Enabling TLS/SSL...");
                  con.enableSSL(ssl);
              }

          } else {
              con.sendResponse(502, "Unsupported mechanism");
          }
        }
      };
    }

    protected Command pbsz()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          //String size = parms;
          if(con.isSSLEnabled()) {
              // For SSL, the buffer size should always be 0
              // Any other size should be accepted
              con.sendResponse(200, "The protection buffer size was set to 0");
          } else {
              con.sendResponse(503, "You can't set the protection buffer size in an insecure connection");
          }
        }
      };
    }

    protected Command prot()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String level = parms;
          level = level.toUpperCase();
          if(!con.isSSLEnabled()) {
              con.sendResponse(503, "You can't update the protection level in an insecure connection");
          } else if(level.equals("C")) {
              secureData = false;
              con.sendResponse(200, "Protection level set to clear");
          } else if(level.equals("P")) {
              secureData = true;
              con.sendResponse(200, "Protection level set to protected");
          } else if(level.equals("S") || level.equals("E")) {
              con.sendResponse(521, "Unsupported protection level");
          } else {
              con.sendResponse(502, "Unknown protection level");
          }
        }
      };
    }

    protected Command lpsv()
    { // Obsolete Command
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          FTPServer server = con.getServer();
          //passiveServer = Utils.createServer(0, 5, server.getAddress(), server.getSSLContext(), secureData);
          passiveServer = createPassiveServer(server);
          passive = true;

          String host = passiveServer.getInetAddress().getHostAddress();
          int port = passiveServer.getLocalPort();

          if (host.equals("0.0.0.0") || host.equals("::")
          || host.equals("::0") || host.equals("0:0:0:0:0:0:0:0")
          || host.equals("00:00:00:00:00:00:00:00")
          || host.equals("0000:0000:0000:0000:0000:0000:0000:0000"))
          {
              // Sends a valid address instead of a wildcard
              host = InetAddress.getLocalHost().getHostAddress();
          }

          String[] addr = host.split("\\.");

          String address = addr[0] + "," + addr[1] + "," + addr[2] + "," + addr[3];
          String addressPort = port / 256 + "," + port % 256;

          con.sendResponse(229, "Enabled Passive Mode (4,4," + address + ",2," + addressPort + ")");
        }
      };
        
    }

    protected Command lprt()
    { // Obsolete Command
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String data = parms;
          String[] args = data.split(",");

          int hostLength = Integer.parseInt(args[1]);
          int portLength = Integer.parseInt(args[hostLength + 2]);

          String host = "";
          for(int i = 0; i < hostLength; i++) {
              host += "." + args[i + 2];
          }
          activeHost = host.substring(1);

          int port = 0;
          for(int i = 0; i < portLength; i++) {
              int num = Integer.parseInt(args[i + hostLength + 3]);
              int pos = (portLength - i - 1) * 8;
              port |= num << pos;
          }
          activePort = port;

          passive = false;

          if(passiveServer != null) {
              Utils.closeQuietly(passiveServer);
              passiveServer = null;
          }
          con.sendResponse(200, "Enabled Active Mode");
        }
      };
    }

    protected Command epsv()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          FTPServer server = con.getServer();
          //passiveServer = Utils.createServer(0, 5, server.getAddress(), server.getSSLContext(), secureData);
          passiveServer = createPassiveServer(server);
          passive = true;
          con.sendResponse(229, "Enabled Passive Mode (|||" + passiveServer.getLocalPort() + "|)");
        }
      };
    }

    protected Command eprt()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String data = parms;
          char delimiter = data.charAt(0);
          String[] args = data.split(String.format("\\%s", delimiter));
          activeHost = args[2];
          activePort = Integer.parseInt(args[3]);
          passive = false;
          if(passiveServer != null) {
              Utils.closeQuietly(passiveServer);
              passiveServer = null;
          }
          con.sendResponse(200, "Enabled Active Mode");
        }
      };
    }
    
    @SuppressWarnings("all")
    protected boolean authenticate(IUserAuthenticator auth, String password) {
        try {
            con.setFileSystem(auth.authenticate(con, address, username, password));
            authenticated = true;
            return true;
        } catch(AuthException ex) {
            return false;
        } catch(Exception ex) {
            //ex.printStackTrace();
            return false;
        }
    }
    
    protected ServerSocket createPassiveServer(FTPServer server) throws IOException
    {
      return Utils.createServer(0, 5, server.getAddress(), server.getSSLContext(), secureData);
    }
}
