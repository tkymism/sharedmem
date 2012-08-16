package com.tkym.labs.sharedmem.win;

@SuppressWarnings("serial") class SharedMemoryException extends RuntimeException{
	SharedMemoryException(Throwable t) {
		super(t);
	}
}