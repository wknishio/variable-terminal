/*
 * Copyright 2017 Guilherme Chaguri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guichaguri.minimalftp.impl;

import com.guichaguri.minimalftp.Utils;
import com.guichaguri.minimalftp.api.IFileSystem;
import java.io.*;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Native File System
 *
 * Allows the manipulation of any file inside a directory
 * @author Guilherme Chaguri
 */
public class NativeFileSystem implements IFileSystem<File> {

    private final File rootDir;
    private static Method canExecuteMethod;
    private static Method setReadableMethod;
    private static Method setWritableMethod;
    private static Method setExecutableMethod;
    
    static
    {
      try
      {
        canExecuteMethod = File.class.getDeclaredMethod("canExecute");
        setReadableMethod = File.class.getDeclaredMethod("setReadable", Boolean.class, Boolean.class);
        setWritableMethod = File.class.getDeclaredMethod("setWritable", Boolean.class, Boolean.class);
        setExecutableMethod = File.class.getDeclaredMethod("setExecutable", Boolean.class, Boolean.class);
      }
      catch (Throwable t)
      {
        
      }
    }
    
    public boolean canExecute(File file)
    {
      try
      {
        if (canExecuteMethod != null)
        {
          Boolean result = (Boolean) canExecuteMethod.invoke(file);
          return result;
        }
      }
      catch (Throwable t)
      {
        
      }
      return false;
    }
    
    public boolean setReadable(File file, boolean readable, boolean ownerOnly)
    {
      try
      {
        if (setReadableMethod != null)
        {
          Boolean result = (Boolean) setReadableMethod.invoke(file, readable, ownerOnly);
          return result;
        }
      }
      catch (Throwable t)
      {
        
      }
      return false;
    }
    
    public boolean setWritable(File file, boolean writable, boolean ownerOnly)
    {
      try
      {
        if (setWritableMethod != null)
        {
          Boolean result = (Boolean) setWritableMethod.invoke(file, writable, ownerOnly);
          return result;
        }
      }
      catch (Throwable t)
      {
        
      }
      return false;
    }
    
    public boolean setExecutable(File file, boolean executable, boolean ownerOnly)
    {
      try
      {
        if (setExecutableMethod != null)
        {
          Boolean result = (Boolean) setExecutableMethod.invoke(file, executable, ownerOnly);
          return result;
        }
      }
      catch (Throwable t)
      {
        
      }
      return false;
    }
    
