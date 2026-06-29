package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.RegisterPatientUseCase
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.core.error.toUserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultRegisterPatientComponent(
    componentContext: ComponentContext,
    private val registerUseCase: RegisterPatientUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (RegisterPatientComponent.Output) -> Unit,
) : RegisterPatientComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(RegisterPatientState())
    override val state: Value<RegisterPatientState> = _state

    override fun onNameChanged(name: String) {
        _state.update { it.copy(name = name, nameError = null) }
    }

    override fun onLastNameChanged(lastName: String) {
        _state.update { it.copy(lastName = lastName, lastNameError = null) }
    }

    override fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    override fun onPhoneChanged(phone: String) {
        _state.update { it.copy(phone = phone) }
    }

    override fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    override fun onConfirmPasswordChanged(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    override fun onSubmit() {
        val s = _state.value
        if (s.isLoading) return

        val confirmError = if (s.password != s.confirmPassword) "Las contraseñas no coinciden" else null
        if (confirmError != null) {
            _state.update { it.copy(confirmPasswordError = confirmError) }
            return
        }

        _state.update { it.copy(isLoading = true, serverError = null) }

        scope.launch {
            registerUseCase(
                firstName = s.name,
                lastName = s.lastName,
                email = s.email,
                phone = s.phone.takeIf { it.isNotBlank() },
                password = s.password,
            )
                .onSuccess {
                    _state.update { it.copy(isLoading = false, success = true) }
                    onOutput(RegisterPatientComponent.Output.Success(s.email))
                }
                .onFailure { error ->
                    when (error) {
                        is AuthError.ValidationError -> {
                            _state.update { st ->
                                st.copy(
                                    isLoading = false,
                                    nameError = if (error.field == AuthError.ValidationError.Field.NAME) fieldMessage(error) else st.nameError,
                                    lastNameError = if (error.field == AuthError.ValidationError.Field.LAST_NAME) fieldMessage(error) else st.lastNameError,
                                    emailError = if (error.field == AuthError.ValidationError.Field.EMAIL) fieldMessage(error) else st.emailError,
                                    passwordError = if (error.field == AuthError.ValidationError.Field.PASSWORD) fieldMessage(error) else st.passwordError,
                                )
                            }
                        }
                        is AuthError -> _state.update { it.copy(isLoading = false, serverError = error) }
                        else -> _state.update { it.copy(isLoading = false, serverError = AuthError.Unknown(error)) }
                    }
                }
        }
    }

    override fun onBack() {
        onOutput(RegisterPatientComponent.Output.Back)
    }

    private fun fieldMessage(error: AuthError.ValidationError): String = when (error.kind) {
        AuthError.ValidationError.Kind.EMPTY_NAME -> "El nombre es requerido"
        AuthError.ValidationError.Kind.EMPTY_LAST_NAME -> "El apellido es requerido"
        AuthError.ValidationError.Kind.INVALID_EMAIL -> "Email no válido"
        AuthError.ValidationError.Kind.EMPTY_PASSWORD -> "La contraseña es requerida"
        AuthError.ValidationError.Kind.WEAK_PASSWORD -> "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número"
        AuthError.ValidationError.Kind.PASSWORD_MISMATCH -> "Las contraseñas no coinciden"
    }
}
