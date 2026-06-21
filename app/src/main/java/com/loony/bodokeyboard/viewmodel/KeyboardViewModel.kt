package com.loony.bodokeyboard.viewmodel

import android.content.Context
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.loony.bodokeyboard.data.KeyboardMode
import com.loony.bodokeyboard.data.GifResult
import com.loony.bodokeyboard.data.GiphyResponse
import com.loony.bodokeyboard.transliteration.BodoTranslitMappings
import com.loony.bodokeyboard.transliteration.TransliterationDatabase
import com.loony.bodokeyboard.transliteration.TransliterationEngine
import com.loony.bodokeyboard.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import androidx.lifecycle.viewModelScope

/**
 * Holds all keyboard state and drives UI recomposition via Compose State.
 *
 * Responsibilities:
 *  - Shift / CapsLock / Symbols layout toggles
 *  - Keyboard mode switching (Bodo / English / Translit / Emoji / GIF)
 *  - Transliteration buffer management (delegates to [TransliterationEngine])
 *  - Word suggestions (static word lists + SQLite learned rules)
 *  - User preference values (haptic, sound, keyboard height)
 *  - Recently used emoji list
 */
class KeyboardViewModel : ViewModel() {

    private var db: TransliterationDatabase? = null

    fun initDatabase(context: Context) {
        if (db == null) {
            db = TransliterationDatabase(context)
            refreshEngineRules()
        }
    }

    private fun refreshEngineRules() {
        val learnedRules = db?.getAllRules() ?: emptyList()
        translitEngine.updateRules(learnedRules.associate { it.latin to it.bodo })
    }

    // ── Layout state ──────────────────────────────────────────────────────────

    private val _isShifted  = mutableStateOf(false)
    val isShifted:  State<Boolean> = _isShifted

    private val _isCapsLock = mutableStateOf(false)
    val isCapsLock: State<Boolean> = _isCapsLock

    private val _isSymbols  = mutableStateOf(false)
    val isSymbols:  State<Boolean> = _isSymbols

    private val _isSymbols2 = mutableStateOf(false)
    val isSymbols2: State<Boolean> = _isSymbols2

    private val _keyboardMode = mutableStateOf(KeyboardMode.ENGLISH)
    val keyboardMode: State<KeyboardMode> = _keyboardMode

    // ── Emoji panel ───────────────────────────────────────────────────────────

    /** Most-recently-used emoji, newest first. Kept in memory only. */
    val recentEmojis = mutableStateListOf<String>()

    fun addRecentEmoji(emoji: String) {
        recentEmojis.remove(emoji)
        recentEmojis.add(0, emoji)
        while (recentEmojis.size > 30) recentEmojis.removeAt(recentEmojis.size - 1)
    }

    // ── GIF panel ─────────────────────────────────────────────────────────────

    private val _gifs = mutableStateOf<List<GifResult>>(emptyList())
    val gifs: State<List<GifResult>> = _gifs

    private val _isGifLoading = mutableStateOf(false)
    val isGifLoading: State<Boolean> = _isGifLoading

    private var gifSearchJob: Job? = null

