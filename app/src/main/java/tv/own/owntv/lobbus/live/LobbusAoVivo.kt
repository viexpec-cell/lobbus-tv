package tv.own.owntv.lobbus.live

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.Text
import tv.own.owntv.lobbus.lobbusLocale
import tv.own.owntv.R
import tv.own.owntv.lobbus.home.LobbusRotuloMono
import tv.own.owntv.lobbus.home.LobbusSobrancelha
import tv.own.owntv.lobbus.home.LobbusTitulo
import tv.own.owntv.lobbus.home.lobbusTituloDestaque
import tv.own.owntv.ui.theme.StageColors
import tv.own.owntv.ui.theme.StageRadii
import tv.own.owntv.ui.theme.mpx
import tv.own.owntv.ui.theme.mpxSp
import tv.own.owntv.ui.theme.stageAccent
import tv.own.owntv.ui.theme.stageText

/**
 * LOBBUS: as peças de assinatura da tela Ao vivo, do protótipo aprovado
 * (`docs/lobbus/design/LobbusTV-02-ao-vivo.jpg`).
 *
 * Mesma regra das peças do Início: sem estado, só desenho. Quem sabe de canal, EPG, foco e prévia
 * continua sendo `features/live`; daqui saem o cabeçalho da tela, o card "No ar agora" e a lista
 * "A seguir neste canal".
 */

/**
 * O cabeçalho de uma tela inteira: a sobrancelha em Geist Mono e, embaixo, o nome da tela no
 * tratamento de cabeçalho, com o laranja na expressão do fim e ponto final ("Canais ao vivo.").
 */
@Composable
fun LobbusCabecalhoDeTela(ordem: Int, rotulo: String, titulo: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        LobbusSobrancelha(ordem = ordem, rotulo = rotulo, tipo = null)
        Spacer(Modifier.height(10.mpx))
        Text(
            text = lobbusTituloDestaque(titulo, LobbusTitulo.CABECALHO),
            style = stageText(46, 900, (-1).mpxSp),
            color = StageColors.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * O card "No ar agora": o programa que está passando no canal em foco, com a moldura laranja do
 * protótipo, a faixa de horário em mono, a ficha do programa, a sinopse e quanto falta.
 *
 * Cada parte é opcional porque o EPG de uma fonte Xtream costuma vir pela metade: o card mostra o
 * que existe, sem buraco quando falta.
 */
@Composable
fun LobbusNoArAgora(
    faixaDeHorario: String,
    titulo: String,
    detalhes: String?,
    classificacao: String?,
    sinopse: String?,
    progresso: Float,
    restante: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StageRadii.Row))
            .background(StageColors.Ink.copy(alpha = 0.04f))
            .border(1.mpx, stageAccent.accent, RoundedCornerShape(StageRadii.Row))
            .padding(horizontal = 26.mpx, vertical = 22.mpx),
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(12.mpx), verticalAlignment = Alignment.CenterVertically) {
                LobbusRotuloMono(
                    texto = stringResource(
                        R.string.lobbus_rotulo_entre_colchetes,
                        stringResource(R.string.lobbus_no_ar_agora).uppercase(lobbusLocale()),
                    ),
                    cor = stageAccent.accent,
                    tamanho = 13,
                )
                LobbusRotuloMono(faixaDeHorario.uppercase(lobbusLocale()), StageColors.Muted, tamanho = 13)
            }
            Spacer(Modifier.height(12.mpx))
            Text(
                titulo,
                style = stageText(42, 800, (-0.5).mpxSp),
                color = StageColors.Text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (detalhes != null || classificacao != null) {
                Spacer(Modifier.height(10.mpx))
                Row(horizontalArrangement = Arrangement.spacedBy(14.mpx), verticalAlignment = Alignment.CenterVertically) {
                    if (detalhes != null) {
                        Text(
                            detalhes,
                            style = stageText(19, 500),
                            color = StageColors.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (classificacao != null) LobbusEtiquetaDeFicha(classificacao)
                }
            }
            if (sinopse != null) {
                Spacer(Modifier.height(14.mpx))
                Text(
                    sinopse,
                    style = stageText(19, 400).copy(lineHeight = (19 * 1.55f).mpxSp),
                    color = StageColors.Body,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(18.mpx))
            Row(horizontalArrangement = Arrangement.spacedBy(18.mpx), verticalAlignment = Alignment.CenterVertically) {
                LobbusBarraDeProgresso(progresso, Modifier.weight(1f))
                if (restante != null) LobbusRotuloMono(restante.uppercase(lobbusLocale()), StageColors.Muted, tamanho = 13)
            }
        }
    }
}

/** A etiqueta de classificação indicativa da ficha, no mesmo desenho das etiquetas do destaque. */
@Composable
private fun LobbusEtiquetaDeFicha(texto: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(StageRadii.Plate))
            .border(1.mpx, StageColors.Ink.copy(alpha = 0.18f), RoundedCornerShape(StageRadii.Plate))
            .padding(horizontal = 10.mpx, vertical = 4.mpx),
    ) {
        LobbusRotuloMono(texto, StageColors.Muted, tamanho = 12)
    }
}

/** A barra de progresso do protótipo: trilho fino e o laranja por cima, sem cantos arredondados. */
@Composable
fun LobbusBarraDeProgresso(progresso: Float, modifier: Modifier = Modifier, altura: Dp = 4.mpx) {
    Box(modifier.height(altura).background(StageColors.Ink.copy(alpha = 0.12f))) {
        Box(
            Modifier
                .fillMaxWidth(progresso.coerceIn(0f, 1f))
                .height(altura)
                .background(stageAccent.accent),
        )
    }
}

/** O rótulo "A SEGUIR NESTE CANAL" que abre a lista do que vem depois. */
@Composable
fun LobbusRotuloASeguir(modifier: Modifier = Modifier) {
    Box(modifier) {
        LobbusRotuloMono(
            stringResource(R.string.lobbus_a_seguir_no_canal).uppercase(lobbusLocale()),
            StageColors.Dim,
            tamanho = 13,
        )
    }
}

/**
 * Uma linha de "A seguir": a hora em mono, o título e, depois, o gênero e a duração.
 * [focado] vem do componente de foco de quem chama, para a linha responder ao controle remoto.
 */
@Composable
fun LobbusLinhaASeguir(hora: String, titulo: String, detalhe: String?, focado: Boolean, destacado: Boolean) {
    val corDoTexto = if (focado) stageAccent.onAccent else if (destacado) StageColors.Text else StageColors.Muted
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 22.mpx),
        horizontalArrangement = Arrangement.spacedBy(20.mpx),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LobbusRotuloMono(
            texto = hora,
            cor = if (focado) stageAccent.onAccent else if (destacado) stageAccent.accent else StageColors.Muted,
            tamanho = 15,
            modifier = Modifier.width(62.mpx),
        )
        Text(titulo, style = stageText(20, 700), color = corDoTexto, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (detalhe != null) {
            Text(
                detalhe,
                style = stageText(18, 400),
                color = if (focado) stageAccent.onAccent.copy(alpha = 0.7f) else StageColors.Dim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Cor de fundo da linha de canal em foco, para o print e a tela usarem a mesma. */
val LobbusFundoDeLinhaEmFoco: Color @Composable get() = stageAccent.accent.copy(alpha = 0.10f)
