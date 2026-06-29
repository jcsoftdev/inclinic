package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.navigation.PatientConfig
import com.inclinic.app.features.patient.assistant.presentation.component.AssistantChatComponent
import com.inclinic.app.features.patient.moderation.presentation.component.BlockUserComponent
import com.inclinic.app.features.patient.moderation.presentation.component.ReportUserComponent
import com.inclinic.app.ui.atoms.PatientTab

interface PatientFlowComponent {
    val stack: Value<ChildStack<*, Child>>

    fun navigateTo(config: PatientConfig)
    fun onNavTabSelected(tab: PatientTab)

    sealed interface Child {
        class Home(val component: PatientHomeComponent) : Child
        class Search(val component: DoctorSearchComponent) : Child
        class DoctorProfile(val component: DoctorProfileComponent) : Child
        class ConsultType(val component: ConsultTypeComponent) : Child
        class Availability(val component: AvailabilityCalendarComponent) : Child
        class Booking(val component: BookingComponent) : Child
        class Payment(val component: PaymentComponent) : Child
        class Appointments(val component: PatientAppointmentsListComponent) : Child
        class AppointmentDetail(val component: AppointmentDetailComponent) : Child
        class CancelAppointment(val component: CancelAppointmentComponent) : Child
        class Chat(val component: ChatComponent) : Child
        class MedicalHistory(val component: MedicalHistoryComponent) : Child
        class Profile(val component: ProfileOverviewComponent) : Child
        class EditProfile(val component: PatientProfileComponent) : Child
        class RescheduleResponse(val component: RescheduleResponseComponent) : Child
        class AssistantChat(val component: AssistantChatComponent) : Child

        // Appointment lifecycle
        class RescheduleAppointment(val component: RescheduleAppointmentComponent) : Child
        class ChangeVisitType(val component: ChangeVisitTypeComponent) : Child
        class DisputeAppointment(val component: DisputeAppointmentComponent) : Child
        class ConfirmRating(val component: ConfirmRatingComponent) : Child

        // Messages & Notifications
        class MessagesList(val component: MessagesListComponent) : Child
        class Notifications(val component: NotificationsComponent) : Child
        class Settings(val component: SettingsComponent) : Child

        // Medical history deep screens
        class MedicalRecordDetail(val component: MedicalRecordDetailComponent) : Child
        class PrescriptionDetail(val component: PrescriptionDetailComponent) : Child
        class HistoryAccessLogs(val component: HistoryAccessLogsComponent) : Child
        class ShareRequests(val component: ShareRequestsComponent) : Child
        class ApproveShareRequest(val component: ApproveShareRequestComponent) : Child

        // Symptom analysis
        class SymptomInput(val component: SymptomInputComponent) : Child
        class SymptomResults(val component: SymptomResultsComponent) : Child

        // Therapy packages
        class TherapyPackages(val component: TherapyPackagesListComponent) : Child
        class TherapyPackageDetail(val component: TherapyPackageDetailComponent) : Child
        class TherapyOffers(val component: TherapyOffersComponent) : Child
        class Negotiation(val component: NegotiationComponent) : Child

        // Moderation
        class ReportUser(val component: ReportUserComponent) : Child
        class BlockUser(val component: BlockUserComponent) : Child

        // Profile & account management
        class ClinicalProfile(val component: ClinicalProfileComponent) : Child
        class DeleteAccount(val component: DeleteAccountComponent) : Child
        class Membership(val component: MembershipComponent) : Child

        // Sharing management
        class ActiveAccesses(val component: ActiveAccessesComponent) : Child
    }

    sealed interface Output {
        data object Logout : Output
    }
}
