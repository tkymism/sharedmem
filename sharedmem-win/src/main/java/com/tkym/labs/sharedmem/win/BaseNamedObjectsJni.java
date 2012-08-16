package com.tkym.labs.sharedmem.win;

import java.nio.ByteBuffer;

class BaseNamedObjectsJni {
	static {
		System.loadLibrary("sharedmem-win-x86");
	}
	private static BaseNamedObjectsJni singleton = new BaseNamedObjectsJni();
	private BaseNamedObjectsJni(){}
	
	static BaseNamedObjectsJni getInstance(){
		return singleton;
	}
	public static final int PAGE_READONLY = 0x02;
	public static final int PAGE_READWRITE = 0x04;
	public static final int PAGE_WRITECOPY = 0x08;
	public static final int FILE_MAP_COPY = 0x0001;
	public static final int FILE_MAP_WRITE = 0x0002;
	public static final int FILE_MAP_READ = 0x0004;
	native int createFileMapping(String name, int pageAccess, int upper, int lower);
	native int openFileMapping(String name, int desiredAccess);
	native int mapViewOfFile(int handle, int desiredAccess);
	native boolean unmapViewOfFile(int address);
	native ByteBuffer createByteBuffer(int addr, long capacity);
	native boolean closeHandle(int handle);
	static int WAIT_OBJECT_0	= 0;
	static int WAIT_ABANDONED	= 80;
	static int ERROR_WAIT_NO_CHILDREN = 128;
	static int WAIT_TIMEOUT		= 258;
	static int ERROR_NOT_OWNER	= 288;
	static int INFINITE = 0xFFFFFFFF;
	native int createMutex(String name);
	native int openMutex(String name);
	native int releaseMutex(int handle);
	native int waitForSingleObject(int handle, int waitMsec);
}