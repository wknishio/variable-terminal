/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.terminal.ansi;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.sun.jna.Platform;

/**
 * UnixLikeTerminal extends from ANSITerminal and defines functionality that is common to
 *  {@code UnixTerminal} and {@code CygwinTerminal}, like setting tty modes; echo, cbreak
 *  and minimum characters for reading as well as a shutdown hook to set the tty back to
 *  original state at the end.
 * <p>
 *  If requested, it handles Control-C input to terminate the program, and hooks
 *  into Unix WINCH signal to detect when the user has resized the terminal,
 *  if supported by the JVM.
 *
 * @author Andreas
 * @author Martin
 */
public abstract class UnixLikeTTYTerminal extends UnixLikeTerminal {

    private final File ttyDev;
    private String sttyStatusToRestore;

    /**
     * Creates a UnixTerminal using a specified input stream, output stream and character set, with a custom size
     * querier instead of using the default one. This way you can override size detection (if you want to force the
     * terminal to a fixed size, for example). You also choose how you want ctrl+c key strokes to be handled.
     *
     * @param ttyDev TTY device file that is representing this terminal session, will be used when calling stty to make
     *               it operate on this session
     * @param terminalInput Input stream to read terminal input from
     * @param terminalOutput Output stream to write terminal output to
     * @param terminalCharset Character set to use when converting characters to bytes
     * @param terminalCtrlCBehaviour Special settings on how the terminal will behave, see {@code UnixTerminalMode} for
     *                               more details
     * @throws IOException If there was an I/O error while setting up the terminal
     */
    protected UnixLikeTTYTerminal(
            File ttyDev,
            InputStream terminalInput,
            OutputStream terminalOutput,
            Charset terminalCharset,
            CtrlCBehaviour terminalCtrlCBehaviour) throws IOException {

        super(terminalInput,
                terminalOutput,
                terminalCharset,
                terminalCtrlCBehaviour);

        this.ttyDev = ttyDev;

        // Take ownership of the terminal
        realAcquire();
    }

    
    protected void acquire() throws IOException {
        // Hack!
    }

    private void realAcquire() throws IOException {
        super.acquire();
    }

    
    protected void registerTerminalResizeListener(final Runnable onResize) throws IOException {
        try {
            Class<?> signalClass = Class.forName("sun.misc.Signal");
            for(Method m : signalClass.getDeclaredMethods()) {
                if("handle".equals(m.getName())) {
                    Object windowResizeHandler = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Class.forName("sun.misc.SignalHandler")}, new InvocationHandler() {
                        
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if("handle".equals(method.getName())) {
                                onResize.run();
                            }
                            return null;
                        }
                    });
                    m.invoke(null, signalClass.getConstructor(String.class).newInstance("WINCH"), windowResizeHandler);
                }
            }
        }
        catch(Throwable ignore) {
            // We're probably running on a non-Sun JVM and there's no way to catch signals without resorting to native
            // code integration
        }
    }

    
    protected void saveTerminalSettings() throws IOException {
        sttyStatusToRestore = runSTTYCommand("-g").trim();
    }

    
    protected void restoreTerminalSettings() throws IOException {
        if(sttyStatusToRestore != null) {
        	runSTTYCommand(sttyStatusToRestore);
        }
    }

    
    protected void keyEchoEnabled(boolean enabled) throws IOException {
    	runSTTYCommand(enabled ? "echo" : "-echo");
    }

    
    protected void canonicalMode(boolean enabled) throws IOException {
    	runSTTYCommand(enabled ? "icanon" : "-icanon");
        if(!enabled) {
        	runSTTYCommand("min", "1");
        }
    }

    
    protected void keyStrokeSignalsEnabled(boolean enabled) throws IOException {
        if(enabled) {
        	runSTTYCommand("intr", "^C");
        }
        else {
        	runSTTYCommand("intr", "undef");
        }
    }

    protected String runSTTYCommand(String... parameters) throws IOException {
        List<String> commandLine = new ArrayList<String>(Collections.singletonList(
                getSTTYCommand()));
        commandLine.addAll(Arrays.asList(parameters));
        return exec(commandLine.toArray(new String[commandLine.size()]));
    }

    protected String exec(String... cmd) throws IOException {
    	
        if (ttyDev != null)
        {
            //Here's what we try to do, but that is Java 7+ only:
            // processBuilder.redirectInput(ProcessBuilder.Redirect.from(ttyDev));
            //instead, for Java 6, we join the cmd into a scriptlet with redirection
            //and replace cmd by a call to sh with the scriptlet:
           StringBuilder sb = new StringBuilder();
           for (String arg : cmd) { sb.append(arg).append(' '); }
           sb.append("< ").append(ttyDev);
           if (Platform.isWindows())
           {
        	   cmd = new String[] { "cmd.exe", "/c", sb.toString() };
           }
           else
           {
        	   cmd = new String[] { "sh", "-c", sb.toString() };
           }
           
        }
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process process = pb.start();
        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        InputStream stdout = process.getInputStream();
        int readByte = stdout.read();
        while(readByte >= 0) {
            stdoutBuffer.write(readByte);
            readByte = stdout.read();
        }
        ByteArrayInputStream stdoutBufferInputStream = new ByteArrayInputStream(stdoutBuffer.toByteArray());
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdoutBufferInputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    private String getSTTYCommand() {
        return "/bin/stty";
    }
}