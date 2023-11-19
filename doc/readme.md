# Variable-Terminal-Lite

## Overview

This software is a java remote computer administration tool.

It has various functions inspired by TELNET, FTP, SSH, NETBUS, VNC,
but its incompatible with those tools/protocols/standards.

This is the lite version intended to run in restricted environments as Android

Some of the available features are:

* RC4, ISAAC, SALSA, HC256, GRAIN or LEA encryption.
* UPnP, NAT-PMP and PCP NAT port forwarding.
* SOCKS and HTTP proxy network connections.
* Multiple simultaneous sessions.
* Limited native process creation and control.
* Integrated beanshell and groovysh remote shells.
* Both client and server can be run in background
* Automatic client reconnection after disconnection.
* Adjustable text font size.
* Simple text messaging between client and server.
* File transfer with compression, resume and verification.
* Remote screen capture.
* Remote desktop view and control.
* Remote clipboard control.
* Multiple display support.
* Audio chat communication between client and server.
* Network tunneling with TCP redirection and SOCKS/HTTP proxy.
* Network usage rate limiter.
* Network latency verification.
* Remote popup alerts.
* Remote browser opening.
* Remote mail client opening.
* Remote printing of texts and files.
* Remote audio playback.
* Remote disc tray control.
* Record client commands to text file.
* Execute client commands from text file.
* Record client console output to text file.

The minimum required java version to execute is at least 1.5.<br>
This software comes with no warranty, use at your own risk!

## Installation and initialization

To install this software, just extract the contents of the released zip file in<br>
any folder.

There are 4 main modes of operation for this software:

* Client module to control instances of server module.
* Server module to be controlled by instances of client module.
* Agent module is the client module in background without user interface.
* Daemon module is the server module in background without user interface.

In user interface 2 options are available:

* Graphical console needs a graphical environment but have connection<br>
configuration dialogs and helper menus for commands.

* Shell console uses the native shell of the operating system and can be used<br>
in environments without graphical interface but with a text terminal.

The default and recommended user interface is the graphical console, as most<br>
computers nowadays have support for graphical environments.

The included scripts using format vate-[mode]-[interface].[*] can<br>
be used to start this software used in a specific combination of mode and user<br>
interface.

## Connection settings

To start running effectively, connection settings must be set for client<br>
and server, when using graphical console, a dialog will appear to ease the<br>
configuration of connection settings.

The connection settings are these:

* Connection mode controls which instance will attempt to connect and which<br>
instance will listen for connections, as in a TCP connection.
    * Active connection mode means it will attempt to connect, it is the<br>
    default connection mode of the client.
    * Passive connection mode means it will listen for connections, it is the<br>
    default connection mode of the server.
    * To connect successfully, one instance must be in active connection mode while<br>
    the other must be in passive connection mode.
* Connection host controls which TCP host address will be used for connection.
* Connection port controls which TCP port will be used for connection, default 6060.
* Connection NAT port sets a NAT port mapping using UPnP or NAT-PMP protocols.
* Proxy type enables support for connections using HTTP or SOCKS proxies.
* Proxy host controls the TCP host address of the proxy.
* Proxy port controls the TCP port of the proxy.
* Proxy user controls the user for proxy authentication.
* Proxy password controls the password for proxy authentication.
* Encryption type enables connection encryption using encryption algorithms.
* Encryption password sets the password for connection encryption.
* Session user sets the session user for session.
* Session password sets the session password for session.
* Session shell sets the command to be used as remote shell in server.
* Session maximum define a limit for simultaneous sessions in server.
* Session commands set commands separated by *; to be run when session starts.

All connection settings can be set using the files<br>
"vate-client.properties" for client instances and<br>
"vate-server.properties" for server instances,<br>
those files support UTF-8 encoding.

The connection settings can also be set using program arguments at startup,<br>
these are the available program arguments:

