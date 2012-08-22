package com.tkym.labs.sharedmem.win;


class NamedMemoryFactory{
	private static final String MUTEX_EXTENSION = ".mtx";
	NamedMemoryOwner create(String name, long length){
		try {
			Mutex mutex = 
					MutexRepository.
						getInstance().
						create(name+MUTEX_EXTENSION);
			FileMap filemap = 
					FileMapRepository.
						getInstance().
						create(name, length);
			return new NamedMemoryOwner(mutex, filemap, length);
		} catch (BaseNamedObjectsException e) {
			throw new NamedMemoryException(e);
		}
	}
	NamedMemory open(String name, NamedMemoryMode mode){
		try {
			Mutex mutex = 
					MutexRepository.
						getInstance().
						open(name+MUTEX_EXTENSION);
			FileMap filemap = 
					FileMapRepository.
						getInstance().
						open(name, mode.code);
			return new NamedMemory(mutex, filemap, mode);
		} catch (BaseNamedObjectsException e) {
			throw new NamedMemoryException(e);
		}
	}
}