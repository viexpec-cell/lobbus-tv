package tv.own.owntv.ui.theme

import androidx.compose.ui.graphics.Color
import tv.own.owntv.core.theme.AccentColor
import tv.own.owntv.core.theme.OwnTVPalette

/**
 * Material 3 tonal palette for OwnTV (teal-seeded). NEUTRAL + secondary/tertiary roles are
 * theme-only; the `primary` roles are seeded per [AccentColor] (default teal == these values).
 *
 * Dark uses a near-black background (#040e0b) so the panel colours pop against
 * the deep dark surface while keeping a subtle green undertone.
 *
 * **The values themselves live in core**, in [OwnTVPalette], because the mobile app renders the
 * same product and a second copy of the hex codes would drift. This file only wraps them in
 * Compose's [Color], which core cannot do — it carries the Compose runtime, not `compose-ui`.
 */

// Brand mark color (the OwnTV play logo) — constant.
val AccentCyan = Color(OwnTVPalette.AccentCyan)

// ---------------- DARK ----------------
// LOBBUS: neutros quentes do design system da Lobbus (grafite amarronzado, nunca cinza frio) no lugar
// dos neutros esverdeados do OwnTV (OwnTVPalette). Os nomes ficam iguais para não mexer nas telas.
val DarkBackground = Color(0xFF0D0906)            // ink
val DarkSurface = Color(0xFF110C08)
val DarkSurfaceContainerLowest = Color(0xFF0A0705)
val DarkSurfaceContainerLow = Color(0xFF15100C)   // ink-2
val DarkSurfaceContainer = Color(0xFF1A140F)
val DarkSurfaceContainerHigh = Color(0xFF221A14)  // graphite
val DarkSurfaceContainerHighest = Color(0xFF2E241C) // graphite-2
val DarkOnSurface = Color(0xFFF6EFE9)             // text
val DarkOnSurfaceVariant = Color(0xFFAB9C8F)      // muted
val DarkOutline = Color(0xFF5A4C40)
val DarkOutlineVariant = Color(0xFF2E241C)
val DarkSecondary = Color(0xFFFFE9DB)             // amber
val DarkOnSecondary = Color(0xFF140C06)
val DarkSecondaryContainer = Color(0xFF3A2C21)
val DarkOnSecondaryContainer = Color(0xFFFFE9DB)
val DarkTertiary = Color(0xFFFF7A2E)              // orange-2
val DarkOnTertiary = Color(0xFF140C06)
val DarkTertiaryContainer = Color(0xFF4A2410)
val DarkOnTertiaryContainer = Color(0xFFFFE9DB)
val DarkError = Color(0xFFFF5A45)                 // danger

// ---------------- LIGHT (M3 light) ----------------
val LightBackground = Color(OwnTVPalette.LightBackground)
val LightSurface = Color(OwnTVPalette.LightSurface)
val LightSurfaceContainerLowest = Color(OwnTVPalette.LightSurfaceContainerLowest)
val LightSurfaceContainerLow = Color(OwnTVPalette.LightSurfaceContainerLow)
val LightSurfaceContainer = Color(OwnTVPalette.LightSurfaceContainer)
val LightSurfaceContainerHigh = Color(OwnTVPalette.LightSurfaceContainerHigh)
val LightSurfaceContainerHighest = Color(OwnTVPalette.LightSurfaceContainerHighest)
val LightOnSurface = Color(OwnTVPalette.LightOnSurface)
val LightOnSurfaceVariant = Color(OwnTVPalette.LightOnSurfaceVariant)
val LightOutline = Color(OwnTVPalette.LightOutline)
val LightOutlineVariant = Color(OwnTVPalette.LightOutlineVariant)
val LightSecondary = Color(OwnTVPalette.LightSecondary)
val LightOnSecondary = Color(OwnTVPalette.LightOnSecondary)
val LightSecondaryContainer = Color(OwnTVPalette.LightSecondaryContainer)
val LightOnSecondaryContainer = Color(OwnTVPalette.LightOnSecondaryContainer)
val LightTertiary = Color(OwnTVPalette.LightTertiary)
val LightOnTertiary = Color(OwnTVPalette.LightOnTertiary)
val LightTertiaryContainer = Color(OwnTVPalette.LightTertiaryContainer)
val LightOnTertiaryContainer = Color(OwnTVPalette.LightOnTertiaryContainer)
val LightError = Color(OwnTVPalette.LightError)
