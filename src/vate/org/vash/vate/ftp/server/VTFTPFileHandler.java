package org.vash.vate.ftp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.guichaguri.minimalftp.api.ResponseException;
import com.guichaguri.minimalftp.handler.FTPFileHandler;

public class VTFTPFileHandler extends FTPFileHandler
{
  protected VTFTPConnection vtcon;
  
  public VTFTPFileHandler(VTFTPConnection connection)
  {
    super(connection);
    this.vtcon = connection;
  }
  
  protected Object getFile(String path) throws IOException
  {
    //System.out.println("getFile(" + path + ")");
    if (path.equals("...") || path.equals(".."))
    {
      return fs.getParent(cwd);
    }
    else if (path.equals("/"))
    {
      return fs.getRoot();
    }
    else if (path.equals("."))
    {
      return fs.findFile(fs.getPath(cwd));
    }
    else if (path.startsWith("/"))
    {
      return fs.findFile(path.substring(1));
    }
    else if (path.startsWith("./"))
    {
      return fs.findFile(cwd, path.substring(2));
    }
    else
    {
      return fs.findFile(cwd, path);
    }
  }
  
  protected void sendStream(final InputStream in)
  {
    Runnable thread = (new Runnable()
    {
      public void run()
      {
        try
        {
          con.sendData(in);
          con.sendResponse(226, "File sent!");
        }
        catch(ResponseException ex)
        {
          con.sendResponse(ex.getCode(), ex.getMessage());
        }
        catch(Exception ex)
        {
          con.sendResponse(451, ex.getMessage());
        }
      }
    });
    vtcon.executorService.execute(thread);
  }
  
  protected void receiveStream(final OutputStream out)
  {
    Runnable thread = (new Runnable()
    {
      public void run()
      {
        try
        {
          con.receiveData(out);
          con.sendResponse(226, "File received!");
        }
        catch(ResponseException ex)
        {
          con.sendResponse(ex.getCode(), ex.getMessage());
        }
        catch(Exception ex)
        {
          con.sendResponse(451, ex.getMessage());
        }
      }
    });
    vtcon.executorService.execute(thread);
  }
}