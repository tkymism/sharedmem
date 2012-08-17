package com.tkym.labs.sharedmem.api;

@SuppressWarnings("serial") 
public class SharedMemoryException extends RuntimeException{
	public SharedMemoryException(String msg) {
		super(msg);
	}
	public SharedMemoryException(Throwable t) {
		super(t);
	}
}