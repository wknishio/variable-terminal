package org.vash.vate.client.dialog;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.connection.VTClientConnector;
import org.vash.vate.console.VTConsole;
import org.vash.vate.dialog.VTConfigurationDialogParameter;
import org.vash.vate.dialog.VTFileDialog;
import org.vash.vate.graphics.font.VTGlobalTextStyleManager;

public class VTClientConfigurationDialog extends Dialog
{
  private static final long serialVersionUID = 1L;
  
  private VTConfigurationDialogParameter connectionMode;
  private VTConfigurationDialogParameter connectionHost;
  private VTConfigurationDialogParameter connectionPort;
  private VTConfigurationDialogParameter natPort;
  private VTConfigurationDialogParameter proxyType;
  private VTConfigurationDialogParameter proxyHost;
  private VTConfigurationDialogParameter proxyPort;
  //private VTConfigurationDialogParameter proxySecurity;
  private VTConfigurationDialogParameter proxyUser;
  private VTConfigurationDialogParameter proxyPassword;
  private VTConfigurationDialogParameter encryptionType;
  private VTConfigurationDialogParameter encryptionPassword;
  
  private VTConfigurationDialogParameter sessionCommands;
  // private VTConfigurationDialogParameter sessionLines;
  private VTConfigurationDialogParameter sessionShell;
  private VTConfigurationDialogParameter sessionUser;
  private VTConfigurationDialogParameter sessionPassword;
  
  private VTConfigurationDialogParameter pingLimit;
  private VTConfigurationDialogParameter pingInterval;
  
  private Runnable application;
  private Frame owner;
  // private Button okButton;
  private static Method setIconImageMethod;
  
