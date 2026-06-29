package com.inclinic.app.features.doctor.profile.presentation

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.application.GetMySpecialtyRequestsUseCase
import com.inclinic.app.features.doctor.profile.core.model.SpecialtyRequestStatus
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultMySpecialtyRequestsComponent
import com.inclinic.app.features.doctor.profile.presentation.component.MySpecialtyRequestsComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultMySpecialtyRequestsComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val repo = FakeDoctorProfileRepository()

    private fun createComponent(
        onOutput: (MySpecialtyRequestsComponent.Output) -> Unit = {},
    ) = DefaultMySpecialtyRequestsComponent(
        componentContext = ctx,
        getMyRequests = GetMySpecialtyRequestsUseCase(repo, dispatchers),
        dispatchers = dispatchers,
        onOutput = onOutput,
    )

    @Test
    fun loads_requests_on_init() = runTest {
        val component = createComponent()

        assertEquals(3, component.state.value.requests.size)
        assertEquals(SpecialtyRequestStatus.Pending, component.state.value.requests.first().status)
        assertEquals(1, repo.getMySpecialtyRequestsCallCount)
    }

    @Test
    fun sets_error_when_load_fails() = runTest {
        repo.getMySpecialtyRequestsResult = Result.failure(RuntimeException("Network error"))
        val component = createComponent()

        assertNotNull(component.state.value.error)
        assertTrue(component.state.value.requests.isEmpty())
    }

    @Test
    fun onRetry_reloads() = runTest {
        repo.getMySpecialtyRequestsResult = Result.failure(RuntimeException("Timeout"))
        val component = createComponent()
        assertNotNull(component.state.value.error)

        repo.getMySpecialtyRequestsResult =
            Result.success(FakeDoctorProfileRepository.defaultRequests)
        component.onRetry()

        assertNull(component.state.value.error)
        assertEquals(3, component.state.value.requests.size)
    }

    @Test
    fun onBack_emits_Back_output() {
        var output: MySpecialtyRequestsComponent.Output? = null
        val component = createComponent(onOutput = { output = it })

        component.onBack()

        assertEquals(MySpecialtyRequestsComponent.Output.Back, output)
    }

    @Test
    fun onRequestNew_emits_RequestNew_output() {
        var output: MySpecialtyRequestsComponent.Output? = null
        val component = createComponent(onOutput = { output = it })

        component.onRequestNew()

        assertEquals(MySpecialtyRequestsComponent.Output.RequestNew, output)
    }

    @Test
    fun state_settles_without_loading_via_flow() = runTest {
        val component = createComponent()

        component.state.asFlow().test {
            val loaded = expectMostRecentItem()
            assertFalse(loaded.isLoading)
            assertNull(loaded.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private fun <T : Any> Value<T>.asFlow(): Flow<T> = callbackFlow {
    val cancellation = subscribe { trySend(it) }
    awaitClose { cancellation.cancel() }
}
