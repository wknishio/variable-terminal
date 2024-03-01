package org.vash.vate.shell.adapter;

import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.vash.vate.VT;
//import org.codehaus.groovy.tools.shell.Groovysh;
//import org.codehaus.groovy.tools.shell.IO;
import org.vash.vate.runtime.VTRuntimeProcess;
import org.vash.vate.stream.filter.VTAutoFlushOutputStream;
import org.vash.vate.stream.pipe.VTPipedInputStream;
import org.vash.vate.stream.pipe.VTPipedOutputStream;

import bsh.Interpreter;

public class VTShellProcessor
{
  public static final int SHELL_TYPE_PROCESS = 0;
  public static final int SHELL_TYPE_BEANSHELL = 1;
  public static final int SHELL_TYPE_GROOVYSH = 2;
  
  private int shellType = SHELL_TYPE_PROCESS;
  // private boolean supressEchoShell = true;
  
  private VTRuntimeProcess shellProcess;
  
  private Thread shellThread;
  
  private InputStream shellInputStream;
  private InputStream shellErrorStream;
  private OutputStream shellOutputStream;
  private Reader shellOutputReader;
  private Reader shellErrorReader;
  private Writer shellCommandExecutor;
  
  private Charset shellCharset;
  
  // private ExecutorService threads;
  
  private Interpreter beanshell;
  // private Groovysh groovyshell;
  
  private List<Closeable> closeables = new ArrayList<Closeable>();
  
  public VTShellProcessor()
  {
    
  }
  
  public VTRuntimeProcess getRuntimeProcess()
  {
    return shellProcess;
  }
  
  public void setRuntimeProcess(VTRuntimeProcess shellProcess)
  {
    this.shellProcess = shellProcess;
  }
  
  public void setShellType(int shellType)
  {
    this.shellType = shellType;
  }
  
  public int getShellType()
  {
    return shellType;
  }
  
  // public void setSuppressEchoShell(boolean supressEchoShell)
  // {
  // this.supressEchoShell = supressEchoShell;
  // }
  
  // public void setThreads(ExecutorService threads)
  // {
  // this.threads = threads;
  // }
  
  public boolean startShell() throws Throwable
  {
    if (shellType == SHELL_TYPE_PROCESS)
    {
      shellProcess.start();
      shellInputStream = shellProcess.getIn();
      shellErrorStream = shellProcess.getErr();
      shellOutputStream = shellProcess.getOut();
      
      closeables.clear();
      closeables.add(shellInputStream);
      closeables.add(shellErrorStream);
      closeables.add(shellOutputStream);
    }
    if (shellType == SHELL_TYPE_GROOVYSH)
    {
//      VTPipedOutputStream pipeOut1 = new VTPipedOutputStream();
//      VTPipedInputStream pipeIn1 = new VTPipedInputStream(pipeOut1);
//      VTPipedOutputStream pipeOut2 = new VTPipedOutputStream();
//      VTPipedInputStream pipeIn2 = new VTPipedInputStream(pipeOut2);
//      
//      shellInputStream = pipeIn1;
//      shellErrorStream = pipeIn1;
//      shellOutputStream = new VTAutoFlushOutputStream(pipeOut2);
//      final InputStream in = pipeIn2;
//      final OutputStream out = new VTAutoFlushOutputStream(pipeOut1);
//      
//      closeables.clear();
//      closeables.add(out);
//      closeables.add(shellOutputStream);
//      
//      IO io = new IO(in, out, out);
//      groovyshell = new Groovysh(io);
//      groovyshell.setErrorHook(new Closure<Object>(this)
//      {
//        public Object call(Object... args)
//        {
//          if (args[0] instanceof Throwable)
//          {
//            Throwable t = (Throwable) args[0];
//            t.printStackTrace();
//          }
//          return groovyshell.getDefaultErrorHook().call(args);
//        }
//      });
//      shellThread = new Thread()
//      {
//        public void run()
//        {
//          try
//          {
//            groovyshell.run("");
//          }
//          catch (Throwable t)
//          {
//            //t.printStackTrace();
//          }
//        }
//      };
//      shellThread.start();
    }
    if (shellType == SHELL_TYPE_BEANSHELL)
    {
      VTPipedOutputStream pipeOut1 = new VTPipedOutputStream();
      VTPipedInputStream pipeIn1 = new VTPipedInputStream(pipeOut1, VT.VT_REDUCED_BUFFER_SIZE_BYTES);
      VTPipedOutputStream pipeOut2 = new VTPipedOutputStream();
      VTPipedInputStream pipeIn2 = new VTPipedInputStream(pipeOut2, VT.VT_REDUCED_BUFFER_SIZE_BYTES);
      
      shellInputStream = pipeIn1;
      shellErrorStream = pipeIn1;
      shellOutputStream = new VTAutoFlushOutputStream(pipeOut2);
      final InputStreamReader in = new InputStreamReader(pipeIn2);
      final PrintStream out = new PrintStream(new VTAutoFlushOutputStream(pipeOut1));
      
      closeables.clear();
      closeables.add(out);
      closeables.add(shellOutputStream);
      
      beanshell = new Interpreter(in, out, out, true);
      beanshell.setExitOnEOF(false);
      // beanshell.set( "bsh.args", new String[] {});
      Interpreter.setShutdownOnExit(false);
      
      shellThread = new Thread()
      {
        public void run()
        {
          try
          {
            beanshell.run();
          }
          catch (Throwable t)
          {
            
          }
        }
      };
      shellThread.start();
    }
    
    if (shellCharset != null)
    {
      shellCommandExecutor = new OutputStreamWriter(shellOutputStream, shellCharset);
      shellOutputReader = new InputStreamReader(shellInputStream, shellCharset);
      shellErrorReader = new InputStreamReader(shellErrorStream, shellCharset);
      return true;
    }
    else
    {
      shellCommandExecutor = new OutputStreamWriter(shellOutputStream);
      shellOutputReader = new InputStreamReader(shellInputStream);
      shellErrorReader = new InputStreamReader(shellErrorStream);
      return true;
    }
  }
  
