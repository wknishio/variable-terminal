/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna). lanterna
 * is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>. Copyright (C) 2010-2020 Martin Berglund
 */
package org.vash.vate.console.lanterna.separated;

import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.AbstractInteractableComponent;
import com.googlecode.lanterna.gui2.Direction;
//import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.InteractableRenderer;
import com.googlecode.lanterna.gui2.ScrollBar;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
//import com.googlecode.lanterna.gui2.Interactable.Result;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.awt.Adjustable;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This component keeps a text content that is editable by the user. A TextBox
 * can be single line or multiline and lets the user navigate the cursor in the
 * text area by using the arrow keys, page up, page down, home and end. For
 * multi-line {@code TextBox}:es, scrollbars will be automatically displayed if
 * needed.
 * <p>
 * Size-wise, a {@code TextBox} should be hard-coded to a particular size, it's
 * not good at guessing how large it should be. You can do this through the
 * constructor.
 */
public class VTLanternaTextBoxModified extends AbstractInteractableComponent<VTLanternaTextBoxModified>
{
  
  /**
   * Enum value to force a {@code TextBox} to be either single line or multi
   * line. This is usually auto-detected if the text box has some initial
   * content by scanning that content for \n characters.
   */
  public enum Style
  {
    /**
     * The {@code TextBox} contains a single line of text and is typically drawn
     * on one row
     */
    SINGLE_LINE,
    /**
     * The {@code TextBox} contains a none, one or many lines of text and is
     * normally drawn over multiple lines
     */
    MULTI_LINE,;
  }
  
  public final List<String> lines;
  protected final Style style;
  
  protected TerminalPosition caretPosition;
  protected boolean caretWarp;
  protected boolean readOnly;
  protected boolean horizontalFocusSwitching;
  protected boolean verticalFocusSwitching;
  protected final int maxLineLength;
  public int longestRow;
  protected Character mask;
  public Pattern validationPattern;
  
  // private Terminal terminal;
  
  /**
   * Main constructor of the {@code TextBox} which decides size, initial content
   * and style
   * 
   * @param preferredSize
   *          Size of the {@code TextBox}
   * @param initialContent
   *          Initial content of the {@code TextBox}
   * @param style
   *          Style to use for this {@code TextBox}, instead of auto-detecting
   */
  public VTLanternaTextBoxModified(TerminalSize preferredSize, String initialContent, Style style)
  {
    this.lines = new ArrayList<String>();
    this.style = style;
    this.readOnly = false;
    this.caretWarp = false;
    this.verticalFocusSwitching = true;
    this.horizontalFocusSwitching = (style == Style.SINGLE_LINE);
    this.caretPosition = TerminalPosition.TOP_LEFT_CORNER;
    this.maxLineLength = -1;
    this.longestRow = 80; // To fit the cursor
    this.mask = null;
    this.validationPattern = null;
    setText(initialContent);
    // Re-adjust caret position
    this.caretPosition = TerminalPosition.TOP_LEFT_CORNER.withColumn(getLine(0).length());
    
    if (preferredSize == null)
    {
      preferredSize = new TerminalSize(Math.max(10, longestRow), lines.size());
    }
    setPreferredSize(preferredSize);
  }
  
  public boolean isMovementKeyStroke(KeyStroke keyStroke)
  {
    if (keyStroke.getKeyType() != KeyType.ArrowDown && keyStroke.getKeyType() != KeyType.ArrowUp && keyStroke.getKeyType() != KeyType.ArrowLeft && keyStroke.getKeyType() != KeyType.ArrowRight && keyStroke.getKeyType() != KeyType.PageDown && keyStroke.getKeyType() != KeyType.PageUp && keyStroke.getKeyType() != KeyType.Home && keyStroke.getKeyType() != KeyType.End)
    {
      return false;
    }
    return true;
  }
  
  /*
   * public void updateSelection(KeyStroke keyStroke) { if
   * (keyStroke.isShiftDown() || keyStroke.getKeyType() == KeyType.MouseEvent) {
   * if (getSelectionStartPosition() != null) {
   * setSelectionEndPosition(getCaretPosition()); } else {
   * setSelectionStartPosition(getCaretPosition()); } } }
   */
  
  public void updateSelection(KeyStroke keyStroke, int x, int y)
  {
    if (keyStroke.isShiftDown() || keyStroke.getKeyType() == KeyType.MouseEvent)
    {
      if (getSelectionStartPosition() != null)
      {
        setSelectionEndPosition(new TerminalPosition(x, y));
      }
      else
      {
        setSelectionStartPosition(new TerminalPosition(x, y));
      }
    }
  }
  
