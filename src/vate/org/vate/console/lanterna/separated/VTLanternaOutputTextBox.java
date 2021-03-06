package org.vate.console.lanterna.separated;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.TextBoxModified;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;

public class VTLanternaOutputTextBox extends TextBoxModified
{
  private volatile int maximumLines = 0;
  private int hiddenColumn = 0;
  private int carriageColumn = -1;
  private Terminal terminal;
  private boolean editable;
  // private int hiddenRow;

  public VTLanternaOutputTextBox(TerminalSize terminalSize, String string, Style multiLine, int maximumLines)
  {
    super(terminalSize, string, multiLine);
    this.maximumLines = maximumLines;
  }

  public void setEditable(boolean editable)
  {
    this.editable = editable;
  }

//    protected TextBoxRenderer createDefaultRenderer() {
//        return new DefaultTextBoxRenderer2();
//    }

  public void setTerminal(Terminal terminal)
  {
    this.terminal = terminal;
  }

  public void setCarriageColumn(int column)
  {
    this.carriageColumn = column;
  }

  public void setHiddenColumn(int column)
  {
    this.hiddenColumn = column;
  }

  public int getHiddenColumn()
  {
    return hiddenColumn;
  }

  public TerminalPosition getCursorLocation()
  {
    // return null;
    return getRenderer().getCursorLocation(this);
  }

  public TerminalPosition getTopLeft()
  {
    return getRenderer().getViewTopLeft();
  }

  public void scrollup()
  {
    if (isReadOnly())
    {
      if (getRenderer().getViewTopLeft().getRow() == 0)
      {
        return;
      }
      else
      {
        getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRelativeRow(-1));
      }
      return;
    }
    // String line = lines.get(caretPosition.getRow());
    if (caretPosition.getRow() > 0)
    {
      String caretLine = getLine(caretPosition.getRow());

      int trueColumnPosition = caretPosition.getColumn();
      // int trueColumnPosition = TerminalTextUtils.getColumnIndex(caretLine,
      // caretPosition.getColumn());
      // int trueColumnPosition = caretPosition.getColumn();
      if (caretLine.length() > trueColumnPosition)
      {
        trueColumnPosition = TerminalTextUtils.getColumnIndex(caretLine, caretPosition.getColumn());
      }

      caretPosition = caretPosition.withRelativeRow(-1).withColumn(trueColumnPosition);

    }
  }

  public void scrolldown()
  {
    if (isReadOnly())
    {
      if (getRenderer().getViewTopLeft().getRow() + getSize().getRows() == lines.size())
      {
        return;
      }
      else
      {
        getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRelativeRow(1));
      }
      return;
    }
    // String line = lines.get(caretPosition.getRow());
    if (caretPosition.getRow() < lines.size() - 1)
    {
      String caretLine = getLine(caretPosition.getRow());

      int trueColumnPosition = caretPosition.getColumn();
      // int trueColumnPosition = TerminalTextUtils.getColumnIndex(caretLine,
      // caretPosition.getColumn());
      // int trueColumnPosition = caretPosition.getColumn();
      if (caretLine.length() > trueColumnPosition)
      {
        trueColumnPosition = TerminalTextUtils.getColumnIndex(caretLine, caretPosition.getColumn());
      }

      caretPosition = caretPosition.withRelativeRow(+1).withColumn(trueColumnPosition);
    }
  }

  public void scrollleft()
  {
    if (isReadOnly())
    {
      getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRelativeColumn(-1));
      return;
    }
    if (caretPosition.getColumn() > 0)
    {
      caretPosition = caretPosition.withRelativeColumn(-1);
    }
  }

  public void scrollright()
  {
    if (isReadOnly())
    {
      getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRelativeColumn(+1));
      return;
    }
