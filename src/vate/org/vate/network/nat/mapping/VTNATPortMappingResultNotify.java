package org.vate.network.nat.mapping;

import java.util.Map;
import com.offbynull.portmapper.mapper.MappedPort;
import com.offbynull.portmapper.mapper.PortMapper;

public interface VTNATPortMappingResultNotify
{
  public void result(Map<PortMapper, MappedPort> currentMappedPorts);
}
