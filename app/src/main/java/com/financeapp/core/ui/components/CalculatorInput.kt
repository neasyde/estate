package com.financeapp.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.R
import com.financeapp.core.ui.anim.Motion
import com.financeapp.core.ui.anim.reducedMotion
import com.financeapp.core.utils.rememberHaptics
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Calculator sheet. Replaces the old hand-rolled one.
 *
 * State lives in a single [CalcState] (Compose state holder) so debouncing, history, and live
 * preview all see the same source of truth. The expression parser uses [BigDecimal] for lossless
 * decimal arithmetic.
 *
 * Interactions:
 *  - Single tap on a digit/operator appends to the expression.
 *  - Long-press the `AC` button to clear history (not just the entry).
 *  - Long-press the backspace button to wipe the current number (preserves operators).
 *  - The result is committed to [onValueChange] only on `=` / `Apply` / selecting a history entry.
 *  - On dismiss (scrim / back) the parent value is preserved — no leftover state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorSheet(
    value: String,
    onValueChange: (String) -> Unit,
    currencySymbol: String,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state = remember { CalcState(value) }
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()

    LaunchedEffect(Unit) { state.resetTo(value) }

    val localeContext = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        CompositionLocalProvider(LocalContext provides localeContext) {
        Column(Modifier.padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 16.dp)) {
            HistoryBar(
                history = state.history,
                onPick = { entry ->
                    haptics(true)
                    state.applyHistory(entry)
                },
            )
            Spacer(Modifier.height(4.dp))
            Display(state = state, currencySymbol = currencySymbol)
            Spacer(Modifier.height(12.dp))

            Spacer(Modifier.height(12.dp))

            val rows = listOf(
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "−"),
                listOf("1", "2", "3", "+"),
                listOf("0", "00", ".", "="),
            )
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { key ->
                        when (key) {
                            "=" -> CalcButton(
                                modifier = Modifier.weight(1f),
                                background = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                onTap = {
                                    haptics(true)
                                    scope.launch {
                                        state.commitResult { committed -> onValueChange(committed) }
                                        onDismiss()
                                    }
                                },
                            ) { Text("=", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold) }
                            "÷", "×", "−", "+" -> CalcButton(
                                modifier = Modifier.weight(1f),
                                background = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary,
                                onTap = { state.appendOperator(key) },
                            ) { Text(key, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) }
                            "." -> CalcButton(
                                modifier = Modifier.weight(1f),
                                background = MaterialTheme.colorScheme.surfaceVariant,
                                onTap = { state.appendDecimal() },
                            ) { Text(".", style = MaterialTheme.typography.headlineSmall) }
                            else -> CalcButton(
                                modifier = Modifier.weight(1f),
                                background = MaterialTheme.colorScheme.surfaceVariant,
                                onTap = { state.appendDigit(key) },
                            ) { Text(key, style = MaterialTheme.typography.headlineSmall) }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcButton(
                    modifier = Modifier.weight(1f),
                    background = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    onTap = { state.clear() },
                    onLongPress = {
                        haptics(true)
                        state.clearHistory()
                    },
                ) { Text("AC", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                CalcButton(
                    modifier = Modifier.weight(1f),
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    onTap = { state.percent() },
                ) { Text("%", style = MaterialTheme.typography.titleMedium) }
                CalcButton(
                    modifier = Modifier.weight(1f),
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    onTap = { state.appendOperator("÷") },
                ) { Text("÷", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) }
                CalcButton(
                    modifier = Modifier.weight(1f),
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    onTap = { state.backspace() },
                    onLongPress = {
                        haptics(true)
                        state.clearEntry()
                    },
                ) { Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = stringResource(R.string.action_backspace), modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(6.dp))
        }
        }
    }
}

/** Single source of truth for the calculator. Pure state — no business logic leaks into composables. */
@Stable
internal class CalcState(initial: String) {
    private val _expression = mutableStateOf("")
    val expression: String get() = _expression.value
    private val _result = mutableStateOf<String?>(null)
    val result: String? get() = _result.value
    private val _history = mutableStateListOf<HistoryEntry>()
    val history: List<HistoryEntry> get() = _history

    val livePreview: String? by derivedStateOf {
        if (_result.value != null) null else evaluateForPreview(_expression.value)
    }

    val canApply: Boolean by derivedStateOf { _expression.value.isNotEmpty() }

    fun resetTo(@Suppress("UNUSED_PARAMETER") v: String) {
        _expression.value = ""
        _result.value = null
    }

    fun appendDigit(d: String) {
        if (_result.value != null) {
            _expression.value = d
            _result.value = null
            return
        }
        val cur = _expression.value
        val lastNumber = cur.takeLastWhile { it.isDigit() || it == '.' }
        if (lastNumber.length >= 12) return
        _expression.value = cur + d
    }

    fun appendDecimal() {
        if (_result.value != null) {
            _expression.value = "0."
            _result.value = null
            return
        }
        val cur = _expression.value
        val lastNumberStart = cur.indexOfLast { !it.isDigit() && it != '.' }.let { if (it == -1) 0 else it + 1 }
        val lastNumber = cur.substring(lastNumberStart)
        if (lastNumber.contains('.')) return
        _expression.value = if (cur.isEmpty() || cur.last().let { !it.isDigit() && it != '.' }) "$cur." else "$cur."
    }

    fun appendOperator(op: String) {
        val cur = _expression.value
        if (cur.isEmpty()) {
            if (op == "−") _expression.value = "0−"
            return
        }
        if (_result.value != null) {
            _expression.value = _result.value.orEmpty() + op
            _result.value = null
            return
        }
        val last = cur.last()
        if (last in listOf('+', '−', '×', '÷')) {
            _expression.value = cur.dropLast(1) + op
        } else {
            _expression.value = cur + op
        }
    }

    fun backspace() {
        val cur = _expression.value
        if (cur.isNotEmpty()) {
            _expression.value = cur.dropLast(1)
        }
        if (_result.value != null) _result.value = null
    }

    fun clearEntry() {
        val cur = _expression.value
        val lastOp = cur.indexOfLast { it in listOf('+', '−', '×', '÷') }
        _expression.value = if (lastOp == -1) "" else cur.substring(0, lastOp + 1)
    }

    fun clear() {
        _expression.value = ""
        _result.value = null
    }

    fun clearHistory() {
        _history.clear()
    }

    fun toggleSign() {
        val cur = _expression.value
        if (cur.isEmpty()) {
            _expression.value = "−"
            return
        }
        val lastOp = cur.indexOfLast { it in listOf('+', '−', '×', '÷') }
        val numStart = if (lastOp == -1) 0 else lastOp + 1
        val prefix = cur.substring(0, numStart)
        val num = cur.substring(numStart)
        val newNum = if (num.startsWith('−')) num.drop(1) else "−$num"
        _expression.value = prefix + newNum
    }

    fun percent() {
        val cur = _expression.value
        if (cur.isEmpty()) {
            _expression.value = "0"
            return
        }
        val lastOp = cur.indexOfLast { it in listOf('+', '−', '×', '÷') }
        if (lastOp == -1) {
            val num = parseNumOrNull(cur) ?: return
            _expression.value = formatBigDecimal(num.divide(BigDecimal(100), MathContext.DECIMAL64))
        } else {
            val left = cur.substring(0, lastOp)
            val op = cur[lastOp]
            val right = cur.substring(lastOp + 1)
            if (right.isEmpty()) return
            val leftNum = parseNumOrNull(left) ?: return
            val rightNum = parseNumOrNull(right) ?: return
            val newRight = when (op) {
                '+', '−' -> leftNum.multiply(rightNum, MathContext.DECIMAL64)
                    .divide(BigDecimal(100), MathContext.DECIMAL64)
                else -> rightNum.divide(BigDecimal(100), MathContext.DECIMAL64)
            }
            _expression.value = "$left$op${formatBigDecimal(newRight)}"
        }
    }

    private fun parseNumOrNull(s: String): BigDecimal? =
        runCatching { BigDecimal(s.replace(",", ".")) }.getOrNull()

    fun applyHistory(entry: HistoryEntry) {
        _expression.value = entry.expression
        _result.value = entry.result
    }

    fun commitResult(block: (String) -> Unit) {
        val v = evaluate(_expression.value)
        if (v == null) {
            _result.value = "Error"
            return
        }
        _result.value = v
        if (_expression.value.isNotEmpty() && v != "Error") {
            _history.add(0, HistoryEntry(_expression.value, v))
            while (_history.size > 8) _history.removeAt(_history.lastIndex)
        }
        block(v)
    }

    private fun evaluateForPreview(expr: String): String? {
        if (expr.isEmpty()) return null
        return evaluate(expr, lenient = true)
    }

    private fun evaluate(expr: String, lenient: Boolean = false): String? {
        if (expr.isBlank()) return null
        val cleaned = expr.replace(" ", "").replace("×", "*").replace("÷", "/").replace("−", "-").replace(",", ".")
        if (cleaned.firstOrNull() in listOf('*', '/')) return null
        if (cleaned.lastOrNull() in listOf('+', '-', '*', '/')) return null
        return runCatching {
            val parser = BigDecimalParser(cleaned)
            val result = parser.parse()
            formatBigDecimal(result)
        }.getOrNull()
    }
}

/** Recursive-descent parser over [BigDecimal]. */
private class BigDecimalParser(private val src: String) {
    private var pos = 0

    fun parse(): BigDecimal = parseAddSub()

    private fun parseAddSub(): BigDecimal {
        var left = parseMulDiv()
        while (pos < src.length && (src[pos] == '+' || src[pos] == '-')) {
            val op = src[pos++]
            val right = parseMulDiv()
            left = if (op == '+') left.add(right) else left.subtract(right)
        }
        return left
    }

    private fun parseMulDiv(): BigDecimal {
        var left = parsePrimary()
        while (pos < src.length && (src[pos] == '*' || src[pos] == '/')) {
            val op = src[pos++]
            val right = parsePrimary()
            left = if (op == '*') left.multiply(right, MathContext.DECIMAL64)
            else if (right.signum() == 0) throw ArithmeticException("div by zero")
            else left.divide(right, MathContext.DECIMAL64)
        }
        return left
    }

    private fun parsePrimary(): BigDecimal {
        if (pos >= src.length) throw IllegalArgumentException("Unexpected end of expression")
        if (src[pos] == '(') {
            pos++
            val r = parseAddSub()
            if (pos < src.length && src[pos] == ')') pos++
            return r
        }
        val start = pos
        if (src[pos] == '-') pos++
        while (pos < src.length && (src[pos].isDigit() || src[pos] == '.')) pos++
        return BigDecimal(src.substring(start, pos))
    }
}

private fun formatBigDecimal(value: BigDecimal): String {
    val stripped = value.stripTrailingZeros()
    return if (stripped.scale() <= 0) {
        stripped.toBigInteger().toString()
    } else {
        val rounded = stripped.setScale(minOf(stripped.scale(), 8), RoundingMode.HALF_UP)
        rounded.toPlainString()
    }
}

internal data class HistoryEntry(val expression: String, val result: String)

/* ------------------------------ Composables ------------------------------ */

@Composable
private fun HistoryBar(
    history: List<HistoryEntry>,
    onPick: (HistoryEntry) -> Unit,
) {
    val show = history.isNotEmpty()
    if (reducedMotion()) {
        if (show) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
                ) {
                    items(
                        count = history.size,
                        key = { idx -> "${history[idx].expression}->${history[idx].result}" },
                    ) { idx ->
                        val entry = history[idx]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPick(entry) },
                        ) {
                            Text(
                                text = entry.result,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    } else {
        AnimatedVisibility(visible = show, enter = fadeIn(), exit = fadeOut()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
                ) {
                    items(
                        count = history.size,
                        key = { idx -> "${history[idx].expression}->${history[idx].result}" },
                    ) { idx ->
                        val entry = history[idx]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPick(entry) },
                        ) {
                            Text(
                                text = entry.result,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Display(state: CalcState, currencySymbol: String) {
    val display = state.result ?: state.expression.ifEmpty { "0" }
    val preview = state.livePreview
    val text = display
        .replace("/", "÷")
        .replace("*", "×")
        .replace("-", "−")
    val reduced = reducedMotion()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.End,
    ) {
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                if (reduced) {
                    fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                } else {
                    fadeIn(tween(Motion.Short)) togetherWith fadeOut(tween(Motion.Short))
                }
            },
            label = "calcDisplay",
        ) { targetText ->
            Text(
                text = "$currencySymbol $targetText",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (preview != null && state.result == null) {
            Text(
                text = "= $preview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            )
        }
    }
}

/** Universal button surface — every key in the sheet is just this with different content. */
@Composable
private fun CalcButton(
    modifier: Modifier,
    background: Color,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onTap: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    tall: Boolean = false,
    compact: Boolean = false,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = if (reducedMotion()) tween(0) else tween(Motion.Short),
        label = "calcKeyScale",
    )
    val shape = if (tall) RoundedCornerShape(16.dp) else CircleShape
    val ratio = when {
        tall -> 2.4f
        compact -> 0.85f
        else -> 1f
    }
    Box(
        modifier
            .aspectRatio(ratio)
            .scale(scale)
            .clip(shape)
            .background(background)
            .pointerInput(onLongPress) {
                if (onLongPress != null) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onLongPress = { onLongPress() },
                    )
                } else {
                    detectTapGestures(onTap = { onTap() })
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides contentColor,
            content = content,
        )
    }
}
