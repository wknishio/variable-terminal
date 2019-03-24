# Variable-Terminal

## Overview

<pre>This software is a java remote computer administration tool.</pre>

<pre>Some of the available features are:</pre>

* <pre>RC4 and AES encryption.</pre>
* <pre>SOCKS and HTTP network proxies.</pre>
* <pre>UPnP, NAT-PMP and PCP NAT port forwarding.</pre>
* <pre>Multiple simultaneous sessions.</pre>
* <pre>Adjustable text font size.</pre>
* <pre>Simple text messaging.</pre>
* <pre>File transfer with compression, resume and verification.</pre>
* <pre>ZIP file compression and extraction.</pre>
* <pre>Remote screen capture.</pre>
* <pre>Remote desktop control.</pre>
* <pre>Multiple display support.</pre>
* <pre>Audio chat communication.</pre>
* <pre>TCP and SOCKS network tunneling.</pre>
* <pre>Network usage rate limiter.</pre>
* <pre>Automatic client reconnection after disconnection.</pre>
* <pre>Limited native process creation and control.</pre>
* <pre>Popup alerts.</pre>
* <pre>Printing of texts and files.</pre>
* <pre>Audio playback.</pre>
* <pre>Optical drive control.</pre>

<pre>The minimum required java version to execute is at least 1.5.<br>
This software comes with no warranty, use at your own risk!</pre>

## Installation and initialization

<pre>To install this software, just extract the contents of the released zip file in<br>
any folder.</pre>

<pre>There are 3 main modes of operation for this software:</pre>

* <pre>Client module to control instances of server module.</pre>
* <pre>Server module to be controlled by instances of client module.</pre>
* <pre>Daemon module is the server module in background without user interface.</pre>

<pre>In user interface 2 options are available:</pre>

* <pre>Graphical console needs a graphical environment but have connection<br>
configuration dialogs and helper menus for commands.</pre>

* <pre>Shell console uses the native shell of the operating system and can be used<br>
in environments without graphical interface but with a text terminal.</pre>

<pre>The default and recommended user interface is the graphical console, as most<br>
computers nowadays have support for graphical environments.</pre>

<pre>The included scripts in the format variable-terminal-[mode]-[interface].[*] can<br>
be used to start this software in the desired mode and user interface.</pre>

## Connection settings

<pre>To start running effectively, connection settings must be set for client<br>
and server, when using graphical console, a dialog will appear to ease the<br>
configuration of connection settings.</pre>

<pre>The connection settings are these:</pre>

* <pre>Connection mode controls which instance will attempt to connect and which<br>
instance will listen for connections, as in a TCP connection.</pre>
    * <pre>Active connection mode means it will attempt to connect, it is the<br>
    default connection mode of the client.</pre>
    * <pre>Passive connection mode means it will listen for connections, it is the<br>
    default connection mode of the server.</pre>
    * <pre>To connect successfully, one instance must be in active connection mode while<br>
    the other must be in passive connection mode.</pre>
* <pre>Connection host controls which TCP host address will be used for connection.</pre>
* <pre>Connection port controls which TCP port will be used for connection, default 6060.</pre>
* <pre>Connection NAT port sets a NAT port mapping using UPnP or NAT-PMP protocols.</pre>
* <pre>Encryption type enables connection encryption using RC4 or AES algorithms.</pre>
* <pre>Encryption password sets the password for connection encryption.</pre>
* <pre>Proxy type enables support for connections using HTTP or SOCKS proxies.</pre>
* <pre>Proxy host controls the TCP host address of the proxy.</pre>
* <pre>Proxy port controls the TCP port of the proxy.</pre>
* <pre>Proxy authentication enables HTTP or SOCKS proxy authentication.</pre>
* <pre>Proxy user controls the user for proxy authentication.</pre>
* <pre>Proxy password controls the password for proxy authentication.</pre>
* <pre>Authentication login sets the authentication login for connection.</pre>
* <pre>Authentication password sets the authentication password for connection.</pre>
* <pre>Sessions limit define a limit for simultaneous sessions in server.</pre>
* <pre>Session commands define commands to be sent to server when session starts.</pre>

<pre>All connection settings can be set using the files<br>
"variable-terminal-client.properties" for client instances and<br>
"variable-terminal-server.properties" for server instances,<br>
those files support UTF-8 encoding.</pre>

<pre>The connection settings can also be set using program arguments at startup,<br>
these are the available program arguments:</pre>

* <pre>-H: show available program arguments</pre>
* <pre>-C: use client module</pre>
* <pre>-S: use server module</pre>
* <pre>-D: use daemon module</pre>
* <pre>-LF: load settings file</pre>
* <pre>-CM: connection mode, passive(P), active(A)</pre>
* <pre>-CH: connection host, default null</pre>
* <pre>-CP: connection port, default 6060</pre>
* <pre>-NP: NAT port, default null</pre>
* <pre>-ET: encryption type, AES(A), RC4(R), disabled(D), default disabled</pre>
* <pre>-EK: encryption password, default null</pre>
* <pre>-PT: proxy type, SOCKS(S), HTTP(H), disabled(D), default disabled</pre>
* <pre>-PH: proxy host, default null</pre>
* <pre>-PP: proxy port, default 1080 for SOCKS or 8080 for HTTP</pre>
* <pre>-PA: proxy authentication, enabled(E), disabled(D), default disabled</pre>
* <pre>-PU: proxy user, default null</pre>
* <pre>-PK: proxy password, default null</pre>
* <pre>-AL: authentication login, default null</pre>
* <pre>-AK: authentication password, default null</pre>
* <pre>-SL: sessions limit, default 0, available in server</pre>
* <pre>-SC: session commands, separated by *;, default null, available in client</pre>

