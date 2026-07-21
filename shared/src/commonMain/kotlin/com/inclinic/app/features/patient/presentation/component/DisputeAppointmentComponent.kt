package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.DisputeReason
import com.inclinic.app.core.platform.PickedFile

interface DisputeAppointmentComponent {
    val state: Value<DisputeAppointmentState>

    fun onReasonSelected(reason: DisputeReason)
    fun onDetailsChanged(details: String)

    /** Sube una foto de evidencia al bucket de disputas; su URL se acumula en el estado. */
    fun onEvidencePicked(file: PickedFile)
    fun onRemoveEvidence(index: Int)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object Disputed : Output
    }
}

data class DisputeAppointmentState(
    val appointment: Appointment? = null,
    val selectedReason: DisputeReason? = null,
    val details: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    /** URLs de las fotos de evidencia ya subidas. */
    val evidenceUrls: List<String> = emptyList(),
    val isUploadingEvidence: Boolean = false,
)
