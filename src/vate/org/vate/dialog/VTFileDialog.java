package org.vate.dialog;

import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;

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
			this.setIconImage(ImageIO.read(this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/terminal.png")));
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
			this.setIconImage(ImageIO.read(this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/terminal.png")));
		}
		catch (Throwable e)
		{
			
		}
	}
}