    /**
     * Creates a native file system.
     *
     * If the root directory does not exists, it will be created
     * @param rootDir The root directory
     */
    public NativeFileSystem(File rootDir)
    {
        this.rootDir = rootDir;
        //if(!rootDir.exists()) rootDir.mkdirs();
    }

    
    public File getRoot() {
        return rootDir;
    }

    
    public String getPath(File file) {
        return rootDir.toURI().relativize(file.toURI()).getPath();
    }

    
    public boolean exists(File file) {
        return file.exists();
    }

    
    public boolean isDirectory(File file) {
        return file.isDirectory();
    }

    
    public int getPermissions(File file) {
        int perms = 0;
        perms = Utils.setPermission(perms, Utils.CAT_OWNER + Utils.TYPE_READ, file.canRead());
        perms = Utils.setPermission(perms, Utils.CAT_OWNER + Utils.TYPE_WRITE, file.canWrite());
//        perms = Utils.setPermission(perms, Utils.CAT_OWNER + Utils.TYPE_EXECUTE, file.canExecute());
        perms = Utils.setPermission(perms, Utils.CAT_OWNER + Utils.TYPE_EXECUTE, this.canExecute(file));
        return perms;
    }

    
    public long getSize(File file) {
        return file.length();
    }

    
    public long getLastModified(File file) {
        return file.lastModified();
    }

    
    public int getHardLinks(File file) {
        return file.isDirectory() ? 3 : 1;
    }

    
    public String getName(File file) {
        return file.getName();
    }

    
    public String getOwner(File file) {
        return "-";
    }

    
    public String getGroup(File file) {
        return "-";
    }

    
    public File getParent(File file) throws IOException {
        if(file.equals(rootDir)) {
            throw new FileNotFoundException("No permission to access this file");
        }

        return file.getParentFile();
    }

    
    public File[] listFiles(File dir) throws IOException {
        if(!dir.isDirectory()) throw new IOException("Not a directory");

        return dir.listFiles();
    }

    
    public File findFile(String path) throws IOException {
        File file = new File(rootDir, path);

        if(!isInside(rootDir, file)) {
            throw new FileNotFoundException("No permission to access this file");
        }

        return file;
    }

    
    public File findFile(File cwd, String path) throws IOException {
        File file = new File(cwd, path);

        if(!isInside(rootDir, file)) {
            throw new FileNotFoundException("No permission to access this file");
        }

        return file;
    }

    
    public InputStream readFile(File file, long start) throws IOException {
        // Not really needed, but helps a bit in performance
        if(start <= 0) {
            return new FileInputStream(file);
        }

        // Use RandomAccessFile to seek a file
        final RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(start);

        // Create a stream using the RandomAccessFile
        return new FileInputStream(raf.getFD()) {
            
            public void close() throws IOException {
                super.close();
                raf.close();
            }
        };
    }

    
    public OutputStream writeFile(File file, long start) throws IOException {
        // Not really needed, but helps a bit in performance
        if(start <= 0) {
            return new FileOutputStream(file, false);
        } else if(start == file.length()) {
            return new FileOutputStream(file, true);
        }

        // Use RandomAccessFile to seek a file
        final RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(start);

        // Create a stream using the RandomAccessFile
        return new FileOutputStream(raf.getFD()) {
            
            public void close() throws IOException {
                super.close();
                raf.close();
            }
        };
    }

    
    public void mkdirs(File file) throws IOException {
        if(!file.mkdirs()) throw new IOException("Couldn't create the directory");
    }

    
    public void delete(File file) throws IOException {
        if(file.isDirectory()) {
//            Files.walk(file.toPath()) // Walks through all files, except links
//            .sorted(Comparator.reverseOrder()) // Reverse order, so it deletes from the highest depth to the lowest one
//            .map(Path::toFile) // Converts the Path objects to File objects
//            .forEach(File::delete); // Deletes it
            File[] childs = file.listFiles();
            for (File child : childs)
            {
                delete(child);
            }
            file.delete();
        } else {
            // Deletes a single file
            file.delete();
            //if(!file.delete()) throw new IOException("Couldn't delete the file");
        }
    }

    
    public void rename(File from, File to) throws IOException {
        if(!from.renameTo(to)) throw new IOException("Couldn't rename the file");
    }

    
    public void chmod(File file, int perms) throws IOException {
        boolean read = Utils.hasPermission(perms, Utils.CAT_OWNER + Utils.TYPE_READ);
        boolean write = Utils.hasPermission(perms, Utils.CAT_OWNER + Utils.TYPE_WRITE);
        boolean execute = Utils.hasPermission(perms, Utils.CAT_OWNER + Utils.TYPE_EXECUTE);
        
//        if(!file.setReadable(read, true)) throw new IOException("Couldn't update the readable permission");
//        if(!file.setWritable(write, true)) throw new IOException("Couldn't update the writable permission");
//        if(!file.setExecutable(execute, true)) throw new IOException("Couldn't update the executable permission");
        
        if(!this.setReadable(file, read, true)) throw new IOException("Couldn't update the readable permission");
        if(!this.setWritable(file, write, true)) throw new IOException("Couldn't update the writable permission");
        if(!this.setExecutable(file, execute, true)) throw new IOException("Couldn't update the executable permission");
    }

    
    public void touch(File file, long time) throws IOException {
        if(!file.setLastModified(time)) throw new IOException("Couldn't touch the file");
    }

    protected boolean isInside(File dir, File file) {
        if(file.equals(dir)) return true;
        
        try {
            return file.getCanonicalPath().startsWith(dir.getCanonicalPath());
        } catch(IOException ex) {
            return false;
        }
    }
    
    public byte[] getDigest(File file, String algorithm, int bufferSize) throws IOException, NoSuchAlgorithmException {
      MessageDigest d = MessageDigest.getInstance(algorithm);
      InputStream in = readFile(file, 0);
      byte[] bytes = new byte[bufferSize];
      int length;

      while((length = in.read(bytes)) != -1) {
          d.update(bytes, 0, length);
      }

      return d.digest();
    }
}
