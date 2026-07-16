@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.DoctorPlan
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.core.model.Review
import com.inclinic.app.features.auth.application.GetSpecialtiesUseCase
import com.inclinic.app.features.auth.fakes.FakeAuthRemoteDataSource
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.auth.infrastructure.local.SpecialtyCacheDataSource
import com.inclinic.app.features.patient.infrastructure.remote.DoctorFilters
import com.inclinic.app.features.patient.infrastructure.remote.DoctorSearchDataSource
import com.inclinic.app.features.patient.infrastructure.remote.PagedDoctors
import com.inclinic.app.features.patient.search.application.SearchDoctorsUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun searchTestDoctor(id: String = "doc-1", name: String = "Dr. Ana Torres"): Doctor = Doctor(
    id = id, fullName = name, email = "ana@test.com", photoUrl = null,
    specialties = emptyList(), plan = DoctorPlan.FREE, ratingAverage = 4.5, ratingsCount = 10,
    consultationFee = 120.0, homeVisitAvailable = false, virtualVisitAvailable = true,
    bio = "Especialista", isVerified = true, cmpLicense = "CMP-123",
    onboardingStatus = OnboardingStatus.APPROVED,
)

private class FakeSearchDoctorSearchDataSource(
    private val searchResult: Result<PagedDoctors> = Result.success(PagedDoctors(listOf(searchTestDoctor()), false)),
) : DoctorSearchDataSource {
    var searchCallCount: Int = 0
    var lastFilters: DoctorFilters? = null
    var lastPage: Int = 0

    override suspend fun searchDoctors(filters: DoctorFilters, page: Int): Result<PagedDoctors> {
        searchCallCount++
        lastFilters = filters
        lastPage = page
        return searchResult
    }

    override suspend fun getDoctorById(doctorId: String): Result<Doctor> =
        Result.success(searchTestDoctor())

    override suspend fun getDoctorReviews(doctorId: String, page: Int): Result<List<Review>> =
        Result.success(emptyList())
}

class DefaultDoctorSearchComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeSearchDoctorSearchDataSource = FakeSearchDoctorSearchDataSource(),
        outputs: MutableList<DoctorSearchComponent.Output> = mutableListOf(),
    ): DefaultDoctorSearchComponent {
        return DefaultDoctorSearchComponent(
            componentContext = ctx,
            searchDoctors = SearchDoctorsUseCase(dataSource, dispatchers),
            getSpecialties = GetSpecialtiesUseCase(
                cache = SpecialtyCacheDataSource(remote = FakeAuthRemoteDataSource()),
                dispatchers = dispatchers,
            ),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun initial_load_populates_results() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.results.size)
        assertEquals("doc-1", state.results.first().id)
        assertNull(state.error)
    }

    @Test
    fun initial_load_failure_sets_error() = runTest {
        val ds = FakeSearchDoctorSearchDataSource(searchResult = Result.failure(Exception("Network error")))
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertTrue(state.results.isEmpty())
        assertNotNull(state.error)
    }

    @Test
    fun onQueryChange_updates_query_in_state() = runTest {
        val component = createComponent()

        component.onQueryChange("cardio")

        assertEquals("cardio", component.state.value.query)
    }

    @Test
    fun onSpecialtyChange_updates_selectedSpecialty_and_resets_page() = runTest {
        val component = createComponent()

        component.onSpecialtyChange("Cardiología")

        val state = component.state.value
        assertEquals("Cardiología", state.selectedSpecialty)
        assertEquals(1, state.page)
    }

    @Test
    fun onSortChange_updates_sortOrder() = runTest {
        val component = createComponent()

        component.onSortChange(DoctorSortOrder.TopRated)

        assertEquals(DoctorSortOrder.TopRated, component.state.value.sortOrder)
    }

    @Test
    fun onMinPriceChange_updates_minPrice() = runTest {
        val component = createComponent()

        component.onMinPriceChange(50.0)

        assertEquals(50.0, component.state.value.minPrice)
    }

    @Test
    fun onMaxPriceChange_updates_maxPrice() = runTest {
        val component = createComponent()

        component.onMaxPriceChange(200.0)

        assertEquals(200.0, component.state.value.maxPrice)
    }

    @Test
    fun onDoctorTapped_emits_NavigateToDoctorProfile() = runTest {
        val outputs = mutableListOf<DoctorSearchComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onDoctorTapped("doc-1")

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is DoctorSearchComponent.Output.NavigateToDoctorProfile)
        assertEquals("doc-1", (output as DoctorSearchComponent.Output.NavigateToDoctorProfile).doctorId)
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val ds = FakeSearchDoctorSearchDataSource(searchResult = Result.failure(Exception("Fail")))
        val component = createComponent(dataSource = ds)
        assertNotNull(component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }

    @Test
    fun onLoadMore_increments_page_when_hasMore() = runTest {
        val ds = FakeSearchDoctorSearchDataSource(
            searchResult = Result.success(PagedDoctors(listOf(searchTestDoctor()), hasMore = true)),
        )
        val component = createComponent(dataSource = ds)
        assertEquals(1, component.state.value.page)

        component.onLoadMore()

        assertEquals(2, component.state.value.page)
    }

    @Test
    fun onApplyFilters_updates_all_advanced_fields_and_resets_page() = runTest {
        val component = createComponent()
        component.onLoadMore() // bump page to 2 first

        component.onApplyFilters(
            minPrice = 50.0,
            maxPrice = 200.0,
            minRating = 4.0,
            offersTelemedicine = true,
            offersHomeVisit = false,
            sortOrder = DoctorSortOrder.PriceAsc,
        )

        val state = component.state.value
        assertEquals(50.0, state.minPrice)
        assertEquals(200.0, state.maxPrice)
        assertEquals(4.0, state.minRating)
        assertEquals(true, state.offersTelemedicine)
        assertEquals(false, state.offersHomeVisit)
        assertEquals(DoctorSortOrder.PriceAsc, state.sortOrder)
        assertEquals(1, state.page)
    }

    @Test
    fun onApplyFilters_forwards_advanced_filters_to_datasource() = runTest {
        val ds = FakeSearchDoctorSearchDataSource()
        val component = createComponent(dataSource = ds)

        component.onApplyFilters(
            minPrice = 50.0,
            maxPrice = 200.0,
            minRating = 4.0,
            offersTelemedicine = true,
            offersHomeVisit = true,
            sortOrder = DoctorSortOrder.PriceDesc,
        )

        val filters = ds.lastFilters
        assertNotNull(filters)
        assertEquals(50.0, filters.minPrice)
        assertEquals(200.0, filters.maxPrice)
        assertEquals(4.0, filters.minRating)
        assertEquals(true, filters.offersTelemedicine)
        assertEquals(true, filters.offersHomeVisit)
        assertEquals("price_desc", filters.sortBy)
    }

    @Test
    fun onResetFilters_clears_advanced_filters_and_keeps_query() = runTest {
        val component = createComponent()
        component.onQueryChange("cardio")
        component.onApplyFilters(
            minPrice = 50.0, maxPrice = 200.0, minRating = 4.0,
            offersTelemedicine = true, offersHomeVisit = true,
            sortOrder = DoctorSortOrder.PriceAsc,
        )

        component.onResetFilters()

        val state = component.state.value
        assertNull(state.minPrice)
        assertNull(state.maxPrice)
        assertNull(state.minRating)
        assertNull(state.offersTelemedicine)
        assertNull(state.offersHomeVisit)
        assertEquals(DoctorSortOrder.Recent, state.sortOrder)
        assertEquals("cardio", state.query)
        assertEquals(1, state.page)
    }
}
