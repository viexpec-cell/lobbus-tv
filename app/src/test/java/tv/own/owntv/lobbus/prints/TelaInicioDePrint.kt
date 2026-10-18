package tv.own.owntv.lobbus.prints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.tv.material3.Text
import tv.own.owntv.lobbus.home.LobbusMetaHeroi
import tv.own.owntv.lobbus.home.LobbusSinopse
import tv.own.owntv.lobbus.home.LobbusSobrancelha
import tv.own.owntv.lobbus.home.LobbusTituloFileira
import tv.own.owntv.lobbus.home.LobbusTituloHeroi
import tv.own.owntv.ui.components.OwnTVIcon
import tv.own.owntv.ui.stage.StageButton
import tv.own.owntv.ui.theme.StageColors
import tv.own.owntv.ui.theme.StageRadii
import tv.own.owntv.ui.theme.mpx
import tv.own.owntv.ui.theme.stageAccent
import tv.own.owntv.ui.theme.stageText

/**
 * LOBBUS: a tela Início montada para o print, na geometria do `HomeScreen`.
 *
 * O destaque é desenhado pelas peças de produção (`lobbus/home/LobbusHeroi.kt`) e pelos botões de
 * produção (`StageButton`), com dados do [CatalogoFalso] no lugar do banco. O menu lateral, a barra
 * de cima e a arte dos cards continuam sendo blocos de exemplo: o menu e a barra são da casca do
 * app, que ainda não foi refeita, e a capa não carrega em print (ver `docs/lobbus/PRINTS.md`).
 *
 * Portanto: tipografia, cor, espaçamento, etiquetas, contador e foco saem iguais aos do app; a arte
 * das capas e a moldura da casca, não.
 */

/** Tag do botão principal, para o print sair com o foco onde o protótipo o mostra. */
const val TAG_BOTAO_ASSISTIR = "lobbus-botao-assistir"

@Composable
fun TelaInicio() {
    val destaque = CatalogoFalso.destaque
    Row(Modifier.fillMaxSize()) {
        MenuLateralDeExemplo()
        Column(Modifier.padding(start = 72.mpx, top = 88.mpx, end = 40.mpx)) {
            BarraDeCima()
            Spacer(Modifier.height(44.mpx))
            LobbusSobrancelha(ordem = 1, rotulo = destaque.rotulo, tipo = destaque.tipo)
            Spacer(Modifier.height(8.mpx))
            // O ponto final é da identidade e quem o põe é o `LobbusTituloHeroi`. O catálogo falso
            // guarda "fogo." com o ponto desde os blocos de exemplo, que o desenhavam à mão, então
            // aqui ele sai antes, senão o título vira "fogo..".
            LobbusTituloHeroi("${destaque.tituloClaro} ${destaque.tituloAcento.removeSuffix(".")}")
            Spacer(Modifier.height(18.mpx))
            LobbusMetaHeroi(
                nota = destaque.nota.toString().replace('.', ','),
                partes = listOf(destaque.ano.toString(), destaque.duracao, destaque.generos),
                classificacao = destaque.classificacao,
                etiquetas = destaque.selos.map { it.rotulo },
            )
            Spacer(Modifier.height(20.mpx))
            LobbusSinopse(destaque.sinopse, maxLinhas = 4)
            Spacer(Modifier.height(36.mpx))
            BotoesDoDestaque()
            Spacer(Modifier.height(64.mpx))
            LobbusTituloFileira(
                titulo = "Continuar assistindo",
                contador = "${CatalogoFalso.continuarAssistindo.size} TÍTULOS",
                ordem = 2,
                altura = 46.mpx,
                inicio = 0.mpx,
            )
            Spacer(Modifier.height(20.mpx))
            Row(horizontalArrangement = Arrangement.spacedBy(24.mpx)) {
                CatalogoFalso.continuarAssistindo.forEachIndexed { indice, item ->
                    CardEmAndamento(
                        item = item,
                        modifier = if (indice == 1) Modifier.testTag(TAG_CARD_FOCO) else Modifier,
                    )
                }
            }
        }
    }
}

/** A marca e o relógio do alto da tela: bloco de exemplo, a casca do app ainda é a do OwnTV. */
@Composable
private fun BarraDeCima() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Lobbus", color = StageColors.Text, style = stageText(34, 900))
        Spacer(Modifier.width(14.mpx))
        Box(
            Modifier
                .clip(RoundedCornerShape(StageRadii.Pill))
                .background(stageAccent.accent)
                .padding(horizontal = 14.mpx, vertical = 4.mpx),
        ) {
            RotuloMono("TV", stageAccent.onAccent, tamanho = 12)
        }
        Spacer(Modifier.width(28.mpx))
        RotuloMono("SEX · 16 OUT · ${CatalogoFalso.hora(CatalogoFalso.AGORA)}", StageColors.Muted)
    }
}

/** Os três botões do destaque, com os componentes de produção. */
@Composable
private fun BotoesDoDestaque() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.mpx),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StageButton(
            text = "Assistir",
            onClick = {},
            icon = OwnTVIcon.PLAY,
            iconFilled = true,
            filled = true,
            modifier = Modifier.testTag(TAG_BOTAO_ASSISTIR),
        )
        StageButton(text = "Mais informações", onClick = {}, icon = OwnTVIcon.INFO)
        StageButton(text = null, onClick = {}, icon = OwnTVIcon.FAVORITE, round = true)
    }
}
