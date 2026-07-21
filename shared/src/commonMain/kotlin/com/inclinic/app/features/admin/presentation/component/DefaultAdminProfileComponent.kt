package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.GetCurrentUserUseCase
import com.inclinic.app.features.auth.application.LogoutUseCase
import com.inclinic.app.features.auth.application.UpdateUserProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminProfileComponent(
    componentContext: ComponentContext,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val dispatchers: AppDispatchers,
    private val onOpenSecurity: () -> Unit,
    private val onLogout: () -> Unit,
    private val onBack: () -> Unit,
) : AdminProfileComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    // Declared before `init` on purpose: Kotlin initialises properties in declaration order,
    // so `loadUser()` below would touch a still-null `_state` if this came after the block.
    private val _state = MutableValue(AdminProfileState())
    override val state: Value<AdminProfileState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        loadUser()
    }

    override fun onOpenSecurity() = onOpenSecurity.invoke()

    override fun onLogout() {
        _state.update { it.copy(isLoggingOut = true) }
        scope.launch {
            logoutUseCase()
            onLogout.invoke()
        }
    }

    override fun onRetry() = loadUser()

    override fun onBack() = onBack.invoke()

    // ── Edit profile ─────────────────────────────────────────────────────────

    override fun onEditStart() {
        val user = _state.value.user ?: return
        _state.update {
            it.copy(
                isEditing = true,
                editFirstName = user.firstName,
                editLastName = user.lastName,
                editPhone = user.phone.orEmpty(),
                editError = null,
            )
        }
    }

    override fun onEditCancel() {
        _state.update { it.copy(isEditing = false, editError = null) }
    }

    override fun onEditFirstNameChange(value: String) {
        _state.update { it.copy(editFirstName = value, editError = null) }
    }

    override fun onEditLastNameChange(value: String) {
        _state.update { it.copy(editLastName = value, editError = null) }
    }

    override fun onEditPhoneChange(value: String) {
        _state.update { it.copy(editPhone = value, editError = null) }
    }

    override fun onEditSave() {
        val firstName = _state.value.editFirstName.trim()
        val lastName = _state.value.editLastName.trim()
        val phone = _state.value.editPhone.trim()

        if (firstName.length < 2) {
            _state.update { it.copy(editError = "Nombre debe tener al menos 2 caracteres") }
            return
        }
        if (lastName.length < 2) {
            _state.update { it.copy(editError = "Apellido debe tener al menos 2 caracteres") }
            return
        }
        if (phone.isNotEmpty() && phone.length < 6) {
            _state.update { it.copy(editError = "Teléfono inválido") }
            return
        }
        if (_state.value.isSaving) return

        _state.update { it.copy(isSaving = true, editError = null) }
        scope.launch {
            updateUserProfileUseCase(firstName, lastName, phone.ifEmpty { null })
                .onSuccess { updated ->
                    _state.update { it.copy(isSaving = false, isEditing = false, user = updated, editError = null) }
                }
                .onFailure { err ->
                    // Do NOT clear isEditing/edit* fields — keep the user's entered values on a rejected submit.
                    _state.update { it.copy(isSaving = false, editError = err.toUserMessage("Error al guardar el perfil")) }
                }
        }
    }

    private fun loadUser() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getCurrentUserUseCase()
                .onSuccess { user ->
                    _state.update { it.copy(isLoading = false, user = user) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoading = false, error = err.toUserMessage("Error al cargar el perfil"))
                    }
                }
        }
    }
}
