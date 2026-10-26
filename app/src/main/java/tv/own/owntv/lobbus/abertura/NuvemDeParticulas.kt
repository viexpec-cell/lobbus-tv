package tv.own.owntv.lobbus.abertura

import android.content.Context
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import org.json.JSONObject
import tv.own.owntv.R

/**
 * LOBBUS: a nuvem de partículas que vira o lobo na abertura.
 *
 * Os pontos de destino são os mesmos do protótipo aprovado: `res/raw/lobbus_lobo_pontos.json`, que
 * veio de `docs/lobbus/movimento/abertura/prototipo/assets/wolf_pts.json`, em coordenadas de -0,5 a
 * 0,5 com a proporção do desenho. O protótipo usa 9000 pontos; na TV começamos com [NA_TV], que o
 * item 4b.1 manda cair para 3000 se a TCL não segurar 60 quadros por segundo.
 *
 * Tudo é determinístico: mesma semente, mesmo quadro. Sem isso dois prints da mesma abertura sairiam
 * diferentes e não daria para comparar com o vídeo de referência.
 */
class NuvemDeParticulas(
    destinos: FloatArray,
    val proporcao: Float,
    semente: Long = SEMENTE,
) {
    /** Quantas partículas a nuvem desenha. */
    val quantidade: Int = destinos.size / 2

    private val destinoX = FloatArray(quantidade)
    private val destinoY = FloatArray(quantidade)

    /** Onde cada partícula nasce, em coordenadas de -0,5 a 0,5. */
    private val origemX = FloatArray(quantidade)
    private val origemY = FloatArray(quantidade)

    /** Atraso de cada partícula, de 0 a 0,45 do trajeto (item 4b.1). */
    private val atraso = FloatArray(quantidade)

    /** Turbulência no meio do caminho, para a nuvem não andar em linha reta. */
    private val desvioX = FloatArray(quantidade)
    private val desvioY = FloatArray(quantidade)

    /** Para onde cada partícula se espalha depois do impacto. */
    private val fugaX = FloatArray(quantidade)
    private val fugaY = FloatArray(quantidade)

    init {
        val sorteio = Random(semente)
        for (i in 0 until quantidade) {
            destinoX[i] = destinos[i * 2]
            destinoY[i] = destinos[i * 2 + 1]
            // Nuvem inicial: um anel largo em volta do centro, como no protótipo.
            val angulo = sorteio.nextFloat() * 2f * Math.PI.toFloat()
            val raio = 0.35f + sorteio.nextFloat() * 0.55f
            origemX[i] = cos(angulo) * raio
            origemY[i] = sin(angulo) * raio * 0.72f
            atraso[i] = sorteio.nextFloat() * 0.45f
            desvioX[i] = (sorteio.nextFloat() - 0.5f) * 0.22f
            desvioY[i] = (sorteio.nextFloat() - 0.5f) * 0.22f
            val anguloDeFuga = sorteio.nextFloat() * 2f * Math.PI.toFloat()
            val forca = 0.25f + sorteio.nextFloat() * 0.45f
            fugaX[i] = cos(anguloDeFuga) * forca
            fugaY[i] = sin(anguloDeFuga) * forca
        }
    }

    /**
     * Escreve em [saida] as posições das partículas no instante [t], já em pixels da tela, prontas
     * para `drawPoints`. [saida] tem que ter `quantidade * 2` casas e é reaproveitado entre quadros:
     * alocar 10 mil floats por quadro é justamente o que derruba os 60 qps.
     *
     * Devolve quantas casas de [saida] valem neste quadro.
     */
    fun posicoesEm(
        t: Float,
        centroX: Float,
        centroY: Float,
        altura: Float,
        saida: FloatArray,
    ): Int {
        val giro = mistura(1.1f, 0f, smoothstep(progresso(t, AberturaTempos.MORPH)))
        val espalha = progresso(t, AberturaTempos.DISSOLVE)
        val largura = altura * proporcao
        val cos = cos(giro)
        val sin = sin(giro)
        var casa = 0
        for (i in 0 until quantidade) {
            // Cada partícula percorre o trajeto na sua hora: o atraso encurta o tempo útil dela.
            val bruto = progresso(t, AberturaTempos.MORPH)
            val andamento = ((bruto - atraso[i]) / (1f - atraso[i])).coerceIn(0f, 1f)
            val caminho = expoInOut(andamento)
            // Turbulência: vale no meio do trajeto e zera nas pontas.
            val turbulencia = sin(caminho * Math.PI.toFloat())
            var x = mistura(origemX[i], destinoX[i], caminho) + desvioX[i] * turbulencia
            var y = mistura(origemY[i], destinoY[i], caminho) + desvioY[i] * turbulencia
            // O giro de 1,1 rad some conforme a nuvem fecha no lobo.
            val giradoX = x * cos - y * sin
            val giradoY = x * sin + y * cos
            x = giradoX
            y = giradoY
            if (espalha > 0f) {
                val forca = power2In(espalha)
                x += fugaX[i] * forca
                y += fugaY[i] * forca
            }
            saida[casa++] = centroX + x * largura
            saida[casa++] = centroY + y * altura
        }
        return casa
    }

    companion object {
        /** Quantas partículas a TV desenha. Cai para 3000 se a TCL não segurar 60 qps (item 4b.1). */
        const val NA_TV = 5000

        private const val SEMENTE = 20261022L

        /**
         * Lê os pontos do lobo de `res/raw`. O arquivo tem 9000 pontos; pegamos um a cada N para
         * chegar em [quantidade] sem perder a forma, porque os pontos vêm embaralhados na origem.
         */
        fun doRecurso(contexto: Context, quantidade: Int = NA_TV): NuvemDeParticulas {
            val texto = contexto.resources.openRawResource(R.raw.lobbus_lobo_pontos)
                .bufferedReader()
                .use { it.readText() }
            val json = JSONObject(texto)
            val proporcao = json.getDouble("aspect").toFloat()
            val pts = json.getJSONArray("pts")
            val disponiveis = pts.length() / 2
            val usados = quantidade.coerceAtMost(disponiveis)
            val passo = disponiveis / usados
            val destinos = FloatArray(usados * 2)
            for (i in 0 until usados) {
                val origem = i * passo
                destinos[i * 2] = pts.getDouble(origem * 2).toFloat()
                destinos[i * 2 + 1] = pts.getDouble(origem * 2 + 1).toFloat()
            }
            return NuvemDeParticulas(destinos, proporcao)
        }
    }
}
