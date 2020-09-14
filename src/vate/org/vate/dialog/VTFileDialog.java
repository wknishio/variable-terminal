package org.vate.dialog;

import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;

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
			setIconImage.invoke(this, ImageIO.read(this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/remote.png")));
			//this.setIconImage(ImageIO.read(this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/remote.png")));
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
			setIconImage.invoke(this, ImageIO.read(this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/remote.png")));
			//this.setIconImage(ImageIO.read(this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/remote.png")));
		}
		catch (Throwable e)
		{
			
		}
	}
}