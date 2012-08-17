package com.tkym.labs.sharedmem.api;

public interface SharedMemoryFactory {
	public SharedMemory create(String name);
}