package org.vash.vate.client.graphicsmode;

// import java.awt.BorderLayout;
// import java.awt.FlowLayout;
import java.awt.FlowLayout;
// import java.awt.Font;
// import java.awt.GridLayout;
import java.awt.Label;
// import java.awt.Color;
// import java.awt.Cursor;
// import java.awt.Cursor;
import java.awt.Panel;
import java.awt.SystemColor;
// import java.awt.TextField;

public class VTGraphicsModeClientStatusBar extends Panel
{
  private static final long serialVersionUID = 1L;
  // private Label voidLabel;
  private Label modeStatusLabel;
  private Label viewStatusLabel;
  private Label viewQualityLabel;
  private Label controlStatusLabel;
  
  public VTGraphicsModeClientStatusBar()
  {
    // this.voidLabel = new Label("", Label.LEFT);
    this.modeStatusLabel = new Label("", Label.LEFT);
    this.viewStatusLabel = new Label("", Label.LEFT);
    this.viewQualityLabel = new Label("", Label.LEFT);
    this.controlStatusLabel = new Label("", Label.LEFT);
    this.getInsets().set(0, 0, 0, 0);
    this.setFocusable(false);
    this.setBackground(SystemColor.control);
    this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    // this.modeStatusLabel.setEditable(false);
    // this.modeStatusLabel.setFocusable(false);
    // this.modeStatusLabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    // this.viewStatusTextField.setEditable(false);
    // this.viewStatusTextField.setFocusable(false);
    // this.viewStatusTextField.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    // this.controlStatusTextField.setEditable(false);
    // this.controlStatusTextField.setFocusable(false);
    // this.controlStatusTextField.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    // this.statusLabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    // this.add(voidLabel);
    this.add(modeStatusLabel);
    this.add(viewStatusLabel);
    this.add(viewQualityLabel);
    this.add(controlStatusLabel);
  }
  
  /*
   * public void setStatusBarFont(Font font) {
   * this.modeStatusLabel.setFont(font); this.viewStatusLabel.setFont(font);
   * this.controlStatusLabel.setFont(font); }
   */
  
  public void setModeStatusText(String text)
  {
    this.modeStatusLabel.setText(text);
  }
  
  public void setViewStatusText(String text)
  {
    this.viewStatusLabel.setText(text);
  }
  
  public void setViewQualityText(String text)
  {
    this.viewQualityLabel.setText(text);
  }
  
  public void setControlStatusText(String text)
  {
    this.controlStatusLabel.setText(text);
  }
}
