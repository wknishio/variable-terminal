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
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.IOSafeTerminal;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.terminal.TerminalResizeListener;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This class is similar to what SwingTerminal used to be before Lanterna 3.0; a Frame that contains a terminal
 * emulator. In Lanterna 3, this class is just an AWT Frame containing a {@link AWTTerminal} component, but it also
 * implements the {@link com.googlecode.lanterna.terminal.Terminal} interface and delegates all calls to the internal
 * {@link AWTTerminal}. You can tweak the class a bit to have special behaviours when exiting private mode or when the
 * user presses ESC key.
 *
 * <p>Please note that this is the AWT version and there is a Swing counterpart: {@link SwingTerminalFrame}
 * @see AWTTerminal
 * @see SwingTerminalFrame
 * @author martin
 */
@SuppressWarnings("serial")
public class AWTTerminalFrame extends Frame implements IOSafeTerminal {
    private final AWTTerminal awtTerminal;
    private final EnumSet<TerminalEmulatorAutoCloseTrigger> autoCloseTriggers;
    //private Dimension initialSize;
    private boolean disposed;
	private TerminalSize defaultTerminalSize;
	
	//private ScrollPane scrollpane;
	
	private Panel midPanel;
	private Panel bottomPanel;
	private Panel spacerPanelNorth;
	private Panel spacerPanelSouth;
	private Panel spacerPanelWest;
	private Panel spacerPanelEast;


    /**
     * Creates a new AWTTerminalFrame with an optional list of auto-close triggers
     * @param autoCloseTriggers What to trigger automatic disposal of the Frame
     */
    @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
    public AWTTerminalFrame(java.awt.Color lastLineBackground, TerminalEmulatorAutoCloseTrigger... autoCloseTriggers) {
        this("AwtTerminalFrame", lastLineBackground, autoCloseTriggers);
    }

    /**
     * Creates a new AWTTerminalFrame with a given window title and an optional list of auto-close triggers
     * @param title Title to use for the window
     * @param autoCloseTriggers What to trigger automatic disposal of the Frame
     */
    @SuppressWarnings("WeakerAccess")
    public AWTTerminalFrame(String title, java.awt.Color lastLineBackground, TerminalEmulatorAutoCloseTrigger... autoCloseTriggers) throws HeadlessException {
        this(title, new AWTTerminal(lastLineBackground), autoCloseTriggers);
    }

    /**
     * Creates a new AWTTerminalFrame using a specified title and a series of AWT terminal configuration objects
     * @param title What title to use for the window
     * @param deviceConfiguration Device configuration for the embedded AWTTerminal
     * @param fontConfiguration Font configuration for the embedded AWTTerminal
     * @param colorConfiguration Color configuration for the embedded AWTTerminal
     * @param autoCloseTriggers What to trigger automatic disposal of the Frame
     */
    public AWTTerminalFrame(String title,
                            TerminalEmulatorDeviceConfiguration deviceConfiguration,
                            AWTTerminalFontConfiguration fontConfiguration,
                            TerminalEmulatorColorConfiguration colorConfiguration,
                            java.awt.Color lastLineBackground,
                            TerminalEmulatorAutoCloseTrigger... autoCloseTriggers) {
        this(title, null, deviceConfiguration, fontConfiguration, colorConfiguration, lastLineBackground, autoCloseTriggers);
    }

    /**
     * Creates a new AWTTerminalFrame using a specified title and a series of AWT terminal configuration objects
     * @param title What title to use for the window
     * @param terminalSize Initial size of the terminal, in rows and columns. If null, it will default to 80x25.
     * @param deviceConfiguration Device configuration for the embedded AWTTerminal
     * @param fontConfiguration Font configuration for the embedded AWTTerminal
     * @param colorConfiguration Color configuration for the embedded AWTTerminal
     * @param autoCloseTriggers What to trigger automatic disposal of the Frame
     */
    public AWTTerminalFrame(String title,
                            TerminalSize terminalSize,
                            TerminalEmulatorDeviceConfiguration deviceConfiguration,
                            AWTTerminalFontConfiguration fontConfiguration,
                            TerminalEmulatorColorConfiguration colorConfiguration,
                            java.awt.Color lastLineBackground,
                            TerminalEmulatorAutoCloseTrigger... autoCloseTriggers
                            ) {
        this(title,
                new AWTTerminal(terminalSize, deviceConfiguration, fontConfiguration, colorConfiguration, lastLineBackground),
                autoCloseTriggers);
    }
    
