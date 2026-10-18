package tv.own.owntv.lobbus.prints

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * LOBBUS: catálogo de mentira para os prints das telas.
 *
 * Existe só no código de teste e nunca vai para o APK. Regra "player apenas" do CLAUDE.md: aqui não
 * há nenhuma lista, servidor, login, link ou conteúdo real, apenas nomes inventados e genéricos.
 *
 * As telas da Fase 4 são escritas como composables sem estado que recebem estes modelos, então a
 * mesma estrutura serve para o print e para o app com dados de verdade.
 *
 * O relógio é fixo ([AGORA]) para dois prints da mesma tela saírem iguais e poderem ser comparados.
 */
object CatalogoFalso {

    /** Sexta, 16 de outubro de 2026, 23:20: a mesma hora que aparece no protótipo aprovado. */
    val AGORA: LocalDateTime = LocalDateTime.of(2026, 10, 16, 23, 20)

    private val HORA = DateTimeFormatter.ofPattern("HH:mm")

    fun hora(momento: LocalDateTime): String = momento.format(HORA)

    // ---------------------------------------------------------------- destaque do Início

    val destaque = Destaque(
        rotulo = "EM ALTA HOJE",
        tipo = "FILME",
        tituloClaro = "Linha de",
        tituloAcento = "fogo.",
        nota = 7.8,
        ano = 2026,
        duracao = "2h08",
        generos = "Ação, Suspense",
        classificacao = "16",
        selos = listOf(Selo.QUATRO_K, Selo.DOLBY_VISION, Selo.ATMOS),
        sinopse = "Uma brigadista tem uma noite para segurar a linha de contenção antes que o " +
            "incêndio chegue à cidade onde cresceu.",
    )

    // ---------------------------------------------------------------- Continuar assistindo

    val continuarAssistindo = listOf(
        EmAndamento("Maré Alta", "T2:E4", "FALTAM 14 MIN", progresso = 0.72f),
        EmAndamento("Cidade Acesa", "FILME", "FALTAM 41 MIN", progresso = 0.55f),
        EmAndamento("Órbita", "T1:E7", "FALTAM 22 MIN", progresso = 0.38f),
        EmAndamento("O Último Inverno", "FILME", "FALTAM 1H05", progresso = 0.21f),
        EmAndamento("Vertigem", "T3:E1", "FALTAM 36 MIN", progresso = 0.12f),
    )

    // ---------------------------------------------------------------- Ao vivo

    val canais = listOf(
        Canal(
            numero = 101,
            nome = "Canal Um HD",
            categoria = "ABERTOS",
            favorito = true,
            qualidade = "HD",
            noAr = Programa(
                titulo = "Jornal da Noite",
                inicio = AGORA.minusMinutes(50),
                fim = AGORA.plusMinutes(10),
                descricao = "O resumo do dia, com previsão do tempo e esportes no fim do bloco.",
            ),
            aSeguir = listOf(
                Programa("Documentário: Rios do Sul", AGORA.plusMinutes(10), AGORA.plusMinutes(70)),
                Programa("Cinema da Madrugada", AGORA.plusMinutes(70), AGORA.plusMinutes(190)),
            ),
        ),
        Canal(
            numero = 102,
            nome = "Esporte Total 4K",
            categoria = "ESPORTES",
            favorito = true,
            qualidade = "4K",
            noAr = Programa(
                titulo = "Rodada Completa",
                inicio = AGORA.minusMinutes(25),
                fim = AGORA.plusMinutes(95),
                descricao = "Transmissão com os melhores momentos de todos os jogos da rodada.",
            ),
            aSeguir = listOf(
                Programa("Mesa Redonda", AGORA.plusMinutes(95), AGORA.plusMinutes(155)),
            ),
        ),
        Canal(
            numero = 103,
            nome = "Cine Clássico",
            categoria = "FILMES",
            favorito = false,
            qualidade = "HD",
            noAr = Programa(
                titulo = "A Casa na Colina",
                inicio = AGORA.minusMinutes(70),
                fim = AGORA.plusMinutes(35),
                descricao = "Um arquiteto volta à casa da família e encontra os planos que nunca assinou.",
            ),
            aSeguir = listOf(
                Programa("Noite de Suspense", AGORA.plusMinutes(35), AGORA.plusMinutes(145)),
            ),
        ),
        Canal(
            numero = 104,
            nome = "Notícias 24h",
            categoria = "NOTÍCIAS",
            favorito = false,
            qualidade = "HD",
            noAr = Programa(
                titulo = "Boletim das 23h",
                inicio = AGORA.minusMinutes(20),
                fim = AGORA.plusMinutes(10),
                descricao = "Plantão com as notícias da última hora.",
            ),
            aSeguir = listOf(
                Programa("Economia em Foco", AGORA.plusMinutes(10), AGORA.plusMinutes(70)),
            ),
        ),
    )

