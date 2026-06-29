@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.auth.application

import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.fakes.FakeAuthRemoteDataSource
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegisterPatientUseCaseTest {

    private val fakeRemote = FakeAuthRemoteDataSource()
    private val dispatchers = TestAppDispatchers()

    private val useCase = RegisterPatientUseCase(
        remote = fakeRemote,
        dispatchers = dispatchers,
    )

    // --- Validation: firstName ---

    @Test
    fun empty_firstName_returns_EMPTY_NAME_without_network_call() = runTest {
        val result = useCase(firstName = "", lastName = "Lopez", email = "user@test.com", phone = null, password = "Secure1pass")

        assertTrue(result.isFailure)
        val error = assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(AuthError.ValidationError.Kind.EMPTY_NAME, error.kind)
        assertEquals(0, fakeRemote.registerPatientCallCount)
    }

    @Test
    fun blank_firstName_returns_EMPTY_NAME() = runTest {
        val result = useCase(firstName = "   ", lastName = "Lopez", email = "user@test.com", phone = null, password = "Secure1pass")

        assertTrue(result.isFailure)
        val error = assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(AuthError.ValidationError.Kind.EMPTY_NAME, error.kind)
        assertEquals(0, fakeRemote.registerPatientCallCount)
    }

    // --- Validation: lastName ---

    @Test
    fun empty_lastName_returns_EMPTY_LAST_NAME_without_network_call() = runTest {
        val result = useCase(firstName = "Maria", lastName = "", email = "user@test.com", phone = null, password = "Secure1pass")

        assertTrue(result.isFailure)
        val error = assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(AuthError.ValidationError.Kind.EMPTY_LAST_NAME, error.kind)
        assertEquals(AuthError.ValidationError.Field.LAST_NAME, error.field)
        assertEquals(0, fakeRemote.registerPatientCallCount)
    }

    @Test
    fun blank_lastName_returns_EMPTY_LAST_NAME() = runTest {
        val result = useCase(firstName = "Maria", lastName = "   ", email = "user@test.com", phone = null, password = "Secure1pass")

        assertTrue(result.isFailure)
        val error = assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(AuthError.ValidationError.Kind.EMPTY_LAST_NAME, error.kind)
        assertEquals(0, fakeRemote.registerPatientCallCount)
    }

    // --- Validation: email ---

    @Test
    fun invalid_email_returns_INVALID_EMAIL_without_network_call() = runTest {
        val result = useCase(firstName = "Maria", lastName = "Lopez", email = "not-an-email", phone = null, password = "Secure1pass")

        assertTrue(result.isFailure)
        val error = assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(AuthError.ValidationError.Kind.INVALID_EMAIL, error.kind)
        assertEquals(0, fakeRemote.registerPatientCallCount)
    }

    @Test
    fun email_without_at_sign_returns_INVALID_EMAIL() = runTest {
        val result = useCase(firstName = "Maria", lastName = "Lopez", email = "userdomain.com", phone = null, password = "Secure1pass")

        assertTrue(result.isFailure)
        val error = assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(AuthError.ValidationError.Kind.INVALID_EMAIL, error.kind)
        assertEquals(0, fakeRemote.registerPatientCallCount)
    }

    // --- Validation: password ---

    @Test
    fun weak_password_returns_WEAK_PASSWORD_without_network_call() = runTest {
        val result = useCase(firstName = "Maria", lastName = "Lopez", email = "user@test.com", phone = null, password = "simple")

        assertTrue(result.isFailure)
        val error = assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(AuthError.ValidationError.Kind.WEAK_PASSWORD, error.kind)
        assertEquals(0, fakeRemote.registerPatientCallCount)
    }

    @Test
    fun password_no_uppercase_returns_WEAK_PASSWORD() = runTest {
        val result = useCase(firstName = "Maria", lastName = "Lopez", email = "user@test.com", phone = null, password = "nouppercase1")

        assertTrue(result.isFailure)
        val error = assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(AuthError.ValidationError.Kind.WEAK_PASSWORD, error.kind)
        assertEquals(0, fakeRemote.registerPatientCallCount)
    }

    @Test
    fun password_no_digit_returns_WEAK_PASSWORD() = runTest {
        val result = useCase(firstName = "Maria", lastName = "Lopez", email = "user@test.com", phone = null, password = "NoDigitPassword")

        assertTrue(result.isFailure)
        val error = assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(AuthError.ValidationError.Kind.WEAK_PASSWORD, error.kind)
        assertEquals(0, fakeRemote.registerPatientCallCount)
    }

    // --- Happy path ---

    @Test
    fun valid_input_calls_registerPatient_with_correct_fields_and_returns_success() = runTest {
        fakeRemote.registerPatientResult = Result.success(Unit)

        val result = useCase(firstName = "Maria", lastName = "Lopez", email = "maria@test.com", phone = "+51999888777", password = "Secure1pass")

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRemote.registerPatientCallCount)
        assertEquals("Maria", fakeRemote.lastRegisterPatientFirstName)
        assertEquals("Lopez", fakeRemote.lastRegisterPatientLastName)
        assertEquals("+51999888777", fakeRemote.lastRegisterPatientPhone)
    }

    @Test
    fun blank_phone_is_sent_as_null_to_registerPatient() = runTest {
        fakeRemote.registerPatientResult = Result.success(Unit)

        val result = useCase(firstName = "Maria", lastName = "Lopez", email = "maria@test.com", phone = "  ", password = "Secure1pass")

        assertTrue(result.isSuccess)
        assertNull(fakeRemote.lastRegisterPatientPhone)
    }

    @Test
    fun null_phone_is_sent_as_null_to_registerPatient() = runTest {
        fakeRemote.registerPatientResult = Result.success(Unit)

        useCase(firstName = "Maria", lastName = "Lopez", email = "maria@test.com", phone = null, password = "Secure1pass")

        assertNull(fakeRemote.lastRegisterPatientPhone)
    }

    @Test
    fun remote_failure_propagates_as_failure() = runTest {
        fakeRemote.registerPatientResult = Result.failure(Exception("Network error"))

        val result = useCase(firstName = "Maria", lastName = "Lopez", email = "maria@test.com", phone = null, password = "Secure1pass")

        assertTrue(result.isFailure)
        assertEquals(1, fakeRemote.registerPatientCallCount)
    }

    @Test
    fun doctor_register_method_is_not_called_by_patient_use_case() = runTest {
        fakeRemote.registerPatientResult = Result.success(Unit)

        useCase(firstName = "Maria", lastName = "Lopez", email = "maria@test.com", phone = null, password = "Secure1pass")

        assertEquals(0, fakeRemote.registerCallCount)
    }
}
