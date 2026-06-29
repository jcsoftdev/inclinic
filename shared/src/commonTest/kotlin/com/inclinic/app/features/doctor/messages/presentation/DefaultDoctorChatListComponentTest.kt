package com.inclinic.app.features.doctor.messages.presentation

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.messages.application.GetDoctorChatThreadsUseCase
import com.inclinic.app.features.doctor.messages.core.port.ThreadFilter
import com.inclinic.app.features.doctor.messages.fakes.FakeDoctorMessagesRepository
import com.inclinic.app.features.doctor.messages.fakes.stubThread
import com.inclinic.app.features.doctor.messages.presentation.component.DefaultDoctorChatListComponent
import com.inclinic.app.features.doctor.messages.presentation.component.DoctorChatListComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class DefaultDoctorChatListComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val repo = FakeDoctorMessagesRepository()

    private fun createComponent(
        onOutput: (DoctorChatListComponent.Output) -> Unit = {},
    ) = DefaultDoctorChatListComponent(
        componentContext = ctx,
        getThreads = GetDoctorChatThreadsUseCase(repo, dispatchers),
        dispatchers = dispatchers,
        onOutput = onOutput,
    )

    @Test
    fun initial_filter_is_all() {
        val component = createComponent()
        assertEquals(ThreadFilter.ALL, component.state.value.activeFilter)
    }

    @Test
    fun loads_threads_on_init() = runTest {
        repo.listThreadsResult = Result.success(listOf(stubThread("t1"), stubThread("t2")))
        val component = createComponent()
        assertEquals(2, component.state.value.threads.size)
    }

    @Test
    fun filter_change_to_unread_shows_only_unread_threads() = runTest {
        repo.listThreadsResult = Result.success(listOf(
            stubThread("t1", unread = true),
            stubThread("t2", unread = false),
        ))
        val component = createComponent()
        component.onFilterChange(ThreadFilter.UNREAD)
        assertEquals(ThreadFilter.UNREAD, component.state.value.activeFilter)
        // After filter change and reload, only unread threads shown
        assertEquals(1, component.state.value.threads.size)
        assertEquals("t1", component.state.value.threads[0].id)
    }

    @Test
    fun thread_click_passes_otherPartyId_to_navigate_output() {
        var output: DoctorChatListComponent.Output? = null
        val component = createComponent(onOutput = { output = it })
        component.onThreadClick("patient-42")
        assertEquals(DoctorChatListComponent.Output.NavigateToConversation("patient-42"), output)
    }

    @Test
    fun on_back_emits_back_output() {
        var output: DoctorChatListComponent.Output? = null
        val component = createComponent(onOutput = { output = it })
        component.onBack()
        assertEquals(DoctorChatListComponent.Output.Back, output)
    }

    @Test
    fun error_is_shown_when_load_fails() = runTest {
        repo.listThreadsResult = Result.failure(RuntimeException("Load failed"))
        val component = createComponent()
        assertEquals("Load failed", component.state.value.error)
    }

    @Test
    fun state_is_not_loading_after_successful_load() = runTest {
        repo.listThreadsResult = Result.success(emptyList())
        val component = createComponent()
        component.state.asFlow().test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private fun <T : Any> com.arkivanov.decompose.value.Value<T>.asFlow(): Flow<T> = callbackFlow {
    val cancellation = subscribe { trySend(it) }
    awaitClose { cancellation.cancel() }
}
