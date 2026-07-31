package com.noteflow.app.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Simple local password hashing for per-note locks. No password is ever stored
 * in plain text — only a random salt and the SHA-256 hash of (salt + password).
 * There is no length limit on the password, per user request.
 */
object PasswordUtils {

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(Base64.decode(salt, Base64.NO_WRAP))
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        if (password.isEmpty()) return false
        return hash(password, salt) == expectedHash
    }
}
