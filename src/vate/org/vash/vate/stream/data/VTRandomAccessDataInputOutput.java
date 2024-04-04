package org.vash.vate.stream.data;

import java.io.Closeable;
import java.io.IOException;

public interface VTRandomAccessDataInputOutput extends VTRandomAccessDataInput, VTRandomAccessDataOutput, Closeable
{
  public void reset() throws IOException;
}