package org.vash.vate.console.graphical;

import java.awt.Dimension;
import java.awt.ScrollPane;
import java.awt.TextArea;

public class VTGraphicalConsoleTextAreaScrollPane extends ScrollPane
{
  private static final long serialVersionUID = 1L;
  private TextArea textArea;
  
  public VTGraphicalConsoleTextAreaScrollPane(TextArea textArea)
  {
    super(ScrollPane.SCROLLBARS_ALWAYS);
    this.textArea = textArea;
    this.add(textArea);
    textArea.setSize(textArea.getMaximumSize());
    this.setSize(textArea.getPreferredSize());
    // textArea.setSize(textArea.getPreferredSize(83, 200));
    // this.setPreferredSize(textArea.getPreferredSize(83, 200));
    // this.setMinimumSize(textArea.getPreferredSize(25, 80));
  }
  
  // public Dimension getPreferredSize()
  // {
  // Dimension area = textArea.getPreferredSize();
  // area.width = area.width + this.getVScrollbarWidth() +
  // this.getHScrollbarHeight();
  // area.height = area.height + this.getHScrollbarHeight() +
  // this.getVScrollbarWidth();
  // return area;
  // }
  
  public Dimension getViewportSize()
  {
    return textArea.getSize();
  }
}
