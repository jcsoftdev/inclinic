package com.inclinic.app.features.doctor.onboarding.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.onboarding.core.model.DoctorOnboardingDraft
import com.inclinic.app.features.doctor.onboarding.core.model.PersonalData
import com.inclinic.app.features.doctor.onboarding.core.model.PriceConfig
import com.inclinic.app.features.doctor.onboarding.core.model.WeeklySchedule
import com.inclinic.app.features.doctor.onboarding.fakes.FakeDoctorOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubmitOnboardingUseCaseTest {

    private val fakeRepo = FakeDoctorOnboardingRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = SubmitOnboardingUseCase(repository = fakeRepo, dispatchers = dispatchers)

    private val sampleDraft = DoctorOnboardingDraft(
        personalData = PersonalData(
            firstName = "Carlos",
            lastName = "Ramirez",
            cmpLicense = "CMP-12345",
            phone = "+51999000111",
        ),
        documents = emptyList(),
        specialties = listOf("sp-cardiology"),
        schedule = WeeklySchedule(slots = emptyMap()),
        prices = PriceConfig(consultationFee = 80.0, supportsPresential = true, supportsVirtual = false),
    )

    @Test
    fun happy_path_returns_success_and_delegates_draft() = runTest {
        fakeRepo.submitResult = Result.success(Unit)

        val result = useCase(sampleDraft)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepo.submitCallCount)
        assertEquals(sampleDraft, fakeRepo.lastSubmittedDraft)
    }

    @Test
    fun propagates_repository_error() = runTest {
        val error = RuntimeException("Server error")
        fakeRepo.submitResult = Result.failure(error)

        val result = useCase(sampleDraft)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        assertEquals(1, fakeRepo.submitCallCount)
    }
}
