package com.tkym.labs.sharedmem.win;

import static com.tkym.labs.sharedmem.win.BaseNamedObjectsApi.CREATE_FILE_MAPPING;

@SuppressWarnings("serial") 
class BaseNamedObjectsException extends Exception{
	private static final int ERROR_ALREADY_EXISTS = 183;
	
	private BaseNamedObjectsApi apiName;
	private final int code;

	BaseNamedObjectsException(BaseNamedObjectsApi apiName, int code, String... args){
		super(message(apiName, code, args));
		this.code = code;
	}
	
	int getCode(){ return code; }
	BaseNamedObjectsApi getApiName(){
		return apiName;
	}
	static String message(BaseNamedObjectsApi apiName, int code, String... args){
		if (apiName == CREATE_FILE_MAPPING)
			if(code == ERROR_ALREADY_EXISTS) 
				return "name["+args[0]+" ] is already exists.";
		return apiName+" error code="+code;
	}
}