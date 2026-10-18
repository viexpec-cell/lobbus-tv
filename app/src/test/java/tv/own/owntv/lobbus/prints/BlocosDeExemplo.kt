package tv.own.owntv.lobbus.prints

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.tv.material3.Text
import tv.own.owntv.ui.components.OwnTVIcon
import tv.own.owntv.ui.theme.LobbusMonoFamily
import tv.own.owntv.ui.theme.LocalStagePalette
import tv.own.owntv.ui.theme.StageColors
import tv.own.owntv.ui.theme.StageRadii
import tv.own.owntv.ui.theme.mpx
import tv.own.owntv.ui.theme.mpxSp
import tv.own.owntv.ui.theme.stageAccent
import tv.own.owntv.ui.theme.stageText

/**
 * LOBBUS: blocos de exemplo, pequenos de propósito, só para provar que a máquina de prints desenha
 * o tema real do app (cores, Montserrat, Geist Mono, ícones de traço e o anel de foco laranja).
 *
 * Não são as telas da Fase 4 e não ficam no APK: são código de teste. Quando cada tela de verdade
 * existir, o teste de print chama a tela e estes blocos podem sair daqui.
 */

/** Tag do card em foco, usada pelo print de recorte. */
const val TAG_CARD_FOCO = "lobbus-card-foco"

/** Tag da fileira inteira, para o recorte de uma fileira sem o resto da tela. */
const val TAG_FILEIRA = "lobbus-fileira"

/** Rótulo em Geist Mono caixa alta, com o tracking largo do design system. */
@Composable
fun RotuloMono(texto: String, cor: Color = StageColors.Dim, tamanho: Number = 13) {
    Text(
        text = texto,
        color = cor,
        // Tracking de 0,15em do design system, escrito em pixels de projeto como o resto.
        style = stageText(tamanho, 500, letterSpacing = (tamanho.toFloat() * 0.15f).mpxSp),
        fontFamily = LobbusMonoFamily,
    )
}

/** O menu lateral fixo de 128 px, com ícones de traço e rótulo mono embaixo. */
@Composable
fun MenuLateralDeExemplo(itemAtivo: Int = 1, modifier: Modifier = Modifier) {
    val paleta = LocalStagePalette.current
    val acento = stageAccent
    val itens = listOf(
        OwnTVIcon.SEARCH to "BUSCAR",
        OwnTVIcon.HOME to "INÍCIO",
        OwnTVIcon.LIVE_TV to "AO VIVO",
        OwnTVIcon.EPG to "GUIA",
        OwnTVIcon.MOVIES to "FILMES",
        OwnTVIcon.SERIES to "SÉRIES",
        OwnTVIcon.FAVORITE to "MINHA LISTA",
    )
    Column(
        modifier = modifier
            .width(128.mpx)
            .fillMaxHeight()
            .background(paleta.glassTint)
            .padding(vertical = 40.mpx),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(36.mpx),
    ) {
        itens.forEachIndexed { indice, (icone, rotulo) ->
            val ativo = indice == itemAtivo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(64.mpx)
                        .then(
                            if (ativo) {
                                Modifier
                                    .clip(RoundedCornerShape(StageRadii.Plate))
                                    .background(acento.accent.copy(alpha = 0.14f))
                                    .border(2.mpx, acento.accent, RoundedCornerShape(StageRadii.Plate))
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    OwnTVIcon(
                        icon = icone,
                        tint = if (ativo) acento.accent else paleta.navText,
                        modifier = Modifier.size(28.mpx),
                    )
                }
                Spacer(Modifier.height(10.mpx))
                RotuloMono(rotulo, if (ativo) StageColors.Text else paleta.navText, tamanho = 12)
            }
        }
    }
}

/**
 * Um card de "Continuar assistindo": capa chapada, título, origem e barra de progresso.
 *
 * É focável de verdade, então o anel laranja do print vem do foco do controle remoto que o teste
 * pede com `requestFocus()`, e não de um desenho fixo.
 */
