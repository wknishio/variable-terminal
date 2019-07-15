# Variable-Terminal

## Overview

This software is a java remote computer administration tool.

Some of the available features are:

* RC4 and AES encryption.
* SOCKS and HTTP network proxies.
* UPnP, NAT-PMP and PCP NAT port forwarding.
* Multiple simultaneous sessions.
* Adjustable text font size.
* Simple text messaging.
* File transfer with compression, resume and verification.
* ZIP file compression and extraction.
* Remote screen capture.
* Remote desktop control.
* Multiple display support.
* Audio chat communication.
* TCP and SOCKS network tunneling.
* Network usage rate limiter.
* Automatic client reconnection after disconnection.
* Limited native process creation and control.
* Popup alerts.
* Printing of texts and files.
* Audio playback.
* Optical drive control.

The minimum required java version to execute is at least 1.5.<br>
This software comes with no warranty, use at your own risk!

## Installation and initialization

To install this software, just extract the contents of the released zip file in<br>
any folder.

There are 3 main modes of operation for this software:

* Client module to control instances of server module.
* Server module to be controlled by instances of client module.
* Daemon module is the server module in background without user interface.

In user interface 2 options are available:

* Graphical console needs a graphical environment but have connection<br>
configuration dialogs and helper menus for commands.

* Shell console uses the native shell of the operating system and can be used<br>
in environments without graphical interface but with a text terminal.

The default and recommended user interface is the graphical console, as most<br>
computers nowadays have support for graphical environments.

The included scripts in the format variable-terminal-[mode]-[interface].[*] can<br>
be used to start this software in the desired mode and user interface.

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
* Proxy authentication enables HTTP or SOCKS proxy authentication.
* Proxy user controls the user for proxy authentication.
* Proxy password controls the password for proxy authentication.
* Encryption type enables connection encryption using RC4 or AES algorithms.
* Encryption password sets the password for connection encryption.
* Authentication login sets the authentication login for connection.
* Authentication password sets the authentication password for connection.
* Sessions limit define a limit for simultaneous sessions in server.
* Session commands define commands to be sent to server when session starts.

All connection settings can be set using the files<br>
"variable-terminal-client.properties" for client instances and<br>
"variable-terminal-server.properties" for server instances,<br>
those files support UTF-8 encoding.

The connection settings can also be set using program arguments at startup,<br>
these are the available program arguments:

* -H: show available program arguments
* -C: use client module | -S: use server module | -D: use daemon module
* -LF: load settings file
* -CM: connection mode, passive(P), active(A)
* -CH: connection host, default null
* -CP: connection port, default 6060
* -NP: NAT port, default null
* -PT: proxy type, SOCKS(S), HTTP(H), disabled(D), default disabled
* -PH: proxy host, default null
* -PP: proxy port, default 1080 for SOCKS or 8080 for HTTP
* -PA: proxy authentication, enabled(E), disabled(D), default disabled
* -PU: proxy user, default null
* -PK: proxy password, default null
* -ET: encryption type, AES(A), RC4(R), disabled(D), default disabled
* -EK: encryption password, default null
* -AL: authentication login, default null
* -AK: authentication password, default null
* -SL: sessions limit, default 0, available in server
* -SC: session commands, separated by "*;", default null, available in client

## Console commands

This software is primarily a command-line tool driven by text commands.

After successfully connecting and authenticating to a server, the client console<br>
can receive text command inputs from the user.

When the user types a command in client and press enter, this software checks if<br>
the command must be redirected to the external shell running on the server or if<br>
the command is a internal command of this software.

Most of the functions of this software are executed by these internal commands.

After configuring the server, the server console can also receive text command<br>
inputs, but only internal commands of this software.

When using graphical console, the helper menu "Commands" will be available,<br>
assisting the use of all internal commands of this software.

Some of the available internal commands in client console:

* Commands \*VTFILETRANSFER or \*VTFT do file transfers.
* Commands \*VTSCREENSHOT or \*VTSCS do remote screen captures.
* Commands \*VTGRAPHICSLINK or \*VTGL toggle remote desktop control.
* Commands \*VTAUDIOLINK or \*VTAL toggle audio chat communication.
* Commands \*VTBELL or \*VTBL play remote system audio alert.

There are more internal commands in client console and server console:

* Commands \*VTHELP or \*VTHLP show a list of available internal commands.
* Commands \*VTHELP [COMMAND] or \*VTHLP [CMD] show more details about<br>
specific internal commands.

## Building from sources

The minimum required java version to build from sources is at least JDK 1.7.

The included scripts "build.bat" and "build.sh" perform the build process using<br>
ant and the file "build.xml" is an ant build file for this software.

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
* Commons-compress by Apache Software Foundation, for ZIP64 format support
* client-side Throttle by James Edwards, for network data rate limiter
* zstd-jni by Luben Karavelov, for zstd compression
* DirectRobot by Killer99@rune-server.ee, for better screen capture
* PortMapper by Kasra Faghihi(offbynull) for PCP port forwarding support

## Additional utilities

Some additional utilities are included with server distributions:

* beanshell2 by pejobo
* autologon for windows
* lockstation for windows
* logon for windows
* srvany for windows
* uac for windows

## License

<pre>This software is under MIT license

Copyright (c) 2019 William Kendi Nishio

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
