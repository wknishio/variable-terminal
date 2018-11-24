package org.vate.client.dialog;

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

import javax.imageio.ImageIO;

import org.vate.VT;
import org.vate.client.VTClient;
import org.vate.client.connection.VTClientConnection;
import org.vate.client.connection.VTClientConnector;
import org.vate.dialog.VTFileDialog;
import org.vate.graphics.font.VTGlobalTextStyleManager;

public class VTClientConnectionDialog extends Dialog
{
	private static final long serialVersionUID = 1L;
	
	private VTClientConnectionDialogParameter connectionMode;
	private VTClientConnectionDialogParameter connectionHost;
	private VTClientConnectionDialogParameter connectionPort;
	private VTClientConnectionDialogParameter authenticationLogin;
	private VTClientConnectionDialogParameter authenticationPassword;
	private VTClientConnectionDialogParameter natPort;
	private VTClientConnectionDialogParameter proxyType;
	private VTClientConnectionDialogParameter proxyHost;
	private VTClientConnectionDialogParameter proxyPort;
	private VTClientConnectionDialogParameter proxySecurity;
	private VTClientConnectionDialogParameter proxyUser;
	private VTClientConnectionDialogParameter proxyPassword;
	private VTClientConnectionDialogParameter encryptionType;
	private VTClientConnectionDialogParameter encryptionPassword;
	private VTClientConnectionDialogParameter sessionCommands;
	
	// private VTConnectionDialogParameter sessionsLimit;
	private Runnable application;
	private Frame owner;
	//private volatile boolean opened;
	// private Button okButton;
	
	public VTClientConnectionDialog(Frame owner, String title, boolean modal, Runnable application)
	{
		this(owner, title, modal, owner.getGraphicsConfiguration(), application);
		this.owner = owner;
		this.application = application;
	}
	
