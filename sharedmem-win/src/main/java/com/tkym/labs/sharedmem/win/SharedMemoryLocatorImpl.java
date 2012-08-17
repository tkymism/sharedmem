package com.tkym.labs.sharedmem.win;

import java.nio.ByteBuffer;

import com.tkym.labs.sharedmem.api.SharedMemory;
import com.tkym.labs.sharedmem.api.SharedMemoryAccesser;
import com.tkym.labs.sharedmem.api.SharedMemoryLocator;

class SharedMemoryLocatorImpl implements SharedMemoryLocator{
	private final FileMap file;
	private final FileMapView view;
	private final SharedMemoryImpl sharedMemory;
	SharedMemoryLocatorImpl(SharedMemoryImpl sharedMemory, FileMap file, int mode) throws BaseNamedObjectsException {
		this.sharedMemory = sharedMemory;
		this.file = file;
		this.view = file.map(mode);
	}
	ByteBuffer bytebuffer(int where, long length){
		return view.createByteBuffer(where, length);
	}
	@Override
	public SharedMemoryAccesser locate(int where, long length){
		return new SharedMemoryAccesser(
				this,
				bytebuffer(where, length));
	}
	@Override
	public SharedMemory detach(){
		view.unmap();
		file.close();
		return sharedMemory;
	}
}