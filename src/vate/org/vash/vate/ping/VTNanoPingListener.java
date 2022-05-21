package org.vash.vate.ping;

public interface VTNanoPingListener
{
  public void pingObtained(long localNanoDelay, long remoteNanoDelay);
}