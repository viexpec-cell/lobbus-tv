package tv.own.owntv.lobbus.prints

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import tv.own.owntv.lobbus.abertura.DadosDaAbertura
import tv.own.owntv.lobbus.abertura.LobbusAbertura
import tv.own.owntv.lobbus.abertura.NuvemDeParticulas

/**
 * LOBBUS: a abertura (item 4b.1) parada no instante pedido, para o print.
 *
 * A abertura desenha o quadro de um instante e nada mais, então aqui não há relógio nem som: o
 * print de `t = 2,8` tem que sair igual ao quadro de 2,8 s do vídeo aprovado em
 * `docs/lobbus/movimento/abertura/referencia.mp4`.
 */
@Composable
fun TelaAbertura(tempo: Float) {
    val contexto = LocalContext.current
    val nuvem = remember(contexto) { NuvemDeParticulas.doRecurso(contexto) }
    LobbusAbertura(
        tempo = tempo,
        dados = DADOS_DE_EXEMPLO,
        nuvem = nuvem,
        modifier = Modifier,
    )
}

/**
 * O que a abertura escreve, com os números do catálogo falso.
 *
 * No app esses valores vêm do banco local; aqui são fixos, para dois prints do mesmo instante
 * saírem idênticos.
 */
private val DADOS_DE_EXEMPLO = DadosDaAbertura(
    nucleo = "LOBBUS.CORE // TV v0.4.0",
    relogio = "SEX · 16 OUT · 23:20:08",
    coordenadas = "31°46′S · 52°20′W",
    estado = "STATUS ● ONLINE",
    iniciando = "INICIANDO %1\$s",
    formatoDoLog = "%1\$s: %2\$s",
    slogan = "O MARKETING COMO DEVE SER.",
    linhasDeLog = listOf(
        "NÚCLEO LOBBUS" to "OK",
        "FONTE XTREAM" to "CONECTADA",
        "CANAIS" to "312",
        "GUIA DE PROGRAMAÇÃO" to "ATÉ 06:00",
        "FILMES" to "4.187",
        "SÉRIES" to "913",
    ),
)
