package org.vash.vate.console.lanterna.separated;

import java.awt.Adjustable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.ScrollPane;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.peer.ScrollPanePeer;

public class VTScrollPane extends ScrollPane
{
  private static final long serialVersionUID = 1L;
  private VTScrollPaneAdjustable verticalAdjustable;
  private VTScrollPaneAdjustable horizontalAdjustable;
  private int vtscrollbarDisplayPolicy = 0;
  
  public VTScrollPane() throws HeadlessException 
  {
    this(SCROLLBARS_AS_NEEDED);
  }
  
  public VTScrollPane(int scrollbarDisplayPolicy) throws HeadlessException 
  {
    super(scrollbarDisplayPolicy);
    vtscrollbarDisplayPolicy = scrollbarDisplayPolicy;
    
    verticalAdjustable = new VTScrollPaneAdjustable(this, new VTPeerFixer(this),
                                           Adjustable.VERTICAL);
    horizontalAdjustable = new VTScrollPaneAdjustable(this, new VTPeerFixer(this),
                                           Adjustable.HORIZONTAL);
    setWheelScrollingEnabled(false);
  }
  
  public Adjustable getVAdjustable() {
    return verticalAdjustable;
  }
  
  public Adjustable getHAdjustable() {
    return horizontalAdjustable;
  }
  
  @SuppressWarnings("deprecation")
  public void layout() {
    if (getComponentCount()==0) {
      return;
  }
  Component c = getComponent(0);
  Point p = getScrollPosition();
  Dimension cs = vtcalculateChildSize();
  Dimension vs = getViewportSize();

  c.reshape(- p.x, - p.y, cs.width, cs.height);
  ScrollPanePeer peer = (ScrollPanePeer)this.getPeer();
  if (peer != null) {
      peer.childResized(cs.width, cs.height);
  }

  // update adjustables... the viewport size may have changed
  // with the scrollbars coming or going so the viewport size
  // is updated before the adjustables.
  vs = getViewportSize();
  horizontalAdjustable.setSpan(0, cs.width, vs.width);
  verticalAdjustable.setSpan(0, cs.height, vs.height);
    
  }
  
  private Dimension vtcalculateChildSize() {
    //
    // calculate the view size, accounting for border but not scrollbars
    // - don't use right/bottom insets since they vary depending
    //   on whether or not scrollbars were displayed on last resize
    //
    Dimension       size = getSize();
    Insets          insets = getInsets();
    int             viewWidth = size.width - insets.left*2;
    int             viewHeight = size.height - insets.top*2;

    //
    // determine whether or not horz or vert scrollbars will be displayed
    //
    boolean vbarOn;
    boolean hbarOn;
    Component child = getComponent(0);
    Dimension childSize = new Dimension(child.getPreferredSize());

    if (vtscrollbarDisplayPolicy == SCROLLBARS_AS_NEEDED) {
        vbarOn = childSize.height > viewHeight;
        hbarOn = childSize.width  > viewWidth;
    } else if (vtscrollbarDisplayPolicy == SCROLLBARS_ALWAYS) {
        vbarOn = hbarOn = true;
    } else { // SCROLLBARS_NEVER
        vbarOn = hbarOn = false;
    }

    //
    // adjust predicted view size to account for scrollbars
    //
    int vbarWidth = getVScrollbarWidth();
    int hbarHeight = getHScrollbarHeight();
    if (vbarOn) {
        viewWidth -= vbarWidth;
    }
    if(hbarOn) {
        viewHeight -= hbarHeight;
    }

    //
    // if child is smaller than view, size it up
    //
    if (childSize.width < viewWidth) {
        childSize.width = viewWidth;
    }
    if (childSize.height < viewHeight) {
        childSize.height = viewHeight;
    }

    return childSize;
  }
  
  public void setScrollPosition(int x, int y) {
    synchronized (getTreeLock()) {
        if (getComponentCount()==0) {
            throw new NullPointerException("child is null");
        }
        horizontalAdjustable.setValue(x);
        verticalAdjustable.setValue(y);
    }
  }
  
  public Point getScrollPosition() {
    synchronized (getTreeLock()) {
        if (getComponentCount()==0) {
            throw new NullPointerException("child is null");
        }
        return new Point(horizontalAdjustable.getValue(), verticalAdjustable.getValue());
    }
  }
  
  private class VTPeerFixer implements AdjustmentListener, java.io.Serializable
  {
      private static final long serialVersionUID = 1043664721353696630L;

      private VTPeerFixer(ScrollPane scroller) {
          this.scroller = scroller;
      }

      /**
       * Invoked when the value of the adjustable has changed.
       */
      @SuppressWarnings("deprecation")
      public void adjustmentValueChanged(AdjustmentEvent e) {
          Adjustable adj = e.getAdjustable();
          int value = e.getValue();
          ScrollPanePeer peer = (ScrollPanePeer) scroller.getPeer();
          if (peer != null) {
              peer.setValue(adj, value);
          }

          Component c = scroller.getComponent(0);
          switch(adj.getOrientation()) {
          case Adjustable.VERTICAL:
              c.move(c.getLocation().x, -(value));
              break;
          case Adjustable.HORIZONTAL:
              c.move(-(value), c.getLocation().y);
              break;
          default:
              throw new IllegalArgumentException("Illegal adjustable orientation");
          }
      }

      private ScrollPane scroller;
  }

}
