package tv.own.owntv.lobbus.abertura

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import tv.own.owntv.R
import tv.own.owntv.ui.theme.LobbusMonoFamily
import tv.own.owntv.ui.theme.StageRadii
import tv.own.owntv.ui.theme.stageText

/**
 * LOBBUS: a abertura do app (item 4b.1 do plano), aprovada pelo Vitor em 22/10/2026.
 *
 * Desenha o quadro do instante [tempo], e só isso: não tem relógio próprio, não toca som e não
 * decide quando acabar. Quem faz isso é [LobbusAberturaAnimada]. Essa separação é de propósito, e é
 * a mesma do protótipo (`renderAt(t)`): com ela a máquina de prints fotografa qualquer instante e o
 * resultado pode ser comparado com `docs/lobbus/movimento/abertura/referencia.mp4`.
 *
 * Coordenadas de projeto: tudo está escrito em pixels de uma tela de 1920x1080 e convertido pela
 * largura real, então a abertura vale em qualquer resolução de TV.
 */
@Composable
fun LobbusAbertura(
    tempo: Float,
    dados: DadosDaAbertura,
    nuvem: NuvemDeParticulas?,
    modifier: Modifier = Modifier,
) {
    val tinta = remember { android.graphics.Paint().apply { isAntiAlias = true } }
    val buffer = remember(nuvem) { FloatArray((nuvem?.quantidade ?: 0) * 2) }
    val medidor = rememberTextMeasurer()

    BoxWithConstraints(modifier.fillMaxSize().background(Fundo)) {
        // u(543) = 543 pixels de projeto, na largura real desta tela.
        val u: (Float) -> Dp = { maxWidth * (it / 1920f) }

        val saiuOHud = 1f - progresso(tempo, AberturaTempos.ENCAIXE)
        val opacidadeDoHud = smoothstep(progresso(tempo, AberturaTempos.HUD)) * saiuOHud

        Canvas(Modifier.fillMaxSize()) {
            val k = size.width / 1920f
            fun px(v: Float) = v * k

            desenharGrade(opacidadeDoHud, k)

            // O carregador: contador, barra e as seis linhas de log. Sobe 24 px e some no fim.
            val fugaDoCarregador = progresso(tempo, AberturaTempos.CARREGADOR_SAI)
            val opacidadeDoCarregador = (1f - fugaDoCarregador) * opacidadeDoHud
            if (opacidadeDoCarregador > 0.01f) {
                val subida = -px(24f) * expoOut(fugaDoCarregador)
                desenharCarregador(
                    medidor = medidor,
                    dados = dados,
                    tempo = tempo,
                    k = k,
                    subida = subida,
                    opacidade = opacidadeDoCarregador,
                )
            }

            // A nuvem de partículas: nasce em 1,30 e se espalha depois do impacto.
            val entradaDaNuvem = smoothstep(progresso(tempo, AberturaTempos.RUIDO))
            val sumicoDaNuvem = 1f - progresso(tempo, AberturaTempos.DISSOLVE)
            val opacidadeDaNuvem = entradaDaNuvem * sumicoDaNuvem
            if (opacidadeDaNuvem > 0.01f) {
                val casas = nuvem?.posicoesEm(
                    t = tempo,
                    centroX = size.width / 2f,
                    centroY = size.height * 0.46f,
                    altura = px(543f),
                    saida = buffer,
                ) ?: 0
                tinta.color = Laranja.copy(alpha = opacidadeDaNuvem).toArgbCompat()
                tinta.strokeWidth = px(2f)
                drawIntoCanvas { it.nativeCanvas.drawPoints(buffer, 0, casas, tinta) }
            }

            // A varredura que revela o lobo sólido: uma linha laranja de cima para baixo.
            val varredura = progresso(tempo, AberturaTempos.VARREDURA)
            if (varredura > 0f && varredura < 1f) {
                val y = size.height * 0.18f + px(543f) * varredura
                drawRect(
                    color = Laranja,
                    topLeft = Offset(size.width * 0.5f - px(260f), y),
                    size = androidx.compose.ui.geometry.Size(px(520f), px(3f)),
                )
            }
        }

        // O lobo sólido: aparece na varredura, pulsa no impacto, esfria de laranja para branco e
        // desliza do centro para o tamanho da assinatura.
        LoboSolido(tempo = tempo, u = u, modifier = Modifier.align(Alignment.Center))

        // A assinatura: a palavra Lobbus subindo da máscara e a pílula TV encaixando.
        Assinatura(tempo = tempo, u = u, modifier = Modifier.align(Alignment.Center))

        // O slogan decodificando, embaixo da assinatura.
        val slogan = progresso(tempo, AberturaTempos.SLOGAN)
        if (slogan > 0f && saiuOHud > 0f) {
            Text(
                text = decodifica(dados.slogan, slogan, semente = 7),
                style = stageText(16, 500, letterSpacing = 2.4f.sp).copy(fontFamily = LobbusMonoFamily),
                color = Apoio.copy(alpha = saiuOHud),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = u(120f)),
            )
        }

        // Os quatro cantos do protótipo.
        CantosDoHud(dados = dados, opacidade = opacidadeDoHud, u = u)
    }
}

