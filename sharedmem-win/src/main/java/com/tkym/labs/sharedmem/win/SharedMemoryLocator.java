package com.tkym.labs.sharedmem.win;

import java.nio.ByteBuffer;


class SharedMemoryLocator {
	private Mutex mutex = null;
	private final FileMap file;
	private final FileMapView view;
	SharedMemoryLocator(FileMap file, int mode, Mutex mutex) throws FileMapException {
		this.file = file;
		this.mutex = mutex;
		this.view = file.map(mode);
	}
	private SharedMemoryLocator unlock() {
		try {
			if (mutex == null) return this;
			mutex.release();
			mutex.close();
			mutex = null;
		} catch (MutexException e) {
			throw new SharedMemoryException(e);
		}
		return this;
	}
	ByteBuffer bytebuffer(int where, long length){
		return view.createByteBuffer(where, length);
	}
	public SharedMemoryAccesser locate(int where, long length){
		return new SharedMemoryAccesser(
				this,
				bytebuffer(where, length));
	}
	public void release(){
		view.unmap();
		file.close();
		unlock();
	}
}