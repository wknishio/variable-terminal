# Variable-Terminal

## Overview

This software is a java remote computer administration tool.

Some of the available features are:

* RC4 and AES encryption.
* SOCKS and HTTP network proxies.
* UPnP and NAT-PMP NAT port forwarding.
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
* Limited native process creation and control.
* Popup alerts.
* Printing of texts and files.
* Audio playback.
* Optical drive control.

The minimum required java version to execute is at least 1.5.
This software comes with no warranty, use at your own risk!

## Installation and initialization

To install this software, just extract the contents of the released zip file in
any folder.

There are 3 main modes of operation for this software:

* Client mode to control instances in server mode.
* Server mode to be controlled by instances in client mode.
* Daemon mode is the server mode in background without user interface.

In user interface 2 options are available:

* Graphical console needs a graphical environment but have connection
configuration dialogs and helper menus for commands.  
* Shell console uses the native shell of the operating system and can be used
in environments without graphical interface but with a text terminal.

The default and recommended user interface is the graphical console, as most
consumer computers nowadays have support for graphical environments.

The included scripts in the format variable-terminal-[mode]-[interface].[*] can
be used to start this software used in a specific combination of mode and user
interface.

## Connection configuration

To start running effectively, connection configurations must be set for client
and server.

When using graphical console, a dialog will appear to ease the connection
configuration.

The connection settings are these:

* Connection mode controls which instance will attempt to connect and which
instance will listen for connections, as in a TCP connection.  
Active connection mode indicates that it will attempt to connect, it is the
default connection mode of the client.  
Passive connection mode indicates that it will listen for connections, it is
the default connection mode of the server.  
To connect successfully, one instance must be in active connection mode while
the other must be in passive connection mode.
* Connection host controls which TCP host address will be used for connection.
* Connection port controls which TCP port will be used for connection, default
TCP port is 6060.
* Connection NAT port sets a NAT port forwarding using UPnP or NAT-PMP protocols.
* Encryption type enables connection encryption using RC4 or AES algorithms.
* Encryption password sets the password for connection encryption.
* Proxy type enables support for connections using HTTP or SOCKS proxies.
* Proxy host controls the TCP host address of the proxy.
* Proxy port controls the TCP port of the proxy.
* Proxy authentication enables HTTP or SOCKS proxy authentication.
* Proxy user controls the user for proxy authentication.
* Proxy password controls the password for proxy authentication.
* Authentication login sets the authentication login for connection.
* Authentication password sets the authentication password for connection.
* Sessions limit define a limit for simultaneous sessions in server.

All connection settings can be set using the files  
"variable-terminal-client.properties" for client instances and  
"variable-terminal-server.properties" for server instances,  
those files support UTF-8 encoding.

The connection settings can also be set using program arguments at startup:

Mode parameter (if mode is not decided by the startup class):

* c(client) | s(server) | d(daemon)

Host parameter or settings file parameter (the next argument):

* [connectionhost/]connectionport[;natport]
* [settingsfile]

Optional parameters (autodetect by format):

* [login/password]
* [encryptiontype;encryptionpassword]
* [proxytype[/proxyuser/proxypassword]/proxyhost/proxyport]
* [sessionslimit]  

## Console commands

This software is primarily a command-line tool driven by text commands.

After successfully connecting and authenticating to a server, the client console
can receive text command inputs from the user.

When the user types a command in client and press enter, this software checks if
the command must be redirected to the external shell running on the server or if
the command is a exclusive command of this software.

Most of the functions of this software are executed by these exclusive commands.

After configuring the server, the server console can also receive text command
inputs, but only exclusive commands of this software.

When using graphical console, the helper menu "Commands" will be available,
assisting the use of all exclusive commands of this software.

Some of the available exclusive commands in client console:

* Commands \*VTFILETRANSFER or \*VTFT do file transfers.
* Commands \*VTSCREENSHOT or \*VTSCS do remote screen captures.
* Commands \*VTGRAPHICSLINK or \*VTGL toggle remote desktop control.
* Commands \*VTAUDIOLINK or \*VTAL toggle audio chat communication.
* Commands \*VTBELL or \*VTBL play remote system audio alert.

There are more exclusive commands in client console and server console:

* Commands \*VTHELP or \*VTHLP show a list of available exclusive commands.
* Commands \*VTHELP [COMMAND] or \*VTHLP [CMD] show more details about
specific exclusive commands.

## Building from sources

The minimum required java version to build from sources is at least JDK 1.7.

The included scripts "build.bat" and "build.sh" perform the build process using
ant and the file "build.xml" is an ant build file for this software.

After running the ant build file, the packaged application distributions can be
found in the "dist" folder.

## Used libraries

Those are the third party libraries used in this software:

* MindTerm by AppGate, for circular buffer pipe stream
* JZlib by Atsuhiko Yamanaka, for pure java zlib compression
* jpountz lz4-java by Adrien Grand, for lz4 compression and xxhash checksum
* Java Native Access by Todd Fast/Timothy Wall/Liang Chen, for native calls
* jsocks by Kirill Kouzoubov/Robert Simac, for SOCKS tunneling support
* JSAP by Martian Software, for command parsing
* PngEncoder by ObjectPlanet, for PNG image encoding
* ARGBPixelGrabber by pumpernickel, for image data extraction
* UPNPLib by sbbi, for UPnP NAT port forwarding support
* TomP2P by Thomas Bocek, for NAT-PMP NAT port forwarding support
* JSpeex by Horizon Wimba, for Speex audio communication
* Concentus Java by Logan Stromberg, for Opus audio communication
* Commons-compress by Apache Software Foundation, for ZIP64 format support
* client-side Throttle by James Edwards, for network data rate limiter
* zstd-jni by Luben Karavelov, for zstd compression

## Additional utilities

Some additional utilities are included with server distributions:

* beanshell2 by pejobo
* autologon for windows
* lockstation for windows
* logon for windows
* srvany for windows
* uac for windows

## License

This software is under GPL license, see license.txt.