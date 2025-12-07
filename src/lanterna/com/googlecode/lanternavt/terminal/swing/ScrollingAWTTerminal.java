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
package com.googlecode.lanternavt.terminal.swing;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.googlecode.lanternavt.SGR;
import com.googlecode.lanternavt.TerminalPosition;
import com.googlecode.lanternavt.TerminalSize;
import com.googlecode.lanternavt.TextColor;
import com.googlecode.lanternavt.graphics.TextGraphics;
import com.googlecode.lanternavt.input.KeyStroke;
import com.googlecode.lanternavt.terminal.IOSafeTerminal;
import com.googlecode.lanternavt.terminal.TerminalResizeListener;

/**
 * This is a AWT Container that carries an {@link AWTTerminal} with a scrollbar, effectively implementing a
 * pseudo-terminal with scrollback history. You can choose the same parameters are for {@link AWTTerminal}, they are
 * forwarded, this class mostly deals with linking the {@link AWTTerminal} with the scrollbar and having them update
 * each other.
 * @author Martin
 */
@SuppressWarnings("serial")
public class ScrollingAWTTerminal extends Container implements IOSafeTerminal {

    private final AWTTerminal awtTerminal;
    private final Scrollbar scrollBar;

    // Used to prevent unnecessary repaints (the component is re-adjusting the scrollbar as part of the repaint
    // operation, we don't need the scrollbar listener to trigger another repaint of the terminal when that happens
    private volatile boolean scrollModelUpdateBySystem;

    /**
     * Creates a new {@code ScrollingAWTTerminal} with all default options
     */
    public ScrollingAWTTerminal() {
        this(TerminalEmulatorDeviceConfiguration.getDefault(),
                AWTTerminalFontConfiguration.getDefault(),
                TerminalEmulatorColorConfiguration.getDefault());
    }
    
    public AWTTerminal getTerminal()
    {
    	return this.awtTerminal;
    }

    /**
     * Creates a new {@code ScrollingAWTTerminal} with customizable settings.
     * @param deviceConfiguration How to configure the terminal virtual device
     * @param fontConfiguration What kind of fonts to use
     * @param colorConfiguration Which color schema to use for ANSI colors
     */
    @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
    public ScrollingAWTTerminal(
            TerminalEmulatorDeviceConfiguration deviceConfiguration,
            AWTTerminalFontConfiguration fontConfiguration,
            TerminalEmulatorColorConfiguration colorConfiguration) {

        this.scrollBar = new Scrollbar(Scrollbar.VERTICAL);
        this.awtTerminal = new AWTTerminal(
                deviceConfiguration,
                fontConfiguration,
                colorConfiguration,
                new ScrollController(),
                null);

        setLayout(new BorderLayout());
        add(awtTerminal, BorderLayout.CENTER);
        add(scrollBar, BorderLayout.EAST);
        this.scrollBar.setMinimum(0);
        this.scrollBar.setMaximum(20);
        this.scrollBar.setValue(0);
        this.scrollBar.setVisibleAmount(20);
        this.scrollBar.addAdjustmentListener(new ScrollbarListener());
        this.scrollModelUpdateBySystem = false;
    }

