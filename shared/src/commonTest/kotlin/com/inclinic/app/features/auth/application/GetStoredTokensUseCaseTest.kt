package com.inclinic.app.features.auth.application

import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.fakes.FakeTokenStorage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetStoredTokensUseCaseTest {

    private val fakeStorage = FakeTokenStorage()
    private val dispatchers = TestAppDispatchers()

    private val useCase = GetStoredTokensUseCase(
        tokenStorage = fakeStorage,
        dispatchers = dispatchers,
    )

    @Test
    fun returns_null_when_storage_is_empty() = runTest {
        val result = useCase()
        assertNull(result)
    }

    @Test
    fun returns_tokens_when_present() = runTest {
        val tokens = AuthTokens("access-123", "refresh-456")
        fakeStorage.save(tokens)

        val result = useCase()

        assertEquals(tokens, result)
    }

    @Test
    fun read_is_non_destructive_calling_twice_returns_same_tokens() = runTest {
        val tokens = AuthTokens("access-abc", "refresh-xyz")
        fakeStorage.save(tokens)

        val first = useCase()
        val second = useCase()

        assertEquals(first, second)
        assertEquals(tokens, fakeStorage.current)
    }

    @Test
    fun read_does_not_mutate_storage() = runTest {
        val tokens = AuthTokens("a", "r")
        fakeStorage.save(tokens)

        useCase()

        assertEquals(tokens, fakeStorage.current)
        assertEquals(0, fakeStorage.clearCallCount)
        assertEquals(1, fakeStorage.saveCallCount) // only the initial save
    }
}
