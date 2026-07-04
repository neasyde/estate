package com.financeapp.core.ui.icons

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IconSearchTest {
    @Test fun matchesRussianKeyword() {
        assertThat(iconMatches("restaurant", "еда")).isTrue()
        assertThat(iconMatches("directions_car", "машина")).isTrue()
    }

    @Test fun matchesEnglishName() {
        assertThat(iconMatches("restaurant", "rest")).isTrue()
    }

    @Test fun blankMatchesAll() {
        assertThat(iconMatches("wallet", "  ")).isTrue()
    }

    @Test fun noMatch() {
        assertThat(iconMatches("restaurant", "самолёт")).isFalse()
    }
}
