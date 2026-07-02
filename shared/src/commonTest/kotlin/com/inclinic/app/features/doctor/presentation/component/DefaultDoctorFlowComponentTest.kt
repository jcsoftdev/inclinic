@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.navigation.DoctorConfig
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.messages.presentation.component.DoctorChatListComponent
import com.inclinic.app.features.doctor.messages.presentation.component.DoctorChatListState
import com.inclinic.app.features.doctor.modality.presentation.component.RespondModalityComponent
import com.inclinic.app.features.doctor.modality.presentation.component.RespondModalityState
import com.inclinic.app.features.doctor.negotiation.presentation.component.RespondPackageNegotiationComponent
import com.inclinic.app.features.doctor.negotiation.presentation.component.RespondPackageNegotiationState
import com.inclinic.app.features.doctor.packages.presentation.component.PackageDetailComponent
import com.inclinic.app.features.doctor.packages.presentation.component.PackageDetailState
import com.inclinic.app.features.doctor.profile.presentation.component.MySpecialtyRequestsComponent
import com.inclinic.app.features.doctor.profile.presentation.component.MySpecialtyRequestsState
import com.inclinic.app.features.doctor.reschedule.presentation.component.RescheduleQueueComponent
import com.inclinic.app.features.doctor.reschedule.presentation.component.RescheduleQueueState
import com.inclinic.app.features.doctor.reschedule_request.presentation.component.RequestRescheduleComponent
import com.inclinic.app.features.doctor.reschedule_request.presentation.component.RequestRescheduleState
import com.inclinic.app.features.doctor.settings.presentation.component.DoctorSettingsComponent
import com.inclinic.app.features.doctor.settings.presentation.component.DoctorSettingsState
import com.inclinic.app.features.doctor.notifications.presentation.component.DoctorNotificationsComponent
import com.inclinic.app.features.doctor.notifications.presentation.component.DoctorNotificationsState
import com.inclinic.app.features.doctor.packages.presentation.component.CreatePackageComponent
import com.inclinic.app.features.doctor.packages.presentation.component.CreatePackageState
import com.inclinic.app.features.doctor.packages.presentation.component.PackagesListComponent
import com.inclinic.app.features.doctor.packages.presentation.component.PackagesListState
import com.inclinic.app.features.doctor.patients.presentation.component.PatientsFilter
import com.inclinic.app.features.doctor.patients.presentation.component.PatientsListComponent
import com.inclinic.app.features.doctor.patients.presentation.component.PatientsListState
import com.inclinic.app.features.doctor.patients.presentation.component.SearchPatientComponent
import com.inclinic.app.features.doctor.patients.presentation.component.SearchPatientState
import com.inclinic.app.features.doctor.profile.presentation.component.EditSpecialtiesComponent
import com.inclinic.app.features.doctor.profile.presentation.component.EditSpecialtiesState
import com.inclinic.app.features.doctor.profile.presentation.component.IncomeComponent
import com.inclinic.app.features.doctor.profile.presentation.component.IncomeState
import com.inclinic.app.features.doctor.profile.presentation.component.MiPerfilComponent
import com.inclinic.app.features.doctor.profile.presentation.component.MiPerfilState
import com.inclinic.app.features.doctor.profile.presentation.component.RequestSpecialtyComponent
import com.inclinic.app.features.doctor.profile.presentation.component.RequestSpecialtyState
import com.inclinic.app.features.doctor.sharing.presentation.component.RequestShareComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.RequestShareState
import com.inclinic.app.features.doctor.sharing.presentation.component.ShareRequestsListComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.ShareRequestsListState
import com.inclinic.app.features.doctor.notifications.core.port.NotificationFilter
import com.inclinic.app.features.doctor.packages.presentation.component.PackageListTab
import com.inclinic.app.features.doctor.profile.presentation.component.PublicProfileComponent
import com.inclinic.app.features.doctor.profile.presentation.component.PublicProfileState
import com.inclinic.app.features.doctor.profile.presentation.component.ReviewsComponent
import com.inclinic.app.features.doctor.profile.presentation.component.ReviewsState
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.CreateTherapyOfferComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.CreateTherapyOfferState
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.TherapyOffersListComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.TherapyOffersListState
import com.inclinic.app.features.doctor.prescriptions.presentation.component.EditPrescriptionComponent
import com.inclinic.app.features.doctor.prescriptions.presentation.component.EditPrescriptionState
import com.inclinic.app.features.patient.presentation.component.DeleteAccountComponent
import com.inclinic.app.features.patient.presentation.component.DeleteAccountState
import com.inclinic.app.features.doctor.profile.presentation.component.ChangePasswordComponent
import com.inclinic.app.features.doctor.profile.presentation.component.ChangePasswordState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for [DefaultDoctorFlowComponent].
 *
 * Strict TDD: verifies tab switching, per-tab stack isolation, and initial configs.
 */
class DefaultDoctorFlowComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val componentContext = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()

    // ── Stub factories (no network, no real use cases) ────────────────────────

    private fun makeComponent(): DefaultDoctorFlowComponent = DefaultDoctorFlowComponent(
        componentContext = componentContext,
        doctorId = "doc-1",
        dispatchers = dispatchers,
        dashboardFactory = { ctx, _, out ->
            StubDoctorDashboardComponent { out(it) }
        },
        dailyScheduleFactory = { ctx, _, out ->
            StubDailyScheduleComponent { out(it) }
        },
        weeklyScheduleFactory = { ctx, _, out ->
            StubWeeklyScheduleComponent { out(it) }
        },
        appointmentDetailFactory = { ctx, apptId, out ->
            StubDoctorAppointmentDetailComponent(apptId) { out(it) }
        },
        patientDetailFactory = { ctx, patientId, out ->
            StubPatientDetailComponent(patientId) { out(it) }
        },
        medicalRecordsListFactory = { ctx, patientId, out ->
            StubMedicalRecordsListComponent(patientId) { out(it) }
        },
        createMedicalRecordFactory = { ctx, patientId, appointmentId, out ->
            StubCreateMedicalRecordComponent { out(it) }
        },
        editMedicalRecordFactory = { ctx, recordId, out ->
            StubEditMedicalRecordComponent { out(it) }
        },
        chatFactory = { ctx, apptId -> StubDoctorChatComponent(ctx, apptId) },
        scheduleConfigFactory = { ctx, _, out ->
            StubScheduleConfigComponent { out(it) }
        },
        priceConfigFactory = { ctx, _, out ->
            StubPriceConfigComponent { out(it) }
        },
        patientsListFactory = { ctx, out ->
            object : PatientsListComponent {
                override val state: Value<PatientsListState> = MutableValue(PatientsListState())
                override fun onRefresh() {}
                override fun onFilterChange(filter: PatientsFilter) {}
                override fun onPatientClicked(patientId: String) { out(PatientsListComponent.Output.NavigateToPatient(patientId)) }
                override fun onSearchClicked() { out(PatientsListComponent.Output.NavigateToSearch) }
                override fun onBack() { out(PatientsListComponent.Output.Back) }
            }
        },
        searchPatientFactory = { ctx, out ->
            object : SearchPatientComponent {
                override val state: Value<SearchPatientState> = MutableValue(SearchPatientState())
                override fun onQueryChange(query: String) {}
                override fun onSearch() {}
                override fun onPatientClicked(patientId: String) { out(SearchPatientComponent.Output.NavigateToPatient(patientId)) }
                override fun onBack() { out(SearchPatientComponent.Output.Back) }
            }
        },
        packagesListFactory = { ctx, out ->
            object : PackagesListComponent {
                override val state: Value<PackagesListState> = MutableValue(PackagesListState())
                override fun onRefresh() {}
                override fun onCreateClicked() { out(PackagesListComponent.Output.NavigateToCreate) }
                override fun onPackageClicked(id: String) { out(PackagesListComponent.Output.NavigateToDetail(id)) }
                override fun onTabSelected(tab: PackageListTab) {}
                override fun onBack() { out(PackagesListComponent.Output.Back) }
            }
        },
        createPackageFactory = { ctx, out ->
            object : CreatePackageComponent {
                override val state: Value<CreatePackageState> = MutableValue(CreatePackageState())
                override fun onPackageNameChange(value: String) {}
                override fun onSpecialtySelected(specialtyId: String) {}
                override fun onTotalSessionsChange(value: String) {}
                override fun onRegularPriceChange(value: String) {}
                override fun onPackagePriceChange(value: String) {}
                override fun onPrepaidToggle(enabled: Boolean) {}
                override fun onHomeVisitToggle(enabled: Boolean) {}
                override fun onSubmit() {}
                override fun onBack() { out(CreatePackageComponent.Output.Back) }
            }
        },
        shareRequestsListFactory = { ctx, out ->
            object : ShareRequestsListComponent {
                override val state: Value<ShareRequestsListState> = MutableValue(ShareRequestsListState())
                override fun onRefresh() {}
                override fun onSelectIncoming() {}
                override fun onSelectOutgoing() {}
                override fun onCancel(requestId: String) {}
                override fun onRequestNew() { out(ShareRequestsListComponent.Output.NavigateToRequestShare) }
                override fun onBack() { out(ShareRequestsListComponent.Output.Back) }
            }
        },
        requestShareFactory = { ctx, out ->
            object : RequestShareComponent {
                override val state: Value<RequestShareState> = MutableValue(RequestShareState())
                override fun onPatientIdChange(value: String) {}
                override fun onReasonChange(value: String) {}
                override fun onSubmit() {}
                override fun onBack() { out(RequestShareComponent.Output.Back) }
            }
        },
        chatListFactory = { ctx, out ->
            object : DoctorChatListComponent {
                override val state: Value<DoctorChatListState> = MutableValue(DoctorChatListState())
                override fun onRefresh() {}
                override fun onFilterChange(filter: com.inclinic.app.features.doctor.messages.core.port.ThreadFilter) {}
                override fun onThreadClick(threadId: String) { out(DoctorChatListComponent.Output.NavigateToConversation(threadId)) }
                override fun onBack() { out(DoctorChatListComponent.Output.Back) }
            }
        },
        miPerfilFactory = { ctx, out ->
            object : MiPerfilComponent {
                override val state: Value<MiPerfilState> = MutableValue(MiPerfilState())
                override fun onRetry() {}
                override fun onNavigateEditSpecialties() { out(MiPerfilComponent.Output.EditSpecialties) }
                override fun onNavigateRequestSpecialty() { out(MiPerfilComponent.Output.RequestSpecialty) }
                override fun onNavigateMySpecialtyRequests() { out(MiPerfilComponent.Output.MySpecialtyRequests) }
                override fun onNavigateIncome() { out(MiPerfilComponent.Output.Income) }
                override fun onNavigateReviews() { out(MiPerfilComponent.Output.Reviews) }
                override fun onNavigatePublicProfile() { out(MiPerfilComponent.Output.PublicProfile) }
                override fun onNavigateEditHorarios() { out(MiPerfilComponent.Output.EditHorarios) }
                override fun onNavigatePackages() { out(MiPerfilComponent.Output.Packages) }
                override fun onNavigateSharing() { out(MiPerfilComponent.Output.Sharing) }
                override fun onNavigateSettings() { out(MiPerfilComponent.Output.Settings) }
                override fun onNavigateTherapyOffers() { out(MiPerfilComponent.Output.TherapyOffers) }
                override fun onNavigateNoShowQueue() { out(MiPerfilComponent.Output.NoShowQueue) }
                override fun onNavigatePendingClosure() { out(MiPerfilComponent.Output.PendingClosure) }
                override fun onNavigateChangePassword() { out(MiPerfilComponent.Output.ChangePassword) }
                override fun onLogout() { out(MiPerfilComponent.Output.Logout) }
            }
        },
        editSpecialtiesFactory = { ctx, out ->
            object : EditSpecialtiesComponent {
                override val state: Value<EditSpecialtiesState> = MutableValue(EditSpecialtiesState())
                override fun onToggleSpecialty(specialtyId: String) {}
                override fun onSave() {}
                override fun onBack() { out(EditSpecialtiesComponent.Output.Back) }
                override fun onNavigateToRequestSpecialty() { out(EditSpecialtiesComponent.Output.NavigateToRequestSpecialty) }
            }
        },
        requestSpecialtyFactory = { ctx, out ->
            object : RequestSpecialtyComponent {
                override val state: Value<RequestSpecialtyState> = MutableValue(RequestSpecialtyState())
                override fun onSpecialtyNameChange(name: String) {}
                override fun onAddDocumentUrl(url: String) {}
                override fun onRemoveDocumentUrl(url: String) {}
                override fun onCommentChange(comment: String) {}
                override fun onSubmit() {}
                override fun onBack() { out(RequestSpecialtyComponent.Output.Back) }
                override fun onPickCertification(file: com.inclinic.app.core.platform.PickedFile) {}
                override fun onPickDiploma(file: com.inclinic.app.core.platform.PickedFile) {}
            }
        },
        incomeFactory = { ctx, out ->
            object : IncomeComponent {
                override val state: Value<IncomeState> = MutableValue(IncomeState())
                override fun onRetry() {}
                override fun onBack() { out(IncomeComponent.Output.Back) }
            }
        },
        notificationsFactory = { ctx, out ->
            object : DoctorNotificationsComponent {
                override val state: Value<DoctorNotificationsState> = MutableValue(DoctorNotificationsState())
                override fun onRefresh() {}
                override fun onFilterChange(filter: NotificationFilter) {}
                override fun onMarkRead(id: String) {}
                override fun onMarkAllRead() {}
                override fun onBack() { out(DoctorNotificationsComponent.Output.Back) }
            }
        },
        settingsFactory = { ctx, out ->
            object : DoctorSettingsComponent {
                override val state: Value<DoctorSettingsState> = MutableValue(DoctorSettingsState())
                override fun onBack() { out(DoctorSettingsComponent.Output.Back) }
                override fun onLogOut() { out(DoctorSettingsComponent.Output.LoggedOut) }
                override fun onDeleteAccount() { out(DoctorSettingsComponent.Output.NavigateToDeleteAccount) }
                override fun onToggleNewAppointments(enabled: Boolean) {}
                override fun onToggleChatMessages(enabled: Boolean) {}
                override fun onToggleAppointmentReminders(enabled: Boolean) {}
                override fun onToggleTwoFactor(enabled: Boolean) {}
                override fun onConnectMercadoPago() {}
                override fun onMercadoPagoConnectUrlConsumed() {}
                override fun onDisconnectMercadoPago() {}
            }
        },
        rescheduleQueueFactory = { ctx, out ->
            object : RescheduleQueueComponent {
                override val state: Value<RescheduleQueueState> = MutableValue(RescheduleQueueState())
                override fun onRetry() {}
                override fun onApprove(requestId: String) {}
                override fun onReject(requestId: String) {}
                override fun onBack() { out(RescheduleQueueComponent.Output.Back) }
            }
        },
        requestRescheduleFactory = { ctx, appointmentId, out ->
            object : RequestRescheduleComponent {
                override val state: Value<RequestRescheduleState> = MutableValue(RequestRescheduleState())
                override fun onSlotChange(value: String) {}
                override fun onMessageChange(value: String) {}
                override fun onSubmit() {}
                override fun onBack() { out(RequestRescheduleComponent.Output.Back) }
            }
        },
        respondModalityFactory = { ctx, requestId, out ->
            object : RespondModalityComponent {
                override val state: Value<RespondModalityState> = MutableValue(RespondModalityState())
                override fun onRetry() {}
                override fun onPriceChange(value: String) {}
                override fun onApprove() {}
                override fun onReject() {}
                override fun onBack() { out(RespondModalityComponent.Output.Back) }
            }
        },
        respondPackageNegotiationFactory = { ctx, negotiationId, out ->
            object : RespondPackageNegotiationComponent {
                override val state: Value<RespondPackageNegotiationState> = MutableValue(RespondPackageNegotiationState())
                override fun onRetry() {}
                override fun onAccept() {}
                override fun onReject() {}
                override fun onCounterPriceChange(value: String) {}
                override fun onSubmitCounter() {}
                override fun onBack() { out(RespondPackageNegotiationComponent.Output.Back) }
            }
        },
        packageDetailFactory = { ctx, packageId, out ->
            object : PackageDetailComponent {
                override val state: Value<PackageDetailState> = MutableValue(PackageDetailState())
                override fun onRetry() {}
                override fun onCancel() { out(PackageDetailComponent.Output.Cancelled) }
                override fun onBack() { out(PackageDetailComponent.Output.Back) }
            }
        },
        mySpecialtyRequestsFactory = { ctx, out ->
            object : MySpecialtyRequestsComponent {
                override val state: Value<MySpecialtyRequestsState> = MutableValue(MySpecialtyRequestsState())
                override fun onRetry() {}
                override fun onRequestNew() { out(MySpecialtyRequestsComponent.Output.RequestNew) }
                override fun onBack() { out(MySpecialtyRequestsComponent.Output.Back) }
            }
        },
        reviewsFactory = { ctx, out ->
            object : ReviewsComponent {
                override val state: Value<ReviewsState> = MutableValue(ReviewsState())
                override fun onRetry() {}
                override fun onBack() { out(ReviewsComponent.Output.Back) }
            }
        },
        publicProfileFactory = { ctx, onBack ->
            object : PublicProfileComponent {
                override val state: Value<PublicProfileState> = MutableValue(PublicProfileState())
                override fun onBack() { onBack() }
            }
        },
        therapyOffersListFactory = { ctx, out ->
            object : TherapyOffersListComponent {
                override val state: Value<TherapyOffersListState> = MutableValue(TherapyOffersListState())
                override fun onRefresh() {}
                override fun onCreateClicked() { out(TherapyOffersListComponent.Output.NavigateToCreate) }
                override fun onBack() { out(TherapyOffersListComponent.Output.Back) }
            }
        },
        createTherapyOfferFactory = { ctx, out ->
            object : CreateTherapyOfferComponent {
                override val state: Value<CreateTherapyOfferState> = MutableValue(CreateTherapyOfferState())
                override fun onTitleChange(v: String) {}
                override fun onSpecialtySelected(id: String) {}
                override fun onTotalSessionsChange(v: String) {}
                override fun onPricePerSessionChange(v: String) {}
                override fun onMinPriceChange(v: String) {}
                override fun onDescriptionChange(v: String) {}
                override fun onActiveToggle(v: Boolean) {}
                override fun onSubmit() {}
                override fun onBack() { out(CreateTherapyOfferComponent.Output.Back) }
            }
        },
        editPrescriptionFactory = { ctx, prescriptionId, out ->
            object : EditPrescriptionComponent {
                override val state: Value<EditPrescriptionState> = MutableValue(EditPrescriptionState())
                override fun onMedicationChange(v: String) {}
                override fun onDosageChange(v: String) {}
                override fun onFrequencyChange(v: String) {}
                override fun onDurationChange(v: String) {}
                override fun onInstructionsChange(v: String) {}
                override fun onUpdateItemName(index: Int, v: String) {}
                override fun onUpdateItemDose(index: Int, v: String) {}
                override fun onUpdateItemFrequency(index: Int, v: String) {}
                override fun onUpdateItemDuration(index: Int, v: String) {}
                override fun onUpdateItemNotes(index: Int, v: String) {}
                override fun onAddItem() {}
                override fun onRemoveItem(index: Int) {}
                override fun onSubmit() {}
                override fun onBack() { out(EditPrescriptionComponent.Output.Back) }
            }
        },
        deleteAccountFactory = { ctx, out ->
            object : DeleteAccountComponent {
                override val state: Value<DeleteAccountState> = MutableValue(DeleteAccountState())
                override fun onPasswordChange(value: String) {}
                override fun onConfirm() {}
                override fun onBack() { out(DeleteAccountComponent.Output.Back) }
                override fun onDismissError() {}
            }
        },
        noShowQueueFactory = { ctx, out ->
            object : com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowQueueComponent {
                override val state: Value<com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowQueueState> =
                    MutableValue(com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowQueueState())
                override fun onTabSelected(tab: com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowTab) {}
                override fun onRetry() {}
                override fun onBack() { out(com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowQueueComponent.Output.Back) }
            }
        },
        pendingClosureQueueFactory = { ctx, out ->
            object : com.inclinic.app.features.doctor.pending_closure.presentation.component.PendingClosureQueueComponent {
                override val state: Value<com.inclinic.app.features.doctor.pending_closure.presentation.component.PendingClosureQueueState> =
                    MutableValue(com.inclinic.app.features.doctor.pending_closure.presentation.component.PendingClosureQueueState())
                override fun onAppointmentTapped(appointmentId: String) {
                    out(com.inclinic.app.features.doctor.pending_closure.presentation.component.PendingClosureQueueComponent.Output.NavigateToDetail(appointmentId))
                }
                override fun onRetry() {}
                override fun onBack() { out(com.inclinic.app.features.doctor.pending_closure.presentation.component.PendingClosureQueueComponent.Output.Back) }
            }
        },
        changePasswordFactory = { ctx, out ->
            object : ChangePasswordComponent {
                override val state: Value<ChangePasswordState> = MutableValue(ChangePasswordState())
                override fun onCurrentPasswordChange(value: String) {}
                override fun onNewPasswordChange(value: String) {}
                override fun onConfirmNewPasswordChange(value: String) {}
                override fun onSubmit() {}
                override fun onBack() { out(ChangePasswordComponent.Output.Back) }
            }
        },
        onOutput = {},
    )

    // ── Tab tests ─────────────────────────────────────────────────────────────

    @Test
    fun initial_tab_is_Inicio() = runTest {
        val component = makeComponent()
        assertEquals(DoctorTab.Inicio, component.currentTab.value)
    }

    @Test
    fun onTabSelected_switches_to_Agenda() = runTest {
        val component = makeComponent()
        component.onTabSelected(DoctorTab.Agenda)
        assertEquals(DoctorTab.Agenda, component.currentTab.value)
    }

    @Test
    fun onTabSelected_switches_to_Pacientes() = runTest {
        val component = makeComponent()
        component.onTabSelected(DoctorTab.Pacientes)
        assertEquals(DoctorTab.Pacientes, component.currentTab.value)
    }

    @Test
    fun onTabSelected_switches_to_Mensajes() = runTest {
        val component = makeComponent()
        component.onTabSelected(DoctorTab.Mensajes)
        assertEquals(DoctorTab.Mensajes, component.currentTab.value)
    }

    @Test
    fun onTabSelected_switches_to_Perfil() = runTest {
        val component = makeComponent()
        component.onTabSelected(DoctorTab.Perfil)
        assertEquals(DoctorTab.Perfil, component.currentTab.value)
    }

    // ── Per-tab initial config tests ──────────────────────────────────────────

    @Test
    fun inicio_stack_initial_config_is_Dashboard() = runTest {
        val component = makeComponent()
        assertEquals(
            DoctorConfig.Dashboard,
            component.iniciStack.value.active.configuration,
        )
    }

    @Test
    fun agenda_stack_initial_config_is_Schedule() = runTest {
        val component = makeComponent()
        assertEquals(
            DoctorConfig.Schedule,
            component.agendaStack.value.active.configuration,
        )
    }

    @Test
    fun pacientes_stack_initial_config_is_Patients() = runTest {
        val component = makeComponent()
        assertEquals(
            DoctorConfig.Patients,
            component.pacientesStack.value.active.configuration,
        )
    }

    @Test
    fun mensajes_stack_initial_config_is_Messages() = runTest {
        val component = makeComponent()
        assertEquals(
            DoctorConfig.Messages,
            component.mensajesStack.value.active.configuration,
        )
    }

    @Test
    fun perfil_stack_initial_config_is_Profile() = runTest {
        val component = makeComponent()
        assertEquals(
            DoctorConfig.Profile,
            component.perfilStack.value.active.configuration,
        )
    }

    // ── Stack isolation ───────────────────────────────────────────────────────

    @Test
    fun navigateTo_PatientDetail_within_Pacientes_pushes_only_to_Pacientes_stack() = runTest {
        val component = makeComponent()
        component.onTabSelected(DoctorTab.Pacientes)

        component.navigateTo(DoctorConfig.PatientDetail("p-42"))

        // Pacientes stack has 2 entries: Patients + PatientDetail
        assertEquals(2, component.pacientesStack.value.backStack.size + 1)
        assertEquals(
            DoctorConfig.PatientDetail("p-42"),
            component.pacientesStack.value.active.configuration,
        )
        // Other stacks are untouched
        assertEquals(1, component.iniciStack.value.items.size)
        assertEquals(1, component.agendaStack.value.items.size)
        assertEquals(1, component.mensajesStack.value.items.size)
        assertEquals(1, component.perfilStack.value.items.size)
    }

    @Test
    fun appointmentDetail_createMedicalRecord_pushes_editor_with_real_appointmentId() = runTest {
        val component = makeComponent()
        component.onTabSelected(DoctorTab.Agenda)
        component.navigateTo(DoctorConfig.AppointmentDetail("appt-77"))

        val child = component.agendaStack.value.active.instance
        assertIs<DoctorFlowComponent.Child.AppointmentDetail>(child)
        // Stub emits NavigateToCreateMedicalRecord(appointmentId = "appt-77", patientId = "pat-1").
        child.component.onCreateMedicalRecord()

        // Routes cross-tab to Pacientes with the real appointmentId threaded through.
        assertEquals(DoctorTab.Pacientes, component.currentTab.value)
        assertEquals(
            DoctorConfig.MedicalRecordEditor(patientId = "pat-1", appointmentId = "appt-77"),
            component.pacientesStack.value.active.configuration,
        )
    }

    @Test
    fun inicio_stack_child_is_Dashboard() = runTest {
        val component = makeComponent()
        assertIs<DoctorFlowComponent.Child.Dashboard>(component.iniciStack.value.active.instance)
    }

    @Test
    fun agenda_stack_child_is_WeeklySchedule() = runTest {
        val component = makeComponent()
        assertIs<DoctorFlowComponent.Child.WeeklySchedule>(component.agendaStack.value.active.instance)
    }

    @Test
    fun pacientes_stack_child_is_PatientsList() = runTest {
        val component = makeComponent()
        assertIs<DoctorFlowComponent.Child.PatientsList>(component.pacientesStack.value.active.instance)
    }

    @Test
    fun mensajes_stack_child_is_ChatList() = runTest {
        val component = makeComponent()
        assertIs<DoctorFlowComponent.Child.ChatList>(component.mensajesStack.value.active.instance)
    }

    @Test
    fun perfil_stack_child_is_MiPerfil() = runTest {
        val component = makeComponent()
        assertIs<DoctorFlowComponent.Child.MiPerfil>(component.perfilStack.value.active.instance)
    }
}
