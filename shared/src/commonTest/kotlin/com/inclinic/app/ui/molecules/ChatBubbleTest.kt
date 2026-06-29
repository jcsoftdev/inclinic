package com.inclinic.app.ui.molecules

import androidx.compose.ui.Alignment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * RED → GREEN tests for [chatBubbleAlignment].
 *
 * Pure helper extracted from ChatBubble to validate alignment logic without
 * a Compose runtime.
 */
class ChatBubbleTest {

    @Test
    fun mine_bubble_aligns_to_end() {
        assertEquals(Alignment.End, chatBubbleAlignment(isMine = true))
    }

    @Test
    fun other_bubble_aligns_to_start() {
        assertEquals(Alignment.Start, chatBubbleAlignment(isMine = false))
    }

    @Test
    fun mine_and_other_alignment_are_distinct() {
        assertNotEquals(
            chatBubbleAlignment(isMine = true),
            chatBubbleAlignment(isMine = false),
        )
    }
}
