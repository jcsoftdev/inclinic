@file:OptIn(DelicateDecomposeApi::class)

package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.navigation.AdminConfig
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem
import com.inclinic.app.features.admin.notifications.presentation.component.AdminNotificationsComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class DefaultAdminFlowComponent(
    componentContext: ComponentContext,
    private val dispatchers: AppDispatchers,
    private val dashboardFactory: (ComponentContext, (AdminDashboardComponent.Output) -> Unit) -> AdminDashboardComponent,
    private val appointmentsFactory: (ComponentContext, (AdminAppointmentsComponent.Output) -> Unit) -> AdminAppointmentsComponent,
    private val appointmentDetailFactory: (ComponentContext, String, (AdminAppointmentDetailComponent.Output) -> Unit) -> AdminAppointmentDetailComponent,
    private val doctorsFactory: (ComponentContext, (AdminDoctorsComponent.Output) -> Unit) -> AdminDoctorsComponent,
    private val doctorDetailFactory: (ComponentContext, String, (AdminDoctorDetailComponent.Output) -> Unit) -> AdminDoctorDetailComponent,
    private val pendingDoctorsFactory: (ComponentContext, (AdminPendingDoctorsComponent.Output) -> Unit) -> AdminPendingDoctorsComponent,
    private val pendingDoctorDetailFactory: (ComponentContext, String, (AdminPendingDoctorDetailComponent.Output) -> Unit) -> AdminPendingDoctorDetailComponent,
    private val disputasFactory: (ComponentContext, (AdminDisputasComponent.Output) -> Unit) -> AdminDisputasComponent,
    private val resolveDisputeFactory: (ComponentContext, String, (AdminResolveDisputeComponent.Output) -> Unit) -> AdminResolveDisputeComponent,
    private val resolveNoShowFactory: (ComponentContext, String, (AdminResolveNoShowComponent.Output) -> Unit) -> AdminResolveNoShowComponent,
    private val financeFactory: (ComponentContext, (AdminFinanceComponent.Output) -> Unit) -> AdminFinanceComponent,
    private val masMenuFactory: (ComponentContext, (AdminMasMenuComponent.Output) -> Unit) -> AdminMasMenuComponent,
    private val patientsFactory: (ComponentContext, (AdminPatientsComponent.Output) -> Unit) -> AdminPatientsComponent,
    private val patientDetailFactory: (ComponentContext, AdminPatientListItem, (AdminPatientDetailComponent.Output) -> Unit) -> AdminPatientDetailComponent,
    private val suspendUserFactory: (ComponentContext, AdminPatientListItem, (AdminSuspendUserComponent.Output) -> Unit) -> AdminSuspendUserComponent,
    private val specialtiesFactory: (ComponentContext, (AdminSpecialtiesComponent.Output) -> Unit) -> AdminSpecialtiesComponent,
    private val specialtyRequestsFactory: (ComponentContext, (AdminSpecialtyRequestsComponent.Output) -> Unit) -> AdminSpecialtyRequestsComponent,
    private val reportsFactory: (ComponentContext, (AdminReportsComponent.Output) -> Unit) -> AdminReportsComponent,
    private val resolveReportFactory: (ComponentContext, com.inclinic.app.core.navigation.AdminConfig.MasResolveReport, (AdminResolveReportComponent.Output) -> Unit) -> AdminResolveReportComponent,
    private val reviewsFactory: (ComponentContext, (AdminReviewsComponent.Output) -> Unit) -> AdminReviewsComponent,
    private val blockedEmailsFactory: (ComponentContext, (AdminBlockedEmailsComponent.Output) -> Unit) -> AdminBlockedEmailsComponent,
    private val notificationsFactory: (ComponentContext, (AdminNotificationsComponent.Output) -> Unit) -> AdminNotificationsComponent,
    private val subscriptionsFactory: (ComponentContext, (AdminSubscriptionsComponent.Output) -> Unit) -> AdminSubscriptionsComponent,
    private val profileFactory: (ComponentContext, () -> Unit, () -> Unit, () -> Unit) -> AdminProfileComponent,
    private val configFactory: (ComponentContext, () -> Unit, () -> Unit) -> AdminConfigComponent,
    private val securityFactory: (ComponentContext, () -> Unit, () -> Unit) -> com.inclinic.app.features.admin.twofactor.presentation.component.AdminSecurityComponent,
    private val twoFactorSetupFactory: (ComponentContext, () -> Unit, () -> Unit) -> com.inclinic.app.features.admin.twofactor.presentation.component.AdminTwoFactorSetupComponent,
    private val patientAppointmentsFactory: (ComponentContext, String, (AdminPatientAppointmentsComponent.Output) -> Unit) -> AdminPatientAppointmentsComponent,
    private val onOutput: (AdminFlowComponent.Output) -> Unit,
) : AdminFlowComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    // ── Per-tab navigation ────────────────────────────────────────────────────

    private val inicioNav = StackNavigation<AdminConfig>()
    private val citasNav = StackNavigation<AdminConfig>()
    private val doctoresNav = StackNavigation<AdminConfig>()
    private val disputasNav = StackNavigation<AdminConfig>()
    private val masNav = StackNavigation<AdminConfig>()

    private val _currentTab = MutableValue(AdminTab.Inicio)
    override val currentTab: Value<AdminTab> = _currentTab

    // ── Per-tab child stacks ──────────────────────────────────────────────────

    override val inicioStack: Value<ChildStack<*, AdminFlowComponent.Child>> = childStack(
        source = inicioNav,
        serializer = AdminConfig.serializer(),
        initialConfiguration = AdminConfig.Dashboard,
        key = "AdminInicio",
        handleBackButton = false,
        childFactory = ::createChild,
    )

    override val citasStack: Value<ChildStack<*, AdminFlowComponent.Child>> = childStack(
        source = citasNav,
        serializer = AdminConfig.serializer(),
        initialConfiguration = AdminConfig.Appointments,
        key = "AdminCitas",
        handleBackButton = false,
        childFactory = ::createChild,
    )

    override val doctoresStack: Value<ChildStack<*, AdminFlowComponent.Child>> = childStack(
        source = doctoresNav,
        serializer = AdminConfig.serializer(),
        initialConfiguration = AdminConfig.Doctors,
        key = "AdminDoctores",
        handleBackButton = false,
        childFactory = ::createChild,
    )

    override val disputasStack: Value<ChildStack<*, AdminFlowComponent.Child>> = childStack(
        source = disputasNav,
        serializer = AdminConfig.serializer(),
        initialConfiguration = AdminConfig.Disputes,
        key = "AdminDisputas",
        handleBackButton = false,
        childFactory = ::createChild,
    )

    override val masStack: Value<ChildStack<*, AdminFlowComponent.Child>> = childStack(
        source = masNav,
        serializer = AdminConfig.serializer(),
        initialConfiguration = AdminConfig.More,
        key = "AdminMas",
        handleBackButton = false,
        childFactory = ::createChild,
    )

    // ── Navigation API ────────────────────────────────────────────────────────

    override fun onTabSelected(tab: AdminTab) {
        _currentTab.value = tab
    }

    // ── Child factory ─────────────────────────────────────────────────────────

    private fun createChild(config: AdminConfig, ctx: ComponentContext): AdminFlowComponent.Child =
        when (config) {
            is AdminConfig.Dashboard -> AdminFlowComponent.Child.Dashboard(
                dashboardFactory(ctx) { output ->
                    when (output) {
                        AdminDashboardComponent.Output.NavigateToNotifications ->
                            inicioNav.push(AdminConfig.Notifications)
                        AdminDashboardComponent.Output.NavigateToDoctorApprovals -> {
                            // Switch to Doctores tab and push PendingDoctors
                            _currentTab.value = AdminTab.Doctores
                            doctoresNav.push(AdminConfig.PendingDoctors)
                        }
                        AdminDashboardComponent.Output.NavigateToDisputes -> {
                            _currentTab.value = AdminTab.Disputas
                        }
                        AdminDashboardComponent.Output.NavigateToFinance ->
                            inicioNav.push(AdminConfig.Finance)
                    }
                }
            )
            is AdminConfig.Finance -> AdminFlowComponent.Child.Finance(
                financeFactory(ctx) { output ->
                    when (output) {
                        is AdminFinanceComponent.Output.Back -> inicioNav.pop()
                    }
                }
            )
            is AdminConfig.Notifications -> AdminFlowComponent.Child.Notifications(
                notificationsFactory(ctx) { output ->
                    when (output) {
                        AdminNotificationsComponent.Output.Back -> inicioNav.pop()
                    }
                }
            )
            is AdminConfig.Disputes -> AdminFlowComponent.Child.DisputasRoot(
                disputasFactory(ctx) { output ->
                    when (output) {
                        is AdminDisputasComponent.Output.NavigateToResolveDispute ->
                            disputasNav.push(AdminConfig.ResolveDispute(output.disputeId))
                        is AdminDisputasComponent.Output.NavigateToResolveNoShow ->
                            disputasNav.push(AdminConfig.ResolveNoShow(output.noShowId))
                    }
                }
            )
            is AdminConfig.ResolveDispute -> AdminFlowComponent.Child.ResolveDispute(
                resolveDisputeFactory(ctx, config.disputeId) { output ->
                    when (output) {
                        is AdminResolveDisputeComponent.Output.Back -> disputasNav.pop()
                    }
                }
            )
            is AdminConfig.ResolveNoShow -> AdminFlowComponent.Child.ResolveNoShow(
                resolveNoShowFactory(ctx, config.noShowId) { output ->
                    when (output) {
                        is AdminResolveNoShowComponent.Output.Back -> disputasNav.pop()
                    }
                }
            )
            is AdminConfig.Appointments -> AdminFlowComponent.Child.Appointments(
                appointmentsFactory(ctx) { output ->
                    when (output) {
                        is AdminAppointmentsComponent.Output.NavigateToDetail ->
                            citasNav.push(AdminConfig.AppointmentDetail(output.appointmentId))
                    }
                }
            )
            is AdminConfig.AppointmentDetail -> AdminFlowComponent.Child.AppointmentDetail(
                appointmentDetailFactory(ctx, config.appointmentId) { output ->
                    when (output) {
                        is AdminAppointmentDetailComponent.Output.Back -> citasNav.pop()
                        is AdminAppointmentDetailComponent.Output.NavigateToResolveDispute -> {
                            // Switch to Disputas tab and push the resolve screen
                            _currentTab.value = AdminTab.Disputas
                            disputasNav.push(AdminConfig.ResolveDispute(output.appointmentId))
                        }
                    }
                }
            )
            is AdminConfig.Doctors -> AdminFlowComponent.Child.DoctoresRoot(
                doctorsFactory(ctx) { output ->
                    when (output) {
                        is AdminDoctorsComponent.Output.NavigateToDetail ->
                            doctoresNav.push(AdminConfig.DoctorDetail(output.doctorId))
                        is AdminDoctorsComponent.Output.NavigateToPendingApprovals ->
                            doctoresNav.push(AdminConfig.PendingDoctors)
                    }
                }
            )
            is AdminConfig.DoctorDetail -> AdminFlowComponent.Child.DoctorDetail(
                doctorDetailFactory(ctx, config.doctorId) { output ->
                    when (output) {
                        is AdminDoctorDetailComponent.Output.Back -> doctoresNav.pop()
                    }
                }
            )
            is AdminConfig.PendingDoctors -> AdminFlowComponent.Child.PendingDoctors(
                pendingDoctorsFactory(ctx) { output ->
                    when (output) {
                        is AdminPendingDoctorsComponent.Output.NavigateToPendingDetail ->
                            doctoresNav.push(AdminConfig.PendingDoctorDetail(output.doctorId))
                        is AdminPendingDoctorsComponent.Output.Back -> doctoresNav.pop()
                    }
                }
            )
            is AdminConfig.PendingDoctorDetail -> AdminFlowComponent.Child.PendingDoctorDetail(
                pendingDoctorDetailFactory(ctx, config.doctorId) { output ->
                    when (output) {
                        is AdminPendingDoctorDetailComponent.Output.Back -> doctoresNav.pop()
                        is AdminPendingDoctorDetailComponent.Output.ApproveSuccess -> doctoresNav.pop()
                    }
                }
            )
            is AdminConfig.More -> AdminFlowComponent.Child.MasMenu(
                masMenuFactory(ctx) { output ->
                    when (output) {
                        AdminMasMenuComponent.Output.NavigateToPatients ->
                            masNav.push(AdminConfig.MasPatients)
                        AdminMasMenuComponent.Output.NavigateToSpecialties ->
                            masNav.push(AdminConfig.MasSpecialties)
                        AdminMasMenuComponent.Output.NavigateToReports ->
                            masNav.push(AdminConfig.MasReports)
                        AdminMasMenuComponent.Output.NavigateToReviews ->
                            masNav.push(AdminConfig.MasReviews)
                        AdminMasMenuComponent.Output.NavigateToBlockedEmails ->
                            masNav.push(AdminConfig.MasBlockedEmails)
                        AdminMasMenuComponent.Output.NavigateToSubscriptions ->
                            masNav.push(AdminConfig.MasSubscriptions)
                        AdminMasMenuComponent.Output.NavigateToProfile ->
                            masNav.push(AdminConfig.MasProfile)
                        AdminMasMenuComponent.Output.NavigateToNotifications ->
                            masNav.push(AdminConfig.MasNotifications)
                        AdminMasMenuComponent.Output.NavigateToConfig ->
                            masNav.push(AdminConfig.MasConfig)
                        AdminMasMenuComponent.Output.NavigateToSecurity ->
                            masNav.push(AdminConfig.MasSecurity)
                    }
                }
            )
            is AdminConfig.MasPatients -> AdminFlowComponent.Child.MasPatients(
                patientsFactory(ctx) { output ->
                    when (output) {
                        AdminPatientsComponent.Output.Back -> masNav.pop()
                        is AdminPatientsComponent.Output.NavigateToDetail ->
                            masNav.push(output.patient.toDetailConfig())
                    }
                }
            )
            is AdminConfig.MasPatientDetail -> AdminFlowComponent.Child.MasPatientDetail(
                patientDetailFactory(ctx, config.toPatient()) { output ->
                    when (output) {
                        AdminPatientDetailComponent.Output.Back -> masNav.pop()
                        is AdminPatientDetailComponent.Output.NavigateToSuspend ->
                            masNav.push(output.patient.toSuspendConfig())
                        AdminPatientDetailComponent.Output.ReactivateSuccess -> {
                            masNav.pop() // back to detail; caller should refresh list
                        }
                        is AdminPatientDetailComponent.Output.NavigateToAppointments ->
                            masNav.push(AdminConfig.MasPatientAppointments(output.patientId))
                    }
                }
            )
            is AdminConfig.MasSuspendUser -> AdminFlowComponent.Child.MasSuspendUser(
                suspendUserFactory(ctx, config.toPatient()) { output ->
                    when (output) {
                        AdminSuspendUserComponent.Output.Back -> masNav.pop()
                        is AdminSuspendUserComponent.Output.SuspendSuccess -> {
                            // Pop suspend + detail screens, back to list.
                            // Guarded single op: drop the last two entries only if the
                            // resulting stack is non-empty; otherwise keep the root.
                            // Avoids the iOS crash from two consecutive pop() calls when
                            // the stack has fewer than two entries above the root.
                            masNav.navigate { stack ->
                                stack.dropLast(2).ifEmpty { stack.take(1) }
                            }
                        }
                    }
                }
            )
            is AdminConfig.MasSpecialties -> AdminFlowComponent.Child.MasSpecialties(
                specialtiesFactory(ctx) { output ->
                    when (output) {
                        AdminSpecialtiesComponent.Output.Back -> masNav.pop()
                        AdminSpecialtiesComponent.Output.OpenRequests ->
                            masNav.push(AdminConfig.MasSpecialtyRequests)
                    }
                }
            )
            is AdminConfig.MasSpecialtyRequests -> AdminFlowComponent.Child.MasSpecialtyRequests(
                specialtyRequestsFactory(ctx) { output ->
                    when (output) {
                        AdminSpecialtyRequestsComponent.Output.Back -> masNav.pop()
                    }
                }
            )
            is AdminConfig.MasReports -> AdminFlowComponent.Child.MasReports(
                reportsFactory(ctx) { output ->
                    when (output) {
                        AdminReportsComponent.Output.Back -> masNav.pop()
                        is AdminReportsComponent.Output.NavigateToResolve ->
                            masNav.push(output.report.toResolveConfig())
                    }
                }
            )
            is AdminConfig.MasResolveReport -> AdminFlowComponent.Child.MasResolveReport(
                resolveReportFactory(ctx, config) { output ->
                    when (output) {
                        AdminResolveReportComponent.Output.Back -> masNav.pop()
                        AdminResolveReportComponent.Output.ResolvedSuccess -> {
                            // Pop back to reports list and refresh
                            masNav.pop()
                        }
                    }
                }
            )
            is AdminConfig.MasReviews -> AdminFlowComponent.Child.MasReviews(
                reviewsFactory(ctx) { output ->
                    when (output) {
                        AdminReviewsComponent.Output.Back -> masNav.pop()
                    }
                }
            )
            is AdminConfig.MasBlockedEmails -> AdminFlowComponent.Child.MasBlockedEmails(
                blockedEmailsFactory(ctx) { output ->
                    when (output) {
                        AdminBlockedEmailsComponent.Output.Back -> masNav.pop()
                    }
                }
            )
            is AdminConfig.MasSubscriptions -> AdminFlowComponent.Child.MasSubscriptions(
                subscriptionsFactory(ctx) { output ->
                    when (output) {
                        AdminSubscriptionsComponent.Output.Back -> masNav.pop()
                    }
                }
            )
            is AdminConfig.MasProfile -> AdminFlowComponent.Child.MasProfile(
                profileFactory(
                    ctx,
                    { masNav.push(AdminConfig.MasSecurity) }, // onOpenSecurity
                    { onOutput(AdminFlowComponent.Output.Logout) }, // onLogout — emits Logout output
                    { masNav.pop() },                          // onBack
                )
            )
            is AdminConfig.MasNotifications -> AdminFlowComponent.Child.MasNotifications(
                notificationsFactory(ctx) { output ->
                    when (output) {
                        AdminNotificationsComponent.Output.Back -> masNav.pop()
                    }
                }
            )
            is AdminConfig.MasConfig -> AdminFlowComponent.Child.MasConfigScreen(
                configFactory(
                    ctx,
                    { masNav.push(AdminConfig.MasSecurity) }, // onOpenSecurity
                    { masNav.pop() },                          // onBack
                )
            )
            is AdminConfig.MasSecurity -> AdminFlowComponent.Child.MasSecurity(
                securityFactory(
                    ctx,
                    { masNav.push(AdminConfig.MasTwoFactorSetup) }, // onNavigateToSetup
                    { masNav.pop() },                                 // onBack
                )
            )
            is AdminConfig.MasTwoFactorSetup -> AdminFlowComponent.Child.MasTwoFactorSetup(
                twoFactorSetupFactory(
                    ctx,
                    { masNav.pop() }, // onActivated — pop back to Security (which re-loads status)
                    { masNav.pop() }, // onBack
                )
            )
            is AdminConfig.MasPatientAppointments -> AdminFlowComponent.Child.MasPatientAppointments(
                patientAppointmentsFactory(ctx, config.patientId) { output ->
                    when (output) {
                        AdminPatientAppointmentsComponent.Output.Back -> masNav.pop()
                        is AdminPatientAppointmentsComponent.Output.NavigateToDetail ->
                            citasNav.push(AdminConfig.AppointmentDetail(output.appointmentId))
                    }
                }
            )
        }
}

