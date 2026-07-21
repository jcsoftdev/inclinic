package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.MedicalRecordDraft
import kotlinx.datetime.DayOfWeek

// ── Stub implementations for DefaultDoctorFlowComponentTest ──────────────────
// Each stub only stores the output lambda and exposes a minimal state.

internal class StubDoctorDashboardComponent(
    private val onOutput: (DoctorDashboardComponent.Output) -> Unit,
) : DoctorDashboardComponent {
    override val state: Value<DoctorDashboardState> = MutableValue(DoctorDashboardState())
    override fun onRefresh() {}
    override fun onNavigateToSchedule() { onOutput(DoctorDashboardComponent.Output.NavigateToSchedule) }
    override fun onNavigateToPendingAppointments() { onOutput(DoctorDashboardComponent.Output.NavigateToPendingAppointments) }
    override fun onNavigateToNotifications() { onOutput(DoctorDashboardComponent.Output.NavigateToNotifications) }
    override fun onAppointmentTap(appointmentId: String) { onOutput(DoctorDashboardComponent.Output.NavigateToAppointmentDetail(appointmentId)) }
    override fun onCreateMedicalRecord() { onOutput(DoctorDashboardComponent.Output.NavigateToCreateMedicalRecord) }
    override fun onNavigateToPackages() { onOutput(DoctorDashboardComponent.Output.NavigateToPackages) }
    override fun onNavigateToPatients() { onOutput(DoctorDashboardComponent.Output.NavigateToPatients) }
    override fun onNavigateToIncome() { onOutput(DoctorDashboardComponent.Output.NavigateToIncome) }
}

internal class StubWeeklyScheduleComponent(
    private val onOutput: (WeeklyScheduleComponent.Output) -> Unit,
) : WeeklyScheduleComponent {
    override val state: Value<WeeklyScheduleState> = MutableValue(WeeklyScheduleState())
    override fun onPreviousWeek() {}
    override fun onNextWeek() {}
    override fun onDayTap(date: kotlinx.datetime.LocalDate) {
        onOutput(WeeklyScheduleComponent.Output.NavigateToDailySchedule(date))
    }
    override fun onBack() { onOutput(WeeklyScheduleComponent.Output.Back) }
}

internal class StubDailyScheduleComponent(
    private val onOutput: (DailyScheduleComponent.Output) -> Unit,
) : DailyScheduleComponent {
    override val state: Value<DailyScheduleState> = MutableValue(DailyScheduleState())
    override fun onPreviousDay() {}
    override fun onNextDay() {}
    override fun onAppointmentTap(appointmentId: String) {
        onOutput(DailyScheduleComponent.Output.NavigateToAppointmentDetail(appointmentId))
    }
    override fun onOpenRescheduleQueue() { onOutput(DailyScheduleComponent.Output.OpenRescheduleQueue) }
    override fun onBack() { onOutput(DailyScheduleComponent.Output.Back) }
}

internal class StubDoctorAppointmentDetailComponent(
    private val appointmentId: String,
    private val onOutput: (DoctorAppointmentDetailComponent.Output) -> Unit,
) : DoctorAppointmentDetailComponent {
    override val state: Value<DoctorAppointmentDetailState> = MutableValue(DoctorAppointmentDetailState())
    override fun onConfirm() {}
    override fun onEvidencePhotoPicked(file: com.inclinic.app.core.platform.PickedFile) {}
    override fun onRemoveEvidencePhoto(index: Int) {}
    override fun onComplete(checkIn: com.inclinic.app.core.platform.GpsFix?) {}
    override fun onSeriousNoShow(checkIn: com.inclinic.app.core.platform.GpsFix) {}
    override fun onNoShow() {}
    override fun onNoShowConfirmed() {}
    override fun onNoShowDismissed() {}
    override fun onNavigateToPatient() { onOutput(DoctorAppointmentDetailComponent.Output.NavigateToPatientDetail("pat-1")) }
    override fun onNavigateToChat() { onOutput(DoctorAppointmentDetailComponent.Output.NavigateToChat(appointmentId)) }
    override fun onRequestReschedule() { onOutput(DoctorAppointmentDetailComponent.Output.NavigateToRequestReschedule(appointmentId)) }
    override fun onCreateMedicalRecord() { onOutput(DoctorAppointmentDetailComponent.Output.NavigateToCreateMedicalRecord(appointmentId, "pat-1")) }
    override fun onNavigateToCreatePrescription() { onOutput(DoctorAppointmentDetailComponent.Output.NavigateToCreatePrescription(appointmentId)) }
    override fun onNavigateToEditPrescription() { onOutput(DoctorAppointmentDetailComponent.Output.NavigateToEditPrescription("presc-1")) }
    override fun onBack() { onOutput(DoctorAppointmentDetailComponent.Output.Back) }
}

internal class StubPatientDetailComponent(
    private val patientId: String,
    private val onOutput: (PatientDetailComponent.Output) -> Unit,
) : PatientDetailComponent {
    override val state: Value<PatientDetailState> = MutableValue(PatientDetailState())
    override fun onViewMedicalRecords() { onOutput(PatientDetailComponent.Output.NavigateToMedicalRecords(patientId)) }
    override fun onBack() { onOutput(PatientDetailComponent.Output.Back) }
}

