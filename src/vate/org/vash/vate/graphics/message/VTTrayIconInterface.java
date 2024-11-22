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
  @SuppressWarnings("unused")
  private Method isSupportedMethod;
  private Method getSystemTrayMethod;
  private Method addMethod;
  private Method removeMethod;
  
  private Method addMouseListenerMethod;
  private Method addActionListenerMethod;
  private Method setImageAutoSizeMethod;
  private Method displayMessageMethod;
  
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
      isSupportedMethod = systemTrayClass.getDeclaredMethod("isSupported");
      
      supported = false;
      //supported = ((Boolean) isSupported.invoke(null));
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
    isSupportedMethod = null;
    
    getSystemTrayMethod = null;
    addMethod = null;
    removeMethod = null;
    
    addMouseListenerMethod = null;
    addActionListenerMethod = null;
    setImageAutoSizeMethod = null;
    displayMessageMethod = null;
    
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
      
      getSystemTrayMethod = systemTrayClass.getDeclaredMethod("getSystemTray");
      addMethod = systemTrayClass.getMethod("add", trayIconClass);
      removeMethod = systemTrayClass.getMethod("remove", trayIconClass);
      
      addMouseListenerMethod = trayIconClass.getMethod("addMouseListener", MouseListener.class);
      addActionListenerMethod = trayIconClass.getMethod("addActionListener", ActionListener.class);
      setImageAutoSizeMethod = trayIconClass.getMethod("setImageAutoSize", boolean.class);
      displayMessageMethod = trayIconClass.getMethod("displayMessage", String.class, String.class, messageTypeClass);
      
      systemTrayObject = getSystemTrayMethod.invoke(null);
      trayIconObject = trayIconClass.getConstructor(Class.forName("java.awt.Image"), String.class).newInstance(frame.getIconImage(), tooltip);
      
      addMouseListenerMethod.invoke(trayIconObject, new MouseListener()
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
      
      addActionListenerMethod.invoke(trayIconObject, new ActionListener()
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
      
      setImageAutoSizeMethod.invoke(trayIconObject, true);
      
      addMethod.invoke(systemTrayObject, trayIconObject);
      
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
      if (displayMessageMethod != null)
      {
        displayMessageMethod.invoke(trayIconObject, caption, text, messageTypeObject);
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
      if (removeMethod != null)
      {
        removeMethod.invoke(systemTrayObject, trayIconObject);
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
