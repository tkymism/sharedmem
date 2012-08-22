package com.tkym.labs.sharedmem.win;

enum NamedMemoryMode{
	READONLY(BaseNamedObjectsJni.FILE_MAP_READ),
	WRITABLE(BaseNamedObjectsJni.FILE_MAP_WRITE),
	COPY(BaseNamedObjectsJni.FILE_MAP_COPY);
	int code;
	NamedMemoryMode(int code){
		this.code = code;
	}
}