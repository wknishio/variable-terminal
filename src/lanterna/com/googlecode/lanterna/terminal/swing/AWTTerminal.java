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
package com.googlecode.lanterna.terminal.swing;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.IOSafeTerminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;

import java.awt.*;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.im.InputMethodRequests;
import java.text.AttributedCharacterIterator;
import java.util.concurrent.TimeUnit;


/**
 * This class provides an AWT implementation of the Terminal interface that is an embeddable component you can put into
 * an AWT container. The class has static helper methods for opening a new frame with an AWTTerminal as its content,
 * similar to how the SwingTerminal used to work in earlier versions of lanterna. This version supports private mode and
 * non-private mode with a scrollback history. You can customize many of the properties by supplying device
 * configuration, font configuration and color configuration when you construct the object.
 * @author martin
 */
@SuppressWarnings("serial")
public class AWTTerminal extends Panel implements IOSafeTerminal {

    private final AWTTerminalImplementation terminalImplementation;
    private final TerminalInputMethodRequests inputMethodRequests;

    /**
     * Creates a new AWTTerminal with all the defaults set and no scroll controller connected.
     */
    public AWTTerminal(boolean customizeLastLine) {
        this(new TerminalScrollController.Null(), customizeLastLine);
    }
    
    public AWTTerminalImplementation getTerminalImplementation()
    {
    	return this.terminalImplementation;
    }


    /**
     * Creates a new AWTTerminal with a particular scrolling controller that will be notified when the terminals
     * history size grows and will be called when this class needs to figure out the current scrolling position.
     * @param scrollController Controller for scrolling the terminal history
     */
    @SuppressWarnings("WeakerAccess")
    public AWTTerminal(TerminalScrollController scrollController, boolean customizeLastLine) {
        this(TerminalEmulatorDeviceConfiguration.getDefault(),
                AWTTerminalFontConfiguration.getDefault(),
                TerminalEmulatorColorConfiguration.getDefault(),
                scrollController, customizeLastLine);
    }

    /**
     * Creates a new AWTTerminal component using custom settings and no scroll controller.
     * @param deviceConfiguration Device configuration to use for this AWTTerminal
     * @param fontConfiguration Font configuration to use for this AWTTerminal
     * @param colorConfiguration Color configuration to use for this AWTTerminal
     */
    public AWTTerminal(
            TerminalEmulatorDeviceConfiguration deviceConfiguration,
            AWTTerminalFontConfiguration fontConfiguration,
            TerminalEmulatorColorConfiguration colorConfiguration,
            boolean customizeLastLine) {

        this(null, deviceConfiguration, fontConfiguration, colorConfiguration, customizeLastLine);
    }

    /**
     * Creates a new AWTTerminal component using custom settings and no scroll controller.
     * @param initialTerminalSize Initial size of the terminal, which will be used when calculating the preferred size
     *                            of the component. If null, it will default to 80x25. If the AWT layout manager forces
     *                            the component to a different size, the value of this parameter won't have any meaning
     * @param deviceConfiguration Device configuration to use for this AWTTerminal
     * @param fontConfiguration Font configuration to use for this AWTTerminal
     * @param colorConfiguration Color configuration to use for this AWTTerminal
     */
    public AWTTerminal(
            TerminalSize initialTerminalSize,
            TerminalEmulatorDeviceConfiguration deviceConfiguration,
            AWTTerminalFontConfiguration fontConfiguration,
            TerminalEmulatorColorConfiguration colorConfiguration,
            boolean customizeLastLine) {

        this(initialTerminalSize,
                deviceConfiguration,
                fontConfiguration,
                colorConfiguration,
                new TerminalScrollController.Null(),
                customizeLastLine);
    }

    /**
     * Creates a new AWTTerminal component using custom settings and a custom scroll controller. The scrolling
     * controller will be notified when the terminal's history size grows and will be called when this class needs to
     * figure out the current scrolling position.
     * @param deviceConfiguration Device configuration to use for this AWTTerminal
     * @param fontConfiguration Font configuration to use for this AWTTerminal
     * @param colorConfiguration Color configuration to use for this AWTTerminal
     * @param scrollController Controller to use for scrolling, the object passed in will be notified whenever the
     *                         scrollable area has changed
     */
    public AWTTerminal(
            TerminalEmulatorDeviceConfiguration deviceConfiguration,
            AWTTerminalFontConfiguration fontConfiguration,
            TerminalEmulatorColorConfiguration colorConfiguration,
            TerminalScrollController scrollController,
            boolean customizeLastLine) {

        this(null, deviceConfiguration, fontConfiguration, colorConfiguration, scrollController, customizeLastLine);
    }



