package java.net;

import java.io.IOException;

public class VTServerSocket extends ServerSocket
{
  
  public VTServerSocket() throws IOException
  {
    super();
  }
  
  private SocketImpl getSocketImpl() throws SocketException
  {
    return getImpl();
  }
  
  public void accept(Socket s) throws IOException
  {
    implAccept(s);
  }
  
  public void setSocketOption(int option, Object value) throws SocketException
  {
    getSocketImpl().setOption(option, value);
  }
  
}
