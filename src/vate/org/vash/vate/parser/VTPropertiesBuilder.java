package org.vash.vate.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class VTPropertiesBuilder
{
  public static VTConfigurationProperties loadProperties(InputStream is) throws Exception
  {
    return loadProperties(is, "UTF-8");
  }
  
  public static VTConfigurationProperties loadProperties(InputStream in, String encoding) throws Exception
  {
    if (!"UTF-8".equalsIgnoreCase(encoding) || !"UTF8".equalsIgnoreCase(encoding))
    {
      VTConfigurationProperties properties = new VTConfigurationProperties();
      properties.load(in);
      return properties;
    }
    StringBuilder sb = new StringBuilder();
    InputStreamReader isr = new InputStreamReader(in, encoding);
    char[] buf = new char[1024];
    while (true)
    {
      int temp = isr.read(buf);
      if (temp < 0)
        break;
      sb.append(buf, 0, temp);
    }
    String inputString = escapifyStr(sb.toString());
    byte[] bytes = inputString.getBytes("ISO-8859-1");
    ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
    VTConfigurationProperties properties = new VTConfigurationProperties();
    properties.load(arrayInputStream);
    return properties;
  }
  
  public static void saveProperties(OutputStream out, VTConfigurationProperties properties, String comments) throws Exception
  {
    saveProperties(out, properties, comments, "UTF-8");
  }
  
  public static void saveProperties(OutputStream out, VTConfigurationProperties properties, String comments, String encoding) throws Exception
  {
    if ("UTF-8".equalsIgnoreCase(encoding) || "UTF8".equalsIgnoreCase(encoding))
    {
      ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
      out.write(0xef); // emits 0xef
      out.write(0xbb); // emits 0xbb
      out.write(0xbf); // emits 0xbf
      out.write('\r'); // emits CR
      out.write('\n'); // emits LF
      properties.store(arrayOutputStream, comments);
      String data = unescapifyStr(new String(arrayOutputStream.toByteArray(), "ISO-8859-1"));
      data.replaceAll("((?<!\\r)\\n|\\r(?!\\n))", "\r\n");
      out.write(data.getBytes("UTF-8"));
      out.flush();
    }
    else
    {
      properties.store(out, comments);
      out.flush();
    }
    
  }
  
  private static char hexDigit(char ch, int offset)
  {
    int val = (ch >> offset) & 0xF;
    if (val <= 9)
      return (char) ('0' + val);
    
    return (char) ('A' + val - 10);
  }
  
  private static String escapifyStr(String str)
  {
    StringBuilder result = new StringBuilder();
    int len = str.length();
    for (int x = 0; x < len; x++)
    {
      char ch = str.charAt(x);
      if (ch <= 0x007e)
      {
        result.append(ch);
        continue;
      }
      
      result.append('\\');
      result.append('u');
      result.append(hexDigit(ch, 12));
      result.append(hexDigit(ch, 8));
      result.append(hexDigit(ch, 4));
      result.append(hexDigit(ch, 0));
    }
    return result.toString();
  }
  
  private static String unescapifyStr(String str)
  {
    StringBuilder result = new StringBuilder();
    String hexadecimalString;
    int decimalCodepoint = 0;
    int len = str.length();
    for (int x = 0; x < len; x++)
    {
      char ch = str.charAt(x);
      if (ch == '\\')
      {
        char next = str.charAt(++x);
        switch (next)
        {
          case 'u':
          {
            hexadecimalString = str.substring(++x, x + 4);
            decimalCodepoint = Integer.parseInt(hexadecimalString, 16);
            result.append(Character.toChars(decimalCodepoint));
            break;
          }
          default:
          {
            result.append('\\');
            result.append(next);
            break;
          }
        }
        continue;
      }
      else
      {
        result.append(ch);
      }
    }
    return result.toString();
  }
}
