package org.vash.vate.client.graphicsmode;

public class VTGraphicsModeClientClipboardTransferEndingTask implements Runnable
{
  private VTGraphicsModeClientWriter writer;
  
  public VTGraphicsModeClientClipboardTransferEndingTask(VTGraphicsModeClientWriter writer)
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
