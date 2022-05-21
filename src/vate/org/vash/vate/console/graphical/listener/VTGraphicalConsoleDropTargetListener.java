package org.vash.vate.console.graphical.listener;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.List;
import org.vash.vate.console.VTConsole;

public class VTGraphicalConsoleDropTargetListener implements DropTargetListener
{
  public void dragEnter(DropTargetDragEvent dtde)
  {
    // System.out.println("dragEnter!");
  }

  public void dragExit(DropTargetEvent dte)
  {
    // System.out.println("dragExit!");
  }

  public void dragOver(DropTargetDragEvent dtde)
  {
    // System.out.println("dragOver!");
  }

  @SuppressWarnings("unchecked")
  public void drop(DropTargetDropEvent dtde)
  {
    try
    {
      Transferable transferable = dtde.getTransferable();
      if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
      {
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
        if (files.size() > 0)
        {
          StringBuilder fileList = new StringBuilder();
          for (File file : files)
          {
            fileList.append(file.getAbsolutePath() + ";");
          }
          fileList.deleteCharAt(fileList.length() - 1);
          VTConsole.input(fileList.toString());
        }
        dtde.dropComplete(true);
      }
      else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
      {
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        String data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
        VTConsole.input(data);
        dtde.dropComplete(true);
      }
      else
      {
        dtde.rejectDrop();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      // dtde.
    }
  }

  public void dropActionChanged(DropTargetDragEvent dtde)
  {
    // dtde.
    // System.out.println("dropActionChanged!");
  }
}
