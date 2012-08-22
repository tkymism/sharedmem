package com.tkym.labs.sharedmem.win;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;


public class NamedMemoryAccesser {
	private final ByteBuffer buffer;
	private final NamedMemoryAttaching view;
	NamedMemoryAccesser(NamedMemoryAttaching view, ByteBuffer buffer){
		this.buffer = buffer;
		this.view = view;
	}
	public NamedMemoryAttaching direct(NamedMemoryDirect direct){
		try {
			direct.direct(buffer);
		} catch (Exception e) {
			throw new NamedMemoryException(e);
		}
		return view;
	}
	/**
	 * http://www.exampledepot.com/egs/java.nio/Buffer2Stream.html
	 */
	public NamedMemoryAttaching write(NamedMemoryWriter writer){
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
			throw new NamedMemoryException(e);
		}
		return view;
	}
	/**
	 * http://www.exampledepot.com/egs/java.nio/Buffer2Stream.html
	 */
	public NamedMemoryAttaching read(NamedMemoryReader attach){
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
			throw new NamedMemoryException(e);
		}
		return view;
	}
	public static interface NamedMemoryReader{
		public void read(InputStream is) throws Exception;
	}
	public static interface NamedMemoryWriter{
		public void write(OutputStream os) throws Exception;
	}
	public static interface NamedMemoryDirect{
		public void direct(ByteBuffer buf) throws Exception;
	}
}