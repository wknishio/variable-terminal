package org.vash.vate.client.graphicsmode;

// import java.awt.Component;
// import java.awt.Graphics;
import java.awt.ScrollPane;
// import java.awt.event.ComponentEvent;
// import java.awt.event.ComponentListener;

public class VTGraphicsModeClientWriterScrollPane extends ScrollPane
{
  private static final long serialVersionUID = 1L;
  
  /*
   * private class VTGraphicsModeClientWriterScrollPaneComponentListener
   * implements ComponentListener { public void componentHidden(ComponentEvent
   * e) { } public void componentMoved(ComponentEvent e) { } public void
   * componentResized(ComponentEvent e) {
   * //System.out.println("componentResized"); revalidate(); } public void
   * componentShown(ComponentEvent e) { revalidate(); } }
   */
  
  public VTGraphicsModeClientWriterScrollPane(int scrollbarsAsNeeded)
  {
    super(scrollbarsAsNeeded);
    this.setWheelScrollingEnabled(false);
    // this.getInsets().set(0, 0, 0, 0);
    // this.addComponentListener(new
    // VTGraphicsModeClientWriterScrollPaneComponentListener());
  }
  
  /*
   * public void invalidate() { }
   */
  
  /* public void revalidate() { super.invalidate(); super.validate(); } */
  
  /*
   * public void validate() { System.out.println("scrolled validate()");
   * super.validate(); }
   */
  
  /*
   * public void update(Graphics g) { paint(g); } public void paint(Graphics g)
   * { } public void paintAll(Graphics g) { }
   */
}