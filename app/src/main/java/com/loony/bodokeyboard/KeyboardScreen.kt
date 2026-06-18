package com.loony.bodokeyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loony.bodokeyboard.data.KeyboardMode
import com.loony.bodokeyboard.ui.emoji.EmojiKeyboard
import com.loony.bodokeyboard.ui.emoji.GifKeyboard
import com.loony.bodokeyboard.ui.keyboard.KeyButton
import com.loony.bodokeyboard.ui.keyboard.SuggestionBar
import com.loony.bodokeyboard.ui.keyboard.ToolbarRow
import com.loony.bodokeyboard.ui.theme.KbBg
import com.loony.bodokeyboard.viewmodel.KeyboardViewModel

// ── Number hints — top-right corner of Q-row keys ─────────────────────────────
private val qRowHints = mapOf(
    "q" to "1", "w" to "2", "e" to "3", "r" to "4", "t" to "5",
    "y" to "6", "u" to "7", "i" to "8", "o" to "9", "p" to "0",
    "Q" to "1", "W" to "2", "E" to "3", "R" to "4", "T" to "5",
    "Y" to "6", "U" to "7", "I" to "8", "O" to "9", "P" to "0",
)

// ── Long-press alternate characters ───────────────────────────────────────────
private val keyAlternates = mapOf(
    "ड"  to listOf("ड़"),
    "ढ"  to listOf("ढ़"),
    "क"  to listOf("क्ष", "क्त", "क्क"),
    "ज"  to listOf("ज्ञ"),
    "त"  to listOf("त्र", "त्त", "त्व"),
    "द"  to listOf("द्व", "द्ध", "द्र"),
    "श"  to listOf("श्र", "श्व"),
    "ह"  to listOf("ह्र", "ह्व"),
    "न"  to listOf("ञ", "ण"),
    "ग"  to listOf("ग्र"),
    "प"  to listOf("प्र", "प्त"),
    "ब"  to listOf("ब्र"),
    "म"  to listOf("म्र"),
    "र"  to listOf("र्", "ऋ"),
    "ल"  to listOf("ळ"),
    "स"  to listOf("स्र", "स्त", "स्व"),
    "व"  to listOf("व्र"),
    "च"  to listOf("च्च"),
    "अ"  to listOf("ॅ", "ॉ"),
    "आ"  to listOf("ा"),
    "इ"  to listOf("ि"),
    "ई"  to listOf("ी"),
    "उ"  to listOf("ु"),
    "ऊ"  to listOf("ू"),
    "ऋ"  to listOf("ृ"),
    "ए"  to listOf("े"),
    "ऐ"  to listOf("ै"),
    "ओ"  to listOf("ो"),
    "औ"  to listOf("ौ"),
    "ं"  to listOf("ँ", "ः"),
    "्"  to listOf("्र", "्व"),
    "."  to listOf("?", "!", ",", ";", ":", "(", ")", "।"),
    "1"  to listOf("१"), "2" to listOf("२"), "3" to listOf("३"),
    "4"  to listOf("४"), "5" to listOf("५"), "6" to listOf("६"),
    "7"  to listOf("७"), "8" to listOf("८"), "9" to listOf("९"),
    "0"  to listOf("०"),
    ","  to listOf("،", "、"),
    "-"  to listOf("—", "–"),
    "?"  to listOf("¿"),
    "!"  to listOf("¡"),
    "@"  to listOf(".com", ".org", ".net", ".in"),
    "'"  to listOf("'", "'"),
    "\"" to listOf("«", "»", "“", "”"),
    "a"  to listOf("á", "à", "â", "ä", "ã", "å"),
    "e"  to listOf("é", "è", "ê", "ë"),
    "i"  to listOf("í", "ì", "î", "ï"),
    "u"  to listOf("ú", "ù", "û", "ü"),
    "n"  to listOf("ñ"),
    "c"  to listOf("ç"),
    "A"  to listOf("Á", "À", "Â", "Ä"),
    "E"  to listOf("É", "È", "Ê"),
    "I"  to listOf("Í", "Ì", "Î"),
    "U"  to listOf("Ú", "Ù", "Û"),
    "N"  to listOf("Ñ"),
    "C"  to listOf("Ç"),
)

