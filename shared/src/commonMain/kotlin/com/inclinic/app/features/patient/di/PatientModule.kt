package com.inclinic.app.features.patient.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.HistoryAccessLog
import com.inclinic.app.core.port.CardTokenizer
import com.inclinic.app.core.port.YapeTokenizer
import com.inclinic.app.features.payment.KtorYapeTokenizer
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.patient.appointments.application.CancelAppointmentUseCase
import com.inclinic.app.features.patient.appointments.application.ConfirmRatingUseCase
import com.inclinic.app.features.patient.appointments.application.DisputeAppointmentUseCase
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import com.inclinic.app.features.patient.appointments.application.GetPatientAppointmentsUseCase
import com.inclinic.app.features.patient.appointments.application.GetRescheduleProposalUseCase
import com.inclinic.app.features.patient.appointments.application.RequestVisitTypeChangeUseCase
import com.inclinic.app.features.patient.address.application.ReverseGeocodeUseCase
import com.inclinic.app.features.patient.address.application.SearchAddressUseCase
import com.inclinic.app.features.patient.address.infrastructure.GeocodeDataSource
import com.inclinic.app.features.patient.address.infrastructure.KtorGeocodeDataSource
import com.inclinic.app.features.patient.address.presentation.AddressPickerComponent
import com.inclinic.app.features.patient.address.presentation.DefaultAddressPickerComponent
import com.inclinic.app.features.patient.appointments.application.RescheduleAppointmentUseCase
import com.inclinic.app.features.patient.appointments.application.RespondRescheduleUseCase
import com.inclinic.app.features.patient.availability.application.GetAvailabilityUseCase
import com.inclinic.app.features.patient.availability.application.GetMonthAvailabilityUseCase
import com.inclinic.app.features.patient.booking.application.CreateAppointmentUseCase
import com.inclinic.app.features.patient.chat.application.GetChatMessagesUseCase
import com.inclinic.app.features.patient.chat.application.SendChatMessageUseCase
import com.inclinic.app.features.patient.chat.application.UploadChatAttachmentUseCase
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorDetailUseCase
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorReviewsUseCase
import com.inclinic.app.features.patient.home.application.GetPatientDashboardUseCase
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import com.inclinic.app.features.patient.infrastructure.remote.ChatDataSource
import com.inclinic.app.features.patient.infrastructure.remote.DoctorSearchDataSource
import com.inclinic.app.features.patient.infrastructure.remote.KtorAppointmentDataSource
import com.inclinic.app.features.patient.infrastructure.remote.KtorChatDataSource
import com.inclinic.app.features.patient.infrastructure.remote.KtorDoctorSearchDataSource
import com.inclinic.app.features.patient.infrastructure.remote.KtorMedicalRecordDataSource
import com.inclinic.app.features.patient.infrastructure.remote.KtorNotificationDataSource
import com.inclinic.app.features.patient.infrastructure.remote.KtorPatientDataSource
import com.inclinic.app.features.patient.infrastructure.remote.KtorPrescriptionDataSource
import com.inclinic.app.features.patient.infrastructure.remote.KtorShareDataSource
import com.inclinic.app.features.patient.infrastructure.remote.KtorSymptomAnalysisDataSource
import com.inclinic.app.features.patient.infrastructure.remote.KtorTherapyPackageDataSource
import com.inclinic.app.features.patient.infrastructure.remote.MedicalRecordDataSource
import com.inclinic.app.features.patient.infrastructure.remote.NotificationDataSource
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import com.inclinic.app.features.patient.infrastructure.remote.PrescriptionDataSource
import com.inclinic.app.features.patient.infrastructure.remote.ShareDataSource
import com.inclinic.app.features.patient.infrastructure.remote.KtorSubscriptionDataSource
import com.inclinic.app.features.patient.infrastructure.remote.SubscriptionDataSource
import com.inclinic.app.features.patient.infrastructure.remote.SymptomAnalysisDataSource
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import com.inclinic.app.features.patient.medical_history.application.GetActiveAccessesUseCase
import com.inclinic.app.features.patient.medical_history.application.GetHistoryAccessLogsUseCase
import com.inclinic.app.features.patient.medical_history.application.RevokeAccessUseCase
import com.inclinic.app.features.patient.medical_history.application.GetMedicalHistoryUseCase
import com.inclinic.app.features.patient.medical_history.application.GetMedicalRecordDetailUseCase
import com.inclinic.app.features.patient.medical_history.application.GetPrescriptionDetailUseCase
import com.inclinic.app.features.patient.medical_history.application.GetPrescriptionPdfUrlUseCase
import com.inclinic.app.features.patient.medical_history.application.DownloadPrescriptionPdfUseCase
import com.inclinic.app.features.patient.medical_history.application.GetShareRequestDetailUseCase
import com.inclinic.app.features.patient.medical_history.application.GetShareRequestsUseCase
import com.inclinic.app.features.patient.medical_history.application.RespondShareRequestUseCase
import com.inclinic.app.features.patient.messages.application.GetConversationsUseCase
import com.inclinic.app.features.patient.notifications.application.GetNotificationsUseCase
import com.inclinic.app.features.patient.notifications.application.MarkAllNotificationsReadUseCase
import com.inclinic.app.features.patient.payment.application.ProcessPaymentUseCase
import com.inclinic.app.features.patient.presentation.component.ActiveAccessesComponent
import com.inclinic.app.features.patient.presentation.component.AppointmentDetailComponent
import com.inclinic.app.features.patient.presentation.component.DefaultActiveAccessesComponent
import com.inclinic.app.features.patient.presentation.component.ApproveShareRequestComponent
import com.inclinic.app.features.patient.presentation.component.AvailabilityCalendarComponent
import com.inclinic.app.features.patient.presentation.component.BookingComponent
import com.inclinic.app.features.patient.presentation.component.CancelAppointmentComponent
import com.inclinic.app.features.patient.presentation.component.ChangePasswordComponent
import com.inclinic.app.features.patient.presentation.component.ChangeVisitTypeComponent
import com.inclinic.app.features.patient.presentation.component.ChatComponent
import com.inclinic.app.features.patient.presentation.component.ConfirmRatingComponent
import com.inclinic.app.features.patient.presentation.component.ConsultTypeComponent
import com.inclinic.app.features.patient.presentation.component.DefaultAppointmentDetailComponent
import com.inclinic.app.features.patient.presentation.component.DefaultApproveShareRequestComponent
import com.inclinic.app.features.patient.presentation.component.DefaultAvailabilityCalendarComponent
import com.inclinic.app.features.patient.presentation.component.DefaultBookingComponent
import com.inclinic.app.features.patient.presentation.component.DefaultCancelAppointmentComponent
import com.inclinic.app.features.patient.presentation.component.DefaultChangePasswordComponent
import com.inclinic.app.features.patient.presentation.component.DefaultChangeVisitTypeComponent
import com.inclinic.app.features.patient.presentation.component.DefaultChatComponent
import com.inclinic.app.features.patient.presentation.component.DefaultConfirmRatingComponent
import com.inclinic.app.features.patient.presentation.component.DefaultConsultTypeComponent
import com.inclinic.app.features.patient.presentation.component.DefaultDisputeAppointmentComponent
import com.inclinic.app.features.patient.presentation.component.DefaultDoctorProfileComponent
import com.inclinic.app.features.patient.presentation.component.DefaultDoctorSearchComponent
import com.inclinic.app.features.patient.presentation.component.DefaultHistoryAccessLogDetailComponent
import com.inclinic.app.features.patient.presentation.component.DefaultHistoryAccessLogsComponent
import com.inclinic.app.features.patient.presentation.component.DefaultMedicalHistoryComponent
import com.inclinic.app.features.patient.presentation.component.DefaultMedicalRecordDetailComponent
import com.inclinic.app.features.patient.presentation.component.DefaultMessagesListComponent
import com.inclinic.app.features.patient.presentation.component.DefaultNegotiationComponent
import com.inclinic.app.features.patient.presentation.component.DefaultNotificationsComponent
import com.inclinic.app.features.patient.presentation.component.DefaultPatientAppointmentsListComponent
import com.inclinic.app.features.patient.presentation.component.ClinicalProfileComponent
import com.inclinic.app.features.patient.presentation.component.DefaultClinicalProfileComponent
import com.inclinic.app.features.patient.presentation.component.DefaultDeleteAccountComponent
import com.inclinic.app.features.patient.presentation.component.DeleteAccountComponent
import com.inclinic.app.features.patient.presentation.component.DefaultPatientFlowComponent
import com.inclinic.app.features.patient.presentation.component.DefaultPatientHomeComponent
import com.inclinic.app.features.patient.presentation.component.DefaultPatientProfileComponent
import com.inclinic.app.features.patient.presentation.component.DefaultPaymentComponent
import com.inclinic.app.features.patient.presentation.component.DefaultPrescriptionDetailComponent
import com.inclinic.app.features.patient.presentation.component.DefaultMembershipComponent
import com.inclinic.app.features.patient.presentation.component.DefaultProfileOverviewComponent
import com.inclinic.app.features.patient.presentation.component.DefaultRescheduleAppointmentComponent
import com.inclinic.app.features.patient.presentation.component.DefaultRescheduleResponseComponent
import com.inclinic.app.features.patient.presentation.component.DefaultSettingsComponent
import com.inclinic.app.features.patient.presentation.component.DefaultShareRequestsComponent
import com.inclinic.app.features.patient.presentation.component.DefaultSymptomInputComponent
import com.inclinic.app.features.patient.presentation.component.DefaultSymptomResultsComponent
import com.inclinic.app.features.patient.presentation.component.DefaultTherapyOffersComponent
import com.inclinic.app.features.patient.presentation.component.DefaultPackageStatementComponent
import com.inclinic.app.features.patient.presentation.component.DefaultTherapyPackageDetailComponent
import com.inclinic.app.features.patient.presentation.component.DefaultTherapyPackagesListComponent
import com.inclinic.app.features.patient.presentation.component.DisputeAppointmentComponent
import com.inclinic.app.features.patient.presentation.component.DoctorProfileComponent
import com.inclinic.app.features.patient.presentation.component.DoctorSearchComponent
import com.inclinic.app.features.patient.presentation.component.HistoryAccessLogDetailComponent
import com.inclinic.app.features.patient.presentation.component.HistoryAccessLogsComponent
import com.inclinic.app.features.patient.presentation.component.MedicalHistoryComponent
import com.inclinic.app.features.patient.presentation.component.MembershipComponent
import com.inclinic.app.features.patient.presentation.component.MedicalRecordDetailComponent
import com.inclinic.app.features.patient.presentation.component.MessagesListComponent
import com.inclinic.app.features.patient.presentation.component.NegotiationComponent
import com.inclinic.app.features.patient.presentation.component.NotificationsComponent
import com.inclinic.app.features.patient.presentation.component.PatientAppointmentsListComponent
import com.inclinic.app.features.patient.presentation.component.PatientFlowComponent
import com.inclinic.app.features.patient.presentation.component.PatientHomeComponent
import com.inclinic.app.features.patient.presentation.component.PatientProfileComponent
import com.inclinic.app.features.patient.presentation.component.PaymentComponent
import com.inclinic.app.features.patient.presentation.component.PrescriptionDetailComponent
import com.inclinic.app.features.patient.presentation.component.ProfileOverviewComponent
import com.inclinic.app.features.patient.presentation.component.RescheduleAppointmentComponent
import com.inclinic.app.features.patient.presentation.component.RescheduleResponseComponent
import com.inclinic.app.features.patient.presentation.component.SettingsComponent
import com.inclinic.app.features.patient.presentation.component.ShareRequestsComponent
import com.inclinic.app.features.patient.presentation.component.SymptomInputComponent
import com.inclinic.app.features.patient.presentation.component.SymptomResultsComponent
import com.inclinic.app.features.patient.presentation.component.TherapyOffersComponent
import com.inclinic.app.features.patient.presentation.component.PackageStatementComponent
import com.inclinic.app.features.patient.presentation.component.TherapyPackageDetailComponent
import com.inclinic.app.features.patient.presentation.component.TherapyPackagesListComponent
import com.inclinic.app.features.patient.profile.application.GetPatientProfileUseCase
import com.inclinic.app.features.patient.profile.application.UpdateClinicalProfileUseCase
import com.inclinic.app.features.patient.profile.application.UpdatePatientProfileUseCase
import com.inclinic.app.features.patient.profile.application.GetMedicalProfileUseCase
import com.inclinic.app.features.patient.profile.application.ChangePasswordUseCase
import com.inclinic.app.features.patient.profile.application.DeleteAccountUseCase
import com.inclinic.app.features.patient.assistant.di.assistantChatModule
import com.inclinic.app.features.patient.moderation.di.moderationModule
import com.inclinic.app.features.patient.moderation.presentation.component.BlockUserComponent
import com.inclinic.app.features.patient.moderation.presentation.component.ReportUserComponent
import com.inclinic.app.features.patient.search.application.AnalyzeSymptomsUseCase
import com.inclinic.app.features.patient.search.application.SearchDoctorsUseCase
import com.inclinic.app.features.patient.subscription.application.GetSubscriptionUseCase
import com.inclinic.app.features.patient.subscription.application.PurchasePremiumUseCase
import com.inclinic.app.features.patient.therapy.application.CreateNegotiationUseCase
import com.inclinic.app.features.patient.therapy.application.GetNegotiationUseCase
import com.inclinic.app.features.patient.therapy.application.GetTherapyOfferDetailUseCase
import com.inclinic.app.features.patient.therapy.application.GetTherapyOffersUseCase
import com.inclinic.app.features.patient.therapy.application.GetPackageStatementUseCase
import com.inclinic.app.features.patient.therapy.application.GetTherapyPackageDetailUseCase
import com.inclinic.app.features.patient.therapy.application.PayPackageInstallmentUseCase
import com.inclinic.app.features.patient.therapy.application.PurchasePackageUseCase
import com.inclinic.app.features.patient.therapy.application.GetTherapyPackagesUseCase
import com.inclinic.app.features.patient.therapy.application.RespondNegotiationUseCase
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val patientModule = module {

    // ── Sub-modules ───────────────────────────────────────────────────────────
    includes(assistantChatModule)
    includes(moderationModule)

    // ── Remote data sources ───────────────────────────────────────────────────
    single<DoctorSearchDataSource> {
        KtorDoctorSearchDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<AppointmentDataSource> {
        KtorAppointmentDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<GeocodeDataSource> {
        KtorGeocodeDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<ChatDataSource> {
        KtorChatDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<MedicalRecordDataSource> {
        KtorMedicalRecordDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<PatientDataSource> {
        KtorPatientDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<TherapyPackageDataSource> {
        KtorTherapyPackageDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<NotificationDataSource> {
        KtorNotificationDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<PrescriptionDataSource> {
        KtorPrescriptionDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<ShareDataSource> {
        KtorShareDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<SymptomAnalysisDataSource> {
        KtorSymptomAnalysisDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }
    single<SubscriptionDataSource> {
        KtorSubscriptionDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }

    // ── Use cases ─────────────────────────────────────────────────────────────
    factory { GetPatientDashboardUseCase(get(), get()) }
    factory { SearchDoctorsUseCase(get(), get()) }
    factory { GetDoctorDetailUseCase(get(), get()) }
    factory { GetDoctorReviewsUseCase(get(), get()) }
    factory { GetAvailabilityUseCase(get(), get()) }
    factory { SearchAddressUseCase(get(), get()) }
    factory { ReverseGeocodeUseCase(get(), get()) }
    factory { GetMonthAvailabilityUseCase(get(), get()) }
    factory { CreateAppointmentUseCase(get(), get()) }
    // Yape tokenizer vive en commonMain (HTTP puro) — funciona en Android e iOS.
    // Public key stub por ahora (mismo patrón que el card tokenizer); con una
    // APP_USR-... real hace la tokenización real contra MercadoPago.
    single<YapeTokenizer> { KtorYapeTokenizer(get(APP_HTTP_CLIENT), publicKey = "TEST-mp-public-key") }
    factory { ProcessPaymentUseCase(get(), get(), get(), get()) }
    factory { GetPatientAppointmentsUseCase(get(), get()) }
    factory { GetAppointmentDetailUseCase(get(), get()) }
    factory { CancelAppointmentUseCase(get(), get()) }
    factory { RescheduleAppointmentUseCase(get(), get()) }
    factory { GetRescheduleProposalUseCase(get(), get()) }
    factory { RespondRescheduleUseCase(get(), get()) }
    factory { GetChatMessagesUseCase(get(), get()) }
    factory { SendChatMessageUseCase(get(), get()) }
    factory { GetMedicalHistoryUseCase(get(), get()) }
    factory { GetPatientProfileUseCase(get(), get()) }
    factory { UpdatePatientProfileUseCase(get(), get()) }
    factory { GetTherapyPackagesUseCase(get(), get()) }
    factory { GetTherapyPackageDetailUseCase(get(), get()) }
    factory { GetPackageStatementUseCase(get(), get()) }
    factory { PayPackageInstallmentUseCase(get(), get()) }
    factory { GetTherapyOffersUseCase(get(), get()) }
    factory { PurchasePackageUseCase(get(), get()) }
    factory { GetNegotiationUseCase(get(), get()) }
    factory { GetTherapyOfferDetailUseCase(get(), get()) }
    factory { CreateNegotiationUseCase(get(), get()) }
    factory { RespondNegotiationUseCase(get(), get()) }

    // New use cases
    factory { DisputeAppointmentUseCase(get(), get()) }
    factory { ConfirmRatingUseCase(get(), get()) }
    factory { RequestVisitTypeChangeUseCase(get(), get()) }
    factory { GetConversationsUseCase(get(), get()) }
    factory { GetNotificationsUseCase(get(), get()) }
    factory { MarkAllNotificationsReadUseCase(get(), get()) }
    factory { GetMedicalRecordDetailUseCase(get(), get()) }
    factory { GetPrescriptionDetailUseCase(get(), get()) }
    factory { GetHistoryAccessLogsUseCase(get(), get()) }
    factory { GetShareRequestsUseCase(get(), get()) }
    factory { GetShareRequestDetailUseCase(get(), get()) }
    factory { RespondShareRequestUseCase(get(), get()) }
    factory { AnalyzeSymptomsUseCase(get(), get()) }
    factory { GetMedicalProfileUseCase(get(), get()) }
    factory { UpdateClinicalProfileUseCase(get(), get()) }
    factory { DeleteAccountUseCase(get(), get()) }
    factory { ChangePasswordUseCase(get(), get()) }
    factory { GetPrescriptionPdfUrlUseCase(get()) }
    factory { DownloadPrescriptionPdfUseCase(get(), get()) }
    factory { UploadChatAttachmentUseCase(get(), get()) }
    factory { GetActiveAccessesUseCase(get(), get()) }
    factory { RevokeAccessUseCase(get(), get()) }
    factory { GetSubscriptionUseCase(get(), get()) }
    factory { PurchasePremiumUseCase(get(), get(), get()) }

    // ── Component factories ───────────────────────────────────────────────────
    factory<PatientHomeComponent> { (ctx: ComponentContext, patientId: String, onOutput: (PatientHomeComponent.Output) -> Unit) ->
        DefaultPatientHomeComponent(ctx, patientId, get(), get(), onOutput)
    }
    factory<DoctorSearchComponent> { (ctx: ComponentContext, onOutput: (DoctorSearchComponent.Output) -> Unit) ->
        DefaultDoctorSearchComponent(ctx, get(), get(), get(), onOutput)
    }
    factory<DoctorProfileComponent> { (ctx: ComponentContext, doctorId: String, onOutput: (DoctorProfileComponent.Output) -> Unit) ->
        DefaultDoctorProfileComponent(ctx, doctorId, get(), get(), get(), onOutput)
    }
    factory<ConsultTypeComponent> { (ctx: ComponentContext, doctorId: String, onOutput: (ConsultTypeComponent.Output) -> Unit) ->
        DefaultConsultTypeComponent(ctx, doctorId, get(), get(), onOutput)
    }
    factory<AvailabilityCalendarComponent> { (ctx: ComponentContext, doctorId: String, consultType: String, onOutput: (AvailabilityCalendarComponent.Output) -> Unit) ->
        DefaultAvailabilityCalendarComponent(ctx, doctorId, consultType, get(), get(), get(), onOutput)
    }
    factory<BookingComponent> { params ->
        DefaultBookingComponent(
            componentContext = params.get(0),
            doctorId         = params.get(1),
            slotId           = params.get(2),
            date             = params.get(3),
            getDoctorDetail  = get(),
            createAppointment = get(),
            dispatchers      = get(),
            telemetry        = null,
            onOutput         = params.get(9),
            consultType      = params.get(4),
            startTime        = params.get(5),
            homeVisitAddress = params.values[6] as String?,
            homeVisitLat     = params.values[7] as Double?,
            homeVisitLng     = params.values[8] as Double?,
        )
    }
    factory<AddressPickerComponent> { (ctx: ComponentContext, onOutput: (AddressPickerComponent.Output) -> Unit) ->
        DefaultAddressPickerComponent(ctx, get(), get(), get(), onOutput)
    }
    factory<PaymentComponent> { (ctx: ComponentContext, appointmentId: String?, therapyPackageId: String?, onOutput: (PaymentComponent.Output) -> Unit) ->
        DefaultPaymentComponent(ctx, appointmentId, therapyPackageId, get(), get(), get(), get(), get(), get(), null, onOutput)
    }
    factory<PatientAppointmentsListComponent> { (ctx: ComponentContext, patientId: String, onOutput: (PatientAppointmentsListComponent.Output) -> Unit) ->
        DefaultPatientAppointmentsListComponent(ctx, patientId, get(), get(), onOutput)
    }
    factory<AppointmentDetailComponent> { (ctx: ComponentContext, appointmentId: String, onOutput: (AppointmentDetailComponent.Output) -> Unit) ->
        DefaultAppointmentDetailComponent(ctx, appointmentId, get(), get(), get(), onOutput)
    }
    factory<CancelAppointmentComponent> { (ctx: ComponentContext, appointmentId: String, onOutput: (CancelAppointmentComponent.Output) -> Unit) ->
        DefaultCancelAppointmentComponent(ctx, appointmentId, get(), get(), get(), onOutput)
    }
    factory<RescheduleResponseComponent> { (ctx: ComponentContext, appointmentId: String, onOutput: (RescheduleResponseComponent.Output) -> Unit) ->
        DefaultRescheduleResponseComponent(ctx, appointmentId, get(), get(), get(), onOutput)
    }
    factory<ChatComponent> { (ctx: ComponentContext, doctorId: String, doctorName: String, onOutput: (ChatComponent.Output) -> Unit) ->
        DefaultChatComponent(ctx, doctorId, doctorName, get(), get(), get(), get(), onOutput)
    }
    factory<MedicalHistoryComponent> { (ctx: ComponentContext, patientId: String, onOutput: (MedicalHistoryComponent.Output) -> Unit) ->
        DefaultMedicalHistoryComponent(ctx, patientId, get(), get(), onOutput)
    }
    factory<ProfileOverviewComponent> { (ctx: ComponentContext, patientId: String, onOutput: (ProfileOverviewComponent.Output) -> Unit) ->
        DefaultProfileOverviewComponent(ctx, patientId, get(), get(), get(), get(), onOutput)
    }
    factory<MembershipComponent> { (ctx: ComponentContext, onOutput: (MembershipComponent.Output) -> Unit) ->
        DefaultMembershipComponent(ctx, get(), get(), get(), onOutput)
    }
    factory<PatientProfileComponent> { (ctx: ComponentContext, patientId: String, onOutput: (PatientProfileComponent.Output) -> Unit) ->
        DefaultPatientProfileComponent(ctx, patientId, get(), get(), get(), get(), onOutput)
    }
    factory<TherapyPackagesListComponent> { (ctx: ComponentContext, patientId: String, onOutput: (TherapyPackagesListComponent.Output) -> Unit) ->
        DefaultTherapyPackagesListComponent(ctx, patientId, get(), get(), onOutput)
    }
    factory<TherapyPackageDetailComponent> { (ctx: ComponentContext, packageId: String, onOutput: (TherapyPackageDetailComponent.Output) -> Unit) ->
        DefaultTherapyPackageDetailComponent(ctx, packageId, get(), get(), onOutput)
    }
    factory<PackageStatementComponent> { (ctx: ComponentContext, packageId: String, onOutput: (PackageStatementComponent.Output) -> Unit) ->
        DefaultPackageStatementComponent(ctx, packageId, get(), get(), get(), onOutput)
    }
    factory<TherapyOffersComponent> { (ctx: ComponentContext, onOutput: (TherapyOffersComponent.Output) -> Unit) ->
        DefaultTherapyOffersComponent(ctx, get(), get(), get(), onOutput)
    }
    factory<NegotiationComponent> { params ->
        DefaultNegotiationComponent(
            componentContext  = params.get(0),
            negotiationId     = params.get(1),
            offerId           = params.get(2),
            getNegotiation    = get(),
            getOfferDetail    = get(),
            createNegotiation = get(),
            respondNegotiation = get(),
            dispatchers       = get(),
            onOutput          = params.get(3),
        )
    }

    // New component factories
    factory<RescheduleAppointmentComponent> { (ctx: ComponentContext, appointmentId: String, onOutput: (RescheduleAppointmentComponent.Output) -> Unit) ->
        DefaultRescheduleAppointmentComponent(ctx, appointmentId, get(), get(), get(), get(), get(), onOutput)
    }
    factory<ChangeVisitTypeComponent> { (ctx: ComponentContext, appointmentId: String, onOutput: (ChangeVisitTypeComponent.Output) -> Unit) ->
        DefaultChangeVisitTypeComponent(ctx, appointmentId, get(), get(), get(), onOutput)
    }
    factory<DisputeAppointmentComponent> { (ctx: ComponentContext, appointmentId: String, onOutput: (DisputeAppointmentComponent.Output) -> Unit) ->
        DefaultDisputeAppointmentComponent(ctx, appointmentId, get(), get(), get(), get(), onOutput)
    }
    factory<ConfirmRatingComponent> { (ctx: ComponentContext, appointmentId: String, onOutput: (ConfirmRatingComponent.Output) -> Unit) ->
        DefaultConfirmRatingComponent(ctx, appointmentId, get(), get(), get(), onOutput)
    }
    factory<MessagesListComponent> { (ctx: ComponentContext, onOutput: (MessagesListComponent.Output) -> Unit) ->
        DefaultMessagesListComponent(ctx, get(), get(), onOutput)
    }
    factory<NotificationsComponent> { (ctx: ComponentContext, onOutput: (NotificationsComponent.Output) -> Unit) ->
        DefaultNotificationsComponent(ctx, get(), get(), get(), onOutput)
    }
    factory<SettingsComponent> { (ctx: ComponentContext, patientId: String, onOutput: (SettingsComponent.Output) -> Unit) ->
        DefaultSettingsComponent(ctx, patientId, get(), get(), onOutput)
    }
    factory<MedicalRecordDetailComponent> { (ctx: ComponentContext, recordId: String, onOutput: (MedicalRecordDetailComponent.Output) -> Unit) ->
        DefaultMedicalRecordDetailComponent(ctx, recordId, get(), get(), onOutput)
    }
    factory<PrescriptionDetailComponent> { (ctx: ComponentContext, prescriptionId: String, onOutput: (PrescriptionDetailComponent.Output) -> Unit) ->
        DefaultPrescriptionDetailComponent(ctx, prescriptionId, get(), get(), get(), get(), onOutput)
    }
    factory<HistoryAccessLogsComponent> { (ctx: ComponentContext, onOutput: (HistoryAccessLogsComponent.Output) -> Unit) ->
        DefaultHistoryAccessLogsComponent(ctx, get(), get(), onOutput)
    }
    factory<HistoryAccessLogDetailComponent> { (ctx: ComponentContext, entry: HistoryAccessLog, onOutput: (HistoryAccessLogDetailComponent.Output) -> Unit) ->
        DefaultHistoryAccessLogDetailComponent(ctx, entry, onOutput)
    }
    factory<ShareRequestsComponent> { (ctx: ComponentContext, onOutput: (ShareRequestsComponent.Output) -> Unit) ->
        DefaultShareRequestsComponent(ctx, get(), get(), get(), onOutput)
    }
    factory<ApproveShareRequestComponent> { (ctx: ComponentContext, requestId: String, onOutput: (ApproveShareRequestComponent.Output) -> Unit) ->
        DefaultApproveShareRequestComponent(ctx, requestId, get(), get(), get(), onOutput)
    }
    factory<SymptomInputComponent> { (ctx: ComponentContext, onOutput: (SymptomInputComponent.Output) -> Unit) ->
        DefaultSymptomInputComponent(ctx, onOutput)
    }
    factory<SymptomResultsComponent> { (ctx: ComponentContext, symptoms: String, onOutput: (SymptomResultsComponent.Output) -> Unit) ->
        DefaultSymptomResultsComponent(ctx, symptoms, get(), get(), onOutput)
    }

    factory<ClinicalProfileComponent> { (ctx: ComponentContext, patientId: String, onOutput: (ClinicalProfileComponent.Output) -> Unit) ->
        DefaultClinicalProfileComponent(ctx, patientId, get(), get(), get(), onOutput)
    }
    factory<ChangePasswordComponent> { (ctx: ComponentContext, onOutput: (ChangePasswordComponent.Output) -> Unit) ->
        DefaultChangePasswordComponent(ctx, get(), get(), onOutput)
    }
    factory<DeleteAccountComponent> { (ctx: ComponentContext, onOutput: (DeleteAccountComponent.Output) -> Unit) ->
        DefaultDeleteAccountComponent(ctx, get(), get(), get(), onOutput)
    }
    factory<ActiveAccessesComponent> { (ctx: ComponentContext, onOutput: (ActiveAccessesComponent.Output) -> Unit) ->
        DefaultActiveAccessesComponent(ctx, get(), get(), get(), onOutput)
    }

    // ── Patient flow (root-level) ─────────────────────────────────────────────
    factory<PatientFlowComponent> { (ctx: ComponentContext, patientId: String) ->
        DefaultPatientFlowComponent(
            componentContext = ctx,
            patientId = patientId,
            dispatchers = get<AppDispatchers>(),
            homeFactory = { c, pid, out -> get { parametersOf(c, pid, out) } },
            searchFactory = { c, out -> get { parametersOf(c, out) } },
            doctorProfileFactory = { c, doctorId, out -> get { parametersOf(c, doctorId, out) } },
            consultTypeFactory = { c, doctorId, out -> get { parametersOf(c, doctorId, out) } },
            availabilityFactory = { c, doctorId, consultType, out -> get { parametersOf(c, doctorId, consultType, out) } },
            addressPickerFactory = { c, out -> get { parametersOf(c, out) } },
            bookingFactory = { c, doctorId, slotId, date, consultType, startTime, address, lat, lng, out ->
                get { parametersOf(c, doctorId, slotId, date, consultType, startTime, address, lat, lng, out) }
            },
            paymentFactory = { c, appointmentId, therapyPackageId, out -> get { parametersOf(c, appointmentId, therapyPackageId, out) } },
            appointmentsFactory = { c, pid, out -> get { parametersOf(c, pid, out) } },
            appointmentDetailFactory = { c, appointmentId, out -> get { parametersOf(c, appointmentId, out) } },
            cancelAppointmentFactory = { c, appointmentId, out -> get { parametersOf(c, appointmentId, out) } },
            chatFactory = { c, doctorId, doctorName, onOutput -> get { parametersOf(c, doctorId, doctorName, onOutput) } },
            rescheduleResponseFactory = { c, appointmentId, out -> get { parametersOf(c, appointmentId, out) } },
            medicalHistoryFactory = { c, pid, out -> get { parametersOf(c, pid, out) } },
            profileOverviewFactory = { c, pid, out -> get { parametersOf(c, pid, out) } },
            membershipFactory = { c, out -> get { parametersOf(c, out) } },
            profileFactory = { c, pid, out -> get { parametersOf(c, pid, out) } },
            assistantChatComponentFactory = { c, onOutput -> get { parametersOf(c, onOutput) } },
            rescheduleAppointmentFactory = { c, appointmentId, out -> get { parametersOf(c, appointmentId, out) } },
            changeVisitTypeFactory = { c, appointmentId, out -> get { parametersOf(c, appointmentId, out) } },
            disputeAppointmentFactory = { c, appointmentId, out -> get { parametersOf(c, appointmentId, out) } },
            confirmRatingFactory = { c, appointmentId, out -> get { parametersOf(c, appointmentId, out) } },
            messagesListFactory = { c, out -> get { parametersOf(c, out) } },
            notificationsFactory = { c, out -> get { parametersOf(c, out) } },
            settingsFactory = { c, pid, out -> get { parametersOf(c, pid, out) } },
            medicalRecordDetailFactory = { c, recordId, out -> get { parametersOf(c, recordId, out) } },
            prescriptionDetailFactory = { c, prescriptionId, out -> get { parametersOf(c, prescriptionId, out) } },
            historyAccessLogsFactory = { c, out -> get { parametersOf(c, out) } },
            historyAccessLogDetailFactory = { c, entry, out -> get { parametersOf(c, entry, out) } },
            shareRequestsFactory = { c, out -> get { parametersOf(c, out) } },
            approveShareRequestFactory = { c, requestId, out -> get { parametersOf(c, requestId, out) } },
            symptomInputFactory = { c, out -> get { parametersOf(c, out) } },
            symptomResultsFactory = { c, symptoms, out -> get { parametersOf(c, symptoms, out) } },
            therapyPackagesFactory = { c, pid, out -> get { parametersOf(c, pid, out) } },
            therapyPackageDetailFactory = { c, packageId, out -> get { parametersOf(c, packageId, out) } },
            packageStatementFactory = { c, packageId, out -> get { parametersOf(c, packageId, out) } },
            therapyOffersFactory = { c, out -> get { parametersOf(c, out) } },
            negotiationFactory = { c, negotiationId, offerId, out -> get { parametersOf(c, negotiationId, offerId, out) } },
            reportUserFactory = { c, userId, userName, out -> get { parametersOf(c, userId, userName, out) } },
            blockUserFactory = { c, userId, userName, out -> get { parametersOf(c, userId, userName, out) } },
            clinicalProfileFactory = { c, out -> get { parametersOf(c, patientId, out) } },
            changePasswordFactory = { c, out -> get { parametersOf(c, out) } },
            deleteAccountFactory = { c, out -> get { parametersOf(c, out) } },
            activeAccessesFactory = { c, out -> get { parametersOf(c, out) } },
            onOutput = {},
        )
    }
}
