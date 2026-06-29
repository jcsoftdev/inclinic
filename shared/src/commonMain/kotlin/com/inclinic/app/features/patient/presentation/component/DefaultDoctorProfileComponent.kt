package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorDetailUseCase
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorReviewsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultDoctorProfileComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val getDoctorDetail: GetDoctorDetailUseCase,
    private val getDoctorReviews: GetDoctorReviewsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (DoctorProfileComponent.Output) -> Unit,
) : DoctorProfileComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(DoctorProfileState())
    override val state: Value<DoctorProfileState> = _state

    init { load() }

    override fun onLoadMoreReviews() {
        val s = _state.value
        if (s.isLoading || !s.hasMoreReviews) return
        val nextPage = s.reviewsPage + 1
        _state.update { it.copy(reviewsPage = nextPage) }
        loadReviews(nextPage)
    }

    override fun onBookTapped() { onOutput(DoctorProfileComponent.Output.NavigateToAvailability(doctorId)) }
    override fun onBack() { onOutput(DoctorProfileComponent.Output.Back) }
    override fun onErrorDismissed() { _state.update { it.copy(error = null) } }

    private fun load() {
        _state.update { it.copy(isLoading = true) }
        scope.launch {
            getDoctorDetail(doctorId)
                .onSuccess { doctor ->
                    _state.update { it.copy(isLoading = false, doctor = doctor) }
                    loadReviews(1)
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Failed to load doctor")) }
                }
        }
    }

    private fun loadReviews(page: Int) {
        scope.launch {
            getDoctorReviews(doctorId, page)
                .onSuccess { reviews ->
                    _state.update { it.copy(
                        reviews = if (page == 1) reviews else it.reviews + reviews,
                        hasMoreReviews = reviews.isNotEmpty(),
                    ) }
                }
                .onFailure { /* reviews failure is non-critical */ }
        }
    }
}