/**
 * Root composable for the keyboard. Routes to either the standard key layout,
 * the emoji picker, or the GIF panel based on [viewModel.keyboardMode].
 */
@Composable
fun KeyboardScreen(
    viewModel: KeyboardViewModel,
    onKeyClick: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onSpaceDrag: (Int) -> Unit = {}
) {
    val isShifted    = viewModel.isShifted.value
    val isCapsLock   = viewModel.isCapsLock.value
    val isSymbols    = viewModel.isSymbols.value
    val isSymbols2   = viewModel.isSymbols2.value
    val mode         = viewModel.keyboardMode.value
    val suggestions  = viewModel.suggestions.value
    val isEmailField = viewModel.isEmailField.value

    var longPressKey by remember { mutableStateOf<String?>(null) }

    val handleLongPress: (String) -> Unit = { key ->
        when (key) {
            "SPACE"     -> onKeyClick("PASTE")
            "BACKSPACE" -> {}  // handled by continuous delete inside KeyButton
            else -> {
                if (key.length == 1 && qRowHints.containsKey(key)) {
                    onKeyClick(qRowHints[key]!!)
                } else if (keyAlternates.containsKey(key)) {
                    longPressKey = key
                }
            }
        }
    }

    val isTranslit = mode == KeyboardMode.TRANSLIT

    val bodoBottomRow = if (isEmailField)
        listOf("SYM", ",", "SPACE", "@", "ENTER")
    else
        listOf("SYM", ",", "SPACE", "।", "ENTER")

    val englishBottomRow = if (isEmailField)
        listOf("SYM", ",", "SPACE", "@", ".com", "ENTER")
    else
        listOf("SYM", ",", "SPACE", ".", "ENTER")

    val rows = buildKeyRows(
        isSymbols    = isSymbols,
        isSymbols2   = isSymbols2,
        isTranslit   = isTranslit,
        isShifted    = isShifted,
        mode         = mode,
        bodoBottomRow    = bodoBottomRow,
        englishBottomRow = englishBottomRow,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(KbBg)
    ) {
        when (mode) {
            KeyboardMode.EMOJI -> EmojiKeyboard(viewModel, onKeyClick)
            KeyboardMode.GIF   -> GifKeyboard(onKeyClick)
            else -> {
                SuggestionBar(
                    suggestions   = suggestions,
                    longPressKey  = longPressKey,
                    longPressAlts = longPressKey?.let { keyAlternates[it] } ?: emptyList(),
                    onSuggestion  = onSuggestionClick,
                    onAlternate   = { onKeyClick(it); longPressKey = null },
                    onDismissAlts = { longPressKey = null }
                )

                ToolbarRow(mode = mode, onKeyClick = onKeyClick)

                KeyRows(
                    rows         = rows,
                    isQwerty     = mode == KeyboardMode.ENGLISH || isTranslit,
                    isSymbols    = isSymbols,
                    mode         = mode,
                    isCapsLock   = isCapsLock,
                    viewModel    = viewModel,
                    onSpaceDrag  = onSpaceDrag,
                    onLongPress  = handleLongPress,
                    onKeyClick   = onKeyClick,
                )
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

/** Renders all key rows with correct spacing and QWERTY row-2 centering padding. */
@Composable
private fun KeyRows(
    rows: List<List<String>>,
    isQwerty: Boolean,
    isSymbols: Boolean,
    mode: KeyboardMode,
    isCapsLock: Boolean,
    viewModel: KeyboardViewModel,
    onSpaceDrag: (Int) -> Unit,
    onLongPress: (String) -> Unit,
    onKeyClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        rows.forEachIndexed { rowIdx, row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Gboard-style centering for QWERTY row 2 (9 keys)
                if (isQwerty && rowIdx == 1 && row.size == 9 && !isSymbols) {
                    Spacer(Modifier.weight(0.5f))
                }

                row.forEach { key ->
                    val hint = if (isQwerty && rowIdx == 0 && !isSymbols) qRowHints[key] else null
                    KeyButton(
                        key         = key,
                        hint        = hint,
                        modifier    = Modifier.weight(keyWeight(key)),
                        mode        = mode,
                        isCapsLock  = isCapsLock,
                        viewModel   = viewModel,
                        onSpaceDrag = onSpaceDrag,
                        onLongPress = onLongPress,
                        onClick     = { onKeyClick(key) }
                    )
                }

                if (isQwerty && rowIdx == 1 && row.size == 9 && !isSymbols) {
                    Spacer(Modifier.weight(0.5f))
                }
            }
        }
    }
}

/** Returns the flex weight for a key based on its identifier. */
private fun keyWeight(key: String): Float = when (key) {
    "SPACE"                       -> 5.5f
    "BACKSPACE", "ENTER", "SHIFT" -> 1.9f
    "SYM", "ABC", "MODE_SWITCH"   -> 1.55f
    "SYM_PAGE"                    -> 1.2f
    ".com"                        -> 1.2f
    else                          -> 1f
}

/** Builds the key row data for the current layout state (no rendering). */
private fun buildKeyRows(
    isSymbols: Boolean,
    isSymbols2: Boolean,
    isTranslit: Boolean,
    isShifted: Boolean,
    mode: KeyboardMode,
    bodoBottomRow: List<String>,
    englishBottomRow: List<String>,
): List<List<String>> = when {
    isSymbols2 -> listOf(
        listOf("~", "`", "|", "•", "√", "π", "÷", "×", "{", "}"),
        listOf("£", "¢", "€", "¥", "^", "°", "=", "_", "\\"),
        listOf("SYM_PAGE", "[", "]", "«", "»", "!", "?", "BACKSPACE"),
        listOf("ABC", ",", "SPACE", ".", "ENTER")
    )
    isSymbols -> listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "₹", "%", "&", "-", "+", "(", ")"),
        listOf("SYM_PAGE", "*", "\"", "'", ":", ";", "!", "?", "BACKSPACE"),
        listOf("ABC", ",", "SPACE", ".", "ENTER")
    )
    isTranslit || mode == KeyboardMode.ENGLISH -> if (!isShifted) listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "BACKSPACE"),
        englishBottomRow
    ) else listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
        listOf("SHIFT", "Z", "X", "C", "V", "B", "N", "M", "BACKSPACE"),
        englishBottomRow
    )
    isShifted -> listOf(
        listOf("अ", "आ", "इ", "ई", "उ", "ऊ", "ऋ", "ए", "ऐ", "ओ", "औ"),
        listOf("ा", "ि", "ी", "ु", "ू", "ृ", "े", "ै", "ो", "ौ"),
        listOf("ं", "ः", "ँ", "्", "।", "॥", "०", "१", "२", "३"),
        listOf("SHIFT", "४", "५", "६", "७", "८", "९", "BACKSPACE"),
        bodoBottomRow
    )
    else -> listOf(
        listOf("क", "ख", "ग", "घ", "ङ", "च", "छ", "ज", "झ", "ञ"),
        listOf("ट", "ठ", "ड", "ढ", "ण", "त", "थ", "द", "ध", "न"),
        listOf("प", "फ", "ब", "भ", "म", "य", "र", "ल", "व"),
        listOf("SHIFT", "श", "ष", "स", "ह", "ड़", "ढ़", "क्ष", "BACKSPACE"),
        bodoBottomRow
    )
}
