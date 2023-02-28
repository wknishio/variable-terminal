package org.vash.vate.client.graphicsmode.remote;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientRemoteInterfaceRefresher implements Runnable
{
  private VTGraphicsModeClientWriter writer;
  
  public VTGraphicsModeClientRemoteInterfaceRefresher(VTGraphicsModeClientWriter writer)
  {
    this.writer = writer;
  }
  
  public void run()
  {
    /*
     * synchronized (this) { while (!writer.isRemoteInterfaceLoaded()) { try {
     * wait(); } catch (InterruptedException e) { } } }
     */
    /*
     * if (!writer.isScreenCaptureModeComplete()) {
     * writer.setScreenCaptureArea(writer.calculateScreenCaptureArea());
     * writer.synchronizeScreenCaptureArea(); }
     */
    writer.setScreenCaptureArea(writer.calculateScreenCaptureArea());
    writer.synchronizeScreenCaptureArea();
    writer.synchronizeColorQuality();
    writer.synchronizeImageCoding();
    // writer.synchronizeImageCoding();
    writer.requestRefresh();
  }
}