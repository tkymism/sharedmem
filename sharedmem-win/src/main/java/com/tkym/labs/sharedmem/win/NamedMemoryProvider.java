package com.tkym.labs.sharedmem.win;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class NamedMemoryProvider{
	private static final NamedMemoryProvider singleton = new NamedMemoryProvider();
	private final Map<String, NamedMemoryOwner> ownermap;
	private NamedMemoryFactory factory = new NamedMemoryFactory();
	private NamedMemoryProvider(){
		ownermap = new ConcurrentHashMap<String, NamedMemoryOwner>();
	}
	public static final NamedMemoryProvider getInstance(){ return singleton; }
	public NamedMemoryProvider register(String name, long length){
		if (ownermap.containsKey(name))
			throw new NamedMemoryException("name[ "+name+"] is already allocate");
		ownermap.put(name, factory.create(name, length));
		return this;
	}
	public NamedMemoryProvider unregister(String name){
		if (!ownermap.containsKey(name))
			throw new NamedMemoryException("name[ "+name+"] is already allocate");
		ownermap.get(name).close();
		ownermap.remove(name);
		return this;
	}
	public NamedMemory readonly(String name){
		return factory.open(name, NamedMemoryMode.READONLY);
	}
	public NamedMemory writable(String name){
		return factory.open(name, NamedMemoryMode.WRITABLE);
	}
	public NamedMemory ascopy(String name){
		return factory.open(name, NamedMemoryMode.COPY);
	}
}