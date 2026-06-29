@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.moderation

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.moderation.application.ReportUserUseCase
import com.inclinic.app.features.patient.moderation.core.model.ReportCategory
import com.inclinic.app.features.patient.moderation.infrastructure.remote.ModerationRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// ── Fake ─────────────────────────────────────────────────────────────────────

private class FakeModerationDataSource : ModerationRemoteDataSource {
    var reportResult: Result<Unit> = Result.success(Unit)
    var lastUserId: String? = null
    var lastReason: String? = null
    var lastCategory: ReportCategory? = null
    var reportCallCount = 0

    override suspend fun reportUser(userId: String, reason: String, category: ReportCategory?): Result<Unit> {
        reportCallCount++
        lastUserId = userId
        lastReason = reason
        lastCategory = category
        return reportResult
    }

    override suspend fun blockUser(userId: String, reason: String?) = Result.success(Unit)
    override suspend fun unblockUser(userId: String) = Result.success(Unit)
}

// ── Tests ─────────────────────────────────────────────────────────────────────

class ReportUserUseCaseTest {

    private val fake = FakeModerationDataSource()
    private val useCase = ReportUserUseCase(fake, TestAppDispatchers())

    @Test
    fun success_forwards_all_args_to_datasource() = runTest {
        val result = useCase("user-42", "Doctor did not show up", ReportCategory.Abuse)

        assertTrue(result.isSuccess)
        assertEquals(1, fake.reportCallCount)
        assertEquals("user-42", fake.lastUserId)
        assertEquals("Doctor did not show up", fake.lastReason)
        assertEquals(ReportCategory.Abuse, fake.lastCategory)
    }

    @Test
    fun success_without_category_passes_null() = runTest {
        val result = useCase("user-1", "Some valid reason here", null)

        assertTrue(result.isSuccess)
        assertEquals(null, fake.lastCategory)
    }

    @Test
    fun reason_too_short_returns_failure_without_calling_datasource() = runTest {
        val result = useCase("user-1", "short", ReportCategory.Spam)

        assertTrue(result.isFailure)
        assertEquals("El motivo debe tener al menos 10 caracteres", result.exceptionOrNull()?.message)
        assertEquals(0, fake.reportCallCount)
    }

    @Test
    fun reason_exactly_10_chars_is_valid() = runTest {
        val result = useCase("user-1", "0123456789", null)

        assertTrue(result.isSuccess)
        assertEquals(1, fake.reportCallCount)
    }

    @Test
    fun reason_over_2000_chars_returns_failure() = runTest {
        val longReason = "a".repeat(2001)
        val result = useCase("user-1", longReason, null)

        assertFalse(result.isSuccess)
        assertEquals("El motivo no puede superar los 2000 caracteres", result.exceptionOrNull()?.message)
        assertEquals(0, fake.reportCallCount)
    }

    @Test
    fun datasource_failure_propagates() = runTest {
        fake.reportResult = Result.failure(Exception("Network error"))
        val result = useCase("user-1", "A valid reason text", null)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
