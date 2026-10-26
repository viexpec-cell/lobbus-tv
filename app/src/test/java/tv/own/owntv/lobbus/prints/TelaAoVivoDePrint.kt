package tv.own.owntv.lobbus.prints

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Text
import tv.own.owntv.lobbus.lobbusLocale
import tv.own.owntv.lobbus.home.LobbusRotuloMono
import tv.own.owntv.lobbus.live.LobbusBarraDeProgresso
import tv.own.owntv.lobbus.live.LobbusCabecalhoDeTela
import tv.own.owntv.lobbus.live.LobbusLinhaASeguir
import tv.own.owntv.lobbus.live.LobbusNoArAgora
import tv.own.owntv.lobbus.live.LobbusRotuloASeguir
import tv.own.owntv.ui.theme.LocalStagePalette
import tv.own.owntv.ui.theme.StageColors
import tv.own.owntv.ui.theme.StageRadii
import tv.own.owntv.ui.theme.mpx
import tv.own.owntv.ui.theme.stageAccent
import tv.own.owntv.ui.theme.stageText

/**
 * LOBBUS: a tela Ao vivo montada para o print.
 *
 * De produção: o cabeçalho da tela, o card "No ar agora" e as linhas de "A seguir"
 * (`lobbus/live/LobbusAoVivo.kt`), que são o item da Fase 4. De exemplo: o menu lateral, a coluna de
 * categorias, a área da prévia (vídeo não desenha em print) e as linhas de canal, que no app são o
 * `LiveStageRow` com dados do banco. As linhas daqui copiam o desenho novo do `LiveStageRow`, então
 * servem para julgar o conjunto, mas quem manda é a tela.
 */

/** Tag da linha de canal que leva o foco no print. */
const val TAG_LINHA_CANAL = "lobbus-linha-canal"

@Composable
fun TelaAoVivo() {
    val canal = CatalogoFalso.canais.first { it.nome == "Cine Clássico" }
    Row(Modifier.fillMaxSize()) {
        MenuLateralDeExemplo(itemAtivo = 2)
        Column(Modifier.padding(start = 48.mpx, top = 44.mpx, end = 40.mpx)) {
            LobbusCabecalhoDeTela(
                ordem = 2,
                rotulo = "TV AO VIVO · ${CatalogoFalso.canais.size} CANAIS",
                titulo = "Canais ao vivo",
            )
            Spacer(Modifier.height(36.mpx))
            Row(horizontalArrangement = Arrangement.spacedBy(40.mpx)) {
                ColunaDeCategorias(Modifier.width(240.mpx))
                ListaDeCanais(Modifier.width(620.mpx))
                PainelDoCanal(canal, Modifier.width(760.mpx))
            }
        }
    }
}

/** Coluna de categorias: bloco de exemplo, a lista de verdade é a `LiveCategories` do app. */
@Composable
private fun ColunaDeCategorias(modifier: Modifier = Modifier) {
    val categorias = listOf(
        "Todos" to 312, "Favoritos" to 18, "Recentes" to 24, "Notícias" to 22,
        "Esportes" to 41, "Filmes" to 56, "Séries" to 38, "Infantil" to 19,
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.mpx)) {
        categorias.forEach { (nome, quantos) ->
            val escolhida = nome == "Todos"
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(StageRadii.Button))
                    .then(
                        if (escolhida) {
                            Modifier
                                .background(stageAccent.accent.copy(alpha = 0.10f))
                                .border(1.mpx, stageAccent.accent, RoundedCornerShape(StageRadii.Button))
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = 24.mpx, vertical = 12.mpx),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    nome,
                    style = stageText(22, if (escolhida) 700 else 500),
                    color = if (escolhida) StageColors.Text else StageColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                LobbusRotuloMono(
                    quantos.toString(),
                    if (escolhida) stageAccent.accent else StageColors.Dim,
                    tamanho = 13,
                )
            }
        }
    }
}

/** A lista de canais, no desenho novo do `LiveStageRow`. */
@Composable
private fun ListaDeCanais(modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("Todos os canais", style = stageText(30, 800), color = StageColors.Text)
            Spacer(Modifier.weight(1f))
            LobbusRotuloMono("${CatalogoFalso.canais.size} CANAIS", StageColors.Dim, tamanho = 13)
        }
        Spacer(Modifier.height(18.mpx))
        Column(verticalArrangement = Arrangement.spacedBy(6.mpx)) {
            CatalogoFalso.canais.forEach { canal ->
                LinhaDeCanal(
                    canal = canal,
                    modifier = if (canal.nome == "Cine Clássico") Modifier.testTag(TAG_LINHA_CANAL) else Modifier,
                )
            }
        }
    }
}

