package com.tkym.labs.sharedmem.win;

import static com.tkym.labs.sharedmem.win.BaseNamedObjectsApi.CREATE_MUTEX;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsApi.OPEN_MUTEX;

class MutexRepository{
	private static MutexRepository singleton = new MutexRepository();
	private MutexRepository(){};
	static MutexRepository getInstance(){
		return singleton;
	}
	Mutex open(String name) throws BaseNamedObjectsException{
		int ret = openMutex(name);
		if (ret < 0)
			throw new BaseNamedObjectsException(
					OPEN_MUTEX,
					-ret,
					"native api error on OpenMutex. " +
					"name ="+name+". ");
		return new Mutex(name, ret);
	}
	Mutex create(String name) throws BaseNamedObjectsException{
		int ret = createMutex(name);
		if (ret < 0)
			throw new BaseNamedObjectsException(
					CREATE_MUTEX,
					-ret,
					"native api error on OpenMutex. " +
					"name ="+name+". ");
		return new Mutex(name, ret);
	}
	private synchronized int openMutex(String name){
		return BaseNamedObjectsJni.getInstance().openMutex(name);
	}
	private synchronized int createMutex(String name){
		return BaseNamedObjectsJni.getInstance().createMutex(name);
	}
}