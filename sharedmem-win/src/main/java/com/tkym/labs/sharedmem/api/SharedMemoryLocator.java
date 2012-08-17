package com.tkym.labs.sharedmem.api;


public interface SharedMemoryLocator {
	public SharedMemoryAccesser locate(int where, long length);
	public SharedMemory detach();
}