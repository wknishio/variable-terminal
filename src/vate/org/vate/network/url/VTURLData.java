package org.vate.network.url;

import java.util.List;
import java.util.Map;

public class VTURLData
{
	public int getCode()
	{
		return code;
	}

	public byte[] getData()
	{
		return data;
	}

	public Map<String,List<String>> getHeaders()
	{
		return headers;
	}

	public String getResponse()
	{
		return response;
	}

	private int code = -1;
	private byte[] data;
	private Map<String,List<String>> headers;
	private String response;
	
	public VTURLData(int code, byte[] data, String response, Map<String,List<String>> headers)
	{
		this.code = code;
		this.data = data;
		this.response = response;
		this.headers = headers;
	}
}
