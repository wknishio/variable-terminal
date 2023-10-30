package org.vash.vate.client.graphicsmode.remote.listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedHashSet;
import java.util.Set;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;
import org.vash.vate.graphics.control.VTAWTControlEvent;

public class VTGraphicsModeClientRemoteInterfaceKeyListener implements KeyListener
{
  private Set<Integer> pressedKeys;
  private Set<Integer> suppressedKeys;
  private volatile boolean interrupted;
  private VTGraphicsModeClientWriter writer;
  private VTAWTControlEvent untyped;
  
  public VTGraphicsModeClientRemoteInterfaceKeyListener(VTGraphicsModeClientWriter writer)
  {
    this.writer = writer;
    this.untyped = new VTAWTControlEvent();
    this.pressedKeys = new LinkedHashSet<Integer>();
    this.suppressedKeys = new LinkedHashSet<Integer>();
  }
  
  public boolean suppressKey(int keycode)
  {
    return writer.localKeyRelease(keycode) && suppressedKeys.add(keycode);
  }
  
  public boolean unsuppressKey(int keycode)
  {
    return suppressedKeys.remove(keycode);
  }
  
  public void clearAllPressedKeys()
  {
    suppressedKeys.clear();
    pressedKeys.clear();
  }
  
  public void keyPressed(KeyEvent event)
  {
    // System.out.println(event.toString());
    /*
     * if (event.getKeyCode() == KeyEvent.VK_WINDOWS) {
     * System.out.println(event.toString()); }
     */
    event.consume();
    /*
     * if (event.getKeyCode() == KeyEvent.VK_UNDEFINED && event.getKeyChar() ==
     * '?') { event.setKeyCode(KeyEvent.VK_SLASH); }
     */
    boolean pressedControl = (event.getModifiersEx() & (KeyEvent.CTRL_DOWN_MASK)) != 0 || pressedKeys.contains(KeyEvent.VK_CONTROL) || event.getKeyCode() == KeyEvent.VK_CONTROL;
    boolean pressedShift = (event.getModifiersEx() & (KeyEvent.SHIFT_DOWN_MASK)) != 0 || pressedKeys.contains(KeyEvent.VK_SHIFT) || event.getKeyCode() == KeyEvent.VK_SHIFT;
    boolean pressedAlt = (event.getModifiersEx() & (KeyEvent.ALT_DOWN_MASK)) != 0 || pressedKeys.contains(KeyEvent.VK_ALT) || event.getKeyCode() == KeyEvent.VK_ALT;
    boolean pressedAltGraph = (event.getModifiersEx() & (KeyEvent.ALT_GRAPH_DOWN_MASK)) != 0 || pressedKeys.contains(KeyEvent.VK_ALT_GRAPH) || event.getKeyCode() == KeyEvent.VK_ALT_GRAPH;
    
    if (pressedControl && pressedShift && (pressedAlt || pressedAltGraph))
    {
      
    }
    else if (!interrupted)
    {
      // System.out.println("keyPressed: " + event.getKeyCode());
      /*
       * if (event.getKeyCode() == KeyEvent.VK_NUM_LOCK) {
       * writer.synchronizeRemoteLockingKey(KeyEvent.VK_NUM_LOCK); } else if
       * (event.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
       * writer.synchronizeRemoteLockingKey(KeyEvent.VK_CAPS_LOCK); } else if
       * (event.getKeyCode() == KeyEvent.VK_SCROLL_LOCK) {
       * writer.synchronizeRemoteLockingKey(KeyEvent.VK_SCROLL_LOCK); } else if
       * (event.getKeyCode() == KeyEvent.VK_KANA_LOCK) {
       * writer.synchronizeRemoteLockingKey(KeyEvent.VK_KANA_LOCK); }
       */
      untyped.id = event.getID();
      untyped.keyCode = event.getKeyCode();
      untyped.keyModifiers = event.getModifiersEx();
      untyped.keyLocation = event.getKeyLocation();
      untyped.keyChar = event.getKeyChar();
      // System.out.println(event);
      writer.writeEvent(untyped);
      /*
       * if (event.getKeyCode() == KeyEvent.VK_NUM_LOCK) {
       * writer.tryDelayedUpdateInAllRemoteLockingKeys(); } else if
       * (event.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
       * writer.tryDelayedUpdateInAllRemoteLockingKeys(); } else if
       * (event.getKeyCode() == KeyEvent.VK_SCROLL_LOCK) {
       * writer.tryDelayedUpdateInAllRemoteLockingKeys(); } else if
       * (event.getKeyCode() == KeyEvent.VK_KANA_LOCK) {
       * writer.tryDelayedUpdateInAllRemoteLockingKeys(); }
       */
    }
    /*
     * if ((event.getKeyCode() == KeyEvent.VK_CONTROL || event.getKeyCode() ==
     * KeyEvent.VK_ALT || event.getKeyCode() == KeyEvent.VK_ALT_GRAPH ||
     * event.getKeyCode() == KeyEvent.VK_SHIFT || event.getKeyCode() ==
     * KeyEvent.VK_META || event.getKeyCode() == KeyEvent.VK_WINDOWS ||
     * event.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) ) { if
     * (event.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) { return; } //Try
     * preventing some local key combinations
     * //ignoreReleaseKeys.add(event.getKeyCode()); //if
     * (writer.localKeyRelease(event.getKeyCode())) //{
     * //pressedKeys.add(event.getKeyCode()); //} if
     * (writer.suppressKey(event.getKeyCode())) {
     * pressedKeys.add(event.getKeyCode()); } }
     */
    if (!interrupted && writer.isSuppressLocalKeyCombinations())
    {
      // if (event.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT)
      // {
      // return;
      // }
      /*
       * if (event.getKeyCode() == KeyEvent.VK_CONTROL || event.getKeyCode() ==
       * KeyEvent.VK_ALT || event.getKeyCode() == KeyEvent.VK_ALT_GRAPH ||
       * event.getKeyCode() == KeyEvent.VK_SHIFT || event.getKeyCode() ==
       * KeyEvent.VK_META || event.getKeyCode() == KeyEvent.VK_WINDOWS ||
       * event.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) { return; }
       */
      if (suppressKey(event.getKeyCode()))
      {
        pressedKeys.add(event.getKeyCode());
      }
    }
  }
  
