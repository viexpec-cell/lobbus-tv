package tv.own.owntv.lobbus.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.Text
import tv.own.owntv.lobbus.lobbusLocale
import tv.own.owntv.R
import tv.own.owntv.ui.components.OwnTVIcon
import tv.own.owntv.ui.theme.LobbusMonoFamily
import tv.own.owntv.ui.theme.StageColors
import tv.own.owntv.ui.theme.StageRadii
import tv.own.owntv.ui.theme.mpx
import tv.own.owntv.ui.theme.mpxSp
import tv.own.owntv.ui.theme.stageAccent
import tv.own.owntv.ui.theme.stageText

/**
 * LOBBUS: as peças de assinatura do destaque do Início, do protótipo aprovado
 * (`docs/lobbus/design/LobbusTV-01-inicio.jpg`).
 *
 * São composables sem estado: recebem texto pronto e não sabem de ViewModel, banco nem rede. Por
 * isso a mesma peça serve para o app com dados de verdade e para os prints da máquina de telas
 * (`docs/lobbus/PRINTS.md`), sem nenhum desvio só para teste.
 *
 * Quem monta o destaque continua sendo o `HomeScreen` do upstream: ele mede, posiciona, guarda o
 * foco e sabe que item está na vez. Daqui saem só a sobrancelha, o título, a linha de informações e
 * o título de fileira.
 */

/** Palavras de ligação que o protótipo desenha em contorno, no meio de um título de três ou mais. */
private val LIGACOES = setOf(
    "a", "as", "ao", "aos", "com", "da", "das", "de", "do", "dos", "e", "em",
    "na", "nas", "no", "nos", "o", "os", "para", "por", "um", "uma",
)

/** Tracking do design system para rótulos em caixa alta: 0,15em. */
private const val TRACKING_MONO = 0.15f

/**
 * Rótulo em Geist Mono, caixa alta e tracking largo: o tipo de letra que o design system reserva
 * para números, siglas e etiquetas técnicas.
 */
@Composable
fun LobbusRotuloMono(
    texto: String,
    cor: Color = StageColors.Dim,
    tamanho: Int = 15,
    peso: Int = 500,
    modifier: Modifier = Modifier,
) {
    Text(
        text = texto,
        color = cor,
        style = stageText(tamanho, peso, letterSpacing = (tamanho * TRACKING_MONO).mpxSp),
        fontFamily = LobbusMonoFamily,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/**
 * A sobrancelha do destaque: `[01] EM ALTA HOJE ──── FILME`.
 *
 * O número é a ordem da seção na tela, não a posição do título em nenhuma lista: o destaque é
 * sempre `[01]`, a primeira fileira `[02]`, e assim por diante.
 */
@Composable
fun LobbusSobrancelha(ordem: Int, rotulo: String, tipo: String?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.mpx),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LobbusRotuloMono(stringResource(R.string.lobbus_indice_secao, ordem), stageAccent.accent)
        LobbusRotuloMono(rotulo.uppercase(lobbusLocale()), StageColors.Text)
        if (tipo != null) {
            Box(
                Modifier
                    .width(56.mpx)
                    .height(1.mpx)
                    .background(StageColors.Ink.copy(alpha = 0.18f)),
            )
            LobbusRotuloMono(tipo.uppercase(lobbusLocale()), StageColors.Muted)
        }
    }
}

/**
 * Os dois tratamentos de título do protótipo. O que muda entre eles é onde o laranja começa e se as
 * palavras de ligação saem em contorno.
 */
enum class LobbusTitulo {
    /**
     * O destaque do Início: a última palavra em laranja, as de ligação em contorno e o resto cheio
     * ("Linha" cheio, "de" em contorno, "fogo." laranja).
     */
    DESTAQUE,

    /**
     * O cabeçalho de uma tela inteira: sem contorno, e o laranja pega a ligação colada na última
     * palavra, porque ali ela faz parte da expressão ("Canais" cheio, "ao vivo." laranja). Nos
     * cabeçalhos em que a última palavra vem sozinha o resultado é o mesmo de sempre ("O que está"
     * cheio, "passando." laranja).
     */
    CABECALHO,

    /**
     * O título de uma fileira: sem contorno e o laranja só na última palavra. A ligação fica cheia,
     * porque aqui ela não forma expressão com o que vem depois ("Top 10 da" cheio, "semana."
     * laranja, como o protótipo de Filmes desenha).
     */
    FILEIRA,
}

/**
 * O título como o protótipo o desenha, no tratamento pedido por [tratamento].
 *
 * O ponto final é da identidade da Lobbus ("Linha de fogo."), então entra aqui e não no dado.
 */
