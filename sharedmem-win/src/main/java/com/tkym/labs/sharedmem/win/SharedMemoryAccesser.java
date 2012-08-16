package com.tkym.labs.sharedmem.win;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SharedMemoryAccesser{
	private final SharedMemoryLocator memory;
	private final ByteBuffer buffer;
	SharedMemoryAccesser(SharedMemoryLocator memory, ByteBuffer buffer){
		this.memory = memory;
		this.buffer = buffer;
	}
	public SharedMemoryLocator direct(SharedMemoryDirectAttach attach){
		try {
			attach.attach(buffer);
		} catch (Exception e) {
			throw new SharedMemoryException(e);
		}
		return memory;
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
		return memory;
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
		return memory;
	}
	public static interface SharedMemoryReader{
		public void read(InputStream is) throws Exception;
	}
	public static interface SharedMemoryWriter{
		public void write(OutputStream os) throws Exception;
	}
	public static interface SharedMemoryDirectAttach{
		public void attach(ByteBuffer buf) throws Exception;
	}
}