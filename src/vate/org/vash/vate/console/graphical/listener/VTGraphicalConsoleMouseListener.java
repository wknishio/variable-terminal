package org.vash.vate.console.graphical.listener;

// import java.awt.Toolkit;
// import java.awt.datatransfer.Clipboard;
// import java.awt.datatransfer.DataFlavor;
// import java.awt.datatransfer.StringSelection;
// import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
// import java.io.IOException;
import org.vash.vate.console.graphical.menu.VTGraphicalConsolePopupMenu;

public class VTGraphicalConsoleMouseListener implements MouseListener
{
  // private Clipboard systemClipboard;
  private VTGraphicalConsolePopupMenu popupMenu;
  
  public VTGraphicalConsoleMouseListener(VTGraphicalConsolePopupMenu popupMenu)
  {
    // this.systemClipboard =
    // Toolkit.getDefaultToolkit().getSystemClipboard();
    this.popupMenu = popupMenu;
  }
  
  public void mouseClicked(MouseEvent e)
  {
    /*
     * if (e.isPopupTrigger()) { }
     */
    // VTGraphicalConsole.updateCaretPosition();
    e.consume();
  }
  
  public void mouseEntered(MouseEvent e)
  {
    e.consume();
  }
  
  public void mouseExited(MouseEvent e)
  {
    e.consume();
  }
  
  public void mousePressed(MouseEvent e)
  {
    if (e.isPopupTrigger())
    {
      popupMenu.show(e.getComponent(), e.getX(), e.getY());
      /*
       * String selectedText = VTGraphicalConsole.getSelectedText(); if
       * (selectedText != null) { StringSelection text = new
       * StringSelection(selectedText); systemClipboard.setContents(text, null);
       * //VTGraphicalConsole.unselectAllText();
       * VTGraphicalConsole.updateCaretPosition(); if
       * (VTGraphicalConsole.isFlushInterrupted()) {
       * VTGraphicalConsole.clearCaretPosition(); } } else { try { if
       * (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
       * String text =
       * systemClipboard.getData(DataFlavor.stringFlavor).toString();
       * VTGraphicalConsole.input(text); } } catch (UnsupportedFlavorException
       * e1) { } catch (IOException e1) { } }
       */
      e.consume();
    }
//		else
//		{
//			if (!VTGraphicalConsole.isFlushInterrupted())
//			{
//				if (VTConsole.getSelectedText() != null)
//				{
//					
//				}
//				else
//				{
//					if (!VTGraphicalConsole.isCaretPositionUpdated())
//					{
//						// VTGraphicalConsole.updateCaretPosition();
//					}
//				}
//			}
//			else
//			{
//				if (VTConsole.getSelectedText() != null)
//				{
//					
//				}
//				else
//				{
//					// VTGraphicalConsole.refreshCaretPosition();
//				}
//			}
//			/* if (e.getClickCount() > 1) { e.consume(); } */
//		}
  }
  
  public void mouseReleased(MouseEvent e)
  {
    if (e.isPopupTrigger())
    {
      popupMenu.show(e.getComponent(), e.getX(), e.getY());
      // System.out.println(e.getComponent().getName());
      /*
       * String selectedText = VTGraphicalConsole.getSelectedText(); if
       * (selectedText != null) { StringSelection text = new
       * StringSelection(selectedText); systemClipboard.setContents(text, null);
       * //VTGraphicalConsole.unselectAllText();
       * VTGraphicalConsole.updateCaretPosition(); if
       * (VTGraphicalConsole.isFlushInterrupted()) {
       * VTGraphicalConsole.clearCaretPosition(); } } else { try { if
       * (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
       * String text =
       * systemClipboard.getData(DataFlavor.stringFlavor).toString();
       * VTGraphicalConsole.input(text); } } catch (UnsupportedFlavorException
       * e1) { } catch (IOException e1) { } }
       */
      e.consume();
    }
//		else
//		{
//			if (!VTGraphicalConsole.isFlushInterrupted())
//			{
//				if (VTConsole.getSelectedText() != null)
//				{
//					
//				}
//				else
//				{
//					if (!VTGraphicalConsole.isCaretPositionUpdated())
//					{
//						// VTGraphicalConsole.updateCaretPosition();
//					}
//				}
//			}
//			else
//			{
//				if (VTConsole.getSelectedText() != null)
//				{
//					
//				}
//				else
//				{
//					// VTGraphicalConsole.refreshCaretPosition();
//				}
//			}
//			/* if (e.getClickCount() > 1) { e.consume(); } */
//		}
  }
}