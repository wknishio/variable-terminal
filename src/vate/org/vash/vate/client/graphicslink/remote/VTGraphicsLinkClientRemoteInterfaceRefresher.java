package org.vash.vate.client.graphicslink.remote;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientRemoteInterfaceRefresher implements Runnable
{
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientRemoteInterfaceRefresher(VTGraphicsLinkClientWriter writer)
  {
    this.writer = writer;
  }
  
  public void run()
  {
    writer.setScreenCaptureArea(writer.calculateScreenCaptureArea());
    writer.sendScreenCaptureArea();
    writer.sendColorQuality();
    writer.sendImageCoding();
    // writer.synchronizeImageCoding();
    writer.requestRefresh();
  }
}