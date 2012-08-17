package com.tkym.labs.sharedmem.api;

import java.util.concurrent.TimeoutException;

public interface SharedMemoryLock {
	public SharedMemory infinity();
	public SharedMemory until(int millisec) throws TimeoutException;
	public SharedMemory release();
	public boolean isLocked();
}