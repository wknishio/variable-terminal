package org.vate.stream.endian;

import java.io.IOException;

import org.vate.stream.array.VTByteArrayInputStream;
import org.vate.stream.array.VTByteArrayOutputStream;
import org.vate.stream.data.RandomAccessDataInputOutput;

public class VTLittleEndianByteArrayInputOutputStream implements RandomAccessDataInputOutput
{
	private VTByteArrayOutputStream output;
	private VTByteArrayInputStream input;
	private VTLittleEndianInputStream dataInput;
	private VTLittleEndianOutputStream dataOutput;
	
	public VTLittleEndianByteArrayInputOutputStream(int size)
	{
		output = new VTByteArrayOutputStream(size);
		input = new VTByteArrayInputStream(output.buf(), 0, size);
		dataOutput = new VTLittleEndianOutputStream(output);
		dataInput = new VTLittleEndianInputStream(input);
	}

	public void readFully(byte[] b) throws IOException
	{
		dataInput.readFully(b);
	}

	public void readFully(byte[] b, int off, int len) throws IOException
	{
		dataInput.readFully(b, off, len);
	}

	public int skipBytes(int n) throws IOException
	{
		return dataInput.skipBytes(n);
	}

	public boolean readBoolean() throws IOException
	{
		return dataInput.readBoolean();
	}

	public byte readByte() throws IOException
	{
		return dataInput.readByte();
	}

	public int readUnsignedByte() throws IOException
	{
		return dataInput.readUnsignedByte();
	}

	public short readShort() throws IOException
	{
		return dataInput.readShort();
	}

	public int readUnsignedShort() throws IOException
	{
		return dataInput.readUnsignedShort();
	}

	public char readChar() throws IOException
	{
		return dataInput.readChar();
	}

	public int readInt() throws IOException
	{
		return dataInput.readInt();
	}

	public long readLong() throws IOException
	{
		return dataInput.readLong();
	}

	public float readFloat() throws IOException
	{
		return dataInput.readFloat();
	}

	public double readDouble() throws IOException
	{
		return dataInput.readDouble();
	}

	public String readLine() throws IOException
	{
		return dataInput.readLine();
	}

	public String readUTF() throws IOException
	{
		return dataInput.readUTF();
	}

