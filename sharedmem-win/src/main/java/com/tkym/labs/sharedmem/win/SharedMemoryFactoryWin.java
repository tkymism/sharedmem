package com.tkym.labs.sharedmem.win;

import com.tkym.labs.sharedmem.api.SharedMemory;
import com.tkym.labs.sharedmem.api.SharedMemoryFactory;

public class SharedMemoryFactoryWin implements SharedMemoryFactory {
	public SharedMemoryFactoryWin(){}
	@Override
	public SharedMemory create(String name) {
		return new SharedMemoryImpl(name);
	}
}
