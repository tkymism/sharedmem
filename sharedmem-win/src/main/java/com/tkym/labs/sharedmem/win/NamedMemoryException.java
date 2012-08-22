package com.tkym.labs.sharedmem.win;

@SuppressWarnings("serial") class NamedMemoryException extends RuntimeException{
	NamedMemoryException(String msg){ super(msg); }
	NamedMemoryException(Throwable t){ super(t); }
}