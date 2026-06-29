package com.inclinic.app.features.doctor.profile.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.application.GetDoctorIncomeUseCase
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultIncomeComponent
import com.inclinic.app.features.doctor.profile.presentation.component.IncomeComponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultIncomeComponentTest {

    private val lifecycle = LifecycleRegistry()
    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()

    private fun makeComponent(
        onOutput: (IncomeComponent.Output) -> Unit = {},
    ): DefaultIncomeComponent {
        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        return DefaultIncomeComponent(
            componentContext = ctx,
            getIncome = GetDoctorIncomeUseCase(fakeRepo, dispatchers),
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
    }

    @Test
    fun loads_income_on_init() = runTest {
        val component = makeComponent()

        assertNotNull(component.state.value.summary)
        assertEquals(18000L, component.state.value.summary?.totalCents)
        assertEquals(1, fakeRepo.getIncomeCallCount)
    }

    @Test
    fun onRetry_reloads() = runTest {
        fakeRepo.getIncomeResult = Result.failure(RuntimeException("Timeout"))
        val component = makeComponent()
        assertNotNull(component.state.value.error)

        fakeRepo.getIncomeResult = Result.success(FakeDoctorProfileRepository.defaultIncome)
        component.onRetry()

        assertNotNull(component.state.value.summary)
        assertNull(component.state.value.error)
    }

    @Test
    fun sets_error_when_load_fails() = runTest {
        fakeRepo.getIncomeResult = Result.failure(RuntimeException("Network error"))
        val component = makeComponent()

        assertNotNull(component.state.value.error)
        assertNull(component.state.value.summary)
    }

    @Test
    fun onBack_emits_Back_output() {
        var output: IncomeComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onBack()

        assertTrue(output is IncomeComponent.Output.Back)
    }
}
