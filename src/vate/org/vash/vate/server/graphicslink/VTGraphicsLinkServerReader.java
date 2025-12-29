package org.vash.vate.server.graphicslink;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.Clipboard;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.vash.vate.VTSystem;
import org.vash.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vash.vate.graphics.clipboard.VTEmptyTransferable;
import org.vash.vate.graphics.control.VTAWTControlEvent;
import org.vash.vate.graphics.control.VTAWTControlProvider;
import org.vash.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vash.vate.graphics.image.VTRectangle;
import org.vash.vate.server.connection.VTServerConnection;

public class VTGraphicsLinkServerReader implements Runnable
{
  private volatile boolean stopped;
  private boolean readOnly;
  private VTServerConnection connection;
  private VTGraphicsLinkServerSession session;
  private VTAWTControlEvent lastEvent;
  private VTAWTControlProvider controlProvider;
  private VTGraphicsLinkServerWriter writer;
  private Clipboard systemClipboard;
  private Set<Integer> pressedKeyboardKeys;
  private Set<Integer> pressedMouseKeys;
  
  public VTGraphicsLinkServerReader(VTGraphicsLinkServerSession session)
  {
    this.session = session;
    this.connection = session.getSession().getConnection();
    this.controlProvider = session.getSession().getControlProvider();
    this.pressedKeyboardKeys = new LinkedHashSet<Integer>();
    this.pressedMouseKeys = new LinkedHashSet<Integer>();
    this.stopped = true;
    this.systemClipboard = controlProvider.getSystemClipboard();
  }
  
  public void setWriter(VTGraphicsLinkServerWriter writer)
  {
    this.writer = writer;
  }
  
  public void dispose()
  {
    stopped = true;
    // failed = false;
    if (controlProvider != null)
    {
      controlProvider.dispose();
    }
  }
  
  public boolean isStopped()
  {
    return stopped;
  }
  
  public void setStopped(boolean stopped)
  {
    this.stopped = stopped;
  }
  
  public boolean isReadOnly()
  {
    return readOnly;
  }
  
  public void setReadOnly(boolean readOnly)
  {
    this.readOnly = readOnly;
  }
  
  public void releaseAllPressedKeys()
  {
    for (Integer key : pressedKeyboardKeys)
    {
      controlProvider.keyRelease(key, 0, 0, ' ');
    }
    pressedKeyboardKeys.clear();
    for (Integer key : pressedMouseKeys)
    {
      controlProvider.mouseRelease(key);
    }
    pressedMouseKeys.clear();
  }
  
