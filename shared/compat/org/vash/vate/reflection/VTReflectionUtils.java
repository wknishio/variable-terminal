package org.vash.vate.reflection;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VTReflectionUtils
{
  public static int getJavaVersion()
  {
    try
    {
      String version = System.getProperty("java.version");
      if (version.startsWith("1."))
      {
        version = version.substring(2, 3);
      }
      else
      {
        int dot = version.indexOf(".");
        if (dot != -1)
        {
          version = version.substring(0, dot);
        }
      }
      return Integer.parseInt(version);
    }
    catch (Throwable t)
    {
      
    }
    return -1;
  }
  
  public static final Set<String> findClassNamesFromPackage(String pkg)
  {
    Set<String> classNames = new LinkedHashSet<String>();
    String path = pkg.replace('.', '/');
    try
    {
      ClassLoader cl = ClassLoader.getSystemClassLoader();
      URL[] urls = ((java.net.URLClassLoader) cl).getURLs();
      for (URL url : urls)
      {
        // System.out.println(url.getFile());
        File jar = new File(url.getFile());
        if (jar.isDirectory())
        {
          File subdir = new File(jar, path);
          if (!subdir.exists())
          {
            continue;
          }
          File[] files = subdir.listFiles();
          for (File file : files)
          {
            if (!file.isFile())
            {
              continue;
            }
            if (file.getName().endsWith(".class"))
            {
              classNames.add(pkg + "." + file.getName().substring(0, file.getName().length() - 6));
            }
          }
        }
        else
        {
          // try to open as ZIP
          // ZipFile zip = null;
          ZipInputStream zip = null;
          try
          {
            zip = new ZipInputStream(new FileInputStream(jar));
            ZipEntry entry = null;
            while ((entry = zip.getNextEntry()) != null)
            {
              // ZipEntry entry = entries.nextElement();
              String file = entry.getName();
              if (!file.startsWith(path))
              {
                continue;
              }
              file = file.substring(path.length() + 1);
              if (file.indexOf('/') < 0 && file.endsWith(".class"))
              {
                classNames.add(pkg + "." + file.substring(0, file.length() - 6));
              }
            }
          }
          catch (Throwable e)
          {
            //
          }
          finally
          {
            if (zip != null)
            {
              try
              {
                zip.close();
              }
              catch (Throwable e)
              {
                
              }
            }
          }
        }
      }
    }
    catch (Throwable t)
    {
      
    }
    return classNames;
  }
  
  public static final Set<Class<?>> findClassesFromNames(Set<String> names)
  {
    Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
    for (String name : names)
    {
      try
      {
        classes.add(Class.forName(name));
      }
      catch (Throwable t)
      {
        
      }
    }
    return classes;
  }
  
  public static final Set<Object> createClassInstancesFromPackage(String pkg)
  {
    return createClassInstancesFromClasses(findClassesFromNames(findClassNamesFromPackage(pkg)));
  }
  
  @SuppressWarnings("all")
  public static final Set<Object> createClassInstancesFromClasses(Set<Class<?>> classes)
  {
    Set<Object> instances = new LinkedHashSet<Object>();
    for (Class<?> clazz : classes)
    {
      try
      {
        instances.add(clazz.newInstance());
      }
      catch (Throwable e)
      {
        
      }
    }
    return instances;
  }
  
  public static boolean isAWTHeadless()
  {
    try
    {
      Class<?> graphicsEnvironmentClass = Class.forName("java.awt.GraphicsEnvironment");
      Method isHeadlessMethod = graphicsEnvironmentClass.getDeclaredMethod("isHeadless");
      Object headless = isHeadlessMethod.invoke(null, new Object[] {});
      if (headless instanceof Boolean)
      {
        Boolean result = (Boolean) headless;
        return result;
      }
    }
    catch (Throwable t)
    {
      
    }
    return true;
  }
  
  public static boolean detectWindows()
  {
    return System.getProperty("os.name").toLowerCase().contains("windows");
  }
}