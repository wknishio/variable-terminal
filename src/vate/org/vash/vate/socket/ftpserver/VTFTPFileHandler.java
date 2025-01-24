package org.vash.vate.socket.ftpserver;

import com.guichaguri.minimalftp.handler.FTPFileHandler;

public class VTFTPFileHandler extends FTPFileHandler
{
  public VTFTPFileHandler(VTFTPConnection connection)
  {
    super(connection);
  }
}