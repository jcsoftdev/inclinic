package com.inclinic.app.core.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DoctorOnboardingStatusTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun onboarding_status_has_four_known_values() {
        val values = OnboardingStatus.entries.toSet()
        assertEquals(
            setOf(
                OnboardingStatus.NONE,
                OnboardingStatus.PENDING,
                OnboardingStatus.APPROVED,
                OnboardingStatus.REJECTED,
            ),
            values,
        )
        assertEquals(4, values.size)
    }

    @Test
    fun doctor_default_onboarding_status_is_approved_for_legacy_payloads() {
        val legacyJson = """
            {
              "id":"d1","fullName":"Dra. Ana","email":"a@b.c","photoUrl":null,
              "specialties":[],"plan":"FREE","ratingAverage":null,"ratingsCount":0,
              "consultationFee":0.0,"homeVisitAvailable":false,"virtualVisitAvailable":false,
              "bio":null,"isVerified":false,"cmpLicense":null
            }
        """.trimIndent()
        val doctor = json.decodeFromString(Doctor.serializer(), legacyJson)
        assertEquals(OnboardingStatus.APPROVED, doctor.onboardingStatus)
    }

    @Test
    fun doctor_can_carry_pending_status_for_new_registrations() {
        val doctor = sampleDoctor().copy(onboardingStatus = OnboardingStatus.PENDING)
        assertEquals(OnboardingStatus.PENDING, doctor.onboardingStatus)
        assertNotEquals(OnboardingStatus.APPROVED, doctor.onboardingStatus)
    }

    @Test
    fun doctor_serializes_round_trip_with_explicit_status() {
        val original = sampleDoctor().copy(onboardingStatus = OnboardingStatus.REJECTED)
        val encoded = Json.encodeToString(Doctor.serializer(), original)
        val decoded = Json.decodeFromString(Doctor.serializer(), encoded)
        assertEquals(OnboardingStatus.REJECTED, decoded.onboardingStatus)
        assertEquals(original, decoded)
    }

    private fun sampleDoctor() = Doctor(
        id = "d1",
        fullName = "Dra. Ana",
        email = "a@b.c",
        photoUrl = null,
        specialties = emptyList(),
        plan = DoctorPlan.FREE,
        ratingAverage = null,
        ratingsCount = 0,
        consultationFee = 0.0,
        homeVisitAvailable = false,
        virtualVisitAvailable = false,
        bio = null,
        isVerified = false,
        cmpLicense = null,
    )
}
