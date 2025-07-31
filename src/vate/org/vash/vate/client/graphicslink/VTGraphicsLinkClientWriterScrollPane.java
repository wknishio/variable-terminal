package org.vash.vate.client.graphicslink;

// import java.awt.Component;
// import java.awt.Graphics;
import java.awt.ScrollPane;
// import java.awt.event.ComponentEvent;
// import java.awt.event.ComponentListener;

public class VTGraphicsLinkClientWriterScrollPane extends ScrollPane
{
  private static final long serialVersionUID = 1L;
  
  /*
   * private class VTGraphicsLinkClientWriterScrollPaneComponentListener
   * implements ComponentListener { public void componentHidden(ComponentEvent
   * e) { } public void componentMoved(ComponentEvent e) { } public void
   * componentResized(ComponentEvent e) {
   * //System.out.println("componentResized"); revalidate(); } public void
   * componentShown(ComponentEvent e) { revalidate(); } }
   */
  
  public VTGraphicsLinkClientWriterScrollPane(int scrollbarsAsNeeded)
  {
    super(scrollbarsAsNeeded);
    this.setWheelScrollingEnabled(true);
    // this.getInsets().set(0, 0, 0, 0);
    // this.addComponentListener(new
    // VTGraphicsLinkClientWriterScrollPaneComponentListener());
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