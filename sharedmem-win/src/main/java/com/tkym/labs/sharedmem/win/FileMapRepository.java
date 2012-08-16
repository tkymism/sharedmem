package com.tkym.labs.sharedmem.win;

import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_COPY;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_READ;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_WRITE;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.PAGE_READWRITE;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsApi.CREATE_FILE_MAPPING;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsApi.OPEN_FILE_MAPPING;

import java.nio.ByteBuffer;

class FileMapRepository{
	private static FileMapRepository singleton = new FileMapRepository();
	private FileMapRepository(){};
	static FileMapRepository getInstance(){
		return singleton;
	}
	FileMap create(String name, long length) throws FileMapException{
		int handle = createMemory(name, length);
		if (handle <= 0) throw new FileMapException(CREATE_FILE_MAPPING, -1*handle, name);
		return new FileMap(name, handle);
	}
	
	int[] separateToIntValue(long value){
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(value);
		buffer.position(0);
		int[] ret = new int[2];
		ret[0] = buffer.getInt();
		ret[1] = buffer.getInt();
		return ret;
	}
	
	private synchronized int createMemory(String name, long length){
		int[] size = separateToIntValue(length);
		return BaseNamedObjectsJni.getInstance().createFileMapping(name, PAGE_READWRITE, size[0], size[1]);
	}
	boolean exists(String name) throws FileMapException{
		try {
			openAsRead(name);
			return true;
		} catch (NamedMapNotExistsException e) {
			return false;
		}
	}
	FileMap openAsRead(String name) throws FileMapException{
		return open(name, FILE_MAP_READ);
	}
	FileMap openAsWrite(String name) throws FileMapException{
		return open(name, FILE_MAP_WRITE);
	}
	FileMap openAsCopy(String name) throws FileMapException{
		return open(name, FILE_MAP_COPY);
	}
	FileMap open(String name, int mode) throws FileMapException{
		int handle = openMemory(name, mode);
		if (handle == -2)
			throw new NamedMapNotExistsException(OPEN_FILE_MAPPING);
		if (handle <= 0)
			throw new FileMapException(OPEN_FILE_MAPPING, -1*handle);
		return new FileMap(name, handle);
	}
	synchronized int openMemory(String name, int mode){
		return BaseNamedObjectsJni.getInstance().openFileMapping(name, mode);
	}
	@SuppressWarnings("serial")
	static class NamedMapNotExistsException extends FileMapException{
		NamedMapNotExistsException(BaseNamedObjectsApi apiName, String... args) {
			super(apiName, 2, args);
		}
	}
}