package com.tkym.labs.sharedmem.win;

import java.nio.ByteBuffer;


class FileMapView{
	private final int startAddress;
	FileMapView(int address){
		this.startAddress = address;
	}
	int getAddress(){
		return startAddress;
	}
	synchronized boolean unmap(){
		return BaseNamedObjectsJni.getInstance().unmapViewOfFile(startAddress);
	}
	void write(int where, byte data){
		byte[] array = new byte[]{data};
		writeNative(where, array);
	}
	byte read(int where){
		return readNative(where, 1)[0];
	}
	void write(int where, byte[] byteArray){
		writeNative(where, byteArray);
	}
	byte[] read(int where, int length){
		return readNative(where, length);
	}
	synchronized ByteBuffer createByteBuffer(int where, long length){
		return BaseNamedObjectsJni.getInstance().
			createByteBuffer(startAddress + where, length);
	}
	private synchronized void writeNative(int where, byte[] byteArray){
		createByteBuffer(where, byteArray.length).put(byteArray);
	}
	private synchronized byte[] readNative(int where, int length){
		byte[] array = new byte[length];
		createByteBuffer(where, array.length).get(array);
		return array;
	}
}