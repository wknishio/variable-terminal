package com.googlecode.lanterna.terminal.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.SystemColor;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.IOSafeTerminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;

@SuppressWarnings("serial")
public class AWTTerminalPanel extends Panel implements IOSafeTerminal
{
  private static final long serialVersionUID = 1L;
  
  private Panel centerPanel;
  private Panel bottomPanel;
  private Panel terminalPanel;
  private Panel spacerPanelNorth;
  private Panel spacerPanelSouth;
  private Panel spacerPanelWest;
  private Panel spacerPanelEast;
  private AWTTerminal awtTerminal;
  private boolean disposed;
  private TerminalSize defaultTerminalSize;
  private final EnumSet<TerminalEmulatorAutoCloseTrigger> autoCloseTriggers;

  public AWTTerminalPanel(AWTTerminal awtTerminal, TerminalEmulatorAutoCloseTrigger... autoCloseTrigger)
  {
    this.awtTerminal = awtTerminal;
    this.autoCloseTriggers = EnumSet.copyOf(Arrays.asList(autoCloseTrigger));
    
    BorderLayout panelLayout = new BorderLayout();
    panelLayout.setHgap(0);
    panelLayout.setVgap(0);
    setLayout(panelLayout);
    setFocusable(true);
    
    spacerPanelNorth = new Panel();
    spacerPanelNorth.setSize(0, 1);
    spacerPanelNorth.setMinimumSize(new Dimension(0, 1));
    spacerPanelNorth.setMaximumSize(new Dimension(0, 1));
    spacerPanelNorth.setPreferredSize(new Dimension(0, 1));
    spacerPanelNorth.setBackground(Color.BLACK);
    spacerPanelNorth.setFocusable(false);
    
    spacerPanelSouth = new Panel();
    spacerPanelSouth.setSize(0, 1);
    spacerPanelSouth.setMinimumSize(new Dimension(0, 1));
    spacerPanelSouth.setMaximumSize(new Dimension(0, 1));
    spacerPanelSouth.setPreferredSize(new Dimension(0, 1));
    spacerPanelSouth.setBackground(Color.BLACK);
    spacerPanelSouth.setFocusable(false);
    
    spacerPanelWest = new Panel();
    spacerPanelWest.setSize(1, 0);
    spacerPanelWest.setMinimumSize(new Dimension(1, 0));
    spacerPanelWest.setMaximumSize(new Dimension(1, 0));
    spacerPanelWest.setPreferredSize(new Dimension(1, 0));
    spacerPanelWest.setBackground(Color.BLACK);
    spacerPanelWest.setFocusable(false);
    
    spacerPanelEast = new Panel();
    spacerPanelEast.setSize(1, 0);
    spacerPanelEast.setMinimumSize(new Dimension(1, 0));
    spacerPanelEast.setMaximumSize(new Dimension(1, 0));
    spacerPanelEast.setPreferredSize(new Dimension(1, 0));
    spacerPanelEast.setBackground(Color.BLACK);
    spacerPanelEast.setFocusable(false);
    
    BorderLayout bottomLayout = new BorderLayout();
    bottomLayout.setHgap(0);
    bottomLayout.setVgap(0);
    bottomPanel = new Panel();
    bottomPanel.setLayout(bottomLayout);
    bottomPanel.setBackground(SystemColor.control);
    bottomPanel.setFocusable(false);
    
    BorderLayout centerLayout = new BorderLayout();
    centerLayout.setHgap(0);
    centerLayout.setVgap(0);
    centerPanel = new Panel();
    centerPanel.setLayout(centerLayout);
    centerPanel.setBackground(Color.BLACK);
    centerPanel.setFocusable(true);
    
    BorderLayout terminalLayout = new BorderLayout();
    terminalLayout.setHgap(0);
    terminalLayout.setVgap(0);
    terminalPanel = new Panel();
    terminalPanel.setLayout(terminalLayout);
    terminalPanel.add(awtTerminal, BorderLayout.CENTER);
    terminalPanel.add(spacerPanelNorth, BorderLayout.NORTH);
    terminalPanel.add(spacerPanelSouth, BorderLayout.SOUTH);
    terminalPanel.add(spacerPanelWest, BorderLayout.WEST);
    terminalPanel.add(spacerPanelEast, BorderLayout.EAST);
    terminalPanel.setBackground(Color.BLACK);
    
    centerPanel.add(terminalPanel, BorderLayout.CENTER);
    
    add(centerPanel, BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);
    
    setBackground(Color.BLACK);
  }
  
  public Panel getBottomPanel()
  {
    return bottomPanel;
  }
  
  public Panel getCenterPanel()
  {
    return centerPanel;
  }

  public void setSpacerBackgroundColor(java.awt.Color color) {
    spacerPanelNorth.setBackground(color);
    spacerPanelSouth.setBackground(color);
    spacerPanelWest.setBackground(color);
    spacerPanelEast.setBackground(color);
    terminalPanel.setBackground(color);
    centerPanel.setBackground(color);
    awtTerminal.setBackground(color);
  }
  
  public AWTTerminal getTerminal()
  {
    return awtTerminal;
  }
  
  public void dispose()
  {
    //super.dispose();
    disposed = true;
  }
  
  public void close()
  {
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
  
  
//  public ScrollPane getScrollPane()
//  {
//    return scrollpane;
//  }

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
