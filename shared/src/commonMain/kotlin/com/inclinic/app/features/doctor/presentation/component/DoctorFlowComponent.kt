package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.navigation.DoctorConfig
import com.inclinic.app.features.doctor.messages.presentation.component.DoctorChatListComponent
import com.inclinic.app.features.doctor.notifications.presentation.component.DoctorNotificationsComponent
import com.inclinic.app.features.doctor.packages.presentation.component.CreatePackageComponent
import com.inclinic.app.features.doctor.packages.presentation.component.PackageDetailComponent
import com.inclinic.app.features.doctor.packages.presentation.component.PackagesListComponent
import com.inclinic.app.features.doctor.patients.presentation.component.PatientsListComponent
import com.inclinic.app.features.doctor.patients.presentation.component.SearchPatientComponent
import com.inclinic.app.features.doctor.profile.presentation.component.ChangePasswordComponent
import com.inclinic.app.features.doctor.profile.presentation.component.EditSpecialtiesComponent
import com.inclinic.app.features.doctor.profile.presentation.component.IncomeComponent
import com.inclinic.app.features.doctor.profile.presentation.component.MiPerfilComponent
import com.inclinic.app.features.doctor.profile.presentation.component.PublicProfileComponent
import com.inclinic.app.features.doctor.profile.presentation.component.ReviewsComponent
import com.inclinic.app.features.doctor.modality.presentation.component.RespondModalityComponent
import com.inclinic.app.features.doctor.negotiation.presentation.component.RespondPackageNegotiationComponent
import com.inclinic.app.features.doctor.profile.presentation.component.MySpecialtyRequestsComponent
import com.inclinic.app.features.doctor.profile.presentation.component.RequestSpecialtyComponent
import com.inclinic.app.features.doctor.reschedule.presentation.component.RescheduleQueueComponent
import com.inclinic.app.features.doctor.reschedule_request.presentation.component.RequestRescheduleComponent
import com.inclinic.app.features.doctor.settings.presentation.component.DoctorSettingsComponent
import com.inclinic.app.features.doctor.prescriptions.presentation.component.EditPrescriptionComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.RequestShareComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.ShareRequestsListComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.CreateTherapyOfferComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.TherapyOffersListComponent
import com.inclinic.app.features.patient.presentation.component.DeleteAccountComponent
import com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowQueueComponent

interface DoctorFlowComponent {
    val currentTab: Value<DoctorTab>
    val iniciStack: Value<ChildStack<*, Child>>
    val agendaStack: Value<ChildStack<*, Child>>
    val pacientesStack: Value<ChildStack<*, Child>>
    val mensajesStack: Value<ChildStack<*, Child>>
    val perfilStack: Value<ChildStack<*, Child>>

    fun onTabSelected(tab: DoctorTab)

    /** Navigate to a doctor destination from outside the flow (e.g., a deep link). */
    fun navigateTo(config: DoctorConfig)

    sealed interface Child {
        // Inicio tab
        class Dashboard(val component: DoctorDashboardComponent) : Child
        class Notifications(val component: DoctorNotificationsComponent) : Child

        // Agenda tab
        class WeeklySchedule(val component: WeeklyScheduleComponent) : Child
        class DailySchedule(val component: DailyScheduleComponent) : Child
        class AppointmentDetail(val component: DoctorAppointmentDetailComponent) : Child
        class CreateMedicalRecord(val component: CreateMedicalRecordComponent) : Child
        class EditMedicalRecord(val component: EditMedicalRecordComponent) : Child
        class ScheduleConfig(val component: ScheduleConfigComponent) : Child
        class PriceConfig(val component: PriceConfigComponent) : Child
        class Chat(val component: DoctorChatComponent) : Child

        // Pacientes tab
        class PatientsList(val component: PatientsListComponent) : Child
        class SearchPatient(val component: SearchPatientComponent) : Child
        class PatientDetail(val component: PatientDetailComponent) : Child
        class MedicalRecordsList(val component: MedicalRecordsListComponent) : Child

        // Mensajes tab
        class ChatList(val component: DoctorChatListComponent) : Child

        // Perfil tab
        class MiPerfil(val component: MiPerfilComponent) : Child
        class EditSpecialties(val component: EditSpecialtiesComponent) : Child
        class RequestSpecialty(val component: RequestSpecialtyComponent) : Child
        class MySpecialtyRequests(val component: MySpecialtyRequestsComponent) : Child
        class Income(val component: IncomeComponent) : Child
        class Reviews(val component: ReviewsComponent) : Child
        class PublicProfile(val component: PublicProfileComponent) : Child
        class Packages(val component: PackagesListComponent) : Child
        class CreatePackage(val component: CreatePackageComponent) : Child
        class PackageDetail(val component: PackageDetailComponent) : Child
        class ShareRequests(val component: ShareRequestsListComponent) : Child
        class RequestShare(val component: RequestShareComponent) : Child
        class Settings(val component: DoctorSettingsComponent) : Child
        class RescheduleQueue(val component: RescheduleQueueComponent) : Child
        class RequestReschedule(val component: RequestRescheduleComponent) : Child
        class RespondModality(val component: RespondModalityComponent) : Child
        class RespondPackageNegotiation(val component: RespondPackageNegotiationComponent) : Child
        class TherapyOffers(val component: TherapyOffersListComponent) : Child
        class CreateTherapyOffer(val component: CreateTherapyOfferComponent) : Child
        class EditPrescription(val component: EditPrescriptionComponent) : Child
        class DeleteAccount(val component: DeleteAccountComponent) : Child
        class NoShowQueue(val component: NoShowQueueComponent) : Child
        class ChangePassword(val component: ChangePasswordComponent) : Child
    }

    sealed interface Output {
        data object Logout : Output
    }
}
