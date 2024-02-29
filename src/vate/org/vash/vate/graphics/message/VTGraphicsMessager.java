package org.vash.vate.graphics.message;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Label;
import java.awt.Panel;
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
import org.vash.vate.graphics.font.VTGlobalTextStyleManager;
import org.vash.vate.graphics.image.VTIconDisplay;
import org.vash.vate.reflection.VTReflectionUtils;

public class VTGraphicsMessager
{
  private static BufferedImage warningIcon16;
  private static BufferedImage warningIcon32;
  // private static BufferedImage displayIcon;
  private static Method setIconImage;
  
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
      setIconImage = Dialog.class.getMethod("setIconImage", Class.forName("java.awt.Image"));
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
  
  public static void showAlert(Frame owner, String title, String message)
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return;
    }
    showAlert(null, owner, title, message);
  }
  
  public static void showAlert(GraphicsDevice device, Frame owner, String title, String message)
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return;
    }
    try
    {
      if (device == null && owner != null)
      {
        device = VTGraphicalDeviceResolver.getCurrentDevice(owner);
      }
      if (device == null)
      {
        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      }
      // final Frame parent = (owner != null ? owner : new
      // Frame(device.getDefaultConfiguration()));
      final Frame parent = (owner != null ? owner : new Frame(device.getDefaultConfiguration()));
      if (owner == null)
      {
        parent.setIconImage(warningIcon16);
        parent.setUndecorated(true);
      }
      // invisible.setUndecorated(true);
      
      // JOptionPane pane = new JOptionPane(message,
      // JOptionPane.WARNING_MESSAGE,
      // JOptionPane.DEFAULT_OPTION);
      // pane.createDialog(invisible, title);
      
      final Dialog dialog = new Dialog(parent, false);
      try
      {
        if (setIconImage != null)
        {
          setIconImage.invoke(dialog, warningIcon16);
        }
      }
      catch (Throwable e)
      {
        
      }
      
      // int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
      dialog.setTitle(title);
      
      // Panel main = new Panel();
      // main.setLayout(new GridLayout(2, 1));
      float fontScaling = VTGlobalTextStyleManager.FONT_SCALING_FACTOR_MONOSPACED;
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
              // if (parent.isDisplayable())
              // {
              // System.out.println("parent.isDisplayable()");
              // return;
              // }
              dialog.setVisible(false);
              dialog.dispose();
              if (parent.isUndecorated())
              {
                parent.dispose();
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
              // if (parent.isDisplayable())
              // {
              // System.out.println("parent.isDisplayable()");
              // return;
              // }
              dialog.setVisible(false);
              dialog.dispose();
              if (parent.isUndecorated())
              {
                parent.dispose();
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
          // if (parent.isDisplayable())
          // {
          // System.out.println("parent.isDisplayable()");
          // return;
          // }
          dialog.setVisible(false);
          dialog.dispose();
          if (parent.isUndecorated())
          {
            parent.dispose();
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
      dialog.setAlwaysOnTop(true);
      
      try
      {
        if (setIconImage != null)
        {
          setIconImage.invoke(dialog, warningIcon16);
        }
      }
      catch (Throwable t)
      {
        
      }
      dialog.setResizable(true);
      dialog.pack();
      // dialog.setUndecorated(true);
      // dialog.setMinimumSize(dialog.getSize());
      dialog.setMaximumSize(dialog.getSize());
      dialog.setLocationRelativeTo(parent);
      
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
          /*
           * Container container = e.getComponent().getParent(); if (container
           * instanceof Frame) { Frame frame = (Frame) container;
           * frame.dispose(); }
           */
          dialog.setVisible(false);
          dialog.dispose();
          // if (parent.isDisplayable())
          // {
          // System.out.println("parent.isDisplayable()");
          // return;
          // }
          if (parent.isUndecorated())
          {
            parent.dispose();
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
      dialog.setVisible(true);
      dialog.toFront();
      button.requestFocusInWindow();
      parent.getToolkit().beep();
      // dialog.toFront();
      // dialog.dispose();
      // invisible.dispose();
      // pane = null;
      // dialog = null;
      // invisible = null;
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
  
  /*
   * public static void main(String[] args) throws Exception {
   * VTGraphicsMessager.showAlert("ALERTA", "AMARELO");
   * VTGraphicsMessager.showAlert("ALERTA", "VERDE");
   * VTGraphicsMessager.showAlert("ALERTA", "VERMELHO"); }
   */
}