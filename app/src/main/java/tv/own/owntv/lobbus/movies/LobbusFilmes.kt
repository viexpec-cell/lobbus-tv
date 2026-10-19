package tv.own.owntv.lobbus.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.Text
import tv.own.owntv.lobbus.home.LobbusRotuloMono
import tv.own.owntv.ui.theme.StageColors
import tv.own.owntv.ui.theme.StageRadii
import tv.own.owntv.ui.theme.mpx
import tv.own.owntv.ui.theme.mpxSp
import tv.own.owntv.ui.theme.stageAccent
import tv.own.owntv.ui.theme.stageText

/**
 * LOBBUS: as peças de assinatura da tela Filmes, do protótipo aprovado
 * (`docs/lobbus/design/LobbusTV-04-filmes.jpg`): o Top 10 de números vazados e a fileira de títulos
 * em 4K, HDR e Dolby Atmos.
 *
 * Mesma regra das outras telas: sem estado, só desenho. Quem sabe de catálogo, paginação e foco
 * continua sendo `features/movies`.
 */

/**
 * O número vazado do Top 10: o algarismo em contorno, grande, atrás e à esquerda do cartaz.
 *
 * O contorno é desenhado pelo próprio texto (`Stroke`), não por uma imagem, então ele acompanha a
 * Montserrat e qualquer tamanho de tela sem perder nitidez.
 */
@Composable
fun LobbusNumeroVazado(numero: Int, tamanho: Int = 240, modifier: Modifier = Modifier) {
    val contorno = with(LocalDensity.current) { 3.mpx.toPx() }
    Text(
        text = numero.toString(),
        style = stageText(tamanho, 800, (-6).mpxSp).copy(
            drawStyle = Stroke(width = contorno),
            lineHeight = tamanho.mpxSp,
        ),
        color = StageColors.Ink.copy(alpha = 0.30f),
        maxLines = 1,
        modifier = modifier,
    )
}

/**
 * Um item do Top 10: o número vazado e, por cima dele, o cartaz.
 *
 * [cartaz] é o desenho da capa, que no app é a imagem do catálogo e no print é um bloco de cor,
 * porque imagem de rede não carrega em print (ver `docs/lobbus/PRINTS.md`).
 */
@Composable
fun LobbusItemTop10(
    numero: Int,
    titulo: String,
    etiqueta: String?,
    focado: Boolean,
    larguraDoCartaz: Dp = 196.mpx,
    alturaDoCartaz: Dp = 294.mpx,
    modifier: Modifier = Modifier,
    cartaz: @Composable () -> Unit,
) {
    Row(modifier.height(alturaDoCartaz + 20.mpx), verticalAlignment = Alignment.Bottom) {
        LobbusNumeroVazado(numero, modifier = Modifier.padding(bottom = 10.mpx))
        Box(
            Modifier
                .padding(start = (-34).mpx)
                .size(larguraDoCartaz, alturaDoCartaz)
                .clip(RoundedCornerShape(StageRadii.Poster))
                .then(
                    if (focado) {
                        Modifier.border(2.mpx, stageAccent.accent, RoundedCornerShape(StageRadii.Poster))
                    } else {
                        Modifier
                    },
                ),
        ) {
            cartaz()
            // Degradê curto no pé do cartaz: o título fica legível sobre qualquer capa.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            0.55f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.78f),
                        ),
                    ),
            )
            if (etiqueta != null) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.mpx)
                        .clip(RoundedCornerShape(StageRadii.Pill))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 10.mpx, vertical = 4.mpx),
                ) {
                    LobbusRotuloMono(etiqueta, StageColors.Text, tamanho = 11)
                }
            }
            Text(
                titulo.uppercase(java.util.Locale.getDefault()),
                style = stageText(22, 800),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 16.mpx, vertical = 18.mpx),
            )
        }
    }
}

/**
 * Um cartão largo da fileira "Em 4K, HDR e Dolby Atmos": a arte em 16:9, as etiquetas técnicas no
 * alto e, embaixo, o título com a linha de ano, gênero e duração em Geist Mono.
 */
@Composable
fun LobbusCartaoLargo(
    titulo: String,
    linhaTecnica: String,
    etiquetas: List<String>,
    focado: Boolean,
    largura: Dp = 374.mpx,
    modifier: Modifier = Modifier,
    arte: @Composable () -> Unit,
) {
    Column(
        modifier
            .width(largura)
            .clip(RoundedCornerShape(StageRadii.Poster))
            .then(
                if (focado) {
                    Modifier.border(2.mpx, stageAccent.accent, RoundedCornerShape(StageRadii.Poster))
                } else {
                    Modifier
                },
            ),
    ) {
        Box(Modifier.fillMaxWidth().height(210.mpx)) {
            arte()
            Row(
                Modifier.align(Alignment.TopStart).padding(12.mpx),
                horizontalArrangement = Arrangement.spacedBy(8.mpx),
            ) {
                etiquetas.forEach { etiqueta ->
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(StageRadii.Pill))
                            .background(Color.Black.copy(alpha = 0.62f))
                            .padding(horizontal = 10.mpx, vertical = 4.mpx),
                    ) {
                        LobbusRotuloMono(etiqueta, StageColors.Text, tamanho = 11)
                    }
                }
            }
        }
        Column(Modifier.padding(horizontal = 18.mpx, vertical = 16.mpx)) {
            Text(
                titulo,
                style = stageText(22, 800),
                color = StageColors.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.mpx))
            LobbusRotuloMono(linhaTecnica, StageColors.Dim, tamanho = 12)
        }
    }
}

/**
 * Um chip de gênero da barra de filtros: o escolhido vem cheio de laranja, os outros em contorno.
 * [destacado] é o chip de qualidade ("4K · HDR · ATMOS"), que fica em contorno laranja mesmo sem
 * estar escolhido, porque o protótipo o trata como um filtro à parte.
 */
@Composable
fun LobbusChipDeGenero(texto: String, escolhido: Boolean, destacado: Boolean = false) {
    val acento = stageAccent
    val fundo = if (escolhido) acento.accent else Color.Transparent
    val borda = when {
        escolhido -> Color.Transparent
        destacado -> acento.accent
        else -> StageColors.Ink.copy(alpha = 0.18f)
    }
    val cor = when {
        escolhido -> acento.onAccent
        destacado -> acento.accent
        else -> StageColors.Muted
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(StageRadii.Pill))
            .background(fundo)
            .border(1.mpx, borda, RoundedCornerShape(StageRadii.Pill))
            .padding(horizontal = 22.mpx, vertical = 10.mpx),
        contentAlignment = Alignment.Center,
    ) {
        LobbusRotuloMono(texto, cor, tamanho = 13, peso = if (escolhido) 500 else 400)
    }
}
