package org.vash.vate.client.graphicsmode;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.vash.vate.VT;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.graphicsmode.listener.VTGraphicsModeClientWindowListener;
import org.vash.vate.client.graphicsmode.options.VTGraphicsModeClientOptionsMenuBar;
import org.vash.vate.client.graphicsmode.remote.VTGraphicsModeClientRemoteInterface;
import org.vash.vate.client.graphicsmode.remote.VTGraphicsModeClientRemoteInterfaceRefresher;
import org.vash.vate.client.graphicsmode.remote.listener.VTGraphicsModeClientRemoteInterfaceFocusListener;
import org.vash.vate.client.graphicsmode.remote.listener.VTGraphicsModeClientRemoteInterfaceKeyListener;
import org.vash.vate.client.graphicsmode.remote.listener.VTGraphicsModeClientRemoteInterfaceMouseListener;
import org.vash.vate.client.graphicsmode.remote.listener.VTGraphicsModeClientRemoteInterfaceMouseMoveListener;
import org.vash.vate.client.graphicsmode.remote.listener.VTGraphicsModeClientRemoteInterfaceMouseWheelListener;
import org.vash.vate.console.VTConsole;
import org.vash.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vash.vate.graphics.clipboard.VTEmptyTransferable;
import org.vash.vate.graphics.control.VTAWTControlEvent;
import org.vash.vate.graphics.control.VTAWTControlProvider;
import org.vash.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vash.vate.graphics.font.VTGlobalTextStyleManager;

public class VTGraphicsModeClientWriter implements Runnable
{
  private static final double IMAGE_SCALE_MULTIPLIER_FACTOR = Math.pow(2D, (1D / 8D));
  public static final int TERMINAL_STATE_FOCUSED = 0;
  public static final int TERMINAL_STATE_VISIBLE = 1;
  public static final int TERMINAL_STATE_IGNORE = 2;
  private volatile boolean stopped;
  private volatile boolean needRefresh;
  private volatile boolean hasRefresh;
  private volatile boolean hasDifference;
  private volatile boolean open;
  private volatile boolean readOnly;
  private volatile boolean controlInterrupted;
  private volatile boolean refreshInterrupted;
  private volatile boolean synchronousRefresh;
  //private volatile boolean dynamicCoding;
  //private volatile boolean separatedCoding;
  private volatile boolean drawPointer;
  private volatile boolean suppressLocalKeyCombinations;
  private volatile boolean ignoreLocalKeyCombinations;
  private volatile int screenCaptureInterval;
  private volatile int colorQuality;
  private volatile int terminalRefreshPolicy;
  private volatile int terminalControlPolicy;
  private volatile int initialWidth;
  private volatile int initialHeight;
  private volatile int screenCaptureMode;
  private volatile int imageCoding;
  private volatile Rectangle captureArea;
  private volatile double captureScale;
  private Toolkit toolkit;
  private BufferedImage imageDataBuffer;
  private Cursor cursor;
  private Clipboard systemClipboard;
  private VTClientConnection connection;
  private VTGraphicsModeClientSession session;
  private VTGraphicsModeClientReader reader;
  private VTGraphicsModeClientWriterFrame frame;
  private VTGraphicsModeClientOptionsMenuBar menuBar;
  private VTGraphicsModeClientWriterScrollPane scrolled;
  // private VTGraphicsModeClientWriterScrollPane scrolledWithBars;
  // private VTGraphicsModeClientWriterScrollPane scrolledWithoutBars;
  private VTGraphicsModeClientWindowListener windowListener;
  private VTGraphicsModeClientRemoteInterface remoteInterface;
  private VTGraphicsModeClientRemoteInterfaceFocusListener focusListener;
  private VTGraphicsModeClientRemoteInterfaceKeyListener keyListener;
  private VTGraphicsModeClientRemoteInterfaceMouseListener mouseListener;
  private VTGraphicsModeClientRemoteInterfaceMouseMoveListener mouseMotionListener;
  private VTGraphicsModeClientRemoteInterfaceMouseWheelListener mouseWheelListener;
  // private VTGraphicsModeClientRemoteInterfaceScrollPaneListener
  // scrollPaneListener;
  // private VTGraphicsModeClientRemoteInterfaceLockingKeySynchronizer
  // lockingKeySynchronizer;
  // private VTGraphicsModeClientRemoteInterfaceCaptureAreaUpdater
  // captureAreaUpdater;
  private VTGraphicsModeClientRemoteInterfaceRefresher graphicsRefresher;
  private VTAWTControlProvider selfControlProvider;
  private int lastFrameState;
  private Rectangle lastFrameBounds;
  private Runnable fullscreenToggler;
  private Runnable frameSizeAdjuster;
  private Runnable menubarToggler;
  // private Runnable requestFocus;
  // private int frameInsetsTop;
  // private int frameInsetsBottom;
  // private int frameInsetsLeft;
  // private int frameInsetsRight;
  // private int scrolledInsetsTop;
  // private int scrolledInsetsBottom;
  // private int scrolledInsetsLeft;
  // private int scrolledInsetsRight;
  // private class VTGraphicaModeClientRequestFocus implements Runnable
  // {
  // public void run()
  // {
  // remoteInterface.requestFocus();
  // }
  // }
  
  private class VTGraphicsModeClientMenubarToggler implements Runnable
  {
    public void run()
    {
      try
      {
        if (menuBar.enabled())
        {
          menuBar.disable();
        }
        else
        {
          menuBar.enable();
        }
        frame.revalidate();
      }
      finally
      {
        
      }
    }
  }
  
  private class VTGraphicsModeClientFullScreenToggler implements Runnable
  {
    private GraphicsDevice device;
    
    public void run()
    {
      try
      {
        if (frame.isUndecorated())
        {
          try
          {
            if (device != null && frame.equals(device.getFullScreenWindow()))
            {
              device.setFullScreenWindow(null);
            }
          }
          catch (Throwable t)
          {
            
          }
          frame.setVisible(false);
          frame.dispose();
          // frame.setMenuBar(menuBar);
          frame.setUndecorated(false);
          frame.setResizable(true);
          frame.setBounds(lastFrameBounds);
          frame.setAlwaysOnTop(false);
          frame.setVisible(true);
          frame.setExtendedState(lastFrameState);
          frame.toFront();
          // frame.requestFocus();
          // remoteInterface.requestFocus();
          // EventQueue.invokeLater(requestFocus);
        }
        else
        {
          lastFrameState = frame.getExtendedState();
          if ((lastFrameState & Frame.ICONIFIED) != 0)
          {
            frame.setExtendedState(Frame.NORMAL);
          }
          lastFrameBounds = frame.getBounds();
          frame.setVisible(false);
          frame.dispose();
          // frame.setMenuBar(null);
          frame.setUndecorated(true);
          frame.setResizable(false);
          frame.setAlwaysOnTop(true);
          frame.setVisible(true);
          frame.setExtendedState(Frame.MAXIMIZED_BOTH);
          
          device = VTGraphicalDeviceResolver.getCurrentDevice(frame);
          if (device != null)
          {
            try
            {
              // remoteInterface.requestFocus();
              device.setFullScreenWindow(frame);
              // frame.toFront();
              // remoteInterface.requestFocus();
              // remoteInterface.requestFocus();
              // frame.setVisible(true);
              // frame.setExtendedState(Frame.MAXIMIZED_BOTH);
              // frame.toFront();
              // remoteInterface.requestFocus();
            }
            catch (Throwable t)
            {
              device = null;
            }
          }
          else
          {
            
            // remoteInterface.requestFocus();
          }
          frame.toFront();
        }
      }
      finally
      {
        
      }
    }
  }
  
