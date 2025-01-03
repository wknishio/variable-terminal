package com.googlecode.lanterna.terminal.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.SystemColor;

@SuppressWarnings("serial")
public class AWTTerminalPanel extends Panel
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

  public AWTTerminalPanel(AWTTerminal awtTerminal)
  {
    this.awtTerminal = awtTerminal;
    
    BorderLayout panelLayout = new BorderLayout();
    panelLayout.setHgap(0);
    panelLayout.setVgap(0);
    setLayout(panelLayout);
    
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
    
    BorderLayout bottomLayout = new BorderLayout();
    bottomLayout.setHgap(0);
    bottomLayout.setVgap(0);
    bottomPanel = new Panel();
    bottomPanel.setLayout(bottomLayout);
    bottomPanel.setBackground(SystemColor.control);
    
    BorderLayout centerLayout = new BorderLayout();
    centerLayout.setHgap(0);
    centerLayout.setVgap(0);
    centerPanel = new Panel();
    centerPanel.setLayout(centerLayout);
    centerPanel.setBackground(Color.BLACK);
    
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
}
