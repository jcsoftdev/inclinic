@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.AppNotification
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.core.model.DisputeReason
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.navigation.PatientConfig
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.assistant.presentation.component.AssistantChatComponent
import com.inclinic.app.features.patient.assistant.presentation.component.AssistantChatState
import com.inclinic.app.features.patient.moderation.presentation.component.BlockUserComponent
import com.inclinic.app.features.patient.moderation.presentation.component.BlockUserState
import com.inclinic.app.features.patient.moderation.presentation.component.ReportUserComponent
import com.inclinic.app.features.patient.moderation.presentation.component.ReportUserState
import com.inclinic.app.ui.atoms.PatientTab
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// ---------------------------------------------------------------------------
// Stub components — each implements ONLY the declared interface members
// ---------------------------------------------------------------------------

private class StubPatientHomeComponent : PatientHomeComponent {
    override val state: Value<PatientHomeState> = MutableValue(PatientHomeState())
    override fun onRefresh() = Unit
    override fun onSearchTapped() = Unit
    override fun onDoctorTapped(doctorId: String) = Unit
    override fun onErrorDismissed() = Unit
    override fun onAssistantChatTapped() = Unit
    override fun onAppointmentsTapped() = Unit
    override fun onAppointmentDetailTapped(appointmentId: String) = Unit
    override fun onProfileTapped() = Unit
    override fun onPackagesTapped() = Unit
    override fun onPremiumTapped() = Unit
    override fun onNavigateToHistoryAccess() = Unit
}

private class StubDoctorSearchComponent : DoctorSearchComponent {
    override val state: Value<DoctorSearchState> = MutableValue(DoctorSearchState())
    override fun onQueryChange(query: String) = Unit
    override fun onSpecialtyChange(specialty: String?) = Unit
    override fun onSortChange(sort: DoctorSortOrder) = Unit
    override fun onMinPriceChange(price: Double?) = Unit
    override fun onMaxPriceChange(price: Double?) = Unit
    override fun onApplyFilters(
        minPrice: Double?,
        maxPrice: Double?,
        minRating: Double?,
        offersTelemedicine: Boolean?,
        offersHomeVisit: Boolean?,
        sortOrder: DoctorSortOrder,
    ) = Unit
    override fun onResetFilters() = Unit
    override fun onLoadMore() = Unit
    override fun onDoctorTapped(doctorId: String) = Unit
    override fun onErrorDismissed() = Unit
}

private class StubDoctorProfileComponent : DoctorProfileComponent {
    override val state: Value<DoctorProfileState> = MutableValue(DoctorProfileState())
    override fun onLoadMoreReviews() = Unit
    override fun onBookTapped() = Unit
    override fun onBack() = Unit
    override fun onErrorDismissed() = Unit
}

private class StubConsultTypeComponent : ConsultTypeComponent {
    override val state: Value<ConsultTypeState> = MutableValue(ConsultTypeState())
    override fun onTypeSelected(type: ConsultType) = Unit
    override fun onContinue() = Unit
    override fun onBack() = Unit
}

private class StubAvailabilityComponent : AvailabilityCalendarComponent {
    override val state: Value<AvailabilityCalendarState> = MutableValue(AvailabilityCalendarState())
    override fun onDateSelected(date: LocalDate) = Unit
    override fun onSlotSelected(slot: AvailabilitySlot) = Unit
    override fun onPrevMonth() = Unit
    override fun onNextMonth() = Unit
    override fun onContinue() = Unit
    override fun onBack() = Unit
}

private class StubBookingComponent : BookingComponent {
    override val state: Value<BookingState> = MutableValue(BookingState())
    override fun onVisitTypeChange(visitType: VisitType) = Unit
    override fun onNotesChange(notes: String) = Unit
    override fun onConfirm() = Unit
    override fun onSkipPayment() = Unit
    override fun onBack() = Unit
}