@Composable
private fun LinhaDeCanal(canal: Canal, modifier: Modifier = Modifier) {
    val paleta = LocalStagePalette.current
    var focada by remember { mutableStateOf(false) }
    Row(
        modifier
            .fillMaxWidth()
            .height(84.mpx)
            .onFocusChanged { focada = it.isFocused }
            .focusable()
            .clip(RoundedCornerShape(StageRadii.Row))
            .then(
                if (focada) {
                    Modifier
                        .background(stageAccent.accent.copy(alpha = 0.10f))
                        .border(2.mpx, stageAccent.accent, RoundedCornerShape(StageRadii.Row))
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 20.mpx),
        horizontalArrangement = Arrangement.spacedBy(18.mpx),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LobbusRotuloMono(canal.numero.toString(), StageColors.Dim, tamanho = 15, modifier = Modifier.width(44.mpx))
        Box(
            Modifier
                .size(76.mpx, 54.mpx)
                .clip(RoundedCornerShape(StageRadii.Plate))
                .background(if (focada) stageAccent.accent else paleta.glassTint),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                canal.nome.filter { it.isUpperCase() }.take(2).ifBlank { canal.nome.take(2).uppercase(lobbusLocale()) },
                style = stageText(22, 800),
                color = if (focada) stageAccent.onAccent else StageColors.Text,
            )
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    canal.nome,
                    style = stageText(22, 700),
                    color = StageColors.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.weight(1f))
                LobbusRotuloMono(faltam(canal.noAr), StageColors.Dim, tamanho = 13)
            }
            Spacer(Modifier.height(2.mpx))
            Text(
                canal.noAr.titulo,
                style = stageText(17, 400),
                color = StageColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.mpx))
            LobbusBarraDeProgresso(canal.noAr.progresso, Modifier.fillMaxWidth())
        }
    }
}

/** A direita: a área da prévia e, embaixo, as peças de produção do "No ar agora" e do "A seguir". */
@Composable
private fun PainelDoCanal(canal: Canal, modifier: Modifier = Modifier) {
    Column(modifier) {
        AreaDaPrevia(canal)
        Spacer(Modifier.height(24.mpx))
        LobbusNoArAgora(
            faixaDeHorario = canal.noAr.faixa,
            titulo = canal.noAr.titulo,
            detalhes = "Drama · 2024 · 1h55",
            classificacao = "14",
            sinopse = canal.noAr.descricao,
            progresso = canal.noAr.progresso,
            restante = faltam(canal.noAr),
        )
        LobbusRotuloASeguir(Modifier.padding(top = 26.mpx, bottom = 12.mpx))
        Column(verticalArrangement = Arrangement.spacedBy(10.mpx)) {
            canal.aSeguir.forEachIndexed { indice, programa ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(62.mpx)
                        .clip(RoundedCornerShape(StageRadii.Row))
                        .background(StageColors.Ink.copy(alpha = 0.06f)),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    LobbusLinhaASeguir(
                        hora = CatalogoFalso.hora(programa.inicio),
                        titulo = programa.titulo,
                        detalhe = null,
                        focado = false,
                        destacado = indice == 0,
                    )
                }
            }
        }
    }
}

/** A moldura da prévia: em print não há vídeo, então fica a área com a tarja e as informações. */
@Composable
private fun AreaDaPrevia(canal: Canal) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(StageRadii.Sheet))
            .background(Color.Black),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.mpx, vertical = 18.mpx),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(StageRadii.Pill))
                    .background(stageAccent.accent)
                    .padding(horizontal = 14.mpx, vertical = 5.mpx),
            ) {
                LobbusRotuloMono("AO VIVO", stageAccent.onAccent, tamanho = 12)
            }
            Spacer(Modifier.width(16.mpx))
            LobbusRotuloMono(
                "${canal.numero} · ${canal.nome.uppercase(lobbusLocale())}",
                StageColors.Muted,
                tamanho = 13,
            )
            Spacer(Modifier.weight(1f))
            LobbusRotuloMono("1080P · 24 FPS · DOLBY DIGITAL+", StageColors.Dim, tamanho = 12)
        }
        Box(Modifier.fillMaxHeight().align(Alignment.BottomStart).padding(start = 22.mpx, bottom = 16.mpx)) {
            Box(Modifier.align(Alignment.BottomStart)) {
                LobbusRotuloMono("PRÉVIA AO VIVO · OK PARA TELA CHEIA", StageColors.Dim, tamanho = 12)
            }
        }
    }
}

/** "FALTAM 47 MIN" a partir do relógio fixo do catálogo. */
private fun faltam(programa: Programa): String {
    val minutos = java.time.Duration.between(CatalogoFalso.AGORA, programa.fim).toMinutes()
    return if (minutos >= 60) {
        "FALTAM ${minutos / 60}H${(minutos % 60).toString().padStart(2, '0')}"
    } else {
        "FALTAM $minutos MIN"
    }
}
