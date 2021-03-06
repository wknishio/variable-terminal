package org.vate.graphics.message;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;

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

  // private TrayIcon trayIcon;
  // private SystemTray systemTray;

  public VTTrayIconInterface()
  {

  }

  public void install(final Frame frame, String tooltip)
  {

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

      isSupported = systemTrayClass.getDeclaredMethod("isSupported");
      getSystemTray = systemTrayClass.getDeclaredMethod("getSystemTray");
      add = systemTrayClass.getMethod("add", trayIconClass);
      remove = systemTrayClass.getMethod("remove", trayIconClass);

      addMouseListener = trayIconClass.getMethod("addMouseListener", MouseListener.class);
      addActionListener = trayIconClass.getMethod("addActionListener", ActionListener.class);
      setImageAutoSize = trayIconClass.getMethod("setImageAutoSize", boolean.class);
      displayMessage = trayIconClass.getMethod("displayMessage", String.class, String.class, messageTypeClass);

      if (!((Boolean) isSupported.invoke(null)))
      {
        return;
      }

      systemTrayObject = getSystemTray.invoke(null);
      trayIconObject = trayIconClass.getConstructor(Class.forName("java.awt.Image"), String.class).newInstance(frame.getIconImage(), tooltip);

      addMouseListener.invoke(trayIconObject, new MouseListener()
      {
        public void mouseClicked(MouseEvent e)
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

//			if (!SystemTray.isSupported())
//			{
//				return;
//			}
//			systemTray = SystemTray.getSystemTray();
//			trayIcon = new TrayIcon(frame.getIconImage(), tooltip);
//			
//			trayIcon.addMouseListener(new MouseListener()
//			{
//				public void mouseClicked(MouseEvent e)
//				{
//					if (!frame.isVisible())
//					{
//						frame.setVisible(true);
//					}
//					if ((frame.getExtendedState() & Frame.ICONIFIED) != 0)
//					{
//						frame.setExtendedState(frame.getExtendedState() ^ Frame.ICONIFIED);
//					}
//					frame.toFront();
//				}
//				
//				public void mousePressed(MouseEvent e)
//				{
//					
//				}
//				
//				public void mouseReleased(MouseEvent e)
//				{
//					
//				}
//				
//				public void mouseEntered(MouseEvent e)
//				{
//					
//				}
//				
//				public void mouseExited(MouseEvent e)
//				{
//					
//				}
//			});
//			
//			trayIcon.addActionListener(new ActionListener()
//			{
//				public void actionPerformed(ActionEvent e)
//				{
//					if (!frame.isVisible())
//					{
//						frame.setVisible(true);
//					}
//					if ((frame.getExtendedState() & Frame.ICONIFIED) != 0)
//					{
//						frame.setExtendedState(frame.getExtendedState() ^ Frame.ICONIFIED);
//					}
//					frame.toFront();
//				}
//			});
//			
//			trayIcon.setImageAutoSize(true);
//			
//			systemTray.add(trayIcon);

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
      // if (trayIcon != null)
      // {
      // trayIcon.displayMessage(caption, text, MessageType.NONE);
      // }
      displayMessage.invoke(trayIconObject, caption, text, messageTypeObject);
    }
    catch (Throwable t)
    {

    }
  }

  public void removeTrayIcon()
  {
    try
    {
      // if (trayIcon != null)
      // {
      // systemTray.remove(trayIcon);
      // }
      remove.invoke(systemTrayObject, trayIconObject);
    }
    catch (Throwable t)
    {

    }
  }
}
