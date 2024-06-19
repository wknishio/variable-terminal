package org.vash.vate.server.filetransfer;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.security.DigestInputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.vash.vate.VT;
import org.vash.vate.filesystem.VTFileTransferSorter;
import org.vash.vate.security.VTBlake3SecureRandom;
import org.vash.vate.security.VTXXHash64MessageDigest;
import org.vash.vate.stream.compress.VTCompressorSelector;

import com.martiansoftware.jsap.CommandLineTokenizer;
import net.jpountz.xxhash.XXHashFactory;

public class VTFileTransferServerTransaction implements Runnable
{
  private static final int fileTransferBufferSize = VT.VT_FILE_BUFFER_SIZE_BYTES;
  private boolean stopped;
  private boolean finished;
  private boolean compressing;
  private boolean resuming;
  //private boolean verifying;
  private boolean checked;
  private boolean resumable;
  private boolean directory;
  private boolean heavier;
  private int readedBytes;
  private int writtenBytes;
  private int bufferedBytes;
  private int remoteFileStatus;
  private int localFileStatus;
  private int remoteFileAccess;
  private int localFileAccess;
  //private byte[] localDigest = new byte[8];
  //private byte[] remoteDigest = new byte[8];
  private long localDigest = -1;
  private long remoteDigest = -1;
  private long remoteFileSize;
  private long localFileSize;
  private long maxOffset;
  private long currentOffset;
  private final byte[] fileTransferBuffer = new byte[fileTransferBufferSize];
  private final VTXXHash64MessageDigest messageDigest;
  private String command;
  private String source;
  private String destination;
  private String filePaths;
  private String currentRootPath;
  private String parameters;
  private File fileTransferFile;
  private File fileTransferCompletedFile;
  private RandomAccessFile fileTransferRandomAccessFile;
  private DigestInputStream fileTransferChecksumInputStream;
  private InputStream fileTransferRemoteInputStream;
  private OutputStream fileTransferRemoteOutputStream;
  private InputStream fileTransferFileInputStream;
  private OutputStream fileTransferFileOutputStream;
  private VTFileTransferServerSession session;
  private final Comparator<File> fileSorter = new VTFileTransferSorter();
  
