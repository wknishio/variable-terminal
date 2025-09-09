package org.vash.vate.client.filetransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.DigestInputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.vash.vate.VTSystem;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.filesystem.VTFileUtils;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.security.VTBlake3MessageDigest;
import org.vash.vate.security.VTXXHash64MessageDigest;
import org.vash.vate.stream.compress.VTCompressorSelector;

import com.martiansoftware.jsap.CommandLineTokenizerMKII;
import net.jpountz.xxhash.XXHashFactory;

public class VTFileTransferClientTransaction implements Runnable
{
  private static final int fileTransferBufferSize = VTSystem.VT_FILE_BUFFER_SIZE_BYTES;
  private volatile boolean stopped;
  private boolean finished;
  private boolean compressing;
  private boolean resuming;
  private boolean deleting;
  //private boolean verifying;
  private boolean checked;
  private boolean resumable;
  private boolean directory;
  private boolean heavier;
  private boolean interrupted;
  private int readedBytes;
  private int writtenBytes;
  private int bufferedBytes;
  private int remoteFileStatus;
  private int localFileStatus;
  private int remoteFileAccess;
  private int localFileAccess;
  private long localFileTime;
  private long remoteFileTime;
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
  private VTFileTransferClientSession session;
  //private final Comparator<File> fileSorter = new VTFileTransferSorter();
   
