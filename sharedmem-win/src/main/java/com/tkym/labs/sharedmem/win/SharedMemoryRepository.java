package com.tkym.labs.sharedmem.win;

import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_COPY;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_READ;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_WRITE;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import com.tkym.labs.sharedmem.win.FileMapRepository.NamedMapNotExistsException;

public class SharedMemoryRepository{
	static final int TIMEOUT_WAIT_MUTEX = 1000;
	private static final SharedMemoryRepository singleton = 
			new SharedMemoryRepository();
	private Map<String, FileMap> allocateFileMap;
	private SharedMemoryRepository(){
		allocateFileMap = new ConcurrentHashMap<String, FileMap>();
	}
	public static SharedMemoryRepository getInstance(){
		return singleton;
	}
	public boolean exists(String name) throws TimeoutException{
		try {
			Mutex mutex = lock(name);
			boolean exists = FileMapRepository.getInstance().exists(name);
			mutex.release();
			mutex.close();
			return exists;
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		}
	}
	private FileMap createFileMap(String name, long length){
		try {
			return FileMapRepository.getInstance().create(name, length);
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		}
	}
	private SharedMemoryLocator open(String name, int mode, Mutex mutex){
		try {
			FileMap map = FileMapRepository.getInstance().open(name,mode);
			return new SharedMemoryLocator(map, mode, mutex);
		} catch (NamedMapNotExistsException e) {
			return null;
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		}
	}
	public SharedMemoryLocator writable(String name) throws TimeoutException{
		return open(name, FILE_MAP_WRITE, lock(name));
	}
	public SharedMemoryLocator readonly(String name) throws TimeoutException{
		return open(name, FILE_MAP_READ, lock(name));
	}
	public SharedMemoryLocator copy(String name){
		return open(name, FILE_MAP_COPY, null);
	}
	public SharedMemoryLocator allocate(String name, long length) throws TimeoutException{
		Mutex mutex = lock(name);
		FileMap map = createFileMap(name, length);
		allocateFileMap.put(name, map);
		return open(name, FILE_MAP_WRITE, mutex);
	}
	private Mutex lock(String name) throws TimeoutException{
		try {
			Mutex mutex = MutexRepository.getInstance().create(name+".mtx");
			mutex.waitFor(SharedMemoryRepository.TIMEOUT_WAIT_MUTEX);
			return mutex;
		} catch (BaseNamedObjectsException e) {
			throw new SharedMemoryException(e);
		} 
	}
	public boolean deallocate(String name){
		FileMap map = allocateFileMap.get(name);
		if (map == null) return false;
		map.close();
		allocateFileMap.remove(name);
		return true;
	}
	@Override
	protected void finalize() throws Throwable {
		for(String key : allocateFileMap.keySet())
			allocateFileMap.get(key).close();
		allocateFileMap.clear();
		super.finalize();
	}
}