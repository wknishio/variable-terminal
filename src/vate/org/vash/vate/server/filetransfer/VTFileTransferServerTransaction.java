package org.vash.vate.server.filetransfer;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.security.DigestInputStream;
import java.security.MessageDigest;
//import java.util.zip.CheckedInputStream;
//import java.util.zip.Checksum;
import org.vash.vate.VT;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.security.VTBlake3MessageDigest;
import org.vash.vate.stream.compress.VTCompressorSelector;

import com.martiansoftware.jsap.CommandLineTokenizer;

//import net.jpountz.xxhash.XXHashFactory;

public class VTFileTransferServerTransaction implements Runnable
{
  private static final int fileTransferBufferSize = VT.VT_FILE_DATA_BUFFER_SIZE;
  private volatile boolean stopped;
  private volatile boolean finished;
  private volatile boolean compressing;
  private volatile boolean resuming;
  private volatile boolean verifying;
  private volatile boolean checked;
  private volatile boolean resumable;
  private volatile boolean directory;
  private volatile boolean stronger;
  // private static final int checksumBufferSize = 64 * 1024;
  private int readedBytes;
  private int writtenBytes;
  private int bufferedBytes;
  private int remoteFileStatus;
  private int localFileStatus;
  private int remoteFileAccess;
  private int localFileAccess;
  // private long remoteChecksum;
  // private long localChecksum;
  private byte[] localChecksum = new byte[64];
  private byte[] remoteChecksum = new byte[64];
  private long remoteFileSize;
  private long localFileSize;
  private long maxOffset;
  private long currentOffset;
  // private volatile long transferDataSize;
  // private volatile long transferDataCount;
  // private volatile long transferFileCount;
  // private volatile long transferDirectoryCount;
  private final byte[] fileTransferBuffer = new byte[fileTransferBufferSize];
  // private final byte[] checksumBuffer = new byte[checksumBufferSize];
  // private Checksum checksum =
  // XXHashFactory.fastestJavaInstance().newStreamingHash64(-1).asChecksum();
  private MessageDigest checksum = new VTBlake3MessageDigest();
  private String command;
  // private String source;
  private String destination;
  private String filePaths;
  private String currentFilePath;
  // private volatile String localFilePath;
  // private volatile String remoteFilePath;
  private String transferParameters;
  // private String[] splitCommand;
  private File fileTransferFile;
  private File fileTransferCompletedFile;
  private RandomAccessFile fileTransferRandomAccessFile;
  // private FileLock fileLock;
  // private CheckedInputStream fileTransferChecksumInputStream;
  private DigestInputStream fileTransferChecksumInputStream;
  private InputStream fileTransferRemoteInputStream;
  private OutputStream fileTransferRemoteOutputStream;
  private InputStream fileTransferFileInputStream;
  private OutputStream fileTransferFileOutputStream;
  private VTFileTransferServerSession session;
  
