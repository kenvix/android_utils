package com.kenvix.android.utils;

import com.kenvix.android.exception.LifecycleEndException;

import org.jetbrains.annotations.NotNull;

import kotlinx.coroutines.CompletableJob;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.JobKt;

public class Coroutines implements AutoCloseable {
    private CompletableJob defaultJob = null;
    private CoroutineScope defaultScope = null;
    private CoroutineScope mainScope = null;
    private CoroutineScope ioScope = null;

    /**
     * 获取默认 Kotlin Coroutine Job (线程不安全)
     * @return CompletableJob
     */
    @NotNull
    public CompletableJob getDefaultJob() {
        return defaultJob == null ? (defaultJob = JobKt.Job(null)) : defaultJob;
    }

    /**
     * 获取默认 Kotlin Coroutine Scope (线程不安全)
     * @return CoroutineScope
     */
    @NotNull
    public CoroutineScope getDefaultScope() {
        return defaultScope == null ?
                (defaultScope = CoroutineScopeKt.CoroutineScope(Dispatchers.getDefault().plus(getDefaultJob())))
                : defaultScope;
    }

    /**
     * 获取主(UI) Kotlin Coroutine Scope (线程不安全)
     * @return CoroutineScope
     */
    @NotNull
    public CoroutineScope getMainScope() {
        return mainScope == null ?
                (mainScope = CoroutineScopeKt.CoroutineScope(Dispatchers.getMain().plus(getDefaultJob())))
                : mainScope;
    }

    /**
     * 获取IO Kotlin Coroutine Scope (线程不安全)
     * @return CoroutineScope
     */
    public CoroutineScope getIoScope() {
        return ioScope == null ?
                (ioScope = CoroutineScopeKt.CoroutineScope(Dispatchers.getIO().plus(getDefaultJob())))
                : ioScope;
    }

    /**
     * 立刻停止所有协程并标记job取消。不允许调用第二次
     */
    @Override
    public void close() {
        if (defaultJob != null) {
            defaultJob.cancel(new LifecycleEndException());
        }
    }
}