private class StubPaymentComponent : PaymentComponent {
    override val state: Value<PaymentState> = MutableValue(PaymentState())
    override fun onCardNumberChange(value: String) = Unit
    override fun onExpiryChange(value: String) = Unit
    override fun onCvvChange(value: String) = Unit
    override fun onCardholderNameChange(value: String) = Unit
    override fun onDocTypeChange(value: String) = Unit
    override fun onDocNumberChange(value: String) = Unit
    override fun onSelectMethod(method: PaymentMethodChoice) = Unit
    override fun onYapePhoneChange(value: String) = Unit
    override fun onYapeOtpChange(value: String) = Unit
    override fun onSubmit() = Unit
    override fun onBack() = Unit
    override fun onErrorDismissed() = Unit
    override fun onPayNow() = Unit
    override fun onCancelReservation() = Unit
    override fun onGoToAppointments() = Unit
    override fun onRetryPayment() = Unit
    override fun onChangeCard() = Unit
}

private class StubPatientAppointmentsListComponent : PatientAppointmentsListComponent {
    override val state: Value<PatientAppointmentsListState> = MutableValue(PatientAppointmentsListState())
    override fun onTabChange(tab: AppointmentsTab) = Unit
    override fun onLoadMore() = Unit
    override fun onRefresh() = Unit
    override fun onAppointmentTapped(appointmentId: String) = Unit
    override fun onPayNow(appointmentId: String) = Unit
    override fun onCancel(appointmentId: String) = Unit
    override fun onReschedule(appointmentId: String) = Unit
    override fun onRespondReschedule(appointmentId: String) = Unit
    override fun onSearchDoctors() = Unit
    override fun onErrorDismissed() = Unit
}

private class StubAppointmentDetailComponent : AppointmentDetailComponent {
    override val state: Value<AppointmentDetailState> = MutableValue(AppointmentDetailState())
    override fun onPayNow() = Unit
    override fun onCancel() = Unit
    override fun onReschedule() = Unit
    override fun onChat() = Unit
    override fun onBack() = Unit
    override fun onErrorDismissed() = Unit
}

private class StubCancelAppointmentComponent : CancelAppointmentComponent {
    override val state: Value<CancelAppointmentState> = MutableValue(CancelAppointmentState())
    override fun onReasonChanged(reason: String) = Unit
    override fun onConfirmCancel() = Unit
    override fun onBack() = Unit
}

private class StubChatComponent : ChatComponent {
    override val state: Value<ChatState> = MutableValue(ChatState())
    override fun onInputChange(text: String) = Unit
    override fun onSend() = Unit
    override fun onBack() = Unit
    override fun onAttachmentPicked(file: com.inclinic.app.core.platform.PickedFile) = Unit
    override fun onRemovePendingAttachment(index: Int) = Unit
    override fun onReportUser(userId: String, userName: String) = Unit
    override fun onBlockUser(userId: String, userName: String) = Unit
}

private class StubRescheduleResponseComponent : RescheduleResponseComponent {
    override val state: Value<RescheduleResponseState> = MutableValue(RescheduleResponseState())
    override fun onAccept() = Unit
    override fun onReject() = Unit
    override fun onBack() = Unit
}

private class StubMedicalHistoryComponent : MedicalHistoryComponent {
    override val state: Value<MedicalHistoryState> = MutableValue(MedicalHistoryState())
    override fun onRefresh() = Unit
    override fun onBack() = Unit
    override fun onNavigateToClinicalProfile() = Unit
}

private class StubFlowPatientProfileComponent : PatientProfileComponent {
    override val state: Value<PatientProfileState> = MutableValue(PatientProfileState())
    override fun onNameChange(name: String) = Unit
    override fun onPhoneChange(phone: String) = Unit
    override fun onDateOfBirthChange(dob: String) = Unit
    override fun onToggleEdit() = Unit
    override fun onSave() = Unit
    override fun onBack() = Unit
    override fun onErrorDismissed() = Unit
}

