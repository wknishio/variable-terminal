package com.googlecode.lanterna.terminal.ansi;

import java.io.IOException;
import java.io.InputStream;

import biz.source_code.utils.RawConsoleInput;

public class RawTerminalInputStream extends InputStream
{
	//public RawTerminalInputStream()
	//{
		//RawConsoleInput input = new RawConsoleInput();
	//}

	public int read() throws IOException
	{
		return RawConsoleInput.read(true);
	}
	
	public int available()
	{
		return 1;
	}
}
