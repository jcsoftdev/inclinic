package com.inclinic.app.features.doctor.messages.application

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * MarkThreadReadUseCase is a no-op stub.
 * Read state is managed server-side when GET /api/chats/{partyId} is called.
 * Tests kept minimal to confirm the class is instantiable and does not throw.
 */
class MarkThreadReadUseCaseTest {

    private val useCase = MarkThreadReadUseCase()

    @Test
    fun can_be_instantiated() {
        // No-op use case — just verify it exists and can be constructed
    }
}