private class StubMembershipComponent : MembershipComponent {
    override val state: Value<MembershipState> = MutableValue(MembershipState())
    override fun onUpgradeTapped() = Unit
    override fun onDismissCheckout() = Unit
    override fun onErrorDismissed() = Unit
    override fun onBack() = Unit
    override fun onCardNumberChange(value: String) = Unit
    override fun onExpiryChange(value: String) = Unit
    override fun onCvvChange(value: String) = Unit
    override fun onCardholderNameChange(value: String) = Unit
    override fun onDocTypeChange(value: String) = Unit
    override fun onDocNumberChange(value: String) = Unit
    override fun onSubmitPurchase() = Unit
}

private class StubProfileOverviewComponent : ProfileOverviewComponent {
    override val state: Value<ProfileOverviewState> = MutableValue(ProfileOverviewState())
    override fun onEditProfile() = Unit
    override fun onSettings() = Unit
    override fun onLogout() = Unit
    override fun onErrorDismissed() = Unit
}

private class StubClinicalProfileComponent : ClinicalProfileComponent {
    override val state: Value<ClinicalProfileState> = MutableValue(ClinicalProfileState())
    override fun onBack() = Unit
    override fun onNavigateToDeleteAccount() = Unit
    override fun onToggleEdit() = Unit
    override fun onBloodTypeChange(value: String) = Unit
    override fun onHeightCmChange(value: String) = Unit
    override fun onWeightKgChange(value: String) = Unit
    override fun onAllergiesChange(value: String) = Unit
    override fun onConditionsChange(value: String) = Unit
    override fun onEmergencyNameChange(value: String) = Unit
    override fun onEmergencyPhoneChange(value: String) = Unit
    override fun onEmergencyRelationChange(value: String) = Unit
    override fun onSave() = Unit
    override fun onDismissError() = Unit
}

private class StubDeleteAccountComponent : DeleteAccountComponent {
    override val state: Value<DeleteAccountState> = MutableValue(DeleteAccountState())
    override fun onPasswordChange(value: String) = Unit
    override fun onConfirm() = Unit
    override fun onBack() = Unit
    override fun onDismissError() = Unit
}

private class StubChangePasswordComponent : ChangePasswordComponent {
    override val state: Value<ChangePasswordState> = MutableValue(ChangePasswordState())
    override fun onCurrentPasswordChange(value: String) = Unit
    override fun onNewPasswordChange(value: String) = Unit
    override fun onConfirmNewPasswordChange(value: String) = Unit
    override fun onSubmit() = Unit
    override fun onBack() = Unit
}

private class StubAssistantChatComponent : AssistantChatComponent {
    override val state: Value<AssistantChatState> = MutableValue(AssistantChatState())
    override fun onInputChange(text: String) = Unit
    override fun onSend() = Unit
    override fun onStop() = Unit
    override fun onDoctorCardReserve(doctorName: String) = Unit
    override fun onErrorDismissed() = Unit
    override fun onRetry() = Unit
    override fun onDisclaimerDismissed() = Unit
    override fun onNavigateToPayment(appointmentId: String) = Unit
}

private class StubRescheduleAppointmentComponent : RescheduleAppointmentComponent {
    override val state: Value<RescheduleAppointmentState> = MutableValue(RescheduleAppointmentState())
    override fun onDateSelected(date: LocalDate) = Unit
    override fun onSlotSelected(slot: AvailabilitySlot) = Unit
    override fun onPrevMonth() = Unit
    override fun onNextMonth() = Unit
    override fun onConfirmReschedule() = Unit
    override fun onBack() = Unit
}

private class StubChangeVisitTypeComponent : ChangeVisitTypeComponent {
    override val state: Value<ChangeVisitTypeState> = MutableValue(ChangeVisitTypeState())
    override fun onNewVisitTypeSelected(type: VisitType) = Unit
    override fun onAddressChanged(address: String) = Unit
    override fun onReasonChanged(reason: String) = Unit
    override fun onSubmit() = Unit
    override fun onBack() = Unit
}