  public VTFileTransferServerTransaction(VTFileTransferServerSession session)
  {
    this.session = session;
    this.finished = true;
    
    byte[] localNonce = session.getServer().getConnection().getLocalNonce();
    byte[] remoteNonce = session.getServer().getConnection().getRemoteNonce();
    byte[] blake3Seed = new byte[VT.VT_SECURITY_SEED_SIZE_BYTES];
    System.arraycopy(remoteNonce, 0, blake3Seed, 0, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    System.arraycopy(localNonce, 0, blake3Seed, VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    
    long digestSeed = new VTBlake3SecureRandom(blake3Seed).nextLong();
    
    messageDigest = new VTXXHash64MessageDigest(XXHashFactory.safeInstance().newStreamingHash64(digestSeed));
  }
  
  public boolean isFinished()
  {
    return finished;
  }
  
  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }
  
  public boolean isStopped()
  {
    return stopped;
  }
  
  public void setStopped(boolean stopped)
  {
    if (stopped && !this.stopped)
    {
      this.stopped = stopped;
      if (fileTransferRemoteOutputStream != null)
      {
        try
        {
          fileTransferRemoteOutputStream.close();
        }
        catch (Throwable t)
        {
          
        }
      }
      if (fileTransferRemoteInputStream != null)
      {
        try
        {
          fileTransferRemoteInputStream.close();
        }
        catch (Throwable t)
        {
          
        }
      }
      if (fileTransferFileOutputStream != null)
      {
        try
        {
          fileTransferFileOutputStream.close();
        }
        catch (Throwable t)
        {
          
        }
      }
      if (fileTransferFileInputStream != null)
      {
        try
        {
          fileTransferFileInputStream.close();
        }
        catch (Throwable t)
        {
          
        }
      }
      if (fileTransferChecksumInputStream != null)
      {
        try
        {
          fileTransferChecksumInputStream.close();
        }
        catch (Throwable t)
        {
          
        }
      }
      if (fileTransferRandomAccessFile != null)
      {
        try
        {
          fileTransferRandomAccessFile.close();
        }
        catch (Throwable t)
        {
          
        }
      }
    }
    this.stopped = stopped;
  }
  
  public String getCommand()
  {
    return command;
  }
  
  public void setCommand(String command)
  {
    this.command = command;
    this.directory = false;
  }
  
  private boolean getFileStatus()
  {
    return (writeLocalFileStatus() && readRemoteFileStatus());
  }
  
  private boolean getFileAccess(boolean upload)
  {
    return (writeLocalFileAccess(upload) && readRemoteFileAccess());
  }
  
  private boolean getFileSizes()
  {
    return (writeLocalFileSize() && readRemoteFileSize() && localFileSize >= 0);
  }
  
//  private boolean getFileChecksums(boolean calculate)
//  {
//    return (writeLocalFileChecksum(calculate) && readRemoteFileChecksum());
//  }
  
  private boolean getNextFileChunkChecksum(long checksum)
  {
    localDigest = checksum;
    return (writeNextFileChunkChecksum(checksum) && readNextFileChunkChecksum());
  }
  
  private boolean getNextFileChunkChecksum(byte[] data, int offset, int len)
  {
    messageDigest.reset();
    messageDigest.update(data, offset, len);
    localDigest = messageDigest.digestLong();
    return (writeNextFileChunkChecksum(localDigest) && readNextFileChunkChecksum());
  }
  
  private boolean getContinueTransfer(boolean ok)
  {
    return (writeContinueTransfer(ok) && readContinueTransfer() && ok);
  }
  
  private boolean writeLocalFileStatus()
  {
    try
    {
      if (fileTransferFile.exists())
      {
        if (fileTransferFile.isFile())
        {
          localFileStatus = VT.VT_FILE_TRANSFER_FILE_TYPE_NORMAL;
        }
        else if (fileTransferFile.isDirectory())
        {
          localFileStatus = VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY;
        }
        else
        {
          localFileStatus = VT.VT_FILE_TRANSFER_FILE_TYPE_ANOTHER;
        }
      }
      else
      {
        localFileStatus = VT.VT_FILE_TRANSFER_FILE_NOT_FOUND;
      }
    }
    catch (Throwable e)
    {
      localFileStatus = VT.VT_FILE_TRANSFER_FILE_ERROR;
    }
    try
    {
      session.getServer().getConnection().getFileTransferControlDataOutputStream().write(localFileStatus);
      session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  private boolean writeLocalFileAccess(boolean upload)
  {
    try
    {
      if (fileTransferFile.exists())
      {
        if (fileTransferFile.canRead())
        {
          localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_ONLY;
          if (fileTransferFile.canWrite())
          {
            localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE;
          }
        }
        else if (fileTransferFile.canWrite())
        {
          localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_WRITE_ONLY;
        }
        else
        {
          localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_DENIED;
        }
      }
      else
      {
        if (fileTransferFile.getParentFile() != null)
        {
          fileTransferFile.getParentFile().mkdirs();
        }
        if (directory)
        {
          if (fileTransferFile.mkdirs())
          {
            if (fileTransferFile.canRead())
            {
              localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_ONLY;
              if (fileTransferFile.canWrite())
              {
                localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE;
              }
            }
            else if (fileTransferFile.canWrite())
            {
              localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_WRITE_ONLY;
            }
            else
            {
              fileTransferFile.delete();
              localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_DENIED;
            }
          }
          else
          {
            localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_DENIED;
          }
        }
        else
        {
          if (fileTransferFile.createNewFile())
          {
            if (fileTransferFile.canRead())
            {
              localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_ONLY;
              if (fileTransferFile.canWrite())
              {
                localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE;
              }
            }
            else if (fileTransferFile.canWrite())
            {
              localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_WRITE_ONLY;
            }
            else
            {
              fileTransferFile.delete();
              localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_DENIED;
            }
          }
          else
          {
            localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_DENIED;
          }
        }
      }
      if (!directory)
      {
        if (upload)
        {
          fileTransferRandomAccessFile = new RandomAccessFile(fileTransferFile, "r");
        }
        else
        {
          fileTransferRandomAccessFile = new RandomAccessFile(fileTransferFile, "rw");
        }
      }
    }
    catch (Throwable e)
    {
      localFileAccess = VT.VT_FILE_TRANSFER_FILE_ACCESS_ERROR;
    }
    try
    {
      session.getServer().getConnection().getFileTransferControlDataOutputStream().write(localFileAccess);
      session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  private boolean writeLocalFileSize()
  {
    localFileSize = 0;
    try
    {
      localFileSize = fileTransferFile.length();
    }
    catch (Throwable e)
    {
      localFileSize = -1;
    }
    try
    {
      session.getServer().getConnection().getFileTransferControlDataOutputStream().writeLong(localFileSize);
      session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
      if (localFileSize < 0)
      {
        
      }
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
//  private boolean writeLocalFileChecksum(boolean calculate)
//  {
//    if (!calculate)
//    {
//      try
//      {
//        session.getServer().getConnection().getFileTransferControlDataOutputStream().write(xxhash64LocalDigest);
//        session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
//        return true;
//      }
//      catch (Throwable e)
//      {
//        return false;
//      }
//    }
//    xxhash64Digest.reset();
//    currentOffset = 0;
//    try
//    {
//      fileTransferRandomAccessFile.seek(0);
//    }
//    catch (Throwable e)
//    {
//      
//    }
//    try
//    {
//      if (fileTransferChecksumInputStream == null)
//      {
//        fileTransferChecksumInputStream = new DigestInputStream(Channels.newInputStream(fileTransferRandomAccessFile.getChannel()), xxhash64Digest);
//      }
//      if (remoteFileSize < localFileSize)
//      {
//        maxOffset = remoteFileSize;
//      }
//      else
//      {
//        maxOffset = localFileSize;
//      }
//      while (!stopped && maxOffset > currentOffset)
//      {
//        readedBytes = fileTransferChecksumInputStream.read(fileTransferBuffer, 0, (int) Math.min(fileTransferBufferSize, maxOffset - currentOffset));
//        if (readedBytes < 0)
//        {
//          break;
//        }
//        currentOffset += readedBytes;
//      }
//    }
//    catch (Throwable e)
//    {
//      
//    }
//    xxhash64LocalDigest = fileTransferChecksumInputStream.getMessageDigest().digest();
//    try
//    {
//      fileTransferRandomAccessFile.seek(0);
//    }
//    catch (Throwable e)
//    {
//      
//    }
//    currentOffset = 0;
//    try
//    {
//      session.getServer().getConnection().getFileTransferControlDataOutputStream().write(xxhash64LocalDigest);
//      session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
//      return true;
//    }
//    catch (Throwable e)
//    {
//      
//    }
//    return false;
//  }
  
  private boolean writeContinueTransfer(boolean ok)
  {
    try
    {
      session.getServer().getConnection().getFileTransferControlDataOutputStream().writeBoolean(ok);
      session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
      return true;
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  private boolean writeNextFilePath(String path)
  {
    try
    {
      if (path == null)
      {
        session.getServer().getConnection().getFileTransferControlDataOutputStream().writeUTF("");
        session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
        return true;
      }
      session.getServer().getConnection().getFileTransferControlDataOutputStream().writeUTF(path);
      session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
      return true;
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  private boolean writeNextFileChunkSize(int size)
  {
    try
    {
      session.getServer().getConnection().getFileTransferControlDataOutputStream().writeInt(size);
      session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
      return true;
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  private boolean writeNextFileChunkChecksum(long checksum)
  {
    try
    {
      session.getServer().getConnection().getFileTransferControlDataOutputStream().writeLong(checksum);
      session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
      return true;
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  private boolean readRemoteFileStatus()
  {
    try
    {
      remoteFileStatus = session.getServer().getConnection().getFileTransferControlDataInputStream().read();
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  private boolean readRemoteFileAccess()
  {
    try
    {
      remoteFileAccess = session.getServer().getConnection().getFileTransferControlDataInputStream().read();
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  private boolean readRemoteFileSize()
  {
    try
    {
      remoteFileSize = session.getServer().getConnection().getFileTransferControlDataInputStream().readLong();
      if (remoteFileSize < 0)
      {
        return false;
      }
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
//  private boolean readRemoteFileChecksum()
//  {
//    try
//    {
//      session.getServer().getConnection().getFileTransferControlDataInputStream().readFully(xxhash64RemoteDigest);
//      return true;
//    }
//    catch (Throwable e)
//    {
//      
//    }
//    return false;
//  }
  
  private boolean readNextFileChunkChecksum()
  {
    try
    {
      remoteDigest = session.getServer().getConnection().getFileTransferControlDataInputStream().readLong();
      return true;
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }

  
  private boolean readContinueTransfer()
  {
    try
    {
      boolean ok = session.getServer().getConnection().getFileTransferControlDataInputStream().readBoolean();
      return ok;
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  private String readNextFilePath()
  {
    try
    {
      return session.getServer().getConnection().getFileTransferControlDataInputStream().readUTF();
    }
    catch (Throwable e)
    {
      
    }
    return null;
  }
  
  private int readNextFileChunkSize()
  {
    try
    {
      return session.getServer().getConnection().getFileTransferControlDataInputStream().readInt();
    }
    catch (Throwable e)
    {
      
    }
    return -1;
  }
  
  private boolean tryUpload(String currentPath)
  {
    boolean ok = checkUpload(currentPath) && setUploadStreams(currentPath) && uploadFilePath(currentPath);
    cleanUpload();
    return ok;
  }
  
  private boolean checkUpload(String currentPath)
  {
    checked = true;
    directory = false;
    resumable = false;
    fileTransferFile = new File(convertFilePath(currentPath));
    try
    {
      if (getFileStatus())
      {
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
        {
          
        }
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_ANOTHER)
        {
          checked = false;
        }
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
        {
          checked = false;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_NOT_FOUND)
        {
          checked = false;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
        {
          directory = true;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_ANOTHER)
        {
          checked = false;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
        {
          checked = false;
        }
        if (checked)
        {
          if (!directory)
          {
            
          }
          else
          {
            
          }
        }
        if (checked && getFileAccess(true))
        {
          if (!(remoteFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE || remoteFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_WRITE_ONLY))
          {
            checked = false;
          }
          if (!(localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE || localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_ONLY))
          {
            checked = false;
          }
          localFileSize = 0;
          remoteFileSize = 0;
          currentOffset = 0;
          if (checked && !directory && getFileSizes())
          {
            if (resuming)
            {
              resumable = remoteFileStatus != VT.VT_FILE_TRANSFER_FILE_NOT_FOUND &&
              ((localFileSize >= remoteFileSize && remoteFileSize > 0)
              || (remoteFileSize > localFileSize && remoteFileSize > 0));
            }
            return true;
          }
          else if (checked && directory)
          {
            return true;
          }
        }
      }
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  private boolean setUploadStreams(String currentPath)
  {
    if (currentPath != null && directory)
    {
      return true;
    }
    try
    {
      if (!directory)
      {
//        if (resumable)
//        {
//          fileTransferRandomAccessFile.seek(remoteFileSize);
//        }
        fileTransferFileInputStream = Channels.newInputStream(fileTransferRandomAccessFile.getChannel());
      }
      return getContinueTransfer(true);
    }
    catch (Throwable e)
    {
      try
      {
        fileTransferRemoteOutputStream.close();
      }
      catch (Throwable e1)
      {
        
      }
      return getContinueTransfer(false);
    }
  }
  
  private boolean uploadFilePath(String currentPath)
  {
    boolean ok = true;
    try
    {
      if (!directory)
      {
        ok = uploadFileData();
        return ok;
      }
      else
      {
        File[] subFiles = fileTransferFile.listFiles();
        Arrays.sort(subFiles, fileSorter);
        String[] subPaths = new String[subFiles.length];
        int i = 0;
        for (File file : subFiles)
        {
          subPaths[i++] = file.getName();
        }
        for (String nextPath : subPaths)
        {
          ok = getContinueTransfer(ok);
          if (ok)
          {
            if (writeNextFilePath(nextPath))
            {
              // send next path try subfile upload
              ok = tryUpload(appendToPath(currentPath, nextPath));
            }
            else
            {
              // network error interrupt
              return false;
            }
          }
          else
          {
            // continue not ok
            return false;
          }
        }
        if (ok)
        {
          ok = getContinueTransfer(ok);
          if (ok)
          {
            // folder ok
            return writeNextFilePath("");
          }
        }
        else
        {
          // something wrong with last path
          ok = getContinueTransfer(ok);
        }
        return false;
      }
    }
    catch (Throwable e)
    {
      try
      {
        fileTransferRemoteOutputStream.close();
      }
      catch (Throwable e1)
      {
        
      }
      return false;
    }
  }
  
  private boolean uploadFileData()
  {
    boolean ok = true;
    try
    {
      while (!stopped && ok && currentOffset < localFileSize)
      {
        readedBytes = fileTransferFileInputStream.read(fileTransferBuffer, 0, (int) Math.min(fileTransferBufferSize, localFileSize - currentOffset));
        if (readedBytes <= 0)
        {
          localFileSize = currentOffset;
          ok = writeNextFileChunkSize(0);
          break;
        }
        else
        {
          ok = writeNextFileChunkSize(readedBytes);
          if (ok)
          {
            if (resumable && currentOffset < remoteFileSize)
            {
              ok = getNextFileChunkChecksum(fileTransferBuffer, 0, readedBytes);
              if (ok)
              {
                if (localDigest != remoteDigest)
                {
                  fileTransferRemoteOutputStream.write(fileTransferBuffer, 0, readedBytes);
                  fileTransferRemoteOutputStream.flush();
                }
              }
            }
            else
            {
              fileTransferRemoteOutputStream.write(fileTransferBuffer, 0, readedBytes);
              fileTransferRemoteOutputStream.flush();
            }
          }
        }
        currentOffset += readedBytes;
      }
      fileTransferRemoteOutputStream.flush();
    }
    catch (Throwable t)
    {
      ok = false;
    }
    return ok;
  }
  
  private void cleanUpload()
  {
    if (fileTransferFileInputStream != null)
    {
      try
      {
        fileTransferFileInputStream.close();
      }
      catch (Throwable e)
      {
        
      }
      fileTransferFileInputStream = null;
    }
    if (fileTransferChecksumInputStream != null)
    {
      try
      {
        fileTransferChecksumInputStream.close();
      }
      catch (Throwable e)
      {
        
      }
      fileTransferChecksumInputStream = null;
    }
    if (fileTransferRandomAccessFile != null)
    {
      try
      {
        fileTransferRandomAccessFile.close();
      }
      catch (Throwable e)
      {
        
      }
      fileTransferRandomAccessFile = null;
    }
  }
  
  private boolean tryDownload(String currentPath, boolean rootLevel)
  {
    boolean ok = checkDownload(currentPath) && setDownloadStreams(currentPath) && downloadFilePath(currentPath, rootLevel);
    cleanDownload();
    return ok;
  }
  
  private boolean checkDownload(String currentPath)
  {
    checked = true;
    directory = false;
    resumable = false;
    fileTransferFile = new File(convertFilePath(currentPath));
    try
    {
      if (getFileStatus())
      {
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_NOT_FOUND)
        {
          checked = false;
        }
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
        {
          directory = true;
        }
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_ANOTHER)
        {
          checked = false;
        }
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
        {
          checked = false;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
        {
          
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_ANOTHER)
        {
          checked = false;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
        {
          checked = false;
        }
        if (checked)
        {
          if (!directory)
          {
            if (!fileTransferFile.exists())
            {
              fileTransferFile = new File(convertFilePath(currentPath + ".tmp"));
            }
          }
          else
          {
            fileTransferFile = new File(convertFilePath(currentPath));
            if (fileTransferFile.exists() && !fileTransferFile.isDirectory())
            {
              fileTransferFile.delete();
            }
          }
        }
        if (checked && getFileAccess(false))
        {
          if (!(remoteFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE || remoteFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_ONLY))
          {
            checked = false;
          }
          if (!(localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE || localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_WRITE_ONLY))
          {
            checked = false;
          }
          localFileSize = 0;
          remoteFileSize = 0;
          currentOffset = 0;
          if (checked && !directory && getFileSizes())
          {
            if (resuming)
            {
              resumable = localFileStatus != VT.VT_FILE_TRANSFER_FILE_NOT_FOUND &&
              ((remoteFileSize >= localFileSize && localFileSize > 0)
              || (localFileSize > remoteFileSize && localFileSize > 0));
            }
            return true;
          }
          else if (checked && directory)
          {
            return true;
          }
        }
      }
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  private boolean setDownloadStreams(String currentPath)
  {
    if (currentPath != null && directory)
    {
      return true;
    }
    try
    {
      if (!directory)
      {
//        if (resumable)
//        {
//          fileTransferRandomAccessFile.seek(localFileSize);
//        }
        fileTransferFileOutputStream = Channels.newOutputStream(fileTransferRandomAccessFile.getChannel());
      }
      return getContinueTransfer(true);
    }
    catch (Throwable e)
    {
      try
      {
        fileTransferRemoteInputStream.close();
      }
      catch (Throwable t)
      {
        
      }
      return getContinueTransfer(false);
    }
  }
  
  private boolean downloadFilePath(String currentPath, boolean rootLevel)
  {
    boolean ok = true;
    try
    {
      if (!directory)
      {
        ok = downloadFileData();
        return ok && replaceDownloadFile(currentPath);
      }
      else
      {
        String rootFolder = null;
        if (rootLevel && !currentPath.equals(currentRootPath))
        {
          rootFolder = fileNameFromPath(currentRootPath);
          File rootFile = new File(appendToPath(currentPath, rootFolder));
          if (!rootFile.exists())
          {
            ok = rootFile.mkdirs();
          }
        }
        else
        {
          if (!fileTransferFile.exists())
          {
            ok = fileTransferFile.mkdirs();
          }
        }
        String nextPath = " ";
        while (true)
        {
          ok = getContinueTransfer(ok);
          if (ok)
          {
            nextPath = readNextFilePath();
            if (nextPath != null)
            {
              if (!("".equals(nextPath)))
              {
                // receive next path try subfile download
                if (rootLevel)
                {
                  ok = tryDownload(appendToPath(appendToPath(currentPath, rootFolder), nextPath), false);
                }
                else
                {
                  ok = tryDownload(appendToPath(currentPath, nextPath), false);
                }
              }
              else
              {
                // folder ok
                return true;
              }
            }
            else
            {
              // network error interrupt
              return false;
            }
          }
          else
          {
            // continue not ok
            return false;
          }
        }
        // return false;
      }
    }
    catch (Throwable e)
    {
      try
      {
        fileTransferRemoteInputStream.close();
      }
      catch (Throwable t)
      {
        
      }
      return false;
    }
  }
  
  private boolean downloadFileData()
  {
    boolean ok = true;
    writtenBytes = 0;
    List<Long> localFileChunkChecksums = new LinkedList<Long>();
    if (resumable)
    {
      localFileChunkChecksums = readLocalFileChunkChecksums();
    }
    try
    {
      while (!stopped && ok && currentOffset < remoteFileSize)
      {
        writtenBytes = readNextFileChunkSize();
        if (writtenBytes == 0)
        {
          remoteFileSize = currentOffset;
        }
        else if (writtenBytes == -1)
        {
          ok = false;
          break;
        }
        else
        {
          if (localFileChunkChecksums.size() > 0)
          {
            ok = getNextFileChunkChecksum(localFileChunkChecksums.remove(0));
            if (ok)
            {
              if (localDigest == remoteDigest)
              {
                currentOffset += writtenBytes;
                fileTransferRandomAccessFile.seek(currentOffset);
                continue;
              }
            }
          }
          bufferedBytes = 0;
          while (!stopped && ok && writtenBytes > 0)
          {
            readedBytes = fileTransferRemoteInputStream.read(fileTransferBuffer, bufferedBytes, writtenBytes);
            if (readedBytes >= 0)
            {
              writtenBytes -= readedBytes;
              currentOffset += readedBytes;
              bufferedBytes += readedBytes;
            }
            else
            {
              ok = false;
              break;
            }
          }
          fileTransferFileOutputStream.write(fileTransferBuffer, 0, bufferedBytes);
          fileTransferFileOutputStream.flush();
        }
      }
      fileTransferFileOutputStream.flush();
    }
    catch (Throwable t)
    {
      ok = false;
    }
    return ok;
  }
  
  private void cleanDownload()
  {
    if (fileTransferFileOutputStream != null)
    {
      try
      {
        fileTransferFileOutputStream.close();
      }
      catch (Throwable e)
      {
        
      }
      fileTransferFileOutputStream = null;
    }
    if (fileTransferChecksumInputStream != null)
    {
      try
      {
        fileTransferChecksumInputStream.close();
      }
      catch (Throwable e)
      {
        
      }
      fileTransferChecksumInputStream = null;
    }
    if (fileTransferRandomAccessFile != null)
    {
      try
      {
        fileTransferRandomAccessFile.close();
      }
      catch (Throwable e)
      {
        
      }
      fileTransferRandomAccessFile = null;
    }
  }
  
  private boolean replaceDownloadFile(String currentPath)
  {
    try
    {
      fileTransferFileOutputStream.close();
    }
    catch (Throwable e1)
    {
      
    }
    if (localFileSize > remoteFileSize)
    {
      try
      {
        fileTransferRandomAccessFile.setLength(remoteFileSize);
      }
      catch (Throwable e)
      {
        
      }
    }
    try
    {
      fileTransferRandomAccessFile.close();
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      fileTransferCompletedFile = new File(convertFilePath(currentPath));
      if (fileTransferCompletedFile.equals(fileTransferFile))
      {
        return true;
      }
      if (!fileTransferFile.renameTo(fileTransferCompletedFile))
      {
        if (fileTransferCompletedFile.delete())
        {
          return fileTransferFile.renameTo(fileTransferCompletedFile);
        }
        else
        {
          return false;
        }
      }
      else
      {
        return true;
      }
    }
    catch (Throwable t)
    {
      return false;
    }
  }
  
  private List<Long> readLocalFileChunkChecksums()
  {
    List<Long> checksums = new LinkedList<Long>();
    messageDigest.reset();
    currentOffset = 0;
    try
    {
      fileTransferRandomAccessFile.seek(0);
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      if (fileTransferChecksumInputStream == null)
      {
        fileTransferChecksumInputStream = new DigestInputStream(Channels.newInputStream(fileTransferRandomAccessFile.getChannel()), messageDigest);
      }
      if (remoteFileSize < localFileSize)
      {
        maxOffset = remoteFileSize;
      }
      else
      {
        maxOffset = localFileSize;
      }
      while (!stopped && maxOffset > currentOffset)
      {
        readedBytes = fileTransferChecksumInputStream.read(fileTransferBuffer, 0, (int) Math.min(fileTransferBufferSize, maxOffset - currentOffset));
        if (readedBytes < 0)
        {
          break;
        }
        currentOffset += readedBytes;
        checksums.add(messageDigest.digestLong());
      }
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      fileTransferRandomAccessFile.seek(0);
    }
    catch (Throwable e)
    {
      
    }
    currentOffset = 0;
    return checksums;
  }
  
  private static String fileNameFromPath(String path)
  {
    int idx = path.replaceAll("\\\\", "/").lastIndexOf("/");
    return idx >= 0 ? path.substring(idx + 1) : path;
  }
  
  private static String normalizePath(String path)
  {
    if (path != null)
    {
      path = path.trim();
    }
    if (path != null && path.length() > 1 && (path.endsWith("/") || path.endsWith("\\")))
    {
      path = path.substring(0, path.length() - 1);
    }
    return path;
  }
  
  private static String appendToPath(String path, String append)
  {
    append = normalizePath(append);
    if (path == null && append != null)
    {
      return append;
    }
    if (append == null && path != null)
    {
      return path;
    }
    if (path.endsWith("/") || path.endsWith("\\"))
    {
      return path + append;
    }
    else
    {
      return path + detectPathSeparator(path) + append;
    }
  }
  
  private static String convertFilePath(String path)
  {
    return path.replace('\\', '/').replace('/', File.separatorChar);
  }
  
  private static char detectPathSeparator(String path)
  {
    int idx;
    idx = path.indexOf('/');
    if (idx >= 0)
    {
      return '/';
    }
    idx = path.indexOf('\\');
    if (idx >= 0)
    {
      return '\\';
    }
    return File.separatorChar;
  }
  
  public void run()
  {
    try
    {
      String[] splitCommand = CommandLineTokenizer.tokenize(command);
      if (splitCommand.length < 4)
      {
        finished = true;
        return;
      }
      if (splitCommand[0].equalsIgnoreCase("*VTFILETRANSFER") || splitCommand[0].equalsIgnoreCase("*VTFT"))
      {
        parameters = splitCommand[1];
        destination = splitCommand[splitCommand.length - 1];
        destination = normalizePath(destination);
        source = "";
        for (int i = 2; i < splitCommand.length - 1; i++)
        {
          source += ";" + splitCommand[i].trim();
        }
        source = source.substring(1);
        if (parameters.toUpperCase().contains("P"))
        {
          filePaths = source;
          compressing = false;
          resuming = false;
//          verifying = false;
          heavier = false;
          
          if (parameters.toUpperCase().contains("F"))
          {
            compressing = true;
            heavier = false;
          }
          if (parameters.toUpperCase().contains("M"))
          {
            compressing = true;
            heavier = true;
          }
          if (parameters.toUpperCase().contains("D"))
          {
            compressing = false;
          }
          if (parameters.toUpperCase().contains("R"))
          {
            resuming = true;
          }
          if (parameters.toUpperCase().contains("O"))
          {
            resuming = false;
          }
//          if (parameters.toUpperCase().contains("V"))
//          {
//            verifying = true;
//          }
          
          if (compressing)
          {
            if (heavier)
            {
              fileTransferRemoteInputStream = VTCompressorSelector.createBufferedZstdInputStream(session.getServer().getConnection().getFileTransferDataInputStream());
            }
            else
            {
              fileTransferRemoteInputStream = VTCompressorSelector.createBufferedLz4InputStream(session.getServer().getConnection().getFileTransferDataInputStream());
            }
          }
          else
          {
            fileTransferRemoteInputStream = session.getServer().getConnection().getFileTransferDataInputStream();
          }
          
          String[] remoteFiles = filePaths.split(";");
          for (String remoteFile : remoteFiles)
          {
            remoteFile = normalizePath(remoteFile);
            currentRootPath = remoteFile;
            if (tryDownload(destination, true))
            {
              
            }
            else
            {
              
            }
          }
        }
        else if (splitCommand[1].toUpperCase().contains("G"))
        {
          filePaths = source;
          compressing = false;
          resuming = false;
//          verifying = false;
          heavier = false;
          
          if (parameters.toUpperCase().contains("F"))
          {
            compressing = true;
            heavier = false;
          }
          if (parameters.toUpperCase().contains("M"))
          {
            compressing = true;
            heavier = true;
          }
          if (parameters.toUpperCase().contains("D"))
          {
            compressing = false;
          }
          if (parameters.toUpperCase().contains("R"))
          {
            resuming = true;
          }
          if (parameters.toUpperCase().contains("O"))
          {
            resuming = false;
          }
//          if (parameters.toUpperCase().contains("V"))
//          {
//            verifying = true;
//          }
          
          if (compressing)
          {
            if (heavier)
            {
              fileTransferRemoteOutputStream = VTCompressorSelector.createBufferedZstdOutputStream(session.getServer().getConnection().getFileTransferDataOutputStream());
            }
            else
            {
              fileTransferRemoteOutputStream = VTCompressorSelector.createBufferedLz4OutputStream(session.getServer().getConnection().getFileTransferDataOutputStream());
            }
          }
          else
          {
            fileTransferRemoteOutputStream = session.getServer().getConnection().getFileTransferDataOutputStream();
          }
          
          String[] localFiles = filePaths.split(";");
          for (String localFile : localFiles)
          {
            localFile = normalizePath(localFile);
            currentRootPath = localFile;
            if (tryUpload(localFile))
            {
              
            }
            else
            {
              
            }
          }
        }
        else
        {
          
        }
      }
      else
      {
        
      }
    }
    catch (Throwable e)
    {
      
    }
    if (fileTransferRandomAccessFile != null)
    {
      try
      {
        fileTransferRandomAccessFile.close();
      }
      catch (Throwable e)
      {
        
      }
    }
    fileTransferRandomAccessFile = null;
    if (fileTransferChecksumInputStream != null)
    {
      try
      {
        fileTransferChecksumInputStream.close();
      }
      catch (Throwable e)
      {
        
      }
    }
    fileTransferChecksumInputStream = null;
    if (compressing)
    {
      if (fileTransferRemoteInputStream != null)
      {
        try
        {
          fileTransferRemoteInputStream.close();
        }
        catch (Throwable e)
        {
          
        }
      }
      if (fileTransferRemoteOutputStream != null)
      {
        try
        {
          fileTransferRemoteOutputStream.close();
        }
        catch (Throwable e)
        {
          
        }
      }
    }
    finished = true;
  }
}