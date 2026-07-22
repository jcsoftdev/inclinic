@file:OptIn(DelicateDecomposeApi::class)

package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.error.toUserMessage
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.features.doctor.onboarding.application.GetOnboardingStatusUseCase
import com.inclinic.app.features.doctor.onboarding.application.ResubmitOnboardingUseCase
import com.inclinic.app.features.doctor.onboarding.application.SubmitOnboardingUseCase
import com.inclinic.app.features.doctor.onboarding.application.UploadDocumentUseCase
import com.inclinic.app.features.doctor.onboarding.core.model.DoctorOnboardingDraft
import com.inclinic.app.features.doctor.onboarding.core.model.PersonalData
import com.inclinic.app.features.doctor.onboarding.core.model.PriceConfig
import com.inclinic.app.features.doctor.onboarding.core.model.UploadedDoc
import com.inclinic.app.features.doctor.onboarding.core.model.WeeklySchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Default implementation of [DoctorOnboardingComponent].
 *
 * Maintains an in-progress [DoctorOnboardingDraft] as partial data is collected
 * from each step. When the last step completes, the draft is submitted via
 * [SubmitOnboardingUseCase] and the stack is replaced with Enviado.
 *
 * The availableSpecialties list is passed in at construction time; in production
 * it comes from GetSpecialtiesUseCase resolved in the DI module.
 */
class DefaultDoctorOnboardingComponent(
    componentContext: ComponentContext,
    private val dispatchers: AppDispatchers,
    private val submitOnboardingUseCase: SubmitOnboardingUseCase,
    private val uploadDocumentUseCase: UploadDocumentUseCase,
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val resubmitOnboardingUseCase: ResubmitOnboardingUseCase,
    private val availableSpecialties: List<String> = emptyList(),
    private val onOutput: (DoctorOnboardingComponent.Output) -> Unit,
) : DoctorOnboardingComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<OnboardingNavConfig>()
    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    // Accumulated draft — mutated as each step completes
    private var personalData: PersonalData? = null
    private var uploadedDocs: List<UploadedDoc> = emptyList()
    private var selectedSpecialties: List<String> = emptyList()
    private var weeklySchedule: WeeklySchedule? = null

    // Referencia al último paso (precios) para reportarle el resultado del envío.
    private var preciosComponent: StepPreciosComponent? = null

    private val _stack = childStack(
        source = navigation,
        serializer = OnboardingNavConfig.serializer(),
        initialConfiguration = OnboardingNavConfig.StepDatos,
        handleBackButton = true,
        childFactory = ::createChild,
    )
    override val stack: Value<ChildStack<*, DoctorOnboardingComponent.Child>> = _stack

    private fun createChild(
        config: OnboardingNavConfig,
        ctx: ComponentContext,
    ): DoctorOnboardingComponent.Child = when (config) {

        OnboardingNavConfig.StepDatos -> DoctorOnboardingComponent.Child.StepDatos(
            DefaultStepDatosComponent(ctx, dispatchers) { data ->
                personalData = data
                navigation.push(OnboardingNavConfig.StepDocumentos)
            }
        )

        OnboardingNavConfig.StepDocumentos -> DoctorOnboardingComponent.Child.StepDocumentos(
            DefaultStepDocumentosComponent(ctx, uploadDocumentUseCase, dispatchers) { docs ->
                uploadedDocs = docs
                navigation.push(OnboardingNavConfig.StepEspecialidades)
            }
        )

        OnboardingNavConfig.StepEspecialidades -> DoctorOnboardingComponent.Child.StepEspecialidades(
            DefaultStepEspecialidadesComponent(ctx, dispatchers, availableSpecialties) { specialties ->
                selectedSpecialties = specialties
                navigation.push(OnboardingNavConfig.StepHorarios)
            }
        )

        OnboardingNavConfig.StepHorarios -> DoctorOnboardingComponent.Child.StepHorarios(
            DefaultStepHorariosComponent(ctx, dispatchers) { schedule ->
                weeklySchedule = schedule
                navigation.push(OnboardingNavConfig.StepPrecios)
            }
        )

        OnboardingNavConfig.StepPrecios -> DoctorOnboardingComponent.Child.StepPrecios(
            DefaultStepPreciosComponent(ctx, dispatchers) { priceConfig ->
                submitDraft(priceConfig)
            }.also { preciosComponent = it }
        )

        OnboardingNavConfig.Enviado -> DoctorOnboardingComponent.Child.Enviado(
            DefaultEnviadoComponent(
                componentContext = ctx,
                dispatchers = dispatchers,
                getOnboardingStatusUseCase = getOnboardingStatusUseCase,
            ) { output ->
                when (output) {
                    EnviadoComponent.Output.LogOut -> onOutput(DoctorOnboardingComponent.Output.NavigateOutToLogin)
                }
            }
        )

        OnboardingNavConfig.Corregir -> DoctorOnboardingComponent.Child.Corregir(
            DefaultCorregirSolicitudComponent(
                componentContext = ctx,
                dispatchers = dispatchers,
                resubmitOnboardingUseCase = resubmitOnboardingUseCase,
            )
        )
    }

    private fun submitDraft(priceConfig: PriceConfig) {
        val pd = personalData
        val sched = weeklySchedule
        if (pd == null || sched == null) {
            // No debería ocurrir (el wizard es lineal), pero si faltan datos avisamos
            // en vez de quedarnos en silencio.
            preciosComponent?.setSubmitError("Faltan datos de pasos anteriores. Vuelve atrás y complétalos.")
            return
        }
        val draft = DoctorOnboardingDraft(
            personalData = pd,
            documents = uploadedDocs,
            specialties = selectedSpecialties,
            schedule = sched,
            prices = priceConfig,
        )

        preciosComponent?.setSubmitting(true)
        scope.launch {
            submitOnboardingUseCase(draft)
                .onSuccess {
                    preciosComponent?.setSubmitting(false)
                    navigation.replaceAll(OnboardingNavConfig.Enviado)
                }
                .onFailure { err ->
                    preciosComponent?.setSubmitError(
                        err.toUserMessage("No se pudo enviar tu solicitud. Revisa tu conexión e inténtalo de nuevo."),
                    )
                }
        }
    }
}
