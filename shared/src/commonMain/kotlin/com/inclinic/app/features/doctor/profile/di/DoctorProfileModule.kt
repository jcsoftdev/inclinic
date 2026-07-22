package com.inclinic.app.features.doctor.profile.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.upload.UploadFileUseCase
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.doctor.profile.application.ChangePasswordUseCase
import com.inclinic.app.features.doctor.profile.application.EditSpecialtiesUseCase
import com.inclinic.app.features.doctor.profile.application.GetDoctorIncomeUseCase
import com.inclinic.app.features.doctor.profile.application.GetDoctorProfileUseCase
import com.inclinic.app.features.doctor.profile.application.GetDoctorReviewsUseCase
import com.inclinic.app.features.doctor.profile.application.GetMySpecialtyRequestsUseCase
import com.inclinic.app.features.doctor.profile.application.RequestSpecialtyUseCase
import com.inclinic.app.features.doctor.profile.application.UpdateDoctorProfileUseCase
import com.inclinic.app.features.doctor.profile.infrastructure.DefaultDoctorProfileRepository
import com.inclinic.app.features.doctor.profile.infrastructure.remote.DoctorProfileExtendedDataSource
import com.inclinic.app.features.doctor.profile.infrastructure.remote.KtorDoctorProfileExtendedDataSource
import com.inclinic.app.features.doctor.profile.presentation.component.ChangePasswordComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultChangePasswordComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultEditSpecialtiesComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultIncomeComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultMiPerfilComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultMySpecialtyRequestsComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultPublicProfileComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultRequestSpecialtyComponent
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultReviewsComponent
import com.inclinic.app.features.doctor.profile.presentation.component.EditSpecialtiesComponent
import com.inclinic.app.features.doctor.profile.presentation.component.IncomeComponent
import com.inclinic.app.features.doctor.profile.presentation.component.MiPerfilComponent
import com.inclinic.app.features.doctor.profile.presentation.component.MySpecialtyRequestsComponent
import com.inclinic.app.features.doctor.profile.presentation.component.PublicProfileComponent
import com.inclinic.app.features.doctor.profile.presentation.component.RequestSpecialtyComponent
import com.inclinic.app.features.doctor.profile.presentation.component.ReviewsComponent
import org.koin.dsl.module

fun doctorProfileModule() = module {

    // Shared stateless datasource -- doctorId passed per-call
    single<DoctorProfileExtendedDataSource> {
        KtorDoctorProfileExtendedDataSource(
            client = get(APP_HTTP_CLIENT),
            baseUrl = get<AuthConfig>().apiBaseUrl,
        )
    }

    // Component factories -- params[0]=ctx, params[1]=doctorId, params[2]=onOutput/onBack

    factory<MiPerfilComponent> { params ->
        val ctx = params[0] as ComponentContext
        val doctorId = params[1] as String
        @Suppress("UNCHECKED_CAST")
        val onOutput = params[2] as (MiPerfilComponent.Output) -> Unit
        val dispatchers = get<AppDispatchers>()
        val repo = DefaultDoctorProfileRepository(get(), dispatchers, doctorId)
        DefaultMiPerfilComponent(
            ctx,
            GetDoctorProfileUseCase(repo, dispatchers),
            UpdateDoctorProfileUseCase(repo, dispatchers),
            get(),
            dispatchers,
            onOutput,
        )
    }

    factory<EditSpecialtiesComponent> { params ->
        val ctx = params[0] as ComponentContext
        val doctorId = params[1] as String
        @Suppress("UNCHECKED_CAST")
        val onOutput = params[2] as (EditSpecialtiesComponent.Output) -> Unit
        val dispatchers = get<AppDispatchers>()
        val repo = DefaultDoctorProfileRepository(get(), dispatchers, doctorId)
        DefaultEditSpecialtiesComponent(
            ctx,
            GetDoctorProfileUseCase(repo, dispatchers),
            EditSpecialtiesUseCase(repo, dispatchers),
            dispatchers,
            onOutput,
        )
    }

    factory<RequestSpecialtyComponent> { params ->
        val ctx = params[0] as ComponentContext
        val doctorId = params[1] as String
        @Suppress("UNCHECKED_CAST")
        val onOutput = params[2] as (RequestSpecialtyComponent.Output) -> Unit
        val dispatchers = get<AppDispatchers>()
        val repo = DefaultDoctorProfileRepository(get(), dispatchers, doctorId)
        DefaultRequestSpecialtyComponent(ctx, RequestSpecialtyUseCase(repo, dispatchers), get(), get<UploadFileUseCase>(), dispatchers, onOutput)
    }

    factory<IncomeComponent> { params ->
        val ctx = params[0] as ComponentContext
        val doctorId = params[1] as String
        @Suppress("UNCHECKED_CAST")
        val onOutput = params[2] as (IncomeComponent.Output) -> Unit
        val dispatchers = get<AppDispatchers>()
        val repo = DefaultDoctorProfileRepository(get(), dispatchers, doctorId)
        DefaultIncomeComponent(ctx, GetDoctorIncomeUseCase(repo, dispatchers), dispatchers, onOutput)
    }

    factory<ReviewsComponent> { params ->
        val ctx = params[0] as ComponentContext
        val doctorId = params[1] as String
        @Suppress("UNCHECKED_CAST")
        val onOutput = params[2] as (ReviewsComponent.Output) -> Unit
        val dispatchers = get<AppDispatchers>()
        val repo = DefaultDoctorProfileRepository(get(), dispatchers, doctorId)
        DefaultReviewsComponent(ctx, GetDoctorReviewsUseCase(repo, dispatchers), dispatchers, onOutput)
    }

    factory<PublicProfileComponent> { params ->
        val ctx = params[0] as ComponentContext
        val doctorId = params[1] as String
        @Suppress("UNCHECKED_CAST")
        val onBack = params[2] as () -> Unit
        val dispatchers = get<AppDispatchers>()
        val repo = DefaultDoctorProfileRepository(get(), dispatchers, doctorId)
        DefaultPublicProfileComponent(ctx, GetDoctorProfileUseCase(repo, dispatchers), dispatchers, onBack)
    }

    factory<MySpecialtyRequestsComponent> { params ->
        val ctx = params[0] as ComponentContext
        val doctorId = params[1] as String
        @Suppress("UNCHECKED_CAST")
        val onOutput = params[2] as (MySpecialtyRequestsComponent.Output) -> Unit
        val dispatchers = get<AppDispatchers>()
        val repo = DefaultDoctorProfileRepository(get(), dispatchers, doctorId)
        DefaultMySpecialtyRequestsComponent(ctx, GetMySpecialtyRequestsUseCase(repo, dispatchers), dispatchers, onOutput)
    }

    factory<ChangePasswordComponent> { params ->
        val ctx = params[0] as ComponentContext
        val doctorId = params[1] as String
        @Suppress("UNCHECKED_CAST")
        val onOutput = params[2] as (ChangePasswordComponent.Output) -> Unit
        val dispatchers = get<AppDispatchers>()
        // doctorId needed only for repo construction; changePassword itself is user-agnostic
        val repo = DefaultDoctorProfileRepository(get(), dispatchers, doctorId)
        DefaultChangePasswordComponent(ctx, ChangePasswordUseCase(repo, dispatchers), dispatchers, onOutput)
    }
}
