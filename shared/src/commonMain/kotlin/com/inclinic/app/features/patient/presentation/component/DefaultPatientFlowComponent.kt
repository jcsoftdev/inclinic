@file:OptIn(DelicateDecomposeApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.navigation.PatientConfig
import com.inclinic.app.features.patient.assistant.presentation.component.AssistantChatComponent
import com.inclinic.app.features.patient.moderation.presentation.component.BlockUserComponent
import com.inclinic.app.features.patient.moderation.presentation.component.ReportUserComponent
import com.inclinic.app.ui.atoms.PatientTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class DefaultPatientFlowComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val dispatchers: AppDispatchers,
    private val homeFactory: (ComponentContext, String, (PatientHomeComponent.Output) -> Unit) -> PatientHomeComponent,
    private val searchFactory: (ComponentContext, (DoctorSearchComponent.Output) -> Unit) -> DoctorSearchComponent,
    private val doctorProfileFactory: (ComponentContext, String, (DoctorProfileComponent.Output) -> Unit) -> DoctorProfileComponent,
    private val consultTypeFactory: (ComponentContext, String, (ConsultTypeComponent.Output) -> Unit) -> ConsultTypeComponent,
    private val availabilityFactory: (ComponentContext, String, String, (AvailabilityCalendarComponent.Output) -> Unit) -> AvailabilityCalendarComponent,
    private val bookingFactory: (ComponentContext, String, String, String, String, (BookingComponent.Output) -> Unit) -> BookingComponent,
    private val paymentFactory: (ComponentContext, String?, String?, (PaymentComponent.Output) -> Unit) -> PaymentComponent,
    private val appointmentsFactory: (ComponentContext, String, (PatientAppointmentsListComponent.Output) -> Unit) -> PatientAppointmentsListComponent,
    private val appointmentDetailFactory: (ComponentContext, String, (AppointmentDetailComponent.Output) -> Unit) -> AppointmentDetailComponent,
    private val cancelAppointmentFactory: (ComponentContext, String, (CancelAppointmentComponent.Output) -> Unit) -> CancelAppointmentComponent,
    private val chatFactory: (ComponentContext, doctorId: String, doctorName: String, onOutput: (ChatComponent.Output) -> Unit) -> ChatComponent,
    private val rescheduleResponseFactory: (ComponentContext, String, (RescheduleResponseComponent.Output) -> Unit) -> RescheduleResponseComponent,
    private val medicalHistoryFactory: (ComponentContext, String, (MedicalHistoryComponent.Output) -> Unit) -> MedicalHistoryComponent,
    private val profileOverviewFactory: (ComponentContext, String, (ProfileOverviewComponent.Output) -> Unit) -> ProfileOverviewComponent,
    private val membershipFactory: (ComponentContext, (MembershipComponent.Output) -> Unit) -> MembershipComponent,
    private val profileFactory: (ComponentContext, String, (PatientProfileComponent.Output) -> Unit) -> PatientProfileComponent,
    private val assistantChatComponentFactory: (ComponentContext) -> AssistantChatComponent,
    private val rescheduleAppointmentFactory: (ComponentContext, String, (RescheduleAppointmentComponent.Output) -> Unit) -> RescheduleAppointmentComponent,
    private val changeVisitTypeFactory: (ComponentContext, String, (ChangeVisitTypeComponent.Output) -> Unit) -> ChangeVisitTypeComponent,
    private val disputeAppointmentFactory: (ComponentContext, String, (DisputeAppointmentComponent.Output) -> Unit) -> DisputeAppointmentComponent,
    private val confirmRatingFactory: (ComponentContext, String, (ConfirmRatingComponent.Output) -> Unit) -> ConfirmRatingComponent,
    private val messagesListFactory: (ComponentContext, (MessagesListComponent.Output) -> Unit) -> MessagesListComponent,
    private val notificationsFactory: (ComponentContext, (NotificationsComponent.Output) -> Unit) -> NotificationsComponent,
    private val settingsFactory: (ComponentContext, String, (SettingsComponent.Output) -> Unit) -> SettingsComponent,
    private val medicalRecordDetailFactory: (ComponentContext, String, (MedicalRecordDetailComponent.Output) -> Unit) -> MedicalRecordDetailComponent,
    private val prescriptionDetailFactory: (ComponentContext, String, (PrescriptionDetailComponent.Output) -> Unit) -> PrescriptionDetailComponent,
    private val historyAccessLogsFactory: (ComponentContext, (HistoryAccessLogsComponent.Output) -> Unit) -> HistoryAccessLogsComponent,
    private val shareRequestsFactory: (ComponentContext, (ShareRequestsComponent.Output) -> Unit) -> ShareRequestsComponent,
    private val approveShareRequestFactory: (ComponentContext, String, (ApproveShareRequestComponent.Output) -> Unit) -> ApproveShareRequestComponent,
    private val symptomInputFactory: (ComponentContext, (SymptomInputComponent.Output) -> Unit) -> SymptomInputComponent,
    private val symptomResultsFactory: (ComponentContext, String, (SymptomResultsComponent.Output) -> Unit) -> SymptomResultsComponent,
    private val therapyPackagesFactory: (ComponentContext, String, (TherapyPackagesListComponent.Output) -> Unit) -> TherapyPackagesListComponent,
    private val therapyPackageDetailFactory: (ComponentContext, String, (TherapyPackageDetailComponent.Output) -> Unit) -> TherapyPackageDetailComponent,
    private val therapyOffersFactory: (ComponentContext, (TherapyOffersComponent.Output) -> Unit) -> TherapyOffersComponent,
    private val negotiationFactory: (ComponentContext, String?, String?, (NegotiationComponent.Output) -> Unit) -> NegotiationComponent,
    private val reportUserFactory: (ComponentContext, userId: String, userName: String, (ReportUserComponent.Output) -> Unit) -> ReportUserComponent,
    private val blockUserFactory: (ComponentContext, userId: String, userName: String, (BlockUserComponent.Output) -> Unit) -> BlockUserComponent,
    private val clinicalProfileFactory: (ComponentContext, (ClinicalProfileComponent.Output) -> Unit) -> ClinicalProfileComponent,
    private val deleteAccountFactory: (ComponentContext, (DeleteAccountComponent.Output) -> Unit) -> DeleteAccountComponent,
    private val activeAccessesFactory: (ComponentContext, (ActiveAccessesComponent.Output) -> Unit) -> ActiveAccessesComponent,
    private val onOutput: (PatientFlowComponent.Output) -> Unit,
) : PatientFlowComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<PatientConfig>()
    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _stack = childStack(
        source = navigation,
        serializer = PatientConfig.serializer(),
        initialConfiguration = PatientConfig.Home,
        handleBackButton = true,
        childFactory = ::createChild,
    )
    override val stack: Value<ChildStack<*, PatientFlowComponent.Child>> = _stack

    override fun navigateTo(config: PatientConfig) {
        navigation.push(config)
    }

    override fun onNavTabSelected(tab: PatientTab) {
        val config: PatientConfig = when (tab) {
            PatientTab.Home -> PatientConfig.Home
            PatientTab.Search -> PatientConfig.Search
            PatientTab.Appointments -> PatientConfig.Appointments
            PatientTab.Messages -> PatientConfig.AssistantChat
            PatientTab.Profile -> PatientConfig.Profile
        }
        navigation.navigate { _ ->
            if (config == PatientConfig.Home) listOf(PatientConfig.Home)
            else listOf(PatientConfig.Home, config)
        }
    }

    private fun createChild(config: PatientConfig, ctx: ComponentContext): PatientFlowComponent.Child =
        when (config) {
            is PatientConfig.Home -> PatientFlowComponent.Child.Home(
                homeFactory(ctx, patientId) { output ->
                    when (output) {
                        PatientHomeComponent.Output.NavigateToSearch -> navigation.bringToFront(PatientConfig.Search)
                        is PatientHomeComponent.Output.NavigateToDoctorProfile ->
                            navigation.push(PatientConfig.DoctorProfile(output.doctorId))
                        PatientHomeComponent.Output.NavigateToAssistantChat ->
                            navigation.bringToFront(PatientConfig.AssistantChat)
                        PatientHomeComponent.Output.NavigateToAppointments ->
                            navigation.bringToFront(PatientConfig.Appointments)
                        is PatientHomeComponent.Output.NavigateToAppointmentDetail ->
                            navigation.push(PatientConfig.AppointmentDetail(output.appointmentId))
                        PatientHomeComponent.Output.NavigateToProfile ->
                            navigation.bringToFront(PatientConfig.Profile)
                        PatientHomeComponent.Output.NavigateToPackages ->
                            navigation.push(PatientConfig.TherapyPackages)
                        PatientHomeComponent.Output.NavigateToPremium ->
                            navigation.push(PatientConfig.Membership)
                    }
                }
            )
            is PatientConfig.Search -> PatientFlowComponent.Child.Search(
                searchFactory(ctx) { output ->
                    when (output) {
                        is DoctorSearchComponent.Output.NavigateToDoctorProfile ->
                            navigation.push(PatientConfig.DoctorProfile(output.doctorId))
                        DoctorSearchComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.DoctorProfile -> PatientFlowComponent.Child.DoctorProfile(
                doctorProfileFactory(ctx, config.doctorId) { output ->
                    when (output) {
                        is DoctorProfileComponent.Output.NavigateToAvailability ->
                            navigation.push(PatientConfig.ConsultType(config.doctorId))
                        DoctorProfileComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.ConsultType -> PatientFlowComponent.Child.ConsultType(
                consultTypeFactory(ctx, config.doctorId) { output ->
                    when (output) {
                        is ConsultTypeComponent.Output.NavigateToAvailability ->
                            navigation.push(PatientConfig.Availability(output.doctorId, output.consultType))
                        ConsultTypeComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.Availability -> PatientFlowComponent.Child.Availability(
                availabilityFactory(ctx, config.doctorId, config.consultType) { output ->
                    when (output) {
                        is AvailabilityCalendarComponent.Output.NavigateToBooking ->
                            navigation.push(PatientConfig.Booking(output.doctorId, output.slotId, output.date, config.consultType))
                        AvailabilityCalendarComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.Booking -> PatientFlowComponent.Child.Booking(
                bookingFactory(ctx, config.doctorId, config.slotId, config.date, config.consultType) { output ->
                    when (output) {
                        is BookingComponent.Output.NavigateToPayment ->
                            navigation.push(PatientConfig.Payment(output.appointmentId))
                        BookingComponent.Output.NavigateToAppointments ->
                            navigation.bringToFront(PatientConfig.Appointments)
                        BookingComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.Payment -> PatientFlowComponent.Child.Payment(
                paymentFactory(ctx, config.appointmentId, config.therapyPackageId) { output ->
                    when (output) {
                        is PaymentComponent.Output.NavigateToSuccess ->
                            navigation.navigate { listOf(PatientConfig.Home, PatientConfig.AppointmentDetail(output.appointmentId)) }
                        PaymentComponent.Output.Back -> navigation.pop()
                        PaymentComponent.Output.NavigateToAppointments ->
                            navigation.navigate { listOf(PatientConfig.Home, PatientConfig.Appointments) }
                        PaymentComponent.Output.NavigateToPackages ->
                            navigation.push(PatientConfig.TherapyPackages)
                    }
                }
            )
            is PatientConfig.Appointments -> PatientFlowComponent.Child.Appointments(
                appointmentsFactory(ctx, patientId) { output ->
                    when (output) {
                        is PatientAppointmentsListComponent.Output.NavigateToAppointmentDetail ->
                            navigation.push(PatientConfig.AppointmentDetail(output.appointmentId))
                        is PatientAppointmentsListComponent.Output.NavigateToPayment ->
                            navigation.push(PatientConfig.Payment(output.appointmentId))
                        is PatientAppointmentsListComponent.Output.NavigateToCancel ->
                            navigation.push(PatientConfig.CancelAppointment(output.appointmentId))
                        is PatientAppointmentsListComponent.Output.NavigateToReschedule ->
                            navigation.push(PatientConfig.Availability(output.doctorId, output.consultType))
                        is PatientAppointmentsListComponent.Output.NavigateToRescheduleResponse ->
                            navigation.push(PatientConfig.RescheduleResponse(output.appointmentId))
                        PatientAppointmentsListComponent.Output.NavigateToSearch ->
                            navigation.bringToFront(PatientConfig.Search)
                    }
                }
            )
            is PatientConfig.AppointmentDetail -> PatientFlowComponent.Child.AppointmentDetail(
                appointmentDetailFactory(ctx, config.appointmentId) { output ->
                    when (output) {
                        is AppointmentDetailComponent.Output.NavigateToPayment ->
                            navigation.push(PatientConfig.Payment(output.appointmentId))
                        is AppointmentDetailComponent.Output.NavigateToCancel ->
                            navigation.push(PatientConfig.CancelAppointment(output.appointmentId))
                        is AppointmentDetailComponent.Output.NavigateToReschedule ->
                            navigation.push(PatientConfig.Availability(output.doctorId, output.consultType))
                        is AppointmentDetailComponent.Output.NavigateToChat ->
                            navigation.push(PatientConfig.Chat(output.doctorId, output.doctorName))
                        is AppointmentDetailComponent.Output.NavigateToRescheduleResponse ->
                            navigation.push(PatientConfig.RescheduleResponse(output.appointmentId))
                        AppointmentDetailComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.CancelAppointment -> PatientFlowComponent.Child.CancelAppointment(
                cancelAppointmentFactory(ctx, config.appointmentId) { output ->
                    when (output) {
                        CancelAppointmentComponent.Output.Back -> navigation.pop()
                        CancelAppointmentComponent.Output.Cancelled -> {
                            navigation.navigate { stack ->
                                stack.filter { it !is PatientConfig.CancelAppointment && it !is PatientConfig.AppointmentDetail }
                            }
                        }
                    }
                }
            )
            is PatientConfig.Chat -> PatientFlowComponent.Child.Chat(
                chatFactory(ctx, config.doctorId, config.doctorName) { output ->
                    when (output) {
                        ChatComponent.Output.Back -> navigation.pop()
                        is ChatComponent.Output.NavigateToReport ->
                            navigation.push(PatientConfig.ReportUser(output.userId, output.userName))
                        is ChatComponent.Output.NavigateToBlock ->
                            navigation.push(PatientConfig.BlockUser(output.userId, output.userName))
                    }
                }
            )
            is PatientConfig.RescheduleResponse -> PatientFlowComponent.Child.RescheduleResponse(
                rescheduleResponseFactory(ctx, config.appointmentId) { output ->
                    when (output) {
                        RescheduleResponseComponent.Output.Back -> navigation.pop()
                        RescheduleResponseComponent.Output.Responded -> navigation.pop()
                    }
                }
            )
            is PatientConfig.MedicalHistory -> PatientFlowComponent.Child.MedicalHistory(
                medicalHistoryFactory(ctx, patientId) { output ->
                    when (output) {
                        MedicalHistoryComponent.Output.Back -> navigation.pop()
                        MedicalHistoryComponent.Output.NavigateToClinicalProfile ->
                            navigation.push(PatientConfig.ClinicalProfile)
                    }
                }
            )
            is PatientConfig.Profile -> PatientFlowComponent.Child.Profile(
                profileOverviewFactory(ctx, patientId) { output ->
                    when (output) {
                        ProfileOverviewComponent.Output.NavigateToEditProfile ->
                            navigation.push(PatientConfig.EditProfile)
                        ProfileOverviewComponent.Output.NavigateToSettings ->
                            navigation.push(PatientConfig.Settings)
                    }
                }
            )
            is PatientConfig.EditProfile -> PatientFlowComponent.Child.EditProfile(
                profileFactory(ctx, patientId) { output ->
                    when (output) {
                        PatientProfileComponent.Output.Back -> navigation.pop()
                        PatientProfileComponent.Output.Saved -> navigation.pop()
                    }
                }
            )
            is PatientConfig.AssistantChat -> PatientFlowComponent.Child.AssistantChat(
                assistantChatComponentFactory(ctx)
            )

            // Appointment lifecycle
            is PatientConfig.RescheduleAppointment -> PatientFlowComponent.Child.RescheduleAppointment(
                rescheduleAppointmentFactory(ctx, config.appointmentId) { output ->
                    when (output) {
                        RescheduleAppointmentComponent.Output.Back -> navigation.pop()
                        RescheduleAppointmentComponent.Output.Rescheduled -> {
                            navigation.navigate { stack ->
                                stack.filter { it !is PatientConfig.RescheduleAppointment && it !is PatientConfig.AppointmentDetail }
                            }
                        }
                    }
                }
            )
            is PatientConfig.ChangeVisitType -> PatientFlowComponent.Child.ChangeVisitType(
                changeVisitTypeFactory(ctx, config.appointmentId) { output ->
                    when (output) {
                        ChangeVisitTypeComponent.Output.Back -> navigation.pop()
                        ChangeVisitTypeComponent.Output.Requested -> navigation.pop()
                    }
                }
            )
            is PatientConfig.DisputeAppointment -> PatientFlowComponent.Child.DisputeAppointment(
                disputeAppointmentFactory(ctx, config.appointmentId) { output ->
                    when (output) {
                        DisputeAppointmentComponent.Output.Back -> navigation.pop()
                        DisputeAppointmentComponent.Output.Disputed -> navigation.pop()
                    }
                }
            )
            is PatientConfig.ConfirmRating -> PatientFlowComponent.Child.ConfirmRating(
                confirmRatingFactory(ctx, config.appointmentId) { output ->
                    when (output) {
                        ConfirmRatingComponent.Output.Back -> navigation.pop()
                        ConfirmRatingComponent.Output.Confirmed -> navigation.pop()
                        is ConfirmRatingComponent.Output.NavigateToDispute ->
                            navigation.push(PatientConfig.DisputeAppointment(output.appointmentId))
                    }
                }
            )

            // Messages & Notifications
            is PatientConfig.MessagesList -> PatientFlowComponent.Child.MessagesList(
                messagesListFactory(ctx) { output ->
                    when (output) {
                        is MessagesListComponent.Output.NavigateToChat ->
                            navigation.push(PatientConfig.Chat(output.doctorId, output.doctorName))
                    }
                }
            )
            is PatientConfig.Notifications -> PatientFlowComponent.Child.Notifications(
                notificationsFactory(ctx) { output ->
                    when (output) {
                        NotificationsComponent.Output.Back -> navigation.pop()
                        is NotificationsComponent.Output.NavigateToAppointment ->
                            navigation.push(PatientConfig.AppointmentDetail(output.appointmentId))
                        is NotificationsComponent.Output.NavigateToPayment ->
                            navigation.push(PatientConfig.Payment(output.appointmentId))
                    }
                }
            )
            is PatientConfig.Settings -> PatientFlowComponent.Child.Settings(
                settingsFactory(ctx, patientId) { output ->
                    when (output) {
                        SettingsComponent.Output.Back -> navigation.pop()
                        SettingsComponent.Output.NavigateToChangePassword -> navigation.pop()
                        SettingsComponent.Output.NavigateToDeleteAccount ->
                            navigation.push(PatientConfig.DeleteAccount)
                    }
                }
            )

            // Profile & account management
            is PatientConfig.Membership -> PatientFlowComponent.Child.Membership(
                membershipFactory(ctx) { output ->
                    when (output) {
                        MembershipComponent.Output.NavigateBack -> navigation.pop()
                    }
                }
            )
            is PatientConfig.ClinicalProfile -> PatientFlowComponent.Child.ClinicalProfile(
                clinicalProfileFactory(ctx) { output ->
                    when (output) {
                        ClinicalProfileComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.DeleteAccount -> PatientFlowComponent.Child.DeleteAccount(
                deleteAccountFactory(ctx) { output ->
                    when (output) {
                        DeleteAccountComponent.Output.Back -> navigation.pop()
                        // Session cleaned via LogoutUseCase → SessionEvents; RootComponent handles nav.
                        DeleteAccountComponent.Output.Deleted -> Unit
                    }
                }
            )

            // Medical history deep screens
            is PatientConfig.MedicalRecordDetail -> PatientFlowComponent.Child.MedicalRecordDetail(
                medicalRecordDetailFactory(ctx, config.recordId) { output ->
                    when (output) {
                        MedicalRecordDetailComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.PrescriptionDetail -> PatientFlowComponent.Child.PrescriptionDetail(
                prescriptionDetailFactory(ctx, config.prescriptionId) { output ->
                    when (output) {
                        PrescriptionDetailComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.HistoryAccessLogs -> PatientFlowComponent.Child.HistoryAccessLogs(
                historyAccessLogsFactory(ctx) { output ->
                    when (output) {
                        HistoryAccessLogsComponent.Output.Back -> navigation.pop()
                        HistoryAccessLogsComponent.Output.NavigateToManageAccess ->
                            navigation.push(PatientConfig.ActiveAccesses)
                    }
                }
            )
            is PatientConfig.ShareRequests -> PatientFlowComponent.Child.ShareRequests(
                shareRequestsFactory(ctx) { output ->
                    when (output) {
                        ShareRequestsComponent.Output.Back -> navigation.pop()
                        is ShareRequestsComponent.Output.NavigateToDetail ->
                            navigation.push(PatientConfig.ApproveShareRequest(output.requestId))
                    }
                }
            )
            is PatientConfig.ApproveShareRequest -> PatientFlowComponent.Child.ApproveShareRequest(
                approveShareRequestFactory(ctx, config.requestId) { output ->
                    when (output) {
                        ApproveShareRequestComponent.Output.Closed -> navigation.pop()
                        ApproveShareRequestComponent.Output.Approved -> navigation.pop()
                        ApproveShareRequestComponent.Output.Rejected -> navigation.pop()
                    }
                }
            )

            // Symptom analysis
            is PatientConfig.SymptomInput -> PatientFlowComponent.Child.SymptomInput(
                symptomInputFactory(ctx) { output ->
                    when (output) {
                        is SymptomInputComponent.Output.NavigateToResults ->
                            navigation.push(PatientConfig.SymptomResults(output.symptoms))
                        SymptomInputComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.SymptomResults -> PatientFlowComponent.Child.SymptomResults(
                symptomResultsFactory(ctx, config.symptoms) { output ->
                    when (output) {
                        SymptomResultsComponent.Output.Back -> navigation.pop()
                        is SymptomResultsComponent.Output.NavigateToDoctorProfile ->
                            navigation.push(PatientConfig.DoctorProfile(output.doctorId))
                        SymptomResultsComponent.Output.EditSymptoms -> navigation.pop()
                    }
                }
            )

            // Therapy packages
            is PatientConfig.TherapyPackages -> PatientFlowComponent.Child.TherapyPackages(
                therapyPackagesFactory(ctx, patientId) { output ->
                    when (output) {
                        is TherapyPackagesListComponent.Output.NavigateToPackageDetail ->
                            navigation.push(PatientConfig.TherapyPackageDetail(output.packageId))
                        TherapyPackagesListComponent.Output.NavigateToOffers ->
                            navigation.push(PatientConfig.TherapyOffers)
                        TherapyPackagesListComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.TherapyPackageDetail -> PatientFlowComponent.Child.TherapyPackageDetail(
                therapyPackageDetailFactory(ctx, config.packageId) { output ->
                    when (output) {
                        is TherapyPackageDetailComponent.Output.NavigateToScheduleSession ->
                            navigation.push(PatientConfig.Availability(output.doctorId, "office"))
                        TherapyPackageDetailComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.TherapyOffers -> PatientFlowComponent.Child.TherapyOffers(
                therapyOffersFactory(ctx) { output ->
                    when (output) {
                        is TherapyOffersComponent.Output.NavigateToNegotiation ->
                            navigation.push(PatientConfig.Negotiation(negotiationId = output.negotiationId))
                        is TherapyOffersComponent.Output.StartNegotiation ->
                            navigation.push(PatientConfig.Negotiation(offerId = output.offerId))
                        is TherapyOffersComponent.Output.NavigateToPayment ->
                            navigation.push(PatientConfig.Payment(therapyPackageId = output.therapyPackageId))
                        TherapyOffersComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.Negotiation -> PatientFlowComponent.Child.Negotiation(
                negotiationFactory(ctx, config.negotiationId, config.offerId) { output ->
                    when (output) {
                        is NegotiationComponent.Output.NavigateToPayment ->
                            navigation.push(PatientConfig.Payment(therapyPackageId = output.therapyPackageId))
                        NegotiationComponent.Output.Back -> navigation.pop()
                        is NegotiationComponent.Output.NavigateToReport ->
                            navigation.push(PatientConfig.ReportUser(output.userId, output.userName))
                        is NegotiationComponent.Output.NavigateToBlock ->
                            navigation.push(PatientConfig.BlockUser(output.userId, output.userName))
                    }
                }
            )

            // Sharing management
            is PatientConfig.ActiveAccesses -> PatientFlowComponent.Child.ActiveAccesses(
                activeAccessesFactory(ctx) { output ->
                    when (output) {
                        ActiveAccessesComponent.Output.Back -> navigation.pop()
                    }
                }
            )

            // Moderation
            is PatientConfig.ReportUser -> PatientFlowComponent.Child.ReportUser(
                reportUserFactory(ctx, config.userId, config.userName) { output ->
                    when (output) {
                        ReportUserComponent.Output.Submitted -> navigation.pop()
                        ReportUserComponent.Output.Back -> navigation.pop()
                    }
                }
            )
            is PatientConfig.BlockUser -> PatientFlowComponent.Child.BlockUser(
                blockUserFactory(ctx, config.userId, config.userName) { output ->
                    when (output) {
                        BlockUserComponent.Output.Blocked -> navigation.pop()
                        BlockUserComponent.Output.Back -> navigation.pop()
                    }
                }
            )
        }
}
