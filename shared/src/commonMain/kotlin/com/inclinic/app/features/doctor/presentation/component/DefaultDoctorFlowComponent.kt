@file:OptIn(DelicateDecomposeApi::class)

package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.navigation.DoctorConfig
import com.inclinic.app.features.doctor.messages.presentation.component.DefaultDoctorChatListComponent
import com.inclinic.app.features.doctor.messages.presentation.component.DoctorChatListComponent
import com.inclinic.app.features.doctor.notifications.presentation.component.DefaultDoctorNotificationsComponent
import com.inclinic.app.features.doctor.notifications.presentation.component.DoctorNotificationsComponent
import com.inclinic.app.features.doctor.packages.presentation.component.CreatePackageComponent
import com.inclinic.app.features.doctor.packages.presentation.component.DefaultCreatePackageComponent
import com.inclinic.app.features.doctor.packages.presentation.component.DefaultPackagesListComponent
import com.inclinic.app.features.doctor.packages.presentation.component.PackageDetailComponent
import com.inclinic.app.features.doctor.packages.presentation.component.PackagesListComponent
import com.inclinic.app.features.doctor.patients.presentation.component.DefaultPatientsListComponent
import com.inclinic.app.features.doctor.patients.presentation.component.DefaultSearchPatientComponent
import com.inclinic.app.features.doctor.patients.presentation.component.PatientsListComponent
import com.inclinic.app.features.doctor.patients.presentation.component.SearchPatientComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultEditSpecialtiesComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultIncomeComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultMiPerfilComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultRequestSpecialtyComponent
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
import com.inclinic.app.features.doctor.sharing.presentation.component.DefaultRequestShareComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.DefaultShareRequestsListComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.RequestShareComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.ShareRequestsListComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.CreateTherapyOfferComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.TherapyOffersListComponent
import com.inclinic.app.features.patient.presentation.component.DeleteAccountComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class DefaultDoctorFlowComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val dispatchers: AppDispatchers,
    private val dashboardFactory: (ComponentContext, String, (DoctorDashboardComponent.Output) -> Unit) -> DoctorDashboardComponent,
    private val dailyScheduleFactory: (ComponentContext, String, (DailyScheduleComponent.Output) -> Unit) -> DailyScheduleComponent,
    private val weeklyScheduleFactory: (ComponentContext, String, (WeeklyScheduleComponent.Output) -> Unit) -> WeeklyScheduleComponent,
    private val appointmentDetailFactory: (ComponentContext, String, (DoctorAppointmentDetailComponent.Output) -> Unit) -> DoctorAppointmentDetailComponent,
    private val patientDetailFactory: (ComponentContext, String, (PatientDetailComponent.Output) -> Unit) -> PatientDetailComponent,
    private val medicalRecordsListFactory: (ComponentContext, String, (MedicalRecordsListComponent.Output) -> Unit) -> MedicalRecordsListComponent,
    private val createMedicalRecordFactory: (ComponentContext, String, String?, (CreateMedicalRecordComponent.Output) -> Unit) -> CreateMedicalRecordComponent,
    private val editMedicalRecordFactory: (ComponentContext, String, (EditMedicalRecordComponent.Output) -> Unit) -> EditMedicalRecordComponent,
    private val chatFactory: (ComponentContext, String) -> DoctorChatComponent,
    private val scheduleConfigFactory: (ComponentContext, String, (ScheduleConfigComponent.Output) -> Unit) -> ScheduleConfigComponent,
    private val priceConfigFactory: (ComponentContext, String, (PriceConfigComponent.Output) -> Unit) -> PriceConfigComponent,
    private val patientsListFactory: (ComponentContext, (PatientsListComponent.Output) -> Unit) -> PatientsListComponent,
    private val searchPatientFactory: (ComponentContext, (SearchPatientComponent.Output) -> Unit) -> SearchPatientComponent,
    private val packagesListFactory: (ComponentContext, (PackagesListComponent.Output) -> Unit) -> PackagesListComponent,
    private val createPackageFactory: (ComponentContext, (CreatePackageComponent.Output) -> Unit) -> CreatePackageComponent,
    private val packageDetailFactory: (ComponentContext, String, (PackageDetailComponent.Output) -> Unit) -> PackageDetailComponent,
    private val shareRequestsListFactory: (ComponentContext, (ShareRequestsListComponent.Output) -> Unit) -> ShareRequestsListComponent,
    private val requestShareFactory: (ComponentContext, (RequestShareComponent.Output) -> Unit) -> RequestShareComponent,
    private val chatListFactory: (ComponentContext, (DoctorChatListComponent.Output) -> Unit) -> DoctorChatListComponent,
    private val miPerfilFactory: (ComponentContext, (MiPerfilComponent.Output) -> Unit) -> MiPerfilComponent,
    private val editSpecialtiesFactory: (ComponentContext, (EditSpecialtiesComponent.Output) -> Unit) -> EditSpecialtiesComponent,
    private val requestSpecialtyFactory: (ComponentContext, (RequestSpecialtyComponent.Output) -> Unit) -> RequestSpecialtyComponent,
    private val mySpecialtyRequestsFactory: (ComponentContext, (MySpecialtyRequestsComponent.Output) -> Unit) -> MySpecialtyRequestsComponent,
    private val incomeFactory: (ComponentContext, (IncomeComponent.Output) -> Unit) -> IncomeComponent,
    private val reviewsFactory: (ComponentContext, (ReviewsComponent.Output) -> Unit) -> ReviewsComponent,
    private val publicProfileFactory: (ComponentContext, () -> Unit) -> PublicProfileComponent,
    private val notificationsFactory: (ComponentContext, (DoctorNotificationsComponent.Output) -> Unit) -> DoctorNotificationsComponent,
    private val settingsFactory: (ComponentContext, (DoctorSettingsComponent.Output) -> Unit) -> DoctorSettingsComponent,
    private val rescheduleQueueFactory: (ComponentContext, (RescheduleQueueComponent.Output) -> Unit) -> RescheduleQueueComponent,
    private val requestRescheduleFactory: (ComponentContext, String, (RequestRescheduleComponent.Output) -> Unit) -> RequestRescheduleComponent,
    private val respondModalityFactory: (ComponentContext, String, (RespondModalityComponent.Output) -> Unit) -> RespondModalityComponent,
    private val respondPackageNegotiationFactory: (ComponentContext, String, (RespondPackageNegotiationComponent.Output) -> Unit) -> RespondPackageNegotiationComponent,
    private val therapyOffersListFactory: (ComponentContext, (TherapyOffersListComponent.Output) -> Unit) -> TherapyOffersListComponent,
    private val createTherapyOfferFactory: (ComponentContext, (CreateTherapyOfferComponent.Output) -> Unit) -> CreateTherapyOfferComponent,
    private val editPrescriptionFactory: (ComponentContext, String, (EditPrescriptionComponent.Output) -> Unit) -> EditPrescriptionComponent,
    private val deleteAccountFactory: (ComponentContext, (DeleteAccountComponent.Output) -> Unit) -> DeleteAccountComponent,
    private val onOutput: (DoctorFlowComponent.Output) -> Unit,
) : DoctorFlowComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    // ── Per-tab navigation objects ────────────────────────────────────────────

    private val inicioNav = StackNavigation<DoctorConfig>()
    private val agendaNav = StackNavigation<DoctorConfig>()
    private val pacientesNav = StackNavigation<DoctorConfig>()
    private val mensajesNav = StackNavigation<DoctorConfig>()
    private val perfilNav = StackNavigation<DoctorConfig>()

    // ── Tab state ─────────────────────────────────────────────────────────────

    private val _currentTab = MutableValue(DoctorTab.Inicio)
    override val currentTab: Value<DoctorTab> = _currentTab

    // ── Per-tab child stacks ──────────────────────────────────────────────────

    override val iniciStack: Value<ChildStack<*, DoctorFlowComponent.Child>> = childStack(
        source = inicioNav,
        serializer = DoctorConfig.serializer(),
        initialConfiguration = DoctorConfig.Dashboard,
        key = "Inicio",
        handleBackButton = false,
        childFactory = ::createChild,
    )

    override val agendaStack: Value<ChildStack<*, DoctorFlowComponent.Child>> = childStack(
        source = agendaNav,
        serializer = DoctorConfig.serializer(),
        initialConfiguration = DoctorConfig.Schedule,
        key = "Agenda",
        handleBackButton = false,
        childFactory = ::createChild,
    )

    override val pacientesStack: Value<ChildStack<*, DoctorFlowComponent.Child>> = childStack(
        source = pacientesNav,
        serializer = DoctorConfig.serializer(),
        initialConfiguration = DoctorConfig.Patients,
        key = "Pacientes",
        handleBackButton = false,
        childFactory = ::createChild,
    )

    override val mensajesStack: Value<ChildStack<*, DoctorFlowComponent.Child>> = childStack(
        source = mensajesNav,
        serializer = DoctorConfig.serializer(),
        initialConfiguration = DoctorConfig.Messages,
        key = "Mensajes",
        handleBackButton = false,
        childFactory = ::createChild,
    )

    override val perfilStack: Value<ChildStack<*, DoctorFlowComponent.Child>> = childStack(
        source = perfilNav,
        serializer = DoctorConfig.serializer(),
        initialConfiguration = DoctorConfig.Profile,
        key = "Perfil",
        handleBackButton = false,
        childFactory = ::createChild,
    )

    // ── Navigation API ────────────────────────────────────────────────────────

    override fun onTabSelected(tab: DoctorTab) {
        _currentTab.value = tab
    }

    override fun navigateTo(config: DoctorConfig) {
        activeNav().push(config)
    }

    private fun activeNav(): StackNavigation<DoctorConfig> = when (_currentTab.value) {
        DoctorTab.Inicio -> inicioNav
        DoctorTab.Agenda -> agendaNav
        DoctorTab.Pacientes -> pacientesNav
        DoctorTab.Mensajes -> mensajesNav
        DoctorTab.Perfil -> perfilNav
    }

    // ── Child factory ─────────────────────────────────────────────────────────

    private fun createChild(config: DoctorConfig, ctx: ComponentContext): DoctorFlowComponent.Child =
        when (config) {

            // ── Inicio tab ────────────────────────────────────────────────────
            is DoctorConfig.Dashboard -> DoctorFlowComponent.Child.Dashboard(
                dashboardFactory(ctx, doctorId) { output ->
                    when (output) {
                        DoctorDashboardComponent.Output.NavigateToSchedule -> {
                            _currentTab.value = DoctorTab.Agenda
                        }
                        DoctorDashboardComponent.Output.NavigateToPendingAppointments -> {
                            _currentTab.value = DoctorTab.Agenda
                        }
                        DoctorDashboardComponent.Output.NavigateToNotifications -> {
                            inicioNav.push(DoctorConfig.Notifications)
                        }
                        is DoctorDashboardComponent.Output.NavigateToAppointmentDetail -> {
                            _currentTab.value = DoctorTab.Agenda
                            agendaNav.push(DoctorConfig.AppointmentDetail(output.appointmentId))
                        }
                        DoctorDashboardComponent.Output.NavigateToCreateMedicalRecord -> {
                            _currentTab.value = DoctorTab.Pacientes
                            pacientesNav.push(DoctorConfig.Patients)
                        }
                        DoctorDashboardComponent.Output.NavigateToPatients -> {
                            _currentTab.value = DoctorTab.Pacientes
                        }
                        DoctorDashboardComponent.Output.NavigateToPackages -> {
                            _currentTab.value = DoctorTab.Perfil
                            perfilNav.push(DoctorConfig.Packages)
                        }
                        DoctorDashboardComponent.Output.NavigateToIncome -> {
                            _currentTab.value = DoctorTab.Perfil
                            perfilNav.push(DoctorConfig.Income)
                        }
                    }
                }
            )
            is DoctorConfig.Notifications -> DoctorFlowComponent.Child.Notifications(
                notificationsFactory(ctx) { output ->
                    when (output) {
                        DoctorNotificationsComponent.Output.Back -> inicioNav.pop()
                    }
                }
            )

            // ── Agenda tab ────────────────────────────────────────────────────
            is DoctorConfig.Schedule -> DoctorFlowComponent.Child.WeeklySchedule(
                weeklyScheduleFactory(ctx, doctorId) { output ->
                    when (output) {
                        is WeeklyScheduleComponent.Output.NavigateToDailySchedule ->
                            agendaNav.push(DoctorConfig.DailySchedule(output.date.toString()))
                        WeeklyScheduleComponent.Output.Back -> agendaNav.pop()
                    }
                }
            )
            is DoctorConfig.DailySchedule -> DoctorFlowComponent.Child.DailySchedule(
                dailyScheduleFactory(ctx, doctorId) { output ->
                    when (output) {
                        is DailyScheduleComponent.Output.NavigateToAppointmentDetail ->
                            agendaNav.push(DoctorConfig.AppointmentDetail(output.appointmentId))
                        DailyScheduleComponent.Output.OpenRescheduleQueue ->
                            agendaNav.push(DoctorConfig.RescheduleQueue)
                        DailyScheduleComponent.Output.Back -> agendaNav.pop()
                    }
                }
            )
            is DoctorConfig.AppointmentDetail -> DoctorFlowComponent.Child.AppointmentDetail(
                appointmentDetailFactory(ctx, config.appointmentId) { output ->
                    when (output) {
                        is DoctorAppointmentDetailComponent.Output.NavigateToPatientDetail -> {
                            // Cross-tab: switch to Pacientes and push patient detail
                            _currentTab.value = DoctorTab.Pacientes
                            pacientesNav.push(DoctorConfig.PatientDetail(output.patientId))
                        }
                        is DoctorAppointmentDetailComponent.Output.NavigateToChat ->
                            agendaNav.push(DoctorConfig.Chat(output.appointmentId))
                        is DoctorAppointmentDetailComponent.Output.NavigateToRequestReschedule ->
                            agendaNav.push(DoctorConfig.RequestReschedule(output.appointmentId))
                        is DoctorAppointmentDetailComponent.Output.NavigateToCreateMedicalRecord -> {
                            // Real appointmentId from the loaded appointment — links the new record.
                            _currentTab.value = DoctorTab.Pacientes
                            pacientesNav.push(
                                DoctorConfig.MedicalRecordEditor(
                                    patientId = output.patientId,
                                    appointmentId = output.appointmentId,
                                ),
                            )
                        }
                        DoctorAppointmentDetailComponent.Output.Back -> agendaNav.pop()
                    }
                }
            )
            is DoctorConfig.CompleteWithEvidence -> DoctorFlowComponent.Child.AppointmentDetail(
                appointmentDetailFactory(ctx, config.appointmentId) { output ->
                    when (output) {
                        is DoctorAppointmentDetailComponent.Output.NavigateToPatientDetail -> {
                            _currentTab.value = DoctorTab.Pacientes
                            pacientesNav.push(DoctorConfig.PatientDetail(output.patientId))
                        }
                        is DoctorAppointmentDetailComponent.Output.NavigateToChat ->
                            agendaNav.push(DoctorConfig.Chat(output.appointmentId))
                        is DoctorAppointmentDetailComponent.Output.NavigateToRequestReschedule ->
                            agendaNav.push(DoctorConfig.RequestReschedule(output.appointmentId))
                        is DoctorAppointmentDetailComponent.Output.NavigateToCreateMedicalRecord -> {
                            _currentTab.value = DoctorTab.Pacientes
                            pacientesNav.push(
                                DoctorConfig.MedicalRecordEditor(
                                    patientId = output.patientId,
                                    appointmentId = output.appointmentId,
                                ),
                            )
                        }
                        DoctorAppointmentDetailComponent.Output.Back -> agendaNav.pop()
                    }
                }
            )
            is DoctorConfig.MedicalRecordEditor -> DoctorFlowComponent.Child.CreateMedicalRecord(
                // Factory signature is (ctx, patientId, appointmentId, onOutput).
                // appointmentId is null when entered from the patient-history list, and
                // a real id when entered from AppointmentDetail (NavigateToCreateMedicalRecord).
                createMedicalRecordFactory(ctx, config.patientId, config.appointmentId) { output ->
                    when (output) {
                        is CreateMedicalRecordComponent.Output.Success -> activeNav().pop()
                        CreateMedicalRecordComponent.Output.Back -> activeNav().pop()
                    }
                }
            )
            is DoctorConfig.EditMedicalRecord -> DoctorFlowComponent.Child.EditMedicalRecord(
                editMedicalRecordFactory(ctx, config.recordId) { output ->
                    when (output) {
                        EditMedicalRecordComponent.Output.Success -> activeNav().pop()
                        EditMedicalRecordComponent.Output.Back -> activeNav().pop()
                    }
                }
            )
            is DoctorConfig.ScheduleConfig -> DoctorFlowComponent.Child.ScheduleConfig(
                scheduleConfigFactory(ctx, doctorId) { output ->
                    when (output) {
                        ScheduleConfigComponent.Output.Back -> activeNav().pop()
                    }
                }
            )
            is DoctorConfig.PriceConfig -> DoctorFlowComponent.Child.PriceConfig(
                priceConfigFactory(ctx, doctorId) { output ->
                    when (output) {
                        PriceConfigComponent.Output.Back -> activeNav().pop()
                    }
                }
            )
            is DoctorConfig.Chat -> DoctorFlowComponent.Child.Chat(
                chatFactory(ctx, config.appointmentId)
            )

            // ── Pacientes tab ─────────────────────────────────────────────────
            is DoctorConfig.Patients -> DoctorFlowComponent.Child.PatientsList(
                patientsListFactory(ctx) { output ->
                    when (output) {
                        is PatientsListComponent.Output.NavigateToPatient ->
                            pacientesNav.push(DoctorConfig.PatientDetail(output.patientId))
                        PatientsListComponent.Output.NavigateToSearch ->
                            pacientesNav.push(DoctorConfig.SearchPatient)
                        PatientsListComponent.Output.Back -> pacientesNav.pop()
                    }
                }
            )
            is DoctorConfig.SearchPatient -> DoctorFlowComponent.Child.SearchPatient(
                searchPatientFactory(ctx) { output ->
                    when (output) {
                        is SearchPatientComponent.Output.NavigateToPatient ->
                            pacientesNav.push(DoctorConfig.PatientDetail(output.patientId))
                        SearchPatientComponent.Output.Back -> pacientesNav.pop()
                    }
                }
            )
            is DoctorConfig.PatientDetail -> DoctorFlowComponent.Child.PatientDetail(
                patientDetailFactory(ctx, config.patientId) { output ->
                    when (output) {
                        is PatientDetailComponent.Output.NavigateToMedicalRecords ->
                            pacientesNav.push(DoctorConfig.MedicalRecordsList(output.patientId))
                        PatientDetailComponent.Output.Back -> pacientesNav.pop()
                    }
                }
            )
            is DoctorConfig.MedicalRecordsList -> DoctorFlowComponent.Child.MedicalRecordsList(
                medicalRecordsListFactory(ctx, config.patientId) { output ->
                    when (output) {
                        is MedicalRecordsListComponent.Output.NavigateToCreateRecord ->
                            pacientesNav.push(DoctorConfig.MedicalRecordEditor(output.patientId, output.appointmentId))
                        is MedicalRecordsListComponent.Output.NavigateToEditRecord ->
                            pacientesNav.push(DoctorConfig.EditMedicalRecord(output.recordId))
                        MedicalRecordsListComponent.Output.Back -> pacientesNav.pop()
                    }
                }
            )

            // ── Mensajes tab ──────────────────────────────────────────────────
            is DoctorConfig.Messages -> DoctorFlowComponent.Child.ChatList(
                chatListFactory(ctx) { output ->
                    when (output) {
                        is DoctorChatListComponent.Output.NavigateToConversation ->
                            mensajesNav.push(DoctorConfig.Conversation(output.threadId))
                        DoctorChatListComponent.Output.Back -> mensajesNav.pop()
                    }
                }
            )
            is DoctorConfig.Conversation -> DoctorFlowComponent.Child.Chat(
                chatFactory(ctx, config.threadId)
            )

            // ── Perfil tab ────────────────────────────────────────────────────
            is DoctorConfig.Profile -> DoctorFlowComponent.Child.MiPerfil(
                miPerfilFactory(ctx) { output ->
                    when (output) {
                        MiPerfilComponent.Output.EditSpecialties ->
                            perfilNav.push(DoctorConfig.EditSpecialties)
                        MiPerfilComponent.Output.RequestSpecialty ->
                            perfilNav.push(DoctorConfig.RequestSpecialty)
                        MiPerfilComponent.Output.MySpecialtyRequests ->
                            perfilNav.push(DoctorConfig.MySpecialtyRequests)
                        MiPerfilComponent.Output.Income ->
                            perfilNav.push(DoctorConfig.Income)
                        MiPerfilComponent.Output.Reviews ->
                            perfilNav.push(DoctorConfig.Reviews)
                        MiPerfilComponent.Output.PublicProfile ->
                            perfilNav.push(DoctorConfig.PublicProfile)
                        MiPerfilComponent.Output.EditHorarios ->
                            perfilNav.push(DoctorConfig.ScheduleConfig)
                        MiPerfilComponent.Output.Packages ->
                            perfilNav.push(DoctorConfig.Packages)
                        MiPerfilComponent.Output.Sharing ->
                            perfilNav.push(DoctorConfig.ShareRequests)
                        MiPerfilComponent.Output.Settings ->
                            perfilNav.push(DoctorConfig.Settings)
                        MiPerfilComponent.Output.TherapyOffers ->
                            perfilNav.push(DoctorConfig.TherapyOffers)
                        MiPerfilComponent.Output.Logout ->
                            onOutput(DoctorFlowComponent.Output.Logout)
                    }
                }
            )
            is DoctorConfig.EditSpecialties -> DoctorFlowComponent.Child.EditSpecialties(
                editSpecialtiesFactory(ctx) { output ->
                    when (output) {
                        EditSpecialtiesComponent.Output.Back -> perfilNav.pop()
                        EditSpecialtiesComponent.Output.Saved -> perfilNav.pop()
                        EditSpecialtiesComponent.Output.NavigateToRequestSpecialty ->
                            perfilNav.push(DoctorConfig.RequestSpecialty)
                    }
                }
            )
            is DoctorConfig.RequestSpecialty -> DoctorFlowComponent.Child.RequestSpecialty(
                requestSpecialtyFactory(ctx) { output ->
                    when (output) {
                        RequestSpecialtyComponent.Output.Back -> perfilNav.pop()
                        RequestSpecialtyComponent.Output.Submitted -> perfilNav.pop()
                    }
                }
            )
            is DoctorConfig.MySpecialtyRequests -> DoctorFlowComponent.Child.MySpecialtyRequests(
                mySpecialtyRequestsFactory(ctx) { output ->
                    when (output) {
                        MySpecialtyRequestsComponent.Output.Back -> perfilNav.pop()
                        MySpecialtyRequestsComponent.Output.RequestNew -> perfilNav.push(DoctorConfig.RequestSpecialty)
                    }
                }
            )
            is DoctorConfig.Income -> DoctorFlowComponent.Child.Income(
                incomeFactory(ctx) { output ->
                    when (output) {
                        IncomeComponent.Output.Back -> perfilNav.pop()
                    }
                }
            )
            is DoctorConfig.Reviews -> DoctorFlowComponent.Child.Reviews(
                reviewsFactory(ctx) { output ->
                    when (output) {
                        ReviewsComponent.Output.Back -> perfilNav.pop()
                    }
                }
            )
            is DoctorConfig.PublicProfile -> DoctorFlowComponent.Child.PublicProfile(
                publicProfileFactory(ctx) { perfilNav.pop() }
            )
            is DoctorConfig.Packages -> DoctorFlowComponent.Child.Packages(
                packagesListFactory(ctx) { output ->
                    when (output) {
                        PackagesListComponent.Output.NavigateToCreate ->
                            perfilNav.push(DoctorConfig.CreatePackage)
                        is PackagesListComponent.Output.NavigateToDetail ->
                            perfilNav.push(DoctorConfig.PackageDetail(output.packageId))
                        PackagesListComponent.Output.Back -> perfilNav.pop()
                    }
                }
            )
            is DoctorConfig.CreatePackage -> DoctorFlowComponent.Child.CreatePackage(
                createPackageFactory(ctx) { output ->
                    when (output) {
                        CreatePackageComponent.Output.PackageCreated -> perfilNav.pop()
                        CreatePackageComponent.Output.Back -> perfilNav.pop()
                    }
                }
            )
            is DoctorConfig.PackageDetail -> DoctorFlowComponent.Child.PackageDetail(
                packageDetailFactory(ctx, config.packageId) { output ->
                    when (output) {
                        PackageDetailComponent.Output.Cancelled -> perfilNav.pop()
                        PackageDetailComponent.Output.Back -> perfilNav.pop()
                    }
                }
            )
            is DoctorConfig.ShareRequests -> DoctorFlowComponent.Child.ShareRequests(
                shareRequestsListFactory(ctx) { output ->
                    when (output) {
                        ShareRequestsListComponent.Output.NavigateToRequestShare ->
                            perfilNav.push(DoctorConfig.RequestShare)
                        ShareRequestsListComponent.Output.Back -> perfilNav.pop()
                    }
                }
            )
            is DoctorConfig.RequestShare -> DoctorFlowComponent.Child.RequestShare(
                requestShareFactory(ctx) { output ->
                    when (output) {
                        RequestShareComponent.Output.Success -> perfilNav.pop()
                        RequestShareComponent.Output.Back -> perfilNav.pop()
                    }
                }
            )
            is DoctorConfig.Settings -> DoctorFlowComponent.Child.Settings(
                settingsFactory(ctx) { output ->
                    when (output) {
                        DoctorSettingsComponent.Output.Back -> perfilNav.pop()
                        DoctorSettingsComponent.Output.LoggedOut -> onOutput(DoctorFlowComponent.Output.Logout)
                        DoctorSettingsComponent.Output.NavigateToDeleteAccount ->
                            perfilNav.push(DoctorConfig.DeleteAccount)
                    }
                }
            )
            is DoctorConfig.DeleteAccount -> DoctorFlowComponent.Child.DeleteAccount(
                deleteAccountFactory(ctx) { output ->
                    when (output) {
                        DeleteAccountComponent.Output.Back -> perfilNav.pop()
                        // Session cleaned via LogoutUseCase → SessionEvents; RootComponent handles nav.
                        DeleteAccountComponent.Output.Deleted -> Unit
                    }
                }
            )
            is DoctorConfig.RescheduleQueue -> DoctorFlowComponent.Child.RescheduleQueue(
                rescheduleQueueFactory(ctx) { output ->
                    when (output) {
                        RescheduleQueueComponent.Output.Back -> agendaNav.pop()
                    }
                }
            )
            is DoctorConfig.RequestReschedule -> DoctorFlowComponent.Child.RequestReschedule(
                requestRescheduleFactory(ctx, config.appointmentId) { output ->
                    when (output) {
                        RequestRescheduleComponent.Output.Success -> agendaNav.pop()
                        RequestRescheduleComponent.Output.Back -> agendaNav.pop()
                    }
                }
            )
            is DoctorConfig.RespondModality -> DoctorFlowComponent.Child.RespondModality(
                respondModalityFactory(ctx, config.requestId) { output ->
                    when (output) {
                        RespondModalityComponent.Output.Responded -> inicioNav.pop()
                        RespondModalityComponent.Output.Back -> inicioNav.pop()
                    }
                }
            )
            is DoctorConfig.RespondPackageNegotiation -> DoctorFlowComponent.Child.RespondPackageNegotiation(
                respondPackageNegotiationFactory(ctx, config.negotiationId) { output ->
                    when (output) {
                        RespondPackageNegotiationComponent.Output.Responded -> activeNav().pop()
                        RespondPackageNegotiationComponent.Output.Back -> activeNav().pop()
                    }
                }
            )

            is DoctorConfig.TherapyOffers -> DoctorFlowComponent.Child.TherapyOffers(
                therapyOffersListFactory(ctx) { output ->
                    when (output) {
                        TherapyOffersListComponent.Output.NavigateToCreate ->
                            perfilNav.push(DoctorConfig.CreateTherapyOffer)
                        TherapyOffersListComponent.Output.Back -> perfilNav.pop()
                    }
                }
            )
            is DoctorConfig.CreateTherapyOffer -> DoctorFlowComponent.Child.CreateTherapyOffer(
                createTherapyOfferFactory(ctx) { output ->
                    when (output) {
                        CreateTherapyOfferComponent.Output.OfferCreated -> perfilNav.pop()
                        CreateTherapyOfferComponent.Output.Back -> perfilNav.pop()
                    }
                }
            )
            is DoctorConfig.EditPrescription -> DoctorFlowComponent.Child.EditPrescription(
                editPrescriptionFactory(ctx, config.prescriptionId) { output ->
                    when (output) {
                        EditPrescriptionComponent.Output.Saved -> activeNav().pop()
                        EditPrescriptionComponent.Output.Back -> activeNav().pop()
                    }
                }
            )

            // These configs are handled at the root level (via RootConfig), not inside the flow
            DoctorConfig.Onboarding,
            DoctorConfig.Enviado,
            DoctorConfig.CorregirSolicitud ->
                error("DoctorConfig $config is handled at root level, not inside DoctorFlowComponent")
        }
}
