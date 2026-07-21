package com.inclinic.app.core.error

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThrowableUserMessageTest {

    @Test
    fun isNotFoundError_true_for_ApiError_NotFound() {
        assertTrue(ApiError.NotFound.isNotFoundError())
    }

    @Test
    fun isNotFoundError_false_for_other_ApiError_variants() {
        assertFalse(ApiError.Network.isNotFoundError())
        assertFalse(ApiError.Timeout.isNotFoundError())
        assertFalse(ApiError.Unauthorized.isNotFoundError())
        assertFalse(ApiError.Server(500).isNotFoundError())
    }

    @Test
    fun isNotFoundError_false_for_unrelated_throwables() {
        assertFalse(RuntimeException("boom").isNotFoundError())
        assertFalse(IllegalStateException().isNotFoundError())
    }

    // Note: the ClientRequestException(404) branch (raw Ktor 404s from data sources
    // that runCatching { client.get {...} } directly, e.g. KtorAdminDataSource's
    // by-id lookups) needs a live HttpResponse to construct and isn't covered by a
    // unit test here — verified by full-suite compile + the admin detail screens'
    // manual gap description.
}
