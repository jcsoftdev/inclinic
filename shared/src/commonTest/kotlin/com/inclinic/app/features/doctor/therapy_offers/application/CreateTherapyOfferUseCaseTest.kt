package com.inclinic.app.features.doctor.therapy_offers.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.therapy_offers.core.model.NewOfferDraft
import com.inclinic.app.features.doctor.therapy_offers.fakes.FakeDoctorTherapyOffersRepository
import com.inclinic.app.features.doctor.therapy_offers.fakes.offerFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateTherapyOfferUseCaseTest {

    private val repo = FakeDoctorTherapyOffersRepository()
    private val useCase = CreateTherapyOfferUseCase(repo, TestAppDispatchers())

    private fun draft(title: String = "Cardio Premium") = NewOfferDraft(
        title = title,
        specialtyId = "spe-1",
        totalSessions = 8,
        pricePerSession = 80.0,
        minPricePerSession = null,
        sessionDurationMin = null,
        description = null,
        isActive = true,
    )

    @Test
    fun returns_created_offer() = runTest {
        repo.createResult = Result.success(offerFixture(id = "new-1", title = "Cardio Premium"))
        val result = useCase(draft("Cardio Premium"))
        assertTrue(result.isSuccess)
        assertEquals("new-1", result.getOrThrow().id)
        assertEquals("Cardio Premium", result.getOrThrow().title)
    }

    @Test
    fun passes_draft_to_repository() = runTest {
        useCase(draft("Terapia Nueva"))
        assertEquals("Terapia Nueva", repo.lastCreatedDraft?.title)
        assertEquals("spe-1", repo.lastCreatedDraft?.specialtyId)
        assertEquals(8, repo.lastCreatedDraft?.totalSessions)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.createResult = Result.failure(RuntimeException("validation error"))
        val result = useCase(draft())
        assertTrue(result.isFailure)
    }
}
