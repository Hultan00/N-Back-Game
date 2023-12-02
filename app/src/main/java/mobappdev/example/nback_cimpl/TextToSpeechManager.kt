package mobappdev.example.nback_cimpl

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import java.util.*

class TextToSpeechManager(context: Context, private val onInitListener: OnInitListener? = null) :
    OnInitListener {

    private val textToSpeech: TextToSpeech = TextToSpeech(context, this)
    private var initialized = false

    init {
        textToSpeech.language = Locale.getDefault()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            initialized = true
            onInitListener?.onInit(status)
        } else {
            initialized = false
        }
    }

    fun speak(text: String) {
        if (initialized) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun num2Char(num: Int): Char {
        return ('A' + num - 1)
    }

    fun shutDown() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}
