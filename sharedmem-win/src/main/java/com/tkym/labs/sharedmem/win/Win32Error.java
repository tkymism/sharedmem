package com.tkym.labs.sharedmem.win;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * http://ir9.jp/prog/ayu/win32err.htm
 * @author takayama
 */
public enum Win32Error {
	ERROR_FILE_NOT_FOUND(2, "指定されたファイルが見つかりません。"),
	ERROR_ALREADY_EXISTS(183, "既に存在するファイルを作成することはできません。"),
	ERROR_NOT_OWNER(288, "呼び出し側が所有していないミューテックスを解放しようとしています。"),
	;
	Win32Error(int code, String message){
		this.code = code;
		this.message = message;
	}
	public boolean equals(int code){
		return this.code == code;
	}
	int code;
	String message;
	public static Map<Integer, Win32Error> DIR = new ConcurrentHashMap<Integer, Win32Error>();
	static {
		DIR.put(ERROR_ALREADY_EXISTS.code, ERROR_ALREADY_EXISTS);
		DIR.put(ERROR_NOT_OWNER.code, ERROR_NOT_OWNER);
	}
}