@file:OptIn(ExperimentalFoundationApi::class)

package com.loony.bodokeyboard

import android.view.HapticFeedbackConstants
import android.view.inputmethod.EditorInfo
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs

// ── Palette ──────────────────────────────────────────────────────────────────
private val KbBg       = Color(0xFF171717)   // Gboard-like dark background
private val KeyNorm    = Color(0xFF303134)   // Gboard-like key color
private val KeyPressed = Color(0xFF424346)   
private val KeySpec    = Color(0xFF1F1F1F)   // special keys
private val KeySpecP   = Color(0xFF303134)   
private val EnterBg    = Color(0xFF8AB4F8)   // Gboard-like blue enter
private val EnterBgP   = Color(0xFF669DF6)   
private val CapsActive = Color(0xFF8AB4F8)   
private val SuggBg     = Color(0xFF171717)   
private val SuggTxt    = Color(0xFFE8EAED)   
private val DividerC   = Color(0xFF3C4043)   
private val ChipHighBg = Color(0xFF8AB4F8)   
private val ChipBg     = Color(0xFF303134)   
private val KeyTxt     = Color(0xFFE8EAED)   
private val HintTxt    = Color(0xFF9AA0A6)   
private val ToolTxt    = Color(0xFF9AA0A6)   

private val KeyShape   = RoundedCornerShape(6.dp)
private val ChipShape  = RoundedCornerShape(50)

// ── Number hints shown in the top-right corner of Q-row keys ─────────────────
private val qRowHints = mapOf(
    "q" to "1", "w" to "2", "e" to "3", "r" to "4", "t" to "5",
    "y" to "6", "u" to "7", "i" to "8", "o" to "9", "p" to "0",
    "Q" to "1", "W" to "2", "E" to "3", "R" to "4", "T" to "5",
    "Y" to "6", "U" to "7", "I" to "8", "O" to "9", "P" to "0",
)

// ── Alternates map ───────────────────────────────────────────────────────────
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
    "'"  to listOf("‘", "’"),
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

