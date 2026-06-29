package com.inclinic.app.features.auth.infrastructure.remote

import com.inclinic.app.core.model.Specialty
import com.inclinic.app.features.auth.infrastructure.remote.dto.DoctorFreelanceRequestDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.LoginRequestDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.LoginResponseDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.TwoFactorVerifyResponseDto

/**
 * Port (interface) for remote authentication operations.
 * Implemented by [KtorAuthRemoteDataSource]. Separated for testability.
 */
interface AuthRemoteDataSource {
    suspend fun login(request: LoginRequestDto): Result<LoginResponseDto>
    suspend fun verifyTwoFactor(partialToken: String, code: String): Result<TwoFactorVerifyResponseDto>
    suspend fun register(name: String, email: String, password: String, role: String, specialtyId: String?): Result<Unit>
    suspend fun registerPatient(firstName: String, lastName: String, email: String, phone: String?, password: String): Result<Unit>
    /** POST /api/doctors/freelance — public endpoint, no auth required. Returns Unit on 201. */
    suspend fun registerFreelanceDoctor(request: DoctorFreelanceRequestDto): Result<Unit>
    suspend fun activate(token: String): Result<Unit>
    suspend fun resendActivation(email: String): Result<Unit>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>
    suspend fun refresh(refreshToken: String): Result<LoginResponseDto>
    suspend fun getSpecialties(): Result<List<Specialty>>
    suspend fun getMe(): Result<com.inclinic.app.features.auth.infrastructure.remote.dto.UserDto>
}
