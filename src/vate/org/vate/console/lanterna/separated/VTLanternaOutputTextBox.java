package org.vate.console.lanterna.separated;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.TextBox;
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
		if(caretPosition.getRow() > 0)
		{
            int trueColumnPosition = TerminalTextUtils.getColumnIndex(lines.get(caretPosition.getRow()), caretPosition.getColumn());
            caretPosition = caretPosition.withRelativeRow(-1);
            line = lines.get(caretPosition.getRow());
            if(trueColumnPosition > TerminalTextUtils.getColumnWidth(line))
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
		if(caretPosition.getRow() < lines.size() - 1)
		{
            int trueColumnPosition = TerminalTextUtils.getColumnIndex(lines.get(caretPosition.getRow()), caretPosition.getColumn());
            caretPosition = caretPosition.withRelativeRow(1);
            line = lines.get(caretPosition.getRow());
            if(trueColumnPosition > TerminalTextUtils.getColumnWidth(line))
            {
                caretPosition = caretPosition.withColumn(line.length());
            }
            else
            {
                caretPosition = caretPosition.withColumn(TerminalTextUtils.getStringCharacterIndex(line, trueColumnPosition));
            }
        }
	}
				
	public void setViewportToLastLine()
	{
		getRenderer().setViewTopLeft(TerminalPosition.TOP_LEFT_CORNER.withRow(getLineCount() - getSize().getRows()));
	}
	
	//support \n line feed
	//support \r carriage return
	//support \b backspace
	//support \d delete \u007f
	//support \f clear
	//support bell \u0007
	
	private void outputToLastLineDirect(String data)
	{
		String lastLine = getLastLine();
		StringBuilder builder = new StringBuilder(lastLine);
		
		if (carriageColumn >= 0)
		{
			lastLine = builder.replace(carriageColumn, carriageColumn + data.length(), data).toString();
			carriageColumn += data.length();
			if (carriageColumn >= lastLine.length())
			{
				carriageColumn = -1;
			}
			setLastLine(lastLine);
			//System.out.println("outputToLastLineDirect:[" + lastLine + "]");
			if (longestRow < lastLine.length())
			{
				longestRow = lastLine.length();
			}
		}
		else
		{
			lastLine = builder.append(data).toString();
			setLastLine(lastLine);
			//System.out.println("outputToLastLineDirect:[" + lastLine + "]");
			if (longestRow < lastLine.length())
			{
				longestRow = lastLine.length();
			}
		}
	}
	
	private String outputSingleChar(String data)
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
			outputToLastLineDirect(data.substring(0, current));
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
	
	private String outputMultiChar(String data)
	{
		String result = outputSingleChar(data);
		if (result == null)
		{
			return data;
		}
		String last = null;
		while (result != null)
		{
			last = result;
			result = outputSingleChar(result);
		}
		return last;
	}
	
	private String outputToLastLine(String data)
	{
		//System.out.println("outputToLastLine:[" + data + "]");
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
		
		//System.out.println("maxWidth:" + maxWidth);
		
		int remainingWidth = (carriageColumn >= 0 ? (maxWidth * 2) - carriageColumn : maxWidth) - lastLine.length();
		
		if (carriageColumn >= 0)
		{
			if (data.length() <= remainingWidth)
			{
				data = outputMultiChar(data);
				
				if (data.length() <= 0)
				{
					return null;
				}
				
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
				data = outputMultiChar(data);
				
				if (data.length() <= 0)
				{
					return null;
				}
				
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
				data = outputMultiChar(data);
				
				if (data.length() <= 0)
				{
					return null;
				}
				
				if (carriageColumn >= 0)
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
					if (data.length() <= 0)
					{
						return null;
					}
					
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
				data = outputMultiChar(data);
				
				if (data.length() <= 0)
				{
					return null;
				}
				
				if (carriageColumn >= 0)
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
		}
		return null;
	}
	
	//break string in multiple lines if needed
	private void outputMultiline(String data)
	{
		//System.out.println("outputMultiline:[" + data + "]");
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
						//setLastLine(builder.deleteCharAt(lastLine.length()).toString());	
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
				//reject
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
		//System.out.println("output:[" + data + "]");
		data = data.replace('\t', ' ');
		
		String[] newlines = data.split("\\n", -1);
		
		//System.out.println("newlines.length:[" + newlines.length + "]");
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
		//System.out.println("addLine:[" + data + "]");
		//System.out.println("getLastLine:[" + getLastLine() + "]");
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
	
	public StringBuilder handleDataInput(StringBuilder inputBuffer, String data, boolean replace)
	{
		//String line = lines.get(caretPosition.getRow());
		int column = hiddenColumn;
		///int size = inputBuffer.length();
		if (replace && column < inputBuffer.length())
		{
			inputBuffer.replace(column, column + data.length(), data);
		}
		else
		{
			inputBuffer.insert(column, data);
		}
		hiddenColumn += data.length();
		if (hiddenColumn > inputBuffer.length())
		{
			hiddenColumn = inputBuffer.length();
		}
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