private class StubDisputeAppointmentComponent : DisputeAppointmentComponent {
    override val state: Value<DisputeAppointmentState> = MutableValue(DisputeAppointmentState())
    override fun onReasonSelected(reason: DisputeReason) = Unit
    override fun onDetailsChanged(details: String) = Unit
    override fun onSubmit() = Unit
    override fun onBack() = Unit
}

private class StubConfirmRatingComponent : ConfirmRatingComponent {
    override val state: Value<ConfirmRatingState> = MutableValue(ConfirmRatingState())
    override fun onPunctualityChanged(stars: Int) = Unit
    override fun onProfessionalismChanged(stars: Int) = Unit
    override fun onEmpathyChanged(stars: Int) = Unit
    override fun onCommentChanged(comment: String) = Unit
    override fun onConfirm() = Unit
    override fun onDispute() = Unit
    override fun onBack() = Unit
}

private class StubMessagesListComponent : MessagesListComponent {
    override val state: Value<MessagesListState> = MutableValue(MessagesListState())
    override fun onRefresh() = Unit
    override fun onConversationClick(conversationId: String) = Unit
}

private class StubNotificationsComponent : NotificationsComponent {
    override val state: Value<NotificationsState> = MutableValue(NotificationsState())
    override fun onFilterChange(filter: NotificationFilter) = Unit
    override fun onMarkAllRead() = Unit
    override fun onNotificationClick(notification: AppNotification) = Unit
    override fun onBack() = Unit
}

private class StubFlowSettingsComponent : SettingsComponent {
    override val state: Value<SettingsState> = MutableValue(SettingsState())
    override fun onPushToggle(enabled: Boolean) = Unit
    override fun onAnalyticsToggle(enabled: Boolean) = Unit
    override fun onChangePassword() = Unit
    override fun onSubscribe() = Unit
    override fun onBack() = Unit
    override fun onDeleteAccount() = Unit
}

private class StubMedicalRecordDetailComponent : MedicalRecordDetailComponent {
    override val state: Value<MedicalRecordDetailState> = MutableValue(MedicalRecordDetailState())
    override fun onBack() = Unit
    override fun onNavigateToMembership() = Unit
}

private class StubPrescriptionDetailComponent : PrescriptionDetailComponent {
    override val state: Value<PrescriptionDetailState> = MutableValue(PrescriptionDetailState())
    override fun onBack() = Unit
    override fun onShare() = Unit
    override fun pdfUrl(): String = ""
    override fun onDownloadPdf() = Unit
    override fun onPdfConsumed() = Unit
}

private class StubHistoryAccessLogsComponent : HistoryAccessLogsComponent {
    override val state: Value<HistoryAccessLogsState> = MutableValue(HistoryAccessLogsState())
    override fun onRefresh() = Unit
    override fun onBack() = Unit
    override fun onManageAccess() = Unit
    override fun onLogClick(entry: com.inclinic.app.core.model.HistoryAccessLog) = Unit
}

private class StubHistoryAccessLogDetailComponent(
    override val entry: com.inclinic.app.core.model.HistoryAccessLog,
) : HistoryAccessLogDetailComponent {
    override fun onBack() = Unit
}

private class StubShareRequestsComponent : ShareRequestsComponent {
    override val state: Value<ShareRequestsState> = MutableValue(ShareRequestsState())
    override fun onTabSelected(tab: ShareRequestTab) = Unit
    override fun onRefresh() = Unit
    override fun onBack() = Unit
    override fun onRequestSelected(requestId: String) = Unit
    override fun onInlineApprove(requestId: String) = Unit
    override fun onInlineReject(requestId: String) = Unit
}

private class StubActiveAccessesComponent : ActiveAccessesComponent {
    override val state: Value<ActiveAccessesState> = MutableValue(ActiveAccessesState())
    override fun onRefresh() = Unit
    override fun onBack() = Unit
    override fun onRevoke(requestId: String) = Unit
}

private class StubApproveShareRequestComponent : ApproveShareRequestComponent {
    override val state: Value<ApproveShareRequestState> = MutableValue(ApproveShareRequestState())
    override fun onDurationSelected(days: Int) = Unit
    override fun onApprove() = Unit
    override fun onReject() = Unit
    override fun onClose() = Unit
}

