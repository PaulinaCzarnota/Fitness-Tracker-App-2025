package com.example.fitnesstrackerapp.security

/**
 * Represents the result of a cryptographic operation.
 */
sealed class CryptoResult {
    /** Successful result containing encrypted data and IV, or decrypted data. */
    data class Success(
        val encryptedData: ByteArray? = null,
        val decryptedData: String? = null,
        val iv: ByteArray
    ) : CryptoResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            if (!encryptedData.contentEquals(other.encryptedData)) return false
            if (decryptedData != other.decryptedData) return false
            if (!iv.contentEquals(other.iv)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = encryptedData?.contentHashCode() ?: 0
            result = 31 * result + (decryptedData?.hashCode() ?: 0)
            result = 31 * result + iv.contentHashCode()
            return result
        }
    }

    /** Error result wrapping an exception. */
    data class Error(val exception: Exception) : CryptoResult()
}

