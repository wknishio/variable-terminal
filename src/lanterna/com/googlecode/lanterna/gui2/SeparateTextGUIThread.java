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
package com.googlecode.lanterna.gui2;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of TextGUIThread, this class runs the GUI event processing on a dedicated thread. The GUI
 * needs to be explicitly started in order for the event processing loop to begin, so you must call {@code start()}
 * for this. The GUI thread will stop if {@code stop()} is called, the input stream returns EOF or an exception is
 * thrown from inside the event handling loop.
 * <p>
 * Here is an example of how to use this {@code TextGUIThread}:
 * <pre>
 *     {@code
 *     MultiWindowTextGUI textGUI = new MultiWindowTextGUI(new SeparateTextGUIThread.Factory(), screen);
 *     // ... add components ...
 *     ((AsynchronousTextGUIThread)textGUI.getGUIThread()).start();
 *     // ... this thread will continue while the GUI runs on a separate thread ...
 *     }
 * </pre>
 * @see TextGUIThread
 * @see SameTextGUIThread
 * @author Martin
 */
public class SeparateTextGUIThread extends AbstractTextGUIThread implements AsynchronousTextGUIThread {
    private volatile State state;
    private final Thread textGUIThread;
    private final CountDownLatch waitLatch;

    private SeparateTextGUIThread(TextGUI textGUI) {
        super(textGUI);
        this.waitLatch = new CountDownLatch(1);
        this.textGUIThread = new Thread("LanternaGUI") {
            
            public void run() {
                mainGUILoop();
            }
        };
        textGUIThread.setDaemon(true);
        state = State.CREATED;
    }

    
    public void start() {
        textGUIThread.start();
        state = State.STARTED;
    }

    
    public void stop() {
        if(state != State.STARTED) {
            return;
        }

        state = State.STOPPING;
    }

    
    public void waitForStop() throws InterruptedException {
        waitLatch.await();
    }

    
    public void waitForStop(long time, TimeUnit unit) throws InterruptedException {
        waitLatch.await(time, unit);
    }
    
    
    public State getState() {
        return state;
    }

    
    public Thread getThread() {
        return textGUIThread;
    }

    
    public void invokeLater(Runnable runnable) throws IllegalStateException {
        if(state != State.STARTED) {
            throw new IllegalStateException("Cannot schedule " + runnable + " for execution on the TextGUIThread " +
                    "because the thread is in " + state + " state");
        }
        super.invokeLater(runnable);
    }

    private void mainGUILoop() {
        try {
            //Draw initial screen, after this only draw when the GUI is marked as invalid
            try {
                textGUI.updateScreen();
            }
            catch(IOException e) {
                exceptionHandler.onIOException(e);
            }
            catch(RuntimeException e) {
                exceptionHandler.onRuntimeException(e);
            }
            while(state == State.STARTED) {
                try {
                    if (!processEventsAndUpdate()) {
                        try {
                            Thread.sleep(1);
                        }
                        catch(InterruptedException ignored) {}
                    }
                }
                catch(EOFException e) {
                    stop();
                    if (textGUI instanceof WindowBasedTextGUI) {
                        // Close all windows on EOF
                        for (Window window: ((WindowBasedTextGUI) textGUI).getWindows()) {
                            window.close();
                        }
                    }
                    break; //Break out quickly from the main loop
                }
                catch(IOException e) {
                    if(exceptionHandler.onIOException(e)) {
                        stop();
                        break;
                    }
                }
                catch(RuntimeException e) {
                    if(exceptionHandler.onRuntimeException(e)) {
                        stop();
                        break;
                    }
                }
            }
        }
        finally {
            state = State.STOPPED;
            waitLatch.countDown();
        }
    }


    /**
     * Factory class for creating SeparateTextGUIThread objects
     */
    public static class Factory implements TextGUIThreadFactory {
        
        public TextGUIThread createTextGUIThread(TextGUI textGUI) {
            return new SeparateTextGUIThread(textGUI);
        }
    }
}
