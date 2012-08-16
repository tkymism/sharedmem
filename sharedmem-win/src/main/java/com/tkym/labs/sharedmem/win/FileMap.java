package com.tkym.labs.sharedmem.win;

import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_COPY;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_READ;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_WRITE;

class FileMap{
	private final String name;
	private final int handle;
	FileMap(String name, int handle){
		this.name = name;
		this.handle = handle;
	}
	String getName() {
		return name;
	}
	int getHandle(){
		return handle;
	}
	FileMapView mapAsRead() throws BaseNamedObjectsException{
		return map(FILE_MAP_READ);
	}
	FileMapView mapAsWrite() throws BaseNamedObjectsException{
		return map(FILE_MAP_WRITE);
	}
	FileMapView mapOnCopy() throws BaseNamedObjectsException{
		return map(FILE_MAP_COPY);
	}
	FileMapView map(int mode) throws BaseNamedObjectsException{
		int address = mapView(mode);
		if (address < 0)
			throw new BaseNamedObjectsException(BaseNamedObjectsApi.MAP_VIEW_OF_FILE, address * -1);
		return new FileMapView(address);
	}
	private synchronized int mapView(int mode){
		return BaseNamedObjectsJni.getInstance().mapViewOfFile(handle, mode);
	}
	
	synchronized boolean close(){
		BaseNamedObjectsJni jni = BaseNamedObjectsJni.getInstance();
		return jni.closeHandle(handle);
	}
}