  public VTFileTransferServerTransaction(VTFileTransferServerSession session)
  {
    this.session = session;
    this.finished = true;
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
  
//  public long getTransferDataSize()
//  {
//    return transferDataSize;
//  }
//
//  public long getTransferDataCount()
//  {
//    return transferDataCount;
//  }
//
//  public long getTransferFileCount()
//  {
//    return transferFileCount;
//  }
//
//  public long getTransferDirectoryCount()
//  {
//    return transferDirectoryCount;
//  }
  
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
  
  private boolean getFileChecksums()
  {
    // System.out.println("getFileChecksums:" +
    // fileTransferFile.getAbsolutePath());
    return (writeLocalFileChecksum() && readRemoteFileChecksum());
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
          localFileStatus = VT.VT_FILE_TRANSFER_FILE_TYPE_UNKNOWN;
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
        // System.out.println("fileTransferRandomAccessFile:" +
        // fileTransferFile.getAbsolutePath());
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
      // codec.writeLong(localFileSize,
      // session.getServer().getConnection().getFileTransferControlOutputStream());
      session.getServer().getConnection().getFileTransferControlDataOutputStream().writeLong(localFileSize);
      session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
      if (localFileSize < 0)
      {
        // return false;
      }
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  private boolean writeLocalFileChecksum()
  {
    checksum.reset();
    currentOffset = 0;
    // localChecksum = 0;
    // Arrays.fill(localChecksum, (byte) 0);
    try
    {
      fileTransferRandomAccessFile.seek(0);
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      // localChecksum = -1;
    }
    try
    {
      if (fileTransferChecksumInputStream == null)
      {
        // fileTransferChecksumInputStream = new
        // CheckedInputStream(Channels.newInputStream(fileTransferRandomAccessFile.getChannel()),
        // checksum);
        fileTransferChecksumInputStream = new DigestInputStream(Channels.newInputStream(fileTransferRandomAccessFile.getChannel()), checksum);
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
      }
      // localChecksum =
      // fileTransferChecksumInputStream.getChecksum().getValue() &
      // Long.MAX_VALUE;
      localChecksum = fileTransferChecksumInputStream.getMessageDigest().digest();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      // localChecksum = -1;
    }
    try
    {
      fileTransferRandomAccessFile.seek(0);
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      // localChecksum = -1;
    }
    currentOffset = 0;
    try
    {
      // session.getServer().getConnection().getFileTransferControlDataOutputStream().writeLong(localChecksum);
      session.getServer().getConnection().getFileTransferControlDataOutputStream().write(localChecksum);
      session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
      // System.out.println("localChecksum:" + Arrays.toString(localChecksum));
      // System.out.println("localChecksum.length:" + localChecksum.length);
      return true;
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    return false;
  }
  
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
        session.getServer().getConnection().getFileTransferControlDataOutputStream().writeInt(-1);
        session.getServer().getConnection().getFileTransferControlDataOutputStream().flush();
        return true;
      }
      byte[] data = path.getBytes("UTF-8");
      session.getServer().getConnection().getFileTransferControlDataOutputStream().writeInt(data.length);
      session.getServer().getConnection().getFileTransferControlDataOutputStream().write(data);
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
  
  private boolean readRemoteFileChecksum()
  {
    try
    {
      // remoteChecksum =
      // session.getServer().getConnection().getFileTransferControlDataInputStream().readLong();
      session.getServer().getConnection().getFileTransferControlDataInputStream().readFully(remoteChecksum);
      // System.out.println("remoteChecksum:" +
      // Arrays.toString(remoteChecksum));
      // System.out.println("remoteChecksum.length:" + remoteChecksum.length);
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
      int length = session.getServer().getConnection().getFileTransferControlDataInputStream().readInt();
      if (length < 0)
      {
        return null;
      }
      byte[] data = new byte[length];
      session.getServer().getConnection().getFileTransferControlDataInputStream().readFully(data);
      return new String(data, "UTF-8");
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
    // System.out.println("tryUpload: " + currentPath);
    if (verifying)
    {
      boolean folder = false;
      boolean ok = checkUpload(currentPath) && setUploadStreams(currentPath);
      folder = directory && ok;
      ok = ok && uploadFilePath(currentPath);
      if (!ok)
      {
        cleanUpload();
        return false;
      }
      else
      {
        if (folder)
        {
          cleanUpload();
          return true;
        }
        if (localFileSize > remoteFileSize)
        {
          remoteFileSize = localFileSize;
        }
        else if (remoteFileSize > localFileSize)
        {
          localFileSize = remoteFileSize;
        }
        if (getFileChecksums())
        {
          if (VTArrayComparator.arrayEquals(localChecksum, remoteChecksum))
          {
            cleanUpload();
            return true;
          }
          else
          {
            cleanUpload();
            return false;
          }
        }
        else
        {
          cleanUpload();
          return false;
        }
      }
    }
    else
    {
      boolean ok = checkUpload(currentPath) && setUploadStreams(currentPath) && uploadFilePath(currentPath);
      cleanUpload();
      return ok;
    }
  }
  
  private boolean checkUpload(String currentPath)
  {
    checked = true;
    directory = false;
    resumable = false;
    fileTransferFile = new File(convertFilePath(currentPath));
    if (!fileTransferFile.isAbsolute())
    {
      fileTransferFile = new File(convertFilePath(currentPath));
    }
    // System.out.println("verifyUpload: " +
    // fileTransferFile.getAbsolutePath());
    try
    {
      if (getFileStatus())
      {
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Local
          // file
          // is a directoy!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          // verified = false;
          // if (currentPath == null)
          // {
          // remoteFilePath = appendToPath(remoteFilePath,
          // getFileNameFromPath(localFilePath));
          // }
        }
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_UNKNOWN)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Local
          // file
          // is of unknown type!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          checked = false;
        }
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Error
          // found while analyzing local file!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          checked = false;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_NOT_FOUND)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Remote
          // file not found!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          checked = false;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Remote
          // file is a directory!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          // verified = false;
          directory = true;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_UNKNOWN)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Remote
          // file is of unknown type!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          checked = false;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Error
          // found while analyzing remote file!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          checked = false;
        }
        if (checked)
        {
          if (!directory)
          {
            // transferFileCount++;
          }
          else
          {
            // transferDirectoryCount++;
          }
        }
        if (checked && getFileAccess(true))
        {
          if (!(remoteFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE || remoteFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_WRITE_ONLY))
          {
            // session.getServer().getConnection().getResultWriter().write("\nVT>Local
            // file
            // is not writable!\nVT>");
            // session.getServer().getConnection().getResultWriter().flush();
            checked = false;
          }
          if (!(localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE || localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_ONLY))
          {
            // session.getServer().getConnection().getResultWriter().write("\nVT>Remote
            // file is not readable!\nVT>");
            // session.getServer().getConnection().getResultWriter().flush();
            checked = false;
          }
          localFileSize = 0;
          remoteFileSize = 0;
          currentOffset = 0;
          if (checked && !directory && getFileSizes())
          {
            resumable = false;
            if (resuming)
            {
              if (localFileSize >= remoteFileSize && remoteFileSize >= 0)
              {
                if (getFileChecksums())
                {
                  if (remoteFileStatus != VT.VT_FILE_TRANSFER_FILE_NOT_FOUND && VTArrayComparator.arrayEquals(localChecksum, remoteChecksum))
                  {
                    resumable = true;
                  }
                  else
                  {
                    
                  }
                }
                resumable = getContinueTransfer(resumable);
                if (resumable)
                {
                  currentOffset = remoteFileSize;
                }
              }
              else if (remoteFileSize > localFileSize && remoteFileSize >= 0)
              {
                if (getFileChecksums())
                {
                  if (remoteFileStatus != VT.VT_FILE_TRANSFER_FILE_NOT_FOUND && VTArrayComparator.arrayEquals(localChecksum, remoteChecksum))
                  {
                    resumable = true;
                  }
                  else
                  {
                    
                  }
                }
                resumable = getContinueTransfer(resumable);
                if (resumable)
                {
                  currentOffset = localFileSize;
                }
              }
            }
            // transferDataSize += localFileSize - currentOffset;
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
        if (resumable)
        {
          fileTransferRandomAccessFile.seek(remoteFileSize);
        }
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
            fileTransferRemoteOutputStream.write(fileTransferBuffer, 0, readedBytes);
            fileTransferRemoteOutputStream.flush();
          }
        }
        currentOffset += readedBytes;
        // transferDataCount += readedBytes;
      }
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
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
    if (fileTransferRandomAccessFile != null)
    {
      try
      {
        fileTransferRandomAccessFile.close();
      }
      catch (Throwable e)
      {
        
      }
      // System.out.println("cleanUpload:" +
      // fileTransferFile.getAbsolutePath());
      fileTransferRandomAccessFile = null;
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
  }
  
