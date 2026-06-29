package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.Review

interface DoctorProfileComponent {
    val state: Value<DoctorProfileState>

    fun onLoadMoreReviews()
    fun onBookTapped()
    fun onBack()
    fun onErrorDismissed()

    sealed interface Output {
        data class NavigateToAvailability(val doctorId: String) : Output
        data object Back : Output
    }
}

data class DoctorProfileState(
    val doctor: Doctor? = null,
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val reviewsPage: Int = 1,
    val hasMoreReviews: Boolean = true,
    val error: String? = null,
)
