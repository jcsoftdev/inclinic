package com.inclinic.app.features.patient.moderation.di

import com.arkivanov.decompose.ComponentContext
import com.inclinic.app.features.auth.config.AuthConfig
import com.inclinic.app.features.auth.di.APP_HTTP_CLIENT
import com.inclinic.app.features.patient.moderation.application.BlockUserUseCase
import com.inclinic.app.features.patient.moderation.application.ReportUserUseCase
import com.inclinic.app.features.patient.moderation.infrastructure.remote.KtorModerationRemoteDataSource
import com.inclinic.app.features.patient.moderation.infrastructure.remote.ModerationRemoteDataSource
import com.inclinic.app.features.patient.moderation.presentation.component.BlockUserComponent
import com.inclinic.app.features.patient.moderation.presentation.component.DefaultBlockUserComponent
import com.inclinic.app.features.patient.moderation.presentation.component.DefaultReportUserComponent
import com.inclinic.app.features.patient.moderation.presentation.component.ReportUserComponent
import org.koin.dsl.module

val moderationModule = module {

    // ── Remote data source ────────────────────────────────────────────────────
    single<ModerationRemoteDataSource> {
        KtorModerationRemoteDataSource(get(APP_HTTP_CLIENT), get<AuthConfig>().apiBaseUrl)
    }

    // ── Use cases ─────────────────────────────────────────────────────────────
    factory { ReportUserUseCase(get(), get()) }
    factory { BlockUserUseCase(get(), get()) }

    // ── Component factories ───────────────────────────────────────────────────
    factory<ReportUserComponent> { (ctx: ComponentContext, userId: String, userName: String, onOutput: (ReportUserComponent.Output) -> Unit) ->
        DefaultReportUserComponent(ctx, userId, userName, get(), get(), onOutput)
    }
    factory<BlockUserComponent> { (ctx: ComponentContext, userId: String, userName: String, onOutput: (BlockUserComponent.Output) -> Unit) ->
        DefaultBlockUserComponent(ctx, userId, userName, get(), get(), onOutput)
    }
}
