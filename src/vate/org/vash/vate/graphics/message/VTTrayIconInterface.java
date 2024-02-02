package org.vash.vate.graphics.message;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;

import org.vash.vate.runtime.VTRuntimeExit;

public class VTTrayIconInterface
{
  private Method isSupported;
  private Method getSystemTray;
  private Method add;
  private Method remove;
  
  private Method addMouseListener;
  private Method addActionListener;
  private Method setImageAutoSize;
  private Method displayMessage;
  
  private Object systemTrayObject;
  private Object trayIconObject;
  private Object messageTypeObject; 
  
  private Runnable hook;
  
  private boolean supported = false;
  
  public VTTrayIconInterface()
  {
    try
    {
      Class<?> systemTrayClass = Class.forName("java.awt.SystemTray");
      isSupported = systemTrayClass.getDeclaredMethod("isSupported");
      
      supported = ((Boolean) isSupported.invoke(null));
    }
    catch (Throwable t)
    {
      
    }
    
    if (!isSupported())
    {
      return;
    }
    
    hook = new Runnable()
    {
      public void run()
      {
        remove();
      }
    };
  }
  
  public void reset()
  {
    isSupported = null;
    
    getSystemTray = null;
    add = null;
    remove = null;
    
    addMouseListener = null;
    addActionListener = null;
    setImageAutoSize = null;
    displayMessage = null;
    
    systemTrayObject = null;
    trayIconObject = null;
    messageTypeObject = null;
  }
  
  public boolean isSupported()
  {
    return supported;
  }
  
  public void install(final Frame frame, String tooltip)
  {
    if (!isSupported())
    {
      return;
    }
    
    reset();
    
    try
    {
      Class<?> systemTrayClass = Class.forName("java.awt.SystemTray");
      Class<?> trayIconClass = Class.forName("java.awt.TrayIcon");
      Class<?> messageTypeClass = null;
      Class<?> memberClasses[] = trayIconClass.getClasses();
      
      for (Class<?> memberClass : memberClasses)
      {
        if (memberClass.getSimpleName().contains("MessageType"))
        {
          messageTypeClass = memberClass;
          messageTypeObject = messageTypeClass.getDeclaredMethod("valueOf", String.class).invoke(null, "NONE");
        }
      }
      
      getSystemTray = systemTrayClass.getDeclaredMethod("getSystemTray");
      add = systemTrayClass.getMethod("add", trayIconClass);
      remove = systemTrayClass.getMethod("remove", trayIconClass);
      
      addMouseListener = trayIconClass.getMethod("addMouseListener", MouseListener.class);
      addActionListener = trayIconClass.getMethod("addActionListener", ActionListener.class);
      setImageAutoSize = trayIconClass.getMethod("setImageAutoSize", boolean.class);
      displayMessage = trayIconClass.getMethod("displayMessage", String.class, String.class, messageTypeClass);
      
      systemTrayObject = getSystemTray.invoke(null);
      trayIconObject = trayIconClass.getConstructor(Class.forName("java.awt.Image"), String.class).newInstance(frame.getIconImage(), tooltip);
      
      addMouseListener.invoke(trayIconObject, new MouseListener()
      {
        public void mouseClicked(MouseEvent e)
        {
          if (!frame.isVisible())
          {
            frame.setVisible(true);
            frame.toFront();
            return;
          }
          if ((frame.getExtendedState() & Frame.ICONIFIED) != 0)
          {
            frame.setExtendedState(frame.getExtendedState() ^ Frame.ICONIFIED);
            frame.toFront();
          }
          else
          {
            frame.setExtendedState(frame.getExtendedState() ^ Frame.ICONIFIED);
          }
        }
        
        public void mousePressed(MouseEvent e)
        {
          
        }
        
        public void mouseReleased(MouseEvent e)
        {
          
        }
        
        public void mouseEntered(MouseEvent e)
        {
          
        }
        
        public void mouseExited(MouseEvent e)
        {
          
        }
      });
      
      addActionListener.invoke(trayIconObject, new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          if (!frame.isVisible())
          {
            frame.setVisible(true);
          }
          if ((frame.getExtendedState() & Frame.ICONIFIED) != 0)
          {
            frame.setExtendedState(frame.getExtendedState() ^ Frame.ICONIFIED);
          }
          frame.toFront();
        }
      });
      
      setImageAutoSize.invoke(trayIconObject, true);
      
      add.invoke(systemTrayObject, trayIconObject);
      
      VTRuntimeExit.addHook(hook);
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
    }
  }
  
  public void displayMessage(String caption, String text)
  {
    try
    {
      if (displayMessage != null)
      {
        displayMessage.invoke(trayIconObject, caption, text, messageTypeObject);
      }
      // if (trayIcon != null)
      // {
      // trayIcon.displayMessage(caption, text, MessageType.NONE);
      // }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void remove()
  {
    try
    {
      VTRuntimeExit.removeHook(hook);
      if (remove != null)
      {
        remove.invoke(systemTrayObject, trayIconObject);
      }
      // if (trayIcon != null)
      // {
      // systemTray.remove(trayIcon);
      // }
      
    }
    catch (Throwable t)
    {
      
    }
    
    reset();
  }
}
