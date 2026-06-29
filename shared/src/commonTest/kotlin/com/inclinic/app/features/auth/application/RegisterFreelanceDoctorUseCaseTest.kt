package com.inclinic.app.features.auth.application

import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.fakes.FakeAuthRemoteDataSource
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.auth.infrastructure.remote.dto.FreelanceScheduleDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RegisterFreelanceDoctorUseCaseTest {

    private val fakeRemote = FakeAuthRemoteDataSource()
    private val dispatchers = TestAppDispatchers()
    private val useCase = RegisterFreelanceDoctorUseCase(fakeRemote, dispatchers)

    private fun validSchedule() = FreelanceScheduleDto(
        dayOfWeek = "MONDAY",
        startTime = "09:00",
        endTime = "17:00",
    )

    private fun validParams() = RegisterFreelanceDoctorUseCase.Params(
        firstName = "Patricia",
        lastName = "Huaman",
        email = "patricia@test.com",
        phone = "987654321",
        documents = listOf("https://cdn.test/doc1.pdf"),
        specialtyIds = listOf("sp-1"),
        primarySpecialtyId = "sp-1",
        consultationPrice = 80.0,
        schedules = listOf(validSchedule()),
    )

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    fun valid_params_calls_remote_and_returns_success() = runTest {
        fakeRemote.registerFreelanceDoctorResult = Result.success(Unit)

        val result = useCase(validParams())

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRemote.registerFreelanceDoctorCallCount)
    }

    @Test
    fun valid_params_sends_correct_dto_to_remote() = runTest {
        val params = validParams()
        useCase(params)

        val dto = fakeRemote.lastFreelanceRequest
        assertNotNull(dto)
        assertEquals("Patricia", dto.firstName)
        assertEquals("Huaman", dto.lastName)
        assertEquals("patricia@test.com", dto.email)
        assertEquals("987654321", dto.phone)
        assertEquals(listOf("https://cdn.test/doc1.pdf"), dto.documents)
        assertEquals(listOf("sp-1"), dto.specialtyIds)
        assertEquals("sp-1", dto.primarySpecialtyId)
        assertEquals(80.0, dto.consultationPrice)
        assertEquals(1, dto.schedules.size)
        assertEquals("MONDAY", dto.schedules.first().dayOfWeek)
    }

    @Test
    fun remote_failure_propagates_as_failure() = runTest {
        fakeRemote.registerFreelanceDoctorResult = Result.failure(AuthError.NetworkError)

        val result = useCase(validParams())

        assertTrue(result.isFailure)
        assertIs<AuthError.NetworkError>(result.exceptionOrNull())
    }

    // ── Validation — name ─────────────────────────────────────────────────────

    @Test
    fun blank_firstName_returns_validation_error_and_does_not_call_remote() = runTest {
        val result = useCase(validParams().copy(firstName = " "))

        assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(0, fakeRemote.registerFreelanceDoctorCallCount)
    }

    @Test
    fun single_char_firstName_returns_validation_error() = runTest {
        val result = useCase(validParams().copy(firstName = "X"))
        assertIs<AuthError.ValidationError>(result.exceptionOrNull())
    }

    @Test
    fun blank_lastName_returns_validation_error() = runTest {
        val result = useCase(validParams().copy(lastName = ""))
        assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(0, fakeRemote.registerFreelanceDoctorCallCount)
    }

    // ── Validation — email ────────────────────────────────────────────────────

    @Test
    fun invalid_email_returns_invalid_email_validation_error() = runTest {
        val result = useCase(validParams().copy(email = "not-an-email"))

        val err = result.exceptionOrNull()
        assertIs<AuthError.ValidationError>(err)
        assertEquals(AuthError.ValidationError.Kind.INVALID_EMAIL, err.kind)
        assertEquals(0, fakeRemote.registerFreelanceDoctorCallCount)
    }

    // ── Validation — phone ────────────────────────────────────────────────────

    @Test
    fun short_phone_returns_freelance_validation_error() = runTest {
        val result = useCase(validParams().copy(phone = "123"))

        val err = result.exceptionOrNull()
        assertIs<AuthError.FreelanceValidationError>(err)
        assertEquals(AuthError.FreelanceValidationError.Field.PHONE, err.field)
        assertEquals(0, fakeRemote.registerFreelanceDoctorCallCount)
    }

    // ── Validation — documents ────────────────────────────────────────────────

    @Test
    fun empty_documents_returns_freelance_validation_error() = runTest {
        val result = useCase(validParams().copy(documents = emptyList()))

        val err = result.exceptionOrNull()
        assertIs<AuthError.FreelanceValidationError>(err)
        assertEquals(AuthError.FreelanceValidationError.Field.DOCUMENTS, err.field)
    }

    // ── Validation — specialtyIds ─────────────────────────────────────────────

    @Test
    fun empty_specialtyIds_returns_freelance_validation_error() = runTest {
        val result = useCase(validParams().copy(specialtyIds = emptyList()))

        val err = result.exceptionOrNull()
        assertIs<AuthError.FreelanceValidationError>(err)
        assertEquals(AuthError.FreelanceValidationError.Field.SPECIALTY_IDS, err.field)
    }

    // ── Validation — consultationPrice ────────────────────────────────────────

    @Test
    fun price_below_50_returns_freelance_validation_error() = runTest {
        val result = useCase(validParams().copy(consultationPrice = 49.99))

        val err = result.exceptionOrNull()
        assertIs<AuthError.FreelanceValidationError>(err)
        assertEquals(AuthError.FreelanceValidationError.Field.CONSULTATION_PRICE, err.field)
    }

    @Test
    fun price_exactly_50_is_valid() = runTest {
        val result = useCase(validParams().copy(consultationPrice = 50.0))
        assertTrue(result.isSuccess)
    }

    // ── Validation — schedules ────────────────────────────────────────────────

    @Test
    fun empty_schedules_returns_freelance_validation_error() = runTest {
        val result = useCase(validParams().copy(schedules = emptyList()))

        val err = result.exceptionOrNull()
        assertIs<AuthError.FreelanceValidationError>(err)
        assertEquals(AuthError.FreelanceValidationError.Field.SCHEDULES, err.field)
    }
}
