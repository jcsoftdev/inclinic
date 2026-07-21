package com.inclinic.app.features.doctor.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.appointments.application.CompleteAppointmentUseCase
import com.inclinic.app.features.doctor.appointments.application.ConfirmAppointmentUseCase
import com.inclinic.app.features.doctor.appointments.application.GetDoctorAppointmentDetailUseCase
import com.inclinic.app.features.doctor.appointments.application.NoShowUseCase
import com.inclinic.app.features.doctor.chat.application.GetDoctorChatMessagesUseCase
import com.inclinic.app.features.doctor.chat.application.SendDoctorChatMessageUseCase
import com.inclinic.app.features.doctor.config.application.GetDoctorPriceConfigUseCase
import com.inclinic.app.features.doctor.config.application.GetScheduleConfigUseCase
import com.inclinic.app.features.doctor.config.application.SaveScheduleConfigUseCase
import com.inclinic.app.features.doctor.config.application.UpdateDoctorPriceConfigUseCase
import com.inclinic.app.features.doctor.dashboard.application.GetDoctorDashboardUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorChatDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorMedicalRecordDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorPatientDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorProfileDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorScheduleDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.KtorDoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.KtorDoctorChatDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.KtorDoctorMedicalRecordDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.KtorDoctorPatientDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.KtorDoctorProfileDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.KtorDoctorScheduleDataSource
import com.inclinic.app.features.doctor.medical_records.application.CreateMedicalRecordUseCase
import com.inclinic.app.features.doctor.medical_records.application.GetDoctorMedicalRecordsUseCase
import com.inclinic.app.features.doctor.medical_records.application.GetMedicalRecordDetailUseCase
import com.inclinic.app.features.doctor.medical_records.application.UpdateMedicalRecordUseCase
import com.inclinic.app.features.doctor.messages.presentation.component.DoctorChatListComponent
import com.inclinic.app.features.doctor.notifications.presentation.component.DoctorNotificationsComponent
import com.inclinic.app.features.doctor.packages.presentation.component.CreatePackageComponent
import com.inclinic.app.features.doctor.packages.presentation.component.PackageDetailComponent
import com.inclinic.app.features.doctor.packages.presentation.component.PackagesListComponent
import com.inclinic.app.features.doctor.packages.presentation.component.SpecialtyOption
import com.inclinic.app.features.doctor.patient_detail.application.GetPatientDetailUseCase
import com.inclinic.app.features.doctor.patients.presentation.component.PatientsListComponent
import com.inclinic.app.features.doctor.patients.presentation.component.SearchPatientComponent
import com.inclinic.app.features.doctor.presentation.component.CreateMedicalRecordComponent
import com.inclinic.app.features.doctor.presentation.component.DailyScheduleComponent
import com.inclinic.app.features.doctor.presentation.component.DefaultCreateMedicalRecordComponent
import com.inclinic.app.features.doctor.presentation.component.DefaultDailyScheduleComponent
import com.inclinic.app.features.doctor.presentation.component.DefaultDoctorAppointmentDetailComponent
import com.inclinic.app.features.doctor.presentation.component.DefaultDoctorDashboardComponent
import com.inclinic.app.features.doctor.presentation.component.DefaultDoctorFlowComponent
import com.inclinic.app.features.doctor.presentation.component.DefaultEditMedicalRecordComponent
import com.inclinic.app.features.doctor.presentation.component.DefaultMedicalRecordsListComponent
import com.inclinic.app.features.doctor.presentation.component.DefaultPatientDetailComponent
import com.inclinic.app.features.doctor.presentation.component.DefaultPriceConfigComponent
import com.inclinic.app.features.doctor.presentation.component.DefaultScheduleConfigComponent
import com.inclinic.app.features.doctor.presentation.component.DefaultWeeklyScheduleComponent
import com.inclinic.app.features.doctor.no_shows.application.GetNoShowQueueUseCase
import com.inclinic.app.features.doctor.no_shows.presentation.component.DefaultNoShowQueueComponent
import com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowQueueComponent
import com.inclinic.app.features.doctor.pending_closure.application.GetPendingClosureQueueUseCase
import com.inclinic.app.features.doctor.pending_closure.presentation.component.DefaultPendingClosureQueueComponent
import com.inclinic.app.features.doctor.pending_closure.presentation.component.PendingClosureQueueComponent
import com.inclinic.app.features.patient.presentation.component.DeleteAccountComponent
import com.inclinic.app.features.doctor.presentation.component.DoctorAppointmentDetailComponent
import com.inclinic.app.features.doctor.presentation.component.DoctorChatComponent
import com.inclinic.app.features.doctor.presentation.component.DoctorDashboardComponent
import com.inclinic.app.features.doctor.presentation.component.DoctorFlowComponent
import com.inclinic.app.features.doctor.presentation.component.EditMedicalRecordComponent
import com.inclinic.app.features.doctor.presentation.component.MedicalRecordsListComponent
import com.inclinic.app.features.doctor.presentation.component.PatientDetailComponent
import com.inclinic.app.features.doctor.presentation.component.PriceConfigComponent
import com.inclinic.app.features.doctor.presentation.component.ScheduleConfigComponent
import com.inclinic.app.features.doctor.presentation.component.WeeklyScheduleComponent
import com.inclinic.app.features.doctor.profile.presentation.component.ChangePasswordComponent
import com.inclinic.app.features.doctor.profile.presentation.component.EditSpecialtiesComponent
import com.inclinic.app.features.doctor.profile.presentation.component.IncomeComponent
import com.inclinic.app.features.doctor.modality.presentation.component.RespondModalityComponent
import com.inclinic.app.features.doctor.negotiation.presentation.component.RespondPackageNegotiationComponent
import com.inclinic.app.features.doctor.profile.presentation.component.MiPerfilComponent
import com.inclinic.app.features.doctor.profile.presentation.component.MySpecialtyRequestsComponent
import com.inclinic.app.features.doctor.profile.presentation.component.PublicProfileComponent
import com.inclinic.app.features.doctor.profile.presentation.component.ReviewsComponent
import com.inclinic.app.features.doctor.reschedule.presentation.component.RescheduleQueueComponent
import com.inclinic.app.features.doctor.reschedule_request.presentation.component.RequestRescheduleComponent
import com.inclinic.app.features.doctor.settings.presentation.component.DoctorSettingsComponent
import com.inclinic.app.features.doctor.profile.presentation.component.RequestSpecialtyComponent
import com.inclinic.app.features.doctor.schedule.application.GetDailyScheduleUseCase
import com.inclinic.app.features.doctor.schedule.application.GetWeeklyScheduleUseCase
import com.inclinic.app.features.doctor.prescriptions.presentation.component.CreatePrescriptionComponent
import com.inclinic.app.features.doctor.prescriptions.presentation.component.EditPrescriptionComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.RequestShareComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.ShareRequestsListComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.CreateTherapyOfferComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.TherapyOffersListComponent
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val doctorModule = module {

    // ── Remote data sources ───────────────────────────────────────────────────
    single<DoctorAppointmentDataSource> {
        KtorDoctorAppointmentDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<DoctorPatientDataSource> {
        KtorDoctorPatientDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<DoctorMedicalRecordDataSource> {
        KtorDoctorMedicalRecordDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<DoctorChatDataSource> {
        KtorDoctorChatDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<DoctorScheduleDataSource> {
        KtorDoctorScheduleDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<DoctorProfileDataSource> {
        KtorDoctorProfileDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }

    // ── Use cases ─────────────────────────────────────────────────────────────
    factory { GetDoctorDashboardUseCase(get(), get()) }
    factory { GetDailyScheduleUseCase(get(), get()) }
    factory { GetWeeklyScheduleUseCase(get(), get()) }
    factory { GetDoctorAppointmentDetailUseCase(get(), get()) }
    factory { ConfirmAppointmentUseCase(get(), get()) }
    factory { CompleteAppointmentUseCase(get(), get()) }
    factory { NoShowUseCase(get(), get()) }
    factory { GetPatientDetailUseCase(get(), get()) }
    factory { GetDoctorMedicalRecordsUseCase(get(), get()) }
    factory { GetMedicalRecordDetailUseCase(get(), get()) }
    factory { CreateMedicalRecordUseCase(get(), get()) }
    factory { UpdateMedicalRecordUseCase(get(), get()) }
    factory { GetDoctorChatMessagesUseCase(get(), get()) }
    factory { SendDoctorChatMessageUseCase(get(), get()) }
    factory { GetScheduleConfigUseCase(get(), get()) }
    factory { SaveScheduleConfigUseCase(get(), get()) }
    factory { GetDoctorPriceConfigUseCase(get(), get()) }
    factory { UpdateDoctorPriceConfigUseCase(get(), get()) }
    factory { GetNoShowQueueUseCase(get(), get()) }
    factory { GetPendingClosureQueueUseCase(get(), get()) }

    // ── Component factories ───────────────────────────────────────────────────
    factory<NoShowQueueComponent> { (ctx: ComponentContext, onOutput: (NoShowQueueComponent.Output) -> Unit) ->
        DefaultNoShowQueueComponent(ctx, get(), get(), onOutput)
    }
    factory<PendingClosureQueueComponent> { (ctx: ComponentContext, onOutput: (PendingClosureQueueComponent.Output) -> Unit) ->
        DefaultPendingClosureQueueComponent(ctx, get(), get(), onOutput)
    }
    factory<DoctorDashboardComponent> { (ctx: ComponentContext, doctorId: String, onOutput: (DoctorDashboardComponent.Output) -> Unit) ->
        DefaultDoctorDashboardComponent(ctx, doctorId, get(), get(), onOutput, get())
    }
    factory<DailyScheduleComponent> { (ctx: ComponentContext, doctorId: String, onOutput: (DailyScheduleComponent.Output) -> Unit) ->
        DefaultDailyScheduleComponent(ctx, doctorId, get(), get(), onOutput)
    }
    factory<WeeklyScheduleComponent> { (ctx: ComponentContext, doctorId: String, onOutput: (WeeklyScheduleComponent.Output) -> Unit) ->
        DefaultWeeklyScheduleComponent(ctx, doctorId, get(), get(), onOutput)
    }
    factory<DoctorAppointmentDetailComponent> { (ctx: ComponentContext, apptId: String, onOutput: (DoctorAppointmentDetailComponent.Output) -> Unit) ->
        DefaultDoctorAppointmentDetailComponent(ctx, apptId, get(), get(), get(), get(), get(), get(), onOutput)
    }
    factory<PatientDetailComponent> { (ctx: ComponentContext, patientId: String, onOutput: (PatientDetailComponent.Output) -> Unit) ->
        DefaultPatientDetailComponent(ctx, patientId, get(), get(), onOutput)
    }
    factory<MedicalRecordsListComponent> { (ctx: ComponentContext, patientId: String, onOutput: (MedicalRecordsListComponent.Output) -> Unit) ->
        DefaultMedicalRecordsListComponent(ctx, patientId, get(), get(), onOutput)
    }
    factory<CreateMedicalRecordComponent> { (ctx: ComponentContext, patientId: String, appointmentId: String?, onOutput: (CreateMedicalRecordComponent.Output) -> Unit) ->
        DefaultCreateMedicalRecordComponent(ctx, patientId, appointmentId, get(), get(), onOutput)
    }
    factory<EditMedicalRecordComponent> { (ctx: ComponentContext, recordId: String, onOutput: (EditMedicalRecordComponent.Output) -> Unit) ->
        DefaultEditMedicalRecordComponent(ctx, recordId, get(), get(), get(), onOutput)
    }
    factory<DoctorChatComponent> { (ctx: ComponentContext, apptId: String) ->
        DoctorChatComponent(ctx, apptId, get(), get(), get(), get())
    }
    factory<ScheduleConfigComponent> { (ctx: ComponentContext, doctorId: String, onOutput: (ScheduleConfigComponent.Output) -> Unit) ->
        DefaultScheduleConfigComponent(ctx, doctorId, get(), get(), get(), onOutput)
    }
    factory<PriceConfigComponent> { (ctx: ComponentContext, doctorId: String, onOutput: (PriceConfigComponent.Output) -> Unit) ->
        DefaultPriceConfigComponent(ctx, doctorId, get(), get(), get(), onOutput)
    }

    // ── Doctor flow (root-level) ──────────────────────────────────────────────
    factory<DoctorFlowComponent> { (ctx: ComponentContext, doctorId: String) ->
        DefaultDoctorFlowComponent(
            componentContext = ctx,
            doctorId = doctorId,
            dispatchers = get<AppDispatchers>(),
            dashboardFactory = { c, id, out -> get { parametersOf(c, id, out) } },
            dailyScheduleFactory = { c, id, out -> get { parametersOf(c, id, out) } },
            weeklyScheduleFactory = { c, id, out -> get { parametersOf(c, id, out) } },
            appointmentDetailFactory = { c, apptId, out -> get { parametersOf(c, apptId, out) } },
            patientDetailFactory = { c, pid, out -> get { parametersOf(c, pid, out) } },
            medicalRecordsListFactory = { c, pid, out -> get { parametersOf(c, pid, out) } },
            createMedicalRecordFactory = { c, pid, apptId, out -> get { parametersOf(c, pid, apptId, out) } },
            editMedicalRecordFactory = { c, recordId, out -> get { parametersOf(c, recordId, out) } },
            chatFactory = { c, apptId -> get { parametersOf(c, apptId) } },
            scheduleConfigFactory = { c, id, out -> get { parametersOf(c, id, out) } },
            priceConfigFactory = { c, id, out -> get { parametersOf(c, id, out) } },
            patientsListFactory = { c, out -> get<PatientsListComponent> { parametersOf(c, out) } },
            searchPatientFactory = { c, out -> get<SearchPatientComponent> { parametersOf(c, out) } },
            packagesListFactory = { c, out -> get<PackagesListComponent> { parametersOf(c, out) } },
            createPackageFactory = { c, out ->
                get<CreatePackageComponent> { parametersOf(c, "", "", "", emptyList<SpecialtyOption>(), out) }
            },
            packageDetailFactory = { c, packageId, out -> get<PackageDetailComponent> { parametersOf(c, packageId, out) } },
            shareRequestsListFactory = { c, out -> get<ShareRequestsListComponent> { parametersOf(c, out) } },
            requestShareFactory = { c, out -> get<RequestShareComponent> { parametersOf(c, out) } },
            chatListFactory = { c, out -> get<DoctorChatListComponent> { parametersOf(c, out) } },
            miPerfilFactory = { c, out -> get<MiPerfilComponent> { parametersOf(c, doctorId, out) } },
            editSpecialtiesFactory = { c, out -> get<EditSpecialtiesComponent> { parametersOf(c, doctorId, out) } },
            requestSpecialtyFactory = { c, out -> get<RequestSpecialtyComponent> { parametersOf(c, doctorId, out) } },
            mySpecialtyRequestsFactory = { c, out -> get<MySpecialtyRequestsComponent> { parametersOf(c, doctorId, out) } },
            incomeFactory = { c, out -> get<IncomeComponent> { parametersOf(c, doctorId, out) } },
            reviewsFactory = { c, out -> get<ReviewsComponent> { parametersOf(c, doctorId, out) } },
            publicProfileFactory = { c, back -> get<PublicProfileComponent> { parametersOf(c, doctorId, back) } },
            notificationsFactory = { c, out -> get<DoctorNotificationsComponent> { parametersOf(c, out) } },
            settingsFactory = { c, out -> get<DoctorSettingsComponent> { parametersOf(c, out) } },
            rescheduleQueueFactory = { c, out -> get<RescheduleQueueComponent> { parametersOf(c, out) } },
            requestRescheduleFactory = { c, appointmentId, out -> get<RequestRescheduleComponent> { parametersOf(c, appointmentId, out) } },
            respondModalityFactory = { c, requestId, out -> get<RespondModalityComponent> { parametersOf(c, requestId, out) } },
            respondPackageNegotiationFactory = { c, negotiationId, out -> get<RespondPackageNegotiationComponent> { parametersOf(c, negotiationId, out) } },
            therapyOffersListFactory = { c, out -> get<TherapyOffersListComponent> { parametersOf(c, out) } },
            createTherapyOfferFactory = { c, out ->
                get<CreateTherapyOfferComponent> {
                    parametersOf(c, emptyList<com.inclinic.app.features.doctor.therapy_offers.presentation.component.SpecialtyOption>(), out)
                }
            },
            editPrescriptionFactory = { c, prescriptionId, out -> get<EditPrescriptionComponent> { parametersOf(c, prescriptionId, out) } },
            createPrescriptionFactory = { c, appointmentId, out -> get<CreatePrescriptionComponent> { parametersOf(c, appointmentId, out) } },
            deleteAccountFactory = { c, out -> get<DeleteAccountComponent> { parametersOf(c, out) } },
            noShowQueueFactory = { c, out -> get<NoShowQueueComponent> { parametersOf(c, out) } },
            pendingClosureQueueFactory = { c, out -> get<PendingClosureQueueComponent> { parametersOf(c, out) } },
            changePasswordFactory = { c, out -> get<ChangePasswordComponent> { parametersOf(c, doctorId, out) } },
            onOutput = {},
        )
    }
}