## Console commands

<pre>This software is primarily a command-line tool driven by text commands.</pre>

<pre>After successfully connecting and authenticating to a server, the client console<br>
can receive text command inputs from the user.</pre>

<pre>When the user types a command in client and press enter, this software checks if<br>
the command must be redirected to the external shell running on the server or if<br>
the command is a exclusive command of this software.</pre>

<pre>Most of the functions of this software are executed by these exclusive commands.</pre>

<pre>After configuring the server, the server console can also receive text command<br>
inputs, but only exclusive commands of this software.</pre>

<pre>When using graphical console, the helper menu "Commands" will be available,<br>
assisting the use of all exclusive commands of this software.</pre>

<pre>Some of the available exclusive commands in client console:</pre>

* <pre>Commands \*VTFILETRANSFER or \*VTFT do file transfers.</pre>
* <pre>Commands \*VTSCREENSHOT or \*VTSCS do remote screen captures.</pre>
* <pre>Commands \*VTGRAPHICSLINK or \*VTGL toggle remote desktop control.</pre>
* <pre>Commands \*VTAUDIOLINK or \*VTAL toggle audio chat communication.</pre>
* <pre>Commands \*VTBELL or \*VTBL play remote system audio alert.</pre>

<pre>There are more exclusive commands in client console and server console:</pre>

* <pre>Commands \*VTHELP or \*VTHLP show a list of available exclusive commands./pre>
* <pre>Commands \*VTHELP [COMMAND] or \*VTHLP [CMD] show more details about<br>
specific exclusive commands.</pre>

## Building from sources

<pre>The minimum required java version to build from sources is at least JDK 1.7.</pre>

<pre>The included scripts "build.bat" and "build.sh" perform the build process using<br>
ant and the file "build.xml" is an ant build file for this software.</pre>

<pre>After running the ant build file, the packaged application distributions can be<br>
found in the "dist" folder.</pre>

## Used libraries

<pre>Those are the third party libraries used in this software:</pre>

* <pre>MindTerm by AppGate, for circular buffer pipe stream</pre>
* <pre>JZlib by Atsuhiko Yamanaka, for pure java zlib compression</pre>
* <pre>jpountz lz4-java by Adrien Grand, for lz4 compression and xxhash checksum</pre>
* <pre>Java Native Access by Todd Fast/Timothy Wall/Liang Chen, for native calls</pre>
* <pre>jsocks by Kirill Kouzoubov/Robert Simac, for SOCKS tunneling support</pre>
* <pre>JSAP by Martian Software, for command parsing</pre>
* <pre>PngEncoder by ObjectPlanet, for better PNG image encoding</pre>
* <pre>ARGBPixelGrabber by pumpernickel, for image data extraction</pre>
* <pre>UPNPLib by sbbi, for UPnP NAT port forwarding support</pre>
* <pre>TomP2P by Thomas Bocek, for NAT-PMP NAT port forwarding support</pre>
* <pre>JSpeex by Horizon Wimba, for Speex audio communication</pre>
* <pre>Concentus Java by Logan Stromberg, for Opus audio communication</pre>
* <pre>Commons-compress by Apache Software Foundation, for ZIP64 format support</pre>
* <pre>client-side Throttle by James Edwards, for network data rate limiter</pre>
* <pre>zstd-jni by Luben Karavelov, for zstd compression</pre>
* <pre>DirectRobot by Killer99@rune-server.ee, for better screen capture</pre>
* <pre>PortMapper by Kasra Faghihi(offbynull) for PCP port forwarding support</pre>

## Additional utilities

<pre>Some additional utilities are included with server distributions:</pre>

* <pre>beanshell2 by pejobo</pre>
* <pre>autologon for windows</pre>
* <pre>lockstation for windows</pre>
* <pre>logon for windows</pre>
* <pre>srvany for windows</pre>
* <pre>uac for windows</pre>

## License

<pre>This software is under MIT license</pre>

<pre>Copyright (c) 2019 William Kendi Nishio</pre>

<pre>Permission is hereby granted, free of charge, to any person obtaining a copy<br>
of this software and associated documentation files (the "Software"), to deal<br>
in the Software without restriction, including without limitation the rights<br>
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell<br>
copies of the Software, and to permit persons to whom the Software is<br>
furnished to do so, subject to the following conditions:</pre>

<pre>The above copyright notice and this permission notice shall be included in all<br>
copies or substantial portions of the Software.</pre>

<pre>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR<br>
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,<br>
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE<br>
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER<br>
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,<br>
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE<br>
SOFTWARE.</pre>
