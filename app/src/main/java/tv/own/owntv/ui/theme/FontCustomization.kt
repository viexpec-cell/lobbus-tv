package tv.own.owntv.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import tv.own.owntv.R
import tv.own.owntv.core.theme.AppFontFamily

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun variableFont(resourceId: Int, weight: FontWeight, style: FontStyle = FontStyle.Normal) =
    Font(
        resourceId,
        weight = weight,
        style = style,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
    )

private val LoraFamily = FontFamily(
    variableFont(R.font.lora_variable, FontWeight.Normal),
    variableFont(R.font.lora_variable, FontWeight.Medium),
    variableFont(R.font.lora_variable, FontWeight.SemiBold),
    variableFont(R.font.lora_variable, FontWeight.Bold),
    variableFont(R.font.lora_italic_variable, FontWeight.Normal, FontStyle.Italic),
    variableFont(R.font.lora_italic_variable, FontWeight.Bold, FontStyle.Italic),
)

private val PlayfairDisplayFamily = FontFamily(
    variableFont(R.font.playfair_display_variable, FontWeight.Normal),
    variableFont(R.font.playfair_display_variable, FontWeight.Medium),
    variableFont(R.font.playfair_display_variable, FontWeight.SemiBold),
    variableFont(R.font.playfair_display_variable, FontWeight.Bold),
    variableFont(R.font.playfair_display_italic_variable, FontWeight.Normal, FontStyle.Italic),
    variableFont(R.font.playfair_display_italic_variable, FontWeight.Bold, FontStyle.Italic),
)

private val DancingScriptFamily = FontFamily(
    variableFont(R.font.dancing_script_variable, FontWeight.Normal),
    variableFont(R.font.dancing_script_variable, FontWeight.Medium),
    variableFont(R.font.dancing_script_variable, FontWeight.SemiBold),
    variableFont(R.font.dancing_script_variable, FontWeight.Bold),
)

// LOBBUS: a fonte padrão (a opção "Plus Jakarta Sans" do OwnTV) passa a ser a Montserrat da Lobbus,
// em arquivos estáticos de 400 a 900. As outras fontes escolhíveis continuam iguais.
private val PlusJakartaSansFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_extrabold, FontWeight.ExtraBold),
    Font(R.font.montserrat_black, FontWeight.Black),
)

/** LOBBUS: Geist Mono, a fonte dos rótulos técnicos do design system (caixa alta, espaçada). */
val LobbusMonoFamily = FontFamily(
    Font(R.font.geist_mono_regular, FontWeight.Normal),
    Font(R.font.geist_mono_medium, FontWeight.Medium),
)

private val PoppinsFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

fun AppFontFamily.asComposeFamily(): FontFamily = when (this) {
    AppFontFamily.LORA -> LoraFamily
    AppFontFamily.SYSTEM_SANS -> FontFamily.SansSerif
    AppFontFamily.MONOSPACE -> FontFamily.Monospace
    AppFontFamily.PLAYFAIR_DISPLAY -> PlayfairDisplayFamily
    AppFontFamily.DANCING_SCRIPT -> DancingScriptFamily
    AppFontFamily.POPPINS -> PoppinsFamily
    AppFontFamily.PLUS_JAKARTA_SANS -> PlusJakartaSansFamily
}

/** Android/mpv equivalents used by subtitle renderers outside Compose typography. */
fun AppFontFamily.asAndroidTypeface(context: android.content.Context): android.graphics.Typeface = when (this) {
    AppFontFamily.SYSTEM_SANS -> android.graphics.Typeface.SANS_SERIF
    AppFontFamily.MONOSPACE -> android.graphics.Typeface.MONOSPACE
    else -> androidx.core.content.res.ResourcesCompat.getFont(context, subtitleFontResource)
        ?: android.graphics.Typeface.SANS_SERIF
}

val AppFontFamily.subtitleFontResource: Int
    get() = when (this) {
        AppFontFamily.LORA -> R.font.lora_variable
        AppFontFamily.PLAYFAIR_DISPLAY -> R.font.playfair_display_variable
        AppFontFamily.DANCING_SCRIPT -> R.font.dancing_script_variable
        AppFontFamily.POPPINS -> R.font.poppins_regular
        AppFontFamily.PLUS_JAKARTA_SANS -> R.font.montserrat_semibold // LOBBUS: Montserrat
        AppFontFamily.SYSTEM_SANS,
        AppFontFamily.MONOSPACE,
        -> 0
    }

val LocalMainFontFamily = staticCompositionLocalOf<FontFamily> { PlusJakartaSansFamily }
val LocalPopupFontFamily = staticCompositionLocalOf<FontFamily> { PlusJakartaSansFamily }
val LocalUiFontScaleFactor = staticCompositionLocalOf { 1f }
val LocalPopupFontScaleFactor = staticCompositionLocalOf { 1f }
val LocalPopupSizeScaleFactor = staticCompositionLocalOf { 1f }
