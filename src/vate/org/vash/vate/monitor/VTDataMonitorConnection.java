package org.vash.vate.monitor;

import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;

public class VTDataMonitorConnection
{
  private final VTLinkableDynamicMultiplexingInputStream input;
  private final VTLinkableDynamicMultiplexingOutputStream output;
  
  public VTDataMonitorConnection(VTLinkableDynamicMultiplexingInputStream input, VTLinkableDynamicMultiplexingOutputStream output)
  {
    this.input = input;
    this.output = output;
  }
  
  public VTLinkableDynamicMultiplexingInputStream getInput()
  {
    return input;
  }
  
  public VTLinkableDynamicMultiplexingOutputStream getOutput()
  {
    return output;
  }
}