    // ---------------------------------------------------------------- Filmes

    val top10 = listOf(
        Filme("Linha de Fogo", 2026, 128, "Ação, Suspense", 7.8, "16", listOf(Selo.QUATRO_K, Selo.DOLBY_VISION, Selo.ATMOS)),
        Filme("Cidade Acesa", 2025, 112, "Drama", 7.4, "14", listOf(Selo.QUATRO_K, Selo.HDR10)),
        Filme("O Último Inverno", 2024, 134, "Drama, Romance", 8.1, "12", listOf(Selo.QUATRO_K, Selo.ATMOS)),
        Filme("Órbita", 2026, 119, "Ficção científica", 7.2, "12", listOf(Selo.QUATRO_K, Selo.DOLBY_VISION)),
        Filme("Vertigem", 2023, 101, "Suspense", 6.9, "16", listOf(Selo.HDR10)),
        Filme("Maré Alta", 2025, 96, "Aventura", 7.0, "L", listOf(Selo.QUATRO_K)),
        Filme("Pedra Branca", 2024, 141, "Histórico", 7.6, "14", listOf(Selo.ATMOS)),
        Filme("Noite Clara", 2026, 88, "Comédia", 6.5, "12", emptyList()),
        Filme("Chuva de Prata", 2025, 107, "Drama", 7.1, "12", listOf(Selo.HDR10, Selo.ATMOS)),
        Filme("Rota 9", 2023, 124, "Ação", 6.8, "16", listOf(Selo.QUATRO_K)),
    )

    /** A fileira "4K, HDR e Atmos" da tela de Filmes: só o que tem pelo menos um selo técnico. */
    val fileira4K = top10.filter { it.selos.isNotEmpty() }

    // ---------------------------------------------------------------- Série

    val serie = Serie(
        titulo = "Maré Alta",
        ano = 2025,
        generos = "Aventura, Drama",
        nota = 7.0,
        classificacao = "L",
        sinopse = "Três irmãos herdam um estaleiro falido e um barco que ninguém na cidade quer ver " +
            "de volta à água.",
        continuar = "T2:E4",
        temporadas = listOf(
            Temporada(
                numero = 1,
                episodios = listOf(
                    Episodio(1, "A volta", 48, "O estaleiro reabre as portas depois de doze anos.", assistido = true),
                    Episodio(2, "Casco", 45, "A primeira encomenda chega com um prazo impossível.", assistido = true),
                    Episodio(3, "Maré de sizígia", 51, "Uma ressaca põe o cais à prova.", assistido = true),
                ),
            ),
            Temporada(
                numero = 2,
                episodios = listOf(
                    Episodio(1, "Quilha nova", 47, "O projeto muda de mãos.", assistido = true),
                    Episodio(2, "Vento sul", 44, "Um contrato antigo reaparece.", assistido = true),
                    Episodio(3, "Âncora", 49, "A família discute a venda.", assistido = true),
                    Episodio(4, "Arrebentação", 52, "A viagem de teste não sai como o combinado.", progresso = 0.72f),
                    Episodio(5, "Calmaria", 46, "Depois da tempestade, as contas."),
                    Episodio(6, "Porto seguro", 55, "O último dia da temporada no cais."),
                ),
            ),
        ),
    )

