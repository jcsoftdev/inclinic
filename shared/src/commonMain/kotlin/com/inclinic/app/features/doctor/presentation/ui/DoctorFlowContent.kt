package com.inclinic.app.features.doctor.presentation.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.doctor.messages.presentation.ui.DoctorChatListScreen
import com.inclinic.app.features.doctor.notifications.presentation.ui.DoctorNotificationsScreen
import com.inclinic.app.features.doctor.packages.presentation.ui.CreatePackageScreen
import com.inclinic.app.features.doctor.packages.presentation.ui.PackageDetailScreen
import com.inclinic.app.features.doctor.packages.presentation.ui.PackagesListScreen
import com.inclinic.app.features.doctor.patients.presentation.ui.PatientsListScreen
import com.inclinic.app.features.doctor.patients.presentation.ui.SearchPatientScreen
import com.inclinic.app.features.doctor.modality.presentation.ui.RespondModalityScreen
import com.inclinic.app.features.doctor.negotiation.presentation.ui.RespondPackageNegotiationScreen
import com.inclinic.app.features.doctor.presentation.component.DoctorFlowComponent
import com.inclinic.app.features.doctor.presentation.component.DoctorTab
import com.inclinic.app.features.doctor.reschedule.presentation.ui.RescheduleQueueScreen
import com.inclinic.app.features.doctor.reschedule_request.presentation.ui.RequestRescheduleScreen
import com.inclinic.app.features.doctor.profile.presentation.ui.EditSpecialtiesScreen
import com.inclinic.app.features.doctor.profile.presentation.ui.IncomeScreen
import com.inclinic.app.features.doctor.profile.presentation.ui.MiPerfilScreen
import com.inclinic.app.features.doctor.profile.presentation.ui.MySpecialtyRequestsScreen
import com.inclinic.app.features.doctor.profile.presentation.ui.PublicProfileScreen
import com.inclinic.app.features.doctor.profile.presentation.ui.RequestSpecialtyScreen
import com.inclinic.app.features.doctor.profile.presentation.ui.ReviewsScreen
import com.inclinic.app.features.doctor.settings.presentation.ui.DoctorSettingsScreen
import com.inclinic.app.features.doctor.prescriptions.presentation.ui.EditPrescriptionScreen
import com.inclinic.app.features.doctor.sharing.presentation.ui.RequestShareScreen
import com.inclinic.app.features.doctor.sharing.presentation.ui.ShareRequestsScreen
import com.inclinic.app.features.doctor.therapy_offers.presentation.ui.CreateTherapyOfferScreen
import com.inclinic.app.features.doctor.therapy_offers.presentation.ui.TherapyOffersListScreen
import com.inclinic.app.features.patient.presentation.ui.DeleteAccountScreen
import com.inclinic.app.features.doctor.no_shows.presentation.ui.NoShowQueueScreen
import com.inclinic.app.features.doctor.profile.presentation.ui.ChangePasswordScreen
import com.inclinic.app.ui.atoms.DoctorNavBar

