package com.inclinic.app.features.doctor.therapy_offers.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.therapy_offers.application.CreateTherapyOfferUseCase
import com.inclinic.app.features.doctor.therapy_offers.application.GetMyTherapyOffersUseCase
import com.inclinic.app.features.doctor.therapy_offers.core.port.DoctorTherapyOffersRepository
import com.inclinic.app.features.doctor.therapy_offers.infrastructure.DefaultDoctorTherapyOffersRepository
import com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.DoctorTherapyOffersDataSource
import com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.KtorDoctorTherapyOffersDataSource
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.CreateTherapyOfferComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.DefaultCreateTherapyOfferComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.DefaultTherapyOffersListComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.SpecialtyOption
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.TherapyOffersListComponent
import org.koin.dsl.module

val doctorTherapyOffersModule = module {

    single<DoctorTherapyOffersDataSource> {
        KtorDoctorTherapyOffersDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    single<DoctorTherapyOffersRepository> {
        DefaultDoctorTherapyOffersRepository(remote = get(), dispatchers = get())
    }

    factory { GetMyTherapyOffersUseCase(repository = get(), dispatchers = get()) }
    factory { CreateTherapyOfferUseCase(repository = get(), dispatchers = get()) }

    factory<TherapyOffersListComponent> { (ctx: ComponentContext, onOutput: (TherapyOffersListComponent.Output) -> Unit) ->
        DefaultTherapyOffersListComponent(
            componentContext = ctx,
            getMyOffers = get(),
            dispatchers = get<AppDispatchers>(),
            onOutput = onOutput,
        )
    }

    factory<CreateTherapyOfferComponent> { params ->
        @Suppress("UNCHECKED_CAST")
        DefaultCreateTherapyOfferComponent(
            componentContext = params[0],
            specialties = params.get<List<SpecialtyOption>>(),
            createOffer = get(),
            dispatchers = get<AppDispatchers>(),
            onOutput = params.get<(CreateTherapyOfferComponent.Output) -> Unit>(),
        )
    }
}
