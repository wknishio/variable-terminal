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
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

import com.googlecode.lanternavt.SGR;
import com.googlecode.lanternavt.TerminalPosition;
import com.googlecode.lanternavt.TerminalSize;
import com.googlecode.lanternavt.TextColor;
import com.googlecode.lanternavt.graphics.TextGraphics;
import com.googlecode.lanternavt.input.KeyStroke;
import com.googlecode.lanternavt.input.KeyType;
import com.googlecode.lanternavt.terminal.IOSafeTerminal;
import com.googlecode.lanternavt.terminal.TerminalResizeListener;

/**
 * This class is similar to what SwingTerminal used to be before Lanterna 3.0; a JFrame that contains a terminal
 * emulator. In Lanterna 3, this class is just a JFrame containing a SwingTerminal component, but it also implements
 * the Terminal interface and delegates all calls to the internal SwingTerminal. You can tweak the class a bit to have
 * special behaviours when exiting private mode or when the user presses ESC key.
 * @author martin
 */
@SuppressWarnings("serial")
public class SwingTerminalFrame extends JFrame implements IOSafeTerminal {
    private final SwingTerminal swingTerminal;
    private final EnumSet<TerminalEmulatorAutoCloseTrigger> autoCloseTriggers;
    private boolean disposed;

    /**
     * Creates a new SwingTerminalFrame with an optional list of auto-close triggers
     * @param autoCloseTriggers What to trigger automatic disposal of the JFrame
     */
    @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
    public SwingTerminalFrame(TerminalEmulatorAutoCloseTrigger... autoCloseTriggers) {
        this("SwingTerminalFrame", autoCloseTriggers);
    }

    /**
     * Creates a new SwingTerminalFrame with a specific title and an optional list of auto-close triggers
     * @param title Title to use for the window
     * @param autoCloseTriggers What to trigger automatic disposal of the JFrame
     */
    @SuppressWarnings("WeakerAccess")
    public SwingTerminalFrame(String title, TerminalEmulatorAutoCloseTrigger... autoCloseTriggers) throws HeadlessException {
        this(title, new SwingTerminal(), autoCloseTriggers);
    }

    /**
     * Creates a new SwingTerminalFrame using a specified title and a series of swing terminal configuration objects
     * @param title What title to use for the window
     * @param deviceConfiguration Device configuration for the embedded SwingTerminal
     * @param fontConfiguration Font configuration for the embedded SwingTerminal
     * @param colorConfiguration Color configuration for the embedded SwingTerminal
     * @param autoCloseTriggers What to trigger automatic disposal of the JFrame
     */
    public SwingTerminalFrame(String title,
            TerminalEmulatorDeviceConfiguration deviceConfiguration,
            SwingTerminalFontConfiguration fontConfiguration,
            TerminalEmulatorColorConfiguration colorConfiguration,
            TerminalEmulatorAutoCloseTrigger... autoCloseTriggers) {
        this(title, null, deviceConfiguration, fontConfiguration, colorConfiguration, autoCloseTriggers);
    }

    /**
     * Creates a new SwingTerminalFrame using a specified title and a series of swing terminal configuration objects
     * @param title What title to use for the window
     * @param terminalSize Initial size of the terminal, in rows and columns. If null, it will default to 80x25.
     * @param deviceConfiguration Device configuration for the embedded SwingTerminal
     * @param fontConfiguration Font configuration for the embedded SwingTerminal
     * @param colorConfiguration Color configuration for the embedded SwingTerminal
     * @param autoCloseTriggers What to trigger automatic disposal of the JFrame
     */
    public SwingTerminalFrame(String title,
                              TerminalSize terminalSize,
                              TerminalEmulatorDeviceConfiguration deviceConfiguration,
                              SwingTerminalFontConfiguration fontConfiguration,
                              TerminalEmulatorColorConfiguration colorConfiguration,
                              TerminalEmulatorAutoCloseTrigger... autoCloseTriggers) {
        this(title,
                new SwingTerminal(terminalSize, deviceConfiguration, fontConfiguration, colorConfiguration),
                autoCloseTriggers);
    }
    
    private SwingTerminalFrame(String title, SwingTerminal swingTerminal, TerminalEmulatorAutoCloseTrigger... autoCloseTriggers) {
        super(title != null ? title : "SwingTerminalFrame");
        this.swingTerminal = swingTerminal;
        this.autoCloseTriggers = EnumSet.copyOf(Arrays.asList(autoCloseTriggers));
        this.disposed = false;

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(swingTerminal, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(Color.BLACK); //This will reduce white flicker when resizing the window
        pack();

        //Put input focus on the terminal component by default
        swingTerminal.requestFocusInWindow();
    }
    
    /**
     * Returns the current font configuration. Note that it is immutable and cannot be changed.
     * @return This {@link SwingTerminalFrame}'s current font configuration
     */
    public SwingTerminalFontConfiguration getFontConfiguration() {
        return swingTerminal.getFontConfiguration();
    }

    /**
     * Returns this terminal emulator's color configuration. Note that it is immutable and cannot be changed.
     * @return This {@link SwingTerminalFrame}'s color configuration
     */
    public TerminalEmulatorColorConfiguration getColorConfiguration() {
        return swingTerminal.getColorConfiguration();
    }

    /**
     * Returns this terminal emulator's device configuration. Note that it is immutable and cannot be changed.
     * @return This {@link SwingTerminalFrame}'s device configuration
     */
    public TerminalEmulatorDeviceConfiguration getDeviceConfiguration() {
        return swingTerminal.getDeviceConfiguration();
    }
    
    public SwingTerminal getTerminal()
    {
      return this.swingTerminal;
    }

    /**
     * Returns the auto-close triggers used by the SwingTerminalFrame
     * @return Current auto-close trigger
     */
    public Set<TerminalEmulatorAutoCloseTrigger> getAutoCloseTrigger() {
        return EnumSet.copyOf(autoCloseTriggers);
    }

    /**
     * Sets the auto-close trigger to use on this terminal. This will reset any previous triggers. If called with
     * {@code null}, all triggers are cleared.
     * @param autoCloseTrigger Auto-close trigger to use on this terminal, or {@code null} to clear all existing triggers
     * @return Itself
     */
    public SwingTerminalFrame setAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger autoCloseTrigger) {
        this.autoCloseTriggers.clear();
        if(autoCloseTrigger != null) {
            this.autoCloseTriggers.add(autoCloseTrigger);
        }
        return this;
    }