  class VTGraphicsModeClientFrameSizeAdjuster implements Runnable
  {
    public void run()
    {
      try
      {
        if (!frame.isResizable())
        {
          return;
        }
        if (frame.getExtendedState() == Frame.ICONIFIED)
        {
          return;
        }
        
        Insets frameInsets = frame.getInsets();
        Insets scrolledInsets = scrolled.getInsets();
        Dimension interfaceSize = remoteInterface.getSize();
        int maxWidth = frame.getGraphicsConfiguration().getDevice().getDisplayMode().getWidth();
        int maxHeight = frame.getGraphicsConfiguration().getDevice().getDisplayMode().getHeight();
        if (interfaceSize.width + frameInsets.left + frameInsets.right + scrolledInsets.left + scrolledInsets.left >= maxWidth || interfaceSize.height + frameInsets.top + frameInsets.bottom + scrolledInsets.top + scrolledInsets.top >= maxHeight)
        {
          if (frame.getExtendedState() == Frame.MAXIMIZED_BOTH)
          {
            frame.setExtendedState(Frame.NORMAL);
            frame.revalidate();
          }
          else
          {
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            frame.revalidate();
          }
        }
        else
        {
          frame.setExtendedState(Frame.NORMAL);
          frame.setSize(interfaceSize.width + frameInsets.left + frameInsets.right + scrolledInsets.left + scrolledInsets.left, interfaceSize.height + frameInsets.top + frameInsets.bottom + scrolledInsets.top + scrolledInsets.top);
          frame.revalidate();
        }
        // scrolled.setPreferredSize(null);
      }
      finally
      {
        
      }
    }
  }
  
  public VTGraphicsModeClientWriter(VTGraphicsModeClientSession session)
  {
    //this.cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    this.stopped = true;
    this.toolkit = Toolkit.getDefaultToolkit();
    this.session = session;
    this.connection = session.getSession().getConnection();
    this.windowListener = new VTGraphicsModeClientWindowListener(this);
    this.focusListener = new VTGraphicsModeClientRemoteInterfaceFocusListener(this);
    this.keyListener = new VTGraphicsModeClientRemoteInterfaceKeyListener(this);
    this.mouseListener = new VTGraphicsModeClientRemoteInterfaceMouseListener(this);
    this.mouseMotionListener = new VTGraphicsModeClientRemoteInterfaceMouseMoveListener(this);
    this.mouseWheelListener = new VTGraphicsModeClientRemoteInterfaceMouseWheelListener(this);
    // this.lockingKeySynchronizer = new
    // VTGraphicsModeClientRemoteInterfaceLockingKeySynchronizer(this);
    // this.scrollPaneListener = new
    // VTGraphicsModeClientRemoteInterfaceScrollPaneListener(this);
    // this.captureAreaUpdater = new
    // VTGraphicsModeClientRemoteInterfaceCaptureAreaUpdater(this);
    this.graphicsRefresher = new VTGraphicsModeClientRemoteInterfaceRefresher(this);
    this.colorQuality = VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216;
    this.screenCaptureMode = VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_VIEWPORT;
    this.imageCoding = VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF;
    this.terminalRefreshPolicy = TERMINAL_STATE_VISIBLE;
    this.terminalControlPolicy = TERMINAL_STATE_FOCUSED;
    this.synchronousRefresh = false;
    //this.dynamicCoding = false;
    //this.separatedCoding = true;
    // this.remoteInterfaceLoaded = false;
    // this.interruptedRefresh = false;
    this.drawPointer = true;
    // this.screenCaptureModeComplete = false;
    this.suppressLocalKeyCombinations = false;
    this.ignoreLocalKeyCombinations = false;
    this.refreshInterrupted = false;
    // this.synchronizingLockingKey = false;
    this.screenCaptureInterval = 250;
    this.captureScale = 1;
    this.fullscreenToggler = new VTGraphicsModeClientFullScreenToggler();
    this.frameSizeAdjuster = new VTGraphicsModeClientFrameSizeAdjuster();
    this.menubarToggler = new VTGraphicsModeClientMenubarToggler();
    // this.requestFocus = new VTGraphicaModeClientRequestFocus();
    try
    {
      systemClipboard = toolkit.getSystemClipboard();
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public void setInitialScreenSize(int width, int height)
  {
    this.initialWidth = width;
    this.initialHeight = height;
  }
  
  public void remoteKeyPress(int keycode, int keymodifiers, int keylocation, char keychar)
  {
    try
    {
      remoteKeyDown(keycode, keymodifiers, keylocation, keychar);
      remoteKeyUp(keycode, keymodifiers, keylocation, keychar);
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public void remoteKeyDown(int keycode, int keymodifiers, int keylocation, char keychar)
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_KEYBOARD_INPUT_KEY_DOWN);
      connection.getGraphicsControlDataOutputStream().writeInt(keycode);
      connection.getGraphicsControlDataOutputStream().writeInt(keymodifiers);
      connection.getGraphicsControlDataOutputStream().writeInt(keylocation);
      connection.getGraphicsControlDataOutputStream().writeChar(keychar);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public void remoteKeyUp(int keycode, int keymodifiers, int keylocation, char keychar)
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_KEYBOARD_INPUT_KEY_UP);
      connection.getGraphicsControlDataOutputStream().writeInt(keycode);
      connection.getGraphicsControlDataOutputStream().writeInt(keymodifiers);
      connection.getGraphicsControlDataOutputStream().writeInt(keylocation);
      connection.getGraphicsControlDataOutputStream().writeChar(keychar);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public boolean localKeyRelease(int keycode)
  {
    try
    {
      selfControlProvider.keyRelease(keycode, 0, 0, ' ');
      return true;
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  public void clearLocalClipboardContents()
  {
    if (systemClipboard != null)
    {
      try
      {
        systemClipboard.setContents(new VTEmptyTransferable(), null);
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        return;
      }
    }
  }
  
  public void clearRemoteClipboardContents()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_CLIPBOARD_CLEAR_REQUEST);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable e)
    {
      stopped = true;
      return;
    }
  }
  
  public void sendClipboardContents()
  {
    try
    {
      menuBar.sendLocalClipboardContents();
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_SEND_REQUEST);
      connection.getGraphicsControlDataOutputStream().flush();
      session.getSession().getClipboardTransferTask().joinThread();
      session.getSession().getClipboardTransferTask().setSending(true);
      session.getSession().getClipboardTransferTask().startThread();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      return;
    }
  }
  
