package com.inclinic.app.testutil

import com.inclinic.app.core.secure.SecureStorage

/**
 * In-memory implementation of [SecureStorage] for unit tests.
 * Single-threaded by design — no Mutex needed.
 */
class FakeSecureStorage : SecureStorage {
    private val store = mutableMapOf<String, String>()

    override suspend fun putString(key: String, value: String) {
        store[key] = value
    }

    override suspend fun getString(key: String): String? = store[key]

    override suspend fun remove(key: String) {
        store.remove(key)
    }

    override suspend fun clearAll() {
        store.clear()
    }
}
