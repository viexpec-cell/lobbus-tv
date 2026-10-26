package tv.own.owntv.lobbus.abertura

import kotlin.math.pow
import kotlin.random.Random

/**
 * LOBBUS: a linha do tempo da abertura, em segundos.
 *
 * Os números saem de `docs/lobbus/movimento/abertura/prototipo/timeline.json` e da tabela do item
 * 4b.1 em `docs/lobbus/movimento/README.md`, que é o que o Vitor aprovou em 22/10/2026. Mudou lá,
 * muda aqui, e o print da abertura mostra a diferença.
 */
object AberturaTempos {

    /** Quando a abertura larga a tela para o Início. O som ainda tem cauda até 6,0 s. */
    const val FIM = 5.10f

    val HUD = 0.00f..0.35f
    val CONTADOR = 0.25f..1.20f
    const val LOG_PRIMEIRA = 0.32f
    const val LOG_PASSO = 0.14f
    const val LOG_DECODE = 0.16f
    val CARREGADOR_SAI = 1.20f..1.48f
    val RUIDO = 1.30f..1.60f
    val MORPH = 1.38f..2.74f
    val VARREDURA = 2.68f..2.84f
    const val IMPACTO = 2.84f
    val ESFRIA = 2.84f..3.26f
    val DISSOLVE = 2.84f..3.70f
    val LOBO_DESLIZA = 2.98f..3.62f
    val PALAVRA = 3.16f..3.96f
    val PILULA = 3.50f..3.78f
    val SLOGAN = 3.62f..4.12f
    val ENCAIXE = 4.30f..4.85f
    val VOO = 4.35f..5.00f
    val TROCA = 4.98f..5.10f
}

/** Quanto de [faixa] já passou no instante [t], de 0 a 1. */
fun progresso(t: Float, faixa: ClosedFloatingPointRange<Float>): Float {
    val duracao = faixa.endInclusive - faixa.start
    if (duracao <= 0f) return if (t >= faixa.start) 1f else 0f
    return ((t - faixa.start) / duracao).coerceIn(0f, 1f)
}

// As curvas do protótipo. Mesmos nomes do GSAP, para conferir contra `intro.html` sem traduzir.

fun expoOut(x: Float): Float = if (x >= 1f) 1f else 1f - 2f.pow(-10f * x)

fun expoInOut(x: Float): Float = when {
    x <= 0f -> 0f
    x >= 1f -> 1f
    x < 0.5f -> 2f.pow(20f * x - 10f) / 2f
    else -> (2f - 2f.pow(-20f * x + 10f)) / 2f
}

fun power2In(x: Float): Float = x * x

fun power2InOut(x: Float): Float = if (x < 0.5f) 2f * x * x else 1f - (-2f * x + 2f).pow(2) / 2f

fun backOut(x: Float, forca: Float = 1.8f): Float {
    val c = forca + 1f
    val y = x - 1f
    return 1f + c * y.pow(3) + forca * y.pow(2)
}

fun smoothstep(x: Float): Float = x.coerceIn(0f, 1f).let { it * it * (3f - 2f * it) }

/** Mistura linear, usada o tempo todo no desenho. */
fun mistura(de: Float, para: Float, q: Float): Float = de + (para - de) * q

/**
 * O texto "decodificando": as letras ainda não resolvidas saem como ruído.
 *
 * É determinístico por [semente] e por posição, então o mesmo instante desenha sempre o mesmo
 * quadro e dois prints da mesma abertura saem iguais.
 */
fun decodifica(texto: String, progresso: Float, semente: Int): String {
    if (progresso >= 1f) return texto
    if (progresso <= 0f) return ""
    val resolvidas = (texto.length * progresso).toInt()
    val ruido = Random(semente + resolvidas)
    return buildString(texto.length) {
        texto.forEachIndexed { indice, letra ->
            when {
                indice < resolvidas -> append(letra)
                letra == ' ' -> append(' ')
                else -> append(ALFABETO_DE_RUIDO[ruido.nextInt(ALFABETO_DE_RUIDO.length)])
            }
        }
    }
}

private const val ALFABETO_DE_RUIDO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#%&/\\<>*+=-"
