package org.vash.vate.client.graphicslink.remote;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientRemoteInterfaceLockingKeySynchronizer implements Runnable
{
  // private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientRemoteInterfaceLockingKeySynchronizer(VTGraphicsLinkClientWriter writer)
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