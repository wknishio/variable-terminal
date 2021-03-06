package org.vate.startup;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
//import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

import org.vate.VT;
import org.vate.graphics.font.VTGlobalTextStyleManager;
import org.vate.graphics.image.VTIconDisplay;

public class VTGraphicalStartDialog extends Dialog
{
  private static final long serialVersionUID = 1L;
  // private volatile BufferedImage terminalIcon;
  private volatile int mode = 0;

  public VTGraphicalStartDialog(Frame owner)
  {
    super(owner, true);
    // setFont(new Font("Dialog", Font.PLAIN, 12));
    setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
    try
    {
      // InputStream stream =
      // this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/remote.png");
      // terminalIcon = VT.remoteIcon;
      // stream.close();
      Method setIconImage = this.getClass().getMethod("setIconImage", Class.forName("java.awt.Image"));
      setIconImage.invoke(this, VT.remoteIcon);
      // setIconImage(terminalIcon);
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
    }

    VTGlobalTextStyleManager.registerWindow(this);

    final Panel mainPanel = new Panel();
    final Panel titlePanel = new Panel();
    final Panel centerPanel = new Panel();
    final Panel iconPanel = new Panel();
    final Panel buttonsPanel = new Panel();
    final Panel buttonsRow = new Panel();
    final Label choose = new Label("Choose the module:");
    final Label title = new Label("Variable-Terminal " + VT.VT_VERSION);

    mainPanel.setLayout(new BorderLayout());
    titlePanel.setLayout(new BorderLayout());
    centerPanel.setLayout(new BorderLayout());
    iconPanel.setLayout(new BorderLayout());
    buttonsPanel.setLayout(new BorderLayout());

    VTIconDisplay display = new VTIconDisplay();
    display.setFocusable(false);
    display.setImage(VT.remoteIcon, 64, 64);

    final Button client = new Button(" Client ");
    final Button server = new Button(" Server ");

    choose.setAlignment(Label.CENTER);
    title.setAlignment(Label.CENTER);
    choose.setFocusable(false);
    title.setFocusable(false);

    titlePanel.add(new Panel(), BorderLayout.NORTH);
    titlePanel.add(title, BorderLayout.CENTER);
    // titlePanel.add(new Panel(), BorderLayout.WEST);
    // titlePanel.add(new Panel(), BorderLayout.EAST);

    iconPanel.add(new Panel(), BorderLayout.NORTH);
    iconPanel.add(display, BorderLayout.CENTER);
    iconPanel.add(new Panel(), BorderLayout.SOUTH);
    iconPanel.add(new Label("  "), BorderLayout.WEST);
    iconPanel.add(new Label("  "), BorderLayout.EAST);

    centerPanel.add(titlePanel, BorderLayout.NORTH);
    centerPanel.add(iconPanel, BorderLayout.CENTER);
    centerPanel.add(choose, BorderLayout.SOUTH);

    buttonsRow.add(client);
    buttonsRow.add(server);

    // buttonsPanel.add(new Panel(), BorderLayout.NORTH);
    buttonsPanel.add(buttonsRow, BorderLayout.CENTER);
    buttonsPanel.add(new Panel(), BorderLayout.SOUTH);

    mainPanel.add(centerPanel, BorderLayout.CENTER);
    mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

    // add(new Panel(), BorderLayout.EAST);
    // add(new Panel(), BorderLayout.WEST);
    // add(new Label(""), BorderLayout.NORTH);
    // add(new Label(""), BorderLayout.SOUTH);
    add(mainPanel, BorderLayout.CENTER);

    try
    {
      Method setAutoRequestFocus = this.getClass().getMethod("setAutoRequestFocus", boolean.class);
      setAutoRequestFocus.invoke(this, true);
      // setAutoRequestFocus(true);
    }
    catch (Throwable t)
    {

    }

    this.addKeyListener(new KeyListener()
    {
      public void keyTyped(KeyEvent e)
      {

      }

      public void keyPressed(KeyEvent e)
      {
        if (VTGlobalTextStyleManager.processKeyEvent(e))
        {
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_KP_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN || e.getKeyCode() == KeyEvent.VK_HOME)
        {
          client.requestFocus();
        }
        else if (e.getKeyCode() == KeyEvent.VK_END)
        {
          server.requestFocus();
        }
        else if (e.getKeyCode() == KeyEvent.VK_C)
        {
          mode = 1;
          setVisible(false);
        }
        else if (e.getKeyCode() == KeyEvent.VK_S)
        {
          mode = 2;
          setVisible(false);
        }
      }

      public void keyReleased(KeyEvent e)
      {

      }
    });

    mainPanel.addKeyListener(new KeyListener()
    {
      public void keyTyped(KeyEvent e)
      {

      }

      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_KP_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN || e.getKeyCode() == KeyEvent.VK_HOME)
        {
          client.requestFocus();
        }
        else if (e.getKeyCode() == KeyEvent.VK_END)
        {
          server.requestFocus();
        }
        else if (e.getKeyCode() == KeyEvent.VK_C)
        {
          mode = 1;
          setVisible(false);
        }
        else if (e.getKeyCode() == KeyEvent.VK_S)
        {
          mode = 2;
          setVisible(false);
        }
      }

      public void keyReleased(KeyEvent e)
      {

      }
    });

    choose.addKeyListener(new KeyListener()
    {
      public void keyTyped(KeyEvent e)
      {

      }

      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_KP_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN || e.getKeyCode() == KeyEvent.VK_HOME)
        {
          client.requestFocus();
        }
        else if (e.getKeyCode() == KeyEvent.VK_END)
        {
          server.requestFocus();
        }
        else if (e.getKeyCode() == KeyEvent.VK_C)
        {
          mode = 1;
          setVisible(false);
        }
        else if (e.getKeyCode() == KeyEvent.VK_S)
        {
          mode = 2;
          setVisible(false);
        }
      }

      public void keyReleased(KeyEvent e)
      {

      }
    });

    client.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        mode = 1;
        setVisible(false);
      }
    });

    client.addKeyListener(new KeyListener()
    {
      public void keyTyped(KeyEvent e)
      {
        if (e.getKeyChar() == '\n')
        {
          mode = 1;
          setVisible(false);
        }
        if (e.getKeyChar() == '\u001B')
        {
          mode = 0;
          setVisible(false);
        }
      }

      public void keyPressed(KeyEvent e)
      {
        if (VTGlobalTextStyleManager.processKeyEvent(e))
        {
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_KP_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN || e.getKeyCode() == KeyEvent.VK_END)
        {
          server.requestFocus();
        }
        else if (e.getKeyCode() == KeyEvent.VK_C)
        {
          mode = 1;
          setVisible(false);
        }
        else if (e.getKeyCode() == KeyEvent.VK_S)
        {
          mode = 2;
          setVisible(false);
        }
      }

      public void keyReleased(KeyEvent e)
      {

      }
    });

    server.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        mode = 2;
        setVisible(false);
      }
    });

    server.addKeyListener(new KeyListener()
    {
      public void keyTyped(KeyEvent e)
      {
        if (e.getKeyChar() == '\n')
        {
          mode = 2;
          setVisible(false);
        }
        if (e.getKeyChar() == '\u001B')
        {
          mode = 0;
          setVisible(false);
        }
      }

      public void keyPressed(KeyEvent e)
      {
        if (VTGlobalTextStyleManager.processKeyEvent(e))
        {
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_KP_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN || e.getKeyCode() == KeyEvent.VK_HOME)
        {
          client.requestFocus();
        }
        else if (e.getKeyCode() == KeyEvent.VK_C)
        {
          mode = 1;
          setVisible(false);
        }
        else if (e.getKeyCode() == KeyEvent.VK_S)
        {
          mode = 2;
          setVisible(false);
        }
      }

      public void keyReleased(KeyEvent e)
      {

      }
    });

    this.addComponentListener(new ComponentListener()
    {
      public void componentHidden(ComponentEvent e)
      {

      }

      public void componentMoved(ComponentEvent e)
      {

      }

      public void componentResized(ComponentEvent e)
      {

      }

      public void componentShown(ComponentEvent e)
      {
        if (!client.isFocusOwner() && !server.isFocusOwner())
        {
          client.requestFocus();
          EventQueue.invokeLater(new Runnable()
          {
            public void run()
            {
              try
              {
                Thread.sleep(1);
              }
              catch (InterruptedException e)
              {

              }
              client.requestFocus();
            }
          });
        }
      }
    });

    this.addWindowFocusListener(new WindowFocusListener()
    {
      public void windowGainedFocus(WindowEvent e)
      {

      }

      public void windowLostFocus(WindowEvent e)
      {

      }
    });

    this.addWindowListener(new WindowListener()
    {
      public void windowOpened(WindowEvent e)
      {
        client.requestFocus();
        EventQueue.invokeLater(new Runnable()
        {
          public void run()
          {
            try
            {
              Thread.sleep(1);
            }
            catch (InterruptedException e)
            {

            }
            client.requestFocus();
          }
        });
      }

      public void windowClosing(WindowEvent e)
      {
        setVisible(false);
      }

      public void windowClosed(WindowEvent e)
      {

      }

      public void windowIconified(WindowEvent e)
      {

      }

      public void windowDeiconified(WindowEvent e)
      {

      }

      public void windowActivated(WindowEvent e)
      {

      }

      public void windowDeactivated(WindowEvent e)
      {

      }
    });

    setFocusTraversalKeysEnabled(true);
    setResizable(true);
    pack();
    setLocationRelativeTo(owner);
    // client.requestFocus();
  }

  public int getMode()
  {
    return mode;
  }

  public void setVisible(boolean visible)
  {
    getOwner().toFront();
    super.setVisible(visible);
  }
}