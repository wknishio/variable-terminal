package org.vash.vate.client.graphicslink.remote;

public class VTGraphicsLinkClientRemoteInterfaceLockingKeySynchronizer implements Runnable
{
  // private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientRemoteInterfaceLockingKeySynchronizer()
  {
    // this.writer = writer;
  }
  
  public void run()
  {
    try
    {
      // writer.synchronizeAllRemoteLockingKeys();
    }
    finally
    {
      // writer.finishDelayedUpdateInAllRemoteLockingKeys();
    }
  }
}