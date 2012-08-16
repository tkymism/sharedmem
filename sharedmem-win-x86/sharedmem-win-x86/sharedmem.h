#include <jni.h>
#ifndef SHAREDMEM_H
#define SHAREDMEM_H

#ifdef DLLTYPE
#  undef DLLTYPE
#endif
#ifdef SHAREDMEM_EXPORTS
#  define DLLTYPE __declspec(dllexport)
#else
#  define DLLTYPE __declspec(dllimport)
#endif

#ifdef __cplusplus
extern "C" {
#endif
// DLLTYPE BOOL WINAPI add(HWND);
#ifdef __cplusplus
}
#endif

#endif //SHAREDMEM_H