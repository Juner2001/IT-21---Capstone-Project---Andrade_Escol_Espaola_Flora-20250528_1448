package com.example.ecoguard

import android.util.Base64
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility object for AES encryption/decryption using a passphrase-derived key.
 * Stores IV + ciphertext as Base64.
 */
object EncryptionUtil {
    private const val SECRET_PASSPHRASE = "mysecretkey"            // Change this to your secret
    private const val TRANSFORMATION   = "AES/CBC/PKCS5Padding"
    private const val CHARSET_NAME     = "UTF-8"

    // Derive a 16-byte AES key from the passphrase via SHA-256
    private fun deriveKey(passphrase: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash   = digest.digest(passphrase.toByteArray(Charset.forName(CHARSET_NAME)))
        val keyBytes = hash.copyOf(16)
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts plainText and returns Base64(IV + ciphertext)
     */
    fun encrypt(plainText: String): String {
        val keySpec = deriveKey(SECRET_PASSPHRASE)
        val iv = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val ivSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(plainText.toByteArray(Charset.forName(CHARSET_NAME)))

        // Prepend IV to ciphertext and Base64 encode
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts a Base64(IV + ciphertext) string
     */
    fun decrypt(base64CipherText: String): String {
        val allBytes = Base64.decode(base64CipherText, Base64.NO_WRAP)
        val iv = allBytes.copyOfRange(0, 16)
        val cipherBytes = allBytes.copyOfRange(16, allBytes.size)
        val ivSpec = IvParameterSpec(iv)
        val keySpec = deriveKey(SECRET_PASSPHRASE)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        val decrypted = cipher.doFinal(cipherBytes)
        return String(decrypted, Charset.forName(CHARSET_NAME))
    }
}