package com.kenvix.android;

public class NativeCalls {
    static {
        System.loadLibrary("utils-main-lib");
    }


    public static native void initialize(ApplicationEnvironment applicationEnvironment);
    public static native void initializeAsync();
}
