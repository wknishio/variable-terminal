package org.vash.vate.filesystem;

import java.io.File;

public class VTRootFile extends File
{
  private String rootPath;
  private String rootName;
  private VTRootList parent;
  
  public VTRootFile(String path, VTRootList root)
  {
    super(path);
    this.rootPath = path;
    this.rootName = path;
    this.parent = root;
  }
  
  public String getParent()
  {
    return parent.getPath();
  }
  
  public File getParentFile()
  {
    return parent;
  }
  
  public String getName()
  {
    return rootName;
  }
  
  public String getPath()
  {
    return rootPath;
  }
  
  public File getAbsoluteFile()
  {
    return this;
  }
  
  public String getAbsolutePath()
  {
    return rootPath;
  }
  
  public File getCanonicalFile()
  {
    return this;
  }
  
  public String getCanonicalPath()
  {
    return rootPath;
  }
  
  private static final long serialVersionUID = 1L;
}