package org.vate.client.filetransfer;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import org.vate.VT;
import org.vate.console.VTConsole;
import org.vate.help.VTHelpManager;

import com.martiansoftware.jsap.CommandLineTokenizer;

import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.xxhash.XXHashFactory;

public class VTFileTransferClientTransaction implements Runnable
{
	private static final int fileTransferBufferSize = 1024 * 32;
	private volatile boolean stopped;
	private volatile boolean finished;
	private volatile boolean interrupted;
	private boolean compression;
	private boolean resume;
	private boolean check;
	private boolean verified;
	private boolean directory;
	private boolean resumable;
	// private static final int checksumBufferSize = 64 * 1024;
	private int readedBytes;
	private int writtenBytes;
	private int remoteFileStatus;
	private int localFileStatus;
	private int remoteFileAccess;
	private int localFileAccess;
	private long remoteChecksum;
	private long localChecksum;
	private long remoteFileSize;
	private long localFileSize;
	private long maxOffset;
	private long currentOffset;
	private volatile long transferDataSize;
	private volatile long transferDataCount;
	private volatile long transferFileCount;
	private volatile long transferDirectoryCount;
	private final byte[] fileTransferBuffer = new byte[fileTransferBufferSize];
	// private final byte[] checksumBuffer = new byte[checksumBufferSize];
	private Checksum checksum = XXHashFactory.fastestJavaInstance().newStreamingHash32(-1).asChecksum();
	private String command;
	private String source;
	private String destination;
	private String filePaths;
	private volatile String filePath;
	//private volatile String localFilePath;
	//private volatile String remoteFilePath;
	private String transferParameters;
	// private String[] splitCommand;
	private File fileTransferFile;
	private File fileTransferFinalFile;
	private RandomAccessFile fileTransferRandomAccessFile;
	// private FileLock fileLock;
	private CheckedInputStream fileTransferChecksumInputStream;
	private InputStream fileTransferRemoteInputStream;
	private OutputStream fileTransferRemoteOutputStream;
	private InputStream fileTransferFileInputStream;
	private OutputStream fileTransferFileOutputStream;
	private VTFileTransferClientSession session;
	
	public VTFileTransferClientTransaction(VTFileTransferClientSession session)
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
	
	public long getTransferDataSize()
	{
		return transferDataSize;
	}
	
	public long getTransferDataCount()
	{
		return transferDataCount;
	}
	
	public long getTransferFileCount()
	{
		return transferFileCount;
	}
	