// ── KeyboardScreen ────────────────────────────────────────────────────────────
@Composable
fun KeyboardScreen(
    viewModel: KeyboardViewModel,
    onKeyClick: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onSpaceDrag: (Int) -> Unit = {}
) {
    val isShifted      = viewModel.isShifted.value
    val isCapsLock     = viewModel.isCapsLock.value
    val isSymbols      = viewModel.isSymbols.value
    val isSymbols2     = viewModel.isSymbols2.value
    val mode           = viewModel.keyboardMode.value
    val suggestions    = viewModel.suggestions.value
    val isEmailField   = viewModel.isEmailField.value

    var longPressKey by remember { mutableStateOf<String?>(null) }

    val handleLongPress: (String) -> Unit = { key ->
        when (key) {
            "SPACE"     -> onKeyClick("PASTE")
            "BACKSPACE" -> {} // Handled by continuous delete in KeyButton
            else        -> {
                if (key.length == 1 && qRowHints.containsKey(key)) {
                    onKeyClick(qRowHints[key]!!)
                } else if (keyAlternates.containsKey(key)) {
                    longPressKey = key
                }
            }
        }
    }

    // TRANSLIT mode uses English QWERTY layout for Latin input → Bodo output
    val isTranslit = mode == KeyboardMode.TRANSLIT

    val bodoBottomRow = if (isEmailField)
        listOf("SYM", ",", "SPACE", "@", "ENTER")
    else
        listOf("SYM", ",", "SPACE", "।", "ENTER")

    val englishBottomRow = if (isEmailField)
        listOf("SYM", ",", "SPACE", "@", ".com", "ENTER")
    else
        listOf("SYM", ",", "SPACE", ".", "ENTER")

    val rows = when {
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
        isTranslit -> if (!isShifted) listOf(
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
        mode == KeyboardMode.ENGLISH -> if (!isShifted) listOf(
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
        else -> if (isShifted) listOf(
            listOf("अ", "आ", "इ", "ई", "उ", "ऊ", "ऋ", "ए", "ऐ", "ओ", "औ"),
            listOf("ा", "ि", "ी", "ु", "ू", "ृ", "े", "ै", "ो", "ौ"),
            listOf("ं", "ः", "ँ", "्", "।", "॥", "०", "१", "२", "३"),
            listOf("SHIFT", "४", "५", "६", "७", "८", "९", "BACKSPACE"),
            bodoBottomRow
        ) else listOf(
            listOf("क", "ख", "ग", "घ", "ङ", "च", "छ", "ज", "झ", "ञ"),
            listOf("ट", "ठ", "ड", "ढ", "ण", "त", "थ", "द", "ध", "न"),
            listOf("प", "फ", "ब", "भ", "म", "य", "र", "ल", "व"),
            listOf("SHIFT", "श", "ष", "स", "ह", "ड़", "ढ़", "क्ष", "BACKSPACE"),
            bodoBottomRow
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(KbBg)
    ) {
        when (mode) {
            KeyboardMode.EMOJI  -> EmojiKeyboard(onKeyClick)
            KeyboardMode.GIF    -> GifKeyboard(onKeyClick)
            else -> {
                // ── Suggestion / Alternates bar ──────────────────────────────────
                SuggestionBar(
                    suggestions     = suggestions,
                    longPressKey    = longPressKey,
                    onSuggestion    = onSuggestionClick,
                    onAlternate     = { onKeyClick(it); longPressKey = null },
                    onDismissAlts   = { longPressKey = null }
                )

                // ── Toolbar row ───────────────────────────────────────────────────
                ToolbarRow(mode = mode, onKeyClick = onKeyClick)

                // ── Key rows ──────────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val isQwerty = mode == KeyboardMode.ENGLISH || isTranslit
                    rows.forEachIndexed { rowIdx, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Gboard-like Row 2 padding for QWERTY (9 keys)
                            if (isQwerty && rowIdx == 1 && row.size == 9 && !isSymbols) {
                                Spacer(Modifier.weight(0.5f))
                            }

                            row.forEach { key ->
                                val hint = if (isQwerty && rowIdx == 0 && !isSymbols) qRowHints[key] else null
                                KeyButton(
                                    key         = key,
                                    hint        = hint,
                                    modifier    = Modifier.weight(
                                        when (key) {
                                            "SPACE"                          -> 5.5f
                                            "BACKSPACE", "ENTER", "SHIFT",
                                            "SYM", "ABC", "MODE_SWITCH"      -> 1.55f
                                            "SYM_PAGE"                       -> 1.2f
                                            ".com"                           -> 1.2f
                                            else                             -> 1f
                                        }
                                    ),
                                    mode        = mode,
                                    isCapsLock  = isCapsLock,
                                    viewModel   = viewModel,
                                    onSpaceDrag = onSpaceDrag,
                                    onLongPress = handleLongPress,
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
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

// ── Toolbar row ───────────────────────────────────────────────────────────────
private val ToolIconSize = 22.dp

@Composable
private fun ToolbarRow(mode: KeyboardMode, onKeyClick: (String) -> Unit) {
    val modeLabel = if (mode == KeyboardMode.BODO) "EN" else "बर'"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(KbBg)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Collapse / back chevron, circled
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(ChipShape)
                .background(EnterBg.copy(alpha = 0.25f))
                .clickable { onKeyClick("COLLAPSE") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Collapse",
                tint = EnterBg,
                modifier = Modifier.size(ToolIconSize)
            )
        }
        // EN / बर' toggle — swaps keyboard between English and Bodo
        ToolBtn(label = modeLabel) { onKeyClick("MODE_SWITCH") }
        ToolBtn(icon = Icons.Default.EmojiEmotions)  { onKeyClick("EMOJI_SWITCH") }
        ToolBtn(label = "GIF")                       { onKeyClick("GIF_SWITCH") }
        ToolBtn(icon = Icons.Default.ContentPaste)   { onKeyClick("PASTE") }

        Box(
            Modifier
                .width(1.dp)
                .height(24.dp)
                .background(DividerC)
        )

        ToolBtn(icon = Icons.Default.MoreHoriz)      { onKeyClick("SETTINGS_OPEN") }
        ToolBtn(icon = Icons.Default.Mic)            { /* voice input not yet implemented */ }
    }
}

@Composable
private fun ToolBtn(
    icon: ImageVector? = null,
    label: String? = null,
    isHighlight: Boolean = false,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isHighlight) Modifier.background(EnterBg.copy(alpha = 0.25f)) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlight) EnterBg else Color.White,
                modifier = Modifier.size(ToolIconSize)
            )
        } else if (label != null) {
            Text(
                text       = label,
                color      = if (isHighlight) EnterBg else Color.White,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ── Suggestion / Alternates bar ───────────────────────────────────────────────
@Composable
private fun SuggestionBar(
    suggestions: List<String>,
    longPressKey: String?,
    onSuggestion: (String) -> Unit,
    onAlternate: (String) -> Unit,
    onDismissAlts: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(SuggBg)
    ) {
        if (longPressKey != null) {
            val alts = keyAlternates[longPressKey] ?: emptyList()
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AltChip(text = longPressKey, isMain = true,  onClick = { onAlternate(longPressKey) })
                alts.forEach { AltChip(text = it, isMain = false, onClick = { onAlternate(it) }) }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(ChipShape)
                        .background(DividerC)
                        .clickable(onClick = onDismissAlts),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = SuggTxt, fontSize = 12.sp)
                }
            }
        } else {
            val padded = (suggestions + List(3) { "" }).take(3)
            Row(modifier = Modifier.fillMaxSize()) {
                padded.forEachIndexed { i, word ->
                    if (i > 0) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .padding(vertical = 10.dp)
                                .background(DividerC)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(if (word.isNotEmpty()) Modifier.clickable { onSuggestion(word) } else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        if (word.isNotEmpty()) {
                            Text(text = word, color = SuggTxt, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
                .background(DividerC)
        )
    }
}

@Composable
private fun AltChip(text: String, isMain: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .height(28.dp)
            .clip(ChipShape)
            .background(if (isMain) ChipHighBg else ChipBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Emoji keyboard ────────────────────────────────────────────────────────────
private val EMOJI_CATEGORIES = listOf(
    "😀" to listOf(
        "😀","😃","😄","😁","😆","😅","🤣","😂","🙂","🙃","😉","😊","😇","🥰","😍","🤩",
        "😘","😗","😚","😙","🥲","😋","😛","😜","🤪","😝","🤑","🤗","🤭","🤫","🤔","🤐",
        "🤨","😐","😑","😶","😏","😒","🙄","😬","🤥","😌","😔","😪","🤤","😴","😷","🤒",
        "🤕","🤢","🤮","🤧","🥵","🥶","🥴","😵","💫","🤯","🤠","🥳","🥸","😎","🤓","🧐",
        "😕","🫤","😟","🙁","☹️","😮","😯","😲","😳","🥺","🫣","😦","😧","😨","😰","😥",
        "😢","😭","😱","😖","😣","😞","😓","😩","😫","🥱","😤","😡","😠","🤬","😈","👿",
        "💀","☠️","💩","🤡","👹","👺","👻","👽","👾","🤖","😺","😸","😹","😻","😼","😽",
        "🙀","😿","😾","🙈","🙉","🙊","💋","💌","💘","💝","💖","💗","💓","💞","💕","💟",
        "❣️","💔","❤️","🧡","💛","💚","💙","💜","🤎","🖤","🤍","💯","💢","💥","💫","💦",
        "💨","🕳️","💬","💭","💤"
    ),
    "👋" to listOf(
        "👋","🤚","🖐️","✋","🖖","👌","🤌","🤏","✌️","🤞","🤟","🤘","🤙","👈","👉","👆",
        "🖕","👇","☝️","🫵","👍","👎","✊","👊","🤛","🤜","👏","🙌","👐","🤲","🤝","🙏",
        "✍️","💅","🤳","💪","🦾","🦿","🦵","🦶","👂","🦻","👃","🫀","🫁","🧠","🦷","🦴",
        "👀","👁️","👅","👄","🫦","👶","🧒","👦","👧","🧑","👱","👨","🧔","🧔‍♂️","🧔‍♀️",
        "👨‍🦰","👨‍🦱","👨‍🦳","👨‍🦲","👩","👩‍🦰","🧑‍🦰","👩‍🦱","🧑‍🦱","👩‍🦳","🧑‍🦳",
        "👩‍🦲","🧑‍🦲","👱‍♀️","👱‍♂️","🧓","👴","👵","🙍","🙍‍♂️","🙍‍♀️","🙎","🙎‍♂️",
        "🙎‍♀️","🙅","🙅‍♂️","🙅‍♀️","🙆","🙆‍♂️","🙆‍♀️","💁","💁‍♂️","💁‍♀️","🙋","🙋‍♂️",
        "🙋‍♀️","🧏","🧏‍♂️","🧏‍♀️","🙇","🙇‍♂️","🙇‍♀️","🤦","🤦‍♂️","🤦‍♀️","🤷","🤷‍♂️",
        "🤷‍♀️","👮","👮‍♂️","👮‍♀️","🕵️","💂","👷","🤴","👸","👳","👲","🧕","🤵","👰",
        "🤰","🫃","🫄","🤱","👼","🎅","🤶","🧑‍🎄","🦸","🦹","🧙","🧝","🧛","🧟","🧞",
        "🧜","🧚","👫","👬","👭","💏","💑","👨‍👩‍👦","👨‍👩‍👧","👨‍👩‍👧‍👦","👨‍👦","👩‍👦",
        "🗣️","👤","👥","🫂"
    ),
    "🐶" to listOf(
        "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐻‍❄️","🐨","🐯","🦁","🐮","🐷","🐸","🐵",
        "🙈","🙉","🙊","🐔","🐧","🐦","🐤","🦆","🦅","🦉","🦇","🐺","🐗","🐴","🦄","🐝",
        "🪱","🐛","🦋","🐌","🐞","🐜","🪲","🦟","🦗","🪳","🕷️","🦂","🐢","🐍","🦎","🐊",
        "🦕","🦖","🐙","🦑","🦐","🦞","🦀","🐡","🐠","🐟","🐬","🐳","🐋","🦈","🐊","🦭",
        "🐅","🐆","🦓","🦍","🦧","🦣","🐘","🦛","🦏","🐪","🐫","🦒","🦘","🦬","🐃","🐂",
        "🐄","🐎","🐖","🐏","🐑","🦙","🐐","🦌","🐕","🐩","🦮","🐕‍🦺","🐈","🐈‍⬛","🪶",
        "🐓","🦃","🦤","🦚","🦜","🦢","🦩","🕊️","🐇","🦝","🦨","🦡","🦫","🦦","🦥","🐁",
        "🐀","🐿️","🦔","🐾","🌵","🎄","🌲","🌳","🌴","🪵","🌱","🌿","☘️","🍀","🎍","🎋",
        "🍃","🍂","🍁","🍄","🐚","🪸","🌾","💐","🌷","🌹","🥀","🌺","🌸","🌼","🌻","🌞",
        "🌝","🌛","🌜","🌚","🌕","🌖","🌗","🌘","🌑","🌒","🌓","🌔","🌙","🌟","⭐","🌠"
    ),
    "🍎" to listOf(
        "🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝",
        "🍅","🫒","🥑","🍆","🥔","🥕","🌽","🌶️","🫑","🥒","🥬","🥦","🧄","🧅","🍄","🥜",
        "🫘","🌰","🍞","🥐","🥖","🫓","🥨","🥯","🥞","🧇","🧀","🍖","🍗","🥩","🥓","🌭",
        "🍔","🍟","🍕","🫔","🌮","🌯","🥙","🧆","🥚","🍳","🥘","🍲","🫕","🥣","🥗","🍿",
        "🧈","🥫","🍱","🍘","🍙","🍚","🍛","🍜","🍝","🍠","🍢","🍣","🍤","🍥","🥮","🍡",
        "🥟","🥠","🥡","🦀","🦞","🦐","🦑","🦪","🍦","🍧","🍨","🍩","🍪","🎂","🍰","🧁",
        "🥧","🍫","🍬","🍭","🍮","🍯","🍼","🥛","☕","🫖","🍵","🧃","🥤","🧋","🍶","🍺",
        "🍻","🥂","🍷","🫗","🥃","🍸","🍹","🧉","🍾","🧊","🥄","🍴","🍽️","🥢","🧂"
    ),
    "⚽" to listOf(
        "⚽","🏀","🏈","⚾","🥎","🎾","🏐","🏉","🥏","🎱","🏓","🏸","🏒","🏑","🥍","🏏",
        "🪃","🥅","⛳","🪁","🏹","🎣","🤿","🥊","🥋","🎽","🛹","🛼","🛷","⛸️","🥌","🎿",
        "⛷️","🏂","🪂","🏋️","🤼","🤸","🤺","🤾","🏌️","🏇","🧘","🏄","🏊","🤽","🚣","🧗",
        "🚵","🚴","🏆","🥇","🥈","🥉","🏅","🎖️","🏵️","🎗️","🎫","🎟️","🎪","🤹","🎭","🩰",
        "🎨","🎬","🎤","🎧","🎼","🎹","🥁","🪘","🎷","🎺","🎸","🪕","🎻","🎲","♟️","🎯",
        "🎳","🎮","🎰","🧩","🪅","🪆","🪄","🎭","🃏","🀄","🎴","🎠","🎡","🎢","🎪"
    ),
    "🚗" to listOf(
        "🚗","🚕","🚙","🚌","🚎","🏎️","🚓","🚑","🚒","🚐","🛻","🚚","🚛","🚜","🦯","🦽",
        "🦼","🛴","🚲","🛵","🏍️","🛺","🚨","🚥","🚦","🛑","🚧","⚓","🛟","⛵","🛶","🚤",
        "🛳️","⛴️","🛥️","🚢","✈️","🛩️","🛫","🛬","🪂","💺","🚁","🚟","🚠","🚡","🛰️","🚀",
        "🛸","🌍","🌎","🌏","🌐","🗺️","🧭","🏔️","⛰️","🌋","🗻","🏕️","🏖️","🏜️","🏝️","🏞️",
        "🏟️","🏛️","🏗️","🏘️","🏚️","🏠","🏡","🏢","🏣","🏤","🏥","🏦","🏨","🏩","🏪","🏫",
        "🏬","🏭","🏯","🏰","💒","🗼","🗽","⛪","🕌","🛕","🕍","⛩️","🕋","⛲","⛺","🌁",
        "🌃","🏙️","🌄","🌅","🌆","🌇","🌉","♨️","🎠","🎡","🎢","🎪","🚃","🚞","🚝","🚄"
    ),
    "📱" to listOf(
        "📱","📲","💻","⌨️","🖥️","🖨️","🖱️","🖲️","🕹️","💾","💿","📀","🧮","📷","📸","📹",
        "🎥","📽️","🎞️","📞","☎️","📟","📠","📺","📻","🧭","⏱️","⏲️","⏰","🕰️","⌛","⏳",
        "📡","🔋","🪫","🔌","💡","🔦","🕯️","🪔","🧱","🪞","🪟","🛋️","🪑","🚽","🪠","🚿",
        "🛁","🪤","🪒","🧴","🧷","🧹","🧺","🧻","🪣","🧼","🫧","🪥","🧽","🪣","🧯","🛒",
        "🚪","🪜","🧲","🪜","🧰","🔧","🔨","⚒️","🛠️","⛏️","🔩","🪛","🔫","🪃","🏹","🛡️",
        "🪚","🔪","🗡️","⚔️","🛡️","🚬","⚰️","🪦","⚱️","🏺","🔮","📿","🧿","💈","⚗️","🔭",
        "🔬","🩺","🩻","🩹","💊","💉","🩸","🧬","🦠","🧫","🧪","🌡️","🧲","⚙️","🔑","🗝️",
        "🔐","🔒","🔓","🔏","📝","📋","📁","📂","🗂️","📊","📈","📉","🗒️","🗓️","📆","📅",
        "📇","📌","📍","🖇️","📎","✂️","🖊️","🖋️","✒️","🖌️","📏","📐","🗃️","🗄️","🗑️","📦",
        "📫","📪","📬","📭","📮","📯","📜","📃","📄","📑","🧾","📊","📋","📌","🗺️","🏷️"
    ),
    "❤️" to listOf(
        "❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔","❤️‍🔥","❤️‍🩹","❣️","💕","💞","💓",
        "💗","💖","💘","💝","💟","☮️","✝️","☪️","🕉️","☸️","🪯","✡️","🔯","🕎","☯️","☦️",
        "🛐","⛎","♈","♉","♊","♋","♌","♍","♎","♏","♐","♑","♒","♓","🆔","⚛️",
        "🉑","☢️","☣️","📴","📳","🈶","🈚","🈸","🈺","🈷️","✴️","🆚","💮","🉐","㊙️","㊗️",
        "🈴","🈵","🈹","🈲","🅰️","🅱️","🆎","🆑","🅾️","🆘","❌","⭕","🛑","⛔","📛","🚫",
        "💯","💢","♨️","🚷","🚯","🚳","🚱","🔞","📵","🚭","❗","❕","❓","❔","‼️","⁉️",
        "🔅","🔆","〽️","⚠️","🚸","🔱","⚜️","🔰","♻️","✅","🈯","💹","❎","🌐","💠","Ⓜ️",
        "🌀","💤","🏧","🚾","♿","🅿️","🛗","🈳","🈂️","🛂","🛃","🛄","🛅","🚹","🚺","🚼",
        "⚧️","🚻","🚮","🎦","📶","🈁","🔣","ℹ️","🔤","🔡","🔠","🆙","🆒","🆕","🆓","0️⃣",
        "1️⃣","2️⃣","3️⃣","4️⃣","5️⃣","6️⃣","7️⃣","8️⃣","9️⃣","🔟","🔢","▶️","⏸️","⏹️","⏺️",
        "⏭️","⏮️","⏩","⏪","⏫","⏬","◀️","🔼","🔽","➡️","⬅️","⬆️","⬇️","↗️","↘️","↙️",
        "↖️","↕️","↔️","↩️","↪️","⤴️","⤵️","🔀","🔁","🔂","🔃","🎵","🎶","➕","➖","➗",
        "✖️","♾️","💲","💱","™️","©️","®️","〰️","➰","➿","🔚","🔙","🔛","🔝","🔜","✔️",
        "☑️","🔘","🔴","🟠","🟡","🟢","🔵","🟣","⚫","⚪","🟤","🔺","🔻","🔷","🔶","🔹",
        "🔸","🔲","🔳","▪️","▫️","◾","◽","◼️","◻️","🟥","🟧","🟨","🟩","🟦","🟪","⬛","⬜"
    ),
)

@Composable
fun EmojiKeyboard(onKeyClick: (String) -> Unit) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    val emojis = EMOJI_CATEGORIES[selectedCategory].second

    Column(
        modifier = Modifier
            .background(KbBg)
            .height(280.dp)
    ) {
        // Category tab row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(KeySpec)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(EMOJI_CATEGORIES) { idx, (icon, _) ->
                val isSelected = idx == selectedCategory
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) EnterBg.copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { selectedCategory = idx },
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 18.sp)
                }
            }
        }

        // Emoji grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onKeyClick(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 28.sp)
                }
            }
        }

        // Bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            KeyButton(
                key = "ABC", modifier = Modifier.weight(1f),
                mode = KeyboardMode.EMOJI, isCapsLock = false, viewModel = null,
                onClick = { onKeyClick("ABC") }
            )
            KeyButton(
                key = "BACKSPACE", modifier = Modifier.weight(1f),
                mode = KeyboardMode.EMOJI, isCapsLock = false, viewModel = null,
                onClick = { onKeyClick("BACKSPACE") }
            )
        }
    }
}

