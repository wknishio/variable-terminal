package org.vash.vate.graphics.message;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;

import org.vash.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vash.vate.graphics.font.VTFontManager;
import org.vash.vate.graphics.image.VTIconDisplay;
import org.vash.vate.reflection.VTReflectionUtils;

public class VTGraphicsMessager
{
  private static BufferedImage warningIcon16;
  private static BufferedImage warningIcon32;
  // private static BufferedImage displayIcon;
  private static Method setIconImageMethod;
  //private static ConcurrentMap<GraphicsDevice, Dialog> dialogs = new ConcurrentHashMap<GraphicsDevice, Dialog>();
  
  static
  {
    try
    {
      warningIcon16 = ImageIO.read(VTGraphicsMessager.class.getResourceAsStream("/org/vash/vate/graphics/message/resource/warning16.png"));
      warningIcon32 = ImageIO.read(VTGraphicsMessager.class.getResourceAsStream("/org/vash/vate/graphics/message/resource/warning32.png"));
      // displayIcon =
      // ImageIO.read(VTGraphicsMessager.class.getResourceAsStream("/org/vash/vate/graphics/message/resource/warning.png"));
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      setIconImageMethod = Dialog.class.getMethod("setIconImage", Class.forName("java.awt.Image"));
      // setIconImage.setAccessible(true);
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public static void dispose()
  {
    if (warningIcon16 != null)
    {
      warningIcon16.flush();
      warningIcon16 = null;
    }
    if (warningIcon32 != null)
    {
      warningIcon32.flush();
      warningIcon32 = null;
    }
  }
  
  public static Dialog showAlert(Window parent, String title, String message)
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return null;
    }
    return showAlert(null, parent, title, message);
  }
  
