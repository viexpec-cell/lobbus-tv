package tv.own.owntv.lobbus.prints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import tv.own.owntv.core.theme.AccentColor
import tv.own.owntv.core.theme.ThemeMode
import tv.own.owntv.ui.theme.LocalStagePalette
import tv.own.owntv.ui.theme.OwnTVTheme

/**
 * LOBBUS: a máquina de prints das telas, só em código de teste.
 *
 * Roda o Compose de verdade dentro do Robolectric (runtime do Android na JVM) e grava o que foi
 * desenhado num PNG, sem emulador e sem TV. Serve para o Vitor aprovar cada tela da Fase 4 olhando
 * o print ao lado do protótipo em `docs/lobbus/design/`.
 *
 * Como usar: ver `docs/lobbus/PRINTS.md`. Comando: `./gradlew :app:lobbusPrints`.
 */
object LobbusPrint {

    /** Tamanho do protótipo e da TV do Vitor, em pixels. */
    const val LARGURA_PX = 1920
    const val ALTURA_PX = 1080

    /**
     * Configuração de tela que o Robolectric recebe em `@Config(qualifiers = ...)`.
     *
     * 960×540 dp em xhdpi (densidade 2,0) dá exatamente 1920×1080 px, que é o que uma Google TV
     * 1080p reporta: 320 dpi. `television` e `land` põem o Compose no modo TV, deitado.
     */
    const val QUALIFICADORES = "w960dp-h540dp-land-television-xhdpi"

    /**
     * Densidade usada para desenhar: 2,0 da TV × 0,9 do zoom de interface padrão = 1,8 px por dp.
     * É a mesma conta de `StageTokens` e de `MainActivity`, então 1 pixel do protótipo (`mpx`)
     * vira 1 pixel do print.
     */
    const val DENSIDADE = 1.8f

    /** Pasta de saída, entregue pelo Gradle (`gradle/lobbus-prints.gradle.kts`). */
    val pasta: File
        get() = File(System.getProperty("lobbus.prints.dir") ?: "docs/lobbus/prints")

    /** Falso quando os testes rodam fora da tarefa `lobbusPrints`: aí nenhum PNG é gravado. */
    val habilitado: Boolean
        get() = System.getProperty("lobbus.prints.enabled") == "true"

    /** O arquivo de um print pelo nome, sem extensão: `arquivo("04-filmes")`. */
    fun arquivo(nome: String): File {
        pasta.mkdirs()
        return File(pasta, "$nome.png")
    }
}

/**
 * O tema real do app, como a TV o mostra: escuro, acento laranja da Lobbus, fonte Montserrat,
 * anel de foco de 2 dp e a densidade do protótipo. Toda tela de print passa por aqui.
 *
 * O acento `TEAL` é o padrão de fábrica do OwnTV; depois da Fase 3 todas as opções de acento
 * resolvem para o laranja #F2600C, então o nome herdado não muda a cor.
 */
@Composable
fun TemaDePrint(content: @Composable () -> Unit) {
    OwnTVTheme(
        themeMode = ThemeMode.DARK,
        accent = AccentColor.TEAL,
        systemInDarkTheme = true,
        focusBorderWidthDp = 2,
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(LobbusPrint.DENSIDADE, 1f),
        ) {
            Box(Modifier.fillMaxSize().background(LocalStagePalette.current.page)) {
                content()
            }
        }
    }
}

/**
 * Monta o conteúdo dentro do tema real, no tamanho da TV, e espera o Compose assentar.
 * Separado da gravação para dar tempo de mover o foco antes do print.
 */
fun ComposeContentTestRule.montarTela(conteudo: @Composable () -> Unit) {
    setContent { TemaDePrint { conteudo() } }
    waitForIdle()
}

/**
 * Grava a tela inteira (1920×1080) em `docs/lobbus/prints/<nome>.png`.
 *
 * Fora da tarefa `lobbusPrints` não faz nada, para um `./gradlew test` comum não ficar escrevendo
 * imagem no repositório.
 */
fun ComposeContentTestRule.gravarTela(nome: String) {
    if (!LobbusPrint.habilitado) return
    waitForIdle()
    onRoot().captureRoboImage(LobbusPrint.arquivo(nome))
}

/**
 * Grava só um pedaço da tela: o bloco marcado com `Modifier.testTag(tag)`. Serve para o recorte de
 * um card, de uma fileira ou de um card em foco, sem precisar de outra resolução.
 */
fun ComposeContentTestRule.gravarBloco(nome: String, tag: String) {
    if (!LobbusPrint.habilitado) return
    waitForIdle()
    onNodeWithTag(tag).captureRoboImage(LobbusPrint.arquivo(nome))
}

/** Monta e grava a tela inteira, que é o caso comum. */
fun ComposeContentTestRule.printDaTela(nome: String, conteudo: @Composable () -> Unit) {
    montarTela(conteudo)
    gravarTela(nome)
}

/** Monta e grava só um bloco. */
fun ComposeContentTestRule.printDoBloco(nome: String, tag: String, conteudo: @Composable () -> Unit) {
    montarTela(conteudo)
    gravarBloco(nome, tag)
}
