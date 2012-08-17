package com.tkym.labs.sharedmem.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SharedMemoryAccesser{
	private final SharedMemoryLocator locater;
	private final ByteBuffer buffer;
	public SharedMemoryAccesser(SharedMemoryLocator memory, ByteBuffer buffer){
		this.locater = memory;
		this.buffer = buffer;
	}
	public SharedMemoryLocator direct(SharedMemoryDirect direct){
		try {
			direct.direct(buffer);
		} catch (Exception e) {
			throw new SharedMemoryException(e);
		}
		return locater;
	}
	
	/**
	 * http://www.exampledepot.com/egs/java.nio/Buffer2Stream.html
	 */
	public SharedMemoryLocator write(SharedMemoryWriter writer){
		OutputStream stream = new OutputStream() {
			@Override
			public void write(int b){
				buffer.put((byte)b);
			}
			@Override
			public void write(byte[] b, int off, int len)
					throws IOException {
				buffer.put(b, off, len);
			}
		};
		try {
			writer.write(stream);
			stream.close();
		} catch (Exception e) {
			throw new SharedMemoryException(e);
		}
		return locater;
	}
	/**
	 * http://www.exampledepot.com/egs/java.nio/Buffer2Stream.html
	 */
	public SharedMemoryLocator read(SharedMemoryReader attach){
		InputStream stream = new InputStream() {
			@Override
			public int read(){
	            if (!buffer.hasRemaining()) 
	            	return -1;
	            return buffer.get();
	 		}
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				len = Math.min(len, buffer.remaining());
	            buffer.get(b, off, len);
	            return len;
			}
		};
		try {
			attach.read(stream);
		} catch (Exception e) {
			throw new SharedMemoryException(e);
		}
		return locater;
	}
	public static interface SharedMemoryReader{
		public void read(InputStream is) throws Exception;
	}
	public static interface SharedMemoryWriter{
		public void write(OutputStream os) throws Exception;
	}
	public static interface SharedMemoryDirect{
		public void direct(ByteBuffer buf) throws Exception;
	}
}