  static
  {
    try
    {
      setIconImageMethod = Dialog.class.getMethod("setIconImage", Class.forName("java.awt.Image"));
      // setIconImage.setAccessible(true);
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public VTClientConfigurationDialog(Frame owner, String title, boolean modal, Runnable application)
  {
    this(owner, title, modal, owner.getGraphicsConfiguration(), application);
  }
  
  public VTClientConfigurationDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc, final Runnable application)
  {
    super(owner, title, modal, gc);
    // super(title, gc);
    this.owner = owner;
    this.application = application;
    final VTFileDialog loadFileDialog = new VTFileDialog(this, "Variable-Terminal " + VT.VT_VERSION + " - Client - Load File", FileDialog.LOAD);
    final VTFileDialog saveFileDialog = new VTFileDialog(this, "Variable-Terminal " + VT.VT_VERSION + " - Client - Save File", FileDialog.SAVE);
    VTGlobalTextStyleManager.registerWindow(loadFileDialog);
    VTGlobalTextStyleManager.registerWindow(saveFileDialog);
    
    Set<AWTKeyStroke> forwardTraversalKeysGeneral = new HashSet<AWTKeyStroke>();
    Set<AWTKeyStroke> backwardTraversalKeysGeneral = new HashSet<AWTKeyStroke>();
    Set<AWTKeyStroke> forwardTraversalKeysButton = new HashSet<AWTKeyStroke>();
    Set<AWTKeyStroke> backwardTraversalKeysButton = new HashSet<AWTKeyStroke>();
    
    forwardTraversalKeysGeneral.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_DOWN, 0));
    forwardTraversalKeysGeneral.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_KP_DOWN, 0));
    forwardTraversalKeysGeneral.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
    forwardTraversalKeysGeneral.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
    forwardTraversalKeysGeneral.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK));
    
    backwardTraversalKeysGeneral.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_UP, 0));
    backwardTraversalKeysGeneral.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_KP_UP, 0));
    backwardTraversalKeysGeneral.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_PAGE_UP, 0));
    backwardTraversalKeysGeneral.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK));
    backwardTraversalKeysGeneral.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
    
    forwardTraversalKeysButton.addAll(forwardTraversalKeysGeneral);
    backwardTraversalKeysButton.addAll(backwardTraversalKeysGeneral);
    
    forwardTraversalKeysButton.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_RIGHT, 0));
    forwardTraversalKeysButton.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_KP_RIGHT, 0));
    
    backwardTraversalKeysButton.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_LEFT, 0));
    backwardTraversalKeysButton.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_KP_LEFT, 0));
    
    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardTraversalKeysGeneral);
    setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardTraversalKeysGeneral);
    
    try
    {
      if (setIconImageMethod != null)
      {
        setIconImageMethod.invoke(this, VT.remoteIcon);
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
    
    Choice connectionModeChoice = new Choice();
    connectionMode = new VTConfigurationDialogParameter("Connection Mode:", connectionModeChoice, true);
    TextField connectionHostField = new TextField(16);
    connectionHost = new VTConfigurationDialogParameter("Connection Host:", connectionHostField, true);
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
    connectionPort = new VTConfigurationDialogParameter("Connection Port:", connectionPortField, true);
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
    natPort = new VTConfigurationDialogParameter("Connection NAT Port:", natPortField, false);
    Choice proxyTypeChoice = new Choice();
    proxyType = new VTConfigurationDialogParameter("Proxy Type:", proxyTypeChoice, true);
    TextField proxyHostField = new TextField(16);
    proxyHost = new VTConfigurationDialogParameter("Proxy Host:", proxyHostField, false);
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
    proxyPort = new VTConfigurationDialogParameter("Proxy Port:", proxyPortField, false);
    //Choice proxySecurityChoice = new Choice();
    //proxySecurity = new VTClientConfigurationDialogParameter("Proxy Authentication:", proxySecurityChoice, false);
    TextField proxyUserField = new TextField(16);
    proxyUserField.setEchoChar('*');
    proxyUser = new VTConfigurationDialogParameter("Proxy User:", proxyUserField, false);
    TextField proxyPasswordField = new TextField(16);
    proxyPasswordField.setEchoChar('*');
    proxyPassword = new VTConfigurationDialogParameter("Proxy Password:", proxyPasswordField, false);
    Choice encryptionTypeChoice = new Choice();
    encryptionType = new VTConfigurationDialogParameter("Encryption Type:", encryptionTypeChoice, true);
    TextField encryptionPasswordField = new TextField(16);
    encryptionPasswordField.setEchoChar('*');
    encryptionPassword = new VTConfigurationDialogParameter("Encryption Password:", encryptionPasswordField, true);
    TextField sessionCommandField = new TextField(16);
    sessionCommands = new VTConfigurationDialogParameter("Session Commands:", sessionCommandField, true);
    // TextField sessionLinesField = new TextField(16);
    // sessionLines = new VTClientConfigurationDialogParameter("Session Lines:",
    // sessionLinesField, true);
    TextField sessionShellField = new TextField(16);
    sessionShell = new VTConfigurationDialogParameter("Session Shell:", sessionShellField, true);
    
    // BorderLayout dialogLayout = (BorderLayout)this.getLayout();
    
    // try
    // {
    // BorderLayout layout = (BorderLayout) this.getLayout();
    // layout.setHgap(1);
    // layout.setVgap(1);
    // }
    // catch (Throwable t)
    // {
    
    // }
    
    connectionModeChoice.add("ACTIVE");
    connectionModeChoice.add("PASSIVE");
    connectionModeChoice.select("ACTIVE");
    connectionModeChoice.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          if (e.getItem().equals("ACTIVE"))
          {
            setConnectionMode(true);
          }
          else if (e.getItem().equals("PASSIVE"))
          {
            setConnectionMode(false);
          }
        }
      }
    });
    
    proxyTypeChoice.add("NONE");
    proxyTypeChoice.add("DIRECT");
    proxyTypeChoice.add("SOCKS");
    proxyTypeChoice.add("HTTP");
    proxyTypeChoice.add("PLUS");
    proxyTypeChoice.select("NONE");
    proxyTypeChoice.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          if (e.getItem().equals("NONE"))
          {
            setProxyType("NONE");
          }
          else if (e.getItem().equals("DIRECT"))
          {
            setProxyType("DIRECT");
          }
          else if (e.getItem().equals("SOCKS"))
          {
            setProxyType("SOCKS");
          }
          else if (e.getItem().equals("HTTP"))
          {
            setProxyType("HTTP");
          }
          else if (e.getItem().equals("PLUS"))
          {
            setProxyType("PLUS");
          }
        }
      }
    });
    