  public VTFileTransferClientTransaction(VTFileTransferClientSession session)
  {
    this.session = session;
    this.finished = true;
    
    byte[] localNonce = session.getClient().getConnection().getLocalNonce();
    byte[] remoteNonce = session.getClient().getConnection().getRemoteNonce();
    byte[] blake3Seed = new byte[VTSystem.VT_SECURITY_SEED_SIZE_BYTES];
    System.arraycopy(localNonce, 0, blake3Seed, 0, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES);
    System.arraycopy(remoteNonce, 0, blake3Seed, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES);
    
    VTBlake3MessageDigest blake3Digest = new VTBlake3MessageDigest(blake3Seed);
    blake3Digest.update(session.getClient().getConnection().getEncryptionKey());
    blake3Digest.update(session.getClient().getConnection().getDigestedCredentials());
    long digestSeed = blake3Digest.digestLong();
    
    this.messageDigest = new VTXXHash64MessageDigest(XXHashFactory.safeInstance().newStreamingHash64(digestSeed));
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
  
  public void setInterrupted()
  {
    interrupted = true;
  }
  
  public void setStopped(boolean stopped)
  {
    if (stopped && !this.stopped)
    {
      this.stopped = stopped;
      this.interrupted = stopped;
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
      session.endSession();
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
  
  private boolean checkFileStatus()
  {
    return (writeLocalFileStatus() && readRemoteFileStatus());
  }
  
  private boolean checkFileAccess(boolean upload)
  {
    return (writeLocalFileAccess(upload) && readRemoteFileAccess());
  }
  
  private boolean checkFileSizes()
  {
    return (writeLocalFileSize() && readRemoteFileSize() && localFileSize >= 0);
  }
  
  private boolean checkFileTimes()
  {
    return (writeLocalFileTime() && readRemoteFileTime());
  }
  
  private boolean checkNextFileChunkChecksum(long checksum)
  {
    localDigest = checksum;
    return (writeNextFileChunkChecksum(checksum) && readNextFileChunkChecksum());
  }
  
  private boolean checkNextFileChunkChecksum(byte[] data, int offset, int len)
  {
    messageDigest.reset();
    messageDigest.update(data, offset, len);
    localDigest = messageDigest.digestLong();
    return (writeNextFileChunkChecksum(localDigest) && readNextFileChunkChecksum());
  }
  
  private boolean checkContinueTransfer(boolean ok)
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
          localFileStatus = VTSystem.VT_FILE_TRANSFER_STATUS_NORMAL;
        }
        else if (fileTransferFile.isDirectory())
        {
          localFileStatus = VTSystem.VT_FILE_TRANSFER_STATUS_DIRECTORY;
        }
        else
        {
          localFileStatus = VTSystem.VT_FILE_TRANSFER_STATUS_ANOTHER;
        }
      }
      else
      {
        localFileStatus = VTSystem.VT_FILE_TRANSFER_STATUS_INEXISTENT;
      }
    }
    catch (Throwable e)
    {
      localFileStatus = VTSystem.VT_FILE_TRANSFER_STATUS_ERROR;
    }
    try
    {
      session.getClient().getConnection().getFileTransferControlDataOutputStream().write(localFileStatus);
      session.getClient().getConnection().getFileTransferControlDataOutputStream().flush();
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
      if (!fileTransferFile.exists())
      {
        if (fileTransferFile.getParentFile() != null)
        {
          fileTransferFile.getParentFile().mkdirs();
        }
        if (directory)
        {
          fileTransferFile.mkdirs();
        }
        else
        {
          fileTransferFile.createNewFile();
        }
      }
      if (!fileTransferFile.exists())
      {
        localFileAccess = VTSystem.VT_FILE_TRANSFER_ACCESS_DENIED;
      }
      else if (fileTransferFile.canRead())
      {
        localFileAccess = VTSystem.VT_FILE_TRANSFER_ACCESS_READ_ONLY;
        if (fileTransferFile.canWrite())
        {
          localFileAccess = VTSystem.VT_FILE_TRANSFER_ACCESS_READ_AND_WRITE;
        }
      }
      else if (fileTransferFile.canWrite())
      {
        localFileAccess = VTSystem.VT_FILE_TRANSFER_ACCESS_WRITE_ONLY;
      }
      else
      {
        localFileAccess = VTSystem.VT_FILE_TRANSFER_ACCESS_DENIED;
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
      localFileAccess = VTSystem.VT_FILE_TRANSFER_ACCESS_ERROR;
    }
    try
    {
      session.getClient().getConnection().getFileTransferControlDataOutputStream().write(localFileAccess);
      session.getClient().getConnection().getFileTransferControlDataOutputStream().flush();
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
      session.getClient().getConnection().getFileTransferControlDataOutputStream().writeLong(localFileSize);
      session.getClient().getConnection().getFileTransferControlDataOutputStream().flush();
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
  
  private boolean writeLocalFileTime()
  {
    localFileTime = 0;
    try
    {
      localFileTime = fileTransferFile.lastModified();
    }
    catch (Throwable e)
    {
      localFileTime = 0;
    }
    try
    {
      session.getClient().getConnection().getFileTransferControlDataOutputStream().writeLong(localFileTime);
      session.getClient().getConnection().getFileTransferControlDataOutputStream().flush();
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  private boolean writeContinueTransfer(boolean ok)
  {
    try
    {
      session.getClient().getConnection().getFileTransferControlDataOutputStream().writeBoolean(ok);
      session.getClient().getConnection().getFileTransferControlDataOutputStream().flush();
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
        session.getClient().getConnection().getFileTransferControlDataOutputStream().writeUTF("");
        session.getClient().getConnection().getFileTransferControlDataOutputStream().flush();
        return true;
      }
      session.getClient().getConnection().getFileTransferControlDataOutputStream().writeUTF(path);
      session.getClient().getConnection().getFileTransferControlDataOutputStream().flush();
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
      session.getClient().getConnection().getFileTransferControlDataOutputStream().writeInt(size);
      session.getClient().getConnection().getFileTransferControlDataOutputStream().flush();
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
      session.getClient().getConnection().getFileTransferControlDataOutputStream().writeLong(checksum);
      session.getClient().getConnection().getFileTransferControlDataOutputStream().flush();
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
      remoteFileStatus = session.getClient().getConnection().getFileTransferControlDataInputStream().read();
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
      remoteFileAccess = session.getClient().getConnection().getFileTransferControlDataInputStream().read();
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
      remoteFileSize = session.getClient().getConnection().getFileTransferControlDataInputStream().readLong();
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
  
  private boolean readRemoteFileTime()
  {
    try
    {
      remoteFileTime = session.getClient().getConnection().getFileTransferControlDataInputStream().readLong();
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  private boolean readContinueTransfer()
  {
    try
    {
      boolean ok = session.getClient().getConnection().getFileTransferControlDataInputStream().readBoolean();
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
      return session.getClient().getConnection().getFileTransferControlDataInputStream().readUTF(fileTransferBuffer);
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
      return session.getClient().getConnection().getFileTransferControlDataInputStream().readInt();
    }
    catch (Throwable e)
    {
      
    }
    return -1;
  }
  
  private boolean readNextFileChunkChecksum()
  {
    try
    {
      remoteDigest = session.getClient().getConnection().getFileTransferControlDataInputStream().readLong();
      return true;
    }
    catch (Throwable e)
    {
      
    }
    return false;
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
      if (checkFileStatus())
      {
        if (localFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_INEXISTENT)
        {
          checked = false;
        }
        if (localFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_DIRECTORY)
        {
          directory = true;
        }
        if (localFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_ANOTHER)
        {
          checked = false;
        }
        if (localFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_ERROR)
        {
          checked = false;
        }
        if (remoteFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_DIRECTORY)
        {
          
        }
        if (remoteFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_ANOTHER)
        {
          checked = false;
        }
        if (remoteFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_ERROR)
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
        if (checked && checkFileAccess(true))
        {
          if (!(localFileAccess == VTSystem.VT_FILE_TRANSFER_ACCESS_READ_AND_WRITE || localFileAccess == VTSystem.VT_FILE_TRANSFER_ACCESS_READ_ONLY))
          {
            checked = false;
          }
          if (!(remoteFileAccess == VTSystem.VT_FILE_TRANSFER_ACCESS_READ_AND_WRITE || remoteFileAccess == VTSystem.VT_FILE_TRANSFER_ACCESS_WRITE_ONLY))
          {
            checked = false;
          }
          localFileSize = 0;
          remoteFileSize = 0;
          currentOffset = 0;
          if (checked && !directory && checkFileTimes() && checkFileSizes())
          {
            if (resuming)
            {
              resumable = remoteFileStatus != VTSystem.VT_FILE_TRANSFER_STATUS_INEXISTENT &&
              ((localFileSize >= remoteFileSize && remoteFileSize > 0)
              || (remoteFileSize > localFileSize && remoteFileSize > 0));
            }
            return true;
          }
          else if (checked && directory && checkFileTimes())
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
        fileTransferFileInputStream = new FileInputStream(fileTransferRandomAccessFile.getFD());
      }
      return checkContinueTransfer(true);
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
      return checkContinueTransfer(false);
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
        //Arrays.sort(subFiles, fileSorter);
        String[] subPaths = new String[subFiles.length];
        int i = 0;
        for (File file : subFiles)
        {
          subPaths[i++] = file.getName();
        }
        for (String nextPath : subPaths)
        {
          ok = checkContinueTransfer(ok);
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
          ok = checkContinueTransfer(ok);
          if (ok)
          {
            // folder ok
            return writeNextFilePath("");
          }
        }
        else
        {
          // something wrong with last path
          ok = checkContinueTransfer(ok);
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
              ok = checkNextFileChunkChecksum(fileTransferBuffer, 0, readedBytes);
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
      if (checkFileStatus())
      {
        if (localFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_DIRECTORY)
        {
          
        }
        if (localFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_ANOTHER)
        {
          checked = false;
        }
        if (localFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_ERROR)
        {
          checked = false;
        }
        if (remoteFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_INEXISTENT)
        {
          checked = false;
        }
        if (remoteFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_DIRECTORY)
        {
          directory = true;
        }
        if (remoteFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_ANOTHER)
        {
          checked = false;
        }
        if (remoteFileStatus == VTSystem.VT_FILE_TRANSFER_STATUS_ERROR)
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
              //VTFileUtils.truncateFile(fileTransferFile);
              VTFileUtils.deleteQuietly(fileTransferFile);
              //fileTransferFile.delete();
            }
          }
        }
        if (checked && checkFileAccess(false))
        {
          if (!(localFileAccess == VTSystem.VT_FILE_TRANSFER_ACCESS_READ_AND_WRITE || localFileAccess == VTSystem.VT_FILE_TRANSFER_ACCESS_WRITE_ONLY))
          {
            checked = false;
          }
          if (!(remoteFileAccess == VTSystem.VT_FILE_TRANSFER_ACCESS_READ_AND_WRITE || remoteFileAccess == VTSystem.VT_FILE_TRANSFER_ACCESS_READ_ONLY))
          {
            checked = false;
          }
          localFileSize = 0;
          remoteFileSize = 0;
          currentOffset = 0;
          if (checked && !directory && checkFileTimes() && checkFileSizes())
          {
            if (resuming)
            {
              resumable = localFileStatus != VTSystem.VT_FILE_TRANSFER_STATUS_INEXISTENT &&
              ((remoteFileSize >= localFileSize && localFileSize > 0)
              || (localFileSize > remoteFileSize && localFileSize > 0));
            }
            return true;
          }
          else if (checked && directory && checkFileTimes())
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
        fileTransferFileOutputStream = new FileOutputStream(fileTransferRandomAccessFile.getFD());
      }
      return checkContinueTransfer(true);
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
      return checkContinueTransfer(false);
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
        List<String> remoteChildFiles = null;
        long currentFileTime = remoteFileTime;
        String rootFolder = fileNameFromPath(currentRootPath);
        String currentFolder = fileNameFromPath(currentPath);
        if (rootLevel && !currentFolder.equals(rootFolder))
        {
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
        if (deleting)
        {
          remoteChildFiles = new LinkedList<String>();
        }
        String nextPath = " ";
        while (true)
        {
          ok = checkContinueTransfer(ok);
          if (ok)
          {
            nextPath = readNextFilePath();
            if (nextPath != null)
            {
              if (!("".equals(nextPath)))
              {
                if (deleting)
                {
                  remoteChildFiles.add(nextPath);
                }
                // receive next path try subfile download
                if (rootLevel && !currentFolder.equals(rootFolder))
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
                if (rootLevel && !currentFolder.equals(rootFolder))
                {
                  fileTransferCompletedFile = new File(convertFilePath(appendToPath(currentPath, rootFolder)));
                }
                else
                {
                  fileTransferCompletedFile = new File(convertFilePath(currentPath));
                }
                if (deleting)
                {
                  List<String> localChildFiles = new LinkedList<String>();
                  String[] localChildFilesArray = fileTransferCompletedFile.list();
                  if (localChildFilesArray != null)
                  {
                    localChildFiles.addAll(Arrays.asList(localChildFilesArray));
                  }
                  localChildFiles.removeAll(remoteChildFiles);
                  for (String localChildFile : localChildFiles)
                  {
                    VTFileUtils.truncateThenDeleteQuietly(new File(fileTransferCompletedFile, localChildFile));
                  }
                }
                if (currentFileTime >= 0)
                {
                  fileTransferCompletedFile.setLastModified(currentFileTime);
                }
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
            ok = checkNextFileChunkChecksum(localFileChunkChecksums.remove(0));
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
        if (remoteFileTime >= 0)
        {
          fileTransferCompletedFile.setLastModified(remoteFileTime);
        }
        return true;
      }
      if (!fileTransferFile.renameTo(fileTransferCompletedFile))
      {
        VTFileUtils.truncateThenDeleteQuietly(fileTransferCompletedFile);
        if (fileTransferFile.renameTo(fileTransferCompletedFile))
        {
          if (remoteFileTime >= 0)
          {
            fileTransferCompletedFile = new File(convertFilePath(currentPath));
            fileTransferCompletedFile.setLastModified(remoteFileTime);
          }
          return true;
        }
        return false;
      }
      else
      {
        if (remoteFileTime >= 0)
        {
          fileTransferCompletedFile.setLastModified(remoteFileTime);
        }
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
        fileTransferChecksumInputStream = new DigestInputStream(new FileInputStream(fileTransferRandomAccessFile.getFD()), messageDigest);
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
        messageDigest.reset();
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
      interrupted = false;
      String[] splitCommand = CommandLineTokenizerMKII.tokenize(command);
      if (splitCommand.length < 4)
      {
        synchronized (this)
        {
          VTMainConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
          finished = true;
          return;
        }
      }
      if (splitCommand[0].equalsIgnoreCase("*VTFILETRANSFER") || splitCommand[0].equalsIgnoreCase("*VTFT"))
      {
        parameters = splitCommand[1];
        destination = splitCommand[splitCommand.length - 1];
        destination = normalizePath(destination);
        source = "";
        compressing = false;
        resuming = false;
        deleting = false;
//        verifying = false;
        heavier = false;
        for (int i = 2; i < splitCommand.length - 1; i++)
        {
          source += ";" + splitCommand[i].trim();
        }
        source = source.substring(1);
        if (parameters.toUpperCase().contains("P"))
        {
          filePaths = source;
          if (parameters.toUpperCase().contains("Q"))
          {
            compressing = true;
            heavier = false;
          }
          if (parameters.toUpperCase().contains("H"))
          {
            compressing = true;
            heavier = true;
          }
          if (parameters.toUpperCase().contains("R"))
          {
            resuming = true;
          }
          if (parameters.toUpperCase().contains("O"))
          {
            resuming = false;
          }
          if (parameters.toUpperCase().contains("D"))
          {
            deleting = true;
          }
//          if (parameters.toUpperCase().contains("V"))
//          {
//            verifying = true;
//          }
          
          String[] localFiles = filePaths.split(";");
          
          if (compressing)
          {
            if (heavier)
            {
              fileTransferRemoteOutputStream = VTCompressorSelector.createBufferedZstdOutputStream(session.getClient().getConnection().getFileTransferDataOutputStream());
            }
            else
            {
              fileTransferRemoteOutputStream = VTCompressorSelector.createBufferedLz4OutputStream(session.getClient().getConnection().getFileTransferDataOutputStream());
            }
          }
          else
          {
            fileTransferRemoteOutputStream = session.getClient().getConnection().getFileTransferDataOutputStream();
          }
          
          for (String localFile : localFiles)
          {
            localFile = normalizePath(localFile);
            currentRootPath = localFile;
            if (tryUpload(localFile))
            {
              if (!session.getClient().getConnection().isConnected())
              {
                finished = true;
                return;
              }
            }
            else
            {
              if (!session.getClient().getConnection().isConnected())
              {
                return;
              }
              synchronized (this)
              {
                if (interrupted)
                {
                  VTMainConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + source + "]" + "\nVT>Remote file: [" + destination + "]\nVT>");
                }
                else
                {
                  VTMainConsole.print("\nVT>File transfer failed!" + "\nVT>Local file: [" + source + "]" + "\nVT>Remote file: [" + destination + "]\nVT>");
                }
                finished = true;
              }
              return;
            }
          }
          synchronized (this)
          {
            if (!stopped && session.getClient().getConnection().isConnected())
            {
              if (interrupted)
              {
                VTMainConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + source + "]" + "\nVT>Remote file: [" + destination + "]\nVT>");
              }
              else
              {
                VTMainConsole.print("\nVT>File transfer completed!" + "\nVT>Local file: [" + source + "]" + "\nVT>Remote file: [" + destination + "]\nVT>");
              }
            }
            else
            {
              VTMainConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + source + "]" + "\nVT>Remote file: [" + destination + "]\nVT>");
            }
            finished = true;
          }
        }
        else if (parameters.toUpperCase().contains("G"))
        {
          filePaths = source;
          if (parameters.toUpperCase().contains("Q"))
          {
            compressing = true;
            heavier = false;
          }
          if (parameters.toUpperCase().contains("H"))
          {
            compressing = true;
            heavier = true;
          }
          if (parameters.toUpperCase().contains("R"))
          {
            resuming = true;
          }
          if (parameters.toUpperCase().contains("O"))
          {
            resuming = false;
          }
          if (parameters.toUpperCase().contains("D"))
          {
            deleting = true;
          }
//          if (parameters.toUpperCase().contains("V"))
//          {
//            verifying = true;
//          }
          
          if (compressing)
          {
            if (heavier)
            {
              fileTransferRemoteInputStream = VTCompressorSelector.createBufferedZstdInputStream(session.getClient().getConnection().getFileTransferDataInputStream());
            }
            else
            {
              fileTransferRemoteInputStream = VTCompressorSelector.createBufferedLz4InputStream(session.getClient().getConnection().getFileTransferDataInputStream());
            }
          }
          else
          {
            fileTransferRemoteInputStream = session.getClient().getConnection().getFileTransferDataInputStream();
          }
          
          String[] remoteFiles = filePaths.split(";");
          for (String remoteFile : remoteFiles)
          {
            remoteFile = normalizePath(remoteFile);
            currentRootPath = remoteFile;
            if (tryDownload(destination, true))
            {
              if (!session.getClient().getConnection().isConnected())
              {
                finished = true;
                return;
              }
            }
            else
            {
              if (!session.getClient().getConnection().isConnected())
              {
                return;
              }
              synchronized (this)
              {
                if (session.getClient().getConnection().isConnected())
                {
                  if (interrupted)
                  {
                    VTMainConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + destination + "]" + "\nVT>Remote file: [" + source + "]\nVT>");
                  }
                  else
                  {
                    VTMainConsole.print("\nVT>File transfer failed!" + "\nVT>Local file: [" + destination + "]" + "\nVT>Remote file: [" + source + "]\nVT>");
                  }
                }
                finished = true;
              }
              return;
            }
          }
          synchronized (this)
          {
            if (session.getClient().getConnection().isConnected())
            {
              if (!stopped)
              {
                if (interrupted)
                {
                  VTMainConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + destination + "]" + "\nVT>Remote file: [" + source + "]\nVT>");
                }
                else
                {
                  VTMainConsole.print("\nVT>File transfer completed!" + "\nVT>Local file: [" + destination + "]" + "\nVT>Remote file: [" + source + "]\nVT>");
                }
              }
              else
              {
                VTMainConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + destination + "]" + "\nVT>Remote file: [" + source + "]\nVT>");
              }
            }
            finished = true;
          }
        }
        else
        {
          synchronized (this)
          {
            if (session.getClient().getConnection().isConnected())
            {
              VTMainConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
            }
            finished = true;
          }
        }
      }
      else
      {
        synchronized (this)
        {
          if (session.getClient().getConnection().isConnected())
          {
            VTMainConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
          }
          finished = true;
        }
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