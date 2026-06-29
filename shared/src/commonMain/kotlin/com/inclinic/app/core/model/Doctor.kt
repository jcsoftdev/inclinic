package com.inclinic.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Doctor(
    val id: String,
    val fullName: String,
    val email: String,
    val photoUrl: String?,
    val specialties: List<Specialty>,
    val plan: DoctorPlan,
    val ratingAverage: Double?,
    val ratingsCount: Int,
    val consultationFee: Double,
    val homeVisitAvailable: Boolean,
    val virtualVisitAvailable: Boolean,
    val bio: String?,
    val isVerified: Boolean,
    val cmpLicense: String?,
    val onboardingStatus: OnboardingStatus = OnboardingStatus.APPROVED,
)

@Serializable
enum class DoctorPlan { FREE, PREMIUM }

@Serializable
enum class OnboardingStatus { NONE, PENDING, APPROVED, REJECTED }