@Composable
fun lobbusTituloDestaque(
    titulo: String,
    tratamento: LobbusTitulo = LobbusTitulo.DESTAQUE,
): AnnotatedString {
    val acento = stageAccent.accent
    val contorno = with(LocalDensity.current) { 2.mpx.toPx() }
    val palavras = titulo.trim().split(" ").filter { it.isNotBlank() }
    val ultima = palavras.lastIndex
    // Num cabeçalho, uma ligação colada na última palavra entra no laranja junto com ela: o
    // protótipo escreve "Canais ao vivo.", não "Canais ao vivo." com o "ao" de fora.
    val inicioDoAcento = if (
        tratamento == LobbusTitulo.CABECALHO &&
        palavras.size >= 3 &&
        palavras[ultima - 1].lowercase(lobbusLocale()) in LIGACOES
    ) {
        ultima - 1
    } else {
        ultima
    }
    // O ponto final sai de um recurso, não de um literal solto: o `verifyI18nLiterals` só deixa
    // passar texto visível que veio do XML. Resolvido aqui, fora do construtor, que não é
    // @Composable.
    val fecho = palavras.lastOrNull()?.let { stringResource(R.string.lobbus_titulo_ponto_final, it) }.orEmpty()
    return buildAnnotatedString {
        palavras.forEachIndexed { indice, palavra ->
            val ligacao = tratamento == LobbusTitulo.DESTAQUE &&
                palavras.size >= 3 &&
                indice != 0 &&
                indice != ultima &&
                palavra.lowercase(lobbusLocale()) in LIGACOES
            val estilo = when {
                indice >= inicioDoAcento -> SpanStyle(color = acento)
                ligacao -> SpanStyle(drawStyle = Stroke(width = contorno))
                else -> SpanStyle(color = StageColors.Text)
            }
            withStyle(estilo) {
                append(if (indice == ultima) fecho else palavra)
            }
            if (indice != ultima) append(" ")
        }
    }
}

/** O título do destaque, em 112/900 e no máximo duas linhas. [onLinhas] conta as linhas desenhadas. */
@Composable
fun LobbusTituloHeroi(titulo: String, modifier: Modifier = Modifier, onLinhas: (Int) -> Unit = {}) {
    Text(
        text = lobbusTituloDestaque(titulo),
        style = stageText(112, 900, letterSpacing = (-3).mpxSp).copy(
            lineHeight = 112.mpxSp,
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
        ),
        color = StageColors.Text,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { onLinhas(it.lineCount) },
        // A mesma medida da sinopse. No protótipo o título quebra em duas linhas ("Linha de" e
        // "fogo."), e é essa quebra que põe o laranja sozinho na segunda linha. Com os 900 px da
        // coluna do destaque, um título curto cabe inteiro numa linha e a assinatura se perde.
        modifier = modifier.widthIn(max = 760.mpx),
    )
}

/** Etiqueta técnica do destaque: a classificação indicativa, "4K", "DOLBY VISION", "ATMOS". */
@Composable
private fun LobbusEtiqueta(texto: String, cheia: Boolean, raio: Dp) {
    val fundo = if (cheia) StageColors.Ink.copy(alpha = 0.10f) else Color.Transparent
    val borda = if (cheia) Color.Transparent else StageColors.Ink.copy(alpha = 0.18f)
    Box(
        Modifier
            .clip(RoundedCornerShape(raio))
            .background(fundo)
            .border(1.mpx, borda, RoundedCornerShape(raio))
            .padding(horizontal = 12.mpx, vertical = 5.mpx),
        contentAlignment = Alignment.Center,
    ) {
        LobbusRotuloMono(texto, if (cheia) StageColors.Text else StageColors.Muted, tamanho = 13)
    }
}

/** O ponto que separa as partes da linha de informações. */
@Composable
private fun LobbusSeparador() {
    Box(
        Modifier
            .size(4.mpx)
            .clip(CircleShape)
            .background(StageColors.Muted.copy(alpha = 0.45f)),
    )
}

/**
 * A linha de informações do destaque: nota, ano, duração e gêneros, depois a classificação
 * indicativa e as etiquetas de qualidade do sinal.
 *
 * Cada parte é opcional: o destaque mostra o que o título tem, sem buraco quando falta.
 */
@Composable
fun LobbusMetaHeroi(
    nota: String?,
    partes: List<String>,
    classificacao: String?,
    etiquetas: List<String>,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.mpx),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (nota != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.mpx),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OwnTVIcon(OwnTVIcon.STAR, tint = stageAccent.accent, filled = true, modifier = Modifier.size(21.mpx))
                Text(nota, style = stageText(21, 800), color = StageColors.Text, maxLines = 1)
            }
        }
        partes.forEachIndexed { indice, parte ->
            if (indice > 0 || nota != null) LobbusSeparador()
            Text(
                parte,
                style = stageText(21, 600),
                color = StageColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (classificacao != null) LobbusEtiqueta(classificacao, cheia = false, raio = StageRadii.Plate)
        etiquetas.forEachIndexed { indice, etiqueta ->
            LobbusEtiqueta(etiqueta, cheia = indice == 0, raio = StageRadii.Pill)
        }
    }
}

/** A sinopse do destaque: quatro linhas no máximo, na largura que o protótipo dá a ela. */
@Composable
fun LobbusSinopse(texto: String, maxLinhas: Int) {
    Text(
        texto,
        style = stageText(22, 400).copy(lineHeight = (22 * 1.55f).mpxSp),
        color = StageColors.Body,
        maxLines = maxLinhas,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.widthIn(max = 760.mpx),
    )
}

/**
 * O título de uma fileira, no desenho da Lobbus: a última palavra em laranja com ponto final e, ao
 * lado, o contador em Geist Mono ("[02] 5 TÍTULOS").
 */
@Composable
fun LobbusTituloFileira(titulo: String, contador: String?, ordem: Int?, altura: Dp, inicio: Dp) {
    Row(
        Modifier.padding(start = inicio).height(altura),
        horizontalArrangement = Arrangement.spacedBy(14.mpx),
    ) {
        Text(
            text = lobbusTituloDestaque(titulo, LobbusTitulo.FILEIRA),
            style = stageText(30, 800),
            color = StageColors.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.alignByBaseline(),
        )
        if (contador != null) {
            LobbusRotuloMono(
                texto = if (ordem != null) {
                    stringResource(R.string.lobbus_fileira_contador, ordem, contador)
                } else {
                    contador
                },
                cor = StageColors.Dim,
                tamanho = 13,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}
