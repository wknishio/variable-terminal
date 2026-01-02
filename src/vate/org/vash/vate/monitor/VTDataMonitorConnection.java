package org.vash.vate.monitor;

import org.vash.vate.stream.multiplex.VTMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingOutputStream;

public class VTDataMonitorConnection
{
  private final VTMultiplexingInputStream input;
  private final VTMultiplexingOutputStream output;
  
  public VTDataMonitorConnection(VTMultiplexingInputStream input, VTMultiplexingOutputStream output)
  {
    this.input = input;
    this.output = output;
  }
  
  public VTMultiplexingInputStream getInput()
  {
    return input;
  }
  
  public VTMultiplexingOutputStream getOutput()
  {
    return output;
  }
}