  public void setVerticalAdjustable(Adjustable adjustable)
  {
    this.getRenderer().setVerticalAdjustable(adjustable);
    adjustable.addAdjustmentListener(new AdjustmentListener()
    {
      public void adjustmentValueChanged(AdjustmentEvent e)
      {
        // System.out.println("adjustmentValueChanged:" + e.getValue());
        int value = e.getValue();
        if (isReadOnly())
        {
          getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRow(value));
          invalidate();
        }
        else
        {
          int row = getRenderer().getViewTopLeft().getRow();
          int difference = value - row;
          if (difference > 0)
          {
            for (int i = 0; i < difference; i++)
            {
              scrolldown();
            }
          }
          else
          {
            difference = difference * -1;
            for (int i = 0; i < difference; i++)
            {
              scrollup();
            }
          }
          getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRow(value));
          invalidate();
        }
        // setCaretPosition(e.getValue(), getCaretPosition().getColumn());
        // getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRow(e.getValue()));
        // invalidate();
      }
    });
  }
  
  public void setHorizontalAdjustable(Adjustable adjustable)
  {
    this.getRenderer().setHorizontalAdjustable(adjustable);
    adjustable.addAdjustmentListener(new AdjustmentListener()
    {
      public void adjustmentValueChanged(AdjustmentEvent e)
      {
        // System.out.println(e.toString());
        // System.out.println("visible:" +
        // e.getAdjustable().getVisibleAmount());
        // System.out.println("maximum:" + e.getAdjustable().getMaximum());
        int value = e.getValue();
        if (isReadOnly())
        {
          getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withColumn(value));
          invalidate();
        }
        else
        {
          int column = getRenderer().getViewTopLeft().getColumn();
          int difference = value - column;
          if (difference > 0)
          {
            for (int i = 0; i < difference; i++)
            {
              scrollright();
            }
          }
          else
          {
            difference = difference * -1;
            for (int i = 0; i < difference; i++)
            {
              scrollleft();
            }
          }
          getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withColumn(value));
          invalidate();
        }
        // setCaretPosition(e.getValue(), getCaretPosition().getColumn());
        // getRenderer().setViewTopLeft(getRenderer().getViewTopLeft().withRow(e.getValue()));
        // invalidate();
      }
    });
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
    String line = lines.get(caretPosition.getRow());
    if (caretPosition.getRow() > 0)
    {
      int trueColumnPosition = TerminalTextUtils.getColumnIndex(lines.get(caretPosition.getRow()), caretPosition.getColumn());
      caretPosition = caretPosition.withRelativeRow(-1);
      line = lines.get(caretPosition.getRow());
      if (trueColumnPosition > TerminalTextUtils.getColumnWidth(line))
      {
        caretPosition = caretPosition.withColumn(line.length());
      }
      else
      {
        caretPosition = caretPosition.withColumn(TerminalTextUtils.getStringCharacterIndex(line, trueColumnPosition));
      }
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
    String line = lines.get(caretPosition.getRow());
    if (caretPosition.getRow() < lines.size() - 1)
    {
      int trueColumnPosition = TerminalTextUtils.getColumnIndex(lines.get(caretPosition.getRow()), caretPosition.getColumn());
      caretPosition = caretPosition.withRelativeRow(1);
      line = lines.get(caretPosition.getRow());
      if (trueColumnPosition > TerminalTextUtils.getColumnWidth(line))
      {
        caretPosition = caretPosition.withColumn(line.length());
      }
      else
      {
        caretPosition = caretPosition.withColumn(TerminalTextUtils.getStringCharacterIndex(line, trueColumnPosition));
      }
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
    if (caretPosition.getColumn() < lines.get(caretPosition.getRow()).length())
    {
      caretPosition = caretPosition.withRelativeColumn(1);
    }
//		if(caretPosition.getColumn() < longestRow + 1) {
//			caretPosition = caretPosition.withRelativeColumn(1);
//		}
  }
  
  /**
   * Sets a pattern on which the content of the text box is to be validated. For
   * multi-line TextBox:s, the pattern is checked against each line
   * individually, not the content as a whole. Partial matchings will not be
   * allowed, the whole pattern must match, however, empty lines will always be
   * allowed. When the user tried to modify the content of the TextBox in a way
   * that does not match the pattern, the operation will be silently ignored. If
   * you set this pattern to {@code null}, all validation is turned off.
   * 
   * @param validationPattern
   *          Pattern to validate the lines in this TextBox against, or
   *          {@code null} to disable
   * @return itself
   */
  public synchronized VTLanternaTextBoxModified setValidationPattern(Pattern validationPattern)
  {
    if (validationPattern != null)
    {
      for (String line : lines)
      {
        if (!validated(line))
        {
          throw new IllegalStateException("TextBox validation pattern " + validationPattern + " does not match existing content");
        }
      }
    }
    this.validationPattern = validationPattern;
    return this;
  }
  
  /**
   * Updates the text content of the {@code TextBox} to the supplied string.
   * 
   * @param text
   *          New text to assign to the {@code TextBox}
   * @return Itself
   */
  public synchronized VTLanternaTextBoxModified setText(String text)
  {
    // String[] split = text.split("[\\n\\u000b]", -1);
    // text = text.replace('\u000b', '\n');
    String[] split = text.split("\n");
    if (split.length == 0)
    {
      split = new String[]
      { "" };
    }
    lines.clear();
    longestRow = 80;
    for (String line : split)
    {
      addLine(line);
    }
    if (caretPosition.getRow() > lines.size() - 1)
    {
      caretPosition = caretPosition.withRow(lines.size() - 1);
    }
    if (caretPosition.getColumn() > lines.get(caretPosition.getRow()).length())
    {
      caretPosition = caretPosition.withColumn(lines.get(caretPosition.getRow()).length());
    }
    invalidate();
    return this;
  }
  
  public TextBoxRenderer getRenderer()
  {
    return (TextBoxRenderer) super.getRenderer();
  }
  
  /**
   * Adds a single line to the {@code TextBox} at the end, this only works when
   * in multi-line mode
   * 
   * @param line
   *          Line to add at the end of the content in this {@code TextBox}
   * @return Itself
   */
  public synchronized VTLanternaTextBoxModified addLine(String line)
  {
    StringBuilder bob = new StringBuilder();
    for (int i = 0; i < line.length(); i++)
    {
      char c = line.charAt(i);
      if ((c == '\n' || c == '\u000b') && style == Style.MULTI_LINE)
      {
        String string = bob.toString();
        int lineWidth = TerminalTextUtils.getColumnWidth(string);
        lines.add(string);
        if (longestRow < lineWidth + 1)
        {
          longestRow = lineWidth + 1;
        }
        addLine(line.substring(i + 1));
        return this;
      }
      else if (Character.isISOControl(c) && c != '\t')
      {
        // else if(!TerminalTextUtils.isPrintableCharacter(c)) {
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
    if (longestRow < lineWidth + 1)
    {
      longestRow = lineWidth + 1;
    }
    invalidate();
    return this;
  }
  
  /**
   * Sets if the caret should jump to the beginning of the next line if right
   * arrow is pressed while at the end of a line. Similarly, pressing left arrow
   * at the beginning of a line will make the caret jump to the end of the
   * previous line. This only makes sense for multi-line TextBox:es; for
   * single-line ones it has no effect. By default this is {@code false}.
   * 
   * @param caretWarp
   *          Whether the caret will warp at the beginning/end of lines
   * @return Itself
   */
  public VTLanternaTextBoxModified setCaretWarp(boolean caretWarp)
  {
    this.caretWarp = caretWarp;
    return this;
  }
  
  /**
   * Checks whether caret warp mode is enabled or not. See {@code setCaretWarp}
   * for more details.
   * 
   * @return {@code true} if caret warp mode is enabled
   */
  public boolean isCaretWarp()
  {
    return caretWarp;
  }
  
  /**
   * Returns the position of the caret, as a {@code TerminalPosition} where the
   * row and columns equals the coordinates in a multi-line {@code TextBox} and
   * for single-line {@code TextBox} you can ignore the {@code row} component.
   * 
   * @return Position of the text input caret
   */
  public TerminalPosition getCaretPosition()
  {
    return caretPosition;
  }
  
  /**
   * Moves the text caret position horizontally to a new position in the
   * {@link VTLanternaTextBoxModified}. For multi-line
   * {@link VTLanternaTextBoxModified}:es, this will move the cursor within the
   * current line. If the position is out of bounds, it is automatically set
   * back into range.
   * 
   * @param column
   *          Position, in characters, within the
   *          {@link VTLanternaTextBoxModified} (on the current line for
   *          multi-line {@link VTLanternaTextBoxModified}:es) to where the text
   *          cursor should be moved
   * @return Itself
   */
  public synchronized VTLanternaTextBoxModified setCaretPosition(int column)
  {
    return setCaretPosition(getCaretPosition().getRow(), column);
  }
  
  /**
   * Moves the text caret position to a new position in the
   * {@link VTLanternaTextBoxModified}. For single-line
   * {@link VTLanternaTextBoxModified}:es, the line component is not used. If
   * one of the positions are out of bounds, it is automatically set back into
   * range.
   * 
   * @param line
   *          Which line inside the {@link VTLanternaTextBoxModified} to move
   *          the caret to (0 being the first line), ignored if the
   *          {@link VTLanternaTextBoxModified} is single-line
   * @param column
   *          What column on the specified line to move the text caret to (0
   *          being the first column)
   * @return Itself
   */
  public synchronized VTLanternaTextBoxModified setCaretPosition(int line, int column)
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
    else if (column > longestRow)
    {
      column = longestRow;
    }
    caretPosition = caretPosition.withRow(line).withColumn(column);
    return this;
  }
  
  /**
   * Returns the text in this {@code TextBox}, for multi-line mode all lines
   * will be concatenated together with \n as separator.
   * 
   * @return The text inside this {@code TextBox}
   */
  public synchronized String getText()
  {
    StringBuilder bob = new StringBuilder(lines.get(0));
    for (int i = 1; i < lines.size(); i++)
    {
      bob.append("\n").append(lines.get(i));
    }
    return bob.toString();
  }
  
  /**
   * Helper method, it will return the content of the {@code TextBox} unless
   * it's empty in which case it will return the supplied default value
   * 
   * @param defaultValueIfEmpty
   *          Value to return if the {@code TextBox} is empty
   * @return Text in the {@code TextBox} or {@code defaultValueIfEmpty} is the
   *         {@code TextBox} is empty
   */
  public String getTextOrDefault(String defaultValueIfEmpty)
  {
    String text = getText();
    if (text.length() == 0)
    {
      return defaultValueIfEmpty;
    }
    return text;
  }
  
  /**
   * Returns the current text mask, meaning the substitute to draw instead of
   * the text inside the {@code TextBox}. This is normally used for password
   * input fields so the password isn't shown
   * 
   * @return Current text mask or {@code null} if there is no mask
   */
  public Character getMask()
  {
    return mask;
  }
  
  /**
   * Sets the current text mask, meaning the substitute to draw instead of the
   * text inside the {@code TextBox}. This is normally used for password input
   * fields so the password isn't shown
   * 
   * @param mask
   *          New text mask or {@code null} if there is no mask
   * @return Itself
   */
  public VTLanternaTextBoxModified setMask(Character mask)
  {
    if (mask != null && TerminalTextUtils.isCharCJK(mask))
    {
      throw new IllegalArgumentException("Cannot use a CJK character as a mask");
    }
    this.mask = mask;
    invalidate();
    return this;
  }
  
  /**
   * Returns {@code true} if this {@code TextBox} is in read-only mode, meaning
   * text input from the user through the keyboard is prevented
   * 
   * @return {@code true} if this {@code TextBox} is in read-only mode
   */
  public boolean isReadOnly()
  {
    return readOnly;
  }
  
  /**
   * Sets the read-only mode of the {@code TextBox}, meaning text input from the
   * user through the keyboard is prevented. The user can still focus and scroll
   * through the text in this mode.
   * 
   * @param readOnly
   *          If {@code true} then the {@code TextBox} will switch to read-only
   *          mode
   * @return Itself
   */
  public VTLanternaTextBoxModified setReadOnly(boolean readOnly)
  {
    this.readOnly = readOnly;
    invalidate();
    return this;
  }
  
  /**
   * If {@code true}, the component will switch to the next available component
   * above if the cursor is at the top of the TextBox and the user presses the
   * 'up' array key, or switch to the next available component below if the
   * cursor is at the bottom of the TextBox and the user presses the 'down'
   * array key. The means that for single-line TextBox:es, pressing up and down
   * will always switch focus.
   * 
   * @return {@code true} if vertical focus switching is enabled
   */
  public boolean isVerticalFocusSwitching()
  {
    return verticalFocusSwitching;
  }
  
  /**
   * If set to {@code true}, the component will switch to the next available
   * component above if the cursor is at the top of the TextBox and the user
   * presses the 'up' array key, or switch to the next available component below
   * if the cursor is at the bottom of the TextBox and the user presses the
   * 'down' array key. The means that for single-line TextBox:es, pressing up
   * and down will always switch focus with this mode enabled.
   * 
   * @param verticalFocusSwitching
   *          If called with true, vertical focus switching will be enabled
   * @return Itself
   */
  public VTLanternaTextBoxModified setVerticalFocusSwitching(boolean verticalFocusSwitching)
  {
    this.verticalFocusSwitching = verticalFocusSwitching;
    return this;
  }
  
  /**
   * If {@code true}, the TextBox will switch focus to the next available
   * component to the left if the cursor in the TextBox is at the left-most
   * position (index 0) on the row and the user pressed the 'left' arrow key, or
   * vice versa for pressing the 'right' arrow key when the cursor in at the
   * right-most position of the current row.
   * 
   * @return {@code true} if horizontal focus switching is enabled
   */
  public boolean isHorizontalFocusSwitching()
  {
    return horizontalFocusSwitching;
  }
  
  /**
   * If set to {@code true}, the TextBox will switch focus to the next available
   * component to the left if the cursor in the TextBox is at the left-most
   * position (index 0) on the row and the user pressed the 'left' arrow key, or
   * vice versa for pressing the 'right' arrow key when the cursor in at the
   * right-most position of the current row.
   * 
   * @param horizontalFocusSwitching
   *          If called with true, horizontal focus switching will be enabled
   * @return Itself
   */
  public VTLanternaTextBoxModified setHorizontalFocusSwitching(boolean horizontalFocusSwitching)
  {
    this.horizontalFocusSwitching = horizontalFocusSwitching;
    return this;
  }
  
  /**
   * Returns the line on the specific row. For non-multiline TextBox:es, calling
   * this with index set to 0 will return the same as calling {@code getText()}.
   * If the row index is invalid (less than zero or equals or larger than the
   * number of rows), this method will throw IndexOutOfBoundsException.
   * 
   * @param index
   *          Index of the row to return the contents from
   * @return The line at the specified index, as a String
   * @throws IndexOutOfBoundsException
   *           if the row index is less than zero or too large
   */
  public synchronized String getLine(int index)
  {
    return lines.get(index);
  }
  
  /**
   * Returns the number of lines currently in this TextBox. For single-line
   * TextBox:es, this will always return 1.
   * 
   * @return Number of lines of text currently in this TextBox
   */
  public synchronized int getLineCount()
  {
    return lines.size();
  }
  
  protected TextBoxRenderer createDefaultRenderer()
  {
    DefaultTextBoxRenderer renderer = new DefaultTextBoxRenderer();
    // renderer.setTerminal(terminal);
    return renderer;
  }
  
  public synchronized Result handleKeyStroke(KeyStroke keyStroke)
  {
    // System.out.println("TextBoxModified." + keyStroke);
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
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
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
        if (caretPosition.getColumn() < lines.get(caretPosition.getRow()).length())
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
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case ArrowUp:
        if (caretPosition.getRow() > 0)
        {
          int trueColumnPosition = TerminalTextUtils.getColumnIndex(lines.get(caretPosition.getRow()), caretPosition.getColumn());
          caretPosition = caretPosition.withRelativeRow(-1);
          line = lines.get(caretPosition.getRow());
          if (trueColumnPosition > TerminalTextUtils.getColumnWidth(line))
          {
            caretPosition = caretPosition.withColumn(line.length());
          }
          else
          {
            caretPosition = caretPosition.withColumn(TerminalTextUtils.getStringCharacterIndex(line, trueColumnPosition));
          }
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
          int trueColumnPosition = TerminalTextUtils.getColumnIndex(lines.get(caretPosition.getRow()), caretPosition.getColumn());
          caretPosition = caretPosition.withRelativeRow(1);
          line = lines.get(caretPosition.getRow());
          if (trueColumnPosition > TerminalTextUtils.getColumnWidth(line))
          {
            caretPosition = caretPosition.withColumn(line.length());
          }
          else
          {
            caretPosition = caretPosition.withColumn(TerminalTextUtils.getStringCharacterIndex(line, trueColumnPosition));
          }
        }
        else if (verticalFocusSwitching)
        {
          return Result.MOVE_FOCUS_DOWN;
        }
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case End:
        caretPosition = caretPosition.withColumn(line.length());
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
        if (lines.get(caretPosition.getRow()).length() < caretPosition.getColumn())
        {
          caretPosition = caretPosition.withColumn(lines.get(caretPosition.getRow()).length());
        }
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      case PageUp:
        caretPosition = caretPosition.withRelativeRow(-getSize().getRows());
        if (caretPosition.getRow() < 0)
        {
          caretPosition = caretPosition.withRow(0);
        }
        if (lines.get(caretPosition.getRow()).length() < caretPosition.getColumn())
        {
          caretPosition = caretPosition.withColumn(lines.get(caretPosition.getRow()).length());
        }
        updateSelection(keyStroke, caretPosition.getColumn(), caretPosition.getRow());
        return Result.HANDLED;
      default:
    }
    // System.out.println("super");
    return super.handleKeyStroke(keyStroke);
  }
  
  protected boolean validated(String line)
  {
    return validationPattern == null || line.length() == 0 || validationPattern.matcher(line).matches();
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
  
  public int getMaxLineLength()
  {
    return maxLineLength;
  }
  
  /**
   * Helper interface that doesn't add any new methods but makes coding new text
   * box renderers a little bit more clear
   */
  public interface TextBoxRenderer extends InteractableRenderer<VTLanternaTextBoxModified>
  {
    TerminalPosition getViewTopLeft();
    void setViewTopLeft(TerminalPosition position);
    void setVerticalAdjustable(Adjustable adjustable);
    void setHorizontalAdjustable(Adjustable adjustable);
    // void setTerminal(Terminal terminal);
  }
  
  /**
   * This is the default text box renderer that is used if you don't override
   * anything. With this renderer, the text box is filled with a solid
   * background color and the text is drawn on top of it. Scrollbars are added
   * for multi-line text whenever the text inside the {@code TextBox} does not
   * fit in the available area.
   */
  public static class DefaultTextBoxRenderer implements TextBoxRenderer
  {
    protected TerminalPosition viewTopLeft;
    protected final ScrollBar verticalScrollBar;
    protected final ScrollBar horizontalScrollBar;
    protected boolean hideScrollBars;
    protected Character unusedSpaceCharacter;
    protected Adjustable verticalAdjustable;
    protected Adjustable horizontalAdjustable;
    
    // private Terminal terminal;
    /**
     * Default constructor
     */
    public DefaultTextBoxRenderer()
    {
      viewTopLeft = TerminalPosition.TOP_LEFT_CORNER;
      verticalScrollBar = new ScrollBar(Direction.VERTICAL);
      horizontalScrollBar = new ScrollBar(Direction.HORIZONTAL);
      hideScrollBars = false;
      unusedSpaceCharacter = null;
    }
    
    public void setVerticalAdjustable(Adjustable adjustable)
    {
      this.verticalAdjustable = adjustable;
      //adjustable.setMinimum(0);
      
    }
    
    public void setHorizontalAdjustable(Adjustable adjustable)
    {
      this.horizontalAdjustable = adjustable;
      //adjustable.setMinimum(0);
      
    }
    
    /**
     * Sets the character to represent an empty untyped space in the text box.
     * This will be an empty space by default but you can override it to
     * anything that isn't double-width.
     * 
     * @param unusedSpaceCharacter
     *          Character to draw in unused space of the
     *          {@link VTLanternaTextBoxModified}
     * @throws IllegalArgumentException
     *           If unusedSpaceCharacter is a double-width character
     */
    public void setUnusedSpaceCharacter(char unusedSpaceCharacter)
    {
      if (TerminalTextUtils.isCharDoubleWidth(unusedSpaceCharacter))
      {
        throw new IllegalArgumentException("Cannot use a double-width character as the unused space character in a TextBox");
      }
      this.unusedSpaceCharacter = unusedSpaceCharacter;
    }
    
    public TerminalPosition getViewTopLeft()
    {
      return viewTopLeft;
    }
    
    public void setViewTopLeft(TerminalPosition position)
    {
      if (position.getColumn() < 0)
      {
        position = position.withColumn(0);
      }
      if (position.getRow() < 0)
      {
        position = position.withRow(0);
      }
      viewTopLeft = position;
    }
    
    public TerminalPosition getCursorLocation(VTLanternaTextBoxModified component)
    {
      if (component.isReadOnly())
      {
        return null;
      }
      
      // Adjust caret position if necessary
      TerminalPosition caretPosition = component.getCaretPosition();
      String line = component.getLine(caretPosition.getRow());
      // caretPosition =
      // caretPosition.withColumn(Math.min(caretPosition.getColumn(),
      // line.length()));
      caretPosition = caretPosition.withColumn(Math.min(caretPosition.getColumn(), component.longestRow));
      
      if (caretPosition.getColumn() < line.length())
      {
        return caretPosition.withColumn(TerminalTextUtils.getColumnIndex(line, caretPosition.getColumn())).withRelativeColumn(-viewTopLeft.getColumn()).withRelativeRow(-viewTopLeft.getRow());
      }
      else
      {
        return caretPosition
        // .withColumn(TerminalTextUtils.getColumnIndex(line,
        // caretPosition.getColumn()))
        .withRelativeColumn(-viewTopLeft.getColumn()).withRelativeRow(-viewTopLeft.getRow());
      }
    }
    
    public TerminalPosition getSelectionStartLocation(VTLanternaTextBoxModified component)
    {
      if (!component.selectingText())
      {
        return null;
      }
      
      TerminalPosition selectionStart = new TerminalPosition(component.getMinColumn(), component.getMinRow());
      return selectionStart.withRelativeColumn(-viewTopLeft.getColumn()).withRelativeRow(-viewTopLeft.getRow());
    }
    
    public TerminalPosition getSelectionEndLocation(VTLanternaTextBoxModified component)
    {
      if (!component.selectingText())
      {
        return null;
      }
      
      TerminalPosition selectionEnd = new TerminalPosition(component.getMaxColumn(), component.getMaxRow());
      return selectionEnd.withRelativeColumn(-viewTopLeft.getColumn()).withRelativeRow(-viewTopLeft.getRow());
    }
    
    public TerminalSize getPreferredSize(VTLanternaTextBoxModified component)
    {
      return new TerminalSize(component.longestRow, component.lines.size());
    }
    
    /**
     * Controls whether scrollbars should be visible or not when a multi-line
     * {@code TextBox} has more content than it can draw in the area it was
     * assigned (default: false)
     * 
     * @param hideScrollBars
     *          If {@code true}, don't show scrollbars if the multi-line content
     *          is bigger than the area
     */
    public void setHideScrollBars(boolean hideScrollBars)
    {
      this.hideScrollBars = hideScrollBars;
    }
    
    public void drawComponent(TextGUIGraphics graphics, VTLanternaTextBoxModified component)
    {
      TerminalSize realTextArea = graphics.getSize();
      if (realTextArea.getRows() == 0 || realTextArea.getColumns() == 0)
      {
        return;
      }
      boolean drawVerticalScrollBar = false;
      boolean drawHorizontalScrollBar = false;
      int textBoxLineCount = component.getLineCount();
      if (!hideScrollBars && textBoxLineCount > realTextArea.getRows() && realTextArea.getColumns() > 1)
      {
        realTextArea = realTextArea.withRelativeColumns(-1);
        drawVerticalScrollBar = true;
      }
      if (!hideScrollBars && component.longestRow > realTextArea.getColumns() && realTextArea.getRows() > 1)
      {
        realTextArea = realTextArea.withRelativeRows(-1);
        drawHorizontalScrollBar = true;
        if (textBoxLineCount > realTextArea.getRows() && !drawVerticalScrollBar)
        {
          realTextArea = realTextArea.withRelativeColumns(-1);
          drawVerticalScrollBar = true;
        }
      }
      
      drawTextArea(graphics.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, realTextArea), component);
      
      // Draw scrollbars, if any
      if (verticalAdjustable != null)
      {
        int vmax = Math.max(realTextArea.getRows(), textBoxLineCount);
        // int vmin = Math.min(realTextArea.getRows(), textBoxLineCount);
        if (verticalAdjustable.getVisibleAmount() != realTextArea.getRows())
        {
          verticalAdjustable.setVisibleAmount(realTextArea.getRows());
        }
        if (verticalAdjustable.getMaximum() != vmax)
        {
          verticalAdjustable.setMaximum(vmax);
        }
        if (verticalAdjustable.getBlockIncrement() != graphics.getSize().getRows())
        {
          verticalAdjustable.setBlockIncrement(graphics.getSize().getRows());
        }
        if (verticalAdjustable.getValue() != viewTopLeft.getRow())
        {
          verticalAdjustable.setValue(viewTopLeft.getRow());
        }
      }
      if (horizontalAdjustable != null)
      {
        int hmax = Math.max(realTextArea.getColumns(), component.longestRow);
        // int hmin = Math.min(realTextArea.getColumns(), component.longestRow);
        if (horizontalAdjustable.getVisibleAmount() != realTextArea.getColumns())
        {
          horizontalAdjustable.setVisibleAmount(realTextArea.getColumns());
        }
        if (horizontalAdjustable.getMaximum() != hmax)
        {
          horizontalAdjustable.setMaximum(hmax);
        }
        if (horizontalAdjustable.getBlockIncrement() != graphics.getSize().getColumns())
        {
          horizontalAdjustable.setBlockIncrement(graphics.getSize().getColumns());
        }
        if (horizontalAdjustable.getValue() != viewTopLeft.getColumn())
        {
          horizontalAdjustable.setValue(viewTopLeft.getColumn());
        }
      }
      if (drawVerticalScrollBar)
      {
        verticalScrollBar.onAdded(component.getParent());
        verticalScrollBar.setViewSize(realTextArea.getRows());
        verticalScrollBar.setScrollMaximum(textBoxLineCount);
        verticalScrollBar.setScrollPosition(viewTopLeft.getRow());
        verticalScrollBar.draw(graphics.newTextGraphics(new TerminalPosition(graphics.getSize().getColumns() - 1, 0), new TerminalSize(1, graphics.getSize().getRows() - (drawHorizontalScrollBar ? 1 : 0))));
      }
      if (drawHorizontalScrollBar)
      {
        horizontalScrollBar.onAdded(component.getParent());
        horizontalScrollBar.setViewSize(realTextArea.getColumns());
        horizontalScrollBar.setScrollMaximum(component.longestRow - 1);
        horizontalScrollBar.setScrollPosition(viewTopLeft.getColumn());
        horizontalScrollBar.draw(graphics.newTextGraphics(new TerminalPosition(0, graphics.getSize().getRows() - 1), new TerminalSize(graphics.getSize().getColumns() - (drawVerticalScrollBar ? 1 : 0), 1)));
      }
    }
    
    private void drawTextArea(TextGUIGraphics graphics, VTLanternaTextBoxModified component)
    {
      TerminalSize textAreaSize = graphics.getSize();
      if (viewTopLeft.getColumn() + textAreaSize.getColumns() > component.longestRow)
      {
        viewTopLeft = viewTopLeft.withColumn(component.longestRow - textAreaSize.getColumns());
        if (viewTopLeft.getColumn() < 0)
        {
          viewTopLeft = viewTopLeft.withColumn(0);
        }
      }
      if (viewTopLeft.getRow() + textAreaSize.getRows() > component.getLineCount())
      {
        viewTopLeft = viewTopLeft.withRow(component.getLineCount() - textAreaSize.getRows());
        if (viewTopLeft.getRow() < 0)
        {
          viewTopLeft = viewTopLeft.withRow(0);
        }
      }
      ThemeDefinition themeDefinition = component.getThemeDefinition();
      if (component.isFocused())
      {
        if (component.isReadOnly())
        {
          graphics.applyThemeStyle(themeDefinition.getSelected());
        }
        else
        {
          graphics.applyThemeStyle(themeDefinition.getActive());
        }
      }
      else
      {
        if (component.isReadOnly())
        {
          graphics.applyThemeStyle(themeDefinition.getInsensitive());
        }
        else
        {
          graphics.applyThemeStyle(themeDefinition.getNormal());
        }
      }
      
      Character fillCharacter = unusedSpaceCharacter;
      if (fillCharacter == null)
      {
        fillCharacter = themeDefinition.getCharacter("FILL", ' ');
      }
      graphics.fill(fillCharacter);
      
      if (!component.isReadOnly())
      {
        // Adjust caret position if necessary
        TerminalPosition caretPosition = component.getCaretPosition();
        String caretLine = component.getLine(caretPosition.getRow());
        // caretPosition =
        // caretPosition.withColumn(Math.min(caretPosition.getColumn(),
        // caretLine.length()));
        caretPosition = caretPosition.withColumn(Math.min(caretPosition.getColumn(), component.longestRow));
        
        // Adjust the view if necessary
        int trueColumnPosition = caretPosition.getColumn();
        // int trueColumnPosition = TerminalTextUtils.getColumnIndex(caretLine,
        // caretPosition.getColumn());
        // int trueColumnPosition = caretPosition.getColumn();
        if (caretLine.length() > trueColumnPosition)
        {
          trueColumnPosition = TerminalTextUtils.getColumnIndex(caretLine, caretPosition.getColumn());
        }
        
        if (trueColumnPosition < viewTopLeft.getColumn())
        {
          viewTopLeft = viewTopLeft.withColumn(trueColumnPosition);
        }
        else if (trueColumnPosition >= textAreaSize.getColumns() + viewTopLeft.getColumn())
        {
          viewTopLeft = viewTopLeft.withColumn(trueColumnPosition - textAreaSize.getColumns() + 1);
        }
        if (caretPosition.getRow() < viewTopLeft.getRow())
        {
          viewTopLeft = viewTopLeft.withRow(caretPosition.getRow());
        }
        else if (caretPosition.getRow() >= textAreaSize.getRows() + viewTopLeft.getRow())
        {
          viewTopLeft = viewTopLeft.withRow(caretPosition.getRow() - textAreaSize.getRows() + 1);
        }
        
        // Additional corner-case for CJK characters
        if (trueColumnPosition - viewTopLeft.getColumn() == graphics.getSize().getColumns() - 1)
        {
          if (caretLine.length() > caretPosition.getColumn() && TerminalTextUtils.isCharCJK(caretLine.charAt(caretPosition.getColumn())))
          {
            viewTopLeft = viewTopLeft.withRelativeColumn(1);
          }
        }
      }
      
      TerminalPosition selectionStartLocation = getSelectionStartLocation(component);
      TerminalPosition selectionEndLocation = getSelectionEndLocation(component);
      
      if (selectionStartLocation != null && selectionEndLocation != null)
      {
        for (int row = 0; row < textAreaSize.getRows(); row++)
        {
          int rowIndex = row + viewTopLeft.getRow();
          if (rowIndex >= component.lines.size())
          {
            continue;
          }
          String line = component.lines.get(rowIndex);
          if (component.getMask() != null)
          {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < line.length(); i++)
            {
              builder.append(component.getMask());
            }
            line = builder.toString();
          }
          String fitString = TerminalTextUtils.fitString(line, viewTopLeft.getColumn(), textAreaSize.getColumns());
          try
          {
            if (row >= selectionStartLocation.getRow() && row <= selectionEndLocation.getRow())
            {
              EnumSet<SGR> modifiers = graphics.getActiveModifiers();
              modifiers.remove(SGR.REVERSE);
              int i = selectionStartLocation.getColumn();
              if (i > fitString.length())
              {
                i = fitString.length();
              }
              int j = selectionEndLocation.getColumn() + 1;
              if (j > fitString.length())
              {
                j = fitString.length();
              }
              String start = fitString.substring(0, i);
              String selected = fitString.substring(i, j);
              String end = fitString.substring(j);
              if (start.length() > 0)
              {
                graphics.putString(0, row, start, modifiers);
              }
              modifiers.add(SGR.REVERSE);
              if (selected.length() > 0)
              {
                graphics.putString(i, row, selected, modifiers);
              }
              modifiers.remove(SGR.REVERSE);
              if (end.length() > 0)
              {
                graphics.putString(j, row, end, modifiers);
              }
            }
            else
            {
              EnumSet<SGR> modifiers = graphics.getActiveModifiers();
              modifiers.remove(SGR.REVERSE);
              graphics.putString(0, row, fitString, modifiers);
            }
          }
          catch (Throwable t)
          {
            // t.printStackTrace();
            EnumSet<SGR> modifiers = graphics.getActiveModifiers();
            modifiers.remove(SGR.REVERSE);
            graphics.putString(0, row, fitString, modifiers);
          }
        }
      }
      else
      {
        for (int row = 0; row < textAreaSize.getRows(); row++)
        {
          int rowIndex = row + viewTopLeft.getRow();
          if (rowIndex >= component.lines.size())
          {
            continue;
          }
          String line = component.lines.get(rowIndex);
          if (component.getMask() != null)
          {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < line.length(); i++)
            {
              builder.append(component.getMask());
            }
            line = builder.toString();
          }
          EnumSet<SGR> modifiers = graphics.getActiveModifiers();
          modifiers.remove(SGR.REVERSE);
          graphics.putString(0, row, TerminalTextUtils.fitString(line, viewTopLeft.getColumn(), textAreaSize.getColumns()), modifiers);
        }
      }
      
      TerminalPosition caretPosition;
      String caretLine;
      
      caretPosition = component.getCaretPosition();
      caretLine = component.getLine(caretPosition.getRow());
      caretPosition = this.getCursorLocation(component);
      
      int i = caretPosition.getColumn();
      int j = caretPosition.getColumn() + 1;
      
      if (i > caretLine.length())
      {
        i = caretLine.length();
      }
      if (j > caretLine.length())
      {
        j = caretLine.length();
      }
      
      String caretChar = caretLine.substring(i, j);
      
      if (caretChar.length() == 0)
      {
        i = caretPosition.getColumn();
        caretChar = " ";
      }
      
      EnumSet<SGR> modifiers = graphics.getActiveModifiers();
      modifiers.add(SGR.REVERSE);
      graphics.putString(i, caretPosition.getRow(), caretChar, modifiers);
      modifiers.remove(SGR.REVERSE);
    }
  }
  
  private TerminalPosition selectionStartPosition;
  
  private TerminalPosition selectionEndPosition;
  
  public void setSelectionStartPosition(TerminalPosition position)
  {
    selectionStartPosition = position;
  }
  
  public void setSelectionEndPosition(TerminalPosition position)
  {
    selectionEndPosition = position;
  }
  
  public TerminalPosition getSelectionStartPosition()
  {
    return selectionStartPosition;
  }
  
  public TerminalPosition getSelectionEndPosition()
  {
    return selectionEndPosition;
  }
  
  public int getMinColumn()
  {
    return Math.min(selectionStartPosition.getColumn(), selectionEndPosition.getColumn());
  }
  
  public int getMaxColumn()
  {
    return Math.max(selectionStartPosition.getColumn(), selectionEndPosition.getColumn());
  }
  
  public int getMinRow()
  {
    return Math.min(selectionStartPosition.getRow(), selectionEndPosition.getRow());
  }
  
  public int getMaxRow()
  {
    return Math.max(selectionStartPosition.getRow(), selectionEndPosition.getRow());
  }
  
  public boolean selectingText()
  {
    return (selectionStartPosition != null) && (selectionEndPosition != null);
  }
  
  public String getSelectedText()
  {
    StringBuilder data = new StringBuilder("");
    if (selectingText())
    {
      try
      {
        int minX = getMinColumn();
        int maxX = getMaxColumn() + 1;
        int minY = getMinRow();
        int maxY = getMaxRow();
        // System.out.println("minX:[" + minX + "]");
        // System.out.println("maxX:[" + maxX + "]");
        int start = 0;
        int end = 0;
        for (int row = minY; row <= maxY; row++)
        {
          if (row < lines.size())
          {
            try
            {
              String line = lines.get(row);
              if (line != null)
              {
                start = Math.min(minX, line.length());
                end = Math.min(maxX, line.length());
                data.append(line.substring(start, end) + "\n");
              }
            }
            catch (Throwable t)
            {
              // t.printStackTrace();
            }
          }
          else
          {
            break;
          }
        }
      }
      catch (Throwable t)
      {
        
      }
      if (data.length() > 0)
      {
        data.deleteCharAt(data.length() - 1);
      }
    }
    else
    {
      
    }
    // System.out.println("getSelectedText():[" + data.toString() + "]");
    return data.toString();
  }
}
