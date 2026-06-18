@file:OptIn(ExperimentalFoundationApi::class)

package com.loony.bodokeyboard.ui.keyboard

import android.view.HapticFeedbackConstants
import android.view.inputmethod.EditorInfo
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loony.bodokeyboard.data.KeyboardMode
import com.loony.bodokeyboard.ui.theme.CapsActive
import com.loony.bodokeyboard.ui.theme.EnterBg
import com.loony.bodokeyboard.ui.theme.HintTxt
import com.loony.bodokeyboard.ui.theme.KeyNorm
import com.loony.bodokeyboard.ui.theme.KeyPressed
import com.loony.bodokeyboard.ui.theme.KeyShape
import com.loony.bodokeyboard.ui.theme.KeySpec
import com.loony.bodokeyboard.ui.theme.KeySpecP
import com.loony.bodokeyboard.ui.theme.KeyTxt
import com.loony.bodokeyboard.viewmodel.KeyboardViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * A single keyboard key with press animation, haptic feedback, long-press support,
 * and continuous delete on BACKSPACE hold.
 *
 * @param key         The key identifier / character to emit.
 * @param hint        Optional number hint rendered in the top-right corner.
 * @param mode        The current keyboard mode (controls label and icon rendering).
 * @param isCapsLock  Whether CapsLock is active (used to tint the SHIFT key).
 * @param viewModel   Provides haptic preference and keyboard height; null is safe (GIF panel).
 * @param onSpaceDrag Called with +1 / -1 when the SPACE key is dragged horizontally.
 * @param onLongPress Called with the key string on long press; null = no long-press action.
 * @param onClick     Called on normal tap.
 */
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

    // Continuous delete while BACKSPACE is held
    if (isPressed && key == "BACKSPACE") {
        LaunchedEffect(Unit) {
            delay(400)   // initial delay before repeat
            while (true) {
                onClick()
                delay(60) // repeat interval
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue  = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "keyScale"
    )

    val isSpecial   = key in setOf("SHIFT", "BACKSPACE", "SYM", "ABC", "MODE_SWITCH", "SYM_PAGE")
    val isEnter     = key == "ENTER"
    val isShiftCaps = key == "SHIFT" && isCapsLock

    val bgColor = when {
        isShiftCaps            -> CapsActive
        isSpecial || isEnter   -> if (isPressed) KeySpecP else KeySpec
        else                   -> if (isPressed) KeyPressed else KeyNorm
    }

    val height = 45.dp * (viewModel?.keyboardHeightMultiplier?.value ?: 1f)

    val icon: ImageVector? = when (key) {
        "BACKSPACE" -> Icons.AutoMirrored.Filled.Backspace
        "ENTER"     -> resolveEnterIcon(viewModel)
        "SHIFT"     -> when {
            isCapsLock                -> Icons.Default.KeyboardDoubleArrowUp
            mode == KeyboardMode.BODO -> null  // Bodo shift uses text label
            else                      -> Icons.Default.ArrowUpward
        }
        else -> null
    }

    val label = when (key) {
        "SHIFT"        -> if (mode == KeyboardMode.BODO && !isCapsLock) "आ/क" else ""
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
        key == "SHIFT" && mode == KeyboardMode.BODO         -> 13.sp
        key in setOf("SHIFT", "BACKSPACE", "ENTER")         -> 22.sp
        key in setOf("MODE_SWITCH", "SYM", "ABC")           -> 14.sp
        key == "SPACE"                                       -> 13.sp
        key.length > 2                                       -> 12.sp
        else                                                 -> 18.sp
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
        if (icon != null) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = KeyTxt,
                modifier           = Modifier.align(Alignment.Center).size(22.dp)
            )
        } else {
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
        }

        // Number hint — top-right corner of Q-row keys
        if (hint != null) {
            Text(
                text       = hint,
                modifier   = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 3.dp, end = 4.dp),
                fontSize   = 9.sp,
                color      = HintTxt,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

/** Resolves the correct icon for the ENTER key based on the current IME action. */
private fun resolveEnterIcon(viewModel: KeyboardViewModel?): ImageVector {
    val inputType   = viewModel?.editorInfo?.inputType ?: 0
    val isMultiLine = (inputType and EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) != 0
    if (isMultiLine) return Icons.AutoMirrored.Filled.KeyboardReturn
    return when (viewModel?.imeAction?.value) {
        EditorInfo.IME_ACTION_SEARCH -> Icons.Default.Search
        EditorInfo.IME_ACTION_SEND   -> Icons.AutoMirrored.Filled.Send
        EditorInfo.IME_ACTION_GO     -> Icons.AutoMirrored.Filled.ArrowForward
        EditorInfo.IME_ACTION_DONE   -> Icons.Default.Done
        else                         -> Icons.AutoMirrored.Filled.KeyboardReturn
    }
}
