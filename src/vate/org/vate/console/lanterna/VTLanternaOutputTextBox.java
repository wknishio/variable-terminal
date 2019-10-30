package org.vate.console.lanterna;

import java.io.IOException;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Interactable.Result;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;

public class VTLanternaOutputTextBox extends TextBox
{
	private volatile int maximumlines = 0;
	private int hiddenColumn = 0;
	private int carriageColumn = -1;
	private Terminal terminal;
	//private int hiddenRow;
	
	public VTLanternaOutputTextBox(TerminalSize terminalSize, String string, Style multiLine, int maximumlines)
	{
		super(terminalSize, string, multiLine);
		this.maximumlines = maximumlines;
	}
	
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
			 if(getRenderer().getViewTopLeft().getRow() == 0)
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
		if(caretPosition.getRow() > 0) {
            int trueColumnPosition = TerminalTextUtils.getColumnIndex(lines.get(caretPosition.getRow()), caretPosition.getColumn());
            caretPosition = caretPosition.withRelativeRow(-1);
            line = lines.get(caretPosition.getRow());
            if(trueColumnPosition > TerminalTextUtils.getColumnWidth(line)) {
                caretPosition = caretPosition.withColumn(line.length());
            }
            else {
                caretPosition = caretPosition.withColumn(TerminalTextUtils.getStringCharacterIndex(line, trueColumnPosition));
            }
        }
	}
	
	public void scrolldown()
	{
		if (isReadOnly())
		{
			if(getRenderer().getViewTopLeft().getRow() + getSize().getRows() == lines.size())
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
		if(caretPosition.getRow() < lines.size() - 1) {
            int trueColumnPosition = TerminalTextUtils.getColumnIndex(lines.get(caretPosition.getRow()), caretPosition.getColumn());
            caretPosition = caretPosition.withRelativeRow(1);
            line = lines.get(caretPosition.getRow());
            if(trueColumnPosition > TerminalTextUtils.getColumnWidth(line)) {
                caretPosition = caretPosition.withColumn(line.length());
            }
            else {
                caretPosition = caretPosition.withColumn(TerminalTextUtils.getStringCharacterIndex(line, trueColumnPosition));
            }
        }
	}
				
	public void setViewportToLastLine()
	{
		getRenderer().setViewTopLeft(TerminalPosition.TOP_LEFT_CORNER.withRow(getLineCount() - getSize().getRows()));
	}
	
	//TODO:must support \n line feed
	//TODO:must support \r carriage return
	//TODO:must support \b backspace
	//TODO:must support \d delete 127
	//TODO:must support bell
	//TODO:must support clear
	
	private String outputToLastLine(String data)
	{
		String lastLine = getLastLine();
		StringBuilder builder = new StringBuilder(lastLine);
		
		int maxWidth = 80;
		
		try
		{
			maxWidth = terminal.getTerminalSize().getColumns();
		}
		catch (Throwable e)
		{
			
		}
		
		int remainingWidth = (carriageColumn >= 0 ? (maxWidth * 2) - carriageColumn : maxWidth) - lastLine.length();
		
		if (carriageColumn >= 0)
		{
			if (data.length() <= remainingWidth)
			{
				lastLine = builder.replace(carriageColumn, carriageColumn + data.length(), data).toString();
				carriageColumn += data.length();
				if (carriageColumn >= lastLine.length())
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
				String appended = data.substring(0, remainingWidth);
				String remainder = data.substring(remainingWidth);
				lastLine = builder.replace(carriageColumn, carriageColumn + appended.length(), appended).toString();
				carriageColumn += appended.length();
				if (carriageColumn >= lastLine.length())
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
		}
		else
		{
			if (data.length() <= remainingWidth)
			{
				lastLine = builder.append(data).toString();
				setLastLine(lastLine);
				if (longestRow < lastLine.length())
				{
					longestRow = lastLine.length();
				}
			}
			else
			{
				String appended = data.substring(0, remainingWidth);
				String remainder = data.substring(remainingWidth);
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
	
	//break string in multiple lines if needed
	private void outputMultiline(String data)
	{
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
		if (data !=-'\n')
		{
			if ((data > 0 && data < 32) || data == 127)
			{
				//reject
				return;
			}
			if (data == '\t')
			{
				data = ' ';
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
	
	private void removeLastLine()
	{
		if (getLineCount() >= 1)
		{
			String removed = lines.remove(0);
			if (removed.length() > 80 && removed.length() >= longestRow)
			{
				longestRow = 80;
				try
				{
					longestRow = terminal.getTerminalSize().getColumns();
				}
				catch (Throwable e)
				{
					
				}
				for (String line : lines)
				{
					if (line.length() > longestRow)
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
	
	public synchronized TextBox addLine(String data)
	{
		if (getLineCount() > 0 && maximumlines > 0 && getLineCount() == maximumlines)
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
		TextBox result = putLine(data);
		if (getLineCount() > 1)
		{
			setCaretPosition(getLineCount(), 0);
			setViewportToLastLine();
		}
		return result;
	}
	
	public synchronized TextBox putLine(String line) {
        StringBuilder bob = new StringBuilder();
        for(int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if(c == '\n' && style == Style.MULTI_LINE) {
                String string = bob.toString();
                int lineWidth = TerminalTextUtils.getColumnWidth(string);
                lines.add(string);
                if(longestRow < lineWidth + 1) {
                    longestRow = lineWidth + 1;
                }
                addLine(line.substring(i + 1));
                return this;
            }
            else if(Character.isISOControl(c)) {
                continue;
            }

            bob.append(c);
        }
        String string = bob.toString();
        if(!validated(string)) {
            throw new IllegalStateException("TextBox validation pattern " + validationPattern + " does not match the supplied text");
        }
        int lineWidth = TerminalTextUtils.getColumnWidth(string);
        lines.add(string);
        if(longestRow < lineWidth + 1) {
            longestRow = lineWidth + 1;
        }
        invalidate();
        return this;
    }
	
	public StringBuilder handleDataInput(StringBuilder inputBuffer, char data, boolean replace)
	{
		//String line = lines.get(caretPosition.getRow());
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
	
	public StringBuilder handleDataInput(StringBuilder inputBuffer, KeyType keytype)
	{
		int column = hiddenColumn;
		if (keytype == KeyType.Backspace)
		{
			if (inputBuffer.length() > 0 && column > 0)
			{
				inputBuffer.deleteCharAt(column - 1);
				hiddenColumn--;
			}
		}
		if (keytype == KeyType.Delete)
		{
			if (inputBuffer.length() > 0 && column < inputBuffer.length())
			{
				inputBuffer.deleteCharAt(column);
			}
		}
		if (keytype == KeyType.ArrowLeft)
		{
			if (column > 0)
			{
				hiddenColumn--;
			}
		}
		if (keytype == KeyType.ArrowRight)
		{
			if (column < inputBuffer.length())
			{
				hiddenColumn++;
			}
		}
		return inputBuffer;
	}
}
