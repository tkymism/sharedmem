package com.tkym.labs.sharedmem.api;

public interface SharedMemory {
	public boolean exists();
	public SharedMemoryLock lock();
	public SharedMemory unlock();
	public SharedMemoryLocator allocate(long length);
	public SharedMemoryLocator writable();
	public SharedMemoryLocator readonly();
	public SharedMemoryLocator makecopy();
}