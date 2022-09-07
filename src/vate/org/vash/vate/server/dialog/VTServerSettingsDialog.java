package org.vash.vate.server.dialog;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.lang.reflect.Method;

import org.vash.vate.VT;
import org.vash.vate.console.VTConsole;
import org.vash.vate.dialog.VTFileDialog;
import org.vash.vate.graphics.font.VTGlobalTextStyleManager;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.connection.VTServerConnector;

public class VTServerSettingsDialog extends Dialog
{
  private static final long serialVersionUID = 1L;

  private VTServerSettingsDialogParameter connectionMode;
  private VTServerSettingsDialogParameter connectionHost;
  private VTServerSettingsDialogParameter connectionPort;
  private VTServerSettingsDialogParameter natPort;
  private VTServerSettingsDialogParameter proxyType;
  private VTServerSettingsDialogParameter proxyHost;
  private VTServerSettingsDialogParameter proxyPort;
  private VTServerSettingsDialogParameter proxySecurity;
  private VTServerSettingsDialogParameter proxyUser;
  private VTServerSettingsDialogParameter proxyPassword;
  private VTServerSettingsDialogParameter encryptionType;
  private VTServerSettingsDialogParameter encryptionPassword;

  private VTServerSettingsDialogParameter sessionsMaximum;
  private VTServerSettingsDialogParameter sessionShell;
  private VTServerSettingsDialogParameter sessionUser;
  private VTServerSettingsDialogParameter sessionPassword;

  private Runnable application;
  private Frame owner;

  // private volatile boolean opened;
  // private Button okButton;
  private static Method setIconImage;
  
  static
  {
    try
    {
      setIconImage = VTServerSettingsDialog.class.getMethod("setIconImage", Class.forName("java.awt.Image"));
      //setIconImage.setAccessible(true);
    }
    catch (Throwable e)
    {

    }
  }

  public VTServerSettingsDialog(Frame owner, String title, boolean modal, Runnable application)
  {
    this(owner, title, modal, owner.getGraphicsConfiguration(), application);
  }

