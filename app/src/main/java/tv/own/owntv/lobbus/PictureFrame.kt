package tv.own.owntv.lobbus

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.own.owntv.R

/**
 * LOBBUS: "Moldura" da imagem em tela cheia.
 *
 * Um canal Full HD numa TV 4K é ampliado 2× para ocupar a tela inteira, e a ampliação deixa a imagem
 * mais macia. A moldura desenha o vídeo um pouco menor, centralizado, com bordas pretas em volta: menos
 * ampliação, imagem mais nítida. Sempre respeita a proporção do modo de zoom escolhido (nunca estica).
 *
 * [AUTO] (padrão) aplica 90% só quando o sinal tem até 1080 linhas; conteúdo 4K continua em tela cheia.
 * Vale só para o player em tela cheia: prévias e mini-player não mudam.
 */
enum class PictureFrame(val percent: Int, @param:StringRes val labelRes: Int) {
    AUTO(90, R.string.lobbus_frame_auto),
    OFF(100, R.string.lobbus_frame_off),
    P95(95, R.string.lobbus_frame_95),
    P90(90, R.string.lobbus_frame_90),
    P85(85, R.string.lobbus_frame_85),
    P80(80, R.string.lobbus_frame_80),
    ;

    /** Escala efetiva (0 a 1) para um vídeo com [videoHeight] linhas; null quando ainda não se sabe. */
    fun scaleFor(videoHeight: Int?): Float = when (this) {
        OFF -> 1f
        AUTO -> if (videoHeight != null && videoHeight in 1..HD_MAX_LINES) percent / 100f else 1f
        else -> percent / 100f
    }

    companion object {
        /** 1080p com folga para 1088 (altura codificada de muitos streams H.264). */
        const val HD_MAX_LINES = 1088
    }
}

/** Preferência global da moldura, lida no início do app e observada pelas superfícies de vídeo. */
object PictureFramePrefs {
    private var prefs: SharedPreferences? = null
    private val state = MutableStateFlow(PictureFrame.AUTO)
    val frame: StateFlow<PictureFrame> = state.asStateFlow()

    fun init(context: Context) {
        val p = context.applicationContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        prefs = p
        state.value = p.getString(KEY_FRAME, null)
            ?.let { saved -> PictureFrame.entries.firstOrNull { it.name == saved } }
            ?: PictureFrame.AUTO
    }

    fun set(frame: PictureFrame) {
        state.value = frame
        prefs?.edit()?.putString(KEY_FRAME, frame.name)?.apply()
    }

    private const val PREFS_FILE = "lobbus_prefs"
    private const val KEY_FRAME = "picture_frame"
}
