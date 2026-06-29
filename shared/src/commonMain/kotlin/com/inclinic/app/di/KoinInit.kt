package com.inclinic.app.di

import com.inclinic.app.core.di.coreModule
import com.inclinic.app.features.admin.di.adminModule
import com.inclinic.app.features.auth.di.authModule
import com.inclinic.app.features.doctor.di.doctorModule
import com.inclinic.app.features.doctor.messages.di.doctorMessagesModule
import com.inclinic.app.features.doctor.modality.di.doctorModalityModule
import com.inclinic.app.features.doctor.negotiation.di.doctorNegotiationModule
import com.inclinic.app.features.doctor.prescriptions.di.doctorPrescriptionsModule
import com.inclinic.app.features.doctor.therapy_offers.di.doctorTherapyOffersModule
import com.inclinic.app.features.doctor.notifications.di.doctorNotificationsModule
import com.inclinic.app.features.doctor.onboarding.di.doctorOnboardingModule
import com.inclinic.app.features.doctor.packages.di.doctorPackagesModule
import com.inclinic.app.features.doctor.patients.di.doctorPatientsModule
import com.inclinic.app.features.doctor.profile.di.doctorProfileModule
import com.inclinic.app.features.doctor.reschedule.di.rescheduleQueueModule
import com.inclinic.app.features.doctor.reschedule_request.di.doctorRescheduleRequestModule
import com.inclinic.app.features.doctor.settings.di.doctorSettingsModule
import com.inclinic.app.features.doctor.sharing.di.doctorSharingModule
import com.inclinic.app.features.patient.di.patientModule
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    extraPlatformModule: Module,
    appDeclaration: KoinAppDeclaration = {},
) {
    org.koin.core.context.startKoin {
        appDeclaration()
        modules(
            coreModule,
            authModule,
            patientModule,
            adminModule,
            doctorModule,
            doctorOnboardingModule(),
            doctorProfileModule(),
            doctorSettingsModule(),
            doctorPatientsModule,
            doctorPackagesModule,
            doctorSharingModule(),
            doctorMessagesModule(),
            doctorNotificationsModule(),
            rescheduleQueueModule(),
            doctorRescheduleRequestModule(),
            doctorModalityModule(),
            doctorNegotiationModule(),
            doctorTherapyOffersModule,
            doctorPrescriptionsModule,
            extraPlatformModule,
        )
    }
}
