package org.vate.console.lanterna;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
public class VTLanternaOutputTextBox extends TextBox
{
	private volatile int maximumlines = 0;
	private int hiddenColumn = 0;
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
	
	public void setHiddenColumn(int column)
	{
		this.hiddenColumn = column;
	}
		
	public int getHiddenColumn()
	{
		return hiddenColumn;
	}
		
	public String getLastLine()
	{
		return lines.get(getLineCount() - 1);
	}
	
	public void setLastLine(String line)
	{
		lines.set(getLineCount() - 1, line);
	}
	
	//TODO:must support \n line feed
	//TODO:must support \r carriage return
	//TODO:must support \b backspace
	//TODO:must support \d delete 127
	//TODO:must support bell
	//TODO:must support clear
	
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
			String lastLine = getLastLine();
			lastLine += data;
			setLastLine(lastLine);
		}
		else
		{
			addLine("");
		}
		invalidate();
		if (getLineCount() > 0)
		{
			setCaretPosition(getLineCount(), 0);
		}
	}
	
	public synchronized void output(char[] data, int off, int len)
	{
		String stringData = new String(data, off, len);
		stringData = stringData.replace('\t', ' ');
		//System.out.println("data:[" + data + "]");
		String lastLine = getLastLine();
		
		boolean addline = stringData.indexOf('\n') >= 0;
		
		String[] newlines = stringData.split("\n");
		
		if (newlines.length > 0)
		{
			lastLine += newlines[0];
			setLastLine(lastLine);
			if (newlines.length > 1)
			{
				for (int i = 1; i < newlines.length; i++)
				{
					addLine(newlines[i].replaceAll("\\p{Cntrl}", ""));
				}
			}
			else if (addline)
			{
				addLine("");
			}
			invalidate();
			if (getLineCount() > 0)
			{
				setCaretPosition(getLineCount(), 0);
			}
		}
		else if (addline)
		{
			addLine("");
		}
	}

	public synchronized void output(String data)
	{
		data = data.replace('\t', ' ');
		//System.out.println("data:[" + data + "]");
		String lastLine = getLastLine();
		
		boolean addline = data.indexOf('\n') >= 0;
		
		String[] newlines = data.split("\n");
		
		if (newlines.length > 0)
		{
			lastLine += newlines[0];
			setLastLine(lastLine);
			if (newlines.length > 1)
			{
				for (int i = 1; i < newlines.length; i++)
				{
					addLine(newlines[i].replaceAll("\\p{Cntrl}", ""));
				}
			}
			else if (addline)
			{
				addLine("");
			}
			invalidate();
			if (getLineCount() > 0)
			{
				setCaretPosition(getLineCount(), 0);
			}
		}
		else if (addline)
		{
			addLine("");
		}
	}
	
	public synchronized TextBox addLine(String data)
	{
		if (getLineCount() > 0 && maximumlines > 0 && getLineCount() == maximumlines)
		{
			lines.remove(0);
		}
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
		if (getMaxLineLength() == -1 || getMaxLineLength() > inputBuffer.length() + 1)
		{
			if (replace && column < inputBuffer.length())
			{
				inputBuffer.setCharAt(column, data);
			}
			else
			{
				inputBuffer.insert(column, data);
			}
			hiddenColumn++;
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
