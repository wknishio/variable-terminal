package com.github.luben.zstd.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.UnsatisfiedLinkError;
import java.util.LinkedList;
import java.util.List;

public enum Native {
    ;

    private static final String nativePathOverride = "ZstdNativePath";
    private static final String libnameShort = "zstd-jni";
    private static final String libname = "lib" + libnameShort;
    private static final String errorMsg = "Unsupported OS/arch, cannot find " +
        resourceName() + " or load " + libnameShort + " from system libraries. Please " +
        "try building from source the jar or providing " + libname + " in your system.";
    
    //private static final String ZSTDJNI_TMPLIB_PREFIX = "zstd-jni";

    private static String osName() {
        String os = System.getProperty("os.name").toLowerCase().replace(' ', '_');
        if (os.startsWith("win")){
            return "win";
        } else if (os.startsWith("mac")) {
            return "darwin";
        } else {
            return os;
        }
    }

    private static String osArch() {
        return System.getProperty("os.arch");
    }

    private static String libExtension() {
        if (osName().contains("os_x") || osName().contains("darwin")) {
            return "dylib";
         } else if (osName().contains("win")) {
            return "dll";
        } else {
            return "so";
        }
    }

    private static String resourceName() {
        return "/" + osName() + "/" + osArch() + "/" + libname + "." + libExtension();
    }

    private static boolean loaded = false;

    public static synchronized boolean isLoaded() {
        return loaded;
    }

    public static synchronized void load() {
        load(getTempDir());
    }

    public static synchronized void load(final File tempFolder) {
        if (loaded) {
            return;
        }
        String resourceName = resourceName();

        String overridePath = System.getProperty(nativePathOverride);
        if (overridePath != null) {
            // Do not fall-back to auto-discovery - consumers know better
            System.load(overridePath);
        }

        // try to load the shared library directly from the JAR
        try {
            Class.forName("org.osgi.framework.BundleEvent"); // Simple OSGI env. check
            System.loadLibrary(libname);
            loaded = true;
            return;
        } catch (Throwable e) {
            // ignore both ClassNotFound and UnsatisfiedLinkError, and try other methods
        }

        //InputStream is = Native.class.getResourceAsStream(resourceName);
        InputStream is = null;
        try
        {
            is = createByteArrayInputStreamFromInputStream(Native.class.getResourceAsStream(resourceName));
        }
        catch (Throwable t)
        {
        	
        }
        
        if (is == null) {
            // fall-back to loading the zstd-jni from the system library path.
            // It also cover loading on Android.
            try {
                System.loadLibrary(libnameShort);
                loaded = true;
                return;
            } catch (UnsatisfiedLinkError e) {
                UnsatisfiedLinkError err = new UnsatisfiedLinkError(e.getMessage() + "\n" + errorMsg);
                err.setStackTrace(e.getStackTrace());
                throw err;
            }
        }
        else
        {
        	try
        	{
        		if (tryLoadLibraryInFolder(libname, "." + libExtension(), is.available(), getTempDir()))
            	{
            		return;
            	}
        	}
        	catch (Throwable t)
        	{
        		
        	}
        }
        File tempLib = null;
        FileOutputStream out = null;
        try {
            tempLib = File.createTempFile(libname, "." + libExtension(), tempFolder);
            // try to delete on exit, does not work on Windows
            //tempLib.deleteOnExit();
            // copy to tempLib
            out = new FileOutputStream(tempLib);
            byte[] buf = new byte[1024 * 32];
            while (true) {
                int read = is.read(buf);
                if (read == -1) {
                    break;
                }
                out.write(buf, 0, read);
            }
            try {
                out.flush();
                out.close();
                out = null;
            } catch (IOException e) {
                // ignore
            }
            try {
                System.load(tempLib.getAbsolutePath());
            } catch (UnsatisfiedLinkError e) {
                // fall-back to loading the zstd-jni from the system library path
                try {
                    System.loadLibrary(libnameShort);
                } catch (UnsatisfiedLinkError e1) {
                    // display error in case problem with loading from temp folder
                    // and from system library path - concatenate both messages
                    UnsatisfiedLinkError err = new UnsatisfiedLinkError(
                            e.getMessage() + "\n" +
                            e1.getMessage() + "\n"+
                            errorMsg);
                    err.setStackTrace(e1.getStackTrace());
                    throw err;
                }
            }
            loaded = true;
        } catch (IOException e) {
            // IO errors in extacting and writing the shared object in the temp dir
            ExceptionInInitializerError err = new ExceptionInInitializerError(
                    "Cannot unpack " + libname + ": " + e.getMessage());
            err.setStackTrace(e.getStackTrace());
            throw err;
        }
        finally {
            try {
                is.close();
                if (out != null) {
                    out.close();
                }
                if (tempLib != null && tempLib.exists()) {
                    //tempLib.delete();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    public static File getTempDir() {
        File zstdjnitmp = null;
        String prop = System.getProperty("zstd-jni.tmpdir");
        if (prop != null) {
        	zstdjnitmp = new File(prop);
        	zstdjnitmp.mkdirs();
        }
        else {
            File tmp = new File(System.getProperty("java.io.tmpdir"));
            // Loading DLLs via System.load() under a directory with a unicode
            // name will fail on windows, so use a hash code of the user's
            // name in case the user's name contains non-ASCII characters
            zstdjnitmp = new File(tmp, "zstd-jni-" + System.getProperty("user.name").hashCode());
            zstdjnitmp.mkdirs();
            if (!zstdjnitmp.exists() || !zstdjnitmp.canWrite()) {
            	zstdjnitmp = tmp;
            }
        }
        if (!zstdjnitmp.exists()) {
            return null;
        }
        if (!zstdjnitmp.canWrite()) {
        	return null;
        }
        return zstdjnitmp;
    }
    
    public static ByteArrayInputStream createByteArrayInputStreamFromInputStream(InputStream input) throws IOException
    {
    	ByteArrayOutputStream output = new ByteArrayOutputStream();
    	byte[] buf = new byte[1024 * 32];
    	int readed = 0;
    	while ((readed = input.read(buf)) > 0)
    	{
    		output.write(buf, 0, readed);
    	}
    	input.close();
    	return new ByteArrayInputStream(output.toByteArray());
    }
    
    public static File[] searchMatchingFileInFolder(String name, String extension, long size, File folder)
    {
		List<File> matched = new LinkedList<File>();
		File[] list = folder.listFiles();
		for (int i = 0; i < list.length; i++)
		{
			if (list[i].getName().contains(name)
			&& list[i].getName().endsWith(extension)
			&& list[i].length() == size)
			{
				matched.add(list[i]);
			}
		}
		
		return matched.toArray(new File[] {} );
    }
    
    public static boolean tryLoadLibraryInFolder(String name, String extension, long size, File folder)
    {
    	File[] files = searchMatchingFileInFolder(name, extension, size, folder);
    	if (files != null)
    	{
    		for (int i = 0; i < files.length; i++)
    		{
    			try
        		{
        			System.load(files[i].getAbsolutePath());
        			return true;
        		}
        		catch (Throwable t)
        		{
        			
        		}
    		}
    	}
    	return false;
    }
}
