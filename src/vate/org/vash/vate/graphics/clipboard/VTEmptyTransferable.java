package org.vash.vate.graphics.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class VTEmptyTransferable implements Transferable
{
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
  {
    throw new UnsupportedFlavorException(flavor);
  }
  
  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[0];
  }
  
  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    return false;
  }
}