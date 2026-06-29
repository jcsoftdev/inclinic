package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.specialties.application.CreateSpecialtyUseCase
import com.inclinic.app.features.admin.specialties.application.GetSpecialtiesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminSpecialtiesComponent(
    componentContext: ComponentContext,
    private val getSpecialties: GetSpecialtiesUseCase,
    private val createSpecialty: CreateSpecialtyUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminSpecialtiesComponent.Output) -> Unit,
) : AdminSpecialtiesComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminSpecialtiesState())
    override val state: Value<AdminSpecialtiesState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onFilterChange(filter: AdminSpecialtiesFilter) {
        _state.update { it.copy(activeFilter = filter) }
    }

    override fun onShowCreateDialog() {
        _state.update { it.copy(showCreateDialog = true, createError = null) }
    }

    override fun onDismissCreateDialog() {
        _state.update { it.copy(showCreateDialog = false, createError = null) }
    }

    override fun onCreateSpecialty(name: String, description: String?, icon: String?) {
        if (name.isBlank()) {
            _state.update { it.copy(createError = "El nombre es obligatorio") }
            return
        }
        _state.update { it.copy(isCreating = true, createError = null) }
        scope.launch {
            createSpecialty(name.trim(), description?.takeIf { it.isNotBlank() }, icon?.takeIf { it.isNotBlank() })
                .onSuccess { newItem ->
                    _state.update { st ->
                        st.copy(
                            isCreating = false,
                            showCreateDialog = false,
                            allItems = st.allItems + newItem,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isCreating = false, createError = err.toUserMessage("Error creando especialidad")) }
                }
        }
    }

    override fun onOpenRequests() {
        onOutput(AdminSpecialtiesComponent.Output.OpenRequests)
    }

    override fun onBack() {
        onOutput(AdminSpecialtiesComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getSpecialties()
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, allItems = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando especialidades")) }
                }
        }
    }
}
