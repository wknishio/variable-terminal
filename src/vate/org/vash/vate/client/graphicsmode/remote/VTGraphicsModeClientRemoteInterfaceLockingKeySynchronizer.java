package org.vash.vate.client.graphicsmode.remote;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientRemoteInterfaceLockingKeySynchronizer implements Runnable
{
  // private VTGraphicsModeClientWriter writer;
  
  public VTGraphicsModeClientRemoteInterfaceLockingKeySynchronizer(VTGraphicsModeClientWriter writer)
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