package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment

interface DoctorAppointmentDetailComponent {
    val state: Value<DoctorAppointmentDetailState>

    fun onConfirm()
    fun onComplete(selectedPhotos: List<ByteArray>)
    fun onNoShow()
    fun onNoShowConfirmed()
    fun onNoShowDismissed()
    fun onNavigateToPatient()
    fun onNavigateToChat()
    fun onRequestReschedule()
    fun onCreateMedicalRecord()
    fun onNavigateToCreatePrescription()
    fun onNavigateToEditPrescription()
    fun onBack()

    sealed interface Output {
        data class NavigateToPatientDetail(val patientId: String) : Output
        data class NavigateToChat(val appointmentId: String) : Output
        data class NavigateToRequestReschedule(val appointmentId: String) : Output
        data class NavigateToCreateMedicalRecord(val appointmentId: String, val patientId: String) : Output
        data class NavigateToCreatePrescription(val appointmentId: String) : Output
        data class NavigateToEditPrescription(val prescriptionId: String) : Output
        data object Back : Output
    }
}

data class DoctorAppointmentDetailState(
    val appointment: Appointment? = null,
    val isLoading: Boolean = false,
    val actionInProgress: Boolean = false,
    val showNoShowDialog: Boolean = false,
    val error: String? = null,
)
