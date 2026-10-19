package tv.own.owntv.lobbus.prints

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import java.util.Locale
import tv.own.owntv.lobbus.home.LobbusMetaHeroi
import tv.own.owntv.lobbus.home.LobbusTituloFileira
import tv.own.owntv.lobbus.live.LobbusCabecalhoDeTela
import tv.own.owntv.lobbus.movies.LobbusCartaoLargo
import tv.own.owntv.lobbus.movies.LobbusChipDeGenero
import tv.own.owntv.lobbus.movies.LobbusItemTop10
import tv.own.owntv.ui.theme.mpx

/**
 * LOBBUS: a tela Filmes montada para o print.
 *
 * De produção: o cabeçalho da tela, os chips de gênero, o item do Top 10 com o número vazado, o
 * cartão largo da fileira de 4K, HDR e Atmos e a linha de informações do título em foco
 * (`lobbus/movies/LobbusFilmes.kt`).
 *
 * **Ainda não ligado ao app**: a tela Filmes de hoje é uma grade de cartazes, sem fileiras. Este
 * print mostra o desenho aprovado antes de mexer na grade, porque a ligação com os dados (Top 10
 * vindo do "em alta" e a fileira filtrada pelas capacidades do provedor) é a parte cara e não vale
 * ser feita sobre um desenho que ainda pode mudar.
 */

/** Tag do cartaz que leva o foco no print. */
const val TAG_CARTAZ_TOP10 = "lobbus-cartaz-top10"

private val CORES_DE_CAPA = listOf(
    Color(0xFF2A1F17),
    Color(0xFF231A14),
    Color(0xFF3A2A1E),
    Color(0xFF1F1812),
    Color(0xFF2E241C),
)

@Composable
fun TelaFilmes() {
    val emFoco = CatalogoFalso.top10[1]
    Row(Modifier.fillMaxSize()) {
        MenuLateralDeExemplo(itemAtivo = 4)
        Column(Modifier.padding(start = 48.mpx, top = 44.mpx, end = 40.mpx)) {
            LobbusCabecalhoDeTela(ordem = 4, rotulo = "CATÁLOGO DE FILMES", titulo = "Seu cinema")
            Spacer(Modifier.height(28.mpx))
            BarraDeGeneros()
            Spacer(Modifier.height(34.mpx))
            LobbusTituloFileira(
                titulo = "Top 10 da semana",
                contador = "MAIS VISTOS NA SUA LISTA",
                ordem = 1,
                altura = 46.mpx,
                inicio = 0.mpx,
            )
            Spacer(Modifier.height(12.mpx))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.mpx), userScrollEnabled = false) {
                itemsIndexed(CatalogoFalso.top10.take(5)) { indice, filme ->
                    CartazDoTop10(
                        posicao = indice + 1,
                        filme = filme,
                        modifier = if (indice == 1) Modifier.testTag(TAG_CARTAZ_TOP10) else Modifier,
                    )
                }
            }
            Spacer(Modifier.height(10.mpx))
            LobbusMetaHeroi(
                nota = emFoco.nota.toString().replace('.', ','),
                partes = listOf(emFoco.titulo, emFoco.ano.toString(), emFoco.generos),
                classificacao = emFoco.classificacao,
                etiquetas = emFoco.selos.map { it.rotulo },
            )
            Spacer(Modifier.height(30.mpx))
            LobbusTituloFileira(
                titulo = "Em 4K, HDR e Dolby Atmos",
                contador = "FEITO PARA A SUA TV",
                ordem = 2,
                altura = 46.mpx,
                inicio = 0.mpx,
            )
            Spacer(Modifier.height(12.mpx))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.mpx), userScrollEnabled = false) {
                itemsIndexed(CatalogoFalso.top10.drop(2)) { indice, filme ->
                    LobbusCartaoLargo(
                        titulo = filme.titulo,
                        linhaTecnica = linhaTecnica(filme),
                        etiquetas = filme.selos.map { it.rotulo }.take(2),
                        focado = false,
                        arte = { Box(Modifier.fillMaxSize().background(CORES_DE_CAPA[indice % CORES_DE_CAPA.size])) },
                    )
                }
            }
            Spacer(Modifier.height(26.mpx))
            LobbusTituloFileira(
                titulo = "Adicionados recentemente",
                contador = "NOVIDADES DA LISTA",
                ordem = 3,
                altura = 46.mpx,
                inicio = 0.mpx,
            )
        }
    }
}

/** A barra de filtros do protótipo, com o chip de qualidade em contorno laranja no fim. */
@Composable
private fun BarraDeGeneros() {
    val generos = listOf("TODOS", "AÇÃO", "DRAMA", "COMÉDIA", "SUSPENSE", "TERROR", "NACIONAL", "INFANTIL")
    Row(horizontalArrangement = Arrangement.spacedBy(12.mpx), verticalAlignment = Alignment.CenterVertically) {
        generos.forEachIndexed { indice, genero ->
            LobbusChipDeGenero(genero, escolhido = indice == 0)
        }
        LobbusChipDeGenero("4K · HDR · ATMOS", escolhido = false, destacado = true)
    }
}

/** Um item do Top 10, focável de verdade para o anel do print vir do foco. */
@Composable
private fun CartazDoTop10(posicao: Int, filme: Filme, modifier: Modifier = Modifier) {
    var focado by remember { mutableStateOf(false) }
    LobbusItemTop10(
        numero = posicao,
        titulo = filme.titulo,
        etiqueta = filme.selos.firstOrNull()?.rotulo,
        focado = focado,
        modifier = modifier.onFocusChanged { focado = it.isFocused }.focusable(),
        cartaz = {
            Box(Modifier.fillMaxSize().background(CORES_DE_CAPA[(posicao - 1) % CORES_DE_CAPA.size]))
        },
    )
}

/** "2025 · DRAMA · 2H12", como o protótipo escreve embaixo do cartão largo. */
private fun linhaTecnica(filme: Filme): String {
    val horas = filme.duracaoMin / 60
    val minutos = filme.duracaoMin % 60
    val duracao = "${horas}H${minutos.toString().padStart(2, '0')}"
    return "${filme.ano} · ${filme.generos.uppercase(Locale.getDefault())} · $duracao"
}
