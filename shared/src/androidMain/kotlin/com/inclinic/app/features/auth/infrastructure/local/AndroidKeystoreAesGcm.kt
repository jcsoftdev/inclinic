package com.inclinic.app.features.auth.infrastructure.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Internal AES-256-GCM helper backed by the Android Keystore.
 *
 * Key lifecycle:
 * - Key is generated on first use and stored permanently in the Android Keystore
 *   under [KEY_ALIAS]. Subsequent calls reuse the existing key.
 * - `setUserAuthenticationRequired(false)` — no biometric required in v1.
 *   Track in tech debt if per-token biometric gate is needed in the future.
 * - `setRandomizedEncryptionRequired(true)` — OS enforces a fresh random IV per encrypt.
 *
 * Wire format: `IV (12 bytes) || GCM ciphertext+tag (n + 16 bytes)`.
 * GCM tag length: 128 bits (maximum, recommended).
 *
 * Threading: not thread-safe on its own — callers (EncryptedDataStoreSettings) serialise
 * access via DataStore's single-writer guarantee.
 */
internal class AndroidKeystoreAesGcm {

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }
    private val random = SecureRandom()

    /** Returns the AES key, generating it if it doesn't exist yet. */
    private fun getOrCreateKey(): SecretKey {
        val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // v1: no biometric gate
            .setRandomizedEncryptionRequired(true) // OS enforces unique IV per encrypt
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            .also { it.init(spec) }
            .generateKey()
    }

    /**
     * Encrypts [plaintext] and returns `IV (12 bytes) || ciphertext+tag`.
     * A fresh IV is generated per call via [SecureRandom].
     */
    fun encrypt(plaintext: ByteArray): ByteArray {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv // OS generated (randomized encryption required)
        val ciphertext = cipher.doFinal(plaintext)

        // Wire format: 12 IV bytes || ciphertext+tag
        return iv + ciphertext
    }

    /**
     * Decrypts a blob produced by [encrypt].
     * Splits the first 12 bytes as IV, remainder as ciphertext+tag.
     */
    fun decrypt(blob: ByteArray): ByteArray {
        require(blob.size > IV_LENGTH_BYTES) { "Blob too short to contain IV + ciphertext" }

        val iv = blob.copyOfRange(0, IV_LENGTH_BYTES)
        val ciphertext = blob.copyOfRange(IV_LENGTH_BYTES, blob.size)

        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

        return cipher.doFinal(ciphertext)
    }

    companion object {
        const val KEY_ALIAS = "com.inclinic.app.auth.tokens"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
    }
}