    fun searchGifs(query: String) {
        gifSearchJob?.cancel()
        
        gifSearchJob = viewModelScope.launch(Dispatchers.IO) {
            if (query.isNotEmpty()) {
                delay(500) // Debounce for search
            }
            _isGifLoading.value = true
            try {
                val apiKey = BuildConfig.GIPHY_API_KEY
                val urlString = if (query.isEmpty()) {
                    "https://api.giphy.com/v1/gifs/trending?api_key=$apiKey&limit=20&rating=g"
                } else {
                    "https://api.giphy.com/v1/gifs/search?api_key=$apiKey&q=$query&limit=20&rating=g"
                }
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == 200) {
                    val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                    val response = Json { ignoreUnknownKeys = true }.decodeFromString<GiphyResponse>(jsonString)
                    _gifs.value = response.data.map {
                        GifResult(
                            id = it.id,
                            url = it.images.fixed_height.url,
                            previewUrl = it.images.preview_gif.url
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGifLoading.value = false
            }
        }
    }

    // ── Transliteration ───────────────────────────────────────────────────────

    val translitEngine: TransliterationEngine by lazy { TransliterationEngine() }

    /**
     * Latin keystrokes accumulated since the last commit.
     * The engine transliterates this buffer to Devanagari on each keystroke;
     * the Devanagari is shown as composing text, not the raw Latin.
     */
    private val _translitBuffer = mutableStateOf("")
    val translitBuffer: State<String> = _translitBuffer

    fun isTranslitMode(): Boolean = _keyboardMode.value == KeyboardMode.TRANSLIT

    /** Append [char] to the Latin buffer; return a Result whose [pending] is the Devanagari composing text. */
    fun feedTranslit(char: Char): TransliterationEngine.Result {
        val result = translitEngine.feed(_translitBuffer.value, char)
        _translitBuffer.value = _translitBuffer.value + char
        return result
    }

    /** Remove the last Latin keystroke; return the Devanagari equivalent of the remaining buffer. */
    fun translitBackspace(): String {
        val newLatin = translitEngine.backspace(_translitBuffer.value)
        _translitBuffer.value = newLatin
        return translitEngine.flush(newLatin)
    }

    /** Flush the Latin buffer to Devanagari and clear it. */
    fun flushTranslit(): String {
        val committed = translitEngine.flush(_translitBuffer.value)
        _translitBuffer.value = ""
        return committed
    }

    /** Clear without committing (mode switch, field change, etc.). */
    fun clearTranslitBuffer() {
        _translitBuffer.value = ""
    }

    // ── Preferences ───────────────────────────────────────────────────────────

    val hapticEnabled            = mutableStateOf(true)
    val soundEnabled             = mutableStateOf(false)
    val keyboardHeightMultiplier = mutableStateOf(1.0f)
    val suggestionsEnabled       = mutableStateOf(true)

    // ── Editor info ───────────────────────────────────────────────────────────

    private val _imeAction    = mutableStateOf(EditorInfo.IME_ACTION_DONE)
    val imeAction: State<Int> = _imeAction

    private val _isEmailField = mutableStateOf(false)
    val isEmailField: State<Boolean> = _isEmailField

    private var _editorInfo: EditorInfo? = null
    val editorInfo: EditorInfo? get() = _editorInfo

    fun updateEditorInfo(info: EditorInfo?) {
        _editorInfo = info
        val action = (info?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
        _imeAction.value = if (action == EditorInfo.IME_ACTION_UNSPECIFIED)
            EditorInfo.IME_ACTION_DONE else action

        val inputType = info?.inputType ?: 0
        _isEmailField.value =
            (inputType and EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) != 0 ||
            (inputType and EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS) != 0

        if ((inputType and EditorInfo.TYPE_CLASS_NUMBER) != 0) _isSymbols.value = true
    }

    // ── Shift / CapsLock ──────────────────────────────────────────────────────

    private var lastShiftTime = 0L

    fun toggleShift() {
        val now = System.currentTimeMillis()
        when {
            _isCapsLock.value -> {
                _isCapsLock.value = false
                _isShifted.value  = false
            }
            _isShifted.value && (now - lastShiftTime) < 400L -> {
                // Double-tap → CapsLock
                _isCapsLock.value = true
            }
            else -> _isShifted.value = !_isShifted.value
        }
        lastShiftTime = now
    }

    fun setShift(shifted: Boolean) {
        _isShifted.value = shifted
    }

    /** Drop Shift after a printable key is typed, unless CapsLock is active. */
    fun autoResetShift() {
        if (_isShifted.value && !_isCapsLock.value) _isShifted.value = false
    }

    // ── Symbols / Mode ────────────────────────────────────────────────────────

    fun toggleSymbols() {
        if (_isSymbols.value) {
            resetSymbols()
        } else {
            _isSymbols.value = true
            _isSymbols2.value = false
        }
    }

    fun toggleSymbolsPage() {
        _isSymbols2.value = !_isSymbols2.value
    }

    fun resetSymbols() {
        _isSymbols.value = false
        _isSymbols2.value = false
    }

    fun setMode(mode: KeyboardMode) {
        _keyboardMode.value   = mode
        _isSymbols.value      = false
        _isSymbols2.value     = false
        _isShifted.value      = false
        _isCapsLock.value     = false
        _translitBuffer.value = ""
    }

    // ── Word lists ────────────────────────────────────────────────────────────

    private var bodoWordList: List<String> = listOf(
        "बड'", "बर'", "बडलेण्ड", "बिथ'राय", "गोजोन्थों", "खालाम",
        "गोजोन", "नोगोर", "माव", "मोजां", "जोबोर", "हाबा",
        "थां", "आस", "बिबार", "गोलाव", "दाउ", "फिसाज",
        "गेलेब", "आलो", "बाथ्रा", "सोरां", "मानसि", "नैथि"
    )

    private var englishWordList: List<String> = listOf(
        "Bodo", "Keyboard", "English", "Hello", "World",
        "Good", "Thank", "Please", "Sorry", "Welcome"
    )

    fun setWordLists(bodo: List<String>, english: List<String>) {
        if (bodo.isNotEmpty())    bodoWordList    = bodo
        if (english.isNotEmpty()) englishWordList = english
        updateSuggestions("")
    }

    // ── Suggestions ───────────────────────────────────────────────────────────

    private val _suggestions   = mutableStateOf(listOf<String>())
    val suggestions: State<List<String>> = _suggestions

    private var suggestionsJob: Job? = null

    fun updateSuggestions(text: String) {
        suggestionsJob?.cancel()
        suggestionsJob = viewModelScope.launch {
            // Debounce: skip intermediate keystrokes when typing fast.
            if (text.isNotEmpty()) delay(50)

            if (!suggestionsEnabled.value) {
                _suggestions.value = emptyList()
                return@launch
            }

            val mode = _keyboardMode.value
            if (text.isEmpty()) {
                _suggestions.value = bodoWordList.take(3)
                return@launch
            }

            val result = withContext(Dispatchers.Default) {
                val suggestionsList = mutableListOf<String>()
                val isLatinInput = (mode == KeyboardMode.ENGLISH || mode == KeyboardMode.TRANSLIT) &&
                                   !text.any { it in 'ऀ'..'ॿ' }

                if (isLatinInput) {
                    // 1. The Latin word exactly as typed
                    suggestionsList.add(text)

                    // 2. High-priority: Rules learned from SQLite (user's personal style)
                    val learned = withContext(Dispatchers.IO) {
                        db?.getLearnedRules(text.lowercase()) ?: emptyList()
                    }
                    learned.forEach { suggestionsList.add(it.second) }

                    // 3. The transliterated version of the Latin prefix (static engine)
                    val translitPrefix = translitEngine.flush(text.lowercase())
                    if (translitPrefix != text) {
                        suggestionsList.add(translitPrefix)

                        // Add linguistic alternates (e.g. Anusvara vs full Nasal)
                        BodoTranslitMappings.LINGUISTIC_ALTERNATES.forEach { (primary, alt) ->
                            if (translitPrefix.endsWith(primary)) {
                                suggestionsList.add(
                                    translitPrefix.substring(0, translitPrefix.length - primary.length) + alt
                                )
                            }
                        }
                    }

                    // 4. Dictionary completions based on the transliteration
                    val completions = bodoWordList.filter {
                        it.startsWith(translitPrefix) && it != translitPrefix
                    }.take(3)
                    suggestionsList.addAll(completions)
                } else {
                    // Devanagari input (BODO mode or already-committed TRANSLIT)
                    val filtered = bodoWordList.filter { it.startsWith(text) }.take(3)
                    suggestionsList.addAll(filtered)
                    if (suggestionsList.isEmpty()) suggestionsList.addAll(bodoWordList.take(3))
                }

                suggestionsList.distinct().take(3)
            }

            _suggestions.value = result
        }
    }

    // ── Learning ──────────────────────────────────────────────────────────────

    /**
     * Record a successful transliteration. Breaks the Latin input into
     * engine-recognised components and learns them individually; falls back
     * to a whole-word override when the engine output differs from the user choice.
     */
    fun learnTransliteration(latin: String, bodo: String) {
        if (latin.isEmpty() || bodo.isEmpty()) return

        val components  = translitEngine.decompose(latin.lowercase())
        val defaultBodo = translitEngine.flush(latin.lowercase())

        if (defaultBodo == bodo) {
            // Engine got it right — boost confidence of each sub-component
            components.forEach { (lPart, bPart) -> db?.learn(lPart, bPart) }
        } else {
            // User chose a different output — store as a whole-word override
            db?.learn(latin.lowercase(), bodo)
        }

        refreshEngineRules()
    }
}
