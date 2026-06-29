package com.inclinic.app.features.doctor.messages.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.messages.core.port.ThreadFilter
import com.inclinic.app.features.doctor.messages.fakes.FakeDoctorMessagesRepository
import com.inclinic.app.features.doctor.messages.fakes.stubThread
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDoctorChatThreadsUseCaseTest {

    private val repo = FakeDoctorMessagesRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetDoctorChatThreadsUseCase(repo, dispatchers)

    @Test
    fun returns_all_threads_with_default_filter() = runTest {
        repo.listThreadsResult = Result.success(listOf(stubThread("t1"), stubThread("t2")))
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
    }

    @Test
    fun filters_unread_threads_when_unread_filter_set() = runTest {
        repo.listThreadsResult = Result.success(listOf(
            stubThread("t1", unread = true),
            stubThread("t2", unread = false),
        ))
        val result = useCase(ThreadFilter.UNREAD)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("t1", result.getOrThrow()[0].id)
    }

    @Test
    fun returns_empty_list_when_no_threads() = runTest {
        repo.listThreadsResult = Result.success(emptyList())
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow())
    }

    @Test
    fun propagates_failure() = runTest {
        repo.listThreadsResult = Result.failure(RuntimeException("500"))
        val result = useCase()
        assertTrue(result.isFailure)
    }

    @Test
    fun calls_list_threads_on_repository() = runTest {
        useCase()
        assertEquals(1, repo.listCallCount)
    }
}