  public void receiveClipboardContents()
  {
    try
    {
      menuBar.receiveRemoteClipboardContents();
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_RECEIVE_REQUEST);
      connection.getGraphicsControlDataOutputStream().flush();
      session.getSession().getClipboardTransferTask().joinThread();
      session.getSession().getClipboardTransferTask().setSending(false);
      session.getSession().getClipboardTransferTask().startThread();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      return;
    }
  }
  
  public void cancelClipboardTransfer()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_CANCEL_REQUEST);
      connection.getGraphicsControlDataOutputStream().flush();
      /*
       * if (session.getSession().getClipboardTransferTask().isSending()) {
       * connection.getGraphicsClipboardOutputStream().close(); }
       */
      connection.getGraphicsClipboardOutputStream().close();
      session.getSession().getClipboardTransferTask().interruptThread();
      // session.getSession().getClipboardTransferThread().stop();
      session.getSession().getClipboardTransferTask().joinThread();
      session.getSession().getConnection().resetClipboardStreams();
      session.getSession().getClipboardTransferTask().setInputStream(connection.getGraphicsClipboardDataInputStream());
      session.getSession().getClipboardTransferTask().setOutputStream(connection.getGraphicsClipboardDataOutputStream());
      /*
       * connection.getGraphicsClipboardDataOutputStream().write(0);
       * connection.getGraphicsClipboardDataOutputStream().flush();
       * connection.getGraphicsClipboardDataInputStream().read();
       */
      // finishClipboardContentsTransfer();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      return;
    }
  }
  
  public void finishClipboardContentsTransfer()
  {
    menuBar.finishClipboardContentsTransfer();
    // System.out.println("Clipboard Transfer Finished");
    /*
     * try { connection.resetClipboardStreams(); } catch (Throwable e) { }
     * session.getSession().getClipboardTransferTask().setInputStream(
     * connection.getGraphicsClipboardDataInputStream());
     * session.getSession().getClipboardTransferTask().setOutputStream(
     * connection.getGraphicsClipboardDataOutputStream());
     */
  }
  
  public void dispose()
  {
    captureScale = 1;
    initialWidth = 0;
    initialHeight = 0;
    stopped = true;
    needRefresh = false;
    hasRefresh = false;
    hasDifference = false;
    
    // ignoreFocus = false;
    // ignoreIconification = false;
    
    terminalRefreshPolicy = TERMINAL_STATE_VISIBLE;
    terminalControlPolicy = TERMINAL_STATE_FOCUSED;
    open = false;
    colorQuality = VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216;
    synchronousRefresh = false;
    //dynamicCoding = false;
    //separatedCoding = true;
    // remoteInterfaceLoaded = false;
    // interruptedRefresh = false;
    drawPointer = true;
    screenCaptureMode = VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_VIEWPORT;
    imageCoding = VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF;
    suppressLocalKeyCombinations = false;
    // scaledCapture = false;
    refreshInterrupted = false;
    screenCaptureInterval = 250;
    captureScale = 1;
    imageDataBuffer = null;
    captureArea = null;
    // releaseAllPressedKeys();
    if (remoteInterface != null)
    {
      if (remoteInterface.isAsynchronousRepainterRunning())
      {
        remoteInterface.stopAsynchronousRepainter();
      }
      remoteInterface.dispose();
    }
    if (frame != null)
    {
      frame.setVisible(false);
      frame.dispose();
    }
    if (selfControlProvider != null)
    {
      selfControlProvider.dispose();
    }
    keyListener.clearAllPressedKeys();
  }
  
  protected void finalize()
  {
    if (remoteInterface != null && remoteInterface.isAsynchronousRepainterRunning())
    {
      remoteInterface.stopAsynchronousRepainter();
    }
    if (frame != null)
    {
      frame.setVisible(false);
      frame.dispose();
    }
  }
  
  public void notifyAsynchronousRepainter()
  {
    if (remoteInterface.isAsynchronousRepainterRunning())
    {
      if (remoteInterface.isAsynchronousRepainterInterrupted())
      {
        remoteInterface.resumeAsynchronousRepainter();
      }
    }
    else
    {
      remoteInterface.startAsynchronousRepainter();
    }
    // remoteInterface.startAsynchronousRepainter();
  }
  
  public boolean isStopped()
  {
    return stopped;
  }
  
  public void setStopped(boolean stopped)
  {
    this.stopped = stopped;
    if (stopped)
    {
      try
      {
        connection.closeGraphicsModeStreams();
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
      }
      synchronized (this)
      {
        notify();
      }
    }
  }
  
  public boolean isReadOnly()
  {
    return readOnly;
  }
  
  public void setReadOnly(boolean readOnly)
  {
    this.readOnly = readOnly;
    if (this.readOnly)
    {
      controlInterrupted = true;
      interruptControl();
    }
    else
    {
      controlInterrupted = false;
      restablishControl();
    }
  }
  
  public boolean isControlInterrupted()
  {
    return controlInterrupted;
  }
  
  public boolean isRefreshInterrupted()
  {
    return refreshInterrupted;
  }
  
  public int getColorQuality()
  {
    return colorQuality;
  }
  
  public void setColorQuality(int colorQuality)
  {
    this.colorQuality = colorQuality;
  }
  
  public boolean isSynchronousRefresh()
  {
    return synchronousRefresh;
  }
  
  public void setSynchronousRefresh(boolean synchronousRefresh)
  {
    this.synchronousRefresh = synchronousRefresh;
    this.remoteInterface.setSynchronousRefresh(synchronousRefresh);
  }
  
//  public void setDynamicCoding(boolean dynamicCoding)
//  {
//    this.dynamicCoding = dynamicCoding;
//  }
  
