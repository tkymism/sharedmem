package com.tkym.labs.sharedmem.win;

import java.util.concurrent.TimeoutException;


public class NamedMemory {
	protected final Mutex mutex;
	protected final FileMap filemap;
	private NamedMemoryMode mode;
	NamedMemory(Mutex mutex, FileMap filemap, NamedMemoryMode mode){
		this.mutex = mutex;
		this.filemap = filemap;
		this.mode = mode;
	}
	public NamedMemory lock(int timeout) throws TimeoutException{
		try {
			mutex.waitFor(timeout);
			return this;
		} catch (BaseNamedObjectsException e) {
			throw new NamedMemoryException(e);
		}
	}
	public NamedMemory lock(){
		try {
			mutex.waitFor(BaseNamedObjectsJni.INFINITE);
			return this;
		} catch (BaseNamedObjectsException e) {
			throw new NamedMemoryException(e);
		} catch (TimeoutException e) {
			throw new NamedMemoryException(e);
		}
	}
	public NamedMemory unlock(){
		try {
			mutex.release();
			return this;
		} catch (BaseNamedObjectsException e) {
			throw new NamedMemoryException(e);
		}
	}
	public NamedMemoryAttaching attach(){
		try {
			return new NamedMemoryAttaching(this, filemap.map(mode.code));
		} catch (BaseNamedObjectsException e) {
			throw new NamedMemoryException(e);
		}
	}
}