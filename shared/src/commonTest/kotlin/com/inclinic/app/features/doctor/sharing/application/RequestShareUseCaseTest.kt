package com.inclinic.app.features.doctor.sharing.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.sharing.fakes.FakeDoctorSharingRepository
import com.inclinic.app.features.doctor.sharing.fakes.stubRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequestShareUseCaseTest {

    private val repo = FakeDoctorSharingRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = RequestShareUseCase(repo, dispatchers)

    @Test
    fun returns_created_share_request_on_success() = runTest {
        val expected = stubRequest("new-req")
        repo.requestShareResult = Result.success(expected)
        val result = useCase("patient-1", "Consulta de seguimiento médico")
        assertTrue(result.isSuccess)
        assertEquals("new-req", result.getOrThrow().id)
    }

    @Test
    fun passes_correct_patient_id_to_repository() = runTest {
        useCase("patient-42", "Seguimiento post-operatorio del paciente")
        assertEquals("patient-42", repo.lastRequestPatientId)
    }

    @Test
    fun passes_reason_to_repository() = runTest {
        val reason = "Revisión anual de historial clínico completo"
        useCase("p1", reason)
        assertEquals(reason, repo.lastRequestReason)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.requestShareResult = Result.failure(RuntimeException("403"))
        val result = useCase("p1", "Reason for access to patient history")
        assertTrue(result.isFailure)
    }
}