	public long getTransferDirectoryCount()
	{
		return transferDirectoryCount;
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
	
	private boolean getFileChecksums()
	{
		return (writeLocalFileChecksum() && readRemoteFileChecksum() && localChecksum >= 0);
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
					localFileStatus = VT.VT_FILE_TRANSFER_FILE_TYPE_FILE;
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
		localChecksum = 0;
		try
		{
			fileTransferRandomAccessFile.seek(0);
		}
		catch (Throwable e)
		{
			localChecksum = -1;
		}
		try
		{
			if (fileTransferChecksumInputStream == null)
			{
				fileTransferChecksumInputStream = new CheckedInputStream(Channels.newInputStream(fileTransferRandomAccessFile.getChannel()), checksum);
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
				if (currentOffset + fileTransferBufferSize >= maxOffset)
				{
					readedBytes = fileTransferChecksumInputStream.read(fileTransferBuffer, 0, Long.valueOf(maxOffset - currentOffset).intValue());
				}
				else
				{
					readedBytes = fileTransferChecksumInputStream.read(fileTransferBuffer, 0, fileTransferBufferSize);
				}
				if (readedBytes < 0)
				{
					break;
				}
				currentOffset += readedBytes;
			}
			localChecksum = fileTransferChecksumInputStream.getChecksum().getValue() & Long.MAX_VALUE;
		}
		catch (Throwable e)
		{
			localChecksum = -1;
		}
		try
		{
			fileTransferRandomAccessFile.seek(0);
		}
		catch (Throwable e)
		{
			localChecksum = -1;
		}
		currentOffset = 0;
		try
		{
			session.getClient().getConnection().getFileTransferControlDataOutputStream().writeLong(localChecksum);
			session.getClient().getConnection().getFileTransferControlDataOutputStream().flush();
			if (localChecksum < 0)
			{
				// return false;
			}
			return true;
		}
		catch (Throwable e)
		{
			
		}
		return false;
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
				session.getClient().getConnection().getFileTransferControlDataOutputStream().writeInt(-1);
				session.getClient().getConnection().getFileTransferControlDataOutputStream().flush();
				return true;
			}
			byte[] data = path.getBytes("UTF-8");
			session.getClient().getConnection().getFileTransferControlDataOutputStream().writeInt(data.length);
			session.getClient().getConnection().getFileTransferControlDataOutputStream().write(data);
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
	
	private boolean readRemoteFileChecksum()
	{
		try
		{
			remoteChecksum = session.getClient().getConnection().getFileTransferControlDataInputStream().readLong();
			if (remoteChecksum < 0)
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
			int length = session.getClient().getConnection().getFileTransferControlDataInputStream().readInt();
			if (length < 0)
			{
				return null;
			}
			byte[] data = new byte[length];
			session.getClient().getConnection().getFileTransferControlDataInputStream().readFully(data);
			return new String(data, "UTF-8");
		}
		catch (Throwable e)
		{
			
		}
		return null;
	}
	
	private boolean tryUpload(String currentPath)
	{
		// VTConsole.print("\nVT>Trying to send file to server...\nVT>");
		//System.out.println("tryUpload: " + currentPath);
		if (verifyUpload(currentPath))
		{
			return (setUploadStreams(currentPath) && uploadFilePath(currentPath));
		}
		return false;
	}
	
	private boolean verifyUpload(String currentPath)
	{
		verified = true;
		directory = false;
		resumable = false;
		fileTransferFile = new File(convertFilePath(currentPath));
		if (!fileTransferFile.isAbsolute())
		{
			fileTransferFile = new File(convertFilePath(currentPath));
		}
		//System.out.println("verifyUpload: " + fileTransferFile.getAbsolutePath());
		try
		{
			if (getFileStatus())
			{
				if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_NOT_FOUND)
				{
					// VTConsole.print("\nVT>Local file for transfer not
					// found!\nVT>");
					verified = false;
				}
				if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
				{
					directory = true;
				}
				if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_UNKNOWN)
				{
					// VTConsole.print("\nVT>Local file for transfer is of
					// unknown type!\nVT>");
					verified = false;
				}
				if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
				{
					// VTConsole.print("\nVT>Local file for transfer has
					// error!\nVT>");
					verified = false;
				}
				if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
				{
					//if (currentPath == null)
					//{
						//remoteFilePath = appendToPath(remoteFilePath, getFileNameFromPath(localFilePath));
					//}
				}
				if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_UNKNOWN)
				{
					// VTConsole.print("\nVT>Remote file for transfer is of
					// unknown type!\nVT>");
					verified = false;
				}
				if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
				{
					// VTConsole.print("\nVT>Remote file for transfer has
					// error!\nVT>");
					verified = false;
				}
				if (verified)
				{
					if (!directory)
					{
						transferFileCount++;
					}
					else
					{
						transferDirectoryCount++;
					}
				}
				if (verified && getFileAccess(true))
				{
					if (!(localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE || localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_ONLY))
					{
						// VTConsole.print("\nVT>Local file for transfer is
						// not readable!\nVT>");
						verified = false;
					}
					if (!(remoteFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE || remoteFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_WRITE_ONLY))
					{
						// VTConsole.print("\nVT>Remote file for transfer is
						// not writable!\nVT>");
						verified = false;
					}
					localFileSize = 0;
					remoteFileSize = 0;
					currentOffset = 0;
					if (verified && !directory && getFileSizes())
					{
						resumable = false;
						if (resume)
						{
							if (localFileSize >= remoteFileSize && remoteFileSize >= 0)
							{
								if (check)
								{
									
									// VTConsole.print("\nVT>Checking file
									// transfer resume possibility...\nVT>");
									if (getFileChecksums())
									{
										if (remoteFileStatus != VT.VT_FILE_TRANSFER_FILE_NOT_FOUND && localChecksum == remoteChecksum)
										{
											resumable = true;
											currentOffset = remoteFileSize;
											// VTConsole.print("\nVT>File
											// transfer resume
											// possible!\nVT>");
										}
										else
										{
											// VTConsole.print("\nVT>File
											// transfer resume not
											// possible!\nVT>");
										}
									}
								}
								else
								{
									resumable = true;
									currentOffset = remoteFileSize;
									// VTConsole.print("\nVT>Assuming file
									// transfer resume as possible...\nVT>");
								}
							}
						}
						transferDataSize += localFileSize - currentOffset;
						// VTConsole.print("\nVT>Local file for transfer size:
						// [" + localFileSize + "] bytes\nVT>");
						return true;
					}
					else if (verified && directory)
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
			//e.printStackTrace();
			try
			{
				fileTransferRemoteOutputStream.close();
			}
			catch (Throwable e1)
			{
				
			}
			try
			{
				fileTransferFileInputStream.close();
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
				if (check)
				{
					// VTConsole.print("\nVT>Verifying sent file
					// integrity...\nVT>");
					remoteFileSize = localFileSize;
					if (getFileChecksums())
					{
						if (localChecksum == remoteChecksum)
						{
							// VTConsole.print("\nVT>Sent file integrity
							// verified!\nVT>");
						}
						else
						{
							// VTConsole.print("\nVT>Sent file integrity
							// corrupted!\nVT>");
							// fileTransferFileInputStream.close();
							return false;
						}
					}
					else
					{
						// VTConsole.print("\nVT>failed to verify sent file
						// integrity!\nVT>");
						// fileTransferFileInputStream.close();
						return false;
					}
				}
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
				// return
				// VTArchiveUtils.createTarOutputStream(fileTransferOutputStream,
				// fileTransferBuffer, paths);
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
		finally
		{
			if (fileTransferFileInputStream != null)
			{
				try
				{
					fileTransferFileInputStream.close();
				}
				catch (Throwable e1)
				{
					
				}
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
		}
	}
	
	private boolean uploadFileData()
	{
		boolean ok = true;
		try
		{
			while (!stopped && currentOffset < localFileSize)
			{
				if (localFileSize - currentOffset > fileTransferBufferSize)
				{
					readedBytes = fileTransferFileInputStream.read(fileTransferBuffer, 0, fileTransferBufferSize);
				}
				else
				{
					readedBytes = fileTransferFileInputStream.read(fileTransferBuffer, 0, Long.valueOf(localFileSize - currentOffset).intValue());
				}
				if (readedBytes == -1)
				{
					ok = false;
					fileTransferRemoteOutputStream.close();
					fileTransferFileInputStream.close();
					fileTransferRandomAccessFile.close();
					break;
				}
				else
				{
					fileTransferRemoteOutputStream.write(fileTransferBuffer, 0, readedBytes);
					fileTransferRemoteOutputStream.flush();
				}
				currentOffset += readedBytes;
				transferDataCount += readedBytes;
			}
		}
		catch (Throwable t)
		{
			//t.printStackTrace();
			ok = false;
			try
			{
				fileTransferRemoteOutputStream.close();
			}
			catch (Throwable e1)
			{
				
			}
			try
			{
				fileTransferFileInputStream.close();
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
		}
		return ok;
	}
	
	private boolean tryDownload(String currentPath, boolean rootLevel)
	{
		// VTConsole.print("\nVT>Trying to receive file from
		// server...\nVT>");
		//System.out.println("tryDownload: " + currentPath);
		if (verifyDownload(currentPath))
		{
			return (setDownloadStreams(currentPath) && downloadFilePath(currentPath, rootLevel));
		}
		else
		{
			return false;
		}
	}
	
	private boolean verifyDownload(String currentPath)
	{
		verified = true;
		directory = false;
		resumable = false;
		fileTransferFile = new File(convertFilePath(currentPath));
		if (!fileTransferFile.isAbsolute())
		{
			fileTransferFile = new File(convertFilePath(currentPath));
		}
		//System.out.println("verifyDownload: " + fileTransferFile.getAbsolutePath());
		try
		{
			if (getFileStatus())
			{
				if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
				{
					//if (currentPath == null)
					//{
						//localFilePath = appendToPath(localFilePath, getFileNameFromPath(remoteFilePath));
					//}
				}
				if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_UNKNOWN)
				{
					// VTConsole.print("\nVT>Local file for transfer is of
					// unknown type!\nVT>");
					verified = false;
				}
				if (localFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
				{
					// VTConsole.print("\nVT>Local file for transfer has
					// error!\nVT>");
					verified = false;
				}
				if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_NOT_FOUND)
				{
					// VTConsole.print("\nVT>Remote file for transfer not
					// found!\nVT>");
					verified = false;
				}
				if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_DIRECTORY)
				{
					directory = true;
				}
				if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_TYPE_UNKNOWN)
				{
					// VTConsole.print("\nVT>Remote file for transfer is of
					// unknown type!\nVT>");
					verified = false;
				}
				if (remoteFileStatus == VT.VT_FILE_TRANSFER_FILE_ERROR)
				{
					// VTConsole.print("\nVT>Remote file for transfer has
					// error!\nVT>");
					verified = false;
				}
				if (verified)
				{
					if (!directory)
					{
						transferFileCount++;
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
						transferDirectoryCount++;
						fileTransferFile = new File(convertFilePath(currentPath));
						if (fileTransferFile.exists() && !fileTransferFile.isDirectory())
						{
							fileTransferFile.delete();
						}
					}
				}
				if (verified && getFileAccess(false))
				{
					if (!(localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE || localFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_WRITE_ONLY))
					{
						// VTConsole.print("\nVT>Local file for transfer is
						// not writable!\nVT>");
						verified = false;
					}
					if (!(remoteFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_AND_WRITE || remoteFileAccess == VT.VT_FILE_TRANSFER_FILE_ACCESS_READ_ONLY))
					{
						// VTConsole.print("\nVT>Remote file for transfer is
						// not readable!\nVT>");
						verified = false;
					}
					localFileSize = 0;
					remoteFileSize = 0;
					currentOffset = 0;
					if (verified && !directory && getFileSizes())
					{
						resumable = false;
						if (resume)
						{
							if (remoteFileSize >= localFileSize && localFileSize >= 0)
							{
								if (check)
								{
									// VTConsole.print("\nVT>Checking file
									// transfer resume possibility...\nVT>");
									if (getFileChecksums())
									{
										if (localFileStatus != VT.VT_FILE_TRANSFER_FILE_NOT_FOUND && localChecksum == remoteChecksum)
										{
											resumable = true;
											currentOffset = localFileSize;
											// VTConsole.print("\nVT>File
											// transfer resume
											// possible!\nVT>");
										}
										else
										{
											// VTConsole.print("\nVT>File
											// transfer resume not
											// possible!\nVT>");
										}
									}
								}
								else
								{
									resumable = true;
									currentOffset = localFileSize;
									// VTConsole.print("\nVT>Assuming file
									// transfer resume as possible...\nVT>");
								}
							}
						}
						transferDataSize += remoteFileSize - currentOffset;
						// VTConsole.print("\nVT>Remote file for transfer
						// size: [" + remoteFileSize + "] bytes\nVT>");
						return true;
					}
					else if (verified && directory)
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
			try
			{
				fileTransferRemoteInputStream.close();
			}
			catch (Throwable t)
			{
				
			}
			try
			{
				fileTransferFileOutputStream.close();
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
				if (check)
				{
					// VTConsole.print("\nVT>Verifying received file
					// integrity...\nVT>");
					try
					{
						localFileSize = fileTransferFile.length();
					}
					catch (Throwable t)
					{
						
					}
					if (getFileChecksums())
					{
						if (localChecksum == remoteChecksum)
						{
							// VTConsole.print("\nVT>Received file integrity
							// verified!\nVT>");
						}
						else
						{
							// VTConsole.print("\nVT>Received file integrity
							// corrupted!\nVT>");
							return false;
						}
					}
					else
					{
						// VTConsole.print("\nVT>failed to verify received
						// file integrity!\nVT>");
						return false;
					}
				}
				if (!stopped && ok)
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
						fileTransferFinalFile = new File(convertFilePath(currentPath));
						if (!fileTransferFinalFile.isAbsolute())
						{
							fileTransferFinalFile = new File(convertFilePath(currentPath));
						}
						if (!fileTransferFile.renameTo(fileTransferFinalFile))
						{
							if (fileTransferFinalFile.delete())
							{
								return fileTransferFile.renameTo(fileTransferFinalFile);
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
				return ok;
			}
			else
			{
				String rootFolder = null;
				if (rootLevel)
				{
					rootFolder = getFileNameFromPath(this.filePath);
					new File(appendToPath(currentPath, rootFolder)).mkdirs();
				}
				else
				{
					fileTransferFile.mkdirs();
				}
				String nextPath = " ";
				// String subPath = appendToPath(localFilePath, currentPath);
				while (true)
				{
					ok = getContinueTransfer(ok);
					if (ok)
					{
						nextPath = readNextFilePath();
						//System.out.println("nextPath:" + nextPath);
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
				// return
				// VTArchiveUtils.extractTarInputStream(fileTransferInputStream,
				// fileTransferBuffer, fileTransferFile.getPath());
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
		finally
		{
			if (fileTransferFileOutputStream != null)
			{
				try
				{
					fileTransferFileOutputStream.close();
				}
				catch (Throwable e1)
				{
					
				}
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
		}
	}
	
	private boolean downloadFileData()
	{
		boolean ok = true;
		writtenBytes = 0;
		try
		{
			while (!stopped && currentOffset < remoteFileSize)
			{
				if (remoteFileSize - currentOffset > fileTransferBufferSize)
				{
					readedBytes = fileTransferRemoteInputStream.read(fileTransferBuffer, 0, fileTransferBufferSize);
				}
				else
				{
					readedBytes = fileTransferRemoteInputStream.read(fileTransferBuffer, 0, Long.valueOf(remoteFileSize - currentOffset).intValue());
				}
				if (readedBytes == -1)
				{
					ok = false;
					fileTransferRemoteInputStream.close();
					break;
				}
				fileTransferFileOutputStream.write(fileTransferBuffer, 0, readedBytes);
				writtenBytes += readedBytes;
				if (writtenBytes >= fileTransferBufferSize)
				{
					fileTransferFileOutputStream.flush();
					writtenBytes = 0;
				}
				currentOffset += readedBytes;
				transferDataCount += readedBytes;
			}
			fileTransferFileOutputStream.flush();
		}
		catch (Throwable t)
		{
			ok = false;
			try
			{
				fileTransferRemoteInputStream.close();
			}
			catch (Throwable t1)
			{
				
			}
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
		}
		return ok;
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
			interrupted = false;
			transferDataSize = 0;
			transferDataCount = 0;
			transferFileCount = 0;
			transferDirectoryCount = 0;
			String[] splitCommand = CommandLineTokenizer.tokenize(command);
			if (splitCommand.length < 4)
			{
				synchronized (this)
				{
					// stopped = true;
					VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
					finished = true;
					return;
				}
			}
			if (splitCommand[0].equalsIgnoreCase("*VTFILETRANSFER") || splitCommand[0].equalsIgnoreCase("*VTFT"))
			{
				source = splitCommand[2];
				destination = splitCommand[3];
				transferParameters = splitCommand[1];
				if (transferParameters.toUpperCase().contains("P"))
				{
					filePaths = splitCommand[2];
					//remoteFilePath = splitCommand[3];
					compression = false;
					resume = false;
					check = false;
					if (transferParameters.toUpperCase().contains("C"))
					{
						compression = true;
					}
					if (transferParameters.toUpperCase().contains("R"))
					{
						resume = true;
					}
					if (transferParameters.toUpperCase().contains("V"))
					{
						check = true;
					}
					String[] localFiles = filePaths.split(";");
					
					if (compression)
					{
						// fileTransferOutputStream = new
						// ZOutputStream(session.getClient().getConnection().getFileTransferDataOutputStream(),
						// JZlib.Z_DEFAULT_COMPRESSION, true, 4096);
						// ((ZOutputStream)fileTransferOutputStream).setFlushMode(JZlib.Z_SYNC_FLUSH);
						// fileTransferOutputStream = new
						// GZIPOutputStream(session.getClient().getConnection().getFileTransferDataOutputStream());
						// fileTransferRemoteOutputStream = new
						// SnappyFramedOutputStream(session.getClient().getConnection().getFileTransferDataOutputStream(),
						// 1024 * 8, 0.85d, false);
						fileTransferRemoteOutputStream = new LZ4BlockOutputStream(session.getClient().getConnection().getFileTransferDataOutputStream(), 1024 * 8, LZ4Factory.fastestJavaInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true);
					}
					else
					{
						fileTransferRemoteOutputStream = session.getClient().getConnection().getFileTransferDataOutputStream();
					}
					
					for (String localFile : localFiles)
					{
						//this.remoteFilePath = destination;
						//this.localFilePath = localFile;
						this.filePath = localFile;
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
									VTConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + source + "]" + "\nVT>Remote file: [" + destination + "]\nVT>");
								}
								else
								{
									VTConsole.print("\nVT>File transfer failed!" + "\nVT>Local file: [" + source + "]" + "\nVT>Remote file: [" + destination + "]\nVT>");
								}
								finished = true;
							}
							return;
						}
					}
					synchronized (this)
					{
						if (!stopped)
						{
							if (interrupted)
							{
								VTConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + source + "]" + "\nVT>Remote file: [" + destination + "]\nVT>");
							}
							else
							{
								VTConsole.print("\nVT>File transfer completed!" + "\nVT>Local file: [" + source + "]" + "\nVT>Remote file: [" + destination + "]\nVT>");
							}
						}
						else
						{
							VTConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + source + "]" + "\nVT>Remote file: [" + destination + "]\nVT>");
						}
						finished = true;
					}
				}
				else if (transferParameters.toUpperCase().contains("G"))
				{
					filePaths = splitCommand[2];
					//localFilePath = splitCommand[3];
					compression = false;
					resume = false;
					check = false;
					if (transferParameters.toUpperCase().contains("C"))
					{
						compression = true;
					}
					if (transferParameters.toUpperCase().contains("R"))
					{
						resume = true;
					}
					if (transferParameters.toUpperCase().contains("V"))
					{
						check = true;
					}
					
					if (compression)
					{
						// fileTransferInputStream = new
						// ZInputStream(session.getClient().getConnection().getFileTransferDataInputStream(),
						// true, 4096);
						// ((ZInputStream)fileTransferInputStream).setFlushMode(JZlib.Z_SYNC_FLUSH);
						// fileTransferInputStream = new
						// GZIPInputStream(fileTransferInputStream);
						// fileTransferRemoteInputStream = new
						// SnappyFramedInputStream(session.getClient().getConnection().getFileTransferDataInputStream(),
						// false);
						fileTransferRemoteInputStream = new LZ4BlockInputStream(session.getClient().getConnection().getFileTransferDataInputStream(), LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false);
					}
					else
					{
						fileTransferRemoteInputStream = session.getClient().getConnection().getFileTransferDataInputStream();
					}
					
					String[] remoteFiles = filePaths.split(";");
					for (String remoteFile : remoteFiles)
					{
						//this.localFilePath = destination;
						//this.remoteFilePath = remoteFile;
						this.filePath = remoteFile;
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
								if (interrupted)
								{
									VTConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + destination + "]" + "\nVT>Remote file: [" + source + "]\nVT>");
								}
								else
								{
									VTConsole.print("\nVT>File transfer failed!" + "\nVT>Local file: [" + destination + "]" + "\nVT>Remote file: [" + source + "]\nVT>");
								}
								finished = true;
							}
							return;
						}
					}
					synchronized (this)
					{
						if (!stopped)
						{
							if (interrupted)
							{
								VTConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + destination + "]" + "\nVT>Remote file: [" + source + "]\nVT>");
							}
							else
							{
								VTConsole.print("\nVT>File transfer completed!" + "\nVT>Local file: [" + destination + "]" + "\nVT>Remote file: [" + source + "]\nVT>");
							}
						}
						else
						{
							VTConsole.print("\nVT>File transfer interrupted!" + "\nVT>Local file: [" + destination + "]" + "\nVT>Remote file: [" + source + "]\nVT>");
						}
						finished = true;
					}
				}
				else
				{
					synchronized (this)
					{
						VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
						finished = true;
					}
				}
			}
			else
			{
				synchronized (this)
				{
					VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
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
		finished = true;
	}
}