package org.vate.client.graphicsmode;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.imageio.ImageIO;

public class VTGraphicsModeClientWriterFrame extends Frame
{
	private static final long serialVersionUID = 1L;
	private int lastWidth = -1;
	private int lastHeight = -1;
	
	private class VTGraphicsModeClientWriterFrameComponentListener implements ComponentListener
	{
		public void componentHidden(ComponentEvent e)
		{
			
		}
		
		public void componentMoved(ComponentEvent e)
		{
			
		}
		
		public void componentResized(ComponentEvent e)
		{
			int width = getWidth();
			int height = getHeight();
			if (width != lastWidth || height != lastHeight)
			{
				revalidate();
			}
			else
			{
				
			}
			lastWidth = width;
			lastHeight = height;
			// System.out.println("componentResized");
		}
		
		public void componentShown(ComponentEvent e)
		{
			revalidate();
		}
	}
	
	public VTGraphicsModeClientWriterFrame(GraphicsConfiguration configuration)
	{
		super(configuration);
		this.setBackground(new Color(0x00555555));
		try
		{
			this.setIconImage(ImageIO.read(this.getClass().getResourceAsStream("/org/vate/client/graphicsmode/resource/desktop.png")));
		}
		catch (Throwable t)
		{
			
		}
		this.addComponentListener(new VTGraphicsModeClientWriterFrameComponentListener());
	}
	
	public VTGraphicsModeClientWriterFrame()
	{
		super();
		this.setBackground(new Color(0x00555555));
		try
		{
			this.setIconImage(ImageIO.read(this.getClass().getResourceAsStream("/org/vate/client/graphicsmode/resource/desktop.png")));
		}
		catch (Throwable t)
		{
			
		}
		this.addComponentListener(new VTGraphicsModeClientWriterFrameComponentListener());
	}
	
	public void pack()
	{
		super.pack();
		super.validate();
	}
	
	public void invalidate()
	{
		
	}
	
	public void validate()
	{
		
	}
	
	/* public void validate() { int width = this.getWidth(); int height =
	 * this.getHeight(); if (width != lastWidth || height != lastHeight) {
	 * super.validate(); } lastWidth = width; lastHeight = height; } */
	
	public void revalidate()
	{
		super.invalidate();
		super.validate();
	}
	
	public void forcedValidate()
	{
		super.validate();
	}
	
	/* public void update(Graphics g) { paint(g); }
	 * public void paint(Graphics g) {
	 * }
	 * public void paintAll(Graphics g) {
	 * } */
}