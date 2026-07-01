package com.financeapp.core.utils

import java.security.MessageDigest

object PinHasher {
    fun hash(pin: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