  public void run()
  {
    lastEvent = new VTAWTControlEvent();
    /* if (!readOnly) { */
    session.getSession().getClipboardTransferTask().setInputStream(connection.getGraphicsClipboardDataInputStream());
    session.getSession().getClipboardTransferTask().setOutputStream(connection.getGraphicsClipboardDataOutputStream());
    while (!stopped)
    {
      try
      {
        switch (connection.getGraphicsControlDataInputStream().read())
        {
          case VTSystem.VT_GRAPHICS_LINK_MOUSE_INPUT_MOVE:
          {
            lastEvent.x = connection.getGraphicsControlDataInputStream().readInt();
            lastEvent.y = connection.getGraphicsControlDataInputStream().readInt();
            if (writer.isScaling())
            {
              double scaleX = writer.getScaleFactorX();
              double scaleY = writer.getScaleFactorY();
              controlProvider.mouseMove((int) Math.round(lastEvent.x / scaleX), (int) Math.round(lastEvent.y / scaleY));
            }
            else
            {
              controlProvider.mouseMove(lastEvent.x, lastEvent.y);
            }
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_MOUSE_INPUT_KEY_DOWN:
          {
            lastEvent.button = connection.getGraphicsControlDataInputStream().readInt();
            controlProvider.mousePress(lastEvent.button);
            pressedMouseKeys.add(lastEvent.button);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_MOUSE_INPUT_KEY_UP:
          {
            lastEvent.button = connection.getGraphicsControlDataInputStream().readInt();
            controlProvider.mouseRelease(lastEvent.button);
            pressedMouseKeys.remove(lastEvent.button);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_MOUSE_INPUT_WHEEL:
          {
            lastEvent.wheel = connection.getGraphicsControlDataInputStream().readInt();
            controlProvider.mouseWheel(lastEvent.wheel);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_KEYBOARD_INPUT_KEY_DOWN:
          {
            lastEvent.keyCode = connection.getGraphicsControlDataInputStream().readInt();
            lastEvent.keyModifiers = connection.getGraphicsControlDataInputStream().readInt();
            lastEvent.keyLocation = connection.getGraphicsControlDataInputStream().readInt();
            lastEvent.keyChar = connection.getGraphicsControlDataInputStream().readChar();
            controlProvider.keyPress(lastEvent.keyCode, lastEvent.keyModifiers, lastEvent.keyLocation, lastEvent.keyChar);
            pressedKeyboardKeys.add(lastEvent.keyCode);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_KEYBOARD_INPUT_KEY_UP:
          {
            lastEvent.keyCode = connection.getGraphicsControlDataInputStream().readInt();
            lastEvent.keyModifiers = connection.getGraphicsControlDataInputStream().readInt();
            lastEvent.keyLocation = connection.getGraphicsControlDataInputStream().readInt();
            lastEvent.keyChar = connection.getGraphicsControlDataInputStream().readChar();
            controlProvider.keyRelease(lastEvent.keyCode, lastEvent.keyModifiers, lastEvent.keyLocation, lastEvent.keyChar);
            pressedKeyboardKeys.remove(lastEvent.keyCode);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_KEYBOARD_LOCK_KEY_STATE_ON:
          {
            lastEvent.keyCode = connection.getGraphicsControlDataInputStream().readInt();
            // if
            // (!controlProvider.getLockingKeyState(lastEvent.keyCode))
            // {
            controlProvider.setLockingKeyState(lastEvent.keyCode, true);
            // }
            // controlProvider.keyRelease(lastEvent.keyCode);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_KEYBOARD_LOCK_KEY_STATE_OFF:
          {
            lastEvent.keyCode = connection.getGraphicsControlDataInputStream().readInt();
            // if
            // (controlProvider.getLockingKeyState(lastEvent.keyCode))
            // {
            controlProvider.setLockingKeyState(lastEvent.keyCode, false);
            // }
            // controlProvider.keyRelease(lastEvent.keyCode);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_ANY_INPUT_RELEASE_ALL_PRESSED_KEYS:
          {
            releaseAllPressedKeys();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_REQUEST:
          {
            writer.requestRefresh();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_CLEAR_REQUEST:
          {
            writer.requestClear();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_16:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_216:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_32768:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_32768);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_16777216:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16777216);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_4:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_8:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_8);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_64:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_64);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_512:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_512);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_4096:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4096);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_125:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_125);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_27:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_27);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_262144:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_262144);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_COLOR_QUALITY_2097152:
          {
            writer.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_2097152);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_MODE_ASYNCHRONOUS:
          {
            writer.setRefreshInterrupted(false);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_MODE_SYNCHRONOUS:
          {
            writer.setRefreshInterrupted(false);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_DRAW_POINTER_ON:
          {
            writer.setDrawPointer(true);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_DRAW_POINTER_OFF:
          {
            writer.setDrawPointer(false);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_INTERVAL_CHANGE:
          {
            writer.setScreenCaptureInterval(connection.getGraphicsControlDataInputStream().readInt());
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_MODE_INTERRUPTED:
          {
            writer.setRefreshInterrupted(true);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_CLIPBOARD_TRANSFER_SEND_REQUEST:
          {
            session.getSession().getClipboardTransferTask().joinThread();
            session.getSession().getClipboardTransferTask().setSending(false);
            session.getSession().getClipboardTransferTask().startThread();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_CLIPBOARD_TRANSFER_RECEIVE_REQUEST:
          {
            session.getSession().getClipboardTransferTask().joinThread();
            session.getSession().getClipboardTransferTask().setSending(true);
            session.getSession().getClipboardTransferTask().startThread();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_CLIPBOARD_TRANSFER_CANCEL_REQUEST:
          {
            session.getSession().getConnection().closeClipboardStreams();
            session.getSession().getClipboardTransferTask().interruptThread();
            session.getSession().getClipboardTransferTask().joinThread();
            session.getSession().getConnection().resetClipboardStreams();
            session.getSession().getClipboardTransferTask().setInputStream(connection.getGraphicsClipboardDataInputStream());
            session.getSession().getClipboardTransferTask().setOutputStream(connection.getGraphicsClipboardDataOutputStream());
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_CLIPBOARD_CLEAR_REQUEST:
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
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_CAPTURE_AREA_CHANGE:
          {
            int x = connection.getGraphicsControlDataInputStream().readInt();
            int y = connection.getGraphicsControlDataInputStream().readInt();
            int width = connection.getGraphicsControlDataInputStream().readInt();
            int height = connection.getGraphicsControlDataInputStream().readInt();
            double captureScale = connection.getGraphicsControlDataInputStream().readDouble();
            VTRectangle area = new VTRectangle();
            area.x = x;
            area.y = y;
            area.width = width;
            area.height = height;
            writer.setCaptureArea(area, captureScale);
            // System.out.println("VT_GRAPHICS_LINK_GRAPHICS_CAPTURE_AREA_CHANGE:
            // " + area.x + " " + area.y + " " + area.width + " " +
            // area.height);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_SELECT_DEVICE_DEFAULT:
          {
            GraphicsDevice defaultDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            controlProvider.setGraphicsDevice(defaultDevice);
            writer.setNextDevice(defaultDevice);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_SELECT_DEVICE_UNIFIED:
          {
            controlProvider.setGraphicsDevice(null);
            writer.setNextDevice(null);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_SELECT_DEVICE_NEXT:
          {
            GraphicsDevice currentDevice = controlProvider.getGraphicsDevice();
            if (currentDevice != null)
            {
              GraphicsDevice[] devices = VTGraphicalDeviceResolver.getRasterDevices();
              GraphicsDevice nextDevice = null;
              int deviceNumber = -1;
              while (devices.length > 1 && nextDevice == null)
              {
                deviceNumber++;
                deviceNumber = deviceNumber % devices.length;
                nextDevice = devices[deviceNumber];
                if (nextDevice != null && nextDevice.getIDstring().equals(currentDevice.getIDstring()))
                {
                  deviceNumber++;
                  deviceNumber = deviceNumber % devices.length;
                  nextDevice = devices[deviceNumber];
                  break;
                }
              }
              if (nextDevice != null)
              {
                controlProvider.setGraphicsDevice(nextDevice);
                writer.setNextDevice(nextDevice);
              }
            }
            else
            {
              GraphicsDevice defaultDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
              controlProvider.setGraphicsDevice(defaultDevice);
              writer.setNextDevice(defaultDevice);
            }
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_SELECT_DEVICE_PREVIOUS:
          {
            GraphicsDevice currentDevice = controlProvider.getGraphicsDevice();
            if (currentDevice != null)
            {
              GraphicsDevice[] devices = VTGraphicalDeviceResolver.getRasterDevices();
              GraphicsDevice nextDevice = null;
              int deviceNumber = -1;
              while (devices.length > 1 && nextDevice == null)
              {
                deviceNumber++;
                deviceNumber = deviceNumber % devices.length;
                nextDevice = devices[deviceNumber];
                if (nextDevice != null && nextDevice.getIDstring().equals(currentDevice.getIDstring()))
                {
                  deviceNumber--;
                  if (deviceNumber < 0)
                  {
                    deviceNumber = devices.length - 1;
                  }
                  nextDevice = devices[deviceNumber];
                  break;
                }
              }
              if (nextDevice != null)
              {
                controlProvider.setGraphicsDevice(nextDevice);
                writer.setNextDevice(nextDevice);
              }
            }
            else
            {
              GraphicsDevice defaultDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
              controlProvider.setGraphicsDevice(defaultDevice);
              writer.setNextDevice(defaultDevice);
            }
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_ZSD:
          {
            writer.setImageCoding(VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_ZSD);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_PNG:
          {
            writer.setImageCoding(VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_PNG);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_JPG:
          {
            writer.setImageCoding(VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_JPG);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_GZD:
          {
            writer.setImageCoding(VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_GZD);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_DRAW_POINTER_INCREASE:
          {
            writer.increaseDrawPointer();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_DRAW_POINTER_DECREASE:
          {
            writer.decreaseDrawPointer();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_DRAW_POINTER_NORMALIZE:
          {
            writer.normalizeDrawPointer();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_SESSION_FINISHED:
          {
            stopped = true;
            break;
          }
          default:
          {
            stopped = true;
            // SyncError!
            break;
          }
        }
      }
      catch (IOException e)
      {
        // e.printStackTrace();
        // e.printStackTrace(VTConsole.getSystemOut());
        stopped = true;
        break;
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        stopped = true;
        break;
        // e.printStackTrace(VTConsole.getSystemOut());
        // e.printStackTrace(VTTerminal.getSystemOut());
        // Non-supported key found!
        // System.out.println("KeyCode: " + lastEvent.keyCode);
        // e.printStackTrace();
      }
    }
    releaseAllPressedKeys();
    synchronized (session)
    {
      session.notify();
    }
  }
}