/** O lobo sólido, da varredura até a assinatura pousar. */
@Composable
private fun LoboSolido(tempo: Float, u: (Float) -> Dp, modifier: Modifier = Modifier) {
    val revelado = progresso(tempo, AberturaTempos.VARREDURA)
    if (revelado <= 0f) return
    val desliza = expoInOut(progresso(tempo, AberturaTempos.LOBO_DESLIZA))
    val altura = u(mistura(543f, 300f, desliza))
    // Pulso de 3,5% no impacto, que volta ao normal em 0,2 s.
    val pulso = if (tempo >= AberturaTempos.IMPACTO && tempo < AberturaTempos.IMPACTO + 0.2f) {
        1f + 0.035f * (1f - (tempo - AberturaTempos.IMPACTO) / 0.2f)
    } else {
        1f
    }
    // Esfria de laranja para branco entre 2,84 e 3,26.
    val cor = lerp(Laranja, Branco, smoothstep(progresso(tempo, AberturaTempos.ESFRIA)))
    // No Encaixe o lobo vira brasas: some enquanto a assinatura voa.
    val brasas = 1f - progresso(tempo, AberturaTempos.ENCAIXE)
    Image(
        painter = painterResource(R.drawable.lobbus_mark),
        contentDescription = null,
        colorFilter = ColorFilter.tint(cor),
        modifier = modifier
            .offset(y = u(mistura(0f, -90f, desliza)))
            .size(altura)
            .graphicsLayer {
                scaleX = pulso
                scaleY = pulso
                alpha = brasas
            }
            // A varredura revela de cima para baixo: o que ainda não foi varrido não aparece.
            .drawWithContent {
                clipRect(bottom = size.height * revelado) { this@drawWithContent.drawContent() }
            },
    )
}

/** A palavra Lobbus subindo da máscara e a pílula TV encaixando ao lado. */
@Composable
private fun Assinatura(tempo: Float, u: (Float) -> Dp, modifier: Modifier = Modifier) {
    val palavra = progresso(tempo, AberturaTempos.PALAVRA)
    if (palavra <= 0f) return
    val subida = expoOut(palavra)
    val voo = expoInOut(progresso(tempo, AberturaTempos.VOO))
    val saida = 1f - progresso(tempo, AberturaTempos.TROCA)
    Box(
        modifier
            .offset(
                // No voo, a assinatura vai do centro para o lugar do cabeçalho do Início.
                x = u(mistura(0f, -672f, voo)),
                y = u(mistura(60f, -430f, voo)),
            )
            .alpha(saida),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .width(u(mistura(300f, 212f, voo)))
                // A máscara: a palavra sobe de dentro dela, 112% da própria altura.
                .drawWithContent {
                    clipRect(top = 0f) { this@drawWithContent.drawContent() }
                },
        ) {
            Image(
                painter = painterResource(R.drawable.lobbus_wordmark),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Branco),
                modifier = Modifier
                    .width(u(mistura(300f, 212f, voo)))
                    .offset(y = u(mistura(112f, 0f, subida))),
            )
        }
        val pilula = backOut(progresso(tempo, AberturaTempos.PILULA))
        if (pilula > 0f) {
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = u(54f))
                    .graphicsLayer {
                        scaleX = pilula
                        scaleY = pilula
                    }
                    .clip(RoundedCornerShape(StageRadii.Pill))
                    .background(Laranja)
                    .padding(horizontal = u(14f), vertical = u(4f)),
            ) {
                Text(
                    text = stringResource(R.string.lobbus_tv_label),
                    style = stageText(14, 500, letterSpacing = 2f.sp).copy(fontFamily = LobbusMonoFamily),
                    color = SobreLaranja,
                )
            }
        }
    }
}