@Composable
fun CardEmAndamento(item: EmAndamento, modifier: Modifier = Modifier) {
    val paleta = LocalStagePalette.current
    val acento = stageAccent
    var focado by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .width(378.mpx)
            .onFocusChanged { focado = it.isFocused }
            .focusable()
            .then(
                if (focado) {
                    // Anel sólido de 2 px em laranja, com 3 px de afastamento até o card.
                    Modifier.border(2.mpx, acento.accent, RoundedCornerShape(StageRadii.Poster))
                } else {
                    Modifier
                },
            )
            .padding(3.mpx)
            .clip(RoundedCornerShape(StageRadii.Poster))
            .background(paleta.glassTint),
    ) {
        // Lugar da capa. Imagem de rede não aparece em print (ver PRINTS.md, Limitações), então o
        // bloco de exemplo usa uma cor chapada do tema.
        Box(
            Modifier
                .fillMaxWidth()
                .height(132.mpx)
                .background(Color(0xFF221A14)),
        )
        Column(Modifier.padding(horizontal = 18.mpx, vertical = 14.mpx)) {
            Text(
                text = item.titulo,
                color = StageColors.Text,
                style = stageText(22, if (focado) 800 else 700),
            )
            Spacer(Modifier.height(6.mpx))
            RotuloMono("${item.origem} · ${item.restante}", StageColors.Dim, tamanho = 11)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(5.mpx)
                .background(paleta.segmentOn),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(item.progresso)
                    .fillMaxHeight()
                    .background(acento.accent),
            )
        }
    }
}

/** Título de fileira do design: palavra em laranja, ponto final e o contador em mono. */
@Composable
fun TituloDeFileira(inicio: String, acentuado: String, contador: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(text = inicio, color = StageColors.Text, style = stageText(30, 800))
        Text(text = acentuado, color = stageAccent.accent, style = stageText(30, 800))
        Spacer(Modifier.width(14.mpx))
        RotuloMono(contador, StageColors.Dim, tamanho = 13)
    }
}

/**
 * A fileira "Continuar assistindo" do Início, com o segundo card em foco.
 * [indiceFocado] marca qual card leva a tag do recorte.
 */
@Composable
fun FileiraContinuarAssistindo(indiceFocado: Int = 1, modifier: Modifier = Modifier) {
    Column(modifier.testTag(TAG_FILEIRA)) {
        TituloDeFileira("Continuar ", "assistindo.", "[02] ${CatalogoFalso.continuarAssistindo.size} TÍTULOS")
        Spacer(Modifier.height(20.mpx))
        Row(horizontalArrangement = Arrangement.spacedBy(24.mpx)) {
            CatalogoFalso.continuarAssistindo.forEachIndexed { indice, item ->
                CardEmAndamento(
                    item = item,
                    modifier = if (indice == indiceFocado) Modifier.testTag(TAG_CARD_FOCO) else Modifier,
                )
            }
        }
    }
}

/** A tela de exemplo inteira: menu lateral mais a fileira, nas medidas do protótipo. */
@Composable
fun TelaDeExemplo() {
    Row(Modifier.fillMaxSize()) {
        MenuLateralDeExemplo()
        Column(Modifier.padding(start = 72.mpx, top = 96.mpx, end = 40.mpx)) {
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
            Spacer(Modifier.height(64.mpx))
            Text(
                text = CatalogoFalso.destaque.tituloClaro,
                color = StageColors.Text,
                style = stageText(96, 900),
            )
            Text(
                text = CatalogoFalso.destaque.tituloAcento,
                color = stageAccent.accent,
                style = stageText(96, 900),
            )
            Spacer(Modifier.height(18.mpx))
            RotuloMono(
                CatalogoFalso.destaque.selos.joinToString("   ") { it.rotulo },
                StageColors.Muted,
            )
            Spacer(Modifier.height(72.mpx))
            FileiraContinuarAssistindo()
        }
    }
}
