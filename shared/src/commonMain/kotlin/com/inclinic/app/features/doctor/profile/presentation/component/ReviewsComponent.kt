package com.inclinic.app.features.doctor.profile.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.profile.core.model.DoctorReviewsPage

interface ReviewsComponent {
    val state: Value<ReviewsState>

    fun onRetry()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class ReviewsState(
    val isLoading: Boolean = false,
    val page: DoctorReviewsPage? = null,
    val error: String? = null,
)
