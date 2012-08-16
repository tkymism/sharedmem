package com.tkym.labs.sharedmem.win;

import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.ERROR_WAIT_NO_CHILDREN;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.WAIT_TIMEOUT;

import java.util.concurrent.TimeoutException;


class Mutex{
	private final int handle;
	private final String name;
	Mutex(String name, int handle){
		this.handle = handle;
		this.name = name;
	}
	int getHandle() {
		return handle;
	}
	String getName(){
		return name;
	}
	void waitFor() throws MutexException, TimeoutException{
		waitFor(BaseNamedObjectsJni.INFINITE);
	}
	int waitFor(int msec) throws MutexException, TimeoutException{
		int ret = waitForSingleObject(msec);
		if (ret == ERROR_WAIT_NO_CHILDREN)
			throw new MutexException(ERROR_WAIT_NO_CHILDREN, "ERROR_WAIT_NO_CHILDREN");
		if (ret == WAIT_TIMEOUT)
			throw new TimeoutException("wait For Mutex["+name+"] is timeout. wait time is "+msec+" msec.");
		if (ret != 0)
			throw new MutexException(-ret, "Windows API[WaitForSingleObject] is error.");
		return ret;
	}
	private synchronized int waitForSingleObject(int msec){
		return BaseNamedObjectsJni.
				getInstance().
				waitForSingleObject(this.handle, msec);
	}
	void release() throws MutexException{
		int ret = releaseMutex();
		if (ret < 0)
			throw new MutexException(-ret, "Windows API[ReleaseMutex] is error");
	}
	void close(){
		closeMutex();
	}
	private synchronized int releaseMutex(){
		return BaseNamedObjectsJni.
				getInstance().
				releaseMutex(handle);
	}
	private synchronized boolean closeMutex(){
		return BaseNamedObjectsJni.
				getInstance().
				closeHandle(handle);
	}
}