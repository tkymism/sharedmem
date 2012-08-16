package com.tkym.labs.sharedmem.win;

@SuppressWarnings("serial") 
public class SharedMemoryException extends RuntimeException{
	SharedMemoryException(Throwable t) {
		super(t);
	}
}