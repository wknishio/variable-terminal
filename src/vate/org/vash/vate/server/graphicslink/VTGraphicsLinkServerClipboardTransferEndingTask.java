package org.vash.vate.server.graphicslink;

public class VTGraphicsLinkServerClipboardTransferEndingTask implements Runnable
{
  private VTGraphicsLinkServerWriter writer;
  
  public VTGraphicsLinkServerClipboardTransferEndingTask(VTGraphicsLinkServerWriter writer)
  {
    this.writer = writer;
  }
  
  public void run()
  {
    writer.finishClipboardContentsTransfer();
  }
}