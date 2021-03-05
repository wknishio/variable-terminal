package java.net;

import java.io.IOException;

public class VTSocket extends Socket
{

  public VTSocket() throws IOException
  {
    super();
  }
  
  private SocketImpl getSocketImpl() throws SocketException
  {
    return getImpl();
  }

  public void setSocketOption(int option, Object value) throws SocketException
  {
    getSocketImpl().setOption(option, value);
  }
  
}
