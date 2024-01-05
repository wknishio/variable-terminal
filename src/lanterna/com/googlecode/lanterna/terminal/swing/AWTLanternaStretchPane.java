package com.googlecode.lanterna.terminal.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Panel;

public class AWTLanternaStretchPane extends Panel
{
  private static final long serialVersionUID = 1L;
  
  public AWTLanternaStretchPane(Component child)
  {
    this.setLayout(new BorderLayout());
    this.add(child, BorderLayout.CENTER);
  }  
}
