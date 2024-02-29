package org.vash.vate.graphics.control;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import org.vash.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vash.vate.reflection.VTReflectionUtils;

import static java.awt.event.KeyEvent.*;

public final class VTAWTControlProvider
{
  private boolean inputControlInitialized;
  // private GraphicsDevice graphicsDevice;
  private GraphicsDevice graphicsDevice;
  private Robot interfaceInputRobot;
  private Clipboard systemClipboard;
  private Toolkit toolkit;
  
  public VTAWTControlProvider()
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return;
    }
    this.toolkit = Toolkit.getDefaultToolkit();
    try
    {
      this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      this.systemClipboard = toolkit.getSystemClipboard();
    }
    catch (Throwable e)
    {
      // e.printStackTrace(VTTerminal.getSystemOut());
    }
  }
  
  public final void resetGraphicsDevice()
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return;
    }
    try
    {
      setGraphicsDevice(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
    }
    catch (Throwable e)
    {
      // e.printStackTrace(VTTerminal.getSystemOut());
    }
  }
  
  public final void setGraphicsDevice(GraphicsDevice graphicsDevice)
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return;
    }
    if (this.graphicsDevice != null)
    {
      if (graphicsDevice != null)
      {
        if (!this.graphicsDevice.getIDstring().equals(graphicsDevice.getIDstring()))
        {
          this.graphicsDevice = graphicsDevice;
          initializeInputControl();
        }
      }
      else
      {
        this.graphicsDevice = graphicsDevice;
        initializeInputControl();
      }
    }
    else
    {
      if (this.graphicsDevice != graphicsDevice)
      {
        this.graphicsDevice = graphicsDevice;
        initializeInputControl();
      }
    }
    
  }
  
  public final GraphicsDevice getGraphicsDevice()
  {
    return this.graphicsDevice;
  }
  
  public final boolean initializeInputControl()
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    return initializeInputControl(graphicsDevice);
    /*
     * dispose(); try { if (interfaceInputRobot == null) { interfaceInputRobot =
     * new Robot(); interfaceInputRobot.setAutoDelay(0);
     * interfaceInputRobot.setAutoWaitForIdle(false); } inputControlInitialized
     * = true; return true; } catch (Throwable e) { inputControlInitialized =
     * false; return false; }
     */
  }
  
  private final boolean initializeInputControl(GraphicsDevice device)
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    try
    {
      if (interfaceInputRobot == null)
      {
        interfaceInputRobot = new Robot();
        // interfaceInputRobot = new Robot();
        // interfaceInputRobot = new Robot(device);
        interfaceInputRobot.setAutoDelay(0);
        interfaceInputRobot.setAutoWaitForIdle(false);
      }
      inputControlInitialized = true;
      return true;
    }
    catch (Throwable e)
    {
      // e.printStackTrace(VTTerminal.getSystemOut());
      inputControlInitialized = false;
      this.toolkit = Toolkit.getDefaultToolkit();
      try
      {
        this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        this.systemClipboard = toolkit.getSystemClipboard();
      }
      catch (Throwable ex)
      {
        // e.printStackTrace(VTTerminal.getSystemOut());
      }
      return false;
    }
  }
  
  public final boolean isInputControlInitialized()
  {
    return inputControlInitialized;
  }
  
  public final void reset()
  {
    disposeInputControlResources();
    interfaceInputRobot = null;
  }
  
  public final void dispose()
  {
    disposeInputControlResources();
    interfaceInputRobot = null;
    if (VTReflectionUtils.isAWTHeadless())
    {
      return;
    }
    try
    {
      this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }
    catch (Throwable e)
    {
      // e.printStackTrace(VTTerminal.getSystemOut());
    }
    // toolkit = null;
  }
  
  private final void disposeInputControlResources()
  {
    inputControlInitialized = false;
  }
  
  public final void setAutoWaitForIdle(boolean autoWaitForIdle)
  {
    if (interfaceInputRobot != null)
    {
      interfaceInputRobot.setAutoWaitForIdle(autoWaitForIdle);
    }
  }
  
  public final void waitForIdle()
  {
    if (interfaceInputRobot != null)
    {
      interfaceInputRobot.waitForIdle();
    }
  }
  
  public final void keyPress(int keycode, int keymodifiers, int keylocation, char keychar)
  {
    try
    {
      // windows hack for slash keycode
      if ((keychar == '/') && VTReflectionUtils.detectWindows())
      {
        winAltNumpadASCIIKeyType('/');
        return;
      }
      if ((keychar == '?') && VTReflectionUtils.detectWindows())
      {
        winAltNumpadASCIIKeyType('?');
        return;
      }
      if (keycode == VK_UNDEFINED)
      {
        return;
      }
      interfaceInputRobot.keyPress(keycode);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public final void keyRelease(int keycode, int keymodifiers, int keylocation, char keychar)
  {
    try
    {
      if (keycode == VK_UNDEFINED)
      {
        return;
      }
      interfaceInputRobot.keyRelease(keycode);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public final void mouseMove(int x, int y)
  {
    try
    {
      // Has problems in multi-screen environments with MacOSX
      Rectangle bounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
      interfaceInputRobot.mouseMove(x + bounds.x, y + bounds.y);
      // interfaceInputRobot.mouseMove(x, y);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public final void mousePress(int buttons)
  {
    try
    {
      interfaceInputRobot.mousePress(buttons);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public final void mouseRelease(int buttons)
  {
    try
    {
      interfaceInputRobot.mouseRelease(buttons);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public final void mouseWheel(int wheelAmt)
  {
    try
    {
      interfaceInputRobot.mouseWheel(wheelAmt);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public final boolean getLockingKeyState(int keyCode)
  {
    try
    {
      return toolkit.getLockingKeyState(keyCode);
    }
    catch (Throwable t)
    {
      return false;
    }
  }
  
  public final void setLockingKeyState(int keyCode, boolean on)
  {
    try
    {
      toolkit.setLockingKeyState(keyCode, on);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public final Clipboard getSystemClipboard()
  {
    return systemClipboard;
  }
  
  // windows only thing
  public void winAltNumpadASCIIKeyType(char characterKey)
  {
    switch (characterKey)
    {
      case '\u263A':
        altNumpadASCII("1");
        break;
      case '\u263B':
        altNumpadASCII("2");
        break;
      case '\u2665':
        altNumpadASCII("3");
        break;
      case '\u2666':
        altNumpadASCII("4");
        break;
      case '\u2663':
        altNumpadASCII("5");
        break;
      case '\u2660':
        altNumpadASCII("6");
        break;
      case '\u2642':
        altNumpadASCII("11");
        break;
      case '\u2640':
        altNumpadASCII("12");
        break;
      case '\u266B':
        altNumpadASCII("14");
        break;
      case '\u263C':
        altNumpadASCII("15");
        break;
      case '\u25BA':
        altNumpadASCII("16");
        break;
      case '\u25C4':
        altNumpadASCII("17");
        break;
      case '\u2195':
        altNumpadASCII("18");
        break;
      case '\u203C':
        altNumpadASCII("19");
        break;
      case '\u00B6':
        altNumpadASCII("20");
        break;
      case '\u00A7':
        altNumpadASCII("21");
        break;
      case '\u25AC':
        altNumpadASCII("22");
        break;
      case '\u21A8':
        altNumpadASCII("23");
        break;
      case '\u2191':
        altNumpadASCII("24");
        break;
      case '\u2193':
        altNumpadASCII("25");
        break;
      case '\u2192':
        altNumpadASCII("26");
        break;
      case '\u2190':
        altNumpadASCII("27");
        break;
      case '\u221F':
        altNumpadASCII("28");
        break;
      case '\u2194':
        altNumpadASCII("29");
        break;
      case '\u25B2':
        altNumpadASCII("30");
        break;
      case '\u25BC':
        altNumpadASCII("31");
        break;
      case '!':
        altNumpadASCII("33");
        break;
      case '"':
        altNumpadASCII("34");
        break;
      case '#':
        altNumpadASCII("35");
        break;
      case '$':
        altNumpadASCII("36");
        break;
      case '%':
        altNumpadASCII("37");
        break;
      case '&':
        altNumpadASCII("38");
        break;
      case '\'':
        altNumpadASCII("39");
        break;
      case '(':
        altNumpadASCII("40");
        break;
      case ')':
        altNumpadASCII("41");
        break;
      case '*':
        altNumpadASCII("42");
        break;
      case '+':
        altNumpadASCII("43");
        break;
      case ',':
        altNumpadASCII("44");
        break;
      case '-':
        altNumpadASCII("45");
        break;
      case '.':
        altNumpadASCII("46");
        break;
      case '/':
        altNumpadASCII("47");
        break;
      case '0':
        altNumpadASCII("48");
        break;
      case '1':
        altNumpadASCII("49");
        break;
      case '2':
        altNumpadASCII("50");
        break;
      case '3':
        altNumpadASCII("51");
        break;
      case '4':
        altNumpadASCII("52");
        break;
      case '5':
        altNumpadASCII("53");
        break;
      case '6':
        altNumpadASCII("54");
        break;
      case '7':
        altNumpadASCII("55");
        break;
      case '8':
        altNumpadASCII("56");
        break;
      case '9':
        altNumpadASCII("57");
        break;
      case ':':
        altNumpadASCII("58");
        break;
      case ';':
        altNumpadASCII("59");
        break;
      case '<':
        altNumpadASCII("60");
        break;
      case '=':
        altNumpadASCII("61");
        break;
      case '>':
        altNumpadASCII("62");
        break;
      case '?':
        altNumpadASCII("63");
        break;
      case '@':
        altNumpadASCII("64");
        break;
      case 'A':
        altNumpadASCII("65");
        break;
      case 'B':
        altNumpadASCII("66");
        break;
      case 'C':
        altNumpadASCII("67");
        break;
      case 'D':
        altNumpadASCII("68");
        break;
      case 'E':
        altNumpadASCII("69");
        break;
      case 'F':
        altNumpadASCII("70");
        break;
      case 'G':
        altNumpadASCII("71");
        break;
      case 'H':
        altNumpadASCII("72");
        break;
      case 'I':
        altNumpadASCII("73");
        break;
      case 'J':
        altNumpadASCII("74");
        break;
      case 'K':
        altNumpadASCII("75");
        break;
      case 'L':
        altNumpadASCII("76");
        break;
      case 'M':
        altNumpadASCII("77");
        break;
      case 'N':
        altNumpadASCII("78");
        break;
      case 'O':
        altNumpadASCII("79");
        break;
      case 'P':
        altNumpadASCII("80");
        break;
      case 'Q':
        altNumpadASCII("81");
        break;
      case 'R':
        altNumpadASCII("82");
        break;
      case 'S':
        altNumpadASCII("83");
        break;
      case 'T':
        altNumpadASCII("84");
        break;
      case 'U':
        altNumpadASCII("85");
        break;
      case 'V':
        altNumpadASCII("86");
        break;
      case 'W':
        altNumpadASCII("87");
        break;
      case 'X':
        altNumpadASCII("88");
        break;
      case 'Y':
        altNumpadASCII("89");
        break;
      case 'Z':
        altNumpadASCII("90");
        break;
      case '[':
        altNumpadASCII("91");
        break;
      case '\\':
        altNumpadASCII("92");
        break;
      case ']':
        altNumpadASCII("93");
        break;
      case '^':
        altNumpadASCII("94");
        break;
      case '_':
        altNumpadASCII("95");
        break;
      case '`':
        altNumpadASCII("96");
        break;
      case 'a':
        altNumpadASCII("97");
        break;
      case 'b':
        altNumpadASCII("98");
        break;
      case 'c':
        altNumpadASCII("99");
        break;
      case 'd':
        altNumpadASCII("100");
        break;
      case 'e':
        altNumpadASCII("101");
        break;
      case 'f':
        altNumpadASCII("102");
        break;
      case 'g':
        altNumpadASCII("103");
        break;
      case 'h':
        altNumpadASCII("104");
        break;
      case 'i':
        altNumpadASCII("105");
        break;
      case 'j':
        altNumpadASCII("106");
        break;
      case 'k':
        altNumpadASCII("107");
        break;
      case 'l':
        altNumpadASCII("108");
        break;
      case 'm':
        altNumpadASCII("109");
        break;
      case 'n':
        altNumpadASCII("110");
        break;
      case 'o':
        altNumpadASCII("111");
        break;
      case 'p':
        altNumpadASCII("112");
        break;
      case 'q':
        altNumpadASCII("113");
        break;
      case 'r':
        altNumpadASCII("114");
        break;
      case 's':
        altNumpadASCII("115");
        break;
      case 't':
        altNumpadASCII("116");
        break;
      case 'u':
        altNumpadASCII("117");
        break;
      case 'v':
        altNumpadASCII("118");
        break;
      case 'w':
        altNumpadASCII("119");
        break;
      case 'x':
        altNumpadASCII("120");
        break;
      case 'y':
        altNumpadASCII("121");
        break;
      case 'z':
        altNumpadASCII("122");
        break;
      case '{':
        altNumpadASCII("123");
        break;
      case '|':
        altNumpadASCII("124");
        break;
      case '}':
        altNumpadASCII("125");
        break;
      case '~':
        altNumpadASCII("126");
        break;
      case '\u2302':
        altNumpadASCII("127");
        break;
      case '\u00C7':
        altNumpadASCII("128");
        break;
      case '\u00FC':
        altNumpadASCII("129");
        break;
      case '\u00E9':
        altNumpadASCII("130");
        break;
      case '\u00E2':
        altNumpadASCII("131");
        break;
      case '\u00E4':
        altNumpadASCII("132");
        break;
      case '\u00E0':
        altNumpadASCII("133");
        break;
      case '\u00E5':
        altNumpadASCII("134");
        break;
      case '\u00E7':
        altNumpadASCII("135");
        break;
      case '\u00EA':
        altNumpadASCII("136");
        break;
      case '\u00EB':
        altNumpadASCII("137");
        break;
      case '\u00E8':
        altNumpadASCII("138");
        break;
      case '\u00EF':
        altNumpadASCII("139");
        break;
      case '\u00EE':
        altNumpadASCII("140");
        break;
      case '\u00EC':
        altNumpadASCII("141");
        break;
      case '\u00C4':
        altNumpadASCII("142");
        break;
      case '\u00C5':
        altNumpadASCII("143");
        break;
      case '\u00C9':
        altNumpadASCII("144");
        break;
      case '\u00E6':
        altNumpadASCII("145");
        break;
      case '\u00C6':
        altNumpadASCII("146");
        break;
      case '\u00F4':
        altNumpadASCII("147");
        break;
      case '\u00F6':
        altNumpadASCII("148");
        break;
      case '\u00F2':
        altNumpadASCII("149");
        break;
      case '\u00FB':
        altNumpadASCII("150");
        break;
      case '\u00F9':
        altNumpadASCII("151");
        break;
      case '\u00FF':
        altNumpadASCII("152");
        break;
      case '\u00D6':
        altNumpadASCII("153");
        break;
      case '\u00DC':
        altNumpadASCII("154");
        break;
      case '\u00A2':
        altNumpadASCII("155");
        break;
      case '\u00A3':
        altNumpadASCII("156");
        break;
      case '\u00A5':
        altNumpadASCII("157");
        break;
      case '\u20A7':
        altNumpadASCII("158");
        break;
      case '\u0192':
        altNumpadASCII("159");
        break;
      case '\u00E1':
        altNumpadASCII("160");
        break;
      case '\u00ED':
        altNumpadASCII("161");
        break;
      case '\u00F3':
        altNumpadASCII("162");
        break;
      case '\u00FA':
        altNumpadASCII("163");
        break;
      case '\u00F1':
        altNumpadASCII("164");
        break;
      case '\u00D1':
        altNumpadASCII("165");
        break;
      case '\u00AA':
        altNumpadASCII("166");
        break;
      case '\u00BA':
        altNumpadASCII("167");
        break;
      case '\u00BF':
        altNumpadASCII("168");
        break;
      case '\u2310':
        altNumpadASCII("169");
        break;
      case '\u00AC':
        altNumpadASCII("170");
        break;
      case '\u00BD':
        altNumpadASCII("171");
        break;
      case '\u00BC':
        altNumpadASCII("172");
        break;
      case '\u00A1':
        altNumpadASCII("173");
        break;
      case '\u00AB':
        altNumpadASCII("174");
        break;
      case '\u00BB':
        altNumpadASCII("175");
        break;
      case '\u2591':
        altNumpadASCII("176");
        break;
      case '\u2592':
        altNumpadASCII("177");
        break;
      case '\u2593':
        altNumpadASCII("178");
        break;
      case '\u2502':
        altNumpadASCII("179");
        break;
      case '\u2524':
        altNumpadASCII("180");
        break;
      case '\u2561':
        altNumpadASCII("181");
        break;
      case '\u2562':
        altNumpadASCII("182");
        break;
      case '\u2556':
        altNumpadASCII("183");
        break;
      case '\u2555':
        altNumpadASCII("184");
        break;
      case '\u2563':
        altNumpadASCII("185");
        break;
      case '\u2551':
        altNumpadASCII("186");
        break;
      case '\u2557':
        altNumpadASCII("187");
        break;
      case '\u255D':
        altNumpadASCII("188");
        break;
      case '\u255C':
        altNumpadASCII("189");
        break;
      case '\u255B':
        altNumpadASCII("190");
        break;
      case '\u2510':
        altNumpadASCII("191");
        break;
      case '\u2514':
        altNumpadASCII("192");
        break;
      case '\u2534':
        altNumpadASCII("193");
        break;
      case '\u252C':
        altNumpadASCII("194");
        break;
      case '\u251C':
        altNumpadASCII("195");
        break;
      case '\u2500':
        altNumpadASCII("196");
        break;
      case '\u253C':
        altNumpadASCII("197");
        break;
      case '\u255E':
        altNumpadASCII("198");
        break;
      case '\u255F':
        altNumpadASCII("199");
        break;
      case '\u255A':
        altNumpadASCII("200");
        break;
      case '\u2554':
        altNumpadASCII("201");
        break;
      case '\u2569':
        altNumpadASCII("202");
        break;
      case '\u2566':
        altNumpadASCII("203");
        break;
      case '\u2560':
        altNumpadASCII("204");
        break;
      case '\u2550':
        altNumpadASCII("205");
        break;
      case '\u256C':
        altNumpadASCII("206");
        break;
      case '\u2567':
        altNumpadASCII("207");
        break;
      case '\u2568':
        altNumpadASCII("208");
        break;
      case '\u2564':
        altNumpadASCII("209");
        break;
      case '\u2565':
        altNumpadASCII("210");
        break;
      case '\u2559':
        altNumpadASCII("211");
        break;
      case '\u2558':
        altNumpadASCII("212");
        break;
      case '\u2552':
        altNumpadASCII("213");
        break;
      case '\u2553':
        altNumpadASCII("214");
        break;
      case '\u256B':
        altNumpadASCII("215");
        break;
      case '\u256A':
        altNumpadASCII("216");
        break;
      case '\u2518':
        altNumpadASCII("217");
        break;
      case '\u250C':
        altNumpadASCII("218");
        break;
      case '\u2588':
        altNumpadASCII("219");
        break;
      case '\u2584':
        altNumpadASCII("220");
        break;
      case '\u258C':
        altNumpadASCII("221");
        break;
      case '\u2590':
        altNumpadASCII("222");
        break;
      case '\u2580':
        altNumpadASCII("223");
        break;
      case '\u03B1':
        altNumpadASCII("224");
        break;
      case '\u00DF':
        altNumpadASCII("225");
        break;
      case '\u0393':
        altNumpadASCII("226");
        break;
      case '\u03C0':
        altNumpadASCII("227");
        break;
      case '\u03A3':
        altNumpadASCII("228");
        break;
      case '\u03C3':
        altNumpadASCII("229");
        break;
      case '\u00B5':
        altNumpadASCII("230");
        break;
      case '\u03C4':
        altNumpadASCII("231");
        break;
      case '\u03A6':
        altNumpadASCII("232");
        break;
      case '\u0398':
        altNumpadASCII("233");
        break;
      case '\u03A9':
        altNumpadASCII("234");
        break;
      case '\u03B4':
        altNumpadASCII("235");
        break;
      case '\u221E':
        altNumpadASCII("236");
        break;
      case '\u03C6':
        altNumpadASCII("237");
        break;
      case '\u03B5':
        altNumpadASCII("238");
        break;
      case '\u2229':
        altNumpadASCII("239");
        break;
      case '\u2261':
        altNumpadASCII("240");
        break;
      case '\u00B1':
        altNumpadASCII("241");
        break;
      case '\u2265':
        altNumpadASCII("242");
        break;
      case '\u2264':
        altNumpadASCII("243");
        break;
      case '\u2320':
        altNumpadASCII("244");
        break;
      case '\u2321':
        altNumpadASCII("245");
        break;
      case '\u00F7':
        altNumpadASCII("246");
        break;
      case '\u2248':
        altNumpadASCII("247");
        break;
      case '\u00B0':
        altNumpadASCII("248");
        break;
      case '\u2219':
        altNumpadASCII("249");
        break;
      case '\u00B7':
        altNumpadASCII("250");
        break;
      case '\u221A':
        altNumpadASCII("251");
        break;
      case '\u207F':
        altNumpadASCII("252");
        break;
      case '\u00B2':
        altNumpadASCII("253");
        break;
      case '\u25A0':
        altNumpadASCII("254");
        break;
      default:
        return;
    }
  }
  
  public static String getUnicodeCodePointString(char ch)
  {
    return String.format("%04x", (int) ch);
  }
  
  private void altNumpadASCII(String numpadCodes)
  {
    if (numpadCodes == null || !numpadCodes.matches("^\\d+$"))
    {
      return;
    }
    interfaceInputRobot.keyPress(VK_ALT);
    for (char charater : numpadCodes.toCharArray())
    {
      int NUMPAD_KEY = getNumpadKey(charater);
      if (NUMPAD_KEY != -1)
      {
        interfaceInputRobot.keyPress(NUMPAD_KEY);
        interfaceInputRobot.keyRelease(NUMPAD_KEY);
      }
    }
    interfaceInputRobot.keyRelease(VK_ALT);
  }
  
  private int getNumpadKey(char numberChar)
  {
    switch (numberChar)
    {
      case '0':
        return VK_NUMPAD0;
      case '1':
        return VK_NUMPAD1;
      case '2':
        return VK_NUMPAD2;
      case '3':
        return VK_NUMPAD3;
      case '4':
        return VK_NUMPAD4;
      case '5':
        return VK_NUMPAD5;
      case '6':
        return VK_NUMPAD6;
      case '7':
        return VK_NUMPAD7;
      case '8':
        return VK_NUMPAD8;
      case '9':
        return VK_NUMPAD9;
      default:
        return -1;
    }
  }
  
  // public static void main(String[] args)
  // {//123456780
  // VTAWTControlProvider provider = new VTAWTControlProvider();
  // provider.initializeInputControl();
  // System.out.print(provider.getLockingKeyState(VK_NUM_LOCK));
  // provider.keyPress(VK_DELETE, SHIFT_DOWN_MASK, 0, ' ');
  // provider.keyRelease(VK_DELETE, SHIFT_DOWN_MASK, 0, ' ');
  // //provider.winAltNumpadKeyType('\b');
  // }
}