// ── Patient config ↔ domain helpers ──────────────────────────────────────────

private fun AdminPatientListItem.toDetailConfig() = AdminConfig.MasPatientDetail(
    id = id, userId = userId, firstName = firstName, lastName = lastName,
    email = email, phone = phone, isSuspended = isSuspended,
    suspendedAt = suspendedAt, suspensionReason = suspensionReason,
    subscriptionTier = subscriptionTier, lastLogin = lastLogin, createdAt = createdAt,
    appointmentCount = appointmentCount, therapyPackageCount = therapyPackageCount,
)

private fun AdminPatientListItem.toSuspendConfig() = AdminConfig.MasSuspendUser(
    id = id, userId = userId, firstName = firstName, lastName = lastName,
    email = email, phone = phone, isSuspended = isSuspended,
    suspendedAt = suspendedAt, suspensionReason = suspensionReason,
    subscriptionTier = subscriptionTier, lastLogin = lastLogin, createdAt = createdAt,
    appointmentCount = appointmentCount, therapyPackageCount = therapyPackageCount,
)

private fun AdminConfig.MasPatientDetail.toPatient() = AdminPatientListItem(
    id = id, userId = userId, firstName = firstName, lastName = lastName,
    email = email, phone = phone, isSuspended = isSuspended,
    suspendedAt = suspendedAt, suspensionReason = suspensionReason,
    subscriptionTier = subscriptionTier, lastLogin = lastLogin, createdAt = createdAt,
    appointmentCount = appointmentCount, therapyPackageCount = therapyPackageCount,
)

// ── Report config ↔ domain helpers ───────────────────────────────────────────

private fun com.inclinic.app.features.admin.infrastructure.remote.AdminReportItem.toResolveConfig() =
    com.inclinic.app.core.navigation.AdminConfig.MasResolveReport(
        reportId = id,
        reportStatus = status,
        category = category,
        reason = reason,
        reportedUserFirstName = reportedUser.firstName,
        reportedUserLastName = reportedUser.lastName,
        reportedUserRole = reportedUser.role,
        createdAt = createdAt,
    )

private fun AdminConfig.MasSuspendUser.toPatient() = AdminPatientListItem(
    id = id, userId = userId, firstName = firstName, lastName = lastName,
    email = email, phone = phone, isSuspended = isSuspended,
    suspendedAt = suspendedAt, suspensionReason = suspensionReason,
    subscriptionTier = subscriptionTier, lastLogin = lastLogin, createdAt = createdAt,
    appointmentCount = appointmentCount, therapyPackageCount = therapyPackageCount,
)