    /**
     * Creates a new AWTTerminal component using custom settings and a custom scroll controller. The scrolling
     * controller will be notified when the terminal's history size grows and will be called when this class needs to
     * figure out the current scrolling position.
     * @param initialTerminalSize Initial size of the terminal, which will be used when calculating the preferred size
     *                            of the component. If null, it will default to 80x25. If the AWT layout manager forces
     *                            the component to a different size, the value of this parameter won't have any meaning
     * @param deviceConfiguration Device configuration to use for this AWTTerminal
     * @param fontConfiguration Font configuration to use for this AWTTerminal
     * @param colorConfiguration Color configuration to use for this AWTTerminal
     * @param scrollController Controller to use for scrolling, the object passed in will be notified whenever the
     *                         scrollable area has changed
     */
    public AWTTerminal(
            TerminalSize initialTerminalSize,
            TerminalEmulatorDeviceConfiguration deviceConfiguration,
            AWTTerminalFontConfiguration fontConfiguration,
            TerminalEmulatorColorConfiguration colorConfiguration,
            TerminalScrollController scrollController,
            boolean customizeLastLine) {

        //Enforce valid values on the input parameters
        if(deviceConfiguration == null) {
            deviceConfiguration = TerminalEmulatorDeviceConfiguration.getDefault();
        }
        if(fontConfiguration == null) {
            fontConfiguration = SwingTerminalFontConfiguration.getDefault();
        }
        if(colorConfiguration == null) {
            colorConfiguration = TerminalEmulatorColorConfiguration.getDefault();
        }
        
        // This will enable CJK and complex input systems
        enableInputMethods(true);

        // For some reason an InputMethodListener needs to be attached in order to start receiving IME events.
        addInputMethodListener(new InputMethodListener() {
            
            public void inputMethodTextChanged(InputMethodEvent event) {
            }

            
            public void caretPositionChanged(InputMethodEvent event) {
            }
        });

        terminalImplementation = new AWTTerminalImplementation(
                this,
                fontConfiguration,
                initialTerminalSize,
                deviceConfiguration,
                colorConfiguration,
                scrollController,
                customizeLastLine);
        
        inputMethodRequests = new TerminalInputMethodRequests(this, terminalImplementation);
    }
    
    /**
     * Returns the current font configuration. Note that it is immutable and cannot be changed.
     * @return This AWTTerminal's current font configuration
     */
    public AWTTerminalFontConfiguration getFontConfiguration() {
        return terminalImplementation.getFontConfiguration();
    }

    /**
     * Returns this terminal emulator's color configuration. Note that it is immutable and cannot be changed.
     * @return This {@link AWTTerminal}'s color configuration
     */
    public TerminalEmulatorColorConfiguration getColorConfiguration() {
        return terminalImplementation.getColorConfiguration();
    }

    /**
     * Returns this terminal emulator's device configuration. Note that it is immutable and cannot be changed.
     * @return This {@link AWTTerminal}'s device configuration
     */
    public TerminalEmulatorDeviceConfiguration getDeviceConfiguration() {
        return terminalImplementation.getDeviceConfiguration();
    }

    /**
     * Overridden method from AWT's {@code Component} class that returns the preferred size of the terminal (in pixels)
     * @return The terminal's preferred size in pixels
     */
    
    public synchronized Dimension getPreferredSize() {
        return terminalImplementation.getPreferredSize();
    }

    /**
     * Overridden method from AWT's {@code Component} class that is called by OS window system when the component needs
     * to be redrawn
     * @param componentGraphics {@code Graphics} object to use when drawing the component
     */
    
    public synchronized void paint(Graphics componentGraphics) {
        terminalImplementation.paintComponent(componentGraphics);
    }

    /**
     * Overridden method from AWT's {@code Component} class that is called by OS window system when the component needs
     * to be updated (the size has changed) and redrawn
     * @param componentGraphics {@code Graphics} object to use when drawing the component
     */
    
