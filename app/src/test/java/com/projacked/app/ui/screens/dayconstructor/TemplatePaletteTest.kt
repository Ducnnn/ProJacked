package com.projacked.app.ui.screens.dayconstructor

import com.projacked.app.ui.components.parseTemplateColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplatePaletteTest {

    @Test
    fun `palette has 16 distinct lowercase colours that parse and exclude white and rest`() {
        val hexes = TemplatePalette.map { it.hex }
        assertEquals(16, hexes.size)
        assertEquals(16, hexes.toSet().size)
        for (hex in hexes) {
            assertTrue(hex, Regex("#[0-9a-f]{6}").matches(hex))
            assertNotNull(hex, parseTemplateColor(hex))
        }
        assertTrue("#ffffff" !in hexes)
        assertTrue("#ffa9a3" !in hexes)
    }

    @Test
    fun `default is the first unused colour`() {
        assertEquals("#e53935", defaultTemplateColor(emptyList()))
        assertEquals("#d81b60", defaultTemplateColor(listOf("#E53935", " #1E88E5 ")))
    }

    @Test
    fun `default is red when all are used and white is ignored`() {
        assertEquals("#e53935", defaultTemplateColor(TemplatePalette.map { it.hex }))
        assertEquals("#e53935", defaultTemplateColor(listOf("#FFFFFF")))
    }
}
