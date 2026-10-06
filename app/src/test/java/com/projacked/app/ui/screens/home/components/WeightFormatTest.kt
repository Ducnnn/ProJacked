package com.projacked.app.ui.screens.home.components

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class WeightFormatTest {

    @Test
    fun `whole weight has no decimals`() {
        assertEquals("80", formatWeight(80.0, Locale.US))
    }

    @Test
    fun `fractional weight keeps its decimals`() {
        assertEquals("22.5", formatWeight(22.5, Locale.US))
    }

    @Test
    fun `German locale uses a decimal comma`() {
        assertEquals("22,5", formatWeight(22.5, Locale.GERMANY))
    }
}
