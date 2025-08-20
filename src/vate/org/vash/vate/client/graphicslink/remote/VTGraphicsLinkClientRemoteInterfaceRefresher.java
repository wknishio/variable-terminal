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
    writer.synchronizeScreenCaptureArea();
    writer.synchronizeColorQuality();
    writer.synchronizeImageCoding();
    // writer.synchronizeImageCoding();
    writer.requestRefresh();
  }
}