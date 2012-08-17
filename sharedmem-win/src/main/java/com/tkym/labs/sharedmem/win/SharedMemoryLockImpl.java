package com.tkym.labs.sharedmem.win;

import java.util.concurrent.TimeoutException;

import com.tkym.labs.sharedmem.api.SharedMemory;
import com.tkym.labs.sharedmem.api.SharedMemoryException;
import com.tkym.labs.sharedmem.api.SharedMemoryLock;

/**
 * 
 * @author takayama
 */
class SharedMemoryLockImpl implements SharedMemoryLock{
	private static final String EXTENSION = ".mtx";
	public static final int DEFAULT_TIMEOUT = 1000;
	private final Mutex mutex;
	private boolean locked = false;
	private final SharedMemoryImpl sharedMemory;
	SharedMemoryLockImpl(SharedMemoryImpl sharedMemory, String name){
		this.sharedMemory = sharedMemory;
		try {
			mutex = MutexRepository.
					getInstance().
					create(name+EXTENSION);
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		} 
	}
	@Override
	public SharedMemory infinity(){
		try {
			return until(BaseNamedObjectsJni.INFINITE);
		} catch (TimeoutException e) {
			throw new SharedMemoryException(e);
		}
	}
	@Override
	public SharedMemory until(int millisec) throws TimeoutException{
		if (locked)
			return this.sharedMemory;
		try {
			mutex.waitFor(millisec);
			locked = true;
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		}
		return this.sharedMemory;
	}
	@Override
	public SharedMemory release(){
		if (!locked)
			return this.sharedMemory;
		try {
			mutex.release();
			locked = false;
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		}
		return this.sharedMemory;
	}
	public boolean isLocked(){
		return locked;
	}
	@Override
	protected void finalize() throws Throwable {
		if (locked) release();
		super.finalize();
	}
}