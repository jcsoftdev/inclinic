package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminReviewItem
import com.inclinic.app.features.admin.reviews.application.GetReviewsUseCase
import com.inclinic.app.features.admin.reviews.application.HideReviewUseCase
import com.inclinic.app.features.admin.reviews.application.UnhideReviewUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminReviewsComponent(
    componentContext: ComponentContext,
    private val getReviews: GetReviewsUseCase,
    private val hideReview: HideReviewUseCase,
    private val unhideReview: UnhideReviewUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminReviewsComponent.Output) -> Unit,
) : AdminReviewsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminReviewsState())
    override val state: Value<AdminReviewsState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onFilterChange(filter: AdminReviewsFilter) {
        _state.update { it.copy(activeFilter = filter, error = null) }
        load(filter)
    }

    override fun onShowHideDialog(item: AdminReviewItem) {
        _state.update { it.copy(pendingHideItem = item, actionError = null) }
    }

    override fun onDismissHideDialog() {
        _state.update { it.copy(pendingHideItem = null, actionError = null) }
    }

    override fun onHide(item: AdminReviewItem, reason: String) {
        if (reason.length < 10) {
            _state.update { it.copy(actionError = "La razón debe tener al menos 10 caracteres") }
            return
        }
        _state.update { it.copy(isActing = true, actionError = null) }
        scope.launch {
            hideReview(item.appointmentId, reason)
                .onSuccess {
                    _state.update { st ->
                        st.copy(
                            isActing = false,
                            pendingHideItem = null,
                            items = st.items.map { r ->
                                if (r.appointmentId == item.appointmentId)
                                    r.copy(reviewHiddenAt = "now", reviewHiddenReason = reason)
                                else r
                            },
                        )
                    }
                    // Refresh to get authoritative state
                    load()
                }
                .onFailure { err ->
                    _state.update { it.copy(isActing = false, actionError = err.toUserMessage("Error ocultando reseña")) }
                }
        }
    }

    override fun onUnhide(item: AdminReviewItem) {
        _state.update { it.copy(isActing = true, actionError = null) }
        scope.launch {
            unhideReview(item.appointmentId)
                .onSuccess {
                    _state.update { st ->
                        st.copy(
                            isActing = false,
                            items = st.items.map { r ->
                                if (r.appointmentId == item.appointmentId)
                                    r.copy(reviewHiddenAt = null, reviewHiddenReason = null)
                                else r
                            },
                        )
                    }
                    // Refresh to get authoritative state
                    load()
                }
                .onFailure { err ->
                    _state.update { it.copy(isActing = false, actionError = err.toUserMessage("Error mostrando reseña")) }
                }
        }
    }

    override fun onBack() {
        onOutput(AdminReviewsComponent.Output.Back)
    }

    private fun load(filter: AdminReviewsFilter = _state.value.activeFilter) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getReviews(
                withComment = filter.withComment,
                hidden = filter.hidden,
            )
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, items = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando reseñas")) }
                }
        }
    }
}
