/*
 * Copyright (c) 2002-2004, Martian Software, Inc.
 * This file is made available under the LGPL as described in the accompanying
 * LICENSE.TXT file.
 */

package com.martiansoftware.jsapvt;

import java.util.List;
/**
 * <p>A utility class to parse a command line contained in a single String into
 * an array of argument tokens, much as the JVM (or more accurately, your
 * operating system) does before calling your programs' <code>public static
 * void main(String[] args)</code>
 * methods.</p>
 *
 * <p>This class has been developed to parse the command line in the same way
 * that MS Windows 2000 does.  Arguments containing spaces should be enclosed
 * in quotes. Quotes that should be in the argument string should be escaped
 * with a preceding backslash ('\') character.  Backslash characters that
 * should be in the argument string should also be escaped with a preceding
 * backslash character.</p>
 *
 * Whenever <code>JSAP.parse(String)</code> is called, the specified String is
 * tokenized by this class, then forwarded to <code>JSAP.parse(String[])</code>
 * for further processing.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 * @see com.martiansoftware.jsap.JSAP#parse(String)
 * @see com.martiansoftware.jsap.JSAP#parse(String[])
 */
public class CommandLineTokenizerMKII {

    /**
     * Hide the constructor.
     */
    private CommandLineTokenizerMKII() {
    }

    /**
     * Goofy internal utility to avoid duplicated code.  If the specified
     * StringBuffer is not empty, its contents are appended to the resulting
     * array (temporarily stored in the specified ArrayList).  The StringBuffer
     * is then emptied in order to begin storing the next argument.
     * @param resultBuffer the List temporarily storing the resulting
     * argument array.
     * @param buf the StringBuffer storing the current argument.
     */
    private static void appendToBuffer(
    List<String> resultBuffer,
    StringBuffer buf)
    {
      if (buf.length() > 0)
      {
        resultBuffer.add(buf.toString());
        buf.setLength(0);
      }
    }
    
    /**
     * Parses the specified command line into an array of individual arguments.
     * Arguments containing spaces should be enclosed in quotes.
     * Quotes that should be in the argument string should be escaped with a
     * preceding backslash ('\') character.  Backslash characters that should
     * be in the argument string should also be escaped with a preceding
     * backslash character.
     * @param commandLine the command line to parse
     * @return an argument array representing the specified command line.
     */
  public static String[] tokenize(String commandLine)
  {
    List<String> resultBuffer = new java.util.ArrayList<String>();
    if (commandLine != null)
    {
      int z = commandLine.length();
      boolean insideQuotes = false;
      StringBuffer buf = new StringBuffer();
      char q = 'q';
      char c = 'c';
      char l = ' ';
      char n = ' ';
      for (int i = 0; i < z; i++)
      {
        c = commandLine.charAt(i);
        n = z > i + 1 ? commandLine.charAt(i + 1) : ' ';
        if (c == '"' || c == '\'')
        {
          if ((q == 'q' && Character.isWhitespace(l)) || (c == q && Character.isWhitespace(n)))
          {
            appendToBuffer(resultBuffer, buf);
            insideQuotes = !insideQuotes;
            if (insideQuotes)
            {
              q = c;
            }
            else
            {
              q = 'q';
            }
          }
          else
          {
            buf.append(c);
          }
        }
        else if (insideQuotes && c == '\\')
        {
          if (n == q)
          {
            buf.append(commandLine.charAt(i + 1));
            i++;
          }
          else
          {
            buf.append("\\");
          }
        }
        else
        {
          if (insideQuotes)
          {
            buf.append(c);
          }
          else
          {
            if (Character.isWhitespace(c))
            {
              appendToBuffer(resultBuffer, buf);
            }
            else
            {
              buf.append(c);
            }
          }
        }
        l = c;
      }
      appendToBuffer(resultBuffer, buf);
    }
    String[] result = new String[resultBuffer.size()];
    return ((String[]) resultBuffer.toArray(result));
  }
  
