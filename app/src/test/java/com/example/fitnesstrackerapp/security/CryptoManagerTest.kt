package com.example.fitnesstrackerapp.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for CryptoManager to verify API surface and functionality.
 * These tests lock the public API surface and ensure consistent behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CryptoManagerTest {

    private lateinit var context: Context
    private lateinit var cryptoManager: CryptoManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        cryptoManager = CryptoManager(context)
    }

    @Test
    fun `generateSalt creates non-empty salt`() {
        // When
        val salt = cryptoManager.generateSalt()

        // Then
        assertNotNull(salt)
        assertTrue("Salt should not be empty", salt.isNotEmpty())
        assertEquals("Salt should be 32 bytes", 32, salt.size)
    }

    @Test
    fun `generateSalt creates different salts each time`() {
        // When
        val salt1 = cryptoManager.generateSalt()
        val salt2 = cryptoManager.generateSalt()

        // Then
        assertNotNull(salt1)
        assertNotNull(salt2)
        assertFalse("Salts should be different", salt1.contentEquals(salt2))
    }

    @Test
    fun `hashPassword creates non-empty hash`() {
        // Given
        val password = "testPassword123"
        val salt = cryptoManager.generateSalt()

        // When
        val hash = cryptoManager.hashPassword(password, salt)

        // Then
        assertNotNull(hash)
        assertTrue("Hash should not be empty", hash.isNotEmpty())
        assertEquals("Hash should be 32 bytes (256 bits)", 32, hash.size)
    }

    @Test
    fun `hashPassword creates different hashes for different passwords`() {
        // Given
        val password1 = "password123"
        val password2 = "differentPassword"
        val salt = cryptoManager.generateSalt()

        // When
        val hash1 = cryptoManager.hashPassword(password1, salt)
        val hash2 = cryptoManager.hashPassword(password2, salt)

        // Then
        assertFalse("Different passwords should create different hashes", 
                   hash1.contentEquals(hash2))
    }

    @Test
    fun `hashPassword creates different hashes for different salts`() {
        // Given
        val password = "testPassword"
        val salt1 = cryptoManager.generateSalt()
        val salt2 = cryptoManager.generateSalt()

        // When
        val hash1 = cryptoManager.hashPassword(password, salt1)
        val hash2 = cryptoManager.hashPassword(password, salt2)

        // Then
        assertFalse("Same password with different salts should create different hashes", 
                   hash1.contentEquals(hash2))
    }

    @Test
    fun `verifyPassword returns true for correct password`() {
        // Given
        val password = "correctPassword"
        val salt = cryptoManager.generateSalt()
        val storedHash = cryptoManager.hashPassword(password, salt)

        // When
        val result = cryptoManager.verifyPassword(password, storedHash, salt)

        // Then
        assertTrue("Correct password should be verified", result)
    }

    @Test
    fun `verifyPassword returns false for incorrect password`() {
        // Given
        val correctPassword = "correctPassword"
        val incorrectPassword = "wrongPassword"
        val salt = cryptoManager.generateSalt()
        val storedHash = cryptoManager.hashPassword(correctPassword, salt)

        // When
        val result = cryptoManager.verifyPassword(incorrectPassword, storedHash, salt)

        // Then
        assertFalse("Incorrect password should not be verified", result)
    }

    @Test
    fun `bytesToHex converts bytes to hex string correctly`() {
        // Given
        val bytes = byteArrayOf(0x00, 0x01, 0x0A, 0xFF.toByte())

        // When
        val hex = cryptoManager.bytesToHex(bytes)

        // Then
        assertEquals("00010aff", hex.lowercase())
    }

    @Test
    fun `hexToBytes converts hex string to bytes correctly`() {
        // Given
        val hex = "00010AFF"

        // When
        val bytes = cryptoManager.hexToBytes(hex)

        // Then
        val expected = byteArrayOf(0x00, 0x01, 0x0A, 0xFF.toByte())
        assertArrayEquals(expected, bytes)
    }

    @Test
    fun `bytesToHex and hexToBytes are inverse operations`() {
        // Given
        val originalBytes = cryptoManager.generateSalt()

        // When
        val hex = cryptoManager.bytesToHex(originalBytes)
        val convertedBytes = cryptoManager.hexToBytes(hex)

        // Then
        assertArrayEquals("Conversion should be reversible", originalBytes, convertedBytes)
    }

    @Test
    fun `encrypt creates successful result with encrypted data`() {
        // Given
        val plaintext = "This is a secret message"

        // When
        val result = cryptoManager.encrypt(plaintext)

        // Then
        assertTrue("Encryption should succeed", result is CryptoResult.Success)
        val successResult = result as CryptoResult.Success
        assertNotNull("Encrypted data should not be null", successResult.encryptedData)
        assertNotNull("IV should not be null", successResult.iv)
        assertTrue("Encrypted data should not be empty", successResult.encryptedData!!.isNotEmpty())
        assertTrue("IV should not be empty", successResult.iv!!.isNotEmpty())
    }

    @Test
    fun `encrypt creates different results for same plaintext`() {
        // Given
        val plaintext = "Same message"

        // When
        val result1 = cryptoManager.encrypt(plaintext)
        val result2 = cryptoManager.encrypt(plaintext)

        // Then
        assertTrue("Both encryptions should succeed", 
                  result1 is CryptoResult.Success && result2 is CryptoResult.Success)
        
        val success1 = result1 as CryptoResult.Success
        val success2 = result2 as CryptoResult.Success
        
        assertFalse("Encrypted data should be different", 
                   success1.encryptedData!!.contentEquals(success2.encryptedData!!))
    }

    @Test
    fun `decrypt recovers original plaintext`() {
        // Given
        val originalPlaintext = "Secret message to encrypt and decrypt"
        val encryptResult = cryptoManager.encrypt(originalPlaintext)
        
        assertTrue("Encryption should succeed", encryptResult is CryptoResult.Success)
        val encryptSuccess = encryptResult as CryptoResult.Success

        // When
        val decryptResult = cryptoManager.decrypt(encryptSuccess.encryptedData!!, encryptSuccess.iv!!)

        // Then
        assertTrue("Decryption should succeed", decryptResult is CryptoResult.Success)
        val decryptSuccess = decryptResult as CryptoResult.Success
        assertEquals("Decrypted text should match original", originalPlaintext, decryptSuccess.decryptedData)
    }

    @Test
    fun `decrypt with wrong IV fails`() {
        // Given
        val plaintext = "Secret message"
        val encryptResult = cryptoManager.encrypt(plaintext)
        assertTrue(encryptResult is CryptoResult.Success)
        
        val encryptSuccess = encryptResult as CryptoResult.Success
        val wrongIV = ByteArray(12) { 0x00 } // Wrong IV

        // When
        val decryptResult = cryptoManager.decrypt(encryptSuccess.encryptedData!!, wrongIV)

        // Then
        assertTrue("Decryption with wrong IV should fail", decryptResult is CryptoResult.Error)
    }

    @Test
    fun `encrypt handles empty string`() {
        // Given
        val emptyString = ""

        // When
        val result = cryptoManager.encrypt(emptyString)

        // Then
        assertTrue("Empty string encryption should succeed", result is CryptoResult.Success)
    }

    // API Surface Tests - Ensure methods exist and have correct signatures
    @Test
    fun `CryptoManager API surface test`() {
        // Test that all expected public methods exist with correct signatures
        try {
            // Salt operations
            cryptoManager.generateSalt()
            
            // Password operations
            val salt = ByteArray(32)
            cryptoManager.hashPassword("password", salt)
            val hash = ByteArray(32)
            cryptoManager.verifyPassword("password", hash, salt)
            
            // Conversion operations
            cryptoManager.bytesToHex(ByteArray(16))
            cryptoManager.hexToBytes("00112233")
            
            // Encryption operations
            cryptoManager.encrypt("plaintext")
            cryptoManager.decrypt(ByteArray(16), ByteArray(12))
            
            // Success - API surface is stable
            assertTrue(true)
        } catch (e: NoSuchMethodError) {
            fail("API surface has changed: ${e.message}")
        }
    }

    // Test CryptoResult sealed class
    @Test
    fun `CryptoResult Success has correct properties`() {
        // Given
        val encryptedData = ByteArray(16)
        val iv = ByteArray(12)
        val decryptedData = "test"

        // When
        val successWithEncryption = CryptoResult.Success(encryptedData = encryptedData, iv = iv)
        val successWithDecryption = CryptoResult.Success(decryptedData = decryptedData, iv = iv)

        // Then
        assertEquals(encryptedData, successWithEncryption.encryptedData)
        assertEquals(iv, successWithEncryption.iv)
        assertNull(successWithEncryption.decryptedData)
        
        assertEquals(decryptedData, successWithDecryption.decryptedData)
        assertEquals(iv, successWithDecryption.iv)
        assertNull(successWithDecryption.encryptedData)
    }

    @Test
    fun `CryptoResult Error has exception`() {
        // Given
        val exception = RuntimeException("Test exception")

        // When
        val error = CryptoResult.Error(exception)

        // Then
        assertEquals(exception, error.exception)
    }
}
