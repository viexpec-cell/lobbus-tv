package tv.own.owntv.lobbus.abertura

import android.media.MediaPlayer
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import tv.own.owntv.lobbus.lobbusLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tv.own.owntv.R

/**
 * LOBBUS: o que a abertura escreve nos cantos e no log.
 *
 * O item 4b.1 manda mostrar dados de verdade do banco local. Quem monta isso é quem chama, com
 * [lembrarDadosDaAbertura]: aqui dentro só há texto pronto, para a abertura continuar desenhável
 * sem app em volta (é assim que a máquina de prints a fotografa).
 */
data class DadosDaAbertura(
    val nucleo: String,
    val relogio: String,
    val coordenadas: String,
    val estado: String,
    /** Formato do contador, com %1$s para o número: "INICIANDO %1$s". */
    val iniciando: String,
    /** Formato de uma linha do log, com a chave e o valor: "%1$s: %2$s". */
    val formatoDoLog: String,
    val slogan: String,
    val linhasDeLog: List<Pair<String, String>>,
)

/**
 * Monta os dados da abertura com o que o app sabe agora.
 *
 * Os valores que ainda não chegaram do banco entram como "AGUARDANDO", que é o mesmo texto que o
 * protótipo usa quando não há fonte cadastrada: a abertura nunca espera o banco para começar.
 */
@Composable
fun lembrarDadosDaAbertura(
    versao: String,
    fonteConectada: Boolean?,
    canais: Int?,
    filmes: Int?,
    series: Int?,
    fimDoGuia: String?,
): DadosDaAbertura {
    val aguardando = stringResource(R.string.lobbus_abertura_aguardando)
    val ok = stringResource(R.string.lobbus_abertura_ok)
    val conectada = stringResource(R.string.lobbus_abertura_conectada)
    val semGuia = stringResource(R.string.lobbus_abertura_sem_guia)
    val idioma = lobbusLocale()
    val formato = remember(idioma) { NumberFormat.getIntegerInstance(idioma) }
    fun numero(valor: Int?): String = valor?.let { formato.format(it) } ?: aguardando
    return DadosDaAbertura(
        nucleo = stringResource(R.string.lobbus_abertura_nucleo, versao),
        relogio = LocalDateTime.now().format(RELOGIO).uppercase(idioma),
        coordenadas = stringResource(R.string.lobbus_abertura_coordenadas),
        estado = stringResource(R.string.lobbus_abertura_estado),
        iniciando = stringResource(R.string.lobbus_abertura_iniciando),
        formatoDoLog = stringResource(R.string.lobbus_abertura_log_linha),
        slogan = stringResource(R.string.lobbus_abertura_slogan),
        linhasDeLog = listOf(
            stringResource(R.string.lobbus_abertura_log_nucleo) to ok,
            stringResource(R.string.lobbus_abertura_log_fonte) to
                when (fonteConectada) {
                    true -> conectada
                    false -> stringResource(R.string.lobbus_abertura_offline)
                    null -> aguardando
                },
            stringResource(R.string.lobbus_abertura_log_canais) to numero(canais),
            stringResource(R.string.lobbus_abertura_log_guia) to (fimDoGuia ?: semGuia),
            stringResource(R.string.lobbus_abertura_log_filmes) to numero(filmes),
            stringResource(R.string.lobbus_abertura_log_series) to numero(series),
        ),
    )
}

private val RELOGIO = DateTimeFormatter.ofPattern("EEE · dd MMM · HH:mm:ss")

/** Teto de avanço por quadro: 1/24 s. Acima disso a animação atrasa, em vez de pular. */
private const val PASSO_MAXIMO = 1f / 24f

/**
 * A abertura rodando: relógio próprio, som e a tecla que pula.
 *
 * Regras do item 4b.1 que valem aqui: toca só na abertura a frio (quem chama decide isso), qualquer
 * tecla pula e o som sai em 150 ms, e o foco do controle só vale depois que a assinatura pousa,
 * porque até lá esta tela é quem segura o foco.
 *
 * [comSom] desligado é a opção "Sem som" das Configurações.
 */
@Composable
fun LobbusAberturaAnimada(
    dados: DadosDaAbertura,
    comSom: Boolean,
    onTerminou: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contexto = LocalContext.current
    val foco = remember { FocusRequester() }
    val escopo = rememberCoroutineScope()
    var tempo by remember { mutableFloatStateOf(0f) }
    var terminou by remember { mutableStateOf(false) }

    // A nuvem lê e interpreta 149 KB de pontos, e o MediaPlayer abre o arquivo de som. As duas
    // coisas fora da linha principal: na abertura a frio ela está ocupada subindo banco, Koin e a
    // primeira composição, e qualquer trabalho a mais ali vira engasgo na cara do usuário.
    var nuvem by remember { mutableStateOf<NuvemDeParticulas?>(null) }
    var tocador by remember { mutableStateOf<MediaPlayer?>(null) }
    var pronta by remember { mutableStateOf(false) }

    LaunchedEffect(contexto, comSom) {
        val carregada = withContext(Dispatchers.Default) { NuvemDeParticulas.doRecurso(contexto) }
        val som = if (comSom) {
            withContext(Dispatchers.IO) { runCatching { MediaPlayer.create(contexto, R.raw.lobbus_abertura) }.getOrNull() }
        } else {
            null
        }
        nuvem = carregada
        tocador = som
        pronta = true
    }

    fun encerrar() {
        if (terminou) return
        terminou = true
        val som = tocador
        if (som != null && som.isPlaying) {
            // Som sai em 150 ms, em vez de cortar seco.
            escopo.launch {
                val passos = 10
                repeat(passos) { passo ->
                    val volume = 1f - (passo + 1f) / passos
                    runCatching { som.setVolume(volume, volume) }
                    delay(15)
                }
                runCatching { som.stop() }
            }
        }
        onTerminou()
    }

    DisposableEffect(tocador) {
        onDispose {
            runCatching { tocador?.stop() }
            tocador?.release()
        }
    }

    // O relógio da abertura só começa quando tudo está carregado e um quadro já foi desenhado, e o
    // som começa junto com ele, não antes.
    LaunchedEffect(pronta) {
        if (!pronta) return@LaunchedEffect
        runCatching { foco.requestFocus() }
        withFrameNanos { }
        runCatching { tocador?.start() }
        var anterior = withFrameNanos { it }
        while (true) {
            val agora = withFrameNanos { it }
            // Um quadro demorado atrasa a abertura, nunca pula pedaço dela: sem este limite, um
            // segundo preso na partida saltava o carregador inteiro e a animação "sumia".
            val passo = ((agora - anterior) / 1_000_000_000f).coerceAtMost(PASSO_MAXIMO)
            anterior = agora
            tempo += passo
            if (tempo >= AberturaTempos.FIM) break
        }
        encerrar()
    }

    LobbusAbertura(
        tempo = tempo,
        dados = dados,
        nuvem = nuvem,
        modifier = modifier
            .fillMaxSize()
            .focusRequester(foco)
            .focusable()
            .onKeyEvent { evento ->
                if (evento.type == KeyEventType.KeyDown) {
                    encerrar()
                    true
                } else {
                    false
                }
            },
    )
}
