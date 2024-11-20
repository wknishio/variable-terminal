package org.vash.vate.dialog;

import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.lang.reflect.Method;

import org.vash.vate.VT;

public class VTFileDialog extends FileDialog
{
  private static final long serialVersionUID = 1L;
  // private Runnable application;
  // private Frame owner;
  private static Method setIconImage;
  
  static
  {
    try
    {
      setIconImage = Dialog.class.getMethod("setIconImage", Class.forName("java.awt.Image"));
      // setIconImage.setAccessible(true);
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public VTFileDialog(Dialog owner, String title, int mode)
  {
    super(owner, title, mode);
    // this.owner = owner;
    // this.application = application;
    try
    {
      if (setIconImage != null)
      {
        setIconImage.invoke(this, VT.remoteIcon);
      }
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public VTFileDialog(Frame owner, String title, int mode)
  {
    super(owner, title, mode);
    // this.owner = owner;
    // this.application = application;
    try
    {
      if (setIconImage != null)
      {
        setIconImage.invoke(this, VT.remoteIcon);
      }
    }
    catch (Throwable e)
    {
      
    }
  }
}