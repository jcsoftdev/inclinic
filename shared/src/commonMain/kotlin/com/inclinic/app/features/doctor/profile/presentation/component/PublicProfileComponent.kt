package com.inclinic.app.features.doctor.profile.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.profile.core.model.DoctorProfile

interface PublicProfileComponent {
    val state: Value<PublicProfileState>

    fun onBack()
}

data class PublicProfileState(
    val isLoading: Boolean = false,
    val profile: DoctorProfile? = null,
    val error: String? = null,
)