* -H: show available program arguments
* -C: client module | -S: server module |
* -A: agent module | -D: daemon module 
* -LF: load settings file
* -CM: connection mode, passive(P), active(A)
* -CH: connection host, default null
* -CP: connection port, default 6060
* -CN: connection NAT port, default null
* -PT: proxy type, default none, DIRECT(D), AUTO(A), SOCKS(S), HTTP(H)
* -PH: proxy host, default null
* -PP: proxy port, default 1080 for SOCKS or default 8080 for HTTP
* -PU: proxy user, default null
* -PK: proxy password, default null
* -ET: encryption type, none/RC4(R)/ISAAC(I)/SALSA(S)/HC256(H)/GRAIN(G)/LEA(L)
* -EK encryption password, default null
* -SS: session shell, default null
* -SU: session user, default null
* -SK: session password, default null
* -SM: session maximum, default 0, only in server
* -SC: session commands, separated by "*;", default null, only in client

## Console commands

This software is primarily a command-line tool driven by text commands.

After successfully connecting and authenticating to a server, the client<br>
console can receive text command inputs from the user.

When the user types a command in client and press enter, this software checks<br>
if the command must be redirected to the remote shell running on the server or<br>
if the command is a internal command of this software.

Most of the functions of this software are executed by internal commands.

After configuring the server, the server console can also receive text command<br>
inputs, but only internal commands of this software.

When using graphical console, the helper menu "Command" will be available,<br>
assisting the use of all internal commands of this software.

Some of the available internal commands in client console:

* Commands \*VTFILETRANSFER or \*VTFT do file transfers.
* Commands \*VTSCREENSHOT or \*VTSCS do remote screen captures.
* Commands \*VTGRAPHICSLINK or \*VTGL toggle remote desktop control.
* Commands \*VTAUDIOLINK or \*VTAL toggle audio chat communication.
* Commands \*VTBELL or \*VTBL play remote system audio alert.
* Commands \*VTTUNNEL or \*VTTN set network connection tunnels.
* Commands \*VTLIMIT or \*VTLM limit network connection data rates.

There are more internal commands in client console and server console:

* Commands \*VTHELP or \*VTHLP show a list of available internal commands.
* Commands \*VTHELP [COMMAND] or \*VTHLP [CMD] show more details about<br>
specific internal commands.

## Building from sources

The minimum required java version to build from sources is at least JDK 1.5.

The included scripts "build.bat" and "build.sh" perform the build process<br>
using ant and the file "build.xml" is an ant build file for this software.

After running the ant build file, the packaged application distributions can be<br>
found in the "dist" folder.

## Used libraries

Those are the third party libraries used in this software:

* MindTerm by AppGate, for circular buffer pipe stream
* JZlib by Atsuhiko Yamanaka, for pure java zlib compression
* jpountz lz4-java by Adrien Grand, for lz4 compression and xxhash checksum
* Java Native Access by Todd Fast/Timothy Wall/Liang Chen, for native calls
* jsocks by Kirill Kouzoubov/Robert Simac, for SOCKS tunneling support
* JSAP by Martian Software, for command parsing
* PngEncoder by ObjectPlanet, for better PNG image encoding
* ARGBPixelGrabber by pumpernickel, for image data extraction
* UPNPLib by sbbi, for UPnP NAT port forwarding support
* TomP2P by Thomas Bocek, for NAT-PMP NAT port forwarding support
* JSpeex by Horizon Wimba, for Speex audio communication
* Concentus Java by Logan Stromberg, for Opus audio communication
* client-side Throttle by James Edwards, for network data rate limiter
* DirectRobot by Killer99@rune-server.ee, for better screen capture
* PortMapper by Kasra Faghihi(offbynull) for PCP port forwarding support
* Lanterna by mabe02 for graphical console
* Sixlegs Java PNG Decoder for PNG decoding
* bouncycastle by Legion of the Bouncy Castle for encryption
* beanshell2 by pejobo for alternative shell
* groovy by codehaus for alternative shell
* airlift-aircompressor by Martin Traverso for zstd and lzo compression
* nanohttpd-1.1 by elonen, for server HTTP tunneling
* commons-httpclient by Apache Software Foundation, for client HTTP tunneling

## Additional utilities

Some additional utilities are included with distributions:

* beanshell2 by pejobo
* groovy by codehaus
* autologon for windows
* lockstation for windows
* logon for windows
* srvany for windows
* uac for windows

## License

<pre>This software is under MIT license

Copyright (c) AD 2020 William Kendi Nishio

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.</pre>