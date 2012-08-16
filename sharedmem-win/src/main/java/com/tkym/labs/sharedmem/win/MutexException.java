package com.tkym.labs.sharedmem.win;

@SuppressWarnings("serial") class MutexException extends Exception{
	private int errorCode = -1;
	MutexException(int code, String message){
		super(message+"[error code="+code+"]");
		this.errorCode = code;
	}
	MutexException(String message){
		super(message);
	}
	int getErrorCode(){
		return errorCode;
	}
}