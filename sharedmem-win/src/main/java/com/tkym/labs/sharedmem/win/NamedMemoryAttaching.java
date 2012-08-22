package com.tkym.labs.sharedmem.win;


public class NamedMemoryAttaching {
	private final FileMapView mapview;
	private final NamedMemory memory;
	NamedMemoryAttaching(NamedMemory memory, FileMapView mapview){
		this.mapview = mapview;
		this.memory = memory;
	}
	public NamedMemoryAccesser on(int where, long length){
		return new NamedMemoryAccesser(this, 
				mapview.createByteBuffer(where, length));
	}
	public NamedMemory detach(){
		mapview.unmap();
		return memory;
	}
}