//  public void setSeparatedCoding(boolean separatedCoding)
//  {
//    this.separatedCoding = separatedCoding;
//  }
  
  public boolean isDrawPointer()
  {
    return drawPointer;
  }
  
  public void setDrawPointer(boolean drawPointer)
  {
    this.drawPointer = drawPointer;
  }
  
  public int getScreenCaptureInterval()
  {
    return screenCaptureInterval;
  }
  
  public void setScreenCaptureInterval(int screenCaptureInterval)
  {
    this.screenCaptureInterval = screenCaptureInterval;
  }
  
  /*
   * public boolean isScreenCaptureModeComplete() { return
   * screenCaptureModeComplete; }
   */
  
  /*
   * public void setScreenCaptureModeComplete(boolean screenCaptureModeComplete)
   * { this.screenCaptureModeComplete = screenCaptureModeComplete; }
   */
  
  public void setSuppressLocalKeyCombinations(boolean suppressLocalKeyCombinations)
  {
    this.suppressLocalKeyCombinations = suppressLocalKeyCombinations;
  }
  
  public boolean isSuppressLocalKeyCombinations()
  {
    return suppressLocalKeyCombinations;
  }
  
  public void setIgnoreLocalKeyCombinations(boolean ignoreLocalKeyCombinations)
  {
    this.ignoreLocalKeyCombinations = ignoreLocalKeyCombinations;
  }
  
  public boolean isIgnoreLocalKeyCombinations()
  {
    return ignoreLocalKeyCombinations;
  }
  
  public boolean isOpen()
  {
    return open;
  }
  
  /*
   * public boolean isRemoteInterfaceLoaded() { return remoteInterfaceLoaded; }
   */
  
  /* public boolean isNeedRefresh() { return needRefresh; } */
  
  public void setReader(VTGraphicsModeClientReader reader)
  {
    this.reader = reader;
  }
  
  public void clearAllPressedKeys()
  {
    keyListener.clearAllPressedKeys();
  }
  
  public void releaseAllPressedKeys()
  {
    if (!stopped && !controlInterrupted)
    {
      try
      {
        // synchronizeAllRemoteLockingKeys();
        // System.out.println("releaseAllPressedKeys()");
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_ANY_INPUT_RELEASE_ALL_PRESSED_KEYS);
        connection.getGraphicsControlDataOutputStream().flush();
      }
      catch (Throwable e)
      {
        stopped = true;
      }
      keyListener.clearAllPressedKeys();
    }
  }
  
  public void writeEvent(VTAWTControlEvent event)
  {
    /* if (event != null) { return; } */
    try
    {
      /*
       * if (!canvas.isFocusOwner()) {
       * System.out.println("Canvas Focus Alert!"); }
       */
      if (event != null && (!controlInterrupted && (terminalControlPolicy == TERMINAL_STATE_FOCUSED && remoteInterface.isFocusOwner()) || terminalControlPolicy == TERMINAL_STATE_VISIBLE))
      {
        switch (event.id)
        {
          case MouseEvent.MOUSE_MOVED:
          case MouseEvent.MOUSE_DRAGGED:
          {
            connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_MOUSE_INPUT_MOVE);
            connection.getGraphicsControlDataOutputStream().writeInt(event.x);
            connection.getGraphicsControlDataOutputStream().writeInt(event.y);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
          case MouseEvent.MOUSE_PRESSED:
          {
            connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_MOUSE_INPUT_KEY_DOWN);
            connection.getGraphicsControlDataOutputStream().writeInt(event.button);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
          case MouseEvent.MOUSE_RELEASED:
          {
            connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_MOUSE_INPUT_KEY_UP);
            connection.getGraphicsControlDataOutputStream().writeInt(event.button);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
          case MouseWheelEvent.MOUSE_WHEEL:
          {
            connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_MOUSE_INPUT_WHEEL);
            connection.getGraphicsControlDataOutputStream().writeInt(event.wheel);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
          case KeyEvent.KEY_PRESSED:
          {
            connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_KEYBOARD_INPUT_KEY_DOWN);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyCode);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyModifiers);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyLocation);
            connection.getGraphicsControlDataOutputStream().writeChar(event.keyChar);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
          case KeyEvent.KEY_RELEASED:
          {
            connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_KEYBOARD_INPUT_KEY_UP);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyCode);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyModifiers);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyLocation);
            connection.getGraphicsControlDataOutputStream().writeChar(event.keyChar);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
        }
      }
    }
    catch (IOException e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
  }
  
  /*
   * public void tryDelayedUpdateInAllRemoteLockingKeys() { synchronized
   * (lockingKeySynchronizer) { if (!synchronizingLockingKey) {
   * synchronizingLockingKey = true;
   * EventQueue.invokeLater(lockingKeySynchronizer); } } }
   */
  
  /*
   * public void finishDelayedUpdateInAllRemoteLockingKeys() { synchronized
   * (lockingKeySynchronizer) { synchronizingLockingKey = false; } }
   */
  
  /*
   * public void synchronizeAllRemoteLockingKeys() { if (!readOnly) {
   * synchronizeRemoteLockingKey(KeyEvent.VK_NUM_LOCK);
   * synchronizeRemoteLockingKey(KeyEvent.VK_CAPS_LOCK);
   * synchronizeRemoteLockingKey(KeyEvent.VK_SCROLL_LOCK);
   * synchronizeRemoteLockingKey(KeyEvent.VK_KANA_LOCK); } }
   */
  
  public void synchronizeRemoteLockingKey(int keyCode)
  {
    if (!controlInterrupted)
    {
      try
      {
        if (toolkit.getLockingKeyState(keyCode))
        {
          connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_KEYBOARD_LOCK_KEY_STATE_ON);
        }
        else
        {
          connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_KEYBOARD_LOCK_KEY_STATE_OFF);
        }
        connection.getGraphicsControlDataOutputStream().writeInt(keyCode);
        connection.getGraphicsControlDataOutputStream().flush();
      }
      catch (IOException e)
      {
        // e.printStackTrace();
        stopped = true;
        return;
      }
      catch (Throwable e)
      {
        return;
      }
    }
  }
  
  public void synchronizeColorQuality()
  {
    try
    {
      if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16777216)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_16777216);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_32768)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_32768);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_216);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_16);
      }
//      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_32)
//      {
//        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_32);
//      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_512)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_512);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4096)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_4096);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_4);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_8)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_8);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_125)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_125);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_27)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_27);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_262144)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_262144);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_2097152)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_2097152);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_64)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_QUALITY_64);
      }
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (IOException e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
    catch (Throwable e)
    {
      return;
    }
  }
  
  public void synchronizeImageCoding()
  {
    try
    {
      if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG);
        // connection.getGraphicsControlDataOutputStream().flush();
      }
      else if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG);
        // connection.getGraphicsControlDataOutputStream().flush();
      }
      else if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_DOF)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_DOF);
        // connection.getGraphicsControlDataOutputStream().flush();
      }
      else
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF);
        // connection.getGraphicsControlDataOutputStream().flush();
      }
    }
    catch (IOException e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
    catch (Throwable e)
    {
      return;
    }
  }
  
  public void sendInterruptRefresh()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_INTERRUPTED);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (IOException e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
    catch (Throwable e)
    {
      return;
    }
  }
  
  public void synchronizeRefreshMode()
  {
    try
    {
      if (synchronousRefresh)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_SYNCHRONOUS);
        connection.getGraphicsControlDataOutputStream().flush();
      }
      else
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_ASYNCHRONOUS);
        connection.getGraphicsControlDataOutputStream().flush();
      }
    }
    catch (IOException e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
    catch (Throwable e)
    {
      return;
    }
  }
  
  public void synchronizeDrawPointer()
  {
    try
    {
      if (drawPointer)
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_ON);
        connection.getGraphicsControlDataOutputStream().flush();
      }
      else
      {
        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_OFF);
        connection.getGraphicsControlDataOutputStream().flush();
      }
    }
    catch (IOException e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
    catch (Throwable e)
    {
      return;
    }
  }
  
  public void synchronizeScreenCaptureInterval()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_INTERVAL_CHANGE);
      connection.getGraphicsControlDataOutputStream().writeInt(screenCaptureInterval);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (IOException e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
    catch (Throwable e)
    {
      return;
    }
  }
  
  /*
   * public void synchronizeScreenCaptureMode() { try { if (screenCaptureMode ==
   * VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_UNSCALED_PARTIAL) {
   * connection.getGraphicsImageDataOutputStream().write(VT.
   * VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_UNSCALED_PARTIAL);
   * synchronizeScreenCaptureArea();
   * connection.getGraphicsImageDataOutputStream().flush(); } else if
   * (screenCaptureMode ==
   * VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_UNSCALED_COMPLETE) {
   * connection.getGraphicsImageDataOutputStream().write(VT.
   * VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_UNSCALED_COMPLETE);
   * connection.getGraphicsImageDataOutputStream().flush(); } else if
   * (screenCaptureMode ==
   * VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_KEEP_RATIO) {
   * connection.getGraphicsImageDataOutputStream().write(VT.
   * VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_KEEP_RATIO);
   * connection.getGraphicsImageDataOutputStream().flush(); } else if
   * (screenCaptureMode ==
   * VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_IGNORE_RATIO) {
   * connection.getGraphicsImageDataOutputStream().write(VT.
   * VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_IGNORE_RATIO);
   * connection.getGraphicsImageDataOutputStream().flush(); } } catch
   * (IOException e) { //e.printStackTrace(); stopped = true; return; } catch
   * (Throwable e) { return; } }
   */
  
  public void synchronizeScreenCaptureArea()
  {
    try
    {
      // System.out.println("VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_AREA_CHANGE:
      // " + captureArea.x + " " + captureArea.y + " " + captureArea.width
      // + " " + captureArea.height);
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_AREA_CHANGE);
      connection.getGraphicsControlDataOutputStream().writeInt(captureArea.x);
      connection.getGraphicsControlDataOutputStream().writeInt(captureArea.y);
      connection.getGraphicsControlDataOutputStream().writeInt(captureArea.width);
      connection.getGraphicsControlDataOutputStream().writeInt(captureArea.height);
      connection.getGraphicsControlDataOutputStream().writeDouble(captureScale);
      // connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (IOException e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
    catch (Throwable e)
    {
      return;
    }
  }
  