/** Os quatro cantos: núcleo e versão, relógio, coordenadas de Pelotas e o estado. */
@Composable
private fun CantosDoHud(dados: DadosDaAbertura, opacidade: Float, u: (Float) -> Dp) {
    if (opacidade <= 0.01f) return
    val estilo = stageText(14, 500, letterSpacing = 2f.sp).copy(fontFamily = LobbusMonoFamily)
    val cor = Apoio.copy(alpha = opacidade)
    Box(Modifier.fillMaxSize().padding(u(56f))) {
        Text(dados.nucleo, style = estilo, color = cor, modifier = Modifier.align(Alignment.TopStart))
        Text(dados.relogio, style = estilo, color = cor, modifier = Modifier.align(Alignment.TopEnd))
        Text(dados.coordenadas, style = estilo, color = cor, modifier = Modifier.align(Alignment.BottomStart))
        Box(Modifier.align(Alignment.BottomEnd)) {
            Text(dados.estado, style = estilo, color = cor)
        }
    }
}

/** A grade de seis colunas do fundo. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.desenharGrade(opacidade: Float, k: Float) {
    if (opacidade <= 0.01f) return
    val cor = Branco.copy(alpha = 0.05f * opacidade)
    for (coluna in 1 until 6) {
        val x = size.width * coluna / 6f
        drawRect(
            color = cor,
            topLeft = Offset(x, 0f),
            size = androidx.compose.ui.geometry.Size(1f * k, size.height),
        )
    }
}

/** O contador INICIANDO, a barra de 300 px e as seis linhas de log. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.desenharCarregador(
    medidor: androidx.compose.ui.text.TextMeasurer,
    dados: DadosDaAbertura,
    tempo: Float,
    k: Float,
    subida: Float,
    opacidade: Float,
) {
    fun px(v: Float) = v * k
    val estiloMono = TextStyle(
        fontFamily = LobbusMonoFamily,
        // LOBBUS: px de projeto viram sp pela densidade real (toSp), como o mpxSp de StageTokens.
        // Multiplicar por 1,8 aqui e deixar o .sp aplicar a densidade de novo desenhava 1,8x maior.
        fontSize = px(14f).toSp(),
        letterSpacing = px(2f).toSp(),
    )
    val centroX = size.width / 2f
    val topo = size.height * 0.40f + subida

    // INICIANDO 000 a 100.
    val andamento = smoothstep(progresso(tempo, AberturaTempos.CONTADOR))
    val numero = (andamento * 100f).toInt().toString().padStart(3, '0')
    val cabecalho = String.format(dados.iniciando, numero)
    val medida = medidor.measure(cabecalho, estiloMono)
    drawText(
        textMeasurer = medidor,
        text = cabecalho,
        topLeft = Offset(centroX - medida.size.width / 2f, topo),
        style = estiloMono.copy(color = Texto.copy(alpha = opacidade)),
    )

    // A barra laranja de 300 px.
    val larguraDaBarra = px(300f)
    val alturaDaBarra = px(3f)
    val yDaBarra = topo + px(34f)
    drawRect(
        color = Branco.copy(alpha = 0.12f * opacidade),
        topLeft = Offset(centroX - larguraDaBarra / 2f, yDaBarra),
        size = androidx.compose.ui.geometry.Size(larguraDaBarra, alturaDaBarra),
    )
    drawRect(
        color = Laranja.copy(alpha = opacidade),
        topLeft = Offset(centroX - larguraDaBarra / 2f, yDaBarra),
        size = androidx.compose.ui.geometry.Size(larguraDaBarra * andamento, alturaDaBarra),
    )

    // As seis linhas do log, uma a cada 0,14 s, cada uma decodificando em 0,16 s.
    dados.linhasDeLog.forEachIndexed { indice, linha ->
        val comeco = AberturaTempos.LOG_PRIMEIRA + indice * AberturaTempos.LOG_PASSO
        val avanco = progresso(tempo, comeco..(comeco + AberturaTempos.LOG_DECODE))
        if (avanco <= 0f) return@forEachIndexed
        val texto = String.format(dados.formatoDoLog, linha.first, decodifica(linha.second, avanco, semente = indice * 31))
        val y = yDaBarra + px(34f) + indice * px(26f)
        val larguraDaLinha = medidor.measure(texto, estiloMono).size.width
        drawText(
            textMeasurer = medidor,
            text = texto,
            topLeft = Offset(centroX - larguraDaLinha / 2f, y),
            style = estiloMono.copy(color = Apoio.copy(alpha = opacidade)),
        )
    }
}

private fun Color.toArgbCompat(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt(),
)

// As cores da abertura são fixas: ela roda antes do tema, no preto da marca.
private val Fundo = Color(0xFF0D0906)
private val Laranja = Color(0xFFF2600C)
private val Branco = Color(0xFFF6EFE9)
private val Texto = Color(0xFFF6EFE9)
private val Apoio = Color(0xFFAB9C8F)
private val SobreLaranja = Color(0xFF140C06)
