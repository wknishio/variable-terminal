package org.vash.vate.client.graphicslink;

public class VTGraphicsLinkClientClipboardTransferEndingTask implements Runnable
{
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientClipboardTransferEndingTask(VTGraphicsLinkClientWriter writer)
  {
    this.writer = writer;
  }
  
  public void run()
  {
    try
    {
      writer.finishClipboardContentsTransfer();
    }
    catch (Throwable e)
    {
      
    }
  }
}
