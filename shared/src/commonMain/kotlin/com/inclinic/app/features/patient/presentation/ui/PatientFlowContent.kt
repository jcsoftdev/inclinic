package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.patient.assistant.presentation.ui.AssistantChatScreen
import com.inclinic.app.features.patient.moderation.presentation.ui.BlockUserScreen
import com.inclinic.app.features.patient.moderation.presentation.ui.ReportUserScreen
import com.inclinic.app.features.patient.presentation.component.PatientFlowComponent
import com.inclinic.app.ui.atoms.AppNavBar
import com.inclinic.app.ui.atoms.PatientTab

@Composable
fun PatientFlowContent(component: PatientFlowComponent, modifier: Modifier = Modifier) {
    val onNav = component::onNavTabSelected
    val stack by component.stack.subscribeAsState()
    val currentTab = stack.active.instance.patientTabOrNull()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            currentTab?.let { tab ->
                AppNavBar(currentTab = tab, onTabSelected = onNav)
            }
        },
    ) { padding ->
        Children(stack = component.stack, modifier = Modifier.padding(padding)) { child ->
            when (val c = child.instance) {
                is PatientFlowComponent.Child.Home -> PatientHomeScreen(c.component, onNavTabSelected = onNav)
                is PatientFlowComponent.Child.Search -> DoctorSearchScreen(c.component, onNavTabSelected = onNav)
                is PatientFlowComponent.Child.DoctorProfile -> DoctorProfileScreen(c.component)
                is PatientFlowComponent.Child.ConsultType -> ConsultTypeScreen(c.component)
                is PatientFlowComponent.Child.Availability -> AvailabilityCalendarScreen(c.component)
                is PatientFlowComponent.Child.AddressPicker ->
                    com.inclinic.app.features.patient.address.presentation.AddressPickerScreen(c.component)
                is PatientFlowComponent.Child.Booking -> BookingScreen(c.component)
                is PatientFlowComponent.Child.Payment -> PaymentScreen(c.component)
                is PatientFlowComponent.Child.Appointments -> PatientAppointmentsListScreen(c.component, onNavTabSelected = onNav)
                is PatientFlowComponent.Child.AppointmentDetail -> AppointmentDetailScreen(c.component)
                is PatientFlowComponent.Child.CancelAppointment -> CancelAppointmentScreen(c.component)
                is PatientFlowComponent.Child.Chat -> ChatScreen(c.component)
                is PatientFlowComponent.Child.RescheduleResponse -> RescheduleResponseScreen(c.component)
                is PatientFlowComponent.Child.MedicalHistory -> MedicalHistoryScreen(c.component)
                is PatientFlowComponent.Child.Profile -> ProfileOverviewScreen(c.component)
                is PatientFlowComponent.Child.EditProfile -> PatientProfileScreen(c.component)
                is PatientFlowComponent.Child.AssistantChat -> AssistantChatScreen(c.component, onNavTabSelected = onNav)

                // Appointment lifecycle
                is PatientFlowComponent.Child.RescheduleAppointment -> RescheduleAppointmentScreen(c.component)
                is PatientFlowComponent.Child.ChangeVisitType -> ChangeVisitTypeScreen(c.component)
                is PatientFlowComponent.Child.DisputeAppointment -> DisputeAppointmentScreen(c.component)
                is PatientFlowComponent.Child.ConfirmRating -> ConfirmRatingScreen(c.component)

                // Messages & Notifications
                is PatientFlowComponent.Child.MessagesList -> MessagesListScreen(c.component)
                is PatientFlowComponent.Child.Notifications -> NotificationsScreen(c.component)
                is PatientFlowComponent.Child.Settings -> SettingsScreen(c.component)

                // Medical history deep screens
                is PatientFlowComponent.Child.MedicalRecordDetail -> MedicalRecordDetailScreen(c.component)
                is PatientFlowComponent.Child.PrescriptionDetail -> PrescriptionDetailScreen(c.component)
                is PatientFlowComponent.Child.HistoryAccessLogs -> HistoryAccessLogsScreen(c.component)
                is PatientFlowComponent.Child.HistoryAccessLogDetail -> HistoryAccessLogDetailScreen(c.component)
                is PatientFlowComponent.Child.ShareRequests -> ShareRequestsScreen(c.component)
                is PatientFlowComponent.Child.ApproveShareRequest -> ApproveShareRequestScreen(c.component)

                // Symptom analysis
                is PatientFlowComponent.Child.SymptomInput -> SymptomInputScreen(c.component)
                is PatientFlowComponent.Child.SymptomResults -> SymptomResultsScreen(c.component)

                // Therapy packages
                is PatientFlowComponent.Child.TherapyPackages -> TherapyPackagesListScreen(c.component)
                is PatientFlowComponent.Child.TherapyPackageDetail -> TherapyPackageDetailScreen(c.component)
                is PatientFlowComponent.Child.PackageStatement -> PackageStatementScreen(c.component)
                is PatientFlowComponent.Child.TherapyOffers -> TherapyOffersScreen(c.component)
                is PatientFlowComponent.Child.Negotiation -> NegotiationScreen(c.component)

                // Moderation
                is PatientFlowComponent.Child.ReportUser -> ReportUserScreen(c.component)
                is PatientFlowComponent.Child.BlockUser -> BlockUserScreen(c.component)

                // Profile & account management
                is PatientFlowComponent.Child.Membership -> MembershipScreen(c.component)
                is PatientFlowComponent.Child.ClinicalProfile -> ClinicalProfileScreen(c.component)
                is PatientFlowComponent.Child.ChangePassword -> ChangePasswordScreen(c.component)
                is PatientFlowComponent.Child.DeleteAccount -> DeleteAccountScreen(c.component)

                // Sharing management
                is PatientFlowComponent.Child.ActiveAccesses -> ActiveAccessesScreen(c.component)
            }
        }
    }
}

private fun PatientFlowComponent.Child.patientTabOrNull(): PatientTab? = when (this) {
    is PatientFlowComponent.Child.Home -> PatientTab.Home
    is PatientFlowComponent.Child.Search -> PatientTab.Search
    is PatientFlowComponent.Child.Appointments -> PatientTab.Appointments
    is PatientFlowComponent.Child.AssistantChat -> PatientTab.Messages
    is PatientFlowComponent.Child.Profile -> PatientTab.Profile
    else -> null
}