private class StubSymptomInputComponent : SymptomInputComponent {
    override val state: Value<SymptomInputState> = MutableValue(SymptomInputState())
    override fun onTextChanged(text: String) = Unit
    override fun onChipToggle(chip: String) = Unit
    override fun onSearch() = Unit
    override fun onBack() = Unit
}

private class StubSymptomResultsComponent : SymptomResultsComponent {
    override val state: Value<SymptomResultsState> = MutableValue(SymptomResultsState())
    override fun onEditSymptoms() = Unit
    override fun onViewDoctorProfile(doctorId: String) = Unit
    override fun onBack() = Unit
    override fun onRetry() = Unit
}

private class StubTherapyPackagesListComponent : TherapyPackagesListComponent {
    override val state: Value<TherapyPackagesListState> = MutableValue(TherapyPackagesListState())
    override fun onTabChange(tab: PackagesTab) = Unit
    override fun onPackageTapped(packageId: String) = Unit
    override fun onBuyPackage() = Unit
    override fun onBack() = Unit
    override fun onErrorDismissed() = Unit
}

private class StubTherapyPackageDetailComponent : TherapyPackageDetailComponent {
    override val state: Value<TherapyPackageDetailState> = MutableValue(TherapyPackageDetailState())
    override fun onTabChange(tab: SessionsTab) = Unit
    override fun onScheduleNextSession() = Unit
    override fun onBack() = Unit
    override fun onErrorDismissed() = Unit
}

private class StubTherapyOffersComponent : TherapyOffersComponent {
    override val state: Value<TherapyOffersState> = MutableValue(TherapyOffersState())
    override fun onOfferTapped(offerId: String) = Unit
    override fun onBuy(offerId: String) = Unit
    override fun onNegotiate(offerId: String) = Unit
    override fun onNegotiationTapped(negotiationId: String) = Unit
    override fun onBack() = Unit
    override fun onErrorDismissed() = Unit
}

private class StubNegotiationComponent : NegotiationComponent {
    override val state: Value<NegotiationState> = MutableValue(NegotiationState())
    override fun onProposedPriceChange(value: String) = Unit
    override fun onProposedSessionsChange(value: String) = Unit
    override fun onMessageChange(text: String) = Unit
    override fun onSubmitProposal() = Unit
    override fun onAccept() = Unit
    override fun onReject() = Unit
    override fun onPay() = Unit
    override fun onBack() = Unit
    override fun onErrorDismissed() = Unit
    override fun onReportUser(userId: String, userName: String) = Unit
    override fun onBlockUser(userId: String, userName: String) = Unit
}

private class StubReportUserComponent : ReportUserComponent {
    override val state: Value<ReportUserState> = MutableValue(ReportUserState())
    override fun onReasonChanged(reason: String) = Unit
    override fun onCategorySelected(category: com.inclinic.app.features.patient.moderation.core.model.ReportCategory) = Unit
    override fun onSubmit() = Unit
    override fun onBack() = Unit
}

private class StubBlockUserComponent : BlockUserComponent {
    override val state: Value<BlockUserState> = MutableValue(BlockUserState())
    override fun onReasonChanged(reason: String) = Unit
    override fun onConfirm() = Unit
    override fun onCancel() = Unit
}

// ---------------------------------------------------------------------------
// Test class
// ---------------------------------------------------------------------------

class DefaultPatientFlowComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        outputs: MutableList<PatientFlowComponent.Output> = mutableListOf(),
        onSettingsOutput: ((SettingsComponent.Output) -> Unit) -> Unit = {},
    ): DefaultPatientFlowComponent = DefaultPatientFlowComponent(
        componentContext = ctx,
        patientId = "pat-1",
        dispatchers = dispatchers,
        homeFactory = { _, _, _ -> StubPatientHomeComponent() },
        searchFactory = { _, _ -> StubDoctorSearchComponent() },
        doctorProfileFactory = { _, _, _ -> StubDoctorProfileComponent() },
        consultTypeFactory = { _, _, _ -> StubConsultTypeComponent() },
        availabilityFactory = { _, _, _, _ -> StubAvailabilityComponent() },
        bookingFactory = { _, _, _, _, _, _, _ -> StubBookingComponent() },
        paymentFactory = { _, _, _, _ -> StubPaymentComponent() },
        appointmentsFactory = { _, _, _ -> StubPatientAppointmentsListComponent() },
        appointmentDetailFactory = { _, _, _ -> StubAppointmentDetailComponent() },
        cancelAppointmentFactory = { _, _, _ -> StubCancelAppointmentComponent() },
        chatFactory = { _, _, _, _ -> StubChatComponent() },
        rescheduleResponseFactory = { _, _, _ -> StubRescheduleResponseComponent() },
        medicalHistoryFactory = { _, _, _ -> StubMedicalHistoryComponent() },
        profileOverviewFactory = { _, _, _ -> StubProfileOverviewComponent() },
        membershipFactory = { _, _ -> StubMembershipComponent() },
        profileFactory = { _, _, _ -> StubFlowPatientProfileComponent() },
        assistantChatComponentFactory = { _, _ -> StubAssistantChatComponent() },
        rescheduleAppointmentFactory = { _, _, _ -> StubRescheduleAppointmentComponent() },
        changeVisitTypeFactory = { _, _, _ -> StubChangeVisitTypeComponent() },
        disputeAppointmentFactory = { _, _, _ -> StubDisputeAppointmentComponent() },
        confirmRatingFactory = { _, _, _ -> StubConfirmRatingComponent() },
        messagesListFactory = { _, _ -> StubMessagesListComponent() },
        notificationsFactory = { _, _ -> StubNotificationsComponent() },
        settingsFactory = { _, _, output -> onSettingsOutput(output); StubFlowSettingsComponent() },
        medicalRecordDetailFactory = { _, _, _ -> StubMedicalRecordDetailComponent() },
        prescriptionDetailFactory = { _, _, _ -> StubPrescriptionDetailComponent() },
        historyAccessLogsFactory = { _, _ -> StubHistoryAccessLogsComponent() },
        historyAccessLogDetailFactory = { _, entry, _ -> StubHistoryAccessLogDetailComponent(entry) },
        shareRequestsFactory = { _, _ -> StubShareRequestsComponent() },
        approveShareRequestFactory = { _, _, _ -> StubApproveShareRequestComponent() },
        symptomInputFactory = { _, _ -> StubSymptomInputComponent() },
        symptomResultsFactory = { _, _, _ -> StubSymptomResultsComponent() },
        therapyPackagesFactory = { _, _, _ -> StubTherapyPackagesListComponent() },
        therapyPackageDetailFactory = { _, _, _ -> StubTherapyPackageDetailComponent() },
        therapyOffersFactory = { _, _ -> StubTherapyOffersComponent() },
        negotiationFactory = { _, _, _, _ -> StubNegotiationComponent() },
        reportUserFactory = { _, _, _, _ -> StubReportUserComponent() },
        blockUserFactory = { _, _, _, _ -> StubBlockUserComponent() },
        clinicalProfileFactory = { _, _ -> StubClinicalProfileComponent() },
        changePasswordFactory = { _, _ -> StubChangePasswordComponent() },
        deleteAccountFactory = { _, _ -> StubDeleteAccountComponent() },
        activeAccessesFactory = { _, _ -> StubActiveAccessesComponent() },
        onOutput = outputs::add,
    )

    @Test
    fun initial_stack_has_Home_as_active_child() = runTest {
        val component = createComponent()

        val active = component.stack.value.active
        assertNotNull(active)
        assertTrue(active.instance is PatientFlowComponent.Child.Home)
    }

    @Test
    fun navigateTo_Search_pushes_Search_onto_stack() = runTest {
        val component = createComponent()

        component.navigateTo(PatientConfig.Search)

        val active = component.stack.value.active
        assertTrue(active.instance is PatientFlowComponent.Child.Search)
    }

    @Test
    fun navigateTo_DoctorProfile_pushes_DoctorProfile_onto_stack() = runTest {
        val component = createComponent()

        component.navigateTo(PatientConfig.DoctorProfile("doc-42"))

        val active = component.stack.value.active
        assertTrue(active.instance is PatientFlowComponent.Child.DoctorProfile)
    }

    @Test
    fun navigateTo_Appointments_pushes_Appointments_onto_stack() = runTest {
        val component = createComponent()

        component.navigateTo(PatientConfig.Appointments)

        val active = component.stack.value.active
        assertTrue(active.instance is PatientFlowComponent.Child.Appointments)
    }

    @Test
    fun navigateTo_MessagesList_pushes_MessagesList_onto_stack() = runTest {
        val component = createComponent()

        component.navigateTo(PatientConfig.MessagesList)

        val active = component.stack.value.active
        assertTrue(active.instance is PatientFlowComponent.Child.MessagesList)
    }

    @Test
    fun navigateTo_Settings_pushes_Settings_onto_stack() = runTest {
        val component = createComponent()

        component.navigateTo(PatientConfig.Settings)

        val active = component.stack.value.active
        assertTrue(active.instance is PatientFlowComponent.Child.Settings)
    }

    @Test
    fun settings_NavigateToChangePassword_output_pushes_ChangePassword_child() = runTest {
        var settingsOutput: ((SettingsComponent.Output) -> Unit)? = null
        val component = createComponent(onSettingsOutput = { settingsOutput = it })
        component.navigateTo(PatientConfig.Settings)

        settingsOutput!!(SettingsComponent.Output.NavigateToChangePassword)

        val active = component.stack.value.active
        assertTrue(active.instance is PatientFlowComponent.Child.ChangePassword)
    }

    @Test
    fun onNavTabSelected_Home_resets_stack_to_Home_only() = runTest {
        val component = createComponent()
        component.navigateTo(PatientConfig.Search)

        component.onNavTabSelected(PatientTab.Home)

        val stackSize = component.stack.value.backStack.size + 1
        assertEquals(1, stackSize)
        assertTrue(component.stack.value.active.instance is PatientFlowComponent.Child.Home)
    }

    @Test
    fun onNavTabSelected_Search_navigates_to_Search() = runTest {
        val component = createComponent()

        component.onNavTabSelected(PatientTab.Search)

        assertTrue(component.stack.value.active.instance is PatientFlowComponent.Child.Search)
    }

    @Test
    fun onNavTabSelected_Appointments_navigates_to_Appointments() = runTest {
        val component = createComponent()

        component.onNavTabSelected(PatientTab.Appointments)

        assertTrue(component.stack.value.active.instance is PatientFlowComponent.Child.Appointments)
    }

    @Test
    fun onNavTabSelected_Messages_navigates_to_AssistantChat() = runTest {
        val component = createComponent()

        component.onNavTabSelected(PatientTab.Messages)

        assertTrue(component.stack.value.active.instance is PatientFlowComponent.Child.AssistantChat)
    }

    @Test
    fun onNavTabSelected_Profile_navigates_to_Profile() = runTest {
        val component = createComponent()

        component.onNavTabSelected(PatientTab.Profile)

        assertTrue(component.stack.value.active.instance is PatientFlowComponent.Child.Profile)
    }

    @Test
    fun navigateTo_AppointmentDetail_shows_AppointmentDetail_child() = runTest {
        val component = createComponent()

        component.navigateTo(PatientConfig.AppointmentDetail("apt-1"))

        assertTrue(component.stack.value.active.instance is PatientFlowComponent.Child.AppointmentDetail)
    }

    @Test
    fun navigateTo_AssistantChat_shows_AssistantChat_child() = runTest {
        val component = createComponent()

        component.navigateTo(PatientConfig.AssistantChat)

        assertTrue(component.stack.value.active.instance is PatientFlowComponent.Child.AssistantChat)
    }
}
