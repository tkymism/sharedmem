package com.tkym.labs.sharedmem.win;

class NamedMemoryOwner extends NamedMemory{
	private final long length;
	NamedMemoryOwner(Mutex mutex, FileMap filemap, long length) {
		super(mutex, filemap, NamedMemoryMode.WRITABLE);
		this.length = length;
	}
	public long getLength() {
		return length;
	}
	public void close(){
		super.mutex.close();
		super.filemap.close();
	}
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
}