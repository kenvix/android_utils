package com.kenvix.android.service.base;

import android.app.Service;

import com.kenvix.android.utils.Coroutines;
import com.kenvix.utils.log.Logging;

public abstract class BaseService extends Service implements Logging {
    private String logTag;
    private Coroutines coroutines;
    private Exception initException = null;
    private boolean catchInitException = false;

    public BaseService() {
    }

    protected BaseService(boolean catchInitException) {
        this.catchInitException = catchInitException;
    }

    @Override
    public String getLogTag() {
        return logTag == null ? (logTag = this.getClass().getSimpleName()) : logTag;
    }

    /**
     * 获取 Kotlin 协程操作类
     *
     * @return
     */
    public Coroutines getCoroutines() {
        return coroutines == null ? coroutines = new Coroutines() : coroutines;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (catchInitException) {
            try {
                onInitialize();
            } catch (Exception exception) {
                initException = exception;
                stopSelf();
            }
        } else {
            onInitialize();
        }

        getLogger().finest("Created service");
    }

    public Exception getInitException() {
        return initException;
    }

    @Override
    public void onDestroy() {
        if (coroutines != null)
            coroutines.close();

        coroutines = null;

        super.onDestroy();
    }

    protected abstract void onInitialize();
}