    private AWTTerminalFrame(String title, AWTTerminal awtTerminal, TerminalEmulatorAutoCloseTrigger... autoCloseTrigger) {
        super(title != null ? title : "AWTTerminalFrame");
        this.awtTerminal = awtTerminal;
        this.autoCloseTriggers = EnumSet.copyOf(Arrays.asList(autoCloseTrigger));
        this.disposed = false;
        BorderLayout frameLayout = new BorderLayout();
        frameLayout.setHgap(0);
        frameLayout.setVgap(0);
        setLayout(frameLayout);
        
//        scrollpane = new AWTLanternaScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
//        scrollpane.add(new AWTLanternaStretchPane(this, awtTerminal));
//        scrollpane.setBackground(Color.BLACK);
//        scrollpane.setWheelScrollingEnabled(false);
//        scrollpane.setSize(awtTerminal.getPreferredSize());
//        
//        add(scrollpane, BorderLayout.CENTER);
        
        spacerPanelNorth = new Panel();
        spacerPanelNorth.setSize(0, 1);
        spacerPanelNorth.setMinimumSize(new Dimension(0, 1));
        spacerPanelNorth.setMaximumSize(new Dimension(0, 1));
        spacerPanelNorth.setPreferredSize(new Dimension(0, 1));
        spacerPanelNorth.setBackground(Color.BLACK);
        
        spacerPanelSouth = new Panel();
        spacerPanelSouth.setSize(0, 1);
        spacerPanelSouth.setMinimumSize(new Dimension(0, 1));
        spacerPanelSouth.setMaximumSize(new Dimension(0, 1));
        spacerPanelSouth.setPreferredSize(new Dimension(0, 1));
        spacerPanelSouth.setBackground(Color.BLACK);
        
        spacerPanelWest = new Panel();
        spacerPanelWest.setSize(1, 0);
        spacerPanelWest.setMinimumSize(new Dimension(1, 0));
        spacerPanelWest.setMaximumSize(new Dimension(1, 0));
        spacerPanelWest.setPreferredSize(new Dimension(1, 0));
        spacerPanelWest.setBackground(Color.BLACK);
        
        spacerPanelEast = new Panel();
        spacerPanelEast.setSize(1, 0);
        spacerPanelEast.setMinimumSize(new Dimension(1, 0));
        spacerPanelEast.setMaximumSize(new Dimension(1, 0));
        spacerPanelEast.setPreferredSize(new Dimension(1, 0));
        spacerPanelEast.setBackground(Color.BLACK);
        
        BorderLayout bottomlayout = new BorderLayout();
        bottomlayout.setHgap(0);
        bottomlayout.setVgap(0);
        bottomPanel = new Panel();
        bottomPanel.setLayout(bottomlayout);
        bottomPanel.setBackground(SystemColor.control);
        
        BorderLayout midlayout = new BorderLayout();
        midlayout.setHgap(0);
        midlayout.setVgap(0);
        midPanel = new Panel();
        midPanel.setLayout(midlayout);
        midPanel.add(awtTerminal, BorderLayout.CENTER);
        midPanel.add(spacerPanelNorth, BorderLayout.NORTH);
        midPanel.add(spacerPanelSouth, BorderLayout.SOUTH);
        midPanel.add(spacerPanelWest, BorderLayout.WEST);
        midPanel.add(spacerPanelEast, BorderLayout.EAST);
        midPanel.setBackground(Color.BLACK);
        
        add(midPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setBackground(Color.BLACK); //This will reduce white flicker when resizing the window
        pack();
//        getInsets().set(0, 0, 0, 0);
//        midPanel.getInsets().set(0, 0, 0, 0);
//        bottomPanel.getInsets().set(0, 0, 0, 0);
//        spacerPanelNorth.getInsets().set(0, 0, 0, 0);
//        spacerPanelSouth.getInsets().set(0, 0, 0, 0);
//        spacerPanelWest.getInsets().set(0, 0, 0, 0);
//        spacerPanelEast.getInsets().set(0, 0, 0, 0);
        //initialSize = this.getPreferredSize();
        //Put input focus on the terminal component by default
        awtTerminal.requestFocusInWindow();
    }
    
    /**
     * Returns the current font configuration. Note that it is immutable and cannot be changed.
     * @return This {@link AWTTerminalFrame}'s current font configuration
     */
    public AWTTerminalFontConfiguration getFontConfiguration() {
        return awtTerminal.getFontConfiguration();
    }

    /**
     * Returns this terminal emulator's color configuration. Note that it is immutable and cannot be changed.
     * @return This {@link AWTTerminalFrame}'s color configuration
     */
    public TerminalEmulatorColorConfiguration getColorConfiguration() {
        return awtTerminal.getColorConfiguration();
    }

    /**
     * Returns this terminal emulator's device configuration. Note that it is immutable and cannot be changed.
     * @return This {@link AWTTerminalFrame}'s device configuration
     */
    public TerminalEmulatorDeviceConfiguration getDeviceConfiguration() {
        return awtTerminal.getDeviceConfiguration();
    }
    
    public AWTTerminal getTerminal()
    {
    	return this.awtTerminal;
    }

    /**
     * Returns the auto-close triggers used by the AWTTerminalFrame
     * @return Current auto-close trigger
     */
    public Set<TerminalEmulatorAutoCloseTrigger> getAutoCloseTrigger() {
        return EnumSet.copyOf(autoCloseTriggers);
    }

    public void addAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger autoCloseTrigger) {
        autoCloseTriggers.add(autoCloseTrigger);
    }

    
    public void dispose() {
        super.dispose();
        disposed = true;
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
        awtTerminal.addInput(keyStroke);
    }

