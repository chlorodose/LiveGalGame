package com.example.livegg1.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.Locale

class KeywordSpeechListener(
    private val context: Context,
    private val keyword: String = "吗"
) : RecognitionListener {

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val handler = Handler(Looper.getMainLooper())
    private val recognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINA.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
    }

    private val _keywordTriggers = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val keywordTriggers: SharedFlow<Unit> = _keywordTriggers

    private var isListening: Boolean = false

    init {
        speechRecognizer.setRecognitionListener(this)
    }

    fun startListening() {
        if (isListening) return
        isListening = true
        safelyStartListening()
    }

    fun stopListening() {
        if (!isListening) return
        isListening = false
        handler.removeCallbacksAndMessages(null)
        runCatching {
            speechRecognizer.stopListening()
            speechRecognizer.cancel()
        }
    }

    fun release() {
        stopListening()
        speechRecognizer.destroy()
    }

    private fun safelyStartListening(delayMillis: Long = 0L) {
        if (!isListening) return
        handler.postDelayed(
            {
                runCatching {
                    speechRecognizer.startListening(recognizerIntent)
                }.onFailure { error ->
                    Log.e(TAG, "startListening failed: ${error.message}", error)
                    safelyStartListening(RETRY_DELAY)
                }
            },
            delayMillis
        )
    }

    private fun handleResults(bundle: Bundle?) {
        if (bundle == null) return
        val matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
        if (matches.any { it.contains(keyword) }) {
            _keywordTriggers.tryEmit(Unit)
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "Speech recognizer ready")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "Speech detected")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // No-op
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // No-op
    }

    override fun onEndOfSpeech() {
        Log.d(TAG, "Speech ended")
    }

    override fun onError(error: Int) {
        Log.w(TAG, "Speech recognizer error: $error")
        if (isListening) safelyStartListening(RETRY_DELAY)
    }

    override fun onResults(results: Bundle?) {
        handleResults(results)
        if (isListening) safelyStartListening(RESTART_DELAY)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        handleResults(partialResults)
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // No-op
    }

    private companion object {
        const val TAG = "KeywordSpeechListener"
        const val RETRY_DELAY = 750L
        const val RESTART_DELAY = 300L
    }
}
