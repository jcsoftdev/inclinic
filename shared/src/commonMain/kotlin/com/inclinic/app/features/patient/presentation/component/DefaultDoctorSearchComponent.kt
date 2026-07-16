package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.GetSpecialtiesUseCase
import com.inclinic.app.features.patient.infrastructure.remote.DoctorFilters
import com.inclinic.app.features.patient.infrastructure.remote.PagedDoctors
import com.inclinic.app.features.patient.search.application.SearchDoctorsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DefaultDoctorSearchComponent(
    componentContext: ComponentContext,
    private val searchDoctors: SearchDoctorsUseCase,
    private val getSpecialties: GetSpecialtiesUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (DoctorSearchComponent.Output) -> Unit,
) : DoctorSearchComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    /**
     * Restore persisted filters from [StateKeeper] after a configuration change.
     * Results are not persisted (see [DoctorSearchState] docs); they are re-fetched.
     *
     * REQ-4-009
     */
    private val savedState: DoctorSearchState? =
        stateKeeper.consume("search_state", DoctorSearchState.serializer())

    private val _state = MutableValue(savedState ?: DoctorSearchState())
    override val state: Value<DoctorSearchState> = _state

    private var searchJob: Job? = null

    init {
        // Register save callback so Decompose persists current state before the
        // process is torn down (Android rotation, RAM pressure, etc.).
        stateKeeper.register("search_state", DoctorSearchState.serializer()) { _state.value }
        search(page = 1, reset = true)
        loadSpecialties()
    }

    private fun loadSpecialties() {
        scope.launch {
            getSpecialties().onSuccess { specialties ->
                _state.update { it.copy(specialties = specialties) }
            }
        }
    }

    override fun onQueryChange(query: String) {
        _state.update { it.copy(query = query, page = 1) }
        debounceSearch()
    }

    override fun onSpecialtyChange(specialty: String?) {
        _state.update { it.copy(selectedSpecialty = specialty, page = 1) }
        search(page = 1, reset = true)
    }

    override fun onSortChange(sort: DoctorSortOrder) {
        _state.update { it.copy(sortOrder = sort, page = 1) }
        search(page = 1, reset = true)
    }

    override fun onMinPriceChange(price: Double?) {
        _state.update { it.copy(minPrice = price, page = 1) }
        search(page = 1, reset = true)
    }

    override fun onMaxPriceChange(price: Double?) {
        _state.update { it.copy(maxPrice = price, page = 1) }
        search(page = 1, reset = true)
    }

    override fun onApplyFilters(
        minPrice: Double?,
        maxPrice: Double?,
        minRating: Double?,
        offersTelemedicine: Boolean?,
        offersHomeVisit: Boolean?,
        sortOrder: DoctorSortOrder,
    ) {
        _state.update {
            it.copy(
                minPrice = minPrice,
                maxPrice = maxPrice,
                minRating = minRating,
                offersTelemedicine = offersTelemedicine,
                offersHomeVisit = offersHomeVisit,
                sortOrder = sortOrder,
                page = 1,
            )
        }
        search(page = 1, reset = true)
    }

    override fun onResetFilters() {
        _state.update {
            it.copy(
                minPrice = null,
                maxPrice = null,
                minRating = null,
                offersTelemedicine = null,
                offersHomeVisit = null,
                sortOrder = DoctorSortOrder.Recent,
                page = 1,
            )
        }
        search(page = 1, reset = true)
    }

    override fun onLoadMore() {
        val s = _state.value
        if (s.isLoading || !s.hasMore) return
        val nextPage = s.page + 1
        _state.update { it.copy(page = nextPage) }
        search(page = nextPage, reset = false)
    }

    override fun onDoctorTapped(doctorId: String) {
        onOutput(DoctorSearchComponent.Output.NavigateToDoctorProfile(doctorId))
    }

    override fun onErrorDismissed() { _state.update { it.copy(error = null) } }

    private fun debounceSearch() {
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(300)
            search(page = 1, reset = true)
        }
    }

    private fun search(page: Int, reset: Boolean) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val s = _state.value
            val sortBy = when (s.sortOrder) {
                DoctorSortOrder.Recent -> "newest"
                DoctorSortOrder.TopRated -> "rating_desc"
                DoctorSortOrder.PriceAsc -> "price_asc"
                DoctorSortOrder.PriceDesc -> "price_desc"
            }
            val filters = DoctorFilters(
                query = s.query.takeIf { it.isNotBlank() },
                specialty = s.selectedSpecialty,
                minPrice = s.minPrice,
                maxPrice = s.maxPrice,
                minRating = s.minRating,
                offersTelemedicine = s.offersTelemedicine,
                offersHomeVisit = s.offersHomeVisit,
                sortBy = sortBy,
            )
            searchDoctors(filters, page)
                .onSuccess { paged ->
                    _state.update { it.copy(
                        isLoading = false,
                        results = if (reset) paged.doctors else (it.results + paged.doctors).distinctBy { d -> d.id },
                        hasMore = paged.hasMore,
                    ) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Search failed")) }
                }
        }
    }
}