  public VTServerSettingsDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc, final Runnable application)
  {
    super(owner, title, modal, gc);
    // super(title, gc);
    this.owner = owner;
    this.application = application;
    final VTFileDialog loadFileDialog = new VTFileDialog(this, "Variable-Terminal " + VT.VT_VERSION + " - Server - Load File", FileDialog.LOAD);
    final VTFileDialog saveFileDialog = new VTFileDialog(this, "Variable-Terminal " + VT.VT_VERSION + " - Server - Save File", FileDialog.SAVE);
    VTGlobalTextStyleManager.registerWindow(loadFileDialog);
    VTGlobalTextStyleManager.registerWindow(saveFileDialog);
    
    try
    {
      if (setIconImage != null)
      {
        setIconImage.invoke(this, VT.remoteIcon);
      }
    }
    catch (Throwable e)
    {
      
    }

    this.addWindowListener(new WindowListener()
    {

      public void windowActivated(WindowEvent e)
      {

      }

      public void windowClosed(WindowEvent e)
      {

      }

      public void windowClosing(WindowEvent e)
      {
        close();
      }

      public void windowDeactivated(WindowEvent e)
      {

      }

      public void windowDeiconified(WindowEvent e)
      {

      }

      public void windowIconified(WindowEvent e)
      {

      }

      public void windowOpened(WindowEvent e)
      {

      }
    });

    VTGlobalTextStyleManager.registerWindow(this);

    // centerPanel.getInsets().set(4, 4, 4, 4);

    Choice connectionModeChoice = new Choice();
    connectionMode = new VTServerSettingsDialogParameter("Connection Mode:", connectionModeChoice, true);
    TextField connectionHostField = new TextField(16);
    connectionHost = new VTServerSettingsDialogParameter("Connection Host:", connectionHostField, true);
    TextField connectionPortField = new TextField(5);
    connectionPortField.addTextListener(new TextListener()
    {
      public void textValueChanged(TextEvent e)
      {
        TextField field = ((TextField) e.getSource());
        String value = field.getText();

        if (!value.matches("(\\d+?)|()"))
        {
          int position = field.getCaretPosition();
          field.setText(value.replaceAll("\\D", ""));
          field.setCaretPosition(position);
        }
        else
        {
          try
          {
            Integer number = Integer.parseInt(value);
            if (number > 65535 || number < 1)
            {
              int position = field.getCaretPosition();
              field.setText(value.substring(1));
              field.setCaretPosition(position);
            }
            else
            {
              // okButton.setEnabled(true);
            }
          }
          catch (Throwable ex)
          {
            // okButton.setEnabled(false);
          }
        }
      }
    });
    connectionPort = new VTServerSettingsDialogParameter("Connection Port:", connectionPortField, true);
    TextField natPortField = new TextField(5);
    natPortField.addTextListener(new TextListener()
    {
      public void textValueChanged(TextEvent e)
      {
        TextField field = ((TextField) e.getSource());
        String value = field.getText();

        if (!value.matches("(\\d+?)|()"))
        {
          int position = field.getCaretPosition();
          field.setText(value.replaceAll("\\D", ""));
          field.setCaretPosition(position);
        }
        else
        {
          try
          {
            Integer number = Integer.parseInt(value);
            if (number > 65535 || number < 1)
            {
              int position = field.getCaretPosition();
              field.setText(value.substring(1));
              field.setCaretPosition(position);
            }
          }
          catch (Throwable ex)
          {

          }
        }
      }
    });
    natPort = new VTServerSettingsDialogParameter("Connection NAT Port:", natPortField, false);
    Choice proxyTypeChoice = new Choice();
    proxyType = new VTServerSettingsDialogParameter("Proxy Type:", proxyTypeChoice, true);
    TextField proxyHostField = new TextField(16);
    proxyHost = new VTServerSettingsDialogParameter("Proxy Host:", proxyHostField, false);
    TextField proxyPortField = new TextField(5);
    proxyPortField.addTextListener(new TextListener()
    {
      public void textValueChanged(TextEvent e)
      {
        TextField field = ((TextField) e.getSource());
        String value = field.getText();

        if (!value.matches("(\\d+?)|()"))
        {
          int position = field.getCaretPosition();
          field.setText(value.replaceAll("\\D", ""));
          field.setCaretPosition(position);
        }
        else
        {
          try
          {
            Integer number = Integer.parseInt(value);
            if (number > 65535 || number < 1)
            {
              int position = field.getCaretPosition();
              field.setText(value.substring(1));
              field.setCaretPosition(position);
            }
          }
          catch (Throwable ex)
          {

          }
        }
      }
    });
    proxyPort = new VTServerSettingsDialogParameter("Proxy Port:", proxyPortField, false);
    Choice proxySecurityChoice = new Choice();
    proxySecurity = new VTServerSettingsDialogParameter("Proxy Authentication:", proxySecurityChoice, false);
    TextField proxyUserField = new TextField(16);
    proxyUserField.setEchoChar('*');
    proxyUser = new VTServerSettingsDialogParameter("Proxy User:", proxyUserField, false);
    TextField proxyPasswordField = new TextField(16);
    proxyPasswordField.setEchoChar('*');
    proxyPassword = new VTServerSettingsDialogParameter("Proxy Password:", proxyPasswordField, false);
    Choice encryptionTypeChoice = new Choice();
    encryptionType = new VTServerSettingsDialogParameter("Encryption Type:", encryptionTypeChoice, true);
    TextField encryptionPasswordField = new TextField(16);
    encryptionPasswordField.setEchoChar('*');
    encryptionPassword = new VTServerSettingsDialogParameter("Encryption Password:", encryptionPasswordField, true);

    // try
    // {
    // BorderLayout layout = (BorderLayout) this.getLayout();
    // layout.setHgap(1);
    // layout.setVgap(1);
    // }
    // catch (Throwable t)
    // {

    // }

    connectionModeChoice.add("Active");
    connectionModeChoice.add("Passive");
    connectionModeChoice.select("Passive");
    connectionModeChoice.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          if (e.getItem().equals("Active"))
          {
            setConnectionMode(true);
          }
          else if (e.getItem().equals("Passive"))
          {
            setConnectionMode(false);
          }
        }
      }
    });

    proxyTypeChoice.add("None");
    proxyTypeChoice.add("SOCKS");
    proxyTypeChoice.add("HTTP");
    proxyTypeChoice.select("None");
    proxyTypeChoice.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          if (e.getItem().equals("None"))
          {
            setProxyType("None");
          }
          else if (e.getItem().equals("SOCKS"))
          {
            setProxyType("SOCKS");
          }
          else if (e.getItem().equals("HTTP"))
          {
            setProxyType("HTTP");
          }
        }
      }
    });

    proxySecurityChoice.add("Disabled");
    proxySecurityChoice.add("Enabled");
    proxySecurityChoice.select("Disabled");
    proxySecurityChoice.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          if (e.getItem().equals("Disabled"))
          {
            setProxySecurity(false);
          }
          else if (e.getItem().equals("Enabled"))
          {
            setProxySecurity(true);
          }
        }
      }
    });

    encryptionTypeChoice.add("None");
    encryptionTypeChoice.add("RC4");
    encryptionTypeChoice.add("AES");
    encryptionTypeChoice.add("ISAAC");
    encryptionTypeChoice.add("SALSA");
    encryptionTypeChoice.add("HC256");
    encryptionTypeChoice.add("GRAIN");
    //encryptionTypeChoice.add("BLOWFISH");
    encryptionTypeChoice.select("None");
    encryptionTypeChoice.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          if (e.getItem().equals("None"))
          {
            setEncryptionType("None");
          }
          else if (e.getItem().equals("RC4"))
          {
            setEncryptionType("RC4");
          }
          else if (e.getItem().equals("AES"))
          {
            setEncryptionType("AES");
          }
          //else if (e.getItem().equals("BLOWFISH"))
          //{
            //setEncryptionType("BLOWFISH");
          //}
          else if (e.getItem().equals("SALSA"))
          {
            setEncryptionType("SALSA");
          }
          else if (e.getItem().equals("HC256"))
          {
            setEncryptionType("HC256");
          }
          else if (e.getItem().equals("GRAIN"))
          {
            setEncryptionType("GRAIN");
          }
          else if (e.getItem().equals("ISAAC"))
          {
            setEncryptionType("ISAAC");
          }
        }
      }
    });

    TextField sessionsMaximumField = new TextField(16);
    sessionsMaximumField.addTextListener(new TextListener()
    {
      public void textValueChanged(TextEvent e)
      {
        TextField field = ((TextField) e.getSource());
        String value = field.getText();

        if (!value.matches("(\\d+?)|()"))
        {
          int position = field.getCaretPosition();
          field.setText(value.replaceAll("\\D", ""));
          field.setCaretPosition(position);
        }
      }
    });
    sessionsMaximum = new VTServerSettingsDialogParameter("Session Maximum:", sessionsMaximumField, true);
    
    TextField sessionShellField = new TextField(16);
    sessionShell = new VTServerSettingsDialogParameter("Session Shell:", sessionShellField, true);

    TextField connectionUserField = new TextField(16);
    connectionUserField.setEchoChar('*');
    sessionUser = new VTServerSettingsDialogParameter("Session User:", connectionUserField, true);

    TextField connectionPasswordField = new TextField(16);
    connectionPasswordField.setEchoChar('*');
    sessionPassword = new VTServerSettingsDialogParameter("Session Password:", connectionPasswordField, true);

    addKeyListener(new KeyListener()
    {
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          execute();
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
          close();
          return;
        }
      }

      public void keyReleased(KeyEvent e)
      {

      }

      public void keyTyped(KeyEvent e)
      {

        if (e.getKeyChar() == '\n')
        {
          execute();
          return;
        }
        if (e.getKeyChar() == '\u001B')
        {
          close();
          return;
        }
      }
    });

    Button loadButton = new Button("Load");
    Button saveButton = new Button("Save");

    Panel buttonPanel1 = new Panel();
    GridLayout buttonLayout1 = new GridLayout(1, 2);
    buttonLayout1.setHgap(1);
    buttonLayout1.setVgap(1);
    buttonPanel1.setLayout(buttonLayout1);
    buttonPanel1.add(loadButton);
    buttonPanel1.add(saveButton);

    loadButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        loadFileDialog.setDirectory(System.getProperty("user.dir"));
        loadFileDialog.setFile("vate-server.properties");
        loadFileDialog.setVisible(true);
        if (loadFileDialog.getFile() != null)
        {
          if (application instanceof VTServer)
          {
            VTServer server = (VTServer) application;
            try
            {
              server.loadServerSettingsFile(new File(loadFileDialog.getDirectory(), loadFileDialog.getFile()).getAbsolutePath());
            }
            catch (Throwable t)
            {
              // t.printStackTrace();
            }
            load();
          }
        }
      }
    });

    saveButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        saveFileDialog.setDirectory(System.getProperty("user.dir"));
        saveFileDialog.setFile("vate-server.properties");
        saveFileDialog.setVisible(true);
        if (saveFileDialog.getFile() != null)
        {
          if (application instanceof VTServer)
          {
            VTServer server = (VTServer) application;
            update();
            try
            {
              server.saveServerSettingsFile(new File(saveFileDialog.getDirectory(), saveFileDialog.getFile()).getAbsolutePath());
            }
            catch (Throwable t)
            {
              // t.printStackTrace();
            }
          }
        }
      }
    });

    addKeyListener(new KeyListener()
    {
      public void keyPressed(KeyEvent e)
      {
        if (VTGlobalTextStyleManager.processKeyEvent(e))
        {
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          execute();
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
          close();
          return;
        }
      }

      public void keyReleased(KeyEvent e)
      {

      }

      public void keyTyped(KeyEvent e)
      {
        if (e.getKeyChar() == '\n')
        {
          execute();
          return;
        }
        if (e.getKeyChar() == '\u001B')
        {
          close();
          return;
        }
      }
    });

    loadButton.addKeyListener(new KeyListener()
    {
      public void keyPressed(KeyEvent e)
      {
        if (VTGlobalTextStyleManager.processKeyEvent(e))
        {
          return;
        }
      }

      public void keyReleased(KeyEvent e)
      {

      }

      public void keyTyped(KeyEvent e)
      {
        if (e.getKeyChar() == '\n')
        {
          loadFileDialog.setDirectory(System.getProperty("user.dir"));
          loadFileDialog.setFile("vate-server.properties");
          loadFileDialog.setVisible(true);
          if (loadFileDialog.getFile() != null)
          {
            VTServer server = (VTServer) application;
            try
            {
              server.loadServerSettingsFile(new File(loadFileDialog.getDirectory(), loadFileDialog.getFile()).getAbsolutePath());
            }
            catch (Throwable t)
            {
              // t.printStackTrace();
            }
            load();
          }
        }
        if (e.getKeyChar() == '\u001B')
        {
          close();
        }
      }
    });

    saveButton.addKeyListener(new KeyListener()
    {
      public void keyPressed(KeyEvent e)
      {
        if (VTGlobalTextStyleManager.processKeyEvent(e))
        {
          return;
        }
      }

      public void keyReleased(KeyEvent e)
      {

      }

      public void keyTyped(KeyEvent e)
      {
        if (e.getKeyChar() == '\n')
        {
          saveFileDialog.setDirectory(System.getProperty("user.dir"));
          saveFileDialog.setFile("vate-server.properties");
          saveFileDialog.setVisible(true);
          if (saveFileDialog.getFile() != null)
          {
            if (application instanceof VTServer)
            {
              VTServer server = (VTServer) application;
              update();
              try
              {
                server.saveServerSettingsFile(new File(saveFileDialog.getDirectory(), saveFileDialog.getFile()).getAbsolutePath());
              }
              catch (Throwable t)
              {
                // t.printStackTrace();
              }
            }
          }
        }
        if (e.getKeyChar() == '\u001B')
        {
          close();
        }
      }
    });

    Button okButton = new Button("Proceed");
    Button closeButton = new Button("Cancel");
    Panel buttonPanel2 = new Panel();
    GridLayout buttonLayout2 = new GridLayout(1, 2);
    buttonLayout2.setHgap(1);
    buttonLayout2.setVgap(1);
    buttonPanel2.setLayout(buttonLayout2);
    buttonPanel2.add(okButton);
    buttonPanel2.add(closeButton);

    // okButton.setEnabled(false);
    okButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        execute();
      }
    });

    closeButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        close();
      }
    });

    okButton.addKeyListener(new KeyListener()
    {
      public void keyPressed(KeyEvent e)
      {
        if (VTGlobalTextStyleManager.processKeyEvent(e))
        {
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          execute();
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
          close();
          return;
        }
      }

      public void keyReleased(KeyEvent e)
      {

      }

      public void keyTyped(KeyEvent e)
      {
        if (e.getKeyChar() == '\n')
        {
          execute();
          return;
        }
        if (e.getKeyChar() == '\u001B')
        {
          close();
          return;
        }
      }
    });

    closeButton.addKeyListener(new KeyListener()
    {
      public void keyPressed(KeyEvent e)
      {
        if (VTGlobalTextStyleManager.processKeyEvent(e))
        {
          return;
        }
      }

      public void keyReleased(KeyEvent e)
      {

      }

      public void keyTyped(KeyEvent e)
      {
        if (e.getKeyChar() == '\n')
        {
          close();
        }
        if (e.getKeyChar() == '\u001B')
        {
          close();
        }
      }
    });

    Panel centerPanel = new Panel();
    GridLayout centerLayout = new GridLayout(18, 1);
    centerLayout.setHgap(1);
    centerLayout.setVgap(1);
    centerPanel.setLayout(centerLayout);

    Panel mainPanel = new Panel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(new Panel(), BorderLayout.EAST);
    mainPanel.add(new Panel(), BorderLayout.WEST);
    mainPanel.add(new Panel(), BorderLayout.NORTH);
    mainPanel.add(new Panel(), BorderLayout.SOUTH);
    mainPanel.add(centerPanel, BorderLayout.CENTER);
    add(mainPanel);

    centerPanel.add(connectionMode);
    centerPanel.add(connectionHost);
    centerPanel.add(connectionPort);
    centerPanel.add(natPort);
    
    centerPanel.add(proxyType);
    centerPanel.add(proxyHost);
    centerPanel.add(proxyPort);
    centerPanel.add(proxySecurity);
    centerPanel.add(proxyUser);
    centerPanel.add(proxyPassword);

    centerPanel.add(encryptionType);
    centerPanel.add(encryptionPassword);

    centerPanel.add(sessionShell);
    centerPanel.add(sessionsMaximum);
    
    centerPanel.add(sessionUser);
    centerPanel.add(sessionPassword);

    centerPanel.add(buttonPanel1);

    centerPanel.add(buttonPanel2);

    for (Component component : centerPanel.getComponents())
    {
      if (component instanceof Panel)
      {
        Panel panel1 = (Panel) component;
        for (Component element1 : panel1.getComponents())
        {
          if (element1 instanceof Panel)
          {
            Panel panel2 = (Panel) element1;
            for (Component element2 : panel2.getComponents())
            {
              element2.addKeyListener(new KeyListener()
              {
                public void keyPressed(KeyEvent e)
                {
                  if (VTGlobalTextStyleManager.processKeyEvent(e))
                  {
                    return;
                  }
                  if (e.getKeyCode() == KeyEvent.VK_ENTER)
                  {
                    execute();
                    return;
                  }
                  if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                  {
                    close();
                    return;
                  }
                }

                public void keyReleased(KeyEvent e)
                {

                }

                public void keyTyped(KeyEvent e)
                {
                  if (e.getKeyChar() == '\n')
                  {
                    execute();
                    return;
                  }
                  if (e.getKeyChar() == '\u001B')
                  {
                    close();
                    return;
                  }
                }
              });
            }
          }
        }
      }
    }

    MenuBar menubar = new MenuBar();
    Menu textMenu = new Menu("Fonts");
    MenuItem increaseTextMenu = new MenuItem("Increase ");
    MenuItem decreaseTextMenu = new MenuItem("Decrease ");
    MenuItem defaultTextMenu = new MenuItem("Normalize ");
    MenuItem toggleBoldTextMenu = new MenuItem("Intensitize ");

    increaseTextMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VTGlobalTextStyleManager.increaseFontSize();
      }
    });

    decreaseTextMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VTGlobalTextStyleManager.decreaseFontSize();
      }
    });

    defaultTextMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VTGlobalTextStyleManager.defaultFontSize();
      }
    });

    toggleBoldTextMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (VTGlobalTextStyleManager.isFontStyleBold())
        {
          VTGlobalTextStyleManager.disableFontStyleBold();
        }
        else
        {
          VTGlobalTextStyleManager.enableFontStyleBold();
        }