  public static int findParameterStart(String commandLine, int parameterNumber)
  {
    //List resultBuffer = new java.util.ArrayList();
    int currentParameterCount = -1;
    int currentParameterSize = 0;
    if (commandLine != null)
    {
      int z = commandLine.length();
      boolean insideQuotes = false;
      //StringBuffer buf = new StringBuffer();
      char q = 'q';
      char c = 'c';
      char l = ' ';
      char n = ' ';
      for (int i = 0; i < z; i++)
      {
        c = commandLine.charAt(i);
        n = z > i + 1 ? commandLine.charAt(i + 1) : ' ';
        if (c == '"' || c == '\'')
        {
          if ((q == 'q' && Character.isWhitespace(l)) || (c == q && Character.isWhitespace(n)))
          {
            insideQuotes = !insideQuotes;
            if (insideQuotes)
            {
              q = c;
              //started argument if currentArgumentSize == 0
              if (currentParameterSize == 0)
              {
                currentParameterCount++;
                if (currentParameterCount == parameterNumber)
                {
                  return i;
                }
              }
              currentParameterSize++;
            }
            else
            {
              q = 'q';
              //terminated argument
              currentParameterSize = 0;
            }
          }
          else
          {
            //started argument if currentArgumentSize == 0
            if (currentParameterSize == 0)
            {
              currentParameterCount++;
              if (currentParameterCount == parameterNumber)
              {
                return i;
              }
            }
            currentParameterSize++;
          }
        }
        else if (insideQuotes && c == '\\')
        {
          if (n == q)
          {
            //started argument if currentArgumentSize == 0
            if (currentParameterSize == 0)
            {
              currentParameterCount++;
              if (currentParameterCount == parameterNumber)
              {
                return i;
              }
            }
            currentParameterSize++;
            i++;
          }
          else
          {
            //started argument if currentArgumentSize == 0
            if (currentParameterSize == 0)
            {
              currentParameterCount++;
              if (currentParameterCount == parameterNumber)
              {
                return i;
              }
            }
            currentParameterSize++;
          }
        }
        else
        {
          if (insideQuotes)
          {
            //started argument if currentArgumentSize == 0
            if (currentParameterSize == 0)
            {
              currentParameterCount++;
              if (currentParameterCount == parameterNumber)
              {
                return i;
              }
            }
            currentParameterSize++;
          }
          else
          {
            if (Character.isWhitespace(c))
            {
              //terminated argument
              currentParameterSize = 0;
            }
            else
            {
              //started argument if currentArgumentSize == 0
              if (currentParameterSize == 0)
              {
                currentParameterCount++;
                if (currentParameterCount == parameterNumber)
                {
                  return i;
                }
              }
              currentParameterSize++;
            }
          }
        }
        l = c;
      }
      //terminated argument
      currentParameterSize = 0;
    }
    return -1;
  }
  
  public static String parseCommandParameter(String commandLine, int parameterIndex, boolean removeQuotes)
  {
    String result = "";
    int parameterStart = findParameterStart(commandLine, parameterIndex);
    
    result = commandLine.substring(parameterStart);
    
    if (!removeQuotes)
    {
      return result;
    }
    
    boolean startedWithSingleQuote = (parameterStart > 0) && (commandLine.charAt(parameterStart) == '\'') && (commandLine.charAt(parameterStart - 1) != '\\');
    boolean startedWithDoubleQuote = (parameterStart > 0) && (commandLine.charAt(parameterStart) == '\"') && (commandLine.charAt(parameterStart - 1) != '\\');
    
    int singleQuoteIndex = result.lastIndexOf('\'');
    int doubleQuoteIndex = result.lastIndexOf('\"');
    int lastBackslashIndex = result.lastIndexOf('\\');
    
    if (startedWithSingleQuote && (singleQuoteIndex >= 0))
    {
      if (lastBackslashIndex < 0 || (lastBackslashIndex + 1 != singleQuoteIndex))
      {
        result = result.substring(1, singleQuoteIndex);
      }
    }
    else if (startedWithDoubleQuote && (doubleQuoteIndex >= 0))
    {
      if (lastBackslashIndex < 0 || (lastBackslashIndex + 1 != doubleQuoteIndex))
      {
        result = result.substring(1, doubleQuoteIndex);
      }
    }
    
    return result;
  }
}
