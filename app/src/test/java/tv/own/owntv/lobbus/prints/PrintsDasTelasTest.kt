package tv.own.owntv.lobbus.prints

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * LOBBUS: os prints das telas. Um teste por imagem.
 *
 * `./gradlew :app:lobbusPrints` roda só esta classe e grava os PNGs em `docs/lobbus/prints/`.
 * Fora dessa tarefa os testes rodam, montam a tela e não gravam nada, o que também serve de
 * verificação barata de que as telas compilam e compõem sem explodir.
 *
 * Como acrescentar um print de uma tela nova: ver `docs/lobbus/PRINTS.md`.
 */
@RunWith(AndroidJUnit4::class)
// Gráficos nativos: o Robolectric desenha de verdade (fontes, gradientes, sombras). Sem isso o
// bitmap sai em branco.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// SDK 37 é o compileSdk do app e o Robolectric 4.17 tem o runtime dele. Se um dia faltar o
// android-all dessa API, baixe para `sdk = [36]` e anote em PRINTS.md.
//
// `application`: um Application vazio, não o `OwnTVApp` do manifesto. O arranque de verdade sobe
// Koin, banco, player e bibliotecas nativas, e derruba a JVM do teste (saída 2, sem stack). Um
// print só precisa do Compose e do tema, então o teste não boota o app.
@Config(
    sdk = [37],
    qualifiers = LobbusPrint.QUALIFICADORES,
    application = android.app.Application::class,
)
class PrintsDasTelasTest {

    @get:Rule
    val compose = createComposeRule()

    /** Tela inteira, 1920×1080, para comparar lado a lado com `docs/lobbus/design/`. */
    @Test
    fun telaDeExemplo() {
        compose.printDaTela("exemplo-01-tela-1920x1080") {
            TelaDeExemplo()
        }
    }

    /** Recorte: só a fileira "Continuar assistindo", sem o resto da tela. */
    @Test
    fun recorteDaFileira() {
        compose.printDoBloco("exemplo-02-fileira", TAG_FILEIRA) {
            TelaDeExemplo()
        }
    }

    /**
     * Recorte do card com o foco do controle remoto em cima: o anel laranja do print vem do foco
     * de verdade, pedido aqui com `requestFocus()`, não de um desenho fixo.
     */
    @Test
    fun cardComAnelDeFoco() {
        compose.montarTela { TelaDeExemplo() }
        compose.onNodeWithTag(TAG_CARD_FOCO).requestFocus()
        compose.gravarBloco("exemplo-03-card-em-foco", TAG_CARD_FOCO)
    }
}