    // ---------------------------------------------------------------- Player

    val player = EstadoPlayer(
        titulo = "Maré Alta",
        subtitulo = "T2:E4 · Arrebentação",
        posicao = "37:12",
        duracao = "52:30",
        progresso = 0.71f,
        video = "HEVC · 3840×2160 · 24 fps",
        dinamica = "Dolby Vision",
        audio = "E-AC-3 JOC · Atmos 5.1.2",
        banda = "18,4 Mb/s",
        moldura = "Moldura automática: 100%",
    )
}

// -------------------------------------------------------------------- modelos

/** Selo técnico mostrado como etiqueta nos cards e no detalhe. */
enum class Selo(val rotulo: String) {
    QUATRO_K("4K"),
    HDR10("HDR10+"),
    DOLBY_VISION("DOLBY VISION"),
    ATMOS("ATMOS"),
}

/** O destaque grande do Início, com a palavra em laranja e o ponto final do design. */
data class Destaque(
    val rotulo: String,
    val tipo: String,
    val tituloClaro: String,
    val tituloAcento: String,
    val nota: Double,
    val ano: Int,
    val duracao: String,
    val generos: String,
    val classificacao: String,
    val selos: List<Selo>,
    val sinopse: String,
)

/** Um card de "Continuar assistindo": barra de progresso e quanto falta. */
data class EmAndamento(
    val titulo: String,
    val origem: String,
    val restante: String,
    val progresso: Float,
)

/** Um programa do guia. Sem [fim] definido o card só mostra o horário de início. */
data class Programa(
    val titulo: String,
    val inicio: LocalDateTime,
    val fim: LocalDateTime,
    val descricao: String = "",
) {
    val faixa: String get() = "${CatalogoFalso.hora(inicio)} às ${CatalogoFalso.hora(fim)}"

    /** Quanto do programa já passou, de 0 a 1, no relógio fixo do catálogo. */
    val progresso: Float
        get() {
            val total = java.time.Duration.between(inicio, fim).toMinutes().toFloat()
            val corrido = java.time.Duration.between(inicio, CatalogoFalso.AGORA).toMinutes().toFloat()
            return (corrido / total).coerceIn(0f, 1f)
        }
}

/** Um canal ao vivo com o que está no ar agora e o que vem a seguir. */
data class Canal(
    val numero: Int,
    val nome: String,
    val categoria: String,
    val favorito: Boolean,
    val qualidade: String,
    val noAr: Programa,
    val aSeguir: List<Programa>,
)

data class Filme(
    val titulo: String,
    val ano: Int,
    val duracaoMin: Int,
    val generos: String,
    val nota: Double,
    val classificacao: String,
    val selos: List<Selo>,
) {
    val duracao: String get() = "${duracaoMin / 60}h${(duracaoMin % 60).toString().padStart(2, '0')}"
}

data class Episodio(
    val numero: Int,
    val titulo: String,
    val duracaoMin: Int,
    val sinopse: String,
    val assistido: Boolean = false,
    val progresso: Float = 0f,
)

data class Temporada(val numero: Int, val episodios: List<Episodio>)

data class Serie(
    val titulo: String,
    val ano: Int,
    val generos: String,
    val nota: Double,
    val classificacao: String,
    val sinopse: String,
    val continuar: String,
    val temporadas: List<Temporada>,
)

/** O painel de informações técnicas do player da Fase 4. */
data class EstadoPlayer(
    val titulo: String,
    val subtitulo: String,
    val posicao: String,
    val duracao: String,
    val progresso: Float,
    val video: String,
    val dinamica: String,
    val audio: String,
    val banda: String,
    val moldura: String,
)
