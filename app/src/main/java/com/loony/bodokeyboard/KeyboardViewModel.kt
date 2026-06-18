package com.loony.bodokeyboard

import android.content.Context
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

enum class KeyboardMode { BODO, ENGLISH, EMOJI, TRANSLIT, GIF }

class KeyboardViewModel : ViewModel() {

    private var db: TransliterationDatabase? = null

    fun initDatabase(context: Context) {
        if (db == null) {
            db = TransliterationDatabase(context)
            refreshEngineRules()
        }
    }

    private fun refreshEngineRules() {
        // Pass only the user-learned whole-word overrides (e.g. "bodo" → "बर'").
        // The engine's state machine handles all standard mappings internally.
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

    /**
     * Append [char] to the Latin buffer and return a Result whose [pending]
     * is the Devanagari composing text to pass to setComposingText().
     */
    fun feedTranslit(char: Char): TransliterationEngine.Result {
        val result = translitEngine.feed(_translitBuffer.value, char)
        _translitBuffer.value = _translitBuffer.value + char  // store Latin
        return result
    }

    /**
     * Remove the last Latin keystroke and return the Devanagari equivalent
     * of the remaining buffer (pass to setComposingText()).
     */
    fun translitBackspace(): String {
        val newLatin = translitEngine.backspace(_translitBuffer.value)
        _translitBuffer.value = newLatin
        return translitEngine.flush(newLatin)  // Devanagari for composing
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

    // ── Preferences / misc ────────────────────────────────────────────────────

    val hapticEnabled             = mutableStateOf(true)
    val soundEnabled              = mutableStateOf(false)
    val keyboardHeightMultiplier  = mutableStateOf(1.0f)

    private val _imeAction     = mutableStateOf(EditorInfo.IME_ACTION_DONE)
    val imeAction: State<Int>  = _imeAction

    private val _isEmailField  = mutableStateOf(false)
    val isEmailField: State<Boolean> = _isEmailField

    private var _editorInfo: EditorInfo? = null
    val editorInfo: EditorInfo? get() = _editorInfo

    private val _suggestions   = mutableStateOf(listOf<String>())
    val suggestions: State<List<String>> = _suggestions

    private var lastShiftTime = 0L

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

    // ── Shift / caps ──────────────────────────────────────────────────────────

    fun toggleShift() {
        val now = System.currentTimeMillis()
        when {
            _isCapsLock.value -> {
                _isCapsLock.value = false
                _isShifted.value  = false
            }
            _isShifted.value && (now - lastShiftTime) < 400L -> {
                _isCapsLock.value = true
            }
            else -> _isShifted.value = !_isShifted.value
        }
        lastShiftTime = now
    }

    fun setShift(shifted: Boolean) {
        _isShifted.value = shifted
    }

    fun autoResetShift() {
        if (_isShifted.value && !_isCapsLock.value) _isShifted.value = false
    }

    fun toggleSymbols() {
        if (_isSymbols.value && !_isSymbols2.value) {
            _isSymbols2.value = true
        } else if (_isSymbols.value && _isSymbols2.value) {
            _isSymbols.value = false
            _isSymbols2.value = false
        } else {
            _isSymbols.value = true
            _isSymbols2.value = false
        }
    }

    fun toggleSymbolsPage() {
        _isSymbols2.value = !_isSymbols2.value
    }

    fun setMode(mode: KeyboardMode) {
        _keyboardMode.value = mode
        _isSymbols.value    = false
        _isSymbols2.value   = false
        _isShifted.value    = false
        _isCapsLock.value   = false
        // Discard any pending translit state on mode change
        _translitBuffer.value = ""
    }

    // ── Editor info ───────────────────────────────────────────────────────────

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

    // ── Suggestions ───────────────────────────────────────────────────────────

    /** 
     * Record a successful transliteration by breaking the word down into its
     * constituent sections/syllables and learning them individually.
     * 
     * If you type "mwina" and pick "मैना", it learns "mwi" -> "मै" and "na" -> "ना".
     */
    fun learnTransliteration(latin: String, bodo: String) {
        if (latin.isEmpty() || bodo.isEmpty()) return
        
        // Decompose the Latin input into sections that the engine understands
        val components = translitEngine.decompose(latin.lowercase())
        
        // Try to map these components to the chosen Bodo output
        // This is a simple alignment: if the decomposed Bodo matches the chosen Bodo,
        // we learn the individual components.
        val defaultBodo = translitEngine.flush(latin.lowercase())
        
        if (defaultBodo == bodo) {
            // The engine got it right! Increment counts for these sub-rules
            // so they become "stable" faster.
            components.forEach { (lPart, bPart) ->
                db?.learn(lPart, bPart)
            }
        } else {
            // User chose something different than the default engine output.
            // We learn the whole mapping as an override.
            db?.learn(latin.lowercase(), bodo)
        }

        // Refresh engine rules so new learned patterns take effect in live typing
        refreshEngineRules()
    }

    fun updateSuggestions(text: String) {
        val mode = _keyboardMode.value
        if (text.isEmpty()) {
            _suggestions.value = bodoWordList.take(3)
            return
        }

        val suggestionsList = mutableListOf<String>()
        val isLatinInput = (mode == KeyboardMode.ENGLISH || mode == KeyboardMode.TRANSLIT) &&
                           !text.any { it in '\u0900'..'\u097F' }

        if (isLatinInput) {
            // 1. The Latin word exactly as typed
            suggestionsList.add(text)

            // 2. High-priority: Rules learned from SQLite (User's personal style)
            val learned = db?.getLearnedRules(text.lowercase()) ?: emptyList()
            learned.forEach { suggestionsList.add(it.second) }
            
            // 3. The transliterated version of the Latin prefix (Static engine)
            val translitPrefix = translitEngine.flush(text.lowercase())
            if (translitPrefix != text) {
                suggestionsList.add(translitPrefix)
                
                // Add linguistic alternates from the mapping table (e.g., Anusvara vs full Nasal)
                BodoTranslitMappings.LINGUISTIC_ALTERNATES.forEach { (primary, alt) ->
                    if (translitPrefix.endsWith(primary)) {
                        suggestionsList.add(translitPrefix.substring(0, translitPrefix.length - primary.length) + alt)
                    }
                }
            }
            
            // 3. Dictionary completions based on the transliteration
            val completions = bodoWordList.filter { 
                it.startsWith(translitPrefix) && it != translitPrefix 
            }.take(3)
            suggestionsList.addAll(completions)
        } else {
            // Devanagari input (either BODO mode or already-committed TRANSLIT)
            val filtered = bodoWordList.filter { it.startsWith(text) }.take(3)
            suggestionsList.addAll(filtered)
            if (suggestionsList.isEmpty()) {
                suggestionsList.addAll(bodoWordList.take(3))
            }
        }

        _suggestions.value = suggestionsList.distinct().take(3)
    }
}
