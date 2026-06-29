package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.value.Value

interface PriceConfigComponent {
    val state: Value<PriceConfigState>

    fun onPriceChange(value: String)
    fun onPresentialToggle()
    fun onVirtualToggle()
    fun onSave()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class PriceConfigState(
    val price: String = "",
    val supportsPresential: Boolean = true,
    val supportsVirtual: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
)
