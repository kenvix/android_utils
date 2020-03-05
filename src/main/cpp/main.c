#include <jni.h>
#include "android/log.h"
#include "com_kenvix_android_ApplicationEnvironment.h"
#include "com_kenvix_android_NativeCalls.h"

void initializeApp(JNIEnv *env, jclass clazz, jobject application_environment) {
    jclass appEnvClass = (*env)->GetObjectClass(env, application_environment);

//AppContext Init
//    jmethodID getAppContextM = (*env)->GetStaticMethodID(env, appEnvClass, "getAppContext",
//            "()Landroid/content/Context;");

    jmethodID setAppContextM = (*env)->GetStaticMethodID(env, appEnvClass, "setAppContext",
                                                         "(Landroid/content/Context;)V");
    jmethodID setRootContextM = (*env)->GetStaticMethodID(env, appEnvClass, "setRootContext",
                                                          "(Landroid/content/Context;)V");

    jclass androidContextWrapperClass = (*env)->FindClass(env, "android/content/ContextWrapper");
    jmethodID getApplicationContextM = (*env)->GetMethodID(env, androidContextWrapperClass,
                                                           "getApplicationContext", "()Landroid/content/Context;");
    jmethodID getBaseContextM = (*env)->GetMethodID(env, androidContextWrapperClass,
                                                    "getBaseContext", "()Landroid/content/Context;");

    jobject appContextObj = (*env)->CallObjectMethod(env, application_environment, getApplicationContextM);
    jobject baseContextObj = (*env)->CallObjectMethod(env, application_environment, getBaseContextM);

    (*env)->CallStaticVoidMethod(env, appEnvClass, setAppContextM, appContextObj);
    (*env)->CallStaticVoidMethod(env, appEnvClass, setRootContextM, baseContextObj);

//ApplicationEnvironment init
    jmethodID setInstanceM = (*env)->GetStaticMethodID(env, appEnvClass, "setInstance",
                                                       "(Lcom/kenvix/android/ApplicationEnvironment;)V");
    (*env)->CallStaticVoidMethod(env, appEnvClass, setInstanceM, application_environment);
}

JNIEXPORT void JNICALL
Java_com_kenvix_android_NativeCalls_initialize(JNIEnv *env, jclass clazz,
                                               jobject application_environment) {

    initializeApp(env, clazz, application_environment);
    __android_log_print(ANDROID_LOG_DEBUG,  __FUNCTION__, "Native initialize call finished");
    //(*env)->CallStaticVoidMethod(env, appEnvClass, setAppContext, )
}


int getSignHashCode(JNIEnv *env, jobject context) {

    jclass context_clazz = (*env)->GetObjectClass(env, context);//Context的类

    jmethodID methodID_getPackageManager = (*env)->GetMethodID(env, context_clazz,
                                                               "getPackageManager", "()Landroid/content/pm/PackageManager;");// 得到 getPackageManager 方法的 ID


    jobject packageManager = (*env)->CallObjectMethod(env, context,
                                                      methodID_getPackageManager);// 获得PackageManager对象

    jclass pm_clazz = (*env)->GetObjectClass(env, packageManager);// 获得 PackageManager 类

    jmethodID methodID_pm = (*env)->GetMethodID(env, pm_clazz, "getPackageInfo",
                                                "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");// 得到 getPackageInfo 方法的 ID

    jmethodID methodID_pack = (*env)->GetMethodID(env, context_clazz,
                                                  "getPackageName", "()Ljava/lang/String;");// 得到 getPackageName 方法的 ID


    jstring application_package = (*env)->CallObjectMethod(env, context,
                                                           methodID_pack);// 获得当前应用的包名

    const char *str = (*env)->GetStringUTFChars(env, application_package, 0);
    __android_log_print(ANDROID_LOG_DEBUG, "JNI", "packageName: %s\n", str);

    jobject packageInfo = (*env)->CallObjectMethod(env, packageManager,
                                                   methodID_pm, application_package, 64);// 获得PackageInfo

    jclass packageinfo_clazz = (*env)->GetObjectClass(env, packageInfo);
    jfieldID fieldID_signatures = (*env)->GetFieldID(env, packageinfo_clazz,
                                                     "signatures", "[Landroid/content/pm/Signature;");
    jobjectArray signature_arr = (jobjectArray)(*env)->GetObjectField(env,
                                                                      packageInfo, fieldID_signatures);

    jobject signature = (*env)->GetObjectArrayElement(env, signature_arr, 0);//Signature数组中取出第一个元素

    jclass signature_clazz = (*env)->GetObjectClass(env, signature);//读signature的hashcode
    jmethodID methodID_hashcode = (*env)->GetMethodID(env, signature_clazz,
                                                      "hashCode", "()I");
    jint hashCode = (*env)->CallIntMethod(env, signature, methodID_hashcode);

    __android_log_print(ANDROID_LOG_DEBUG, "JNI", "hashcode: %d\n", hashCode);
    return hashCode;
}

JNIEXPORT void JNICALL
Java_com_kenvix_android_NativeCalls_initializeAsync(JNIEnv *env, jclass clazz) {

    __android_log_print(ANDROID_LOG_DEBUG,  __FUNCTION__, "Native async initialize stage 2 call finished");
}