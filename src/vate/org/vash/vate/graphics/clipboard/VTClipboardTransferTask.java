package org.vash.vate.graphics.clipboard;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.vash.vate.VT;
import org.vash.vate.graphics.image.VTTransferableImage;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.task.VTTask;

public class VTClipboardTransferTask extends VTTask
{
  private volatile boolean sending;
  // private VTImageIO vtImageIO;
  private VTLittleEndianInputStream in;
  private VTLittleEndianOutputStream out;
  // private Reader reader;
  // private Writer writer;
  private Runnable endingTask;
  private Clipboard systemClipboard;
  private Transferable transferable;
  private DataBuffer recyclableDataBuffer;
  
  public VTClipboardTransferTask()
  {
    if (GraphicsEnvironment.isHeadless())
    {
      return;
    }
    // this.vtImageIO = new VTImageIO();
    try
    {
      this.systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public boolean isSending()
  {
    return sending;
  }
  
  public void setSending(boolean sending)
  {
    this.sending = sending;
  }
  
  public void setEndingTask(Runnable endingTask)
  {
    this.endingTask = endingTask;
  }
  
  public boolean checkTransfer()
  {
    if (systemClipboard == null)
    {
      return false;
    }
    if (sending)
    {
      try
      {
        transferable = systemClipboard.getContents(null);
      }
      catch (Throwable e)
      {
        return false;
      }
    }
    return true;
  }
  
  /*
   * public void interrupt() throws IOException { in.close(); out.close(); }
   */
  
  public void setInputStream(InputStream in)
  {
    this.in = new VTLittleEndianInputStream(in);
    /*
     * try { this.reader = new InputStreamReader(in, "UTF-8"); } catch
     * (UnsupportedEncodingException e) { }
     */
  }
  
  public void setOutputStream(OutputStream out)
  {
    this.out = new VTLittleEndianOutputStream(out);
    /*
     * try { this.writer = new OutputStreamWriter(out, "UTF-8"); } catch
     * (UnsupportedEncodingException e) { }
     */
  }
  
  @SuppressWarnings("unchecked")
  public void run()
  {
    try
    {
      try
      {
        if (checkTransfer())
        {
          out.write(VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_STARTABLE);
          out.flush();
        }
        else
        {
          out.write(VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_UNSTARTABLE);
          out.flush();
          in.read();
          return;
        }
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        transferable = null;
        /* if (endingTask != null) { endingTask.run(); } */
        return;
      }
      if (in.read() == VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_STARTABLE)
      {
        if (sending)
        {
          try
          {
            if (transferable == null)
            {
              out.write(VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_EMPTY);
              out.flush();
            }
            else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
            {
              out.write(VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_TEXT);
              byte[] data = ((String) transferable.getTransferData(DataFlavor.stringFlavor)).getBytes("UTF-8");
              out.writeInt(data.length);
              out.write(data);
              out.flush();
              //System.out.println("sent text:" + new String(data, "UTF-8"));
              // writer.write(text);
              // writer.flush();
            }
            else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor))
            {
              out.write(VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_IMAGE);
              VTTransferableImage image = new VTTransferableImage((Image) transferable.getTransferData(DataFlavor.imageFlavor), recyclableDataBuffer);
              recyclableDataBuffer = image.getRecyclableDataBuffer();
              image.write(out);
              out.flush();
              image.flush();
            }
            else if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            {
              List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
              StringBuilder fileList = new StringBuilder();
              if (files.size() > 0)
              {
                for (File file : files)
                {
                  fileList.append(file.getAbsolutePath() + ";");
                }
                fileList.deleteCharAt(fileList.length() - 1);
              }
              byte[] data = fileList.toString().getBytes("UTF-8");
              out.write(VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_TEXT);
              out.writeInt(data.length);
              out.write(data);
              out.flush();
            }
            else
            {
              out.write(VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_UNSUPPORTED);
              out.flush();
            }
          }
          catch (Throwable e)
          {
            // e.printStackTrace();
            try
            {
              out.write(VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_UNSUPPORTED);
              out.flush();
            }
            catch (Throwable e1)
            {
              // e.printStackTrace();
            }
          }
        }
        else
        {
          try
          {
            int type = in.read();
            if (type == VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_EMPTY)
            {
              systemClipboard.setContents(new VTEmptyTransferable(), null);
            }
            else if (type == VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_TEXT)
            {
              int length = in.readInt();
              byte[] data = new byte[length];
              in.readFully(data);
              //System.out.println("received text:" + new String(data, "UTF-8"));
              systemClipboard.setContents(new StringSelection(new String(data, "UTF-8")), null);
            }
            else if (type == VT.VT_GRAPHICS_MODE_CLIPBOARD_TRANSFER_TYPE_IMAGE)
            {
              VTTransferableImage image = new VTTransferableImage(recyclableDataBuffer);
              recyclableDataBuffer = image.getRecyclableDataBuffer();
              image.read(in);
              systemClipboard.setContents(image, image);
            }
            else
            {
              
            }
          }
          catch (Throwable e)
          {
            // e.printStackTrace();
          }
        }
      }
      transferable = null;
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    if (endingTask != null)
    {
      endingTask.run();
    }
  }
}