    private class ScrollController implements TerminalScrollController {
        private int scrollValue;

        
        public void updateModel(final int totalSize, final int screenHeight) {
            if(!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    
                    public void run() {
                        updateModel(totalSize, screenHeight);
                    }
                });
                return;
            }
            try {
                scrollModelUpdateBySystem = true;
                int value = scrollBar.getValue();
                int maximum = scrollBar.getMaximum();
                int visibleAmount = scrollBar.getVisibleAmount();

                if(maximum != totalSize) {
                    int lastMaximum = maximum;
                    maximum = totalSize > screenHeight ? totalSize : screenHeight;
                    if(lastMaximum < maximum &&
                            lastMaximum - visibleAmount - value == 0) {
                        value = scrollBar.getValue() + (maximum - lastMaximum);
                    }
                }
                if(value + screenHeight > maximum) {
                    value = maximum - screenHeight;
                }
                if(visibleAmount != screenHeight) {
                    if(visibleAmount > screenHeight) {
                        value += visibleAmount - screenHeight;
                    }
                    visibleAmount = screenHeight;
                }
                if(value > maximum - visibleAmount) {
                    value = maximum - visibleAmount;
                }
                if(value < 0) {
                    value = 0;
                }

                this.scrollValue = value;

                if(scrollBar.getMaximum() != maximum) {
                    scrollBar.setMaximum(maximum);
                }
                if(scrollBar.getVisibleAmount() != visibleAmount) {
                    scrollBar.setVisibleAmount(visibleAmount);
                }
                if(scrollBar.getValue() != value) {
                    scrollBar.setValue(value);
                }
            }
            finally {
                scrollModelUpdateBySystem = false;
            }
        }

        
        public int getScrollingOffset() {
            return scrollValue;
        }
    }

    private class ScrollbarListener implements AdjustmentListener {
        
        public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
            if(!scrollModelUpdateBySystem) {
                // Only repaint if this was the user adjusting the scrollbar
                awtTerminal.repaint();
            }
        }
    }

    /**
     * Takes a KeyStroke and puts it on the input queue of the terminal emulator. This way you can insert synthetic
     * input events to be processed as if they came from the user typing on the keyboard.
     * @param keyStroke Key stroke input event to put on the queue
     */
    public void addInput(KeyStroke keyStroke) {
        awtTerminal.addInput(keyStroke);
    }

    ///////////
    // Delegate all Terminal interface implementations to SwingTerminal
    ///////////
    
    public KeyStroke pollInput() {
        return awtTerminal.pollInput();
    }

    
    public KeyStroke readInput() {
        return awtTerminal.readInput();
    }

    
    public void enterPrivateMode() {
        awtTerminal.enterPrivateMode();
    }

    
    public void exitPrivateMode() {
        awtTerminal.exitPrivateMode();
    }

    
    public void clearScreen() {
        awtTerminal.clearScreen();
    }

    
    public void setCursorPosition(int x, int y) {
        awtTerminal.setCursorPosition(x, y);
    }

    
    public void setCursorPosition(TerminalPosition position) {
        awtTerminal.setCursorPosition(position);
    }

    
    public TerminalPosition getCursorPosition() {
        return awtTerminal.getCursorPosition();
    }

    
    public void setCursorVisible(boolean visible) {
        awtTerminal.setCursorVisible(visible);
    }

    
    public void putCharacter(char c) {
        awtTerminal.putCharacter(c);
    }

    
    public TextGraphics newTextGraphics() {
        return awtTerminal.newTextGraphics();
    }

    
    public void enableSGR(SGR sgr) {
        awtTerminal.enableSGR(sgr);
    }

    
    public void disableSGR(SGR sgr) {
        awtTerminal.disableSGR(sgr);
    }

    
    public void resetColorAndSGR() {
        awtTerminal.resetColorAndSGR();
    }

    
    public void setForegroundColor(TextColor color) {
        awtTerminal.setForegroundColor(color);
    }

    
    public void setBackgroundColor(TextColor color) {
        awtTerminal.setBackgroundColor(color);
    }

    
    public TerminalSize getTerminalSize() {
        return awtTerminal.getTerminalSize();
    }

    
    public byte[] enquireTerminal(int timeout, TimeUnit timeoutUnit) {
        return awtTerminal.enquireTerminal(timeout, timeoutUnit);
    }

    
    public void bell() {
        awtTerminal.bell();
    }

    
    public void flush() {
        awtTerminal.flush();
    }

    
    public void close() {
        awtTerminal.close();
    }

    
    public void addResizeListener(TerminalResizeListener listener) {
        awtTerminal.addResizeListener(listener);
    }

    
    public void removeResizeListener(TerminalResizeListener listener) {
        awtTerminal.removeResizeListener(listener);
    }

	public AWTTerminalImplementation getTerminalImplementation()
	{
		return awtTerminal.getTerminalImplementation();
	}
	
    public TerminalPosition getSelectionStartPosition()
	{
		return awtTerminal.getSelectionStartPosition();
	}

	public void setSelectionStartPosition(TerminalPosition position)
	{
		awtTerminal.setSelectionStartPosition(position);
	}

	public TerminalPosition getSelectionEndPosition()
	{
		return awtTerminal.getSelectionEndPosition();
	}

	public void setSelectionEndPosition(TerminalPosition position)
	{
		awtTerminal.setSelectionEndPosition(position);
	}
	
	public void putString(String string) {
        awtTerminal.putString(string);
    }
}