//  public void synchronizeDynamicCoding()
//  {
//    try
//    {
//      if (dynamicCoding)
//      {
//        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_CODING_DYNAMIC);
//        connection.getGraphicsControlDataOutputStream().flush();
//      }
//      else
//      {
//        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_COLOR_CODING_DIRECT);
//        connection.getGraphicsControlDataOutputStream().flush();
//      }
//    }
//    catch (IOException e)
//    {
//      stopped = true;
//      return;
//    }
//    catch (Throwable e)
//    {
//      return;
//    }
//  }
  
//  public void synchronizeSeparatedCoding()
//  {
//    try
//    {
//      if (separatedCoding)
//      {
//        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_DIFFERENCE_SEPARATED_CODING);
//        connection.getGraphicsControlDataOutputStream().flush();
//      }
//      else
//      {
//        connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_DIFFERENCE_MIXED_CODING);
//        connection.getGraphicsControlDataOutputStream().flush();
//      }
//    }
//    catch (IOException e)
//    {
//      stopped = true;
//      return;
//    }
//    catch (Throwable e)
//    {
//      return;
//    }
//  }
  
  public void requestNextDevice()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_NEXT);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
  }
  
  public void requestPreviousDevice()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_PREVIOUS);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
  }
  
  public void requestDefaultDevice()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_DEFAULT);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
  }
  
  public void requestUnifiedDevice()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_CHANGE_DEVICE_UNIFIED);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
  }
  
  public void requestClear()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_CLEAR_REQUEST);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
  }
  
  public void requestRefresh()
  {
    // System.out.println("requestRefresh");
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_REQUEST);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
  }
  
  public void interruptControl()
  {
    if (remoteInterface != null)
    {
      keyListener.setInterrupted(true);
      remoteInterface.removeMouseListener(mouseListener);
      remoteInterface.removeMouseMotionListener(mouseMotionListener);
      remoteInterface.removeMouseWheelListener(mouseWheelListener);
      menuBar.interruptControl();
      // statusBar.setControlStatusText("Control: Interrupted");
    }
  }
  
  public void restablishControl()
  {
    if (remoteInterface != null)
    {
      keyListener.setInterrupted(false);
      remoteInterface.addMouseListener(mouseListener);
      remoteInterface.addMouseMotionListener(mouseMotionListener);
      remoteInterface.addMouseWheelListener(mouseWheelListener);
      menuBar.restabilishControl();
      // statusBar.setControlStatusText("Control: Active ");
    }
  }
  
  public void toggleControl()
  {
    if (controlInterrupted)
    {
      controlInterrupted = false;
      restablishControl();
      // statusBar.update(statusBar.getGraphics());
    }
    else
    {
      controlInterrupted = true;
      interruptControl();
      // statusBar.update(statusBar.getGraphics());
    }
  }
  
  public void updateColorQuality(int colorQuality)
  {
    this.colorQuality = colorQuality;
    menuBar.setColorQuality(colorQuality);
    // synchronizeColorQuality();
  }
  
  public void updateImageCoding(int imageCoding)
  {
    this.imageCoding = imageCoding;
    menuBar.setImageCoding(imageCoding);
    // synchronizeImageCoding();
  }
  
  public void toggleDrawPointer()
  {
    if (drawPointer)
    {
      drawPointer = false;
      menuBar.setDrawPointer(false);
      synchronizeDrawPointer();
    }
    else
    {
      drawPointer = true;
      menuBar.setDrawPointer(true);
      synchronizeDrawPointer();
    }
  }
  
  public void increaseDrawPointerSize()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_INCREASE);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void decreaseDrawPointerSize()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_DECREASE);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void normalizeDrawPointerSize()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_DRAW_POINTER_NORMALIZE);
      connection.getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void toggleSuppressLocalKeyCombinations()
  {
    if (suppressLocalKeyCombinations)
    {
      suppressLocalKeyCombinations = false;
      menuBar.setSuppressLocalKeyCombinations(false);
    }
    else
    {
      if (selfControlProvider == null)
      {
        try
        {
          selfControlProvider = new VTAWTControlProvider();
          selfControlProvider.initializeInputControl();
          // selfControlProvider.setAutoWaitForIdle(true);
          suppressLocalKeyCombinations = true;
          menuBar.setSuppressLocalKeyCombinations(true);
        }
        catch (Throwable e)
        {
          suppressLocalKeyCombinations = false;
          menuBar.setSuppressLocalKeyCombinations(false);
        }
      }
      else
      {
        suppressLocalKeyCombinations = true;
        menuBar.setSuppressLocalKeyCombinations(true);
      }
    }
  }
  
  public void toggleIgnoreLocalKeyCombinations()
  {
    if (ignoreLocalKeyCombinations)
    {
      ignoreLocalKeyCombinations = false;
      menuBar.setIgnoreLocalKeyCombinations(false);
    }
    else
    {
      ignoreLocalKeyCombinations = true;
      menuBar.setIgnoreLocalKeyCombinations(true);
    }
  }
  
  public void setTerminalRefreshPolicy(int state)
  {
    this.terminalRefreshPolicy = state;
    menuBar.setTerminalRefreshPolicy(state);
  }
  
  public void setTerminalControlPolicy(int state)
  {
    this.terminalControlPolicy = state;
    menuBar.setTerminalControlPolicy(state);
  }
  
  /*
   * public void alternateIgnoreFocus() { if (ignoreFocus) { ignoreFocus =
   * false; ignoreIconification = false; menuBar.setIgnoreFocus(false);
   * menuBar.setIgnoreIconification(false); } else { ignoreFocus = true;
   * menuBar.setIgnoreFocus(true); } }
   */
  
  /*
   * public void alternateIgnoreIconification() { if (ignoreIconification) {
   * ignoreIconification = false; menuBar.setIgnoreIconification(false); } else
   * { ignoreFocus = true; ignoreIconification = true;
   * menuBar.setIgnoreFocus(true); menuBar.setIgnoreIconification(true); } }
   */
  
  public void setScreenCaptureMode(int nextMode)
  {
    menuBar.setScreenCaptureMode(nextMode);
    screenCaptureMode = nextMode;
    if (screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_KEEP_RATIO || screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      captureScale = 1;
    }
    /* if () { remoteInterface.clearSize(); } */
    // remoteInterface.setImage(null);
    // synchronizeScreenCaptureMode();
  }
  
