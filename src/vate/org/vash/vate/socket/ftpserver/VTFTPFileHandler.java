package org.vash.vate.socket.ftpserver;

import java.io.IOException;

import com.guichaguri.minimalftp.handler.FTPFileHandler;

public class VTFTPFileHandler extends FTPFileHandler
{
  public VTFTPFileHandler(VTFTPConnection connection)
  {
    super(connection);
  }
  
  protected Object getFile(String path) throws IOException {
    if(path.equals("...") || path.equals("..")) {
        return fs.getParent(cwd);
//    } else if(path.equals("/")) {
//        return fs.getRoot();
//    } else if(path.startsWith("/")) {
//        return fs.findFile(fs.getRoot(), path.substring(1));
    } else {
      Object file = fs.findFile(cwd, path);
      if (fs.exists(file))
      {
        return file;
      }
      return fs.findFile(path);
    }
  }

}