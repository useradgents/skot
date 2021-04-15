package tech.skot.core.components

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import tech.skot.core.Poker
import tech.skot.core.SKLog
import tech.skot.model.SKData
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class Component<out V : ComponentVC> : CoroutineScope {
    abstract val view: V

    companion object {
        var errorTreatment: ((component: Component<*>, exception: Exception, errorMessage: String?) -> Unit)? = null
    }

    protected val job = SupervisorJob()
    override val coroutineContext = CoroutineScope(Dispatchers.Main + job).coroutineContext

    open fun onRemove() {
        //remove des Pokers
        removeObservers.forEach { it.invoke() }
        job.cancel()
    }


    private val noCrashExceptionHandler = CoroutineExceptionHandler { _, e ->
        SKLog.i("launchNoCrash ${e.message}")
    }

    protected fun launchNoCrash(start: CoroutineStart = CoroutineStart.DEFAULT,
                                block: suspend CoroutineScope.() -> Unit): Job =
            launch(noCrashExceptionHandler, start, block)


    open val loader: Loader? = null

    open fun treatError(exception: Exception, errorMessage: String?) {
        errorTreatment?.invoke(this, exception, errorMessage)
                ?: throw IllegalStateException("Valorise Component.errorTreatment or override treatError function to use method treating errors")
    }

    fun launchWithOptions(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            withLoader: Boolean = false,
            specificErrorTreatment:((ex:Exception)->Unit)? = null,
            errorMessage: String? = null,
            block: suspend CoroutineScope.() -> Unit): Job =
            if (withLoader && loader == null) {
                throw IllegalStateException("You have to override loader property to launchWithLoader")
            } else {
                launch(context, start) {
                    if (withLoader) {
                        loader?.workStarted()
                    }
                    try {
                        block()
                    } catch (ex: Exception) {
                        if (ex !is CancellationException) {
                            specificErrorTreatment?.invoke(ex) ?: treatError(ex, errorMessage)
                        }
                    } finally {
                        if (withLoader) {
                            loader?.workEnded()
                        }
                    }

                }
            }

    fun launchWithLoaderAndErrors(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            errorMessage: String? = null,
            block: suspend CoroutineScope.() -> Unit) {
        launchWithOptions (
                context = context,
                start = start,
                errorMessage = errorMessage,
                withLoader = true,
                block = block
        )
    }

    private var removeObservers: MutableSet<() -> Unit> = mutableSetOf()
    private fun addRemoveObserver(observer: () -> Unit) {
        removeObservers.add(observer)
    }

    private fun removeRemoveObserver(observer: () -> Unit) {
        removeObservers.remove(observer)
    }

    fun observe(poker: Poker, onPoke: () -> Unit) {
        poker.addObserver(onPoke)
        addRemoveObserver { poker.removeObserver(onPoke) }
    }

    fun logD(message: Any?) {
        SKLog.d("${this::class.simpleName} -- $message")
    }

    fun logE(message: Any? = "", throwable: Throwable) {
        SKLog.e("${this::class.simpleName} -- $message", throwable)
    }


    fun <D : Any> SKData<D>.onData(
            validity: Long? = null,
            withLoaderForFirstData: Boolean = true,
            fallBackDataBeforeFirstDataLoaded: Boolean = false,
            fallBackDataIfError:Boolean = false,
            treatErrors:Boolean = true,
            defaultErrorMessage: String? = null,
            block: (d: D) -> Unit
    ) {
        if (fallBackDataBeforeFirstDataLoaded) {
            fallBackValue()?.let(block)
        }
        launchWithOptions (
                withLoader = withLoaderForFirstData,
                specificErrorTreatment = { ex ->
                    if (fallBackDataIfError) {
                        fallBackValue()?.let(block)
                    }
                    if (treatErrors) {
                        treatError(ex, defaultErrorMessage)
                    }
                }

        ) {
            block(get(validity))
        }
        launchNoCrash {
            flow.drop(1).collect {
                it?.data?.let(block)
            }
        }

    }

}
