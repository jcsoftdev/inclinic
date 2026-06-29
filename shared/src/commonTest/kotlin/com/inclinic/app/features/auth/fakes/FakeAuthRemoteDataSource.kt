package com.inclinic.app.features.auth.fakes

import com.inclinic.app.core.model.Specialty
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import com.inclinic.app.features.auth.infrastructure.remote.dto.DoctorFreelanceRequestDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.LoginRequestDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.LoginResponseDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.TwoFactorVerifyResponseDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.UserDto

/**
 * In-memory stub for [AuthRemoteDataSource].
 * Configurable per-operation results; tracks call counts to assert no-extra-network guarantees.
 */
class FakeAuthRemoteDataSource : AuthRemoteDataSource {

    // --- Login ---
    var loginResult: Result<LoginResponseDto> = Result.success(
        LoginResponseDto(
            user = UserDto(
                id = "user-1",
                email = "test@test.com",
                firstName = "Test",
                lastName = "User",
                role = "PATIENT",
            ),
            accessToken = "fake-access",
            refreshToken = "fake-refresh",
        )
    )
    var loginCallCount = 0
    var lastRequest: LoginRequestDto? = null

    override suspend fun login(request: LoginRequestDto): Result<LoginResponseDto> {
        loginCallCount++
        lastRequest = request
        return loginResult
    }

    // --- Verify Two Factor ---
    var verifyTwoFactorResult: Result<TwoFactorVerifyResponseDto> = Result.success(
        TwoFactorVerifyResponseDto(
            user = UserDto(
                id = "user-1",
                email = "test@test.com",
                firstName = "Test",
                lastName = "User",
                role = "PATIENT",
            ),
            accessToken = "fake-access",
            refreshToken = "fake-refresh",
        )
    )
    var verifyTwoFactorCallCount = 0

    override suspend fun verifyTwoFactor(partialToken: String, code: String): Result<TwoFactorVerifyResponseDto> {
        verifyTwoFactorCallCount++
        return verifyTwoFactorResult
    }

    // --- Register (doctor flow — keep intact) ---
    var registerResult: Result<Unit> = Result.success(Unit)
    var registerCallCount = 0

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        specialtyId: String?,
    ): Result<Unit> {
        registerCallCount++
        return registerResult
    }

    // --- Register patient ---
    var registerPatientResult: Result<Unit> = Result.success(Unit)
    var registerPatientCallCount = 0
    var lastRegisterPatientFirstName: String? = null
    var lastRegisterPatientLastName: String? = null
    var lastRegisterPatientPhone: String? = null

    override suspend fun registerPatient(
        firstName: String,
        lastName: String,
        email: String,
        phone: String?,
        password: String,
    ): Result<Unit> {
        registerPatientCallCount++
        lastRegisterPatientFirstName = firstName
        lastRegisterPatientLastName = lastName
        lastRegisterPatientPhone = phone
        return registerPatientResult
    }

    // --- Register freelance doctor ---
    var registerFreelanceDoctorResult: Result<Unit> = Result.success(Unit)
    var registerFreelanceDoctorCallCount = 0
    var lastFreelanceRequest: DoctorFreelanceRequestDto? = null

    override suspend fun registerFreelanceDoctor(request: DoctorFreelanceRequestDto): Result<Unit> {
        registerFreelanceDoctorCallCount++
        lastFreelanceRequest = request
        return registerFreelanceDoctorResult
    }

    // --- Other stubs (no-op defaults) ---
    override suspend fun activate(token: String): Result<Unit> = Result.success(Unit)
    override suspend fun resendActivation(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun forgotPassword(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> = Result.success(Unit)
    override suspend fun refresh(refreshToken: String): Result<LoginResponseDto> = loginResult
    override suspend fun getSpecialties(): Result<List<Specialty>> = Result.success(emptyList())
    override suspend fun getMe(): Result<UserDto> = Result.success(
        UserDto(id = "user-1", email = "test@test.com", firstName = "Test", lastName = "User", role = "PATIENT")
    )
}
