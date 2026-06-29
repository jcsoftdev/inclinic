package com.inclinic.app.features.doctor.profile.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.application.GetDoctorReviewsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultReviewsComponent(
    componentContext: ComponentContext,
    private val getReviews: GetDoctorReviewsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ReviewsComponent.Output) -> Unit,
) : ReviewsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    private val _state = MutableValue(ReviewsState())
    override val state: Value<ReviewsState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        load()
    }

    override fun onRetry() { load() }

    override fun onBack() = onOutput(ReviewsComponent.Output.Back)

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getReviews()
                .onSuccess { page ->
                    _state.update { it.copy(isLoading = false, page = page) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading reviews")) }
                }
        }
    }
}
