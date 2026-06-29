package com.inclinic.app.features.admin.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.appointments.application.GetAdminAppointmentDetailUseCase
import com.inclinic.app.features.admin.appointments.application.GetAdminAppointmentsUseCase
import com.inclinic.app.features.admin.dashboard.application.GetAdminDashboardUseCase
import com.inclinic.app.features.admin.finance.application.GetFinanceUseCase
import com.inclinic.app.features.admin.doctors.application.ApproveDoctorUseCase
import com.inclinic.app.features.admin.doctors.application.GetAdminDoctorDetailUseCase
import com.inclinic.app.features.admin.doctors.application.GetAdminDoctorsUseCase
import com.inclinic.app.features.admin.doctors.application.GetPendingDoctorsUseCase
import com.inclinic.app.features.admin.specialties.application.CreateSpecialtyUseCase
import com.inclinic.app.features.admin.specialties.application.GetSpecialtiesUseCase
import com.inclinic.app.features.admin.specialties.application.GetSpecialtyRequestsUseCase
import com.inclinic.app.features.admin.specialties.application.ResolveSpecialtyRequestUseCase
import com.inclinic.app.features.admin.patients.application.GetPatientsUseCase
import com.inclinic.app.features.admin.patients.application.SuspendUserUseCase
import com.inclinic.app.features.admin.patients.application.UnsuspendUserUseCase
import com.inclinic.app.features.admin.reports.application.GetReportsUseCase
import com.inclinic.app.features.admin.reports.application.ResolveReportUseCase
import com.inclinic.app.features.admin.reviews.application.GetReviewsUseCase
import com.inclinic.app.features.admin.reviews.application.HideReviewUseCase
import com.inclinic.app.features.admin.reviews.application.UnhideReviewUseCase
import com.inclinic.app.features.admin.blockedemails.application.BlockEmailUseCase
import com.inclinic.app.features.admin.blockedemails.application.GetBlockedEmailsUseCase
import com.inclinic.app.features.admin.blockedemails.application.UnblockEmailUseCase
import com.inclinic.app.features.admin.subscriptions.application.GetSubscriptionsUseCase
import com.inclinic.app.features.admin.subscriptions.application.SetUserSubscriptionUseCase
import com.inclinic.app.features.admin.twofactor.application.DisableTwoFactorUseCase
import com.inclinic.app.features.admin.twofactor.application.EnableTwoFactorUseCase
import com.inclinic.app.features.admin.twofactor.application.GetTwoFactorStatusUseCase
import com.inclinic.app.features.admin.twofactor.application.SetupTwoFactorUseCase
import com.inclinic.app.features.admin.twofactor.presentation.component.AdminSecurityComponent
import com.inclinic.app.features.admin.twofactor.presentation.component.AdminTwoFactorSetupComponent
import com.inclinic.app.features.admin.twofactor.presentation.component.DefaultAdminSecurityComponent
import com.inclinic.app.features.admin.twofactor.presentation.component.DefaultAdminTwoFactorSetupComponent
import com.inclinic.app.features.admin.notifications.application.DeleteAdminNotificationUseCase
import com.inclinic.app.features.admin.notifications.application.GetAdminNotificationsUseCase
import com.inclinic.app.features.admin.notifications.application.MarkAdminNotificationReadUseCase
import com.inclinic.app.features.admin.notifications.application.MarkAllAdminNotificationsReadUseCase
import com.inclinic.app.features.admin.notifications.core.port.AdminNotificationsRepository
import com.inclinic.app.features.admin.notifications.infrastructure.DefaultAdminNotificationsRepository
import com.inclinic.app.features.admin.notifications.infrastructure.remote.AdminNotificationsDataSource
import com.inclinic.app.features.admin.notifications.infrastructure.remote.KtorAdminNotificationsDataSource
import com.inclinic.app.features.admin.notifications.presentation.component.AdminNotificationsComponent
import com.inclinic.app.features.admin.notifications.presentation.component.DefaultAdminNotificationsComponent
import com.inclinic.app.core.navigation.AdminConfig
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem
import com.inclinic.app.features.admin.presentation.component.AdminPatientAppointmentsComponent
import com.inclinic.app.features.admin.presentation.component.AdminPatientDetailComponent
import com.inclinic.app.features.admin.presentation.component.AdminPatientsComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminPatientAppointmentsComponent
import com.inclinic.app.features.admin.presentation.component.AdminReportsComponent
import com.inclinic.app.features.admin.presentation.component.AdminResolveReportComponent
import com.inclinic.app.features.admin.presentation.component.AdminBlockedEmailsComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminBlockedEmailsComponent
import com.inclinic.app.features.admin.presentation.component.AdminReviewsComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminReviewsComponent
import com.inclinic.app.features.admin.presentation.component.AdminSubscriptionsComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminSubscriptionsComponent
import com.inclinic.app.features.admin.presentation.component.AdminSpecialtiesComponent
import com.inclinic.app.features.admin.presentation.component.AdminSpecialtyRequestsComponent
import com.inclinic.app.features.admin.presentation.component.AdminSuspendUserComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminPatientDetailComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminPatientsComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminReportsComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminResolveReportComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminSpecialtiesComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminSpecialtyRequestsComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminSuspendUserComponent
import com.inclinic.app.features.admin.disputes.application.GetDisputesUseCase
import com.inclinic.app.features.admin.disputes.application.GetNoShowsUseCase
import com.inclinic.app.features.admin.disputes.application.ResolveDisputeUseCase
import com.inclinic.app.features.admin.disputes.application.ResolveNoShowUseCase
import com.inclinic.app.features.admin.doctors.application.RejectDoctorUseCase
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.KtorAdminDataSource
import com.inclinic.app.features.admin.presentation.component.AdminAppointmentDetailComponent
import com.inclinic.app.features.admin.presentation.component.AdminAppointmentsComponent
import com.inclinic.app.features.admin.presentation.component.AdminDisputasComponent
import com.inclinic.app.features.admin.presentation.component.AdminDashboardComponent
import com.inclinic.app.features.admin.presentation.component.AdminDoctorDetailComponent
import com.inclinic.app.features.admin.presentation.component.AdminDoctorsComponent
import com.inclinic.app.features.admin.presentation.component.AdminFinanceComponent
import com.inclinic.app.features.admin.presentation.component.AdminFlowComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminFinanceComponent
import com.inclinic.app.features.admin.presentation.component.AdminResolveDisputeComponent
import com.inclinic.app.features.admin.presentation.component.AdminResolveNoShowComponent
import com.inclinic.app.features.admin.presentation.component.AdminPendingDoctorDetailComponent
import com.inclinic.app.features.admin.presentation.component.AdminPendingDoctorsComponent
import com.inclinic.app.features.admin.presentation.component.AdminMasMenuComponent
import com.inclinic.app.features.admin.presentation.component.AdminConfigComponent
import com.inclinic.app.features.admin.presentation.component.AdminPlaceholderComponent
import com.inclinic.app.features.admin.presentation.component.AdminProfileComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminAppointmentDetailComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminConfigComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminProfileComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminMasMenuComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminAppointmentsComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminDashboardComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminDoctorDetailComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminDoctorsComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminDisputasComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminFlowComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminPendingDoctorDetailComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminResolveDisputeComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminResolveNoShowComponent
import com.inclinic.app.features.admin.presentation.component.DefaultAdminPendingDoctorsComponent
import com.inclinic.app.features.auth.application.GetCurrentUserUseCase
import com.inclinic.app.features.auth.application.LogoutUseCase
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val adminModule = module {

    // ── Remote data sources ───────────────────────────────────────────────────
    single<AdminDataSource> {
        KtorAdminDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<AdminNotificationsDataSource> {
        KtorAdminNotificationsDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<AdminNotificationsRepository> {
        DefaultAdminNotificationsRepository(remote = get(), dispatchers = get())
    }

    // ── Use cases ─────────────────────────────────────────────────────────────
    factory { GetAdminDashboardUseCase(get(), get()) }
    factory { GetFinanceUseCase(get(), get()) }
    factory { GetAdminAppointmentsUseCase(get(), get()) }
    factory { GetAdminAppointmentDetailUseCase(get(), get()) }
    factory { GetAdminDoctorsUseCase(get(), get()) }
    factory { GetPendingDoctorsUseCase(get(), get()) }
    factory { GetAdminDoctorDetailUseCase(get(), get()) }
    factory { ApproveDoctorUseCase(get(), get()) }
    factory { RejectDoctorUseCase(get(), get()) }
    factory { GetSpecialtiesUseCase(get(), get()) }
    factory { CreateSpecialtyUseCase(get(), get()) }
    factory { GetSpecialtyRequestsUseCase(get(), get()) }
    factory { ResolveSpecialtyRequestUseCase(get(), get()) }
    factory { GetPatientsUseCase(get(), get()) }
    factory { SuspendUserUseCase(get(), get()) }
    factory { UnsuspendUserUseCase(get(), get()) }
    factory { GetDisputesUseCase(get(), get()) }
    factory { ResolveDisputeUseCase(get(), get()) }
    factory { GetNoShowsUseCase(get(), get()) }
    factory { ResolveNoShowUseCase(get(), get()) }
    factory { GetReportsUseCase(get(), get()) }
    factory { ResolveReportUseCase(get(), get()) }
    factory { GetReviewsUseCase(get(), get()) }
    factory { HideReviewUseCase(get(), get()) }
    factory { UnhideReviewUseCase(get(), get()) }
    factory { GetBlockedEmailsUseCase(get(), get()) }
    factory { BlockEmailUseCase(get(), get()) }
    factory { UnblockEmailUseCase(get(), get()) }
    factory { GetAdminNotificationsUseCase(get(), get()) }
    factory { MarkAdminNotificationReadUseCase(get(), get()) }
    factory { MarkAllAdminNotificationsReadUseCase(get(), get()) }
    factory { DeleteAdminNotificationUseCase(get(), get()) }
    factory { GetSubscriptionsUseCase(get(), get()) }
    factory { SetUserSubscriptionUseCase(get(), get()) }
    factory { GetTwoFactorStatusUseCase(get(), get()) }
    factory { SetupTwoFactorUseCase(get(), get()) }
    factory { EnableTwoFactorUseCase(get(), get()) }
    factory { DisableTwoFactorUseCase(get(), get()) }

    // ── Component factories ───────────────────────────────────────────────────
    factory<AdminDashboardComponent> { (ctx: ComponentContext, onOutput: (AdminDashboardComponent.Output) -> Unit) ->
        DefaultAdminDashboardComponent(ctx, get(), get(), onOutput)
    }
    factory<AdminAppointmentsComponent> { (ctx: ComponentContext, onOutput: (AdminAppointmentsComponent.Output) -> Unit) ->
        DefaultAdminAppointmentsComponent(ctx, get(), get(), onOutput)
    }
    factory<AdminAppointmentDetailComponent> { (ctx: ComponentContext, appointmentId: String, onOutput: (AdminAppointmentDetailComponent.Output) -> Unit) ->
        DefaultAdminAppointmentDetailComponent(ctx, appointmentId, get(), get(), onOutput)
    }
    factory<AdminDoctorsComponent> { (ctx: ComponentContext, onOutput: (AdminDoctorsComponent.Output) -> Unit) ->
        DefaultAdminDoctorsComponent(ctx, get(), get(), onOutput)
    }
    factory<AdminDoctorDetailComponent> { (ctx: ComponentContext, doctorId: String, onOutput: (AdminDoctorDetailComponent.Output) -> Unit) ->
        DefaultAdminDoctorDetailComponent(ctx, doctorId, get(), get(), onOutput)
    }
    factory<AdminPendingDoctorsComponent> { (ctx: ComponentContext, onOutput: (AdminPendingDoctorsComponent.Output) -> Unit) ->
        DefaultAdminPendingDoctorsComponent(ctx, get(), get(), onOutput)
    }
    factory<AdminPendingDoctorDetailComponent> { (ctx: ComponentContext, doctorId: String, onOutput: (AdminPendingDoctorDetailComponent.Output) -> Unit) ->
        DefaultAdminPendingDoctorDetailComponent(ctx, doctorId, get(), get(), get(), get(), onOutput)
    }
    factory<AdminFinanceComponent> { (ctx: ComponentContext, onOutput: (AdminFinanceComponent.Output) -> Unit) ->
        DefaultAdminFinanceComponent(ctx, get(), get(), onOutput)
    }
    factory<AdminPlaceholderComponent> { (ctx: ComponentContext, title: String) ->
        AdminPlaceholderComponent(ctx, title)
    }
    // AdminProfileComponent — reuses GetCurrentUserUseCase and LogoutUseCase from authModule
    factory<AdminProfileComponent> { (ctx: ComponentContext, onOpenSecurity: () -> Unit, onLogout: () -> Unit, onBack: () -> Unit) ->
        DefaultAdminProfileComponent(
            componentContext = ctx,
            getCurrentUserUseCase = get(),
            logoutUseCase = get(),
            dispatchers = get(),
            onOpenSecurity = onOpenSecurity,
            onLogout = onLogout,
            onBack = onBack,
        )
    }
    // AdminConfigComponent — reuses GetTwoFactorStatusUseCase already registered above
    factory<AdminConfigComponent> { (ctx: ComponentContext, onOpenSecurity: () -> Unit, onBack: () -> Unit) ->
        DefaultAdminConfigComponent(
            componentContext = ctx,
            getTwoFactorStatusUseCase = get(),
            dispatchers = get(),
            onOpenSecurity = onOpenSecurity,
            onBack = onBack,
        )
    }
    factory<AdminMasMenuComponent> { (ctx: ComponentContext, onOutput: (AdminMasMenuComponent.Output) -> Unit) ->
        DefaultAdminMasMenuComponent(ctx, get(), onOutput)
    }
    factory<AdminPatientsComponent> { (ctx: ComponentContext, onOutput: (AdminPatientsComponent.Output) -> Unit) ->
        DefaultAdminPatientsComponent(ctx, get(), get(), onOutput)
    }
    factory<AdminPatientDetailComponent> { (ctx: ComponentContext, patient: AdminPatientListItem, onOutput: (AdminPatientDetailComponent.Output) -> Unit) ->
        DefaultAdminPatientDetailComponent(ctx, patient, get(), get(), onOutput)
    }
    factory<AdminSuspendUserComponent> { (ctx: ComponentContext, patient: AdminPatientListItem, onOutput: (AdminSuspendUserComponent.Output) -> Unit) ->
        DefaultAdminSuspendUserComponent(ctx, patient, get(), get(), onOutput)
    }
    factory<AdminReportsComponent> { (ctx: ComponentContext, onOutput: (AdminReportsComponent.Output) -> Unit) ->
        DefaultAdminReportsComponent(ctx, get(), get(), onOutput)
    }
    factory<AdminResolveReportComponent> { (ctx: ComponentContext, config: AdminConfig.MasResolveReport, onOutput: (AdminResolveReportComponent.Output) -> Unit) ->
        DefaultAdminResolveReportComponent(ctx, config, get(), get(), onOutput)
    }
    factory<AdminReviewsComponent> { (ctx: ComponentContext, onOutput: (AdminReviewsComponent.Output) -> Unit) ->
        DefaultAdminReviewsComponent(ctx, get(), get(), get(), get(), onOutput)
    }
    factory<AdminBlockedEmailsComponent> { (ctx: ComponentContext, onOutput: (AdminBlockedEmailsComponent.Output) -> Unit) ->
        DefaultAdminBlockedEmailsComponent(ctx, get(), get(), get(), get(), onOutput)
    }
    factory<AdminSubscriptionsComponent> { (ctx: ComponentContext, onOutput: (AdminSubscriptionsComponent.Output) -> Unit) ->
        DefaultAdminSubscriptionsComponent(ctx, get(), get(), get(), onOutput)
    }
    factory<AdminNotificationsComponent> { (ctx: ComponentContext, onOutput: (AdminNotificationsComponent.Output) -> Unit) ->
        DefaultAdminNotificationsComponent(ctx, get(), get(), get(), get(), get(), onOutput)
    }
    factory<AdminSecurityComponent> { (ctx: ComponentContext, onNavigateToSetup: () -> Unit, onBack: () -> Unit) ->
        DefaultAdminSecurityComponent(ctx, get(), get(), get(), onNavigateToSetup, onBack)
    }
    factory<AdminTwoFactorSetupComponent> { (ctx: ComponentContext, onActivated: () -> Unit, onBack: () -> Unit) ->
        DefaultAdminTwoFactorSetupComponent(ctx, get(), get(), get(), onActivated, onBack)
    }
    factory<AdminPatientAppointmentsComponent> { (ctx: ComponentContext, patientId: String, onOutput: (AdminPatientAppointmentsComponent.Output) -> Unit) ->
        DefaultAdminPatientAppointmentsComponent(ctx, patientId, get(), get(), onOutput)
    }
    factory<AdminSpecialtiesComponent> { (ctx: ComponentContext, onOutput: (AdminSpecialtiesComponent.Output) -> Unit) ->
        DefaultAdminSpecialtiesComponent(ctx, get(), get(), get(), onOutput)
    }
    factory<AdminSpecialtyRequestsComponent> { (ctx: ComponentContext, onOutput: (AdminSpecialtyRequestsComponent.Output) -> Unit) ->
        DefaultAdminSpecialtyRequestsComponent(ctx, get(), get(), get(), onOutput)
    }
    factory<AdminDisputasComponent> { (ctx: ComponentContext, onOutput: (AdminDisputasComponent.Output) -> Unit) ->
        DefaultAdminDisputasComponent(ctx, get(), get(), get(), onOutput)
    }
    factory<AdminResolveDisputeComponent> { (ctx: ComponentContext, disputeId: String, onOutput: (AdminResolveDisputeComponent.Output) -> Unit) ->
        DefaultAdminResolveDisputeComponent(ctx, disputeId, get(), get(), get(), onOutput)
    }
    factory<AdminResolveNoShowComponent> { (ctx: ComponentContext, noShowId: String, onOutput: (AdminResolveNoShowComponent.Output) -> Unit) ->
        DefaultAdminResolveNoShowComponent(ctx, noShowId, get(), get(), get(), onOutput)
    }

    // ── Admin flow (root-level) ───────────────────────────────────────────────
    factory<AdminFlowComponent> { (ctx: ComponentContext) ->
        DefaultAdminFlowComponent(
            componentContext = ctx,
            dispatchers = get<AppDispatchers>(),
            dashboardFactory = { c, out -> get { parametersOf(c, out) } },
            appointmentsFactory = { c, out -> get { parametersOf(c, out) } },
            appointmentDetailFactory = { c, id, out -> get { parametersOf(c, id, out) } },
            doctorsFactory = { c, out -> get { parametersOf(c, out) } },
            doctorDetailFactory = { c, id, out -> get { parametersOf(c, id, out) } },
            pendingDoctorsFactory = { c, out -> get { parametersOf(c, out) } },
            pendingDoctorDetailFactory = { c, id, out -> get { parametersOf(c, id, out) } },
            disputasFactory = { c, out -> get { parametersOf(c, out) } },
            resolveDisputeFactory = { c, id, out -> get { parametersOf(c, id, out) } },
            resolveNoShowFactory = { c, id, out -> get { parametersOf(c, id, out) } },
            financeFactory = { c, out -> get { parametersOf(c, out) } },
            masMenuFactory = { c, out -> get { parametersOf(c, out) } },
            patientsFactory = { c, out -> get { parametersOf(c, out) } },
            patientDetailFactory = { c, patient, out -> get { parametersOf(c, patient, out) } },
            suspendUserFactory = { c, patient, out -> get { parametersOf(c, patient, out) } },
            specialtiesFactory = { c, out -> get { parametersOf(c, out) } },
            specialtyRequestsFactory = { c, out -> get { parametersOf(c, out) } },
            reportsFactory = { c, out -> get { parametersOf(c, out) } },
            resolveReportFactory = { c, config, out -> get { parametersOf(c, config, out) } },
            reviewsFactory = { c, out -> get { parametersOf(c, out) } },
            blockedEmailsFactory = { c, out -> get { parametersOf(c, out) } },
            subscriptionsFactory = { c, out -> get { parametersOf(c, out) } },
            profileFactory = { c, onSec, onLogout, onBack -> get { parametersOf(c, onSec, onLogout, onBack) } },
            configFactory = { c, onSec, onBack -> get { parametersOf(c, onSec, onBack) } },
            notificationsFactory = { c, out -> get { parametersOf(c, out) } },
            placeholderFactory = { c, title -> get { parametersOf(c, title) } },
            securityFactory = { c, onSetup, onBack -> get { parametersOf(c, onSetup, onBack) } },
            twoFactorSetupFactory = { c, onActivated, onBack -> get { parametersOf(c, onActivated, onBack) } },
            patientAppointmentsFactory = { c, patientId, out -> get { parametersOf(c, patientId, out) } },
            onOutput = {},
        )
    }
}