  @SuppressWarnings("deprecation")
  public void stopShell()
  {
    // System.out.println("stopShell(started)");
    if (shellProcess.isAlive())
    {
      try
      {
        shellProcess.destroy();
      }
      catch (Throwable e)
      {
        
      }
      
      try
      {
        shellProcess.waitFor();
      }
      catch (Throwable e)
      {
        
      }
    }
    
//    if (groovyshell != null)
//    {
//      try
//      {
//        groovyshell.getRunner().setRunning(false);
//      }
//      catch (Throwable e)
//      {
//        
//      }
//    }
    
    if (beanshell != null)
    {
      
    }
    
    if (closeables != null && closeables.size() > 0)
    {
      for (Closeable closeable : closeables.toArray(new Closeable[]{ }))
      {
        try
        {
          closeable.close();
        }
        catch (Throwable t)
        {
          
        }
      }
      closeables.clear();
    }
    
    if (shellType != SHELL_TYPE_PROCESS)
    {
//      try
//      {
//        if (shellThread.isAlive())
//        {
//          shellThread.stop();
//        }
//      }
//      catch (Throwable e)
//      {
//        
//      }
    }
    // System.out.println("stopShell(finished)");
  }
  
  public int waitFor() throws InterruptedException
  {
    int code = 0;
    if (shellType == SHELL_TYPE_PROCESS && shellProcess != null && shellProcess.isAlive())
    {
      code = shellProcess.waitFor();
    }
    if (shellType == SHELL_TYPE_BEANSHELL && shellThread != null && shellThread.isAlive())
    {
      shellThread.join();
    }
    if (shellType == SHELL_TYPE_GROOVYSH && shellThread != null && shellThread.isAlive())
    {
      shellThread.join();
    }
    return code;
  }
  
  public InputStream getShellInputStream()
  {
    return shellInputStream;
  }
  
  public InputStream getShellErrorStream()
  {
    return shellErrorStream;
  }
  
  public OutputStream getShellOutputStream()
  {
    return shellOutputStream;
  }
  
  public Reader getShellOutputReader()
  {
    return shellOutputReader;
  }
  
  public Reader getShellErrorReader()
  {
    return shellErrorReader;
  }
  
  public Writer getShellCommandExecutor()
  {
    return shellCommandExecutor;
  }
  
  public boolean setShellEncoding(String shellEncoding)
  {
    if (shellEncoding == null)
    {
      shellCharset = null;
      return true;
    }
    try
    {
      shellCharset = Charset.forName(shellEncoding);
      return true;
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  public String getShellEncoding()
  {
    if (shellCharset != null)
    {
      return shellCharset.name();
    }
    return null;
  }
}
