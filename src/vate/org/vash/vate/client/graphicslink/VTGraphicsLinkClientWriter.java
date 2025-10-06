package org.vash.vate.client.graphicslink;

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

import org.vash.vate.VTSystem;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.graphicslink.listener.VTGraphicsLinkClientWindowListener;
import org.vash.vate.client.graphicslink.options.VTGraphicsLinkClientOptionsMenuBar;
import org.vash.vate.client.graphicslink.remote.VTGraphicsLinkClientRemoteInterface;
import org.vash.vate.client.graphicslink.remote.VTGraphicsLinkClientRemoteInterfaceRefresher;
import org.vash.vate.client.graphicslink.remote.listener.VTGraphicsLinkClientRemoteInterfaceFocusListener;
import org.vash.vate.client.graphicslink.remote.listener.VTGraphicsLinkClientRemoteInterfaceKeyListener;
import org.vash.vate.client.graphicslink.remote.listener.VTGraphicsLinkClientRemoteInterfaceMouseListener;
import org.vash.vate.client.graphicslink.remote.listener.VTGraphicsLinkClientRemoteInterfaceMouseMoveListener;
import org.vash.vate.client.graphicslink.remote.listener.VTGraphicsLinkClientRemoteInterfaceMouseWheelListener;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vash.vate.graphics.clipboard.VTEmptyTransferable;
import org.vash.vate.graphics.control.VTAWTControlEvent;
import org.vash.vate.graphics.control.VTAWTControlProvider;
import org.vash.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vash.vate.graphics.font.VTFontManager;
import org.vash.vate.monitor.VTDataMonitorMenu;

public class VTGraphicsLinkClientWriter implements Runnable
{
  private static final double IMAGE_SCALE_MULTIPLIER_FACTOR = Math.pow(2D, (1D / 8D));
  public static final int TERMINAL_STATE_FOCUSED = 0;
  public static final int TERMINAL_STATE_VISIBLE = 1;
  public static final int TERMINAL_STATE_IGNORE = 2;
  private volatile boolean stopped;
  private volatile boolean needRefresh;
  private boolean hasRefresh;
  private boolean hasDifference;
  private boolean open;
  private boolean readOnly;
  private boolean controlInterrupted;
  private boolean refreshInterrupted;
  private boolean synchronousRefresh;
  private boolean drawPointer;
  private boolean suppressLocalKeyCombinations;
  private boolean ignoreLocalKeyCombinations;
  private boolean hideScrollBars;
  private int screenCaptureInterval;
  private int colorQuality;
  private int terminalRefreshPolicy;
  private int terminalControlPolicy;
  private int initialWidth;
  private int initialHeight;
  private int screenCaptureMode;
  private int imageCoding;
  private Rectangle captureArea;
  private double captureScale;
  private Toolkit toolkit;
  private BufferedImage imageDataBuffer;
  private Cursor cursor;
  private Clipboard systemClipboard;
  private VTClientConnection connection;
  private VTGraphicsLinkClientSession session;
  private VTGraphicsLinkClientReader reader;
  private VTGraphicsLinkClientWriterFrame frame;
  private VTGraphicsLinkClientOptionsMenuBar menuBar;
  private VTGraphicsLinkClientWriterScrollPane scrolledMaybe;
  private VTGraphicsLinkClientWriterScrollPane scrolledNever;
  private VTGraphicsLinkClientWriterScrollPane scrolled;
  // private VTGraphicsLinkClientWriterScrollPane scrolledWithBars;
  // private VTGraphicsLinkClientWriterScrollPane scrolledWithoutBars;
  private VTGraphicsLinkClientWindowListener windowListener;
  private VTGraphicsLinkClientRemoteInterface remoteInterface;
  private VTGraphicsLinkClientRemoteInterfaceFocusListener focusListener;
  private VTGraphicsLinkClientRemoteInterfaceKeyListener keyListener;
  private VTGraphicsLinkClientRemoteInterfaceMouseListener mouseListener;
  private VTGraphicsLinkClientRemoteInterfaceMouseMoveListener mouseMotionListener;
  private VTGraphicsLinkClientRemoteInterfaceMouseWheelListener mouseWheelListener;
  // private VTGraphicsLinkClientRemoteInterfaceScrollPaneListener
  // scrollPaneListener;
  // private VTGraphicsLinkClientRemoteInterfaceLockingKeySynchronizer
  // lockingKeySynchronizer;
  // private VTGraphicsLinkClientRemoteInterfaceCaptureAreaUpdater
  // captureAreaUpdater;
  private VTGraphicsLinkClientRemoteInterfaceRefresher graphicsRefresher;
  private VTAWTControlProvider selfControlProvider;
  private int lastFrameState;
  private Rectangle lastFrameBounds;
  private Runnable fullscreenToggler;
  private Runnable frameSizeAdjuster;
  private Runnable menubarToggler;
  //private Point lastPointerPosition = new Point(0, 0);
  private int lastPointerX = -1;
  private int lastPointerY = -1;
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
  
