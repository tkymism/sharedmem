package com.tkym.labs.sharedmem.win;

@SuppressWarnings("serial") 
class FileMapException extends Exception{
	private BaseNamedObjectsApi apiName;
	private final int code;
	FileMapException(BaseNamedObjectsApi apiName, int code, String... args){
		super(message(apiName, code, args));
		this.code = code;
	}
	
	int getCode(){ return code; }
	BaseNamedObjectsApi getApiName(){
		return apiName;
	}
	static String message(BaseNamedObjectsApi apiName, int code, String... args){
		if (apiName == BaseNamedObjectsApi.CREATE_FILE_MAPPING && code==183) return "name["+args[0]+" ] is already exists.";
		return apiName+" error code="+code;
	}
}