	public int read(byte[] b) throws IOException
	{
		return input.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException
	{
		return input.read(b, off, len);
	}

	public int available() throws IOException
	{
		return input.available();
	}

	public long skip(long l) throws IOException
	{
		return input.skip(l);
	}

	public void close() throws IOException
	{
		
	}
	
	public int readSubInt() throws IOException
	{
		return dataInput.readSubInt();
	}

	public long readUnsignedInt() throws IOException
	{
		return dataInput.readUnsignedInt();
	}

	public void write(int b) throws IOException
	{
		output.write(b);
	}

	public void writeBoolean(boolean v) throws IOException
	{
		dataOutput.writeBoolean(v);
	}

	public void writeByte(int v) throws IOException
	{
		dataOutput.writeByte(v);
	}

	public void writeShort(int v) throws IOException
	{
		dataOutput.writeShort(v);
	}

	public void writeChar(int v) throws IOException
	{
		dataOutput.writeChar(v);
	}

	public void writeInt(int v) throws IOException
	{
		dataOutput.writeInt(v);
	}

	public void writeLong(long v) throws IOException
	{
		dataOutput.writeLong(v);
	}

	public void writeFloat(float v) throws IOException
	{
		dataOutput.writeFloat(v);
	}

	public void writeDouble(double v) throws IOException
	{
		dataOutput.writeDouble(v);
	}

	public void writeBytes(String s) throws IOException
	{
		dataOutput.writeBytes(s);
	}

	public void writeChars(String s) throws IOException
	{
		dataOutput.writeChars(s);
	}

	public void writeUTF(String s) throws IOException
	{
		dataOutput.writeUTF(s);
	}

	public void write(byte[] b) throws IOException
	{
		output.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException
	{
		output.write(b, off, len);
	}

	public void flush() throws IOException
	{
		output.flush();
	}

	public void writeShort(short s) throws IOException
	{
		dataOutput.writeShort(s);
	}

	public void writeUnsignedShort(int s) throws IOException
	{
		dataOutput.writeUnsignedShort(s);
	}

	public void writeChar(char c) throws IOException
	{
		dataOutput.writeChar(c);
	}

	public void writeSubInt(int i) throws IOException
	{
		dataOutput.writeSubInt(i);
	}

	public void setInputPos(int pos)
	{
		input.pos(pos);
		input.count(input.buf().length - pos);
	}
	
	public int getOutputCount()
	{
		return output.count();
	}
	
	public void setOutputCount(int count)
	{
		output.count(count);
	}
	
	public void setOutputSize(int size)
	{
		if (output.buf().length < size)
		{
			byte[] nextbuf = new byte[size];
			System.arraycopy(output.buf(), 0, nextbuf, 0, output.buf().length);
			output.buf(nextbuf);
			input.buf(nextbuf);
		}
	}
	
	public byte getByte(int index) throws IOException
	{
		setInputPos(index);
		return dataInput.readByte();
	}

	public short getShort(int index) throws IOException
	{
		setInputPos(index);
		return dataInput.readShort();
	}

	public int getInt(int index) throws IOException
	{
		setInputPos(index);
		return dataInput.readInt();
	}

	public int getSubInt(int index) throws IOException
	{
		setInputPos(index);
		return dataInput.readSubInt();
	}

	public float getFloat(int index) throws IOException
	{
		setInputPos(index);
		return dataInput.readFloat();
	}

	public long getLong(int index) throws IOException
	{
		setInputPos(index);
		return dataInput.readLong();
	}

	public double getDouble(int index) throws IOException
	{
		setInputPos(index);
		return dataInput.readDouble();
	}

	public int getBytes(int index, byte[] data, int off, int len) throws IOException
	{
		setInputPos(index);
		return input.read(data, off, len);
	}

	public void putByte(int index, byte val) throws IOException
	{
		setOutputCount(index);
		dataOutput.writeByte(val);
	}

	public void putShort(int index, short val) throws IOException
	{
		setOutputCount(index);
		dataOutput.writeShort(val);
	}

	public void putInt(int index, int val) throws IOException
	{
		setOutputCount(index);
		dataOutput.writeInt(val);
	}

	public void putSubInt(int index, int val) throws IOException
	{
		setOutputCount(index);
		dataOutput.writeSubInt(val);
	}

	public void putFloat(int index, float val) throws IOException
	{
		setOutputCount(index);
		dataOutput.writeFloat(val);
	}

	public void putLong(int index, long val) throws IOException
	{
		setOutputCount(index);
		dataOutput.writeLong(val);
	}

	public void putDouble(int index, double val) throws IOException
	{
		setOutputCount(index);
		dataOutput.writeDouble(val);
	}

	public void putBytes(int index, byte[] data, int off, int len) throws IOException
	{
		setOutputCount(index);
		output.write(data, off, len);
	}

	public byte getByte() throws IOException
	{
		return dataInput.readByte();
	}

	public short getShort() throws IOException
	{
		return dataInput.readShort();
	}

	public int getInt() throws IOException
	{
		return dataInput.readInt();
	}

	public int getSubInt() throws IOException
	{
		return dataInput.readSubInt();
	}

	public float getFloat() throws IOException
	{
		return dataInput.readFloat();
	}

	public long getLong() throws IOException
	{
		return dataInput.readLong();
	}

	public double getDouble() throws IOException
	{
		return dataInput.readDouble();
	}

	public int getBytes(byte[] data, int off, int len) throws IOException
	{
		return input.read(data, off, len);
	}
	
	public void putByte(byte val) throws IOException
	{
		dataOutput.writeByte(val);
	}

	public void putShort(short val) throws IOException
	{
		dataOutput.writeShort(val);
	}

	public void putInt(int val) throws IOException
	{
		dataOutput.writeInt(val);
	}

	public void putSubInt(int val) throws IOException
	{
		dataOutput.writeSubInt(val);
	}

	public void putFloat(float val) throws IOException
	{
		dataOutput.writeFloat(val);
	}

	public void putLong(long val) throws IOException
	{
		dataOutput.writeLong(val);
	}

	public void putDouble(double val) throws IOException
	{
		dataOutput.writeDouble(val);
	}

	public void putBytes(byte[] data, int off, int len) throws IOException
	{
		output.write(data, off, len);
	}
}