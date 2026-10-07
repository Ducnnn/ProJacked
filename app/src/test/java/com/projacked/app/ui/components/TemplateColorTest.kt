package com.projacked.app.ui.components

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TemplateColorTest {

    @Test
    fun `six digit colours parse in either case to the same colour`() {
        assertEquals(Color(0xFFFF8800), parseTemplateColor("#ff8800"))
        assertEquals(parseTemplateColor("#ff8800"), parseTemplateColor("#FF8800"))
    }

    @Test
    fun `eight digit colours keep their alpha`() {
        assertEquals(Color(0x80FF8800), parseTemplateColor("#80FF8800"))
    }

    @Test
    fun `anything else is null`() {
        for (bad in listOf("ff8800", "#12", "red", "", "#GGGGGG", "#ff88001")) {
            assertNull(bad, parseTemplateColor(bad))
        }
    }
}
