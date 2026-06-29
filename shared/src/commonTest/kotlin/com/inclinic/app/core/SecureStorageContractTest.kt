package com.inclinic.app.core

import com.inclinic.app.testutil.FakeSecureStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Contract tests for [SecureStorage].
 * Run against [FakeSecureStorage] — any real implementation can be verified
 * against this same contract by substituting the fixture.
 */
class SecureStorageContractTest {

    private val storage = FakeSecureStorage()

    @Test
    fun put_then_get_returns_same_value() = runTest {
        storage.putString("key1", "value1")
        assertEquals("value1", storage.getString("key1"))
    }

    @Test
    fun get_missing_key_returns_null() = runTest {
        assertNull(storage.getString("nonexistent"))
    }

    @Test
    fun remove_then_get_returns_null() = runTest {
        storage.putString("key1", "value1")
        storage.remove("key1")
        assertNull(storage.getString("key1"))
    }

    @Test
    fun remove_nonexistent_key_does_not_throw() = runTest {
        storage.remove("ghost") // should not throw
    }

    @Test
    fun clearAll_makes_all_keys_return_null() = runTest {
        storage.putString("a", "1")
        storage.putString("b", "2")
        storage.putString("c", "3")

        storage.clearAll()

        assertNull(storage.getString("a"))
        assertNull(storage.getString("b"))
        assertNull(storage.getString("c"))
    }

    @Test
    fun overwrite_key_returns_new_value() = runTest {
        storage.putString("key", "old")
        storage.putString("key", "new")
        assertEquals("new", storage.getString("key"))
    }
}
