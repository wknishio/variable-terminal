package org.vash.vate.server.graphicsmode;

public class VTGraphicsModeServerClipboardTransferEndingTask implements Runnable
{
  private VTGraphicsModeServerWriter writer;
  
  public VTGraphicsModeServerClipboardTransferEndingTask(VTGraphicsModeServerWriter writer)
  {
    this.writer = writer;
  }
  
  public void run()
  {
    writer.finishClipboardContentsTransfer();
  }
}