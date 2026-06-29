@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.moderation

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.moderation.application.BlockUserUseCase
import com.inclinic.app.features.patient.moderation.core.model.ReportCategory
import com.inclinic.app.features.patient.moderation.infrastructure.remote.ModerationRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ── Fake ─────────────────────────────────────────────────────────────────────

private class FakeBlockDataSource : ModerationRemoteDataSource {
    var blockResult: Result<Unit> = Result.success(Unit)
    var lastUserId: String? = null
    var lastReason: String? = null
    var blockCallCount = 0

    override suspend fun reportUser(userId: String, reason: String, category: ReportCategory?) = Result.success(Unit)

    override suspend fun blockUser(userId: String, reason: String?): Result<Unit> {
        blockCallCount++
        lastUserId = userId
        lastReason = reason
        return blockResult
    }

    override suspend fun unblockUser(userId: String) = Result.success(Unit)
}

// ── Tests ─────────────────────────────────────────────────────────────────────

class BlockUserUseCaseTest {

    private val fake = FakeBlockDataSource()
    private val useCase = BlockUserUseCase(fake, TestAppDispatchers())

    @Test
    fun success_with_no_reason_passes_null_to_datasource() = runTest {
        val result = useCase("user-42", null)

        assertTrue(result.isSuccess)
        assertEquals(1, fake.blockCallCount)
        assertEquals("user-42", fake.lastUserId)
        assertNull(fake.lastReason)
    }

    @Test
    fun success_with_optional_reason() = runTest {
        val result = useCase("user-42", "Mensajes insistentes")

        assertTrue(result.isSuccess)
        assertEquals("Mensajes insistentes", fake.lastReason)
    }

    @Test
    fun blank_reason_is_normalized_to_null() = runTest {
        val result = useCase("user-1", "   ")

        assertTrue(result.isSuccess)
        assertNull(fake.lastReason)
    }

    @Test
    fun reason_over_500_chars_returns_failure_without_calling_datasource() = runTest {
        val longReason = "x".repeat(501)
        val result = useCase("user-1", longReason)

        assertFalse(result.isSuccess)
        assertEquals("El motivo no puede superar los 500 caracteres", result.exceptionOrNull()?.message)
        assertEquals(0, fake.blockCallCount)
    }

    @Test
    fun reason_exactly_500_chars_is_valid() = runTest {
        val result = useCase("user-1", "a".repeat(500))

        assertTrue(result.isSuccess)
        assertEquals(1, fake.blockCallCount)
    }

    @Test
    fun datasource_failure_propagates() = runTest {
        fake.blockResult = Result.failure(Exception("Server error"))
        val result = useCase("user-1", null)

        assertTrue(result.isFailure)
        assertEquals("Server error", result.exceptionOrNull()?.message)
    }
}
