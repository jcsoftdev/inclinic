package com.inclinic.app.core.navigation

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DoctorConfigTest {

    private val json = Json { classDiscriminator = "type" }

    @Test
    fun existing_configs_remain_assignable_to_sealed_interface() {
        val existing: List<DoctorConfig> = listOf(
            DoctorConfig.Dashboard,
            DoctorConfig.Schedule,
            DoctorConfig.DailySchedule(date = "2026-05-20"),
            DoctorConfig.AppointmentDetail(appointmentId = "a1"),
            DoctorConfig.CompleteWithEvidence(appointmentId = "a1"),
            DoctorConfig.PatientDetail(patientId = "p1"),
            DoctorConfig.MedicalRecordsList(patientId = "p1"),
            DoctorConfig.MedicalRecordEditor(patientId = "p1"),
            DoctorConfig.EditMedicalRecord(recordId = "r1"),
            DoctorConfig.Chat(appointmentId = "a1"),
            DoctorConfig.ScheduleConfig,
            DoctorConfig.PriceConfig,
        )
        assertEquals(12, existing.size)
    }

    @Test
    fun new_doctor_flow_configs_exist_and_are_assignable() {
        val news: List<DoctorConfig> = listOf(
            DoctorConfig.Onboarding,
            DoctorConfig.Enviado,
            DoctorConfig.CorregirSolicitud,
            DoctorConfig.Profile,
            DoctorConfig.EditSpecialties,
            DoctorConfig.RequestSpecialty,
            DoctorConfig.Patients,
            DoctorConfig.SearchPatient,
            DoctorConfig.Packages,
            DoctorConfig.CreatePackage,
            DoctorConfig.ShareRequests,
            DoctorConfig.RequestShare,
            DoctorConfig.Messages,
            DoctorConfig.Conversation(threadId = "t1"),
            DoctorConfig.Notifications,
            DoctorConfig.Income,
        )
        assertEquals(16, news.size)
        assertTrue(news.all { it is DoctorConfig })
    }

    @Test
    fun parametric_configs_carry_their_id() {
        assertEquals("t-7", (DoctorConfig.Conversation(threadId = "t-7") as DoctorConfig.Conversation).threadId)
    }

    @Test
    fun medicalRecordEditor_appointmentId_defaults_to_null() {
        assertEquals(null, DoctorConfig.MedicalRecordEditor(patientId = "p1").appointmentId)
    }

    @Test
    fun medicalRecordEditor_carries_appointmentId_when_provided() {
        val cfg = DoctorConfig.MedicalRecordEditor(patientId = "p1", appointmentId = "a-9")
        assertEquals("a-9", cfg.appointmentId)
    }

    @Test
    fun medicalRecordEditor_serializes_round_trip_with_appointmentId() {
        val original: DoctorConfig = DoctorConfig.MedicalRecordEditor(patientId = "p1", appointmentId = "a-9")
        val encoded = json.encodeToString(DoctorConfig.serializer(), original)
        val decoded = json.decodeFromString(DoctorConfig.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun new_configs_serialize_round_trip() {
        val original: DoctorConfig = DoctorConfig.Conversation(threadId = "t-42")
        val encoded = json.encodeToString(DoctorConfig.serializer(), original)
        val decoded = json.decodeFromString(DoctorConfig.serializer(), encoded)
        assertEquals(original, decoded)
    }
}
