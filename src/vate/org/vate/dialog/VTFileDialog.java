package org.vate.dialog;

import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.lang.reflect.Method;

import org.vate.VT;

public class VTFileDialog extends FileDialog
{
  private static final long serialVersionUID = 1L;
  // private Runnable application;
  // private Frame owner;

  public VTFileDialog(Dialog owner, String title, int mode)
  {
    super(owner, title, mode);
    // this.owner = owner;
    // this.application = application;
    try
    {
      Method setIconImage = this.getClass().getMethod("setIconImage", Class.forName("java.awt.Image"));
      // InputStream stream =
      // this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/remote.png");
      setIconImage.invoke(this, VT.remoteIcon);
      // stream.close();
      // this.setIconImage(ImageIO.read(this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/remote.png")));
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
      Method setIconImage = this.getClass().getMethod("setIconImage", Class.forName("java.awt.Image"));
      // InputStream stream =
      // this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/remote.png");
      setIconImage.invoke(this, VT.remoteIcon);
      // stream.close();
      // this.setIconImage(ImageIO.read(this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/remote.png")));
    }
    catch (Throwable e)
    {

    }
  }
}