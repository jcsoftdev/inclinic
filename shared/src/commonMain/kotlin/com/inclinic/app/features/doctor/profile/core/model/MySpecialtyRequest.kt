package com.inclinic.app.features.doctor.profile.core.model

enum class SpecialtyRequestStatus {
    Pending,
    Approved,
    Rejected,
    Expired,
    Unknown,
}

data class MySpecialtyRequest(
    val id: String,
    val specialtyName: String,
    val status: SpecialtyRequestStatus,
    val createdAt: String,
    val documentCount: Int = 0,
    val rejectionReason: String? = null,
)
