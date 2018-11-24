package org.vate.server.graphicsmode;

import org.vate.server.session.VTServerSession;
import org.vate.task.VTTask;

public class VTGraphicsModeServer extends VTTask
{
	private VTGraphicsModeServerSessionHandler sessionHandler;
	
	public VTGraphicsModeServer(VTServerSession session)
	{
		this.sessionHandler = new VTGraphicsModeServerSessionHandler(new VTGraphicsModeServerSession(session));
		// this.setStopped(true);
	}
	
	public boolean isStopped()
	{
		return sessionHandler.isStopped();
	}
	
	public void setStopped(boolean stopped)
	{
		sessionHandler.setStopped(stopped);
	}
	
	public boolean isReadOnly()
	{
		return sessionHandler.isReadOnly();
	}
	
	public void setReadOnly(boolean readOnly)
	{
		sessionHandler.setReadOnly(readOnly);
	}
	
	/* public void setHighQuality(boolean highQuality) {
	 * sessionHandler.setHighQuality(highQuality); } */
	
	public void run()
	{
		sessionHandler.run();
	}
}