    public synchronized void update(Graphics componentGraphics) {
        // This is supposed to solve AWT flickering, but in my testing this method isn't called at all!
        terminalImplementation.paintComponent(componentGraphics);
    }

    /**
     * Takes a KeyStroke and puts it on the input queue of the terminal emulator. This way you can insert synthetic
     * input events to be processed as if they came from the user typing on the keyboard.
     * @param keyStroke Key stroke input event to put on the queue
     */
    public void addInput(KeyStroke keyStroke) {
        terminalImplementation.addInput(keyStroke);
    }
    
    
    public InputMethodRequests getInputMethodRequests() {
        return inputMethodRequests;
    }

    
    protected void processInputMethodEvent(InputMethodEvent e) {
        AttributedCharacterIterator iterator = e.getText();
        for(int i = 0; i < e.getCommittedCharacterCount(); i++) {
            terminalImplementation.addInput(new KeyStroke(iterator.current(), false, false));
            iterator.next();
        }
    }

    // Terminal methods below here, just forward to the implementation

    
    public void enterPrivateMode() {
        terminalImplementation.enterPrivateMode();
    }

    
    public void exitPrivateMode() {
        terminalImplementation.exitPrivateMode();
    }

    
    public void clearScreen() {
        terminalImplementation.clearScreen();
    }

    
    public void setCursorPosition(int x, int y) {
        terminalImplementation.setCursorPosition(x, y);
    }

    
    public void setCursorPosition(TerminalPosition position) {
        terminalImplementation.setCursorPosition(position);
    }

    
    public TerminalPosition getCursorPosition() {
        return terminalImplementation.getCursorPosition();
    }

    
    public void setCursorVisible(boolean visible) {
        terminalImplementation.setCursorVisible(visible);
    }

    
    public void putCharacter(char c) {
        terminalImplementation.putCharacter(c);
    }

    
    public void enableSGR(SGR sgr) {
        terminalImplementation.enableSGR(sgr);
    }

    
    public void disableSGR(SGR sgr) {
        terminalImplementation.disableSGR(sgr);
    }

    
    public void resetColorAndSGR() {
        terminalImplementation.resetColorAndSGR();
    }

    
    public void setForegroundColor(TextColor color) {
        terminalImplementation.setForegroundColor(color);
    }

    
    public void setBackgroundColor(TextColor color) {
        terminalImplementation.setBackgroundColor(color);
    }

    
    public TerminalSize getTerminalSize() {
        return terminalImplementation.getTerminalSize();
    }

    
    public byte[] enquireTerminal(int timeout, TimeUnit timeoutUnit) {
        return terminalImplementation.enquireTerminal(timeout, timeoutUnit);
    }

    
    public void bell() {
        terminalImplementation.bell();
    }

    
    public void flush() {
        terminalImplementation.flush();
    }

    
    public void close() {
        terminalImplementation.close();
    }

    
    public KeyStroke pollInput() {
        return terminalImplementation.pollInput();
    }

    
    public KeyStroke readInput() {
        return terminalImplementation.readInput();
    }

    
    public TextGraphics newTextGraphics() {
        return terminalImplementation.newTextGraphics();
    }

    
    public void addResizeListener(TerminalResizeListener listener) {
        terminalImplementation.addResizeListener(listener);
    }

    
    public void removeResizeListener(TerminalResizeListener listener) {
        terminalImplementation.removeResizeListener(listener);
    }
    
    public AWTTerminalFontConfiguration getTerminalFontConfiguration()
    {
		return terminalImplementation.getFontConfiguration();
    }
    
	public TerminalPosition getSelectionStartPosition()
	{
		return terminalImplementation.getSelectionStartPosition();
	}

	public void setSelectionStartPosition(TerminalPosition position)
	{
		terminalImplementation.setSelectionStartPosition(position);
	}

	public TerminalPosition getSelectionEndPosition()
	{
		return terminalImplementation.getSelectionEndPosition();
	}

	public void setSelectionEndPosition(TerminalPosition position)
	{
		terminalImplementation.setSelectionEndPosition(position);
	}

	public void putString(String string) {
        terminalImplementation.putString(string);
    }
}
