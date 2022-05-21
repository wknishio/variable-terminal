package org.vash.vate.nativeutils;

public interface VTMinimalCLibrary extends VTCLibrary
{
  public int putenv(String env);
}