  public static Dialog showAlert(GraphicsDevice device, Window parent, String title, String message)
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return null;
    }
    try
    {
      if (parent != null && parent instanceof Dialog && parent.isDisplayable())
      {
        return showAlert(device, (Dialog) parent, title, message);
      }
      if (device == null && parent != null)
      {
        device = VTGraphicalDeviceResolver.getCurrentDevice(parent);
      }
      if (device == null)
      {
        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      }
      
//      final Frame frame = (parent != null && parent instanceof Frame ? (Frame) parent : new Frame(device.getDefaultConfiguration()));
//      if (parent == null || !(parent instanceof Frame))
//      {
//        frame.setIconImage(warningIcon16);
//        frame.setUndecorated(true);
//      }
      
      int locationX = Math.max((device.getDisplayMode().getWidth() / 2), 0);
      int locationY = Math.max((device.getDisplayMode().getHeight() / 2), 0);
      final Frame frame = new Frame(device.getDefaultConfiguration());
      frame.setIconImage(warningIcon16);
      frame.setUndecorated(true);
      frame.setLocation(locationX, locationY);
      
      // invisible.setUndecorated(true);
      // JOptionPane pane = new JOptionPane(message,
      // JOptionPane.WARNING_MESSAGE,
      // JOptionPane.DEFAULT_OPTION);
      // pane.createDialog(invisible, title);
      
      final Dialog dialog = new Dialog(frame, false);
      try
      {
        if (setIconImageMethod != null)
        {
          setIconImageMethod.invoke(dialog, warningIcon16);
        }
      }
      catch (Throwable e)
      {
        
      }
      
      // int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
      dialog.setTitle(title);
      dialog.setLocationByPlatform(true);
      
      // Panel main = new Panel();
      // main.setLayout(new GridLayout(2, 1));
      float fontScaling = VTFontManager.FONT_SCALING_FACTOR_MONOSPACED;
      int imageScaling = (int)Math.ceil(2 * fontScaling);
      
      Panel p0 = new Panel();
      p0.setLayout(new BorderLayout());
      
      Panel p1 = new Panel();
      p1.setLayout(new BorderLayout());
      
      VTIconDisplay display = new VTIconDisplay();
      display.setFocusable(false);
      display.setImage(warningIcon32, 16 * imageScaling, 16 * imageScaling);
      
      Label messageLabel = new Label(message);
      Label titleLabel = new Label(title);
      
      messageLabel.setAlignment(Label.CENTER);
      titleLabel.setAlignment(Label.CENTER);
      
      messageLabel.setFocusable(false);
      titleLabel.setFocusable(false);
      
      p1.add(titleLabel, BorderLayout.NORTH);
      p1.add(display, BorderLayout.CENTER);
      p1.add(messageLabel, BorderLayout.SOUTH);
      Panel p2 = new Panel();
      final Button button = new Button(" OK ");
      dialog.addKeyListener(new KeyListener()
      {
        private boolean pressed = false;
        
        public void keyPressed(KeyEvent e)
        {
          if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE)
          {
            pressed = true;
            // button.dispatchEvent(new KeyEvent(e.getComponent(),
            // e.getID(), e.getWhen(),
            // e.getModifiersEx(), KeyEvent.VK_SPACE,
            // e.getKeyChar(), e.getKeyLocation()));
            // e.getID() == KeyEvent.key_p
          }
        }
        
        public void keyReleased(KeyEvent e)
        {
          if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE)
          {
            if (pressed)
            {
              VTFontManager.unregisterWindow(dialog);
              dialog.setVisible(false);
              dialog.dispose();
              if (frame.isUndecorated())
              {
                frame.dispose();
              }
            }
            // pressed = false;
            // button.dispatchEvent(new KeyEvent(e.getComponent(),
            // e.getID(), e.getWhen(),
            // e.getModifiersEx(), KeyEvent.VK_SPACE,
            // e.getKeyChar(), e.getKeyLocation()));
          }
        }
        
        public void keyTyped(KeyEvent e)
        {
          /*
           * if (e.getKeyChar() == '\n') { button.dispatchEvent(new
           * KeyEvent(button, e.getID(), e.getWhen(), e.getModifiersEx(),
           * e.getKeyCode(), ' ', e.getKeyLocation())); }
           */
        }
      });
      button.addKeyListener(new KeyListener()
      {
        private boolean pressed = false;
        
        public void keyPressed(KeyEvent e)
        {
          if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE)
          {
            pressed = true;
            // button.dispatchEvent(new KeyEvent(e.getComponent(),
            // e.getID(), e.getWhen(),
            // e.getModifiersEx(), KeyEvent.VK_SPACE,
            // e.getKeyChar(), e.getKeyLocation()));
            // e.getID() == KeyEvent.key_p
          }
        }
        
        public void keyReleased(KeyEvent e)
        {
          if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE)
          {
            if (pressed)
            {
              VTFontManager.unregisterWindow(dialog);
              dialog.setVisible(false);
              dialog.dispose();
              if (frame.isUndecorated())
              {
                frame.dispose();
              }
            }
            // pressed = false;
            // button.dispatchEvent(new KeyEvent(e.getComponent(),
            // e.getID(), e.getWhen(),
            // e.getModifiersEx(), KeyEvent.VK_SPACE,
            // e.getKeyChar(), e.getKeyLocation()));
          }
        }
        
        public void keyTyped(KeyEvent e)
        {
          /*
           * if (e.getKeyChar() == '\n') { button.dispatchEvent(new
           * KeyEvent(button, e.getID(), e.getWhen(), e.getModifiersEx(),
           * e.getKeyCode(), ' ', e.getKeyLocation())); }
           */
        }
      });
      button.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          VTFontManager.unregisterWindow(dialog);
          dialog.setVisible(false);
          dialog.dispose();
          if (frame.isUndecorated())
          {
            frame.dispose();
          }
        }
      });
      button.setFocusable(true);
      p2.add(button);
      // main.add(p1);
      // main.add(p2);
      // dialog.setFocusable(false);
      p0.add(p1, BorderLayout.CENTER);
      p0.add(p2, BorderLayout.SOUTH);
      dialog.add(p0);
      dialog.setModal(false);
      //dialog.setAlwaysOnTop(true);
      
      try
      {
        if (setIconImageMethod != null)
        {
          setIconImageMethod.invoke(dialog, warningIcon16);
        }
      }
      catch (Throwable t)
      {
        
      }
      VTFontManager.registerWindow(dialog);
      dialog.setResizable(true);
      dialog.pack();
      // dialog.setUndecorated(true);
      // dialog.setMinimumSize(dialog.getSize());
      dialog.setMaximumSize(dialog.getSize());
      dialog.setLocationRelativeTo(frame);
      
      //dialog.setLocationRelativeTo(frame);
      
      dialog.addWindowListener(new WindowListener()
      {
        public void windowActivated(WindowEvent e)
        {
          button.requestFocusInWindow();
        }
        
        public void windowClosed(WindowEvent e)
        {
          
        }
        
        public void windowClosing(WindowEvent e)
        {
          VTFontManager.unregisterWindow(dialog);
          dialog.setVisible(false);
          dialog.dispose();
          if (frame.isUndecorated())
          {
            frame.dispose();
          }
        }
        
        public void windowDeactivated(WindowEvent e)
        {
          
        }
        
        public void windowDeiconified(WindowEvent e)
        {
          // dialog.set
          button.requestFocusInWindow();
        }
        
        public void windowIconified(WindowEvent e)
        {
          // dialog.setVisible(true);
          // dialog.toFront();
        }
        
        public void windowOpened(WindowEvent e)
        {
          button.requestFocusInWindow();
        }
      });
      dialog.getToolkit().beep();
      dialog.setAlwaysOnTop(true);
      dialog.setVisible(true);
      dialog.toFront();
      dialog.requestFocus();
      // dialog.toFront();
      // dialog.dispose();
      // invisible.dispose();
      // pane = null;
      // dialog = null;
      // invisible = null;
      return dialog;
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    return null;
  }
  
  private static Dialog showAlert(GraphicsDevice device, Dialog parent, String title, String message)
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return null;
    }
    try
    {
      if (device == null && parent != null)
      {
        device = VTGraphicalDeviceResolver.getCurrentDevice(parent);
      }
      if (device == null)
      {
        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      }
      
      int locationX = Math.max((device.getDisplayMode().getWidth() / 2), 0);
      int locationY = Math.max((device.getDisplayMode().getHeight() / 2), 0);
      final Frame frame = new Frame(device.getDefaultConfiguration());
      frame.setIconImage(warningIcon16);
      frame.setUndecorated(true);
      frame.setLocation(locationX, locationY);
      
      final Dialog dialog = new Dialog(frame, false);
      try
      {
        if (setIconImageMethod != null)
        {
          setIconImageMethod.invoke(dialog, warningIcon16);
        }
      }
      catch (Throwable e)
      {
        
      }
      
      // int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
      dialog.setTitle(title);
      dialog.setLocationByPlatform(true);
      
      // Panel main = new Panel();
      // main.setLayout(new GridLayout(2, 1));
      float fontScaling = VTFontManager.FONT_SCALING_FACTOR_MONOSPACED;
      int imageScaling = (int)Math.ceil(2 * fontScaling);
      
      Panel p1 = new Panel();
      p1.setLayout(new BorderLayout());
      
      VTIconDisplay display = new VTIconDisplay();
      display.setFocusable(false);
      display.setImage(warningIcon32, 16 * imageScaling, 16 * imageScaling);
      
      Label messageLabel = new Label(message);
      Label titleLabel = new Label(title);
      
      messageLabel.setAlignment(Label.CENTER);
      titleLabel.setAlignment(Label.CENTER);
      
      messageLabel.setFocusable(false);
      titleLabel.setFocusable(false);
      
      p1.add(titleLabel, BorderLayout.NORTH);
      p1.add(display, BorderLayout.CENTER);
      p1.add(messageLabel, BorderLayout.SOUTH);
      Panel p2 = new Panel();
      final Button button = new Button(" OK ");
      dialog.addKeyListener(new KeyListener()
      {
        private boolean pressed = false;
        
        public void keyPressed(KeyEvent e)
        {
          if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE)
          {
            pressed = true;
            // button.dispatchEvent(new KeyEvent(e.getComponent(),
            // e.getID(), e.getWhen(),
            // e.getModifiersEx(), KeyEvent.VK_SPACE,
            // e.getKeyChar(), e.getKeyLocation()));
            // e.getID() == KeyEvent.key_p
          }
        }
        
        public void keyReleased(KeyEvent e)
        {
          if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE)
          {
            if (pressed)
            {
              VTFontManager.unregisterWindow(dialog);
              dialog.setVisible(false);
              dialog.dispose();
              if (frame.isUndecorated())
              {
                frame.dispose();
              }
            }
            // pressed = false;
            // button.dispatchEvent(new KeyEvent(e.getComponent(),
            // e.getID(), e.getWhen(),
            // e.getModifiersEx(), KeyEvent.VK_SPACE,
            // e.getKeyChar(), e.getKeyLocation()));
          }
        }
        
        public void keyTyped(KeyEvent e)
        {
          /*
           * if (e.getKeyChar() == '\n') { button.dispatchEvent(new
           * KeyEvent(button, e.getID(), e.getWhen(), e.getModifiersEx(),
           * e.getKeyCode(), ' ', e.getKeyLocation())); }
           */
        }
      });
      button.addKeyListener(new KeyListener()
      {
        private boolean pressed = false;
        
        public void keyPressed(KeyEvent e)
        {
          if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE)
          {
            pressed = true;
            // button.dispatchEvent(new KeyEvent(e.getComponent(),
            // e.getID(), e.getWhen(),
            // e.getModifiersEx(), KeyEvent.VK_SPACE,
            // e.getKeyChar(), e.getKeyLocation()));
            // e.getID() == KeyEvent.key_p
          }
        }
        
        public void keyReleased(KeyEvent e)
        {
          if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE)
          {
            if (pressed)
            {
              VTFontManager.unregisterWindow(dialog);
              dialog.setVisible(false);
              dialog.dispose();
              if (frame.isUndecorated())
              {
                frame.dispose();
              }
            }
            // pressed = false;
            // button.dispatchEvent(new KeyEvent(e.getComponent(),
            // e.getID(), e.getWhen(),
            // e.getModifiersEx(), KeyEvent.VK_SPACE,
            // e.getKeyChar(), e.getKeyLocation()));
          }
        }
        
        public void keyTyped(KeyEvent e)
        {
          /*
           * if (e.getKeyChar() == '\n') { button.dispatchEvent(new
           * KeyEvent(button, e.getID(), e.getWhen(), e.getModifiersEx(),
           * e.getKeyCode(), ' ', e.getKeyLocation())); }
           */
        }
      });
      button.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          VTFontManager.unregisterWindow(dialog);
          dialog.setVisible(false);
          dialog.dispose();
          if (frame.isUndecorated())
          {
            frame.dispose();
          }
        }
      });
      button.setFocusable(true);
      p2.add(button);
      // main.add(p1);
      // main.add(p2);
      // dialog.setFocusable(false);
      dialog.add(p1, BorderLayout.CENTER);
      dialog.add(p2, BorderLayout.SOUTH);
      dialog.setModal(false);
      //dialog.setAlwaysOnTop(true);
      
      try
      {
        if (setIconImageMethod != null)
        {
          setIconImageMethod.invoke(dialog, warningIcon16);
        }
      }
      catch (Throwable t)
      {
        
      }
      VTFontManager.registerWindow(dialog);
      dialog.setResizable(true);
      dialog.pack();
      // dialog.setUndecorated(true);
      // dialog.setMinimumSize(dialog.getSize());
      dialog.setMaximumSize(dialog.getSize());
      dialog.setLocationRelativeTo(frame);
      
      dialog.addWindowListener(new WindowListener()
      {
        public void windowActivated(WindowEvent e)
        {
          button.requestFocusInWindow();
        }
        
        public void windowClosed(WindowEvent e)
        {
          
        }
        
        public void windowClosing(WindowEvent e)
        {
          VTFontManager.unregisterWindow(dialog);
          dialog.setVisible(false);
          dialog.dispose();
          if (frame.isUndecorated())
          {
            frame.dispose();
          }
        }
        
        public void windowDeactivated(WindowEvent e)
        {
          
        }
        
        public void windowDeiconified(WindowEvent e)
        {
          // dialog.set
          button.requestFocusInWindow();
        }
        
        public void windowIconified(WindowEvent e)
        {
          // dialog.setVisible(true);
          // dialog.toFront();
        }
        
        public void windowOpened(WindowEvent e)
        {
          button.requestFocusInWindow();
        }
      });
      dialog.getToolkit().beep();
      dialog.setAlwaysOnTop(true);
      dialog.setVisible(true);
      dialog.toFront();
      dialog.requestFocus();
      // dialog.toFront();
      // dialog.dispose();
      // invisible.dispose();
      // pane = null;
      // dialog = null;
      // invisible = null;
      return dialog;
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    return null;
  }
  
  /*
   * public static void main(String[] args) throws Exception {
   * VTGraphicsMessager.showAlert("ALERTA", "AMARELO");
   * VTGraphicsMessager.showAlert("ALERTA", "VERDE");
   * VTGraphicsMessager.showAlert("ALERTA", "VERMELHO"); }
   */
}