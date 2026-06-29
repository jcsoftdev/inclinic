package com.inclinic.app.features.patient.chat.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatPollingService(
    private val doctorId: String,
    private val getMessages: suspend () -> Result<List<ChatMessage>>,
    private val dispatchers: AppDispatchers,
) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private var job: Job? = null
    private var consecutiveErrors = 0

    fun start(scope: CoroutineScope) {
        job = scope.launch(dispatchers.io) {
            while (isActive) {
                val result = getMessages()
                if (result.isSuccess) {
                    consecutiveErrors = 0
                    _messages.value = result.getOrThrow()
                    delay(POLL_INTERVAL_MS)
                } else {
                    consecutiveErrors++
                    delay(backoffDelay())
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun backoffDelay(): Long = when (consecutiveErrors) {
        1 -> 5_000L
        2 -> 10_000L
        3 -> 20_000L
        else -> 60_000L
    }

    companion object {
        private const val POLL_INTERVAL_MS = 5_000L
    }
}