//  public void toggleDynamicCoding()
//  {
//    if (dynamicCoding)
//    {
//      dynamicCoding = false;
//      menuBar.setDynamicCoding(false);
//      synchronizeDynamicCoding();
//    }
//    else
//    {
//      dynamicCoding = true;
//      menuBar.setDynamicCoding(true);
//      synchronizeDynamicCoding();
//    }
//  }
//  
//  public void toggleSeparatedCoding()
//  {
//    if (separatedCoding)
//    {
//      separatedCoding = false;
//      menuBar.setInterleavedCoding(false);
//      synchronizeSeparatedCoding();
//    }
//    else
//    {
//      separatedCoding = true;
//      menuBar.setInterleavedCoding(true);
//      synchronizeSeparatedCoding();
//    }
//  }
  
  public void toggleFullScreenMode()
  {
    // System.out.println("full screen toggled");
    EventQueue.invokeLater(fullscreenToggler);
    // fullscreenToggler.run();
  }
  
  public void toggleMenubar()
  {
    // System.out.println("full screen toggled");
    EventQueue.invokeLater(menubarToggler);
  }
  
  public void adjustFrameSize()
  {
    EventQueue.invokeLater(frameSizeAdjuster);
  }
  
  public void updateRefreshMode(boolean synchronousRefresh)
  {
    menuBar.resumeRefresh();
    this.refreshInterrupted = false;
    this.synchronousRefresh = synchronousRefresh;
    menuBar.setSynchronousRefresh(synchronousRefresh);
    synchronizeRefreshMode();
  }
  
  public void interruptRefresh()
  {
    menuBar.interruptRefresh();
    this.refreshInterrupted = true;
    sendInterruptRefresh();
  }
  
  public void updateCaptureScale(int captureScale)
  {
    if (screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_KEEP_RATIO || screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      setScreenCaptureMode(VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_VIEWPORT);
    }
    if (captureScale == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_DEFAULT_SCALE)
    {
      resetCaptureScale();
    }
    if (captureScale == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_INCREASE_SCALE)
    {
      increaseCaptureScale();
    }
    if (captureScale == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_DECREASE_SCALE)
    {
      decreaseCaptureScale();
    }
  }
  
  public void updateScreenCaptureInterval(int screenCaptureInterval)
  {
    this.screenCaptureInterval = screenCaptureInterval;
    menuBar.setScreenCaptureInterval(screenCaptureInterval);
    synchronizeScreenCaptureInterval();
  }
  
  public Rectangle calculateScreenCaptureArea()
  {
    Point local = scrolled.getScrollPosition();
    Dimension size = scrolled.getViewportSize();
    // Rectangle area = new Rectangle(local.x, local.y, size.width,
    // size.height);
    Rectangle area = new Rectangle();
    if (screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_ENTIRE)
    {
      area.x = -1;
      area.y = -1;
      area.width = imageDataBuffer.getWidth();
      area.height = imageDataBuffer.getHeight();
    }
    else if (screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_KEEP_RATIO)
    {
      area.x = -2;
      area.y = -2;
      area.width = size.width;
      area.height = size.height;
    }
    else if (screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      area.x = -3;
      area.y = -3;
      area.width = size.width;
      area.height = size.height;
    }
    else
    {
      area.x = local.x;
      area.y = local.y;
      area.width = size.width;
      area.height = size.height;
    }
    // System.out.println("trueArea:" + area);
    return area;
  }
  
  public void setScreenCaptureArea(Rectangle captureArea)
  {
    this.captureArea = captureArea;
    // synchronizeScreenCaptureArea();
  }
  
  public void defaultDevice()
  {
    requestDefaultDevice();
  }
  
  public void nextDevice()
  {
    requestNextDevice();
  }
  
  public void previousDevice()
  {
    requestPreviousDevice();
  }
  
  public void unifiedDevice()
  {
    requestUnifiedDevice();
  }
  
  public void clearRemoteGraphics()
  {
    /*
     * BufferedImage image = remoteInterface.getImage(); if (image != null) {
     * VTImageIO.clearImage(image); }
     */
    // frame.getLayout().layoutContainer(scrolled);
    // frame.validate();
    // remoteInterface.setImage(null);
    remoteInterface.clearImage();
    remoteInterface.repaint();
    scrolled.validate();
    requestClear();
  }
  
  /* public void toBack() { if (frame != null) { frame.toBack(); } } */
  
  /*
   * public void releaseFocus() { if (remoteInterface != null) {
   * remoteInterface.transferFocus(); } }
   */
  
  public void requestFocus()
  {
    // System.out.println("requestFocus()");
    // System.out.println("writer.requestFocus()");
    if (remoteInterface != null)
    {
      remoteInterface.requestFocusInWindow();
    }
  }
  
  public void resizeRemoteGraphics(int width, int height)
  {
    // Dimension oldSize = remoteInterface.getSize();
    Dimension newSize = new Dimension(width, height);
    // System.out.println("resizeRemoteGraphics oldSize " + oldSize.width +
    // " " + oldSize.height + " newSize " + newSize.width + " " +
    // newSize.height);
    
    // if (!newSize.equals(oldSize))
    // {
    
    // }
    // else
    // {
    // System.out.println("resizeRemoteGraphics not changed size");
    // }
    remoteInterface.setSize(newSize);
    remoteInterface.setMaximumSize(newSize);
    remoteInterface.setMinimumSize(newSize);
    remoteInterface.setPreferredSize(newSize);
    // remoteInterface.repaint();
    // remoteInterface.redraw();
    scrolled.validate();
    // frame.getLayout().layoutContainer(scrolled);
    // frame.validate();
    /*
     * if (!remoteInterfaceLoaded) { remoteInterfaceLoaded = true;
     * scrolled.setScrollPosition((imageDataBuffer.getWidth() -
     * scrolled.getViewportSize().width) / 2, (imageDataBuffer.getHeight() -
     * scrolled.getViewportSize().height) / 2); synchronized (graphicsRefresher)
     * { graphicsRefresher.notify(); } }
     */
  }
  
  public void setRemoteGraphics(BufferedImage newImageData)
  {
    imageDataBuffer = newImageData;
    remoteInterface.setImage(imageDataBuffer);
    // remoteInterface.refreshImage();
    // scrolled.setPreferredSize(scrolled.getSize());
    remoteInterface.refreshImage();
    remoteInterface.repaint();
    scrolled.validate();
    scrolled.doLayout();
    // frame.getLayout().layoutContainer(scrolled);
    // frame.validate();
  }
  
  public void differenceRemoteGraphics(BufferedImage newImageData)
  {
    // System.out.println("differenceRemoteGraphics");
    imageDataBuffer = newImageData;
    // needRefresh = true;
    hasRefresh = false;
    hasDifference = true;
    updateRemoteGraphics();
    // updateRemoteGraphics();
    // updateRemoteGraphics();
  }
  
  public void refreshRemoteGraphics(BufferedImage newImageData)
  {
    // System.out.println("newFrameRemoteGraphics");
    imageDataBuffer = newImageData;
    // needRefresh = true;
    hasRefresh = true;
    hasDifference = false;
    updateRemoteGraphics();
    // updateRemoteGraphics();
    // updateRemoteGraphics();
  }
  
  public void notModifiedRemoteGraphics()
  {
    // needRefresh = true;
    // System.out.println("notModifiedRemoteGraphics");
    hasRefresh = false;
    hasDifference = false;
    // updateRemoteGraphics();
    // updateRemoteGraphics();
    // updateRemoteGraphics();
  }
  
  public void increaseCaptureScale()
  {
    if (screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_KEEP_RATIO || screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      setScreenCaptureMode(VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_VIEWPORT);
      captureScale = 1;
    }
    captureScale = captureScale * IMAGE_SCALE_MULTIPLIER_FACTOR;
  }
  
  public void decreaseCaptureScale()
  {
    if (screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_KEEP_RATIO || screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      setScreenCaptureMode(VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_VIEWPORT);
      captureScale = 1;
    }
    captureScale = captureScale / IMAGE_SCALE_MULTIPLIER_FACTOR;
  }
  
  public void resetCaptureScale()
  {
    if (screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_KEEP_RATIO || screenCaptureMode == VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      setScreenCaptureMode(VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_SCALED_VIEWPORT);
    }
    captureScale = 1;
  }
  
  public void requestInterfaceRefresh()
  {
    needRefresh = true;
    synchronized (this)
    {
      notify();
    }
  }
  
  //public void setRefreshArea(Rectangle area)
  //{
    //remoteInterface.setRefreshArea(area);
  //}
  
  public void increaseCaptureInterval()
  {
    menuBar.increaseCaptureInterval();
  }
  
  public void decreaseCaptureInterval()
  {
    menuBar.decreaseCaptureInterval();
  }
  
  public void decreaseColorQuality()
  {
    menuBar.decreaseColorQuality();
  }
  
  public void increaseColorQuality()
  {
    menuBar.increaseColorQuality();
  }
  
  public boolean isClipboardControlEnabled()
  {
    return menuBar.isClipBoardControlEnabled();
  }
  
  private void updateRemoteGraphics()
  {
    try
    {
      // Point local = scrolled.getScrollPosition();
      // Dimension size = scrolled.getViewportSize();
      if (hasDifference)
      {
        if (remoteInterface != null && remoteInterface.isAsynchronousRepainterRunning() && !remoteInterface.isAsynchronousRepainterInterrupted())
        {
          remoteInterface.interruptAsynchronousRepainter();
        }
        remoteInterface.setImage(imageDataBuffer);
        remoteInterface.refreshImage();
        // scrolled.setPreferredSize(scrolled.getSize());
        remoteInterface.repaint();
        // remoteInterface.redraw();
      }
      else if (hasRefresh)
      {
        if (remoteInterface != null && remoteInterface.isAsynchronousRepainterRunning() && !remoteInterface.isAsynchronousRepainterInterrupted())
        {
          remoteInterface.interruptAsynchronousRepainter();
        }
        remoteInterface.setImage(imageDataBuffer);
        remoteInterface.refreshImage();
        // scrolled.setPreferredSize(scrolled.getSize());
        remoteInterface.repaint();
        scrolled.validate();
        
        // remoteInterface.redraw();
        
        // frame.getLayout().layoutContainer(scrolled);
        // frame.validate();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
    synchronized (this)
    {
      // needRefresh = false;
      hasRefresh = false;
      hasDifference = false;
    }
    // EventQueue.invokeLater(graphicsRefresher);
    // requestRefresh();
  }
  
  /*
   * private void sendSessionEnding() { try {
   * connection.getGraphicsControlDataOutputStream().write(VT.
   * VT_GRAPHICS_MODE_SESSION_ENDING);
   * connection.getGraphicsControlDataOutputStream().flush(); } catch (Throwable
   * e) { stopped = true; } }
   */
  
  private void createCustomCursor()
  {
    int dpi = VTGlobalTextStyleManager.BASE_FONT_DPI;
    int calculatedSize = Math.max(32, dpi / 3);
    Dimension bestSize = toolkit.getBestCursorSize(calculatedSize, calculatedSize);
    if (bestSize.width != 0 && bestSize.height != 0)
    {
      try
      {
        if ((bestSize.width >= calculatedSize / 2) && (bestSize.height >= calculatedSize / 2))
        {
          BufferedImage cursorImage = new BufferedImage(bestSize.width, bestSize.height, BufferedImage.TYPE_INT_ARGB);
          Point center = new Point(bestSize.width / 2, bestSize.height / 2);
          // int limit = center.x;
          // System.out.println("bestsize: x: " + bestSize.width + ",
          // y: " + bestSize.height);
          // System.out.println("center: x: " + center.x + ", y: " +
          // center.y);
          // System.out.println("limit: " + limit);
          for (int i = 2; i < center.x && (center.x - i) >= 0; i++)
          {
            cursorImage.setRGB(center.x - i, center.y, 0xFF000000);
            cursorImage.setRGB(center.x - i, center.y + 1, 0xFFFFFFFF);
            cursorImage.setRGB(center.x - i, center.y - 1, 0xFFFFFFFF);
          }
          for (int i = 2; i < center.x && (center.x + i) < bestSize.width; i++)
          {
            cursorImage.setRGB(center.x + i, center.y, 0xFF000000);
            cursorImage.setRGB(center.x + i, center.y + 1, 0xFFFFFFFF);
            cursorImage.setRGB(center.x + i, center.y - 1, 0xFFFFFFFF);
          }
          for (int i = 2; i < center.y && (center.y - i) >= 0; i++)
          {
            cursorImage.setRGB(center.x, center.y - i, 0xFF000000);
            cursorImage.setRGB(center.x + 1, center.y - i, 0xFFFFFFFF);
            cursorImage.setRGB(center.x - 1, center.y - i, 0xFFFFFFFF);
          }
          for (int i = 2; i < center.y && (center.y + i) < bestSize.height; i++)
          {
            cursorImage.setRGB(center.x, center.y + i, 0xFF000000);
            cursorImage.setRGB(center.x + 1, center.y + i, 0xFFFFFFFF);
            cursorImage.setRGB(center.x - 1, center.y + i, 0xFFFFFFFF);
          }
          //cursor = toolkit.createCustomCursor(cursorImage, center, "VT_GRAPHICSMODE_CROSSHAIR_CURSOR");
          // cursor =
          // Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        }
        else
        {
          //cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
          // cursor =
          // Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        }
        // cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
      }
      catch (Throwable e)
      {
        //cursor = null;
        // e.printStackTrace();
      }
    }
    else
    {
      // cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
      // cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }
    //cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  }
  
  public void run()
  {
    /*
     * try { toolkit = Toolkit.getDefaultToolkit(); } catch (Throwable e) {
     * dispose(); sendSessionEnding(); return; }
     */
    createCustomCursor();
    try
    {
      // selfControlProvider = new VTAWTControlProvider();
      // selfControlProvider.initializeInputControl();
      if (VTConsole.isGraphical())
      {
        GraphicsDevice device = VTGraphicalDeviceResolver.getCurrentDevice(VTConsole.getFrame());
        if (device != null)
        {
          frame = new VTGraphicsModeClientWriterFrame(device.getDefaultConfiguration());
        }
        else
        {
          frame = new VTGraphicsModeClientWriterFrame();
        }
      }
      else
      {
        frame = new VTGraphicsModeClientWriterFrame();
      }
      VTGlobalTextStyleManager.registerWindow(frame);
      
      frame.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Client - Remote Graphics Link");
      frame.setFocusable(false);
      // frame.getInsets().set(0, 0, 0, 0);
      BorderLayout frameLayout = new BorderLayout();
      frameLayout.setHgap(0);
      frameLayout.setVgap(0);
      frame.setLayout(frameLayout);
      frame.getInsets().set(0, 0, 0, 0);
      menuBar = new VTGraphicsModeClientOptionsMenuBar(this, frame);
      // menuBar.setReadOnly(readOnly);
      // frame.setMenuBar(menuBar);
      scrolled = new VTGraphicsModeClientWriterScrollPane(VTGraphicsModeClientWriterScrollPane.SCROLLBARS_AS_NEEDED);
      
      // scrolledWithBars = new
      // VTGraphicsModeClientWriterScrollPane(VTGraphicsModeClientWriterScrollPane.SCROLLBARS_AS_NEEDED);
      // scrolledWithoutBars = new
      // VTGraphicsModeClientWriterScrollPane(VTGraphicsModeClientWriterScrollPane.SCROLLBARS_NEVER);
      // scrolled.setBackground(new Color(0x00999999));
      // scrolled.setBackground(new Color(0x00BFBFBF));
      scrolled.setBackground(new Color(0x00AAAAAA));
      // scrolledWithBars.setBackground(new Color(0x00AAAAAA));
      // scrolledWithoutBars.setBackground(new Color(0x00AAAAAA));
      // scrolled.add
      scrolled.setFocusable(false);
      scrolled.getInsets().set(0, 0, 0, 0);
      // scrolled.setWheelScrollingEnabled(false);
      remoteInterface = new VTGraphicsModeClientRemoteInterface(scrolled);
      // remoteInterface.setImage(new BufferedImage(1, 1,
      // BufferedImage.TYPE_INT_RGB));
      remoteInterface.setFocusTraversalKeysEnabled(false);
      // remoteInterface.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      if (cursor != null)
      {
        remoteInterface.setCursor(cursor);
      }
      remoteInterface.addFocusListener(focusListener);
      frame.addWindowListener(windowListener);
      /* if (!readOnly) { */
      // synchronizeAllRemoteLockingKeys();
      session.getSession().getClipboardTransferTask().setEndingTask(new VTGraphicsModeClientClipboardTransferEndingTask(this));
      session.getSession().getClipboardTransferTask().setInputStream(connection.getGraphicsClipboardDataInputStream());
      session.getSession().getClipboardTransferTask().setOutputStream(connection.getGraphicsClipboardDataOutputStream());
      // controlInterrupted = false;
      keyListener.setInterrupted(false);
      keyListener.clearAllPressedKeys();
      remoteInterface.addKeyListener(keyListener);
      // remoteInterface.addMouseListener(mouseListener);
      // remoteInterface.addMouseMotionListener(mouseMotionListener);
      // remoteInterface.addMouseWheelListener(mouseWheelListener);
      // }
      if (readOnly)
      {
        controlInterrupted = true;
        interruptControl();
      }
      else
      {
        controlInterrupted = false;
        restablishControl();
      }
      frame.add(scrolled, BorderLayout.CENTER);
      // frame.add(scrolledWithoutBars, BorderLayout.NORTH);
      // frame.add(statusBar, BorderLayout.SOUTH);
      scrolled.add(remoteInterface);
      
      Rectangle screenSize = frame.getGraphicsConfiguration().getBounds();
      
      if (initialWidth > 0 && initialHeight > 0 && (initialWidth < screenSize.width) && (initialHeight < screenSize.height))
      {
        frame.pack();
        Insets frameInsets = frame.getInsets();
        Insets scrolledInsets = scrolled.getInsets();
        
        int totalWidth = initialWidth + frameInsets.left + frameInsets.right + scrolledInsets.left + scrolledInsets.right;
        int totalHeight = initialHeight + frameInsets.top + frameInsets.bottom + scrolledInsets.top + scrolledInsets.bottom;
        if ((totalWidth < screenSize.width) && (totalHeight < screenSize.height))
        {
          frame.setSize(totalWidth, totalHeight);
        }
        else
        {
          frame.setSize(((screenSize.width * 3) / 4), ((screenSize.height * 3) / 4));
          // frame.setSize(((screenSize.width) / 2),
          // ((screenSize.height) / 2));
        }
        // frame.setLocationRelativeTo(null);
      }
      else
      {
        frame.setSize(((screenSize.width * 3) / 4), ((screenSize.height * 3) / 4));
        // frame.setSize(((screenSize.width) / 2), ((screenSize.height)
        // / 2));
        // frame.setLocationRelativeTo(null);
      }
      // if (VTConsole.isGraphical())
      // {
      // frame.setLocationRelativeTo(VTGraphicalConsole.getFrame());
      // }
      // else
      // {
      // frame.setLocationByPlatform(true);
      // }
      frame.setLocationByPlatform(true);
      frame.setVisible(true);
      
      scrolled.setVisible(true);
      remoteInterface.setVisible(true);
      
      // frame.toFront();
      // remoteInterface.requestFocus();
      
      frame.getInsets().set(0, 0, 0, 0);
      scrolled.getInsets().set(0, 0, 0, 0);
      // remoteInterface.getin
      
      synchronized (reader)
      {
        open = true;
        reader.notify();
      }
      // updateScreenCaptureArea(area);
      // EventQueue.invokeAndWait(captureAreaUpdater);
      EventQueue.invokeLater(graphicsRefresher);
      // synchronizingLockingKey = false;
      // tryDelayedUpdateInAllRemoteLockingKeys();
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
      synchronized (reader)
      {
        reader.setFailed(true);
        // reader.setStopped(true);
        open = true;
        stopped = true;
        reader.notify();
      }
    }
    while (!stopped)
    {
      try
      {
        synchronized (this)
        {
          while (!stopped && (!needRefresh || ((terminalRefreshPolicy == TERMINAL_STATE_VISIBLE) && (((frame.getExtendedState() & Frame.ICONIFIED)) != 0)) || ((terminalRefreshPolicy == TERMINAL_STATE_FOCUSED) && !remoteInterface.isFocusOwner())))
          {
            wait();
          }
          if (!stopped)
          {
            needRefresh = false;
            EventQueue.invokeLater(graphicsRefresher);
            // updateRemoteGraphics();
          }
        }
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        // e.printStackTrace(VTConsole.getSystemOut());
        stopped = true;
        break;
      }
    }
    if (remoteInterface != null && remoteInterface.isAsynchronousRepainterRunning())
    {
      remoteInterface.stopAsynchronousRepainter();
    }
    if (frame != null)
    {
      frame.setVisible(false);
      frame.dispose();
    }
    // sendSessionEnding();
    synchronized (session)
    {
      session.notify();
    }
  }
  
  public void setKeyboardShortcutsMenuEnabled(boolean enabled)
  {
    menuBar.setKeyboardShortcutsMenuEnabled(enabled);
  }
}