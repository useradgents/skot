@file:OptIn(ExperimentalCoroutinesApi::class)

package tech.skot.core.components

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.skot.core.view.SKPermission
import tech.skot.core.view.Style
import tech.skot.model.DatedData
import tech.skot.model.SKData

@OptIn(DelicateCoroutinesApi::class)
class TestUseSKData {
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    open class IncrementingSimpleSKData : SKData<Int> {
        override val defaultValidity = 200L
        override val flow = MutableStateFlow<DatedData<Int>?>(null)
        override val _current
            get() = flow.value

        override suspend fun update(): Int {
            val newDatedValue = newDatedData()
            flow.value = newDatedValue
            return newDatedValue.data
        }

        @Suppress("MemberVisibilityCanBePrivate", "RedundantSuspendModifier")
        suspend fun newDatedData(): DatedData<Int> {
            return DatedData((_current?.data ?: -1) + 1)
        }

        override suspend fun fallBackValue(): Int {
            println("fallBackValue used")
            return -1
        }
    }

    @Test
    fun `Component SKData_onData`() {
        val incData = IncrementingSimpleSKData()

        val compo =
            object : SKComponent<SKComponentVC>() {
                override val view =
                    object : SKComponentVC {
                        override fun displayMessage(message: SKComponentVC.Message) {
                        }

                        @Deprecated(
                            "Use  SKComponent.displayMessageError(message) or  view.displayMessage(SKComponentVC.Message.Error(message))",
                        )
                        override fun displayErrorMessage(message: String) {
                        }

                        override fun closeKeyboard() {
                        }

                        override fun onRemove() {
                        }

                        override fun requestPermissions(
                            permissions: List<SKPermission>,
                            onResult: (permissionsOk: List<SKPermission>) -> Unit,
                        ) {
                        }

                        override fun hasPermission(vararg permission: SKPermission): Boolean {
                            return false
                        }

                        override fun notificationsPermissionManaged(): Boolean {
                            return true
                        }

                        override fun hasNotificationsPermission(): Boolean {
                            return true
                        }

                        override fun requestNotificationsPermissions(
                            onOk: () -> Unit,
                            onKo: (() -> Unit)?,
                        ) {
                        }

                        override var style: Style? = null
                    }

                private var counter = 0

                fun test() {
                    launch {
                        incData.onData(withLoaderForFirstData = false) {
                            assert(it == counter)
                            counter++
                        }
                    }
                }

                override fun treatError(
                    exception: Exception,
                    defaultErrorMessage: String?,
                ) {
                    println("error ${exception.message}")
                }
            }

        compo.test()
        runBlocking {
            delay(1000)
            incData.update()
            delay(1000)
            incData.update()
        }
    }
}