@Composable
fun GifKeyboard(onKeyClick: (String) -> Unit) {
    val sampleGifs = listOf("🔥", "🎉", "❤️", "😂", "👍", "🙏", "✨", "🙌")

    Column(
        modifier = Modifier
            .background(KbBg)
            .height(240.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("GIF Search (Placeholder)", color = ToolTxt, fontSize = 16.sp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            sampleGifs.forEach { gif ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(KeyNorm, RoundedCornerShape(8.dp))
                        .clickable { onKeyClick(gif) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(gif, fontSize = 20.sp)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        KeyButton(
            key = "ABC", modifier = Modifier.fillMaxWidth(0.3f),
            mode = KeyboardMode.GIF, isCapsLock = false, viewModel = null,
            onClick = { onKeyClick("ABC") }
        )
    }
}

// ── KeyButton ─────────────────────────────────────────────────────────────────
@Composable
fun KeyButton(
    key: String,
    hint: String? = null,
    modifier: Modifier = Modifier,
    mode: KeyboardMode,
    isCapsLock: Boolean,
    viewModel: KeyboardViewModel?,
    onSpaceDrag: (Int) -> Unit = {},
    onLongPress: ((String) -> Unit)? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    if (isPressed && key == "BACKSPACE") {
        LaunchedEffect(Unit) {
            delay(400) // Initial delay before starting repeat
            while (true) {
                onClick()
                delay(60) // Repeat interval
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "keyScale"
    )

    val isSpecial    = key in setOf("SHIFT", "BACKSPACE", "SYM", "ABC", "MODE_SWITCH", "SYM_PAGE")
    val isEnter      = key == "ENTER"
    val isShiftCaps  = key == "SHIFT" && isCapsLock

    val bgColor: Color = when {
        isShiftCaps -> CapsActive
        isSpecial || isEnter -> if (isPressed) KeySpecP else KeySpec
        else        -> if (isPressed) KeyPressed else KeyNorm
    }

    val height = (if (key in setOf("SPACE", "SYM", "ABC", "MODE_SWITCH", "ENTER", ",", ".", "@", ".com", "।", "EMOJI_SWITCH")) 45.dp else 45.dp) *
            (viewModel?.keyboardHeightMultiplier?.value ?: 1f)

    val label = when (key) {
        "BACKSPACE"    -> "⌫"
        "ENTER"        -> {
            val inputType = viewModel?.editorInfo?.inputType ?: 0
            val isMultiLine = (inputType and EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) != 0
            if (isMultiLine) {
                "↵"
            } else {
                when (viewModel?.imeAction?.value) {
                    EditorInfo.IME_ACTION_SEARCH -> "🔍"
                    EditorInfo.IME_ACTION_SEND   -> "➤"
                    EditorInfo.IME_ACTION_GO     -> "➡"
                    EditorInfo.IME_ACTION_DONE   -> "✓"
                    else                         -> "↵"
                }
            }
        }
        "SHIFT"        -> when {
            isCapsLock                   -> "⇪"
            mode == KeyboardMode.BODO    -> "आ/क"
            else                         -> "⇧"
        }
        "SPACE"        -> when (mode) {
            KeyboardMode.BODO     -> "बर'"
            KeyboardMode.TRANSLIT -> "बर' (Translit)"
            KeyboardMode.ENGLISH  -> "English"
            else                  -> ""
        }
        "SYM"          -> "?123"
        "SYM_PAGE"     -> if (viewModel?.isSymbols2?.value == true) "?123" else "=\\<"
        "ABC"          -> "ABC"
        "MODE_SWITCH"  -> if (mode == KeyboardMode.BODO) "EN" else "बर'"
        "EMOJI_SWITCH" -> "😊"
        else           -> key
    }

    val fontSize = when {
        key == "SHIFT" && mode == KeyboardMode.BODO -> 11.sp
        key == "MODE_SWITCH" || key == "SYM" || key == "ABC" -> 14.sp
        key == "SPACE"         -> 13.sp
        key.length > 2         -> 12.sp
        else                   -> 18.sp
    }

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .clip(KeyShape)
            .background(bgColor)
            .then(
                if (key == "SPACE") Modifier.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd    = { dragAccumulator = 0f },
                        onDragCancel = { dragAccumulator = 0f },
                        onHorizontalDrag = { _, amount ->
                            dragAccumulator += amount
                            if (abs(dragAccumulator) > 40f) {
                                onSpaceDrag(if (dragAccumulator > 0) 1 else -1)
                                dragAccumulator = 0f
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        }
                    )
                } else Modifier
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick = {
                    if (viewModel?.hapticEnabled?.value == true)
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                },
                onLongClick = onLongPress?.let { lp ->
                    {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        lp(key)
                    }
                }
            )
    ) {
        // Key label — centred
        Text(
            text       = label,
            modifier   = Modifier.align(Alignment.Center),
            fontSize   = fontSize,
            fontWeight = when (key) {
                "MODE_SWITCH", "SYM", "ABC" -> FontWeight.SemiBold
                else                         -> FontWeight.Normal
            },
            color = KeyTxt
        )

        // Number hint — top-right corner
        if (hint != null) {
            Text(
                text     = hint,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 3.dp, end = 4.dp),
                fontSize = 9.sp,
                color    = HintTxt,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}
