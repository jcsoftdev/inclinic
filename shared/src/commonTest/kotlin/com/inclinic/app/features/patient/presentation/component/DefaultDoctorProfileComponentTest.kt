@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.DoctorPlan
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.core.model.Review
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorDetailUseCase
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorReviewsUseCase
import com.inclinic.app.features.patient.infrastructure.remote.DoctorFilters
import com.inclinic.app.features.patient.infrastructure.remote.DoctorSearchDataSource
import com.inclinic.app.features.patient.infrastructure.remote.PagedDoctors
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun profileTestDoctor(id: String = "doc-1"): Doctor = Doctor(
    id = id, fullName = "Dr. Roberto Mendez", email = "roberto@test.com", photoUrl = null,
    specialties = emptyList(), plan = DoctorPlan.FREE, ratingAverage = 4.8, ratingsCount = 30,
    consultationFee = 150.0, homeVisitAvailable = true, virtualVisitAvailable = true,
    bio = "Cardiólogo con 10 años de experiencia", isVerified = true, cmpLicense = "CMP-456",
    onboardingStatus = OnboardingStatus.APPROVED,
)

private fun profileTestReview(id: String = "rev-1"): Review = Review(
    id = id, doctorId = "doc-1", patientId = "pat-1", patientName = "Juan López",
    rating = 5, comment = "Excelente doctor", createdAt = Clock.System.now(),
)

private class FakeProfileDoctorSearchDataSource(
    private val doctorResult: Result<Doctor> = Result.success(profileTestDoctor()),
    private val reviewsResult: Result<List<Review>> = Result.success(listOf(profileTestReview())),
) : DoctorSearchDataSource {
    override suspend fun searchDoctors(filters: DoctorFilters, page: Int): Result<PagedDoctors> =
        Result.success(PagedDoctors(emptyList(), false))

    override suspend fun getDoctorById(doctorId: String): Result<Doctor> = doctorResult

    override suspend fun getDoctorReviews(doctorId: String, page: Int): Result<List<Review>> = reviewsResult
}

class DefaultDoctorProfileComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        doctorId: String = "doc-1",
        dataSource: FakeProfileDoctorSearchDataSource = FakeProfileDoctorSearchDataSource(),
        outputs: MutableList<DoctorProfileComponent.Output> = mutableListOf(),
    ): DefaultDoctorProfileComponent {
        return DefaultDoctorProfileComponent(
            componentContext = ctx,
            doctorId = doctorId,
            getDoctorDetail = GetDoctorDetailUseCase(dataSource, dispatchers),
            getDoctorReviews = GetDoctorReviewsUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_doctor_and_reviews_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.doctor)
        assertEquals("doc-1", state.doctor?.id)
        assertEquals("Dr. Roberto Mendez", state.doctor?.fullName)
        assertEquals(1, state.reviews.size)
        assertEquals("rev-1", state.reviews.first().id)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_and_no_doctor() = runTest {
        val ds = FakeProfileDoctorSearchDataSource(doctorResult = Result.failure(Exception("Not found")))
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.doctor)
        assertEquals("Not found", state.error)
    }

    @Test
    fun load_with_no_reviews_sets_hasMoreReviews_false() = runTest {
        val ds = FakeProfileDoctorSearchDataSource(reviewsResult = Result.success(emptyList()))
        val component = createComponent(dataSource = ds)

        assertFalse(component.state.value.hasMoreReviews)
        assertTrue(component.state.value.reviews.isEmpty())
    }

    @Test
    fun onLoadMoreReviews_increments_page_and_appends_reviews() = runTest {
        val ds = FakeProfileDoctorSearchDataSource(
            reviewsResult = Result.success(listOf(profileTestReview())),
        )
        val component = createComponent(dataSource = ds)
        assertEquals(1, component.state.value.reviews.size)

        component.onLoadMoreReviews()

        assertEquals(2, component.state.value.reviewsPage)
        assertEquals(2, component.state.value.reviews.size)
    }

    @Test
    fun onBookTapped_emits_NavigateToAvailability_with_doctorId() = runTest {
        val outputs = mutableListOf<DoctorProfileComponent.Output>()
        val component = createComponent(doctorId = "doc-1", outputs = outputs)

        component.onBookTapped()

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is DoctorProfileComponent.Output.NavigateToAvailability)
        assertEquals("doc-1", (output as DoctorProfileComponent.Output.NavigateToAvailability).doctorId)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<DoctorProfileComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is DoctorProfileComponent.Output.Back)
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val ds = FakeProfileDoctorSearchDataSource(doctorResult = Result.failure(Exception("Load failed")))
        val component = createComponent(dataSource = ds)
        assertNotNull(component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }
}
