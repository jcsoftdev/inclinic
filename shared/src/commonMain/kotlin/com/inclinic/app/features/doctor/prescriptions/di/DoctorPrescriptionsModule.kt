package com.inclinic.app.features.doctor.prescriptions.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.prescriptions.application.CreatePrescriptionUseCase
import com.inclinic.app.features.doctor.prescriptions.application.GetPrescriptionUseCase
import com.inclinic.app.features.doctor.prescriptions.application.UpdatePrescriptionUseCase
import com.inclinic.app.features.doctor.prescriptions.core.port.DoctorPrescriptionsRepository
import com.inclinic.app.features.doctor.prescriptions.infrastructure.DefaultDoctorPrescriptionsRepository
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.DoctorPrescriptionsDataSource
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.KtorDoctorPrescriptionsDataSource
import com.inclinic.app.features.doctor.prescriptions.presentation.component.CreatePrescriptionComponent
import com.inclinic.app.features.doctor.prescriptions.presentation.component.DefaultCreatePrescriptionComponent
import com.inclinic.app.features.doctor.prescriptions.presentation.component.DefaultEditPrescriptionComponent
import com.inclinic.app.features.doctor.prescriptions.presentation.component.EditPrescriptionComponent
import org.koin.dsl.module

val doctorPrescriptionsModule = module {

    single<DoctorPrescriptionsDataSource> {
        KtorDoctorPrescriptionsDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    single<DoctorPrescriptionsRepository> {
        DefaultDoctorPrescriptionsRepository(remote = get(), dispatchers = get())
    }

    factory { GetPrescriptionUseCase(repository = get(), dispatchers = get()) }
    factory { UpdatePrescriptionUseCase(repository = get(), dispatchers = get()) }
    factory { CreatePrescriptionUseCase(repository = get(), dispatchers = get()) }

    factory<EditPrescriptionComponent> { (ctx: ComponentContext, prescriptionId: String, onOutput: (EditPrescriptionComponent.Output) -> Unit) ->
        DefaultEditPrescriptionComponent(
            componentContext = ctx,
            prescriptionId = prescriptionId,
            getPrescription = get(),
            updatePrescription = get(),
            dispatchers = get<AppDispatchers>(),
            onOutput = onOutput,
        )
    }

    factory<CreatePrescriptionComponent> { (ctx: ComponentContext, appointmentId: String, onOutput: (CreatePrescriptionComponent.Output) -> Unit) ->
        DefaultCreatePrescriptionComponent(
            componentContext = ctx,
            appointmentId = appointmentId,
            createPrescription = get(),
            dispatchers = get<AppDispatchers>(),
            onOutput = onOutput,
        )
    }
}