//    proxySecurityChoice.add("Disabled");
//    proxySecurityChoice.add("Enabled");
//    proxySecurityChoice.select("Disabled");
//    proxySecurityChoice.addItemListener(new ItemListener()
//    {
//      public void itemStateChanged(ItemEvent e)
//      {
//        if (e.getStateChange() == ItemEvent.SELECTED)
//        {
//          if (e.getItem().equals("Disabled"))
//          {
//            setProxySecurity(false);
//          }
//          else if (e.getItem().equals("Enabled"))
//          {
//            setProxySecurity(true);
//          }
//        }
//      }
//    });
    
    encryptionTypeChoice.add("NONE");
    encryptionTypeChoice.add("ISAAC");
    encryptionTypeChoice.add("VMPC");
    encryptionTypeChoice.add("SALSA");
    encryptionTypeChoice.add("HC");
    //encryptionTypeChoice.add("GRAIN");
    encryptionTypeChoice.add("ZUC");
    // encryptionTypeChoice.add("BLOWFISH");
    encryptionTypeChoice.select("NONE");
    encryptionTypeChoice.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          if (e.getItem().equals("NONE"))
          {
            setEncryptionType("NONE");
          }
          else if (e.getItem().equals("VMPC"))
          {
            setEncryptionType("VMPC");
          }
          else if (e.getItem().equals("ZUC"))
          {
            setEncryptionType("ZUC");
          }
          // else if (e.getItem().equals("BLOWFISH"))
          // {
          // setEncryptionType("BLOWFISH");
          // }
          else if (e.getItem().equals("SALSA"))
          {
            setEncryptionType("SALSA");
          }
          else if (e.getItem().equals("HC"))
          {
            setEncryptionType("HC");
          }
