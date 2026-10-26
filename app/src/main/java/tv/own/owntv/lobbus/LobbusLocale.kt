package tv.own.owntv.lobbus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.intl.Locale as LocaleDoCompose
import java.util.Locale

/**
 * LOBBUS: o idioma da interface, lido de um jeito que o Compose acompanha.
 *
 * As telas da Lobbus põem rótulos em caixa alta o tempo todo, e caixa alta depende de idioma (o
 * caso clássico é o turco, onde "i" vira "İ"). Usar `Locale.getDefault()` dentro de um composable
 * lê o idioma por fora do Compose: se o usuário troca o idioma da TV, a tela continua desenhada com
 * o idioma velho até ser recomposta por outro motivo. O lint do Compose rejeita isso
 * (`NonObservableLocale`), e com razão.
 */
@Composable
@ReadOnlyComposable
fun lobbusLocale(): Locale = Locale.forLanguageTag(LocaleDoCompose.current.toLanguageTag())