internal class StubMedicalRecordsListComponent(
    private val patientId: String,
    private val onOutput: (MedicalRecordsListComponent.Output) -> Unit,
) : MedicalRecordsListComponent {
    override val state: Value<MedicalRecordsListState> = MutableValue(MedicalRecordsListState())
    override fun onCreateRecord() { onOutput(MedicalRecordsListComponent.Output.NavigateToCreateRecord(patientId)) }
    override fun onRecordTap(recordId: String) { onOutput(MedicalRecordsListComponent.Output.NavigateToEditRecord(recordId)) }
    override fun onBack() { onOutput(MedicalRecordsListComponent.Output.Back) }
}

internal class StubCreateMedicalRecordComponent(
    private val onOutput: (CreateMedicalRecordComponent.Output) -> Unit,
) : CreateMedicalRecordComponent {
    override val state: Value<CreateMedicalRecordState> = MutableValue(CreateMedicalRecordState())
    override fun onDiagnosisChange(value: String) {}
    override fun onSymptomsChange(value: String) {}
    override fun onTreatmentChange(value: String) {}
    override fun onPrescriptionChange(value: String) {}
    override fun onNotesChange(value: String) {}
    override fun onSubmit() {}
    override fun onBack() { onOutput(CreateMedicalRecordComponent.Output.Back) }
    override fun onRestoreDraft() {}
    override fun onDiscardDraft() {}
}

internal class StubEditMedicalRecordComponent(
    private val onOutput: (EditMedicalRecordComponent.Output) -> Unit,
) : EditMedicalRecordComponent {
    override val state: Value<EditMedicalRecordState> = MutableValue(
        EditMedicalRecordState(draft = MedicalRecordDraft(appointmentId = ""))
    )
    override fun onDiagnosisChange(value: String) {}
    override fun onSymptomsChange(value: String) {}
    override fun onTreatmentChange(value: String) {}
    override fun onPrescriptionChange(value: String) {}
    override fun onNotesChange(value: String) {}
    override fun onSubmit() {}
    override fun onBack() { onOutput(EditMedicalRecordComponent.Output.Back) }
}

internal class StubScheduleConfigComponent(
    private val onOutput: (ScheduleConfigComponent.Output) -> Unit,
) : ScheduleConfigComponent {
    override val state: Value<ScheduleConfigState> = MutableValue(ScheduleConfigState())
    override fun onToggleDay(day: DayOfWeek) {}
    override fun onExpandDay(day: DayOfWeek) {}
    override fun onStartTimeChange(day: DayOfWeek, value: String) {}
    override fun onEndTimeChange(day: DayOfWeek, value: String) {}
    override fun onMaxPatientsChange(day: DayOfWeek, value: String) {}
    override fun onSlotDurationChange(day: DayOfWeek, minutes: Int) {}
    override fun onPriceChange(day: DayOfWeek, value: String) {}
    override fun onToggleAllowNegotiation(day: DayOfWeek) {}
    override fun onSave() {}
    override fun onBack() { onOutput(ScheduleConfigComponent.Output.Back) }
}

internal class StubPriceConfigComponent(
    private val onOutput: (PriceConfigComponent.Output) -> Unit,
) : PriceConfigComponent {
    override val state: Value<PriceConfigState> = MutableValue(PriceConfigState())
    override fun onPriceChange(value: String) {}
    override fun onPresentialToggle() {}
    override fun onVirtualToggle() {}
    override fun onSave() {}
    override fun onBack() { onOutput(PriceConfigComponent.Output.Back) }
}

/**
 * Stub for [DoctorChatComponent]. Since DoctorChatComponent is a concrete class (not
 * an interface) we cannot implement it directly. The flow tests avoid triggering chat
 * navigation, so the factory throws if called — this makes missing stubs visible quickly.
 *
 * Tests that need chat navigation should use a real DoctorChatComponent wired via Koin.
 */
internal fun StubDoctorChatComponent(ctx: ComponentContext, appointmentId: String): DoctorChatComponent {
    val dispatchers = com.inclinic.app.features.auth.fakes.TestAppDispatchers()
    val noOpDataSource = object : com.inclinic.app.features.doctor.infrastructure.remote.DoctorChatDataSource {
        override suspend fun getMessages(appointmentId: String) =
            Result.success(emptyList<com.inclinic.app.core.model.ChatMessage>())
        override suspend fun sendMessage(appointmentId: String, text: String, attachments: List<String>) =
            Result.success(com.inclinic.app.core.model.ChatMessage(
                id = "msg-0", appointmentId = appointmentId, senderId = "doc",
                senderRole = com.inclinic.app.core.model.SenderRole.DOCTOR,
                text = text, sentAt = kotlin.time.Clock.System.now(), readAt = null,
            ))
    }
    val noOpUpload = com.inclinic.app.core.upload.UploadFileUseCase(
        dataSource = com.inclinic.app.core.upload.FakeUploadDataSource(),
        dispatchers = dispatchers,
    )
    val noOpGetMessages = com.inclinic.app.features.doctor.chat.application.GetDoctorChatMessagesUseCase(
        dataSource = noOpDataSource,
        dispatchers = dispatchers,
    )
    val noOpSendMessage = com.inclinic.app.features.doctor.chat.application.SendDoctorChatMessageUseCase(
        dataSource = noOpDataSource,
        dispatchers = dispatchers,
    )
    return DoctorChatComponent(ctx, appointmentId, noOpGetMessages, noOpSendMessage, noOpUpload, dispatchers)
}
