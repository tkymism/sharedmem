package com.tkym.labs.sharedmem.win;

import static com.tkym.labs.sharedmem.win.Win32Error.ERROR_FILE_NOT_FOUND;

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
	private final String mutexName;
	private Mutex mutex = null;
	private final SharedMemoryImpl sharedMemory;
	SharedMemoryLockImpl(SharedMemoryImpl sharedMemory, String name){
		this.sharedMemory = sharedMemory;
		mutexName = name + EXTENSION;
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
		if (isLocked())
			return this.sharedMemory;
		try {
			mutex = openOrCreateMutex();
			mutex.waitFor(millisec);
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		}
		return this.sharedMemory;
	}
	
	Mutex openOrCreateMutex() throws BaseNamedObjectsException{
		try{
			return MutexRepository.
					getInstance().
					open(mutexName);
		}catch (BaseNamedObjectsException e){
			if (e.getCode() == ERROR_FILE_NOT_FOUND.code)
				return MutexRepository.
						getInstance().
						create(mutexName);
			throw e;
		}
	}
	
	@Override
	public SharedMemory release(){
		if (!isLocked())
			return this.sharedMemory;
		try {
			mutex.release();
			mutex.close();
			mutex = null;
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		}
		return this.sharedMemory;
	}
	public boolean isLocked(){
		return mutex != null;
	}
	@Override
	protected void finalize() throws Throwable {
		if (isLocked()) release();
		super.finalize();
	}
}