  public void keyReleased(KeyEvent event)
  {
    // System.out.println(event.toString());
    /*
     * if (event.getKeyCode() == KeyEvent.VK_WINDOWS) {
     * System.out.println(event.toString()); }
     */
    event.consume();
    if (!interrupted && writer.isSuppressLocalKeyCombinations() && unsuppressKey(event.getKeyCode()))
    {
      return;
    }
    
    if (writer.isIgnoreLocalKeyCombinations())
    {
      if (!interrupted)
      {
        // System.out.println("keyReleased: " + event.getKeyCode());
        /*
         * if (event.getKeyCode() == KeyEvent.VK_NUM_LOCK) {
         * writer.synchronizeRemoteLockingKey(KeyEvent.VK_NUM_LOCK); } else if
         * (event.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
         * writer.synchronizeRemoteLockingKey(KeyEvent.VK_CAPS_LOCK); } else if
         * (event.getKeyCode() == KeyEvent.VK_SCROLL_LOCK) {
         * writer.synchronizeRemoteLockingKey(KeyEvent.VK_SCROLL_LOCK); } else
         * if (event.getKeyCode() == KeyEvent.VK_KANA_LOCK) {
         * writer.synchronizeRemoteLockingKey(KeyEvent.VK_KANA_LOCK); }
         */
        untyped.id = event.getID();
        untyped.keyCode = event.getKeyCode();
        untyped.keyModifiers = event.getModifiersEx();
        untyped.keyLocation = event.getKeyLocation();
        untyped.keyChar = event.getKeyChar();
        // System.out.println(event);
        writer.writeEvent(untyped);
        /*
         * if (event.getKeyCode() == KeyEvent.VK_NUM_LOCK) {
         * writer.tryDelayedUpdateInAllRemoteLockingKeys(); } else if
         * (event.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
         * writer.tryDelayedUpdateInAllRemoteLockingKeys(); } else if
         * (event.getKeyCode() == KeyEvent.VK_SCROLL_LOCK) {
         * writer.tryDelayedUpdateInAllRemoteLockingKeys(); } else if
         * (event.getKeyCode() == KeyEvent.VK_KANA_LOCK) {
         * writer.tryDelayedUpdateInAllRemoteLockingKeys(); }
         */
      }
      if (!interrupted && writer.isSuppressLocalKeyCombinations())
      {
        pressedKeys.remove(event.getKeyCode());
      }
      return;
    }
    
    boolean releasedControl = event.getKeyCode() == KeyEvent.VK_CONTROL;
    boolean releasedShift = event.getKeyCode() == KeyEvent.VK_SHIFT;
    boolean releasedAlt = event.getKeyCode() == KeyEvent.VK_ALT;
    boolean releasedAltGraph = event.getKeyCode() == KeyEvent.VK_ALT_GRAPH;
    boolean pressedControl = (event.getModifiersEx() & (KeyEvent.CTRL_DOWN_MASK)) != 0 || pressedKeys.contains(KeyEvent.VK_CONTROL) && !(releasedControl);
    boolean pressedShift = (event.getModifiersEx() & (KeyEvent.SHIFT_DOWN_MASK)) != 0 || pressedKeys.contains(KeyEvent.VK_SHIFT) && !(releasedShift);
    boolean pressedAlt = (event.getModifiersEx() & (KeyEvent.ALT_DOWN_MASK)) != 0 || pressedKeys.contains(KeyEvent.VK_ALT) && !(releasedAlt);
    boolean pressedAltGraph = (event.getModifiersEx() & (KeyEvent.ALT_GRAPH_DOWN_MASK)) != 0 || pressedKeys.contains(KeyEvent.VK_ALT_GRAPH) && !(releasedAltGraph);
    
    /*
     * else { }
     */
    // System.out.println("released!");
    /*
     * System.out.println("pre-pressed keys: " +
     * Arrays.toString(pressedKeys.toArray())); System.out.println("keycode: " +
     * event.getKeyCode()); //System.out.println("pressed keys: " +
     * Arrays.toString(pressedKeys.toArray())); if
     * (pressedKeys.remove(event.getKeyCode())) { return; }
     * System.out.println("post-pressed keys: " +
     * Arrays.toString(pressedKeys.toArray()));
     */
    // System.out.println("released!");
    /*
     * else { pressedKeys.remove(event.getKeyCode());
     * //pressedKeys.remove(event.getKeyCode()); }
     */
    // pressedKeys.add(event.getKeyCode());
    if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_ENTER))
    {
      writer.toggleFullScreenMode();
    }
    else if (pressedControl && pressedShift && event.getKeyCode() == KeyEvent.VK_BACK_SPACE)
    {
      writer.toggleMenubar();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_PAGE_DOWN))
    {
      writer.decreaseCaptureScale();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_PAGE_UP))
    {
      writer.increaseCaptureScale();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_INSERT))
    {
      writer.resetCaptureScale();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_END))
    {
      writer.nextDevice();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_HOME))
    {
      writer.previousDevice();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_1))
    {
      if (!writer.isRefreshInterrupted())
      {
        writer.interruptRefresh();
      }
      else
      {
        writer.updateRefreshMode(writer.isSynchronousRefresh());
      }
      
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_2))
    {
      writer.clearRemoteGraphics();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_3))
    {
      writer.increaseCaptureInterval();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_4))
    {
      writer.decreaseCaptureInterval();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_5))
    {
      writer.decreaseColorQuality();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_6))
    {
      writer.increaseColorQuality();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_7))
    {
      writer.decreaseDrawPointerSize();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_8))
    {
      writer.increaseDrawPointerSize();
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_9))
    {
      if (writer.isClipboardControlEnabled())
      {
        writer.receiveClipboardContents();
      }
    }
    else if (pressedControl && pressedShift && (event.getKeyCode() == KeyEvent.VK_0))
    {
      if (writer.isClipboardControlEnabled())
      {
        writer.sendClipboardContents();
      }
    }
    // else if (pressedKeys.contains(KeyEvent.VK_CONTROL)
    // && pressedKeys.contains(KeyEvent.VK_SHIFT)
    // && (event.getKeyCode() == KeyEvent.VK_3
    // || event.getKeyChar() == '3'))
    // {
    // writer.unifiedDevice();
    // }
    // else if (((event.getModifiersEx() & (KeyEvent.CTRL_DOWN_MASK |
    // KeyEvent.SHIFT_DOWN_MASK)) == (KeyEvent.CTRL_DOWN_MASK |
    // KeyEvent.SHIFT_DOWN_MASK)) && (event.getKeyCode() == KeyEvent.VK_3 ||
    // event.getKeyChar() == '3'))
    // {
    // writer.unifiedDevice();
    // }
    else if (pressedControl && pressedShift && event.getKeyCode() == KeyEvent.VK_SPACE)
    {
      writer.toggleHideScrollBars();
      
    }
    else if (pressedControl && pressedShift && releasedAlt)
    {
      toggleControl(false);
    }
    else if (pressedControl && pressedAlt && releasedShift)
    {
      toggleControl(false);
    }
    else if (pressedShift && pressedAlt && releasedControl)
    {
      toggleControl(false);
    }
    else if (pressedControl && pressedShift && releasedAltGraph)
    {
      toggleControl(true);
    }
    else if (pressedControl && pressedAltGraph && releasedShift)
    {
      toggleControl(true);
    }
    else if (pressedShift && pressedAltGraph && releasedControl)
    {
      toggleControl(true);
    }
    else if (!interrupted)
    {
      untyped.id = event.getID();
      untyped.keyCode = event.getKeyCode();
      untyped.keyModifiers = event.getModifiersEx();
      untyped.keyLocation = event.getKeyLocation();
      untyped.keyChar = event.getKeyChar();
      writer.writeEvent(untyped);
    }
    if (!interrupted && writer.isSuppressLocalKeyCombinations())
    {
      pressedKeys.remove(event.getKeyCode());
    }
  }
  
  public void keyTyped(KeyEvent event)
  {
    // System.out.println(event.toString());
    /*
     * if (event.getKeyCode() == KeyEvent.VK_WINDOWS) {
     * System.out.println(event.toString()); }
     */
    event.consume();
    // System.out.println(event.toString());
  }
  
  public void setInterrupted(boolean interrupted)
  {
    this.interrupted = interrupted;
  }
  
  private void toggleControl(boolean altGraph)
  {
    if (!interrupted)
    {
      untyped.id = KeyEvent.KEY_RELEASED;
      untyped.keyCode = KeyEvent.VK_CONTROL;
      writer.writeEvent(untyped);
      
      untyped.keyCode = KeyEvent.VK_SHIFT;
      writer.writeEvent(untyped);
      
      if (altGraph)
      {
        untyped.keyCode = KeyEvent.VK_ALT_GRAPH;
        writer.writeEvent(untyped);
      }
      else
      {
        untyped.keyCode = KeyEvent.VK_ALT;
        writer.writeEvent(untyped);
      }
      
      writer.releaseAllPressedKeys();
    }
    writer.toggleControl();
  }
}