	public VTClientConnectionDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc, final Runnable application)
	{
		super(owner, title, modal, gc);
		//super(title, gc);
		this.owner = owner;
		this.application = application;
		final VTFileDialog loadFileDialog = new VTFileDialog(this, "Variable-Terminal Client " + VT.VT_VERSION + " - Load File", FileDialog.LOAD);
		final VTFileDialog saveFileDialog = new VTFileDialog(this, "Variable-Terminal Client " + VT.VT_VERSION + " - Save File", FileDialog.SAVE);
		VTGlobalTextStyleManager.registerWindow(loadFileDialog);
		VTGlobalTextStyleManager.registerWindow(saveFileDialog);
		try
		{
			this.setIconImage(ImageIO.read(this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/terminal.png")));
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
		
		Panel centerPanel = new Panel();
		GridLayout centerLayout = new GridLayout(17, 1);
		centerLayout.setHgap(1);
		centerLayout.setVgap(1);
		centerPanel.setLayout(centerLayout);
		//centerPanel.getInsets().set(4, 4, 4, 4);
		
		Choice connectionModeChoice = new Choice();
		connectionMode = new VTClientConnectionDialogParameter("Connection Mode:", connectionModeChoice, true);
		TextField connectionHostField = new TextField(20);
		connectionHost = new VTClientConnectionDialogParameter("Connection Host:", connectionHostField, true);
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
		connectionPort = new VTClientConnectionDialogParameter("Connection Port:", connectionPortField, true);
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
		natPort = new VTClientConnectionDialogParameter("Connection NAT Port:", natPortField, false);
		Choice proxyTypeChoice = new Choice();
		proxyType = new VTClientConnectionDialogParameter("Proxy Type:", proxyTypeChoice, true);
		TextField proxyHostField = new TextField(20);
		proxyHost = new VTClientConnectionDialogParameter("Proxy Host:", proxyHostField, false);
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
		proxyPort = new VTClientConnectionDialogParameter("Proxy Port:", proxyPortField, false);
		Choice proxySecurityChoice = new Choice();
		proxySecurity = new VTClientConnectionDialogParameter("Proxy Authentication:", proxySecurityChoice, false);
		TextField proxyUserField = new TextField(20);
		proxyUserField.setEchoChar('*');
		proxyUser = new VTClientConnectionDialogParameter("Proxy User:", proxyUserField, false);
		TextField proxyPasswordField = new TextField(20);
		proxyPasswordField.setEchoChar('*');
		proxyPassword = new VTClientConnectionDialogParameter("Proxy Password:", proxyPasswordField, false);
		Choice encryptionTypeChoice = new Choice();
		encryptionType = new VTClientConnectionDialogParameter("Encryption Type:", encryptionTypeChoice, true);
		TextField encryptionPasswordField = new TextField(20);
		encryptionPasswordField.setEchoChar('*');
		encryptionPassword = new VTClientConnectionDialogParameter("Encryption Password:", encryptionPasswordField, false);
		TextField sessionCommandField = new TextField(20);
		sessionCommands = new VTClientConnectionDialogParameter("Session Commands:", sessionCommandField, true);
		
		// BorderLayout dialogLayout = (BorderLayout)this.getLayout();
		
		//add(new Panel(), BorderLayout.EAST);
		//add(new Panel(), BorderLayout.WEST);
		//add(new Panel(), BorderLayout.NORTH);
		//add(new Panel(), BorderLayout.SOUTH);
		add(centerPanel, BorderLayout.CENTER);
		//try
		//{
			//BorderLayout layout = (BorderLayout) this.getLayout();
			//layout.setHgap(1);
			//layout.setVgap(1);
		//}
		//catch (Throwable t)
		//{
			
		//}
		
		connectionModeChoice.add("Active");
		connectionModeChoice.add("Passive");
		connectionModeChoice.select("Active");
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
				}
			}
		});
		
		// okButton.setEnabled(false);
		
		TextField connectionLoginField = new TextField(20);
		connectionLoginField.setEchoChar('*');
		authenticationLogin = new VTClientConnectionDialogParameter("Authentication Login:", connectionLoginField, true);
		
		TextField connectionPasswordField = new TextField(20);
		connectionPasswordField.setEchoChar('*');
		authenticationPassword = new VTClientConnectionDialogParameter("Authentication Password:", connectionPasswordField, true);
		
		centerPanel.add(connectionMode);
		centerPanel.add(connectionHost);
		centerPanel.add(connectionPort);
		centerPanel.add(natPort);
		
		centerPanel.add(encryptionType);
		centerPanel.add(encryptionPassword);
		centerPanel.add(proxyType);
		centerPanel.add(proxyHost);
		centerPanel.add(proxyPort);
		centerPanel.add(proxySecurity);
		centerPanel.add(proxyUser);
		centerPanel.add(proxyPassword);
		
		centerPanel.add(authenticationLogin);
		centerPanel.add(authenticationPassword);
		
		centerPanel.add(sessionCommands);
		
		addKeyListener(new KeyListener()
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
					execute();
				}
				if (e.getKeyChar() == '\u001B')
				{
					close();
				}
			}
		});
		
		for (Component component : centerPanel.getComponents())
		{
			if (component instanceof Panel)
			{
				Panel panel = (Panel) component;
				for (Component element : panel.getComponents())
				{
					element.addKeyListener(new KeyListener()
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
								execute();
							}
							if (e.getKeyChar() == '\u001B')
							{
								close();
							}
						}
					});
				}
			}
		}
		
		Button loadButton = new Button("Load");
		Button saveButton = new Button("Save");
		
		Panel buttonPanel1 = new Panel();
		GridLayout buttonLayout1 = new GridLayout(1, 2);
		buttonLayout1.setHgap(1);
		buttonLayout1.setVgap(1);
		buttonPanel1.setLayout(buttonLayout1);
		buttonPanel1.add(loadButton);
		buttonPanel1.add(saveButton);
		centerPanel.add(buttonPanel1);
		
		loadButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				loadFileDialog.setDirectory(System.getProperty("user.dir"));
				loadFileDialog.setFile("variable-terminal-client.properties");
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
				saveFileDialog.setFile("variable-terminal-client.properties");
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
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
				{
					e.consume();
					VTGlobalTextStyleManager.decreaseFontSize();
					return;
				}
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_PAGE_UP)
				{
					e.consume();
					VTGlobalTextStyleManager.increaseFontSize();
					return;
				}
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_HOME)
				{
					e.consume();
					VTGlobalTextStyleManager.defaultFontSize();
					return;
				}
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_END)
				{
					e.consume();
					if (VTGlobalTextStyleManager.isFontStyleBold())
					{
						VTGlobalTextStyleManager.disableFontStyleBold();
					}
					else
					{
						VTGlobalTextStyleManager.enableFontStyleBold();
					}
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
					loadFileDialog.setFile("variable-terminal-client.properties");
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
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
				{
					e.consume();
					VTGlobalTextStyleManager.decreaseFontSize();
					return;
				}
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_PAGE_UP)
				{
					e.consume();
					VTGlobalTextStyleManager.increaseFontSize();
					return;
				}
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_HOME)
				{
					e.consume();
					VTGlobalTextStyleManager.defaultFontSize();
					return;
				}
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_END)
				{
					e.consume();
					if (VTGlobalTextStyleManager.isFontStyleBold())
					{
						VTGlobalTextStyleManager.disableFontStyleBold();
					}
					else
					{
						VTGlobalTextStyleManager.enableFontStyleBold();
					}
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
					saveFileDialog.setFile("variable-terminal-client.properties");
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
		
		Button okButton = new Button("Connect");
		Button closeButton = new Button("Cancel");
		
		Panel buttonPanel2 = new Panel();
		GridLayout buttonLayout2 = new GridLayout(1, 2);
		buttonLayout2.setHgap(1);
		buttonLayout2.setVgap(1);
		buttonPanel2.setLayout(buttonLayout2);
		buttonPanel2.add(okButton);
		buttonPanel2.add(closeButton);
		centerPanel.add(buttonPanel2);
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
			}
			
			public void keyReleased(KeyEvent e)
			{
				
			}
			
			public void keyTyped(KeyEvent e)
			{
				if (e.getKeyChar() == '\n')
				{
					execute();
				}
				if (e.getKeyChar() == '\u001B')
				{
					close();
				}
			}
		});
		
		closeButton.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
				{
					e.consume();
					VTGlobalTextStyleManager.decreaseFontSize();
					return;
				}
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_PAGE_UP)
				{
					e.consume();
					VTGlobalTextStyleManager.increaseFontSize();
					return;
				}
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_HOME)
				{
					e.consume();
					VTGlobalTextStyleManager.defaultFontSize();
					return;
				}
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_END)
				{
					e.consume();
					if (VTGlobalTextStyleManager.isFontStyleBold())
					{
						VTGlobalTextStyleManager.disableFontStyleBold();
					}
					else
					{
						VTGlobalTextStyleManager.enableFontStyleBold();
					}
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
		textMenu.add(defaultTextMenu);
		textMenu.add(toggleBoldTextMenu);
		
		menubar.add(textMenu);
		textMenu.setEnabled(true);
		
		//this.setMenuBar(menubar);
		
		setResizable(true);
		pack();
		// setMinimumSize(getSize());
	}
	
	public void open()
	{
		if (isVisible())
		{
			//opened = true;
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
				setProxySecurity(connector.isUseProxyAuthentication());
				proxyUser.setParameter(connector.getProxyUser());
				proxyPassword.setParameter(connector.getProxyPassword());
				authenticationLogin.setParameter(client.getLogin());
				authenticationPassword.setParameter(client.getPassword());
				sessionCommands.setParameter(connector.getSessionCommands());
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
				setProxySecurity(client.isUseProxyAuthentication());
				proxyUser.setParameter(client.getProxyUser());
				proxyPassword.setParameter(client.getProxyPassword());
				authenticationLogin.setParameter(client.getLogin());
				authenticationPassword.setParameter(client.getPassword());
				sessionCommands.setParameter(client.getSessionCommands());
			}
		}
	}
	
	private void setConnectionMode(boolean active)
	{
		if (active)
		{
			connectionMode.setParameter("Active");
			//connectionHost.setEnabled(true);
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
			//connectionHost.setEnabled(false);
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
			encryptionPassword.setEnabled(false);
		}
		else if (encryption.toUpperCase().startsWith("R"))
		{
			encryptionType.setParameter("RC4");
			encryptionPassword.setEnabled(true);
		}
		else if (encryption.toUpperCase().startsWith("A"))
		{
			encryptionType.setParameter("AES");
			encryptionPassword.setEnabled(true);
		}
		else
		{
			encryptionType.setParameter("None");
			encryptionPassword.setEnabled(false);
		}
	}
	
	private void setProxyType(String proxy)
	{
		if (proxy == null)
		{
			proxyType.setParameter("None");
			proxyHost.setEnabled(false);
			proxyPort.setEnabled(false);
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
		if (application instanceof VTClient)
		{
			VTClient client = (VTClient) application;
			VTClientConnector connector = client.getClientConnector();
			if (connector != null)
			{
				connector.setActive(connectionMode.getParameter().equals("Active"));
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
				client.setLogin(authenticationLogin.getParameter());
				client.setPassword(authenticationPassword.getParameter());
				authenticationLogin.setParameter("");
				authenticationPassword.setParameter("");
				connector.setSessionCommands(sessionCommands.getParameter());
				
				/* if (VTTerminal.isGraphical()) { try {
				 * //connector.getHandler().getConnection().setSkipLine(true);
				 * VTGraphicalConsoleReader.getCurrentThread().interrupt(); }
				 * catch (Throwable t) {
				 * } } */
			}
			else
			{
				client.setActive(connectionMode.getParameter().equals("Active"));
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
				client.setProxyType(proxyType.isEnabled() ? proxyType.getParameter() : "None");
				client.setProxyAddress(proxyHost.getParameter());
				try
				{
					client.setProxyPort(Integer.parseInt(proxyPort.getParameter()));
				}
				catch (Throwable e)
				{
					
				}
				client.setUseProxyAuthentication(proxySecurity.isEnabled() ? proxySecurity.getParameter().equals("Enabled") : false);
				client.setProxyUser(proxyUser.getParameter());
				client.setProxyPassword(proxyPassword.getParameter());
				client.setLogin(authenticationLogin.getParameter());
				client.setPassword(authenticationPassword.getParameter());
				authenticationLogin.setParameter("");
				authenticationPassword.setParameter("");
				client.setSessionCommands(sessionCommands.getParameter());
			}
		}
	}
	
	public void execute()
	{
		update();
		//close();
//		VTConsole.println("");
//		try
//		{
//			VTConsole.interruptReadLine();
//		}
//		catch (Throwable t)
//		{
//			
//		}
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
					connector.triggerConnectionRetryTimeoutThread();
				}
				//VTConsole.println("");
			}
			else
			{
				//VTConsole.println("");
			}
		}
		close();
//		VTConsole.println("");
//		try
//		{
//			VTConsole.interruptReadLine();
//		}
//		catch (Throwable t)
//		{
//			
//		}
		// try
		// {
		// if (VTConsole.isReadingLine())
		// {
		// VTConsole.println("");
		// }
		// }
		// catch (Throwable t)
		// {
		//
		// }
	}
	
	public void close()
	{
		if (!isVisible())
		{
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
			return;
		}
		try
		{
			setVisible(false);
		}
		catch (Throwable t)
		{
			
		}
		try
		{
			dispose();
		}
		catch (Throwable t)
		{
			
		}
		
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
		// VTTerminal.interruptReadLine();
	}
	
	/* public static void main(String[] args) throws Exception { Frame frame =
	 * new Frame(); VTConnectionDialog dialog = new VTConnectionDialog(frame,
	 * "ABC", false, null); dialog.initialize();
	 * dialog.setLocationByPlatform(true); dialog.setVisible(true); } */
}