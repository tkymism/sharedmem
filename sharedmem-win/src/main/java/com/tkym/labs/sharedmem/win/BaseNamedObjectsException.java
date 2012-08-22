package com.tkym.labs.sharedmem.win;


@SuppressWarnings("serial") 
class BaseNamedObjectsException extends Exception{
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
		String name = "UNKNOWN";
		if (args.length > 0)
			name = args[0];
		if (Win32Error.DIR.containsKey(code))
			return Win32Error.DIR.get(code) + 
					" WINAPI NAME=" + apiName + 
					" object name=" + name;
		return apiName+" error code="+code;
	}
}