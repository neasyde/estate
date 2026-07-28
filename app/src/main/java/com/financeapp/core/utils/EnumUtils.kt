package com.financeapp.core.utils

inline fun <reified T : Enum<T>> safeValueOf(value: String?, default: T): T {
    return try {
        value?.let { enumValueOf<T>(it) } ?: default
    } catch (e: IllegalArgumentException) {
        default
    }
}
