package com.tkym.labs.sharedmem.api;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SharedMemoryRepository{
	private static final SharedMemoryRepository singleton = 
			new SharedMemoryRepository();
	private Map<String, SharedMemory> sharedMemoryMap;
	private static SharedMemoryFactory factory = null;
	private SharedMemoryRepository(){
		sharedMemoryMap = new ConcurrentHashMap<String, SharedMemory>();
	}
	@SuppressWarnings("unchecked")
	public static <T extends SharedMemoryFactory> void registerFactory(String className){
		try {
			registerFactory((Class<T>) Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new SharedMemoryException(e);
		}
	}
	public static <T extends SharedMemoryFactory> void registerFactory(Class<T> cls){
		try {
			factory = cls.newInstance();
		} catch (InstantiationException e) {
			throw new SharedMemoryException(e);
		} catch (IllegalAccessException e) {
			throw new SharedMemoryException(e);
		}
	}
	public static void registerFactory(SharedMemoryFactory newFactory){
		factory = newFactory; 
	}
	public static SharedMemoryRepository getInstance(){
		return singleton;
	}
	public SharedMemory get(String name){
		SharedMemory memory = sharedMemoryMap.get(name);
		if (memory == null){
			memory = factory.create(name);
			sharedMemoryMap.put(name, memory);
		}
		return memory;
	}
}