@Composable
fun DoctorFlowContent(component: DoctorFlowComponent, modifier: Modifier = Modifier) {
    val currentTab by component.currentTab.subscribeAsState()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            DoctorNavBar(
                currentTab = currentTab,
                onTabSelected = component::onTabSelected,
            )
        },
    ) { padding ->
        val activeStack = when (currentTab) {
            DoctorTab.Inicio -> component.iniciStack
            DoctorTab.Agenda -> component.agendaStack
            DoctorTab.Pacientes -> component.pacientesStack
            DoctorTab.Mensajes -> component.mensajesStack
            DoctorTab.Perfil -> component.perfilStack
        }

        Children(
            stack = activeStack,
            modifier = Modifier.padding(padding),
            animation = stackAnimation(slide()),
        ) { child ->
            when (val c = child.instance) {
                is DoctorFlowComponent.Child.Dashboard -> DoctorDashboardScreen(c.component)
                is DoctorFlowComponent.Child.Notifications -> DoctorNotificationsScreen(c.component)
                is DoctorFlowComponent.Child.WeeklySchedule -> WeeklyScheduleScreen(c.component)
                is DoctorFlowComponent.Child.DailySchedule -> DailyScheduleScreen(c.component)
                is DoctorFlowComponent.Child.AppointmentDetail -> DoctorAppointmentDetailScreen(c.component)
                is DoctorFlowComponent.Child.CreateMedicalRecord -> CreateMedicalRecordScreen(c.component)
                is DoctorFlowComponent.Child.EditMedicalRecord -> EditMedicalRecordScreen(c.component)
                is DoctorFlowComponent.Child.ScheduleConfig -> ScheduleConfigScreen(c.component)
                is DoctorFlowComponent.Child.PriceConfig -> PriceConfigScreen(c.component)
                is DoctorFlowComponent.Child.Chat -> DoctorChatScreen(c.component)
                is DoctorFlowComponent.Child.PatientsList -> PatientsListScreen(c.component)
                is DoctorFlowComponent.Child.SearchPatient -> SearchPatientScreen(c.component)
                is DoctorFlowComponent.Child.PatientDetail -> PatientDetailScreen(c.component)
                is DoctorFlowComponent.Child.MedicalRecordsList -> MedicalRecordsListScreen(c.component)
                is DoctorFlowComponent.Child.ChatList -> DoctorChatListScreen(c.component)
                is DoctorFlowComponent.Child.MiPerfil -> MiPerfilScreen(c.component)
                is DoctorFlowComponent.Child.EditSpecialties -> EditSpecialtiesScreen(c.component)
                is DoctorFlowComponent.Child.RequestSpecialty -> RequestSpecialtyScreen(c.component)
                is DoctorFlowComponent.Child.MySpecialtyRequests -> MySpecialtyRequestsScreen(c.component)
                is DoctorFlowComponent.Child.Income -> IncomeScreen(c.component)
                is DoctorFlowComponent.Child.Reviews -> ReviewsScreen(c.component)
                is DoctorFlowComponent.Child.PublicProfile -> PublicProfileScreen(c.component)
                is DoctorFlowComponent.Child.Packages -> PackagesListScreen(c.component)
                is DoctorFlowComponent.Child.CreatePackage -> CreatePackageScreen(c.component)
                is DoctorFlowComponent.Child.PackageDetail -> PackageDetailScreen(c.component)
                is DoctorFlowComponent.Child.ShareRequests -> ShareRequestsScreen(c.component)
                is DoctorFlowComponent.Child.RequestShare -> RequestShareScreen(c.component)
                is DoctorFlowComponent.Child.Settings -> DoctorSettingsScreen(c.component)
                is DoctorFlowComponent.Child.RescheduleQueue -> RescheduleQueueScreen(c.component)
                is DoctorFlowComponent.Child.RequestReschedule -> RequestRescheduleScreen(c.component)
                is DoctorFlowComponent.Child.RespondModality -> RespondModalityScreen(c.component)
                is DoctorFlowComponent.Child.RespondPackageNegotiation -> RespondPackageNegotiationScreen(c.component)
                is DoctorFlowComponent.Child.TherapyOffers -> TherapyOffersListScreen(c.component)
                is DoctorFlowComponent.Child.CreateTherapyOffer -> CreateTherapyOfferScreen(c.component)
                is DoctorFlowComponent.Child.EditPrescription -> EditPrescriptionScreen(c.component)
                is DoctorFlowComponent.Child.DeleteAccount -> DeleteAccountScreen(c.component)
                is DoctorFlowComponent.Child.NoShowQueue -> NoShowQueueScreen(c.component)
                is DoctorFlowComponent.Child.ChangePassword -> ChangePasswordScreen(c.component)
            }
        }
    }
}
