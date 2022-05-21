package org.vash.vate.network.nat.mapping;

import java.util.List;

public interface VTNATPortMappingResultNotify
{
  public void result(List<String> externalHosts);
}
