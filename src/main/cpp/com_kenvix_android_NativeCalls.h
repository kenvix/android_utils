/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_kenvix_android_NativeCalls */

#ifndef _Included_com_kenvix_android_NativeCalls
#define _Included_com_kenvix_android_NativeCalls
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_kenvix_android_NativeCalls
 * Method:    getAppContext
 * Signature: ()Landroid/content/Context;
 */
JNIEXPORT jobject JNICALL Java_com_kenvix_android_NativeCalls_getAppContext
  (JNIEnv *, jclass);

/*
 * Class:     com_kenvix_android_NativeCalls
 * Method:    setAppContext
 * Signature: (Landroid/content/Context;)V
 */
JNIEXPORT void JNICALL Java_com_kenvix_android_NativeCalls_setAppContext
  (JNIEnv *, jclass, jobject);

/*
 * Class:     com_kenvix_android_NativeCalls
 * Method:    initialize
 * Signature: (Lcom/kenvix/android/ApplicationEnvironment;)V
 */
JNIEXPORT void JNICALL Java_com_kenvix_android_NativeCalls_initialize
  (JNIEnv *, jclass, jobject);

#ifdef __cplusplus
}
#endif
#endif