//          else if (e.getItem().equals("GRAIN"))
//          {
//            setEncryptionType("GRAIN");
//          }
          else if (e.getItem().equals("ISAAC"))
          {
            setEncryptionType("ISAAC");
          }
        }
      }
    });
    
    // okButton.setEnabled(false);
    
    TextField connectionUserField = new TextField(16);
    connectionUserField.setEchoChar('*');
    sessionUser = new VTConfigurationDialogParameter("Session User:", connectionUserField, true);
    
    TextField connectionPasswordField = new TextField(16);
    connectionPasswordField.setEchoChar('*');
    sessionPassword = new VTConfigurationDialogParameter("Session Password:", connectionPasswordField, true);
    
    TextField pingIntervalField = new TextField(16);
    pingIntervalField.addTextListener(new TextListener()
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
    pingInterval = new VTConfigurationDialogParameter("Ping Interval:", pingIntervalField, true);
    
    TextField pingLimitField = new TextField(16);
    pingLimitField.addTextListener(new TextListener()
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
    pingLimit = new VTConfigurationDialogParameter("Ping Limit:", pingLimitField, true);
    
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
    
    Button loadButton = new Button("Load");
    Button saveButton = new Button("Save");
    
    Panel buttonPanel1 = new Panel();
    GridLayout buttonLayout1 = new GridLayout(1, 2);
    buttonLayout1.setHgap(1);
    buttonLayout1.setVgap(1);
    buttonPanel1.setLayout(buttonLayout1);
    buttonPanel1.add(saveButton);
    buttonPanel1.add(loadButton);
    
    loadButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        loadFileDialog.setDirectory(System.getProperty("user.dir"));
        loadFileDialog.setFile("vate-client.properties");
        loadFileDialog.setVisible(true);
        if (loadFileDialog.getFile() != null)
        {
          if (application instanceof VTClient)
          {
            VTClient client = (VTClient) application;
            try
            {
              client.loadClientSettingsFile(new File(loadFileDialog.getDirectory(), loadFileDialog.getFile()).getAbsolutePath());
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
        saveFileDialog.setFile("vate-client.properties");
        saveFileDialog.setVisible(true);
        if (saveFileDialog.getFile() != null)
        {
          if (application instanceof VTClient)
          {
            VTClient client = (VTClient) application;
            update();
            try
            {
              client.saveClientSettingsFile(new File(saveFileDialog.getDirectory(), saveFileDialog.getFile()).getAbsolutePath());
            }
            catch (Throwable t)
            {
              // t.printStackTrace();
            }
          }
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
          loadFileDialog.setFile("vate-client.properties");
          loadFileDialog.setVisible(true);
          if (loadFileDialog.getFile() != null)
          {
            if (application instanceof VTClient)
            {
              VTClient client = (VTClient) application;
              try
              {
                client.loadClientSettingsFile(new File(loadFileDialog.getDirectory(), loadFileDialog.getFile()).getAbsolutePath());
              }
              catch (Throwable t)
              {
                // t.printStackTrace();
              }
              load();
            }
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
          saveFileDialog.setFile("vate-client.properties");
          saveFileDialog.setVisible(true);
          if (saveFileDialog.getFile() != null)
          {
            if (application instanceof VTClient)
            {
              VTClient client = (VTClient) application;
              update();
              try
              {
                client.saveClientSettingsFile(new File(saveFileDialog.getDirectory(), saveFileDialog.getFile()).getAbsolutePath());
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
    
    loadButton.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardTraversalKeysButton);
    loadButton.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardTraversalKeysButton);
    
    saveButton.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardTraversalKeysButton);
    saveButton.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardTraversalKeysButton);
    
    okButton.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardTraversalKeysButton);
    okButton.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardTraversalKeysButton);
    
    closeButton.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardTraversalKeysButton);
    closeButton.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardTraversalKeysButton);
    
    Panel centerPanel = new Panel();
    GridLayout centerLayout = new GridLayout(19, 1);
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
    //centerPanel.add(proxySecurity);
    centerPanel.add(proxyUser);
    centerPanel.add(proxyPassword);
    
    centerPanel.add(encryptionType);
    centerPanel.add(encryptionPassword);
    
    centerPanel.add(pingLimit);
    centerPanel.add(pingInterval);
    
    centerPanel.add(sessionShell);
    centerPanel.add(sessionCommands);
    // centerPanel.add(sessionLines);
    
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
            for (final Component element2 : panel2.getComponents())
            {
              element2.addKeyListener(new KeyListener()
              {
                public void keyPressed(KeyEvent e)
                {
                  if (VTGlobalTextStyleManager.processKeyEvent(e))
                  {
                    return;
                  }
                  if (e.isShiftDown() && !e.isControlDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_INSERT)
                  {
                    e.consume();
                    if (element2 instanceof TextComponent)
                    {
                      try
                      {
                        String clipboardText = "";
                        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
                        {
                          clipboardText = systemClipboard.getData(DataFlavor.stringFlavor).toString();
                        }
                        else if (systemClipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
                        {
                          @SuppressWarnings("unchecked")
                          List<File> files = (List<File>) systemClipboard.getData(DataFlavor.javaFileListFlavor);
                          if (files.size() > 0)
                          {
                            StringBuilder fileList = new StringBuilder();
                            String fileListString = "";
                            for (File file : files)
                            {
                              fileList.append(" " + file.getAbsolutePath());
                            }
                            fileListString = fileList.substring(1);
                            clipboardText = fileListString;
                          }
                        }
                        TextComponent textComponent = (TextComponent)element2;
                        int start = textComponent.getSelectionStart();
                        int end = textComponent.getSelectionEnd();
                        String componentText = textComponent.getText();
                        String modifiedText = componentText.substring(0, start) + clipboardText + componentText.substring(end, componentText.length());
                        textComponent.setText(modifiedText);
                        textComponent.setCaretPosition(start + clipboardText.length());
                      }
                      catch (Throwable t)
                      {
                        
                      }
                      return;
                    }
                  }
                  if (e.isControlDown() && !e.isShiftDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_INSERT)
                  {
                    e.consume();
                    if (element2 instanceof TextComponent)
                    {
                      try
                      {
                        TextComponent textComponent = (TextComponent)element2;
                        String selectedText = textComponent.getSelectedText();
                        StringSelection text = null;
                        if (selectedText != null)
                        {
                          text = new StringSelection(selectedText);
                        }
                        else
                        {
                          text = new StringSelection("");
                        }
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, null);
                      }
                      catch (Throwable t)
                      {
                        
                      }
                    }
                    return;
                  }
                  if (e.isControlDown() && !e.isShiftDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                  {
                    e.consume();
                    if (element2 instanceof TextComponent)
                    {
                      try
                      {
                        TextComponent textComponent = (TextComponent)element2;
                        String selectedText = textComponent.getText();
                        StringSelection text = null;
                        if (selectedText != null)
                        {
                          text = new StringSelection(selectedText);
                        }
                        else
                        {
                          text = new StringSelection("");
                        }
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, null);
                      }
                      catch (Throwable t)
                      {
                        
                      }
                    }
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
              
              try
              {
                element2.setDropTarget(new DropTarget());
                element2.getDropTarget().setActive(true);
                element2.getDropTarget().addDropTargetListener(new DropTargetListener()
                {
                  public void dragEnter(DropTargetDragEvent dtde)
                  {
                    
                  }
                  public void dragOver(DropTargetDragEvent dtde)
                  {
                    
                  }
                  public void dropActionChanged(DropTargetDragEvent dtde)
                  {
                    
                  }
                  public void dragExit(DropTargetEvent dte)
                  {
                    
                  }
                  @SuppressWarnings("unchecked")
                  public void drop(DropTargetDropEvent dtde)
                  {
                    try
                    {
                      Transferable transferable = dtde.getTransferable();
                      String clipboardText = null;
                      if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                      {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        if (files.size() > 0)
                        {
                          StringBuilder fileList = new StringBuilder();
                          String fileListString = "";
                          for (File file : files)
                          {
                            fileList.append(" " + file.getAbsolutePath());
                          }
                          fileListString = fileList.substring(1);
                          clipboardText = fileListString;
                        }
                        if (element2 instanceof TextComponent && clipboardText != null)
                        {
                          TextComponent textComponent = (TextComponent)element2;
                          int start = textComponent.getSelectionStart();
                          int end = textComponent.getSelectionEnd();
                          String componentText = textComponent.getText();
                          String modifiedText = componentText.substring(0, start) + clipboardText + componentText.substring(end, componentText.length());
                          textComponent.setText(modifiedText);
                          textComponent.setCaretPosition(start + clipboardText.length());
                        }
                        dtde.dropComplete(true);
                      }
                      else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
                      {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        String data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                        clipboardText = data;
                        if (element2 instanceof TextComponent && clipboardText != null)
                        {
                          TextComponent textComponent = (TextComponent)element2;
                          int start = textComponent.getSelectionStart();
                          int end = textComponent.getSelectionEnd();
                          String componentText = textComponent.getText();
                          String modifiedText = componentText.substring(0, start) + clipboardText + componentText.substring(end, componentText.length());
                          textComponent.setText(modifiedText);
                          textComponent.setCaretPosition(start + clipboardText.length());
                        }
                        dtde.dropComplete(true);
                      }
                      else
                      {
                        dtde.rejectDrop();
                      }
                    }
                    catch (Throwable e)
                    {
                      // e.printStackTrace();
                    }
                  }
                });
              }
              catch (Throwable t)
              {
                
              }
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
    // setMinimumSize(getSize());
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
  
  public void load()
  {
    if (application instanceof VTClient)
    {
      VTClient client = (VTClient) application;
      VTClientConnector connector = client.getClientConnector();
      if (connector != null)
      {
        setConnectionMode(connector.isActive());
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
        //setProxySecurity(connector.isUseProxyAuthentication());
        proxyUser.setParameter(connector.getProxyUser());
        proxyPassword.setParameter(connector.getProxyPassword());
        sessionUser.setParameter(client.getUser());
        sessionPassword.setParameter(client.getPassword());
        sessionCommands.setParameter(connector.getSessionCommands());
        // sessionLines.setParameter(connector.getSessionLines());
        sessionShell.setParameter(connector.getSessionShell());
        pingLimit.setParameter(client.getPingLimit() > 0 ? client.getPingLimit() : "");
        pingInterval.setParameter(client.getPingInterval() > 0 ? client.getPingInterval() : "");
      }
      else
      {
        setConnectionMode(client.isActive());
        connectionHost.setParameter(client.getAddress());
        if (client.getPort() != null && client.getPort() > 0)
        {
          connectionPort.setParameter(client.getPort());
        }
        else
        {
          connectionPort.setParameter(6060);
          // connectionPort.setParameter("");
        }
        natPort.setParameter(client.getNatPort());
        setEncryptionType(client.getEncryptionType());
        encryptionPassword.setParameter(client.getEncryptionKey());
        setProxyType(client.getProxyType());
        proxyHost.setParameter(client.getProxyAddress());
        proxyPort.setParameter(client.getProxyPort());
        //setProxySecurity(client.isUseProxyAuthentication());
        proxyUser.setParameter(client.getProxyUser());
        proxyPassword.setParameter(client.getProxyPassword());
        sessionUser.setParameter(client.getUser());
        sessionPassword.setParameter(client.getPassword());
        sessionCommands.setParameter(client.getSessionCommands());
        // sessionLines.setParameter(client.getSessionLines());
        sessionShell.setParameter(client.getSessionShell());
        pingLimit.setParameter(client.getPingLimit() > 0 ? client.getPingLimit() : "");
        pingInterval.setParameter(client.getPingInterval() > 0 ? client.getPingInterval() : "");
      }
    }
  }
  
  private void setConnectionMode(boolean active)
  {
    if (active)
    {
      connectionMode.setParameter("ACTIVE");
      // connectionHost.setEnabled(true);
      natPort.setEnabled(false);
      proxyType.setEnabled(true);
      if (!proxyType.getParameter().equalsIgnoreCase("NONE"))
      {
        proxyHost.setEnabled(true);
        proxyPort.setEnabled(true);
        proxyUser.setEnabled(true);
        proxyPassword.setEnabled(true);
        //proxySecurity.setEnabled(true);
//        if (!proxySecurity.getParameter().equalsIgnoreCase("Disabled"))
//        {
//          proxyUser.setEnabled(true);
//          proxyPassword.setEnabled(true);
//        }
      }
    }
    else
    {
      connectionMode.setParameter("PASSIVE");
      // connectionHost.setEnabled(false);
      natPort.setEnabled(true);
      setProxyType(null);
      proxyType.setEnabled(false);
      proxyHost.setEnabled(false);
      proxyPort.setEnabled(false);
      //proxySecurity.setEnabled(false);
      proxyUser.setEnabled(false);
      proxyPassword.setEnabled(false);
    }
  }
  
  private void setEncryptionType(String encryption)
  {
    if (encryption == null)
    {
      encryptionType.setParameter("NONE");
      // encryptionPassword.setEnabled(false);
    }
    else if (encryption.toUpperCase().startsWith("V"))
    {
      encryptionType.setParameter("VMPC");
      // encryptionPassword.setEnabled(true);
    }
    else if (encryption.toUpperCase().startsWith("Z"))
    {
      encryptionType.setParameter("ZUC");
      // encryptionPassword.setEnabled(true);
    }
    else if (encryption.toUpperCase().startsWith("S"))
    {
      encryptionType.setParameter("SALSA");
      // encryptionPassword.setEnabled(true);
    }
    else if (encryption.toUpperCase().startsWith("H"))
    {
      encryptionType.setParameter("HC");
      // encryptionPassword.setEnabled(true);
    }
    else if (encryption.toUpperCase().startsWith("I"))
    {
      encryptionType.setParameter("ISAAC");
      // encryptionPassword.setEnabled(true);
    }
    else
    {
      encryptionType.setParameter("NONE");
      // encryptionPassword.setEnabled(false);
    }
  }
  
  private void setProxyType(String proxy)
  {
    if (proxy == null)
    {
      proxyType.setParameter("NONE");
      proxyHost.setEnabled(false);
      proxyPort.setEnabled(false);
      proxyUser.setEnabled(false);
      proxyPassword.setEnabled(false);
    }
    else if (proxy.toUpperCase().startsWith("D"))
    {
      proxyType.setParameter("DIRECT");
      proxyHost.setEnabled(false);
      proxyPort.setEnabled(false);
      proxyUser.setEnabled(false);
      proxyPassword.setEnabled(false);
    }
    else if (proxy.toUpperCase().startsWith("S"))
    {
      proxyType.setParameter("SOCKS");
      proxyHost.setEnabled(true);
      proxyPort.setEnabled(true);
      proxyUser.setEnabled(true);
      proxyPassword.setEnabled(true);
    }
    else if (proxy.toUpperCase().startsWith("H"))
    {
      proxyType.setParameter("HTTP");
      proxyHost.setEnabled(true);
      proxyPort.setEnabled(true);
      proxyUser.setEnabled(true);
      proxyPassword.setEnabled(true);
    }
    else if (proxy.toUpperCase().startsWith("P"))
    {
      proxyType.setParameter("PLUS");
      proxyHost.setEnabled(true);
      proxyPort.setEnabled(true);
      proxyUser.setEnabled(true);
      proxyPassword.setEnabled(true);
    }
    else
    {
      proxyType.setParameter("NONE");
      proxyHost.setEnabled(false);
      proxyPort.setEnabled(false);
      proxyUser.setEnabled(false);
      proxyPassword.setEnabled(false);
    }
  }
  
//  public void setProxySecurity(boolean security)
//  {
//    if (security)
//    {
//      proxySecurity.setParameter("Enabled");
//      proxyUser.setEnabled(true);
//      proxyPassword.setEnabled(true);
//    }
//    else
//    {
//      proxySecurity.setParameter("Disabled");
//      proxyUser.setEnabled(false);
//      proxyPassword.setEnabled(false);
//    }
//  }
  
  public void update()
  {
    if (application instanceof VTClient)
    {
      VTClient client = (VTClient) application;
      VTClientConnector connector = client.getClientConnector();
      if (connector != null)
      {
        connector.setActive(connectionMode.getParameter().equals("ACTIVE"));
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
          
        }
        connector.setEncryptionType(encryptionType.getParameter());
        try
        {
          connector.setEncryptionKey(encryptionPassword.getParameter().getBytes("UTF-8"));
        }
        catch (Throwable e)
        {
          
        }
        connector.setProxyType(proxyType.isEnabled() ? proxyType.getParameter() : "NONE");
        connector.setProxyAddress(proxyHost.getParameter());
        try
        {
          connector.setProxyPort(Integer.parseInt(proxyPort.getParameter()));
        }
        catch (Throwable e)
        {
          
        }
        //connector.setUseProxyAuthentication(proxySecurity.isEnabled() ? proxySecurity.getParameter().equals("Enabled") : false);
        connector.setProxyUser(proxyUser.getParameter());
        connector.setProxyPassword(proxyPassword.getParameter());
        client.setUser(sessionUser.getParameter());
        client.setPassword(sessionPassword.getParameter());
        sessionUser.setParameter("");
        sessionPassword.setParameter("");
        connector.setSessionCommands(sessionCommands.getParameter());
        // connector.setSessionLines(sessionLines.getParameter());
        connector.setSessionShell(sessionShell.getParameter());
        try
        {
          int limit = Integer.parseInt(pingLimit.getParameter());
          if (limit > 0)
          {
            client.setPingLimit(limit);
          }
          else
          {
            client.setPingLimit(0);
          }
        }
        catch (Throwable e)
        {
          client.setPingLimit(0);
        }
        try
        {
          int interval = Integer.parseInt(pingInterval.getParameter());
          if (interval > 0)
          {
            client.setPingInterval(interval);
          }
          else
          {
            client.setPingInterval(0);
          }
        }
        catch (Throwable e)
        {
          client.setPingInterval(0);
        }
        /*
         * if (VTTerminal.isGraphical()) { try {
         * //connector.getHandler().getConnection().setSkipLine(true);
         * VTGraphicalConsoleReader.getCurrentThread().interrupt(); } catch
         * (Throwable t) { } }
         */
      }
      else
      {
        client.setActive(connectionMode.getParameter().equals("ACTIVE"));
        client.setAddress(connectionHost.getParameter());
        try
        {
          client.setPort(Integer.parseInt(connectionPort.getParameter()));
        }
        catch (Throwable e)
        {
          client.setPort(6060);
          // client.setPort(null);
        }
        try
        {
          client.setNatPort(Integer.parseInt(natPort.getParameter()));
        }
        catch (Throwable e)
        {
          
        }
        client.setEncryptionType(encryptionType.getParameter());
        try
        {
          client.setEncryptionKey(encryptionPassword.getParameter().getBytes("UTF-8"));
        }
        catch (Throwable e)
        {
          
        }
        client.setProxyType(proxyType.isEnabled() ? proxyType.getParameter() : "NONE");
        client.setProxyAddress(proxyHost.getParameter());
        try
        {
          client.setProxyPort(Integer.parseInt(proxyPort.getParameter()));
        }
        catch (Throwable e)
        {
          
        }
        //client.setUseProxyAuthentication(proxySecurity.isEnabled() ? proxySecurity.getParameter().equals("Enabled") : false);
        client.setProxyUser(proxyUser.getParameter());
        client.setProxyPassword(proxyPassword.getParameter());
        client.setUser(sessionUser.getParameter());
        client.setPassword(sessionPassword.getParameter());
        sessionUser.setParameter("");
        sessionPassword.setParameter("");
        client.setSessionCommands(sessionCommands.getParameter());
        // client.setSessionLines(sessionLines.getParameter());
        client.setSessionShell(sessionShell.getParameter());
        try
        {
          int interval = Integer.parseInt(pingInterval.getParameter());
          if (interval > 0)
          {
            client.setPingInterval(interval);
          }
          else
          {
            client.setPingInterval(0);
          }
        }
        catch (Throwable e)
        {
          client.setPingInterval(0);
        }
        try
        {
          int limit = Integer.parseInt(pingLimit.getParameter());
          if (limit > 0)
          {
            client.setPingLimit(limit);
          }
          else
          {
            client.setPingLimit(0);
          }
        }
        catch (Throwable e)
        {
          client.setPingLimit(0);
        }
      }
    }
  }
  
  public void execute()
  {
    update();
    close();
    if (application instanceof VTClient)
    {
      VTClient client = (VTClient) application;
      client.setSkipConfiguration(true);
      VTClientConnector connector = client.getClientConnector();
      if (connector != null)
      {
        connector.setSkipConfiguration(true);
        // connector.setRetryOnce(true);
        synchronized (connector)
        {
          connector.interruptConnector();
          connector.notify();
        }
        VTClientConnection connection = connector.getHandler().getConnection();
        if (connection.isConnected())
        {
          connection.closeSockets();
        }
        else
        {
          connector.stopConnectionRetryTimeoutThread();
          connector.triggerConnectionRetryTimeoutThread();
        }
        // VTConsole.println("");
      }
      else
      {
        if (client.isManual())
        {
          // System.out.println("manual");
          VTConsole.println("");
          VTConsole.interruptReadLine();
        }
        // VTConsole.println("");
      }
    }
  }
  
  public void close()
  {
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
  }
  
  /*
   * public static void main(String[] args) throws Exception { Frame frame = new
   * Frame(); VTConnectionDialog dialog = new VTConnectionDialog(frame, "ABC",
   * false, null); dialog.initialize(); dialog.setLocationByPlatform(true);
   * dialog.setVisible(true); }
   */
}