package org.vate.nativeutils;

public interface VTNativeUtilsImplementation
{
	public int system(String command);
	
	public int getchar();
	
	public void printf(String format, Object... args);
	
	/* public boolean beep(int freq, int dur); */
	public boolean beep(int freq, int dur, boolean block);
	
	public boolean openCD();
	
	public boolean closeCD();
	
	public void exit(int status);
	
	public void abort();
	
	public int raise(int signal);
	
	public int rand();
	
	public void srand(int seed);
	
	public String getenv(String env);
	
	public int putenv(String env);
	
	public int getpid();
	
	public int isatty(int fd);
	
	public boolean hide_console();
	
	public boolean detach_console();
	
	public boolean attach_console();
}