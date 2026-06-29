package com.inclinic.app.features.patient.moderation.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.patient.moderation.core.model.ReportCategory

interface ReportUserComponent {
    val state: Value<ReportUserState>

    fun onReasonChanged(reason: String)
    fun onCategorySelected(category: ReportCategory)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Submitted : Output
        data object Back : Output
    }
}

data class ReportUserState(
    val targetUserId: String = "",
    val targetUserName: String = "",
    val reason: String = "",
    val selectedCategory: ReportCategory? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)
