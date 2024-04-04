package org.vash.vate.url;

import java.util.List;
import java.util.Map;

public class VTURLResult
{
  public int getCode()
  {
    return code;
  }
  
  public Map<String, List<String>> getHeaders()
  {
    return headers;
  }
  
  public String getResponse()
  {
    return response;
  }
  
  public boolean isFailed()
  {
    return failed;
  }
  
  private int code = -1;
  private String response;
  private Map<String, List<String>> headers;
  private boolean failed = false;
  
  public VTURLResult(int code, String response, Map<String, List<String>> headers, boolean failed)
  {
    this.code = code;
    this.response = response;
    this.headers = headers;
    this.failed = failed;
  }
}
