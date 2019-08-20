package org.vate.client.console.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.vate.client.connection.VTClientConnection;
import org.vate.client.session.VTClientSession;
import org.vate.console.VTConsole;
import org.vate.task.VTTask;

import com.martiansoftware.jsap.CommandLineTokenizer;

public class VTClientRemoteConsoleWriter extends VTTask
{
	private VTClientSession session;
	private VTClientConnection connection;
	private VTClientRemoteConsoleCommandSelector<VTClientRemoteConsoleCommandProcessor> selector;
	
	public VTClientRemoteConsoleWriter(VTClientSession session)
	{
		this.session = session;
		this.connection = session.getConnection();
		this.selector = new VTClientRemoteConsoleCommandSelector<VTClientRemoteConsoleCommandProcessor>(session);
	}
	
	public void run()
	{
		// int p = 0;
		if (!stopped)
		{
			String commands = session.getClient().getClientConnector().getSessionCommands().trim();
			commands = commands.replace("*;", "\n");
			BufferedReader sessionCommandReader = new BufferedReader(new StringReader(commands));
			String line = null;
			// String command = null;
			try
			{
				while (!stopped && (line = sessionCommandReader.readLine()) != null)
				{
					try
					{
						executeCommand(line, null);
					}
					catch (Throwable t)
					{
						stopped = true;
						break;
					}
				}
			}
			catch (Throwable t)
			{
				// stopped = true;
			}
		}
		while (!stopped)
		{
			String[] lines;
			try
			{
				String line = VTConsole.readLine(true);
				if (line.contains("*;"))
				{
					lines = line.split("\\*;");
					for (String subLine : lines)
					{
						executeCommand(subLine, null);
					}
				}
				else
				{
					executeCommand(line, null);
				}
			}
			catch (InterruptedException e)
			{
				// e.printStackTrace();
				stopped = true;
				break;
			}
			catch (Throwable e)
			{
				// e.printStackTrace();
				stopped = true;
				break;
			}
		}
		synchronized (session)
		{
			session.notify();
		}
	}
	
	@SuppressWarnings("unused")
	private void executeStringScript(String script, Set<String> stack)
	{
		if (stack == null)
		{
			stack = new HashSet<String>();
		}
		if (script == null || script.length() < 1 || !stack.add(script))
		{
			// protection for recursion and bad script string
			return;
		}
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new StringReader(script));
			String line = "";
			while (!stopped && (line = reader.readLine()) != null)
			{
				String[] commands = line.split("\\*;");
				for (String command : commands)
				{
					executeCommand(command, stack);
				}
			}
		}
		catch (Throwable t)
		{
			
		}
		finally
		{
			if (script != null)
			{
				stack.remove(script);
			}
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Throwable t)
				{
					
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void executeFileScript(File script, Set<String> stack)
	{
		if (stack == null)
		{
			stack = new HashSet<String>();
		}
		if (script == null || !script.exists() || !stack.add(script.getAbsolutePath()))
		{
			// protection for recursion and bad file paths
			return;
		}
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(script)));
			String line = "";
			while (!stopped && (line = reader.readLine()) != null)
			{
				String[] commands = line.split("\\*;");
				for (String command : commands)
				{
					executeCommand(command, stack);
				}
			}
		}
		catch (Throwable t)
		{
			
		}
		finally
		{
			if (script != null)
			{
				stack.remove(script.getAbsolutePath());
			}
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Throwable t)
				{
					
				}
			}
		}
	}
	
	private void executeCommand(String command, Set<String> stack) throws Throwable
	{
		String parsed[];
		if (session.getClient().getClientConnector().isSkipConfiguration())
		{
			stopped = true;
			return;
		}
		else if (command != null)
		{
			if (!(command.length() == 0))
			{
				parsed = CommandLineTokenizer.tokenize(command);
				if (parsed.length < 1)
				{
					parsed = new String[] { command };
					// p = 0;
					/* for (String part : splitCommand) { splitCommand[p++] =
					 * StringEscapeUtils.unescapeJava(part); } */
				}
			}
			else
			{
				parsed = new String[] { "" };
			}
			if (!selector.selectCommand(command, parsed))
			{
				connection.getCommandWriter().write(command + "\n");
				connection.getCommandWriter().flush();
			}
		}
		else
		{
			System.exit(0);
		}
	}
}