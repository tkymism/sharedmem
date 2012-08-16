#include <Windows.h>
#include <stdio.h>
#include <string>
#include <iostream>
#include "sharedmem.h"
#include "com_tkym_labs_sharedmem_win_BaseNamedObjectsJni.h"
#include <jni.h>

JNIEXPORT jint JNICALL Java_com_tkym_labs_sharedmem_win_BaseNamedObjectsJni_createFileMapping(JNIEnv* env, jobject, jstring name, jint pageAccess, jint upper, jint lower){
	LPCTSTR lpName = (LPCTSTR)env->GetStringChars(name, NULL);
	HANDLE handle = CreateFileMapping((HANDLE)0xFFFFFFFF, NULL, pageAccess, upper, lower, (LPCTSTR)lpName);
	env->ReleaseStringChars(name, (const jchar *)lpName);
	if (handle != NULL && GetLastError() == ERROR_ALREADY_EXISTS){
		CloseHandle(handle);
		return (-1 * ERROR_ALREADY_EXISTS);
	}
	if (handle == NULL) return (jint) (-1*GetLastError());
	else return (jint) handle;
}

JNIEXPORT jint JNICALL Java_com_tkym_labs_sharedmem_win_BaseNamedObjectsJni_openFileMapping(JNIEnv* env, jobject, jstring name, jint desiredAccess){
	LPCTSTR lpName = (LPCTSTR)env->GetStringChars(name, NULL);
	HANDLE handle = OpenFileMapping(desiredAccess, FALSE, lpName);
	env->ReleaseStringChars(name, (const jchar *)lpName);
	if (handle == NULL) return (jint) (-1*GetLastError());
	else return (jint) handle;
}

JNIEXPORT jint JNICALL Java_com_tkym_labs_sharedmem_win_BaseNamedObjectsJni_mapViewOfFile(JNIEnv *, jobject, jint handle, jint desiredAccess){
	LPVOID lpvoid = MapViewOfFile((HANDLE)handle, desiredAccess, 0, 0, 0);
	if (lpvoid == NULL)
		return (jint) (-1*GetLastError());
	else
		return (jint) lpvoid;
}

JNIEXPORT jboolean JNICALL Java_com_tkym_labs_sharedmem_win_BaseNamedObjectsJni_unmapViewOfFile(JNIEnv *, jobject, jint address){
	return UnmapViewOfFile((void *)address);
}

JNIEXPORT jobject JNICALL Java_com_tkym_labs_sharedmem_win_BaseNamedObjectsJni_createByteBuffer(JNIEnv * env, jobject, jint address, jlong capacity){
	return env->NewDirectByteBuffer((unsigned char *)address, capacity);	
}

JNIEXPORT jboolean JNICALL Java_com_tkym_labs_sharedmem_win_BaseNamedObjectsJni_closeHandle(JNIEnv *, jobject, jint handle){
	return((jboolean)CloseHandle((HANDLE)handle));
}

JNIEXPORT jint JNICALL Java_com_tkym_labs_sharedmem_win_BaseNamedObjectsJni_createMutex(JNIEnv * env, jobject, jstring name){
	LPCTSTR lpName = (LPCTSTR)env->GetStringChars(name, NULL);
	HANDLE handle = CreateMutex(NULL, FALSE, lpName);
	env->ReleaseStringChars(name, (const jchar *)lpName);
	if (handle == NULL) return (jint) (-1*GetLastError());
	else return (jint) handle;
}

JNIEXPORT jint JNICALL Java_com_tkym_labs_sharedmem_win_BaseNamedObjectsJni_releaseMutex(JNIEnv *, jobject, jint handle){
	BOOL ret = ReleaseMutex((HANDLE)handle);
	if (ret) return 0;
	else return (jint) (-1*GetLastError());
}

JNIEXPORT jint JNICALL Java_com_tkym_labs_sharedmem_win_BaseNamedObjectsJni_openMutex(JNIEnv * env, jobject, jstring name){
	LPCTSTR lpName = (LPCTSTR)env->GetStringChars(name, NULL);
	HANDLE handle = OpenMutex(MUTEX_ALL_ACCESS, FALSE, lpName);
	env->ReleaseStringChars(name, (const jchar *)lpName);
	if (handle == NULL) return (jint) (-1*GetLastError());
	else return (jint) handle;
}

JNIEXPORT jint JNICALL Java_com_tkym_labs_sharedmem_win_BaseNamedObjectsJni_waitForSingleObject(JNIEnv *, jobject, jint handle, jint msec){
	DWORD dwMilliseconds = msec;
	if (dwMilliseconds < 0) dwMilliseconds = INFINITE;
	return (jint) WaitForSingleObject((HANDLE)handle, dwMilliseconds);
}