    /**
     * Adds an auto-close trigger to use on this terminal.
     * @param autoCloseTrigger Auto-close trigger to add to this terminal
     * @return Itself
     */
    public SwingTerminalFrame addAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger autoCloseTrigger) {
        if(autoCloseTrigger != null) {
            this.autoCloseTriggers.add(autoCloseTrigger);
        }
        return this;
    }

    
    public void dispose() {
        super.dispose();
        disposed = true;
    }
    
    
    public void pack() {
        super.pack();
        disposed = false;
    }

    
    public void close() {
        dispose();
    }

    /**
     * Takes a KeyStroke and puts it on the input queue of the terminal emulator. This way you can insert synthetic
     * input events to be processed as if they came from the user typing on the keyboard.
     * @param keyStroke Key stroke input event to put on the queue
     */
    public void addInput(KeyStroke keyStroke) {
        swingTerminal.addInput(keyStroke);
    }

    ///////////
    // Delegate all Terminal interface implementations to SwingTerminal
    ///////////
    
    public KeyStroke pollInput() {
        if(disposed) {
            return new KeyStroke(KeyType.EOF);
        }
        KeyStroke keyStroke = swingTerminal.pollInput();
        if(autoCloseTriggers.contains(TerminalEmulatorAutoCloseTrigger.CloseOnEscape) &&
                keyStroke != null && 
                keyStroke.getKeyType() == KeyType.ESCAPE) {
            dispose();
        }
        return keyStroke;
    }

    
    public KeyStroke readInput() {
        return swingTerminal.readInput();
    }

    
    public void enterPrivateMode() {
        swingTerminal.enterPrivateMode();
    }

    
    public void exitPrivateMode() {
        swingTerminal.exitPrivateMode();
        if(autoCloseTriggers.contains(TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode)) {
            dispose();
        }
    }

    
    public void clearScreen() {
        swingTerminal.clearScreen();
    }

    
    public void setCursorPosition(int x, int y) {
        swingTerminal.setCursorPosition(x, y);
    }

    
    public void setCursorPosition(TerminalPosition position) {
        swingTerminal.setCursorPosition(position);
    }

    
    public TerminalPosition getCursorPosition() {
        return swingTerminal.getCursorPosition();
    }

    
    public void setCursorVisible(boolean visible) {
        swingTerminal.setCursorVisible(visible);
    }

    
    public void putCharacter(char c) {
        swingTerminal.putCharacter(c);
    }

    
    public TextGraphics newTextGraphics() {
        return swingTerminal.newTextGraphics();
    }

    
    public void enableSGR(SGR sgr) {
        swingTerminal.enableSGR(sgr);
    }

    
    public void disableSGR(SGR sgr) {
        swingTerminal.disableSGR(sgr);
    }

    
    public void resetColorAndSGR() {
        swingTerminal.resetColorAndSGR();
    }

    
    public void setForegroundColor(TextColor color) {
        swingTerminal.setForegroundColor(color);
    }

    
    public void setBackgroundColor(TextColor color) {
        swingTerminal.setBackgroundColor(color);
    }

    
    public TerminalSize getTerminalSize() {
        return swingTerminal.getTerminalSize();
    }

    
    public byte[] enquireTerminal(int timeout, TimeUnit timeoutUnit) {
        return swingTerminal.enquireTerminal(timeout, timeoutUnit);
    }

    
    public void bell() {
        swingTerminal.bell();
    }

    
    public void flush() {
        swingTerminal.flush();
    }

    
    public void addResizeListener(TerminalResizeListener listener) {
        swingTerminal.addResizeListener(listener);
    }

    
    public void removeResizeListener(TerminalResizeListener listener) {
        swingTerminal.removeResizeListener(listener);
    }
    
    public TerminalPosition getSelectionStartPosition()
	{
		return swingTerminal.getSelectionStartPosition();
	}

	public void setSelectionStartPosition(TerminalPosition position)
	{
		swingTerminal.setSelectionStartPosition(position);
	}

	public TerminalPosition getSelectionEndPosition()
	{
		return swingTerminal.getSelectionEndPosition();
	}

	public void setSelectionEndPosition(TerminalPosition position)
	{
		swingTerminal.setSelectionEndPosition(position);
	}
	
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            pack();

            //Put input focus on the terminal component by default
            swingTerminal.requestFocusInWindow();
        }
        super.setVisible(visible);
    }
    
    public void putString(String string) {
        swingTerminal.putString(string);
    }
}
