package com.manmusic.videoplayer

import com.manmusic.videoplayer.ui.theme.ManMusicThemePreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying all 20 Material 3 themes, AMOLED contrast rules,
 * and Instagram identity badge accessibility.
 */
class ThemeAndProfileArchitectureTest {

    @Test
    fun themes_containExactlyTwentyPalettes() {
        val presets = ManMusicThemePreset.values()
        assertEquals("Must contain exactly 20 Material 3 themes", 20, presets.size)

        val themeNames = presets.map { it.themeName }
        assertTrue(themeNames.contains("AMOLED Midnight"))
        assertTrue(themeNames.contains("Neon Cyberpunk"))
        assertTrue(themeNames.contains("Ocean Blue"))
        assertTrue(themeNames.contains("Aurora Glow"))
        assertTrue(themeNames.contains("Sunset Vibes"))
        assertTrue(themeNames.contains("Royal Purple"))
        assertTrue(themeNames.contains("Emerald Forest"))
        assertTrue(themeNames.contains("Ruby Red"))
        assertTrue(themeNames.contains("Golden Luxury"))
        assertTrue(themeNames.contains("Arctic Ice"))
        assertTrue(themeNames.contains("Rose Pink"))
        assertTrue(themeNames.contains("Cyber Lime"))
        assertTrue(themeNames.contains("Sapphire Glass"))
        assertTrue(themeNames.contains("Cherry Blossom"))
        assertTrue(themeNames.contains("Volcano Ember"))
        assertTrue(themeNames.contains("Desert Sand"))
        assertTrue(themeNames.contains("Monochrome Pro"))
        assertTrue(themeNames.contains("Retro Synthwave"))
        assertTrue(themeNames.contains("Mint Fresh"))
        assertTrue(themeNames.contains("Cosmic Galaxy"))
    }

    @Test
    fun amoledMidnight_isGenuinelyPitchDark() {
        val amoled = ManMusicThemePreset.AMOLED_MIDNIGHT
        assertTrue(amoled.isDark)
        assertEquals("AMOLED Midnight must use pitch black", 0xFF000000.toLong(), amoled.background.value.toLong())
    }

    @Test
    fun instagramLabelContrast_isSpecifiedForEveryTheme() {
        for (preset in ManMusicThemePreset.values()) {
            assertNotNull(preset.instagramBadgeBg)
            assertNotNull(preset.instagramBadgeBorder)
            assertNotNull(preset.instagramBadgeText)
            assertTrue(
                "Theme ${preset.themeName} text must contrast with its badge background",
                preset.instagramBadgeText.value != preset.instagramBadgeBg.value
            )
        }
    }
}