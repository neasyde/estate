package com.financeapp.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PinHasherTest {
    @Test fun hashesKnownValue() {
        // SHA-256("1234")
        assertThat(PinHasher.hash("1234"))
            .isEqualTo("03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4")
    }

    @Test fun differentPinsDiffer() {
        assertThat(PinHasher.hash("0000")).isNotEqualTo(PinHasher.hash("0001"))
    }
}
