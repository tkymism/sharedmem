package com.tkym.labs.sharedmem.win;

import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_COPY;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_READ;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_WRITE;

import com.tkym.labs.sharedmem.api.SharedMemory;
import com.tkym.labs.sharedmem.api.SharedMemoryException;
import com.tkym.labs.sharedmem.api.SharedMemoryLocator;
import com.tkym.labs.sharedmem.api.SharedMemoryLock;
import com.tkym.labs.sharedmem.win.FileMapRepository.NamedMapNotExistsException;

class SharedMemoryImpl implements SharedMemory{
	private final String name;
	private final SharedMemoryLockImpl lock;
	private FileMap fileMapOwner = null;
	SharedMemoryImpl(String name){
		this.name = name;
		lock = new SharedMemoryLockImpl(this, name);
	}
	@Override
	public boolean exists(){
		try {
			return FileMapRepository.getInstance().exists(name);
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		}
	}
	private SharedMemoryLocator open(int mode){
		try {
			FileMap map = FileMapRepository.getInstance().open(name,mode);
			return new SharedMemoryLocatorImpl(this, map, mode);
		} catch (NamedMapNotExistsException e) {
			return null;
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		}
	}
	@Override
	public SharedMemoryLock lock(){
		return lock;
	}
	@Override
	public SharedMemory unlock(){
		if (lock.isLocked())
			lock.release();
		else 
			throw new SharedMemoryException(name+" is not locked.");
		return this;
	}
	@Override
	public SharedMemoryLocator allocate(long length){
		try {
			fileMapOwner = FileMapRepository.
					getInstance().
					create(name, length);
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		}
		return writable();
	}
	@Override
	public SharedMemoryLocator writable(){
		return open(FILE_MAP_WRITE);
	}
	@Override
	public SharedMemoryLocator readonly(){
		return open(FILE_MAP_READ);
	}
	@Override
	public SharedMemoryLocator makecopy(){
		return open(FILE_MAP_COPY);
	}
	@Override
	protected void finalize() throws Throwable {
		if (fileMapOwner != null) {
			fileMapOwner.close();
			fileMapOwner = null;
		}
		super.finalize();
	}
}