//				VTGlobalTextStyleManager.packComponents();
      }
    });

    textMenu.add(increaseTextMenu);
    textMenu.add(decreaseTextMenu);
    textMenu.add(toggleBoldTextMenu);
    textMenu.add(defaultTextMenu);

    menubar.add(textMenu);
    textMenu.setEnabled(true);

    // this.setMenuBar(menubar);

    setResizable(true);
    pack();
  }

  public void open()
  {
    if (isVisible())
    {
      // opened = true;
      return;
    }
    load();
    if (owner != null && owner.isVisible())
    {
      setLocationRelativeTo(owner);
    }
    else
    {
      setLocationByPlatform(true);
    }
    pack();
    // setAlwaysOnTop(true);
    try
    {
      setVisible(true);
    }
    catch (Throwable t)
    {

    }

//		opened = true;
//		toFront();
//		requestFocus();
//		while (opened)
//		{
//			synchronized (this)
//			{
//				try
//				{
//					wait();
//				}
//				catch (Throwable t)
//				{
//					
//				}
//			}
//		}
  }

  /* public void close() { dialog.close(); } */

  public void load()
  {
    if (application instanceof VTServer)
    {
      VTServer server = (VTServer) application;
      VTServerConnector connector = server.getServerConnector();
      if (connector != null)
      {
        setConnectionMode(!connector.isPassive());
        connectionHost.setParameter(connector.getAddress());
        if (connector.getPort() != null && connector.getPort() > 0)
        {
          connectionPort.setParameter(connector.getPort());
        }
        else
        {
          connectionPort.setParameter(6060);
          // connectionPort.setParameter("");
        }
        natPort.setParameter(connector.getNatPort());
        setEncryptionType(connector.getEncryptionType());
        encryptionPassword.setParameter(connector.getEncryptionKey());
        setProxyType(connector.getProxyType());
        proxyHost.setParameter(connector.getProxyAddress());
        proxyPort.setParameter(connector.getProxyPort());
        setProxySecurity(connector.isUseProxyAuthentication());
        proxyUser.setParameter(connector.getProxyUser());
        proxyPassword.setParameter(connector.getProxyPassword());
        sessionsMaximum.setParameter(String.valueOf(connector.getSessionsMaximum()));
        sessionShell.setParameter(connector.getSessionShell());
      }
      else
      {
        setConnectionMode(!server.isPassive());
        connectionHost.setParameter(server.getAddress());
        if (server.getPort() != null && server.getPort() > 0)
        {
          connectionPort.setParameter(server.getPort());
        }
        else
        {
          connectionPort.setParameter(6060);
          // connectionPort.setParameter("");
        }
        natPort.setParameter(server.getNatPort());
        setEncryptionType(server.getEncryptionType());
        encryptionPassword.setParameter(server.getEncryptionKey());
        setProxyType(server.getProxyType());
        proxyHost.setParameter(server.getProxyAddress());
        proxyPort.setParameter(server.getProxyPort());
        setProxySecurity(server.isUseProxyAuthentication());
        proxyUser.setParameter(server.getProxyUser());
        proxyPassword.setParameter(server.getProxyPassword());
        sessionsMaximum.setParameter(String.valueOf(server.getSessionsMaximum()));
        sessionShell.setParameter(server.getSessionShell());
      }
    }
  }

  private void setConnectionMode(boolean active)
  {
    if (active)
    {
      connectionMode.setParameter("Active");
      // connectionHost.setEnabled(true);
      natPort.setEnabled(false);
      proxyType.setEnabled(true);
      if (!proxyType.getParameter().equalsIgnoreCase("None"))
      {
        proxyHost.setEnabled(true);
        proxyPort.setEnabled(true);
        proxySecurity.setEnabled(true);
        if (!proxySecurity.getParameter().equalsIgnoreCase("Disabled"))
        {
          proxyUser.setEnabled(true);
          proxyPassword.setEnabled(true);
        }
      }
    }
    else
    {
      connectionMode.setParameter("Passive");
      // connectionHost.setEnabled(false);
      natPort.setEnabled(true);
      setProxyType(null);
      proxyType.setEnabled(false);
      proxyHost.setEnabled(false);
      proxyPort.setEnabled(false);
      proxySecurity.setEnabled(false);
      proxyUser.setEnabled(false);
      proxyPassword.setEnabled(false);
    }
  }

  private void setEncryptionType(String encryption)
  {
    if (encryption == null)
    {
      encryptionType.setParameter("None");
      // encryptionPassword.setEnabled(false);
    }
    else if (encryption.toUpperCase().startsWith("R"))
    {
      encryptionType.setParameter("RC4");
      // encryptionPassword.setEnabled(true);
    }
    else if (encryption.toUpperCase().startsWith("A"))
    {
      encryptionType.setParameter("AES");
      // encryptionPassword.setEnabled(true);
    }
    //else if (encryption.toUpperCase().startsWith("B"))
    //{
      //encryptionType.setParameter("BLOWFISH");
      // encryptionPassword.setEnabled(true);
    //}
    else if (encryption.toUpperCase().startsWith("S"))
    {
      encryptionType.setParameter("SALSA");
      // encryptionPassword.setEnabled(true);
    }
    else if (encryption.toUpperCase().startsWith("H"))
    {
      encryptionType.setParameter("HC256");
      // encryptionPassword.setEnabled(true);
    }
    else if (encryption.toUpperCase().startsWith("G"))
    {
      encryptionType.setParameter("GRAIN");
      // encryptionPassword.setEnabled(true);
    }
    else if (encryption.toUpperCase().startsWith("I"))
    {
      encryptionType.setParameter("ISAAC");
      // encryptionPassword.setEnabled(true);
    }
    else
    {
      encryptionType.setParameter("None");
      // encryptionPassword.setEnabled(false);
    }
  }

  private void setProxyType(String proxy)
  {
    if (proxy == null)
    {
      proxyType.setParameter("None");
      proxyHost.setEnabled(false);
      proxyPort.setEnabled(false);
      // proxySecurity.setParameter("None");
      setProxySecurity(false);
      proxySecurity.setEnabled(false);
      proxyUser.setEnabled(false);
      proxyPassword.setEnabled(false);
    }
    else if (proxy.toUpperCase().startsWith("S"))
    {
      proxyType.setParameter("SOCKS");
      proxyHost.setEnabled(true);
      proxyPort.setEnabled(true);
      proxySecurity.setEnabled(true);
      if (!proxySecurity.getParameter().equalsIgnoreCase("Disabled"))
      {
        proxyUser.setEnabled(true);
        proxyPassword.setEnabled(true);
      }
    }
    else if (proxy.toUpperCase().startsWith("H"))
    {
      proxyType.setParameter("HTTP");
      proxyHost.setEnabled(true);
      proxyPort.setEnabled(true);
      proxySecurity.setEnabled(true);
      if (!proxySecurity.getParameter().equalsIgnoreCase("Disabled"))
      {
        proxyUser.setEnabled(true);
        proxyPassword.setEnabled(true);
      }
    }
    else
    {
      proxyType.setParameter("None");
      proxyHost.setEnabled(false);
      proxyPort.setEnabled(false);
      setProxySecurity(false);
      // proxySecurity.setParameter("None");
      proxySecurity.setEnabled(false);
      proxyUser.setEnabled(false);
      proxyPassword.setEnabled(false);
    }
  }

  public void setProxySecurity(boolean security)
  {
    if (security)
    {
      proxySecurity.setParameter("Enabled");
      proxyUser.setEnabled(true);
      proxyPassword.setEnabled(true);
    }
    else
    {
      proxySecurity.setParameter("Disabled");
      proxyUser.setEnabled(false);
      proxyPassword.setEnabled(false);
    }
  }

  public void update()
  {
    if (application instanceof VTServer)
    {
      VTServer server = (VTServer) application;
      VTServerConnector connector = server.getServerConnector();
      if (connector != null)
      {
        connector.setPassive(connectionMode.getParameter().equals("Passive"));
        connector.setAddress(connectionHost.getParameter());
        try
        {
          connector.setPort(Integer.parseInt(connectionPort.getParameter()));
        }
        catch (Throwable e)
        {
          connector.setPort(6060);
          // connector.setPort(null);
        }
        try
        {
          connector.setNatPort(Integer.parseInt(natPort.getParameter()));
        }
        catch (Throwable e)
        {
          connector.setNatPort(null);
        }
        connector.setEncryptionType(encryptionType.getParameter());
        try
        {
          connector.setEncryptionKey(encryptionPassword.getParameter().getBytes("UTF-8"));
        }
        catch (Throwable e)
        {

        }
        connector.setProxyType(proxyType.isEnabled() ? proxyType.getParameter() : "None");
        connector.setProxyAddress(proxyHost.getParameter());
        try
        {
          connector.setProxyPort(Integer.parseInt(proxyPort.getParameter()));
        }
        catch (Throwable e)
        {

        }
        connector.setUseProxyAuthentication(proxySecurity.isEnabled() ? proxySecurity.getParameter().equals("Enabled") : false);
        connector.setProxyUser(proxyUser.getParameter());
        connector.setProxyPassword(proxyPassword.getParameter());
        try
        {
          connector.setSessionsMaximum(Integer.parseInt(sessionsMaximum.getParameter()));
        }
        catch (Throwable e)
        {
          connector.setSessionsMaximum(0);
        }
        connector.setSessionShell(sessionShell.getParameter());
        String user = sessionUser.getParameter();
        if (user != null && user.length() > 0)
        {
          server.setUniqueUserCredential(sessionUser.getParameter(), sessionPassword.getParameter());
        }
        sessionUser.setParameter("");
        sessionPassword.setParameter("");
        // connector.setSkipConfig(true);
      }
      else
      {
        server.setPassive(connectionMode.getParameter().equals("Passive"));
        server.setAddress(connectionHost.getParameter());
        try
        {
          server.setPort(Integer.parseInt(connectionPort.getParameter()));
        }
        catch (Throwable e)
        {
          server.setPort(6060);
          // server.setPort(null);
        }
        try
        {
          server.setNatPort(Integer.parseInt(natPort.getParameter()));
        }
        catch (Throwable e)
        {
          server.setNatPort(null);
        }
        server.setEncryptionType(encryptionType.getParameter());
        try
        {
          server.setEncryptionKey(encryptionPassword.getParameter().getBytes("UTF-8"));
        }
        catch (Throwable e)
        {

        }
        server.setProxyType(proxyType.isEnabled() ? proxyType.getParameter() : "None");
        server.setProxyAddress(proxyHost.getParameter());
        try
        {
          server.setProxyPort(Integer.parseInt(proxyPort.getParameter()));
        }
        catch (Throwable e)
        {

        }
        server.setUseProxyAuthentication(proxySecurity.isEnabled() ? proxySecurity.getParameter().equals("Enabled") : false);
        server.setProxyUser(proxyUser.getParameter());
        server.setProxyPassword(proxyPassword.getParameter());
        try
        {
          //server.setSessionsLimit(Integer.parseInt(sessionsLimit.getParameter()));
          server.setSessionsMaximum(Integer.parseInt(sessionsMaximum.getParameter()));
        }
        catch (Throwable e)
        {
          server.setSessionsMaximum(0);
        }
        server.setSessionShell(sessionShell.getParameter());
        String user = sessionUser.getParameter();
        if (user != null && user.length() > 0)
        {
          server.setUniqueUserCredential(sessionUser.getParameter(), sessionPassword.getParameter());
        }
        sessionUser.setParameter("");
        sessionPassword.setParameter("");
      }
    }
  }

  public void execute()
  {
    update();
    // close();
    // VTConsole.println("");
    try
    {
      VTConsole.interruptReadLine();
    }
    catch (Throwable t)
    {

    }
    if (application instanceof VTServer)
    {
      VTServer server = (VTServer) application;
      // server.setSkipConfiguration(true);
      VTServerConnector connector = server.getServerConnector();
      if (connector != null && !server.isReconfigure())
      {
        // connector.setSkipConfiguration(true);
        //server.setSkipConfiguration(true);
        synchronized (connector)
        {
          connector.interruptConnector();
          connector.notify();
        }
      }
      else
      {
        server.setSkipConfiguration(true);
      }
    }
    close();
  }

  public void close()
  {
//		if (!isVisible())
//		{
//			synchronized (this)
//			{
//				try
//				{
//					opened = false;
//					notifyAll();
//				}
//				catch (Throwable t)
//				{
//					
//				}
//			}
//			return;
//		}
    try
    {
      setVisible(false);
    }
    catch (Throwable t)
    {

    }
    try
    {
      //dispose();
    }
    catch (Throwable t)
    {

    }
//		try
//		{
//			if (this.getOwner() != null)
//			{
//				this.getOwner().requestFocus();
//			}
//		}
//		catch (Throwable t)
//		{
//			
//		}
//		EventQueue.invokeLater(new Runnable()
//		{
//			public void run()
//			{
//				if (!isVisible())
//				{
//					return;
//				}
//				try
//				{
//					setVisible(false);
//				}
//				catch (Throwable t)
//				{
//					
//				}
//				try
//				{
//					dispose();
//				}
//				catch (Throwable t)
//				{
//					
//				}
//			}
//		});

//		final Object sync = this;
//		EventQueue.invokeLater(new Runnable()
//		{
//			public void run()
//			{
//				synchronized (sync)
//				{
//					try
//					{
//						notifyAll();
//					}
//					catch (Throwable t)
//					{
//						
//					}
//				}
//			}	
//		});

//		synchronized (this)
//		{
//			try
//			{
//				opened = false;
//				notifyAll();
//			}
//			catch (Throwable t)
//			{
//				
//			}
//		}

    // notify();
    // VTTerminal.interruptReadLine();
  }

  /*
   * public static void main(String[] args) throws Exception { Frame frame = new
   * Frame(); VTConnectionDialog dialog = new VTConnectionDialog(frame, "ABC",
   * false, null); dialog.initialize(); dialog.setLocationByPlatform(true);
   * dialog.setVisible(true); }
   */
}