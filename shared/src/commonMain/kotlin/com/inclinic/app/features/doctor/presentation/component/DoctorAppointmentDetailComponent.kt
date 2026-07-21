package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.platform.GpsFix
import com.inclinic.app.core.platform.PickedFile

interface DoctorAppointmentDetailComponent {
    val state: Value<DoctorAppointmentDetailState>

    fun onConfirm()

    /** Sube una foto de evidencia al bucket `visit-proofs`; su URL se acumula en el estado. */
    fun onEvidencePhotoPicked(file: PickedFile)
    fun onRemoveEvidencePhoto(index: Int)

    /**
     * Completa la cita con las fotos de evidencia ya subidas (URLs en el estado).
     * En visitas a domicilio, [checkIn] (GPS del médico) es obligatorio como evidencia;
     * la pantalla lo captura antes de invocar.
     */
    fun onComplete(checkIn: GpsFix? = null)

    /**
     * Falta grave: el médico fue a la visita a domicilio y el paciente no estaba.
     * Reusa las fotos de evidencia ya subidas + el check-in GPS que captura la pantalla.
     */
    fun onSeriousNoShow(checkIn: GpsFix)
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
    /** URLs de las fotos de evidencia ya subidas a `visit-proofs`. */
    val evidencePhotoUrls: List<String> = emptyList(),
    /** True mientras una foto se está subiendo (para deshabilitar el botón). */
    val isUploadingPhoto: Boolean = false,
)
