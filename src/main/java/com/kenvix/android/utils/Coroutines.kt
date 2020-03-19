package com.kenvix.android.utils

import com.kenvix.android.exception.LifecycleEndException
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.Function
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

class Coroutines : AutoCloseable {
    /**
     * 获取默认 Kotlin Coroutine Job (线程不安全)
     * @return CompletableJob
     */
    val defaultJob = Job()

    /**
     * 获取默认 Kotlin Coroutine Scope (线程不安全)
     * @return CoroutineScope
     */
    val defaultScope: CoroutineScope by lazy(LazyThreadSafetyMode.NONE) { CoroutineScope(Default + defaultJob) }

    /**
     * 获取主(UI) Kotlin Coroutine Scope (线程不安全)
     * @return CoroutineScope
     */
    val mainScope: CoroutineScope by lazy(LazyThreadSafetyMode.NONE) { CoroutineScope(Main + defaultJob) }

    /**
     * 获取IO Kotlin Coroutine Scope (线程不安全)
     * @return CoroutineScope
     */
    val ioScope: CoroutineScope by lazy(LazyThreadSafetyMode.NONE) { CoroutineScope(IO + defaultJob) }

    /**
     * 异步运行指定代码（仅用于Java代码）
     */
    @JvmOverloads
    fun runAsync(run: Call, dispatcher: CoroutineDispatcher = Default) {
        defaultScope.launch(dispatcher) {
            run()
        }
    }

    /**
     * 获取在 Java 代码中调用 Kotlin Suspend 函数所需的最后一个参数 (Continuation)
     * @param onFinished 当suspend函数执行完毕后所调用的回调。若 Throwable 不为 null 则说明执行失败。否则为执行成功
     * @param taskDispatcher 协程执行线程的类型。可以为 Dispatchers.Default(CPU密集型) Dispatchers.Main(主线程) Dispatchers.IO(IO密集型)
     */
    @JvmOverloads
    fun <R> getContinuation(
        onFinished: BiConsumer<R?, Throwable?>,
        taskDispatcher: CoroutineDispatcher = Default,
        callbackDispatcher: CoroutineDispatcher? = null
    ): Continuation<R> {
        return object : Continuation<R> {
            override val context: CoroutineContext
                get() = taskDispatcher

            override fun resumeWith(result: Result<R>) {
                if (callbackDispatcher == null || callbackDispatcher == taskDispatcher)
                    onFinished.accept(result.getOrNull(), result.exceptionOrNull())
                else
                    defaultScope.launch(callbackDispatcher)
                        { onFinished.accept(result.getOrNull(), result.exceptionOrNull()) }
            }
        }
    }

    /**
     * 立刻停止所有协程并标记job取消。不允许调用第二次
     */
    override fun close() {
        defaultJob.cancel(LifecycleEndException())
    }

    @FunctionalInterface
    interface Call {
        operator fun invoke()
    }
}