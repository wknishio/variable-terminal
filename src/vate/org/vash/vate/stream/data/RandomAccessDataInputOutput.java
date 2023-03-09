package org.vash.vate.stream.data;

import java.io.Closeable;
import java.io.IOException;

public interface RandomAccessDataInputOutput extends RandomAccessDataInput, RandomAccessDataOutput, Closeable
{
  public void reset() throws IOException;
}