    ///////////
    // Delegate all Terminal interface implementations to AWTTerminal
    ///////////
    
    public KeyStroke pollInput() {
        if(disposed) {
            return new KeyStroke(KeyType.EOF);
        }
        KeyStroke keyStroke = awtTerminal.pollInput();
        if(autoCloseTriggers.contains(TerminalEmulatorAutoCloseTrigger.CloseOnEscape) &&
                keyStroke != null && 
                keyStroke.getKeyType() == KeyType.Escape) {
            dispose();
        }
        return keyStroke;
    }

    
    public KeyStroke readInput() {
        return awtTerminal.readInput();
    }

    
    public void enterPrivateMode() {
        awtTerminal.enterPrivateMode();
    }

    
    public void exitPrivateMode() {
        awtTerminal.exitPrivateMode();
        if(autoCloseTriggers.contains(TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode)) {
            dispose();
        }
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

    
    public void addResizeListener(TerminalResizeListener listener) {
        awtTerminal.addResizeListener(listener);
    }

    
    public void removeResizeListener(TerminalResizeListener listener) {
        awtTerminal.removeResizeListener(listener);
    }
    
    public void setDefaultTerminalSize(TerminalSize size)
    {
    	this.defaultTerminalSize = size;
    }
    
    public void resetTerminalSize()
    {
    	if (defaultTerminalSize != null)
    	{
    		awtTerminal.getTerminalImplementation().setTerminalSize(defaultTerminalSize);
    	}
    }
    
    //public TerminalSize getTerminalSize()
    //{
    	//return awtTerminal.getTerminalSize();
    //}
    
    public Panel getBottomPanel()
    {
    	return bottomPanel;
    }
    
    public Panel getMidPanel()
    {
      return midPanel;
    }
    
//    public ScrollPane getScrollPane()
//    {
//      return scrollpane;
//    }
    
    public void pack()
    {
    	super.pack();
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
