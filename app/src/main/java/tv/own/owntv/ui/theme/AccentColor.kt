package tv.own.owntv.ui.theme

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import tv.own.owntv.R
import tv.own.owntv.core.theme.AccentColor

/**
 * The display label each [AccentColor] carries, and its tonal palette as Compose colours.
 *
 * The tonal values live in core (`AccentColor.roles(isDark)`) so the mobile app seeds its scheme
 * from the same table; only the label — a resource, and therefore per-app — is here. Neutrals
 * (background, surface containers, text, outline) are theme-only and live in [OwnTVColors].
 */
// LOBBUS: as cores prontas do OwnTV viram todas o laranja da Lobbus (nome e tons), para a interface
// nunca mostrar uma cor que o app não usa. Cor personalizada em hex continua possível.
val AccentColor.labelRes: Int
    @StringRes get() = R.string.lobbus_accent_orange

fun AccentColor.primary(isDark: Boolean) = Color(0xFFF2600C)
fun AccentColor.onPrimary(isDark: Boolean) = Color(0xFF140C06)
fun AccentColor.primaryContainer(isDark: Boolean) = if (isDark) Color(0xFF4A2410) else Color(0xFFFFE9DB)
fun AccentColor.onPrimaryContainer(isDark: Boolean) = if (isDark) Color(0xFFFFE9DB) else Color(0xFF6B2A05)