//		if(caretPosition.getColumn() < lines.get(caretPosition.getRow()).length()) {
//            caretPosition = caretPosition.withRelativeColumn(1);
    if (editable)
    {
      if (caretPosition.getColumn() < longestRow)
      {
        caretPosition = caretPosition.withRelativeColumn(1);
      }
    }
    else
    {
      if (caretPosition.getColumn() < longestRow - 1)
      {
        caretPosition = caretPosition.withRelativeColumn(1);
      }
    }
  }

  public synchronized Result handleKeyStroke(KeyStroke keyStroke)
  {
    // System.out.println("LanternaOutputTextBox." + keyStroke);
    if (readOnly)
    {
      return handleKeyStrokeReadOnly(keyStroke);
      // handleKeyStrokeReadOnly(keyStroke);
    }
    String line = lines.get(caretPosition.getRow());
    switch (keyStroke.getKeyType())
    {
//        	case Tab:
//        		if(maxLineLength == -1 || maxLineLength > line.length() + 1) {
//                    line = line.substring(0, caretPosition.getColumn()) + keyStroke.getCharacter() + line.substring(caretPosition.getColumn());
//                    if(validated(line)) {
//                        lines.set(caretPosition.getRow(), line);
//                        caretPosition = caretPosition.withRelativeColumn(1);
//                    }
//                }
//                return Result.HANDLED;
      case Character:
        if (getMaxLineLength() == -1 || getMaxLineLength() > line.length() + 1)
        {
          line = line.substring(0, caretPosition.getColumn()) + keyStroke.getCharacter() + line.substring(caretPosition.getColumn());
          if (validated(line))
          {
            lines.set(caretPosition.getRow(), line);
            caretPosition = caretPosition.withRelativeColumn(1);
          }
        }
        return Result.HANDLED;
      case Backspace:
        if (caretPosition.getColumn() > 0)
        {
          line = line.substring(0, caretPosition.getColumn() - 1) + line.substring(caretPosition.getColumn());
          if (validated(line))
          {
            lines.set(caretPosition.getRow(), line);
            caretPosition = caretPosition.withRelativeColumn(-1);
          }
        }
        else if (style == Style.MULTI_LINE && caretPosition.getRow() > 0)
        {
          String concatenatedLines = lines.get(caretPosition.getRow() - 1) + line;
          if (validated(concatenatedLines))
          {
            lines.remove(caretPosition.getRow());
            caretPosition = caretPosition.withRelativeRow(-1);
            caretPosition = caretPosition.withColumn(lines.get(caretPosition.getRow()).length());
            lines.set(caretPosition.getRow(), concatenatedLines);
          }
        }
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case Delete:
        if (caretPosition.getColumn() < line.length())
        {
          line = line.substring(0, caretPosition.getColumn()) + line.substring(caretPosition.getColumn() + 1);
          if (validated(line))
          {
            lines.set(caretPosition.getRow(), line);
          }
        }
        else if (style == Style.MULTI_LINE && caretPosition.getRow() < lines.size() - 1)
        {
          String concatenatedLines = line + lines.get(caretPosition.getRow() + 1);
          if (validated(concatenatedLines))
          {
            lines.set(caretPosition.getRow(), concatenatedLines);
            lines.remove(caretPosition.getRow() + 1);
          }
        }
        return Result.HANDLED;
      case ArrowLeft:
        if (caretPosition.getColumn() > 0)
        {
          caretPosition = caretPosition.withRelativeColumn(-1);
        }
        else if (style == Style.MULTI_LINE && caretWarp && caretPosition.getRow() > 0)
        {
          caretPosition = caretPosition.withRelativeRow(-1);
          caretPosition = caretPosition.withColumn(lines.get(caretPosition.getRow()).length());
        }
        else if (horizontalFocusSwitching)
        {
          return Result.MOVE_FOCUS_LEFT;
        }
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case ArrowRight:
        // if(caretPosition.getColumn() < lines.get(caretPosition.getRow()).length()) {
        // caretPosition = caretPosition.withRelativeColumn(1);
        // }
        if (editable)
        {
          if (caretPosition.getColumn() < longestRow)
          {
            caretPosition = caretPosition.withRelativeColumn(1);
          }
          else if (style == Style.MULTI_LINE && caretWarp && caretPosition.getRow() < lines.size() - 1)
          {
            caretPosition = caretPosition.withRelativeRow(1);
            caretPosition = caretPosition.withColumn(0);
          }
          else if (horizontalFocusSwitching)
          {
            return Result.MOVE_FOCUS_RIGHT;
          }
        }
        else
        {
          if (caretPosition.getColumn() < longestRow - 1)
          {
            caretPosition = caretPosition.withRelativeColumn(1);
          }
          else if (style == Style.MULTI_LINE && caretWarp && caretPosition.getRow() < lines.size() - 1)
          {
            caretPosition = caretPosition.withRelativeRow(1);
            caretPosition = caretPosition.withColumn(0);
          }
          else if (horizontalFocusSwitching)
          {
            return Result.MOVE_FOCUS_RIGHT;
          }
        }

        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case ArrowUp:
        if (caretPosition.getRow() > 0)
        {
          String caretLine = getLine(caretPosition.getRow());

          int trueColumnPosition = caretPosition.getColumn();
          // int trueColumnPosition = TerminalTextUtils.getColumnIndex(caretLine,
          // caretPosition.getColumn());
          // int trueColumnPosition = caretPosition.getColumn();
          if (caretLine.length() > trueColumnPosition)
          {
            trueColumnPosition = TerminalTextUtils.getColumnIndex(caretLine, caretPosition.getColumn());
          }

          caretPosition = caretPosition.withRelativeRow(-1).withColumn(trueColumnPosition);
        }
        else if (verticalFocusSwitching)
        {
          return Result.MOVE_FOCUS_UP;
        }
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case ArrowDown:
        if (caretPosition.getRow() < lines.size() - 1)
        {

          String caretLine = getLine(caretPosition.getRow());

          int trueColumnPosition = caretPosition.getColumn();
          // int trueColumnPosition = TerminalTextUtils.getColumnIndex(caretLine,
          // caretPosition.getColumn());
          // int trueColumnPosition = caretPosition.getColumn();
          if (caretLine.length() > trueColumnPosition)
          {
            trueColumnPosition = TerminalTextUtils.getColumnIndex(caretLine, caretPosition.getColumn());
          }

          caretPosition = caretPosition.withRelativeRow(+1).withColumn(trueColumnPosition);
        }
        else if (verticalFocusSwitching)
        {
          return Result.MOVE_FOCUS_DOWN;
        }
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case End:
        if (editable)
        {
          caretPosition = caretPosition.withColumn(longestRow);
        }
        else
        {
          caretPosition = caretPosition.withColumn(longestRow - 1);
        }
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case Enter:
        if (style == Style.SINGLE_LINE)
        {
          return Result.MOVE_FOCUS_NEXT;
        }
        String newLine = line.substring(caretPosition.getColumn());
        String oldLine = line.substring(0, caretPosition.getColumn());
        if (validated(newLine) && validated(oldLine))
        {
          lines.set(caretPosition.getRow(), oldLine);
          lines.add(caretPosition.getRow() + 1, newLine);
          caretPosition = caretPosition.withColumn(0).withRelativeRow(1);
        }
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case Home:
        caretPosition = caretPosition.withColumn(0);
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case PageDown:
        caretPosition = caretPosition.withRelativeRow(getSize().getRows());
        if (caretPosition.getRow() > lines.size() - 1)
        {
          caretPosition = caretPosition.withRow(lines.size() - 1);
        }
        // if(lines.get(caretPosition.getRow()).length() < caretPosition.getColumn()) {
        // caretPosition =
        // caretPosition.withColumn(lines.get(caretPosition.getRow()).length());
        // }
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case PageUp:
        caretPosition = caretPosition.withRelativeRow(-getSize().getRows());
        if (caretPosition.getRow() < 0)
        {
          caretPosition = caretPosition.withRow(0);
        }
        // if(lines.get(caretPosition.getRow()).length() < caretPosition.getColumn()) {
        // caretPosition =
        // caretPosition.withColumn(lines.get(caretPosition.getRow()).length());
        // }
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      default:
    }
    return super.handleKeyStroke(keyStroke);
  }

  protected boolean validated(String line)
  {
    return validationPattern == null || line.length() == 0 || validationPattern.matcher(line).matches();
  }

  /**
   * Moves the text caret position to a new position in the
   * {@link TextBoxModified}. For single-line {@link TextBoxModified}:es, the line
   * component is not used. If one of the positions are out of bounds, it is
   * automatically set back into range.
   * 
   * @param line   Which line inside the {@link TextBoxModified} to move the caret
   *               to (0 being the first line), ignored if the
   *               {@link TextBoxModified} is single-line
   * @param column What column on the specified line to move the text caret to (0
   *               being the first column)
   * @return Itself
   */
  public synchronized TextBoxModified setCaretPosition(int line, int column)
  {
    if (line < 0)
    {
      line = 0;
    }
    else if (line >= lines.size())
    {
      line = lines.size() - 1;
    }
    if (column < 0)
    {
      column = 0;
    }
//        else if(column > lines.get(line).length()) {
//            column = lines.get(line).length();
//        }
    else
    {
      if (editable)
      {
        if (column > longestRow)
        {
          column = longestRow;
        }
      }
      else
      {
        if (column > longestRow - 1)
        {
          column = longestRow - 1;
        }
      }
    }
    caretPosition = caretPosition.withRow(line).withColumn(column);
    return this;
  }

  protected Result handleKeyStrokeReadOnly(KeyStroke keyStroke)
  {
    switch (keyStroke.getKeyType())
    {
      case ArrowLeft:
        if (getRenderer().getViewTopLeft().getColumn() == 0 && horizontalFocusSwitching)
        {
          return Result.MOVE_FOCUS_LEFT;
        }
        getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRelativeColumn(-1));
        return Result.HANDLED;
      case ArrowRight:
        if (getRenderer().getViewTopLeft().getColumn() + getSize().getColumns() == longestRow && horizontalFocusSwitching)
        {
          return Result.MOVE_FOCUS_RIGHT;
        }
        getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRelativeColumn(1));
        return Result.HANDLED;
      case ArrowUp:
        if (getRenderer().getViewTopLeft().getRow() == 0 && verticalFocusSwitching)
        {
          return Result.MOVE_FOCUS_UP;
        }
        getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRelativeRow(-1));
        return Result.HANDLED;
      case ArrowDown:
        if (getRenderer().getViewTopLeft().getRow() + getSize().getRows() == lines.size() && verticalFocusSwitching)
        {
          return Result.MOVE_FOCUS_DOWN;
        }
        getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRelativeRow(1));
        return Result.HANDLED;
      case Home:
        getRenderer().setViewTopLeft(TerminalPosition.TOP_LEFT_CORNER);
        return Result.HANDLED;
      case End:
        getRenderer().setViewTopLeft(TerminalPosition.TOP_LEFT_CORNER.withRow(getLineCount() - getSize().getRows()));
        return Result.HANDLED;
      case PageDown:
        getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRelativeRow(getSize().getRows()));
        return Result.HANDLED;
      case PageUp:
        getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRelativeRow(-getSize().getRows()));
        return Result.HANDLED;
      default:
    }
    return super.handleKeyStroke(keyStroke);
  }

  public void setViewportToLastLine()
  {
    getRenderer().setViewTopLeft(TerminalPosition.TOP_LEFT_CORNER.withRow(getLineCount() - getSize().getRows()));
  }

  // support \n line feed
  // support \r carriage return
  // support \b backspace
  // support \d delete \u007f
  // support \f clear
  // support bell \u0007

  private String outputToLastLineDirect(String data)
  {
    String lastLine = getLastLine();
    StringBuilder builder = new StringBuilder(lastLine);
    int maxWidth = 80;

    try
    {
      maxWidth = Math.max(terminal.getTerminalSize().getColumns(), 80);
    }
    catch (Throwable e)
    {

    }
    
    int capacity = maxWidth;
    int used = lastLine.length();
    int remaining = capacity - used + (carriageColumn >= 0 ? used - carriageColumn : 0);
    builder.setLength(0);
    builder.append(lastLine);

    if (data.length() <= remaining)
    {
      if (carriageColumn >= 0)
      {
        lastLine = builder.replace(carriageColumn, carriageColumn + data.length(), data).toString();
        carriageColumn += data.length();
        if (carriageColumn >= lastLine.length())
        {
          carriageColumn = -1;
        }
        setLastLine(lastLine);
        // System.out.println("outputToLastLineDirect:[" + lastLine + "]");
        if (longestRow < lastLine.length())
        {
          longestRow = lastLine.length();
        }
      }
      else
      {
        lastLine = builder.append(data).toString();
        setLastLine(lastLine);
        // System.out.println("outputToLastLineDirect:[" + lastLine + "]");
        if (longestRow < lastLine.length())
        {
          longestRow = lastLine.length();
        }
      }
    }
    else
    {
      if (carriageColumn >= 0)
      {
        String appended = data.substring(0, remaining);
        String remainder = data.substring(remaining);

        lastLine = builder.replace(carriageColumn, carriageColumn + appended.length(), appended).toString();
        carriageColumn += appended.length();
        if (carriageColumn >= capacity)
        {
          carriageColumn = -1;
        }
        setLastLine(lastLine);
        if (longestRow < lastLine.length())
        {
          longestRow = lastLine.length();
        }
        addLine("");
        return remainder;
      }
      else
      {
        String appended = data.substring(0, remaining);
        String remainder = data.substring(remaining);

        lastLine = builder.append(appended).toString();
        setLastLine(lastLine);
        if (longestRow < lastLine.length())
        {
          longestRow = lastLine.length();
        }
        addLine("");
        return remainder;
      }
    }
    return null;
  }

  private String outputSingleControl(String data)
  {
    char output = '\0';
    int current = data.indexOf('\r');
    if (current >= 0)
    {
      output = '\r';
    }
    else
    {
      current = Integer.MAX_VALUE;
    }
    int next = data.indexOf('\b');
    if (next >= 0 && next < current)
    {
      output = '\b';
      current = next;
    }
    next = data.indexOf('\n');
    if (next >= 0 && next < current)
    {
      output = '\n';
      current = next;
    }
    next = data.indexOf('\f');
    if (next >= 0 && next < current)
    {
      output = '\f';
      current = next;
    }
    next = data.indexOf('\u007f');
    if (next >= 0 && next < current)
    {
      output = '\u007f';
      current = next;
    }
    next = data.indexOf('\u0007');
    if (next >= 0 && next < current)
    {
      output = '\u0007';
      current = next;
    }

    if (output != '\0')
    {
      String remainder = data.substring(0, current);
      while (remainder != null)
      {
        remainder = outputToLastLineDirect(remainder);
      }
      data = data.substring(current);
      data = data.replaceFirst("\\p{Cntrl}", "");
      output(output);
    }
    else
    {
      data = null;
    }
    return data;
  }

  private String outputMultiControl(String data)
  {
    String result = outputSingleControl(data);
    if (result == null)
    {
      return data;
    }
    String last = null;
    while (result != null)
    {
      last = result;
      result = outputSingleControl(result);
    }
    return last;
  }

  private String outputToLastLine(String data)
  {
    // System.out.println("outputToLastLine:[" + data + "]");
    String lastLine = getLastLine();
    StringBuilder builder = new StringBuilder();

    int maxWidth = 80;

    try
    {
      maxWidth = Math.max(terminal.getTerminalSize().getColumns(), 80);
    }
    catch (Throwable e)
    {

    }
    
    int capacity = maxWidth;
    int used = lastLine.length();
    int remaining = capacity - used + (carriageColumn >= 0 ? used - carriageColumn : 0);

    if (data.length() <= remaining)
    {
      data = outputMultiControl(data);

      if (data.length() <= 0)
      {
        return null;
      }

      lastLine = getLastLine();
      used = lastLine.length();
      remaining = capacity - used + (carriageColumn >= 0 ? used - carriageColumn : 0);
      builder.setLength(0);
      builder.append(lastLine);

      if (data.length() <= remaining)
      {
        if (carriageColumn >= 0)
        {
          lastLine = builder.replace(carriageColumn, carriageColumn + data.length(), data).toString();
          carriageColumn += data.length();
          if (carriageColumn >= capacity)
          {
            carriageColumn = -1;
          }
          setLastLine(lastLine);
          if (longestRow < lastLine.length())
          {
            longestRow = lastLine.length();
          }
        }
        else
        {
          lastLine = builder.append(data).toString();
          setLastLine(lastLine);
          if (longestRow < lastLine.length())
          {
            longestRow = lastLine.length();
          }
        }
      }
      else
      {
        if (carriageColumn >= 0)
        {
          String appended = data.substring(0, remaining);
          String remainder = data.substring(remaining);

          lastLine = builder.replace(carriageColumn, carriageColumn + appended.length(), appended).toString();
          carriageColumn += appended.length();
          if (carriageColumn >= capacity)
          {
            carriageColumn = -1;
          }
          setLastLine(lastLine);
          if (longestRow < lastLine.length())
          {
            longestRow = lastLine.length();
          }
          addLine("");
          return remainder;
        }
        else
        {
          String appended = data.substring(0, remaining);
          String remainder = data.substring(remaining);

          lastLine = builder.append(appended).toString();
          setLastLine(lastLine);
          if (longestRow < lastLine.length())
          {
            longestRow = lastLine.length();
          }
          addLine("");
          return remainder;
        }
      }
    }
    else
    {
      data = outputMultiControl(data);

      if (data.length() <= 0)
      {
        return null;
      }

      lastLine = getLastLine();
      used = lastLine.length();
      remaining = capacity - used + (carriageColumn >= 0 ? used - carriageColumn : 0);
      builder.setLength(0);
      builder.append(lastLine);

      if (data.length() <= remaining)
      {
        if (carriageColumn >= 0)
        {
          lastLine = builder.replace(carriageColumn, carriageColumn + data.length(), data).toString();
          carriageColumn += data.length();
          if (carriageColumn >= capacity)
          {
            carriageColumn = -1;
          }
          setLastLine(lastLine);
          if (longestRow < lastLine.length())
          {
            longestRow = lastLine.length();
          }
        }
        else
        {
          lastLine = builder.append(data).toString();
          setLastLine(lastLine);
          if (longestRow < lastLine.length())
          {
            longestRow = lastLine.length();
          }
        }
      }
      else
      {
        if (carriageColumn >= 0)
        {
          String appended = data.substring(0, remaining);
          String remainder = data.substring(remaining);

          lastLine = builder.replace(carriageColumn, carriageColumn + appended.length(), appended).toString();
          carriageColumn += appended.length();
          if (carriageColumn >= capacity)
          {
            carriageColumn = -1;
          }
          setLastLine(lastLine);
          if (longestRow < lastLine.length())
          {
            longestRow = lastLine.length();
          }
          addLine("");
          return remainder;
        }
        else
        {
          String appended = data.substring(0, remaining);
          String remainder = data.substring(remaining);

          lastLine = builder.append(appended).toString();
          setLastLine(lastLine);
          if (longestRow < lastLine.length())
          {
            longestRow = lastLine.length();
          }
          addLine("");
          return remainder;
        }
      }
    }
    return null;
  }

  // break string in multiple lines if needed
  private void outputMultiline(String data)
  {
    // System.out.println("outputMultiline:[" + data + "]");
    if (data.length() == 0)
    {
      return;
    }
    String remainder = data;
    while ((remainder = outputToLastLine(remainder)) != null)
    {

    }
  }

  public synchronized void output(char data)
  {
    if (data != -'\n')
    {
      if (data == '\t')
      {
        data = ' ';
        outputToLastLine(String.valueOf(data));
        return;
      }
      if (data == '\r')
      {
        carriageColumn = 0;
        return;
      }
      if (data == '\b')
      {
        String lastLine = getLastLine();
        StringBuilder builder = new StringBuilder(lastLine);
        if (lastLine.length() > 0)
        {
          if (carriageColumn > 0)
          {
            setLastLine(builder.deleteCharAt(carriageColumn - 1).toString());
            carriageColumn--;
          }
          else
          {
            setLastLine(builder.deleteCharAt(lastLine.length()).toString());
          }
        }
        return;
      }
      if (data == '\f')
      {
        setText("");
        return;
      }
      if (data == '\u007f')
      {
        String lastLine = getLastLine();
        StringBuilder builder = new StringBuilder(lastLine);
        if (lastLine.length() > 0)
        {
          if (carriageColumn >= 0)
          {
            setLastLine(builder.deleteCharAt(carriageColumn).toString());
            if (carriageColumn >= lastLine.length())
            {
              carriageColumn = -1;
            }
          }
          else
          {
            // setLastLine(builder.deleteCharAt(lastLine.length()).toString());
          }
        }
        return;
      }
      if (data == '\u0007')
      {
        try
        {
          terminal.bell();
        }
        catch (Throwable t)
        {

        }
        return;
      }
      if ((data > 0 && data < 32) || data == 127)
      {
        // reject
        return;
      }
      outputToLastLine(String.valueOf(data));
    }
    else
    {
      addLine("");
    }
    if (getLineCount() > 0)
    {
      setCaretPosition(getLineCount(), 0);
      setViewportToLastLine();
    }
    invalidate();
  }

  public synchronized void output(char[] buff, int off, int len)
  {
    String data = new String(buff, off, len);

    data = data.replace('\t', ' ');

    String[] newlines = data.split("\\n", -1);

    if (newlines.length > 0)
    {
      outputMultiline(newlines[0]);
      if (newlines.length > 1)
      {
        for (int i = 1; i < newlines.length; i++)
        {
          addLine("");
          outputMultiline(newlines[i]);
        }
      }
      if (getLineCount() > 0)
      {
        setCaretPosition(getLineCount(), 0);
        setViewportToLastLine();
      }
      invalidate();
    }
  }

  public synchronized void output(String data)
  {
    // System.out.println("output:[" + data + "]");
    data = data.replace('\t', ' ');

    String[] newlines = data.split("\\n", -1);

    // System.out.println("newlines.length:[" + newlines.length + "]");
    if (newlines.length > 0)
    {
      outputMultiline(newlines[0]);
      if (newlines.length > 1)
      {
        for (int i = 1; i < newlines.length; i++)
        {
          addLine("");
          outputMultiline(newlines[i]);
        }
      }
      if (getLineCount() > 0)
      {
        setCaretPosition(getLineCount(), 0);
        setViewportToLastLine();
      }
      invalidate();
    }
  }

  private void removeLastLine()
  {
    if (getLineCount() >= 1)
    {
      String removed = lines.remove(0);
      TerminalPosition selectionStart = getSelectionStartPosition();
      TerminalPosition selectionEnd = getSelectionEndPosition();
      if (selectionStart != null)
      {
        if (selectionStart.getRow() > 0)
        {
          setSelectionStartPosition(selectionStart.withRelativeRow(-1));
        }
        else
        {
          setSelectionStartPosition(null);
          setSelectionEndPosition(null);
          selectionStart = null;
          selectionEnd = null;
        }
      }
      if (selectionEnd != null)
      {
        if (selectionEnd.getRow() > 0)
        {
          setSelectionEndPosition(selectionEnd.withRelativeRow(-1));
        }
        else
        {
          setSelectionStartPosition(null);
          setSelectionEndPosition(null);
          selectionStart = null;
          selectionEnd = null;
        }
      }
      if (removed.length() >= longestRow)
      {
        longestRow = 80;
//				try
//				{
//					longestRow = terminal.getTerminalSize().getColumns();
//				}
//				catch (Throwable e)
//				{
//					
//				}
        for (String line : lines)
        {
          if (longestRow < line.length())
          {
            longestRow = line.length();
          }
        }
      }
    }
  }

  public synchronized String getLastLine()
  {
    if (getLineCount() > 0)
    {
      return lines.get(lines.size() - 1);
    }
    return null;
  }

  public synchronized void setLastLine(String line)
  {
    lines.set(lines.size() - 1, line);
  }

  public synchronized TextBoxModified addLine(String data)
  {
    // System.out.println("addLine:[" + data + "]");
    // System.out.println("getLastLine:[" + getLastLine() + "]");
    if (getLineCount() > 0 && maximumLines > 0 && getLineCount() == maximumLines)
    {
      removeLastLine();
    }
    carriageColumn = -1;
    if (lines.size() == 1 && lines.get(0).length() == 0)
    {
      output(data);
      return this;
    }
    data = data.replace('\t', ' ');
    TextBoxModified result = putLine(data);
    if (getLineCount() > 1)
    {
      setCaretPosition(getLineCount(), 0);
      setViewportToLastLine();
    }
    return result;
  }

  public synchronized TextBoxModified putLine(String line)
  {
    StringBuilder bob = new StringBuilder();
    for (int i = 0; i < line.length(); i++)
    {
      char c = line.charAt(i);
      if (c == '\n' && style == Style.MULTI_LINE)
      {
        String string = bob.toString();
        int lineWidth = TerminalTextUtils.getColumnWidth(string);
        lines.add(string);
        if (longestRow < lineWidth)
        {
          longestRow = lineWidth;
        }
        addLine(line.substring(i + 1));
        return this;
      }
      else if (Character.isISOControl(c))
      {
        continue;
      }

      bob.append(c);
    }
    String string = bob.toString();
    if (!validated(string))
    {
      throw new IllegalStateException("TextBox validation pattern " + validationPattern + " does not match the supplied text");
    }
    int lineWidth = TerminalTextUtils.getColumnWidth(string);
    lines.add(string);
    if (longestRow < lineWidth)
    {
      longestRow = lineWidth;
    }
    invalidate();
    return this;
  }

  public int replaceSelectedText(StringBuilder inputBuffer, char data)
  {
    int start = this.getMinColumn();
    int end = this.getMaxColumn() + 1;
    // int replaced = end - start;
    inputBuffer.replace(start, end, String.valueOf(data));
    this.setSelectionStartPosition(null);
    this.setSelectionEndPosition(null);
    return start;
  }

  public int replaceSelectedText(StringBuilder inputBuffer, String data)
  {
    int start = this.getMinColumn();
    int end = this.getMaxColumn() + 1;
    // int replaced = end - start;
    inputBuffer.replace(start, end, data);
    this.setSelectionStartPosition(null);
    this.setSelectionEndPosition(null);
    return start + (data.length() > 0 ? data.length() - 1 : 0);
  }

  public StringBuilder handleDataInput(StringBuilder inputBuffer, char data, boolean replace)
  {
    // String line = lines.get(caretPosition.getRow());
    if (selectingText())
    {
      hiddenColumn = replaceSelectedText(inputBuffer, data);
      hiddenColumn = Math.min(hiddenColumn, inputBuffer.length());
      this.setCaretPosition(hiddenColumn);
      return inputBuffer;
    }
    hiddenColumn = Math.min(hiddenColumn, inputBuffer.length());
    int column = hiddenColumn;
    if (replace && column < inputBuffer.length())
    {
      inputBuffer.setCharAt(column, data);
    }
    else
    {
      inputBuffer.insert(column, data);
    }
    hiddenColumn++;
    return inputBuffer;
  }

  public StringBuilder handleDataInput(StringBuilder inputBuffer, String data, boolean replace)
  {
    /// int size = inputBuffer.length();
    if (selectingText())
    {
      hiddenColumn = replaceSelectedText(inputBuffer, data);
      hiddenColumn = Math.min(hiddenColumn, inputBuffer.length());
      this.setCaretPosition(hiddenColumn);
      return inputBuffer;
    }
    int column = hiddenColumn;
    if (replace && column < inputBuffer.length())
    {
      inputBuffer.replace(column, column + data.length(), data);
    }
    else
    {
      inputBuffer.insert(column, data);
    }
    hiddenColumn += data.length();
    hiddenColumn = Math.min(hiddenColumn, inputBuffer.length());
    return inputBuffer;
  }

  public StringBuilder handleDataInput(StringBuilder inputBuffer, KeyStroke keyStroke)
  {
    int column = hiddenColumn;
    KeyType keyType = keyStroke.getKeyType();
    if (keyType == KeyType.Backspace)
    {
      if (selectingText())
      {
        hiddenColumn = replaceSelectedText(inputBuffer, "");
        hiddenColumn = Math.min(hiddenColumn, inputBuffer.length());
        this.setCaretPosition(hiddenColumn);
        return inputBuffer;
      }
      else
      {
        if (inputBuffer.length() > 0 && column > 0)
        {
          inputBuffer.deleteCharAt(column - 1);
          hiddenColumn--;
        }
      }
    }
    if (keyType == KeyType.Delete)
    {
      if (selectingText())
      {
        hiddenColumn = replaceSelectedText(inputBuffer, "");
        hiddenColumn = Math.min(hiddenColumn, inputBuffer.length());
        this.setCaretPosition(hiddenColumn);
        return inputBuffer;
      }
      else
      {
        if (inputBuffer.length() > 0 && column < inputBuffer.length())
        {
          inputBuffer.deleteCharAt(column);
        }
      }
    }
    if (keyType == KeyType.ArrowLeft)
    {
      if (column > 0)
      {
        hiddenColumn--;
      }
    }
    if (keyType == KeyType.ArrowRight)
    {
      if (column < inputBuffer.length())
      {
        hiddenColumn++;
      }
    }
    if (keyType == KeyType.Home)
    {
      hiddenColumn = 0;
    }
    if (keyType == KeyType.End)
    {
      hiddenColumn = inputBuffer.length();
    }
    updateSelection(keyStroke, hiddenColumn, caretPosition.getRow());
    return inputBuffer;
  }
  /**
   * This is the default text box renderer that is used if you don't override
   * anything. With this renderer, the text box is filled with a solid background
   * color and the text is drawn on top of it. Scrollbars are added for multi-line
   * text whenever the text inside the {@code TextBox} does not fit in the
   * available area.
   */
}