  private class VTGraphicsLinkClientMenubarToggler implements Runnable
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
  
//  private class VTGraphicsLinkClientScrollbarsToggler implements Runnable
//  {
//    public void run()
//    {
//      try
//      {
//        toggleHideScrollBars();
//        frame.revalidate();
//      }
//      finally
//      {
//        
//      }
//    }
//  }
  
  private class VTGraphicsLinkClientFullScreenToggler implements Runnable
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
  
  class VTGraphicsLinkClientFrameSizeAdjuster implements Runnable
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
  
  public VTGraphicsLinkClientWriter(VTGraphicsLinkClientSession session)
  {
    //this.cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    this.stopped = true;
    this.toolkit = Toolkit.getDefaultToolkit();
    this.session = session;
    this.connection = session.getSession().getConnection();
    this.windowListener = new VTGraphicsLinkClientWindowListener(this);
    this.focusListener = new VTGraphicsLinkClientRemoteInterfaceFocusListener(this);
    this.keyListener = new VTGraphicsLinkClientRemoteInterfaceKeyListener(this);
    this.mouseListener = new VTGraphicsLinkClientRemoteInterfaceMouseListener(this);
    this.mouseMotionListener = new VTGraphicsLinkClientRemoteInterfaceMouseMoveListener(this);
    this.mouseWheelListener = new VTGraphicsLinkClientRemoteInterfaceMouseWheelListener(this);
    this.graphicsRefresher = new VTGraphicsLinkClientRemoteInterfaceRefresher(this);
    this.colorQuality = VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216;
    this.screenCaptureMode = VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_SCALED_VIEWPORT;
    this.imageCoding = VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_ZSD;
    this.terminalRefreshPolicy = TERMINAL_STATE_VISIBLE;
    this.terminalControlPolicy = TERMINAL_STATE_FOCUSED;
    this.synchronousRefresh = false;
    this.drawPointer = true;
    this.suppressLocalKeyCombinations = false;
    this.ignoreLocalKeyCombinations = false;
    this.refreshInterrupted = false;
    this.screenCaptureInterval = 250;
    this.captureScale = 1;
    this.fullscreenToggler = new VTGraphicsLinkClientFullScreenToggler();
    this.frameSizeAdjuster = new VTGraphicsLinkClientFrameSizeAdjuster();
    this.menubarToggler = new VTGraphicsLinkClientMenubarToggler();
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_KEYBOARD_INPUT_KEY_DOWN);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_KEYBOARD_INPUT_KEY_UP);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_CLIPBOARD_CLEAR_REQUEST);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_CLIPBOARD_TRANSFER_SEND_REQUEST);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_CLIPBOARD_TRANSFER_RECEIVE_REQUEST);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_CLIPBOARD_TRANSFER_CANCEL_REQUEST);
      connection.getGraphicsControlDataOutputStream().flush();
      connection.getGraphicsClipboardOutputStream().close();
      session.getSession().getClipboardTransferTask().interruptThread();
      // session.getSession().getClipboardTransferThread().stop();
      session.getSession().getClipboardTransferTask().joinThread();
      session.getSession().getConnection().resetClipboardStreams();
      session.getSession().getClipboardTransferTask().setInputStream(connection.getGraphicsClipboardDataInputStream());
      session.getSession().getClipboardTransferTask().setOutputStream(connection.getGraphicsClipboardDataOutputStream());
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
  }
  
  public void dispose()
  {
    hideScrollBars = false;
    lastPointerX = -1;
    lastPointerX = -1;
    captureScale = 1;
    initialWidth = 0;
    initialHeight = 0;
    stopped = true;
    needRefresh = false;
    hasRefresh = false;
    hasDifference = false;    
    terminalRefreshPolicy = TERMINAL_STATE_VISIBLE;
    terminalControlPolicy = TERMINAL_STATE_FOCUSED;
    open = false;
    colorQuality = VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216;
    synchronousRefresh = false;
    drawPointer = true;
    screenCaptureMode = VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_SCALED_VIEWPORT;
    imageCoding = VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_ZSD;
    suppressLocalKeyCombinations = false;
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
  
//  protected void finalize()
//  {
//    if (remoteInterface != null && remoteInterface.isAsynchronousRepainterRunning())
//    {
//      remoteInterface.stopAsynchronousRepainter();
//    }
//    if (frame != null)
//    {
//      frame.setVisible(false);
//      frame.dispose();
//    }
//  }
  
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
      if (remoteInterface != null && remoteInterface.isAsynchronousRepainterRunning())
      {
        remoteInterface.stopAsynchronousRepainter();
      }
      try
      {
        connection.closeGraphicsLinkStreams();
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
    //this.remoteInterface.setSynchronousRefresh(synchronousRefresh);
  }
  
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
  
  public void setReader(VTGraphicsLinkClientReader reader)
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
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_ANY_INPUT_RELEASE_ALL_PRESSED_KEYS);
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
      if (event != null && (!controlInterrupted && (terminalControlPolicy == TERMINAL_STATE_FOCUSED && remoteInterface.isFocusOwner()) || terminalControlPolicy == TERMINAL_STATE_VISIBLE))
      {
        switch (event.id)
        {
          case MouseEvent.MOUSE_MOVED:
          case MouseEvent.MOUSE_DRAGGED:
          {
            connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_MOUSE_INPUT_MOVE);
            connection.getGraphicsControlDataOutputStream().writeInt(event.x);
            connection.getGraphicsControlDataOutputStream().writeInt(event.y);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
          case MouseEvent.MOUSE_PRESSED:
          {
            connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_MOUSE_INPUT_KEY_DOWN);
            connection.getGraphicsControlDataOutputStream().writeInt(event.button);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
          case MouseEvent.MOUSE_RELEASED:
          {
            connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_MOUSE_INPUT_KEY_UP);
            connection.getGraphicsControlDataOutputStream().writeInt(event.button);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
          case MouseWheelEvent.MOUSE_WHEEL:
          {
            connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_MOUSE_INPUT_WHEEL);
            connection.getGraphicsControlDataOutputStream().writeInt(event.wheel);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
          case KeyEvent.KEY_PRESSED:
          {
            connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_KEYBOARD_INPUT_KEY_DOWN);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyCode);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyModifiers);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyLocation);
            connection.getGraphicsControlDataOutputStream().writeChar(event.keyChar);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
          case KeyEvent.KEY_RELEASED:
          {
            connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_KEYBOARD_INPUT_KEY_UP);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyCode);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyModifiers);
            connection.getGraphicsControlDataOutputStream().writeInt(event.keyLocation);
            connection.getGraphicsControlDataOutputStream().writeChar(event.keyChar);
            connection.getGraphicsControlDataOutputStream().flush();
            break;
          }
        }
      }
      
      if (hideScrollBars && remoteInterface.isFocusOwner() && (event.id == MouseEvent.MOUSE_MOVED || event.id == MouseEvent.MOUSE_DRAGGED))
      {
        Dimension viewport = scrolled.getViewportSize();
        Dimension imagesize = remoteInterface.getSize();
        
        int deltaX = 0;
        int deltaY = 0;
        
        int pointerX = event.x;
        int pointerY = event.y;
        
        if (viewport.width < imagesize.width || viewport.height < imagesize.height)
        {
          if (lastPointerX != pointerX || lastPointerY != pointerY)
          {
            Point currentScrollPosition = scrolled.getScrollPosition();
            
            Point nextScrollPosition = new Point(currentScrollPosition);
            
            int scrollX = currentScrollPosition.x;
            int scrollY = currentScrollPosition.y;
            
            int remainingM = (int) Math.min(viewport.width * 0.125, viewport.height * 0.125);
            
            if (pointerX < scrollX + remainingM)
            {
              deltaX = pointerX - remainingM - scrollX;
            }
            else if (pointerX > scrollX + viewport.width - remainingM)
            {
              deltaX = pointerX + remainingM - scrollX - viewport.width;
            }
            
            if (pointerY < scrollY + remainingM)
            {
              deltaY = pointerY - remainingM - scrollY;
            }
            else if (pointerY > scrollY + viewport.height - remainingM)
            {
              deltaY = pointerY + remainingM - scrollY - viewport.height;
            }
            
            if (deltaX != 0 || deltaY != 0)
            {
              nextScrollPosition.x += deltaX;
              nextScrollPosition.y += deltaY;
            }
            else
            {
              nextScrollPosition = null;
            }
            
            if (nextScrollPosition != null && !currentScrollPosition.equals(nextScrollPosition))
            {
              scrolled.setScrollPosition(nextScrollPosition);
              
              Point scrolledPosition = scrolled.getScrollPosition();
              
              deltaX = scrolledPosition.x - currentScrollPosition.x;
              deltaY = scrolledPosition.y - currentScrollPosition.y;
            }
            else
            {
              
            }
          }
          else
          {
            
          }
        }
        else
        {
          
        }
        lastPointerX = pointerX + deltaX;
        lastPointerY = pointerY + deltaY;
      }
      else
      {
        lastPointerX = event.x;
        lastPointerY = event.y;
      }
    }
    catch (IOException e)
    {
      // e.printStackTrace();
      stopped = true;
      return;
    }
  }
  
  public void synchronizeRemoteLockingKey(int keyCode)
  {
    if (!controlInterrupted)
    {
      try
      {
        if (toolkit.getLockingKeyState(keyCode))
        {
          connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_KEYBOARD_LOCK_KEY_STATE_ON);
        }
        else
        {
          connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_KEYBOARD_LOCK_KEY_STATE_OFF);
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
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_16777216);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_32768)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_32768);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_216);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_16);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_512)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_512);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4096)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_4096);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_4);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_8)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_8);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_125)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_125);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_27)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_27);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_262144)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_262144);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_2097152)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_2097152);
      }
      else if (colorQuality == VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_64)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_64);
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
      if (imageCoding == VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_PNG)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_PNG);
      }
      else if (imageCoding == VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_JPG)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_JPG);
      }
      else if (imageCoding == VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_GZD)
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_GZD);
      }
      else
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_ZSD);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_MODE_INTERRUPTED);
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
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_MODE_SYNCHRONOUS);
        connection.getGraphicsControlDataOutputStream().flush();
      }
      else
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_MODE_ASYNCHRONOUS);
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
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_DRAW_POINTER_ON);
        connection.getGraphicsControlDataOutputStream().flush();
      }
      else
      {
        connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_DRAW_POINTER_OFF);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_INTERVAL_CHANGE);
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
  
  public void synchronizeScreenCaptureArea()
  {
    try
    {
      // System.out.println("VT_GRAPHICS_LINK_GRAPHICS_CAPTURE_AREA_CHANGE:
      // " + captureArea.x + " " + captureArea.y + " " + captureArea.width
      // + " " + captureArea.height);
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_AREA_CHANGE);
      connection.getGraphicsControlDataOutputStream().writeInt(captureArea.x);
      connection.getGraphicsControlDataOutputStream().writeInt(captureArea.y);
      connection.getGraphicsControlDataOutputStream().writeInt(captureArea.width);
      connection.getGraphicsControlDataOutputStream().writeInt(captureArea.height);
      connection.getGraphicsControlDataOutputStream().writeDouble(captureScale);
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
  
  public void requestNextDevice()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_SELECT_DEVICE_NEXT);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_SELECT_DEVICE_PREVIOUS);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_SELECT_DEVICE_DEFAULT);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_SELECT_DEVICE_UNIFIED);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_CLEAR_REQUEST);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_REQUEST);
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
      //remoteInterface.removeMouseMotionListener(mouseMotionListener);
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
      //remoteInterface.addMouseMotionListener(mouseMotionListener);
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
  }
  
  public void updateImageCoding(int imageCoding)
  {
    this.imageCoding = imageCoding;
    menuBar.setImageCoding(imageCoding);
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
  
  public void toggleHideScrollBars()
  {
    Point scrollPosition = scrolled.getScrollPosition();
    
    if (hideScrollBars)
    {
      scrolled.remove(remoteInterface);
      frame.remove(scrolled);
      
      scrolled = scrolledMaybe;
    }
    else
    {
      scrolled.remove(remoteInterface);
      frame.remove(scrolled);
      
      scrolled = scrolledNever;
    }
    
    remoteInterface.setScrollPane(scrolled);
    frame.add(scrolled, BorderLayout.CENTER);
    scrolled.add(remoteInterface);
    
    frame.revalidate();
    scrolled.setScrollPosition(scrollPosition);
    remoteInterface.requestFocus();
    
    hideScrollBars = !hideScrollBars;
  }
  
  public void increaseDrawPointerSize()
  {
    try
    {
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_DRAW_POINTER_INCREASE);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_DRAW_POINTER_DECREASE);
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
      connection.getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_IMAGE_DRAW_POINTER_NORMALIZE);
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
  
  public void setScreenCaptureMode(int nextMode)
  {
    menuBar.setScreenCaptureMode(nextMode);
    screenCaptureMode = nextMode;
    if (screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_KEEP_RATIO || screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      captureScale = 1;
    }
  }
  
  public void toggleFullScreenMode()
  {
    EventQueue.invokeLater(fullscreenToggler);
  }
  
  public void toggleMenubar()
  {
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
    if (screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_KEEP_RATIO || screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      setScreenCaptureMode(VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_SCALED_VIEWPORT);
    }
    if (captureScale == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_DEFAULT_SCALE)
    {
      resetCaptureScale();
    }
    if (captureScale == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_INCREASE_SCALE)
    {
      increaseCaptureScale();
    }
    if (captureScale == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_DECREASE_SCALE)
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
    Rectangle area = new Rectangle();
    
    if (screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_SCALED_ENTIRE)
    {
      area.x = -1;
      area.y = -1;
      area.width = imageDataBuffer.getWidth();
      area.height = imageDataBuffer.getHeight();
    }
    else if (screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_KEEP_RATIO)
    {
      area.x = -2;
      area.y = -2;
      area.width = size.width;
      area.height = size.height;
    }
    else if (screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
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
    remoteInterface.clearImage();
    remoteInterface.repaint();
    scrolled.validate();
    requestClear();
  }
  
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
    Dimension newSize = new Dimension(width, height);
    remoteInterface.setSize(newSize);
    remoteInterface.setMaximumSize(newSize);
    remoteInterface.setMinimumSize(newSize);
    remoteInterface.setPreferredSize(newSize);
    // remoteInterface.repaint();
    // remoteInterface.redraw();
    scrolled.validate();
    // frame.getLayout().layoutContainer(scrolled);
    // frame.validate();
  }
  
  public void setRemoteGraphics(BufferedImage newImageData)
  {
    imageDataBuffer = newImageData;
    remoteInterface.setImage(imageDataBuffer);
    remoteInterface.repaint();
    scrolled.validate();
    scrolled.doLayout();
  }
  
  public void differenceRemoteGraphics(BufferedImage newImageData)
  {
    imageDataBuffer = newImageData;
    hasRefresh = false;
    hasDifference = true;
    updateRemoteGraphics();
  }
  
  public void refreshRemoteGraphics(BufferedImage newImageData)
  {
    imageDataBuffer = newImageData;
    hasRefresh = true;
    hasDifference = false;
    updateRemoteGraphics();
  }
  
  public void notModifiedRemoteGraphics()
  {
    hasRefresh = false;
    hasDifference = false;
  }
  
  public void increaseCaptureScale()
  {
    captureScale = captureScale * IMAGE_SCALE_MULTIPLIER_FACTOR;
    if (screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_KEEP_RATIO || screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      setScreenCaptureMode(VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_SCALED_VIEWPORT);
      captureScale = 1;
    }
  }
  
  public void decreaseCaptureScale()
  {
    captureScale = captureScale / IMAGE_SCALE_MULTIPLIER_FACTOR;
    if (screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_KEEP_RATIO || screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      setScreenCaptureMode(VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_SCALED_VIEWPORT);
      captureScale = 1;
    }
  }
  
  public void resetCaptureScale()
  {
    if (screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_KEEP_RATIO || screenCaptureMode == VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_ADJUSTED_IGNORE_RATIO)
    {
      setScreenCaptureMode(VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_MODE_SCALED_VIEWPORT);
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
      if (hasDifference)
      {
        if (remoteInterface != null && remoteInterface.isAsynchronousRepainterRunning() && !remoteInterface.isAsynchronousRepainterInterrupted())
        {
          remoteInterface.interruptAsynchronousRepainter();
        }
        remoteInterface.setImage(imageDataBuffer);
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
        // scrolled.setPreferredSize(scrolled.getSize());
        remoteInterface.repaint();
        scrolled.validate();
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
      hasRefresh = false;
      hasDifference = false;
    }
    // EventQueue.invokeLater(graphicsRefresher);
    // requestRefresh();
  }
  
  private void createCustomCursor()
  {
    int dpi = VTFontManager.BASE_FONT_DPI;
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
          //cursor = toolkit.createCustomCursor(cursorImage, center, "VT_GRAPHICSLINK_CROSSHAIR_CURSOR");
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
    createCustomCursor();
    VTDataMonitorMenu uploadMonitorPanel = null;
    VTDataMonitorMenu downloadMonitorPanel = null;
    try
    {
      if (VTMainConsole.isGraphical())
      {
        GraphicsDevice device = VTGraphicalDeviceResolver.getCurrentDevice(VTMainConsole.getFrame());
        if (device != null)
        {
          frame = new VTGraphicsLinkClientWriterFrame(device.getDefaultConfiguration());
        }
        else
        {
          frame = new VTGraphicsLinkClientWriterFrame();
        }
      }
      else
      {
        frame = new VTGraphicsLinkClientWriterFrame();
      }
      VTFontManager.registerWindow(frame);
      
      frame.setTitle("Variable-Terminal " + VTSystem.VT_VERSION + " - Client - Remote Graphics Link");
      BorderLayout frameLayout = new BorderLayout();
      frameLayout.setHgap(0);
      frameLayout.setVgap(0);
      frame.setLayout(frameLayout);
      frame.getInsets().set(0, 0, 0, 0);
      menuBar = new VTGraphicsLinkClientOptionsMenuBar(this, frame);
      if (session.getSession().getClient().getMonitorService() != null)
      {
        uploadMonitorPanel = new VTDataMonitorMenu(menuBar.getUploadMonitorMenu());
        downloadMonitorPanel = new VTDataMonitorMenu(menuBar.getDownloadMonitorMenu());
        session.getSession().getClient().getMonitorService().addUploadMonitorPanel(uploadMonitorPanel);
        session.getSession().getClient().getMonitorService().addDownloadMonitorPanel(downloadMonitorPanel);
      }
      scrolledMaybe = new VTGraphicsLinkClientWriterScrollPane(VTGraphicsLinkClientWriterScrollPane.SCROLLBARS_AS_NEEDED);
      scrolledMaybe.setBackground(Color.BLACK);
      //scrolledMaybe.setFocusable(false);
      scrolledMaybe.getInsets().set(0, 0, 0, 0);
      scrolledNever = new VTGraphicsLinkClientWriterScrollPane(VTGraphicsLinkClientWriterScrollPane.SCROLLBARS_NEVER);
      scrolledNever.setBackground(Color.BLACK);
      //scrolledNever.setFocusable(false);
      scrolledNever.getInsets().set(0, 0, 0, 0);
      scrolled = scrolledMaybe;
      // scrolled.setWheelScrollingEnabled(false);
      remoteInterface = new VTGraphicsLinkClientRemoteInterface(session.getSession().getExecutorService());
      remoteInterface.setScrollPane(scrolled);
      remoteInterface.setFocusTraversalKeysEnabled(false);
      // remoteInterface.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      if (cursor != null)
      {
        remoteInterface.setCursor(cursor);
      }
      remoteInterface.addFocusListener(focusListener);
      frame.addWindowListener(windowListener);
      session.getSession().getClipboardTransferTask().setEndingTask(new VTGraphicsLinkClientClipboardTransferEndingTask(this));
      session.getSession().getClipboardTransferTask().setInputStream(connection.getGraphicsClipboardDataInputStream());
      session.getSession().getClipboardTransferTask().setOutputStream(connection.getGraphicsClipboardDataOutputStream());
      keyListener.setInterrupted(false);
      keyListener.clearAllPressedKeys();
      remoteInterface.addKeyListener(keyListener);
      // remoteInterface.addMouseListener(mouseListener);
      remoteInterface.addMouseMotionListener(mouseMotionListener);
      // remoteInterface.addMouseWheelListener(mouseWheelListener);
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
      scrolled.add(remoteInterface);
      
      Rectangle screenSize = frame.getGraphicsConfiguration().getBounds();
      
      if (initialWidth > 0 && initialHeight > 0 && (initialWidth < screenSize.width) || (initialHeight < screenSize.height))
      {
        frame.pack();
        Insets frameInsets = frame.getInsets();
        Insets scrolledInsets = scrolled.getInsets();
        
        int totalWidth = initialWidth + frameInsets.left + frameInsets.right + scrolledInsets.left + scrolledInsets.right;
        int totalHeight = initialHeight + frameInsets.top + frameInsets.bottom + scrolledInsets.top + scrolledInsets.bottom;
        
        if ((totalWidth >= screenSize.width))
        {
          totalWidth = ((screenSize.width * 3) / 4);
        }
        
        if ((totalHeight >= screenSize.height))
        {
          totalHeight = ((screenSize.height * 3) / 4);
        }
        
        frame.setSize(totalWidth, totalHeight);
      }
      else
      {
        frame.setSize(((screenSize.width * 3) / 4), ((screenSize.height * 3) / 4));
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
      
      frame.getInsets().set(0, 0, 0, 0);
      scrolled.getInsets().set(0, 0, 0, 0);
      // remoteInterface.getin
      
      synchronized (reader)
      {
        open = true;
        reader.notify();
      }
      EventQueue.invokeLater(graphicsRefresher);
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
      synchronized (reader)
      {
        reader.setFailed(true);
        open = true;
        stopped = true;
        reader.notify();
      }
    }
    while (!stopped)
    {
      try
      {
        while (!stopped && (!needRefresh || ((terminalRefreshPolicy == TERMINAL_STATE_VISIBLE) && (((frame.getExtendedState() & Frame.ICONIFIED)) != 0)) || ((terminalRefreshPolicy == TERMINAL_STATE_FOCUSED) && !remoteInterface.isFocusOwner())))
        {
          synchronized (this)
          {
            wait();
          }
        }
        if (!stopped)
        {
          needRefresh = false;
          EventQueue.invokeLater(graphicsRefresher);
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
    if (uploadMonitorPanel != null && session.getSession().getClient().getMonitorService() != null)
    {
      session.getSession().getClient().getMonitorService().removeUploadMonitorPanel(uploadMonitorPanel);
    }
    if (downloadMonitorPanel != null && session.getSession().getClient().getMonitorService() != null)
    {
      session.getSession().getClient().getMonitorService().removeDownloadMonitorPanel(downloadMonitorPanel);
    }
    if (frame != null)
    {
      frame.setVisible(false);
      frame.dispose();
    }
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