  private boolean tryDownload(String currentPath, boolean rootLevel)
  {
    // System.out.println("tryDownload: " + currentPath);
    if (verifying)
    {
      boolean folder = false;
      boolean ok = checkDownload(currentPath) && setDownloadStreams(currentPath);
      folder = directory && ok;
      ok = ok && downloadFilePath(currentPath, rootLevel);
      if (!ok)
      {
        cleanDownload();
        return false;
      }
      else
      {
        if (folder)
        {
          cleanDownload();
          return true;
        }
        if (localFileSize > remoteFileSize)
        {
          remoteFileSize = localFileSize;
        }
        else if (remoteFileSize > localFileSize)
        {
          localFileSize = remoteFileSize;
        }
        if (getFileChecksums())
        {
          if (VTArrayComparator.arrayEquals(localChecksum, remoteChecksum))
          {
            ok = replaceDownloadFile(currentPath);
            cleanDownload();
            return ok;
          }
          else
          {
            cleanDownload();
            return false;
          }
        }
        else
        {
          cleanDownload();
          return false;
        }
      }
    }
    else
    {
      boolean ok = checkDownload(currentPath) && setDownloadStreams(currentPath) && downloadFilePath(currentPath, rootLevel) && replaceDownloadFile(currentPath);
      cleanDownload();
      return ok;
    }
  }
  
  private boolean checkDownload(String currentPath)
  {
    checked = true;
    directory = false;
    resumable = false;
    fileTransferFile = new File(convertFilePath(currentPath));
    if (!fileTransferFile.isAbsolute())
    {
      fileTransferFile = new File(convertFilePath(currentPath));
    }
    // System.out.println("verifyDownload: " +
    // fileTransferFile.getAbsolutePath());
    try
    {
      if (getFileStatus())
      {
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_NOT_FOUND)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Local
          // file
          // not found!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          checked = false;
        }
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
        {
          // session.getServer().getConnection()V.getResultWriter().write("\nVT>Local
          // file is a directory!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          // verified = false;
          directory = true;
        }
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_UNKNOWN)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Local
          // file
          // is of unknown type!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          checked = false;
        }
        if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Error
          // found while analyzing local file!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          checked = false;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Remote
          // file is a directory!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          // verified = false;
          // if (currentPath == null)
          // {
          // localFilePath = appendToPath(localFilePath,
          // getFileNameFromPath(remoteFilePath));
          // }
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_UNKNOWN)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Remote
          // file is of unknown type!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          checked = false;
        }
        if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
        {
          // session.getServer().getConnection().getResultWriter().write("\nVT>Error
          // found while analyzing remote file!\nVT>");
          // session.getServer().getConnection().getResultWriter().flush();
          checked = false;
        }
        if (checked)
        {
          if (!directory)
          {
            // transferFileCount++;
            if (!fileTransferFile.exists())
            {
              fileTransferFile = new File(convertFilePath(currentPath + ".tmp"));
              if (!fileTransferFile.isAbsolute())
              {
                fileTransferFile = new File(convertFilePath(currentPath + ".tmp"));
              }
            }
          }
          else
          {
            // transferDirectoryCount++;
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
            // session.getServer().getConnection().getResultWriter().write("\nVT>Local
            // file
            // is not readable!\nVT>");
            // session.getServer().getConnection().getResultWriter().flush();
            checked = false;
          }
          if (!(localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE || localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_WRITE_ONLY))
          {
            // session.getServer().getConnection().getResultWriter().write("\nVT>Remote
            // file is not writable!\nVT>");
            // session.getServer().getConnection().getResultWriter().flush();
            checked = false;
          }
          localFileSize = 0;
          remoteFileSize = 0;
          currentOffset = 0;
          if (checked && !directory && getFileSizes())
          {
            resumable = false;
            if (resuming)
            {
              if (remoteFileSize >= localFileSize && localFileSize >= 0)
              {
                if (getFileChecksums())
                {
                  if (localFileStatus != VT.VT_FILE_TRANSFER_FILE_NOT_FOUND && VTArrayComparator.arrayEquals(localChecksum, remoteChecksum))
                  {
                    resumable = true;
                  }
                  else
                  {
                    
                  }
                }
                resumable = getContinueTransfer(resumable);
                if (resumable)
                {
                  currentOffset = localFileSize;
                }
              }
              else if (localFileSize > remoteFileSize && localFileSize >= 0)
              {
                if (getFileChecksums())
                {
                  if (localFileStatus != VT.VT_FILE_TRANSFER_FILE_NOT_FOUND && VTArrayComparator.arrayEquals(localChecksum, remoteChecksum))
                  {
                    try
                    {
                      fileTransferRandomAccessFile.setLength(remoteFileSize);
                      resumable = true;
                    }
                    catch (Throwable t)
                    {
                      
                    }
                  }
                  else
                  {
                    
                  }
                }
                resumable = getContinueTransfer(resumable);
                if (resumable)
                {
                  currentOffset = remoteFileSize;
                }
              }
            }
            // transferDataSize += remoteFileSize - currentOffset;
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
        if (resumable)
        {
          fileTransferRandomAccessFile.seek(localFileSize);
        }
        fileTransferFileOutputStream = Channels.newOutputStream(fileTransferRandomAccessFile.getChannel());
      }
      return getContinueTransfer(true);
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
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
        return ok;
      }
      else
      {
        String rootFolder = null;
        if (rootLevel)
        {
          rootFolder = getFileNameFromPath(this.currentFilePath);
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
        // String subPath = appendToPath(localFilePath, currentPath);
        while (true)
        {
          ok = getContinueTransfer(ok);
          if (ok)
          {
            nextPath = readNextFilePath();
            // System.out.println("nextPath:" + nextPath);
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
          bufferedBytes = 0;
          while (!stopped && ok && writtenBytes > 0)
          {
            readedBytes = fileTransferRemoteInputStream.read(fileTransferBuffer, bufferedBytes, writtenBytes);
            if (readedBytes >= 0)
            {
              writtenBytes -= readedBytes;
              currentOffset += readedBytes;
              bufferedBytes += readedBytes;
              // transferDataCount += readedBytes;
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
      // t.printStackTrace();
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
    if (fileTransferRandomAccessFile != null)
    {
      try
      {
        fileTransferRandomAccessFile.close();
      }
      catch (Throwable e)
      {
        
      }
      // System.out.println("cleanDownload:" +
      // fileTransferFile.getAbsolutePath());
      fileTransferRandomAccessFile = null;
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
      if (!fileTransferCompletedFile.isAbsolute())
      {
        fileTransferCompletedFile = new File(convertFilePath(currentPath));
      }
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
  
  private static String getFileNameFromPath(String path)
  {
    int idx = path.replaceAll("\\\\", "/").lastIndexOf("/");
    return idx >= 0 ? path.substring(idx + 1) : path;
  }
  
  private static String appendToPath(String path, String append)
  {
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
      // stopped = false;
      // finished = false;
      // transferDataSize = 0;
      // transferDataCount = 0;
      // transferFileCount = 0;
      // transferDirectoryCount = 0;
      String[] splitCommand = CommandLineTokenizer.tokenize(command);
      if (splitCommand.length < 4)
      {
        // stopped = true;
        
        finished = true;
        return;
      }
      if (splitCommand[0].equalsIgnoreCase("*VTFILETRANSFER") || splitCommand[0].equalsIgnoreCase("*VTFT"))
      {
        // source = splitCommand[2];
        destination = splitCommand[3];
        transferParameters = splitCommand[1];
        if (transferParameters.toUpperCase().contains("P"))
        {
          filePaths = splitCommand[2];
          // localFilePath = splitCommand[3];
          // remoteFilePath = splitCommand[2];
          compressing = false;
          resuming = false;
          verifying = false;
          stronger = false;
          
          if (transferParameters.toUpperCase().contains("F"))
          {
            compressing = true;
          }
          if (transferParameters.toUpperCase().contains("H"))
          {
            compressing = true;
            stronger = true;
          }
          if (transferParameters.toUpperCase().contains("R"))
          {
            resuming = true;
          }
          if (transferParameters.toUpperCase().contains("V"))
          {
            verifying = true;
          }
          
          if (compressing)
          {
            if (stronger)
            {
              fileTransferRemoteInputStream = VTCompressorSelector.createDirectZstdInputStream(session.getServer().getConnection().getFileTransferDataInputStream());
            }
            else
            {
              fileTransferRemoteInputStream = VTCompressorSelector.createDirectLz4InputStream(session.getServer().getConnection().getFileTransferDataInputStream());
            }
          }
          else
          {
            fileTransferRemoteInputStream = session.getServer().getConnection().getFileTransferDataInputStream();
          }
          
          String[] remoteFiles = filePaths.split(";");
          for (String remoteFile : remoteFiles)
          {
            // this.localFilePath = destination;
            // this.remoteFilePath = remoteFile;
            this.currentFilePath = remoteFile;
            if (tryDownload(destination, true))
            {
              // session.getServer().getConnection().getResultWriter().write("\nVT>File
              // transfer completed!\nVT>");
              // session.getServer().getConnection().getResultWriter().flush();
            }
            else
            {
              // session.getServer().getConnection().getResultWriter().write("\nVT>File
              // transfer failed!\nVT>");
              // session.getServer().getConnection().getResultWriter().flush();
            }
          }
        }
        else if (splitCommand[1].toUpperCase().contains("G"))
        {
          filePaths = splitCommand[2];
          // localFilePath = splitCommand[2];
          // remoteFilePath = splitCommand[3];
          compressing = false;
          resuming = false;
          verifying = false;
          stronger = false;
          
          if (transferParameters.toUpperCase().contains("F"))
          {
            compressing = true;
          }
          if (transferParameters.toUpperCase().contains("H"))
          {
            compressing = true;
            stronger = true;
          }
          if (transferParameters.toUpperCase().contains("R"))
          {
            resuming = true;
          }
          if (transferParameters.toUpperCase().contains("V"))
          {
            verifying = true;
          }
          
          if (compressing)
          {
            if (stronger)
            {
              fileTransferRemoteOutputStream = VTCompressorSelector.createDirectZstdOutputStream(session.getServer().getConnection().getFileTransferDataOutputStream());
            }
            else
            {
              fileTransferRemoteOutputStream = VTCompressorSelector.createDirectLz4OutputStream(session.getServer().getConnection().getFileTransferDataOutputStream());
            }
          }
          else
          {
            fileTransferRemoteOutputStream = session.getServer().getConnection().getFileTransferDataOutputStream();
          }
          
          String[] localFiles = filePaths.split(";");
          for (String localFile : localFiles)
          {
            // this.remoteFilePath = destination;
            // this.localFilePath = localFile;
            this.currentFilePath = localFile;
            if (tryUpload(localFile))
            {
              // session.getServer().getConnection().getResultWriter().write("\nVT>File
              // transfer completed!\nVT>");
              // session.getServer().getConnection().getResultWriter().flush();
            }
            else
            {
              // session.getServer().getConnection().getResultWriter().write("\nVT>File
              // transfer failed!\nVT>");
              // session.getServer().getConnection().getResultWriter().flush();
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