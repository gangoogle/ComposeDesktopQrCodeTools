package com.qrcode.generator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch
import java.math.BigInteger

private val DarkColorPalette = darkColors(
    primary = Color(0xFF79B2FF),
    primaryVariant = Color(0xFF4F7DE8),
    secondary = Color(0xFF66E8C5),
    background = Color(0xFF080D18),
    surface = Color(0xFF121C30),
    onPrimary = Color.White,
    onSecondary = Color(0xFF06201A),
    onBackground = Color(0xFFF4F8FF),
    onSurface = Color(0xFFE7EEFA),
    error = Color(0xFFFF7E8D),
    onError = Color.White
)

private val LightColorPalette = lightColors(
    primary = Color(0xFF2F6FED),
    primaryVariant = Color(0xFF2255BF),
    secondary = Color(0xFF10B981),
    background = Color(0xFFF3F6FC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF122033),
    onSurface = Color(0xFF24344D),
    error = Color(0xFFD9485F),
    onError = Color.White
)

private val DarkPanelBorder = Color(0xFF283755)
private val LightPanelBorder = Color(0xFFDCE6F3)

private fun panelBrush(isDarkMode: Boolean): Brush = if (isDarkMode) {
    Brush.linearGradient(
        colors = listOf(Color(0xFF16233C), Color(0xFF0D1729))
    )
} else {
    Brush.linearGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF7FAFF))
    )
}

private val trailingNumberRegex = Regex("""^(.*?)(\d+)(\D*)$""")

/** Increments the last numeric run in a code, preserving leading zeros. */
internal fun incrementCodeContent(content: String): String {
    val match = trailingNumberRegex.find(content) ?: return content + "1"
    val (prefix, digits, suffix) = match.destructured
    val next = BigInteger(digits).add(BigInteger.ONE).toString()
    val resultDigits = if (next.length < digits.length) next.padStart(digits.length, '0') else next
    return prefix + resultDigits + suffix
}

@Composable
fun App(initialDarkMode: Boolean = true) {
    var inputText by remember { mutableStateOf("") }
    var qrCodeImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var history by remember { mutableStateOf<List<QRCodeHistoryItem>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }
    var selectedHistoryItem by remember { mutableStateOf<QRCodeHistoryItem?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDarkMode by remember { mutableStateOf(initialDarkMode) }

    val qrGenerator = remember { QRCodeGenerator() }
    val historyManager = remember { HistoryManager() }
    val coroutineScope = rememberCoroutineScope()
    val colors = if (isDarkMode) DarkColorPalette else LightColorPalette
    val historyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        try {
            history = historyManager.loadHistory()
        } catch (e: Exception) {
            errorMessage = "加载历史记录失败: ${e.message}"
        }
    }

    fun generateQRCode() {
        if (inputText.isBlank()) {
            return
        }

        coroutineScope.launch {
            isGenerating = true
            errorMessage = null
            try {
                val normalizedInput = inputText.trim()
                val image = qrGenerator.generateQRCode(normalizedInput)
                qrCodeImage = image

                if (image != null) {
                    val historyItem = QRCodeHistoryItem.create(normalizedInput)
                    history = historyManager.addHistoryItem(historyItem)
                    selectedHistoryItem = historyItem
                    inputText = normalizedInput
                    historyListState.animateScrollToItem(0)
                } else {
                    errorMessage = "QR码生成失败，请检查输入内容后重试"
                }
            } catch (e: Exception) {
                errorMessage = "生成 QR 码时发生错误: ${e.message}"
            } finally {
                isGenerating = false
            }
        }
    }

    fun duplicateAndIncrement(item: QRCodeHistoryItem) {
        if (isGenerating) return

        coroutineScope.launch {
            isGenerating = true
            errorMessage = null
            try {
                val nextContent = incrementCodeContent(item.content)
                val image = qrGenerator.generateQRCode(nextContent)
                if (image != null) {
                    val nextItem = QRCodeHistoryItem.create(nextContent)
                    history = historyManager.addHistoryItem(nextItem)
                    selectedHistoryItem = nextItem
                    qrCodeImage = image
                    inputText = nextContent
                    historyListState.animateScrollToItem(0)
                } else {
                    errorMessage = "生成下一个编号失败，请稍后重试"
                }
            } catch (e: Exception) {
                errorMessage = "复制编码时发生错误: ${e.message}"
            } finally {
                isGenerating = false
            }
        }
    }

    MaterialTheme(colors = colors) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDarkMode) {
                            listOf(
                                Color(0xFF070C16),
                                Color(0xFF0E1A31),
                                Color(0xFF080E1A)
                            )
                        } else {
                            listOf(
                                Color(0xFFF7FAFF),
                                Color(0xFFEDF3FF),
                                Color(0xFFF8FBFF)
                            )
                        }
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-180).dp, y = (-240).dp)
                    .size(560.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4C8DFF).copy(alpha = if (isDarkMode) 0.20f else 0.10f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 200.dp, y = 260.dp)
                    .size(640.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF27D3B1).copy(alpha = if (isDarkMode) 0.12f else 0.07f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.7f)
                            .fillMaxHeight()
                            .padding(end = 24.dp)
                    ) {
                        SectionCard(
                            isDarkMode = isDarkMode,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(17.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colors.primary,
                                                        MaterialTheme.colors.secondary
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        QrGlyph(
                                            color = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "QR码生成器",
                                            fontSize = 27.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colors.onBackground
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colors.secondary)
                                            )
                                            Spacer(modifier = Modifier.width(7.dp))
                                            Text(
                                                text = "本地生成 · 历史自动保存",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colors.onBackground.copy(alpha = 0.58f)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { isDarkMode = !isDarkMode },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colors.surface.copy(alpha = if (isDarkMode) 0.72f else 0.82f))
                                        .border(
                                            width = 1.dp,
                                            color = panelBorderColor(isDarkMode),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = if (isDarkMode) "切换到浅色模式" else "切换到深色模式",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colors.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        SectionCard(
                            isDarkMode = isDarkMode,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "输入内容",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = "粘贴或输入文本，生成后会自动保存",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.56f)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colors.primary.copy(alpha = 0.10f))
                                        .padding(horizontal = 9.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "Enter 生成",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colors.primary.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                label = {
                                    Text(
                                        text = "输入文本内容",
                                        fontSize = 14.sp
                                    )
                                },
                                placeholder = {
                                    Text(
                                        text = "在此粘贴或输入要生成 QR 码的文本...",
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(112.dp)
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp) {
                                            if (inputText.isNotBlank() && !isGenerating) {
                                                inputText = inputText.trimEnd('\n', '\r')
                                                generateQRCode()
                                            }
                                            true
                                        } else {
                                            false
                                        }
                                    },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    lineHeight = 26.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colors.onSurface
                                ),
                                maxLines = 6,
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = MaterialTheme.colors.surface.copy(
                                        alpha = if (isDarkMode) 0.34f else 0.72f
                                    ),
                                    focusedBorderColor = MaterialTheme.colors.primary.copy(alpha = 0.9f),
                                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.13f),
                                    focusedLabelColor = MaterialTheme.colors.primary,
                                    unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.52f),
                                    cursorColor = MaterialTheme.colors.primary,
                                    placeholderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.38f),
                                    textColor = MaterialTheme.colors.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            GradientActionButton(
                                enabled = inputText.isNotBlank(),
                                loading = isGenerating,
                                onClick = { generateQRCode() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = MaterialTheme.colors.error.copy(alpha = if (isDarkMode) 0.16f else 0.10f),
                                border = BorderStroke(1.dp, MaterialTheme.colors.error.copy(alpha = 0.32f)),
                                shape = RoundedCornerShape(16.dp),
                                elevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = errorMessage.orEmpty(),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colors.error,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { errorMessage = null },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "关闭",
                                            tint = MaterialTheme.colors.error.copy(alpha = 0.75f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        SectionCard(
                            isDarkMode = isDarkMode,
                            fillHeight = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "预览区域",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = if (selectedHistoryItem != null) "当前展示已选记录对应的二维码" else "生成后将在这里展示二维码结果",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.56f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                qrCodeImage?.let { image ->
                                    Column(
                                        modifier = Modifier.widthIn(max = 440.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .shadow(
                                                    elevation = 22.dp,
                                                    shape = RoundedCornerShape(28.dp),
                                                    ambientColor = Color.Black.copy(alpha = 0.28f),
                                                    spotColor = Color.Black.copy(alpha = 0.32f)
                                                )
                                                .clip(RoundedCornerShape(28.dp))
                                                .background(Color.White)
                                                .padding(16.dp)
                                        ) {
                                            Image(
                                                bitmap = image,
                                                contentDescription = "生成的 QR 码",
                                                modifier = Modifier.size(292.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colors.primary.copy(alpha = 0.09f))
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = selectedHistoryItem?.content ?: inputText,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = 14.sp,
                                                lineHeight = 20.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.82f)
                                            )
                                        }
                                    }
                                } ?: run {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(84.dp)
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colors.primary.copy(alpha = 0.20f),
                                                            MaterialTheme.colors.secondary.copy(alpha = 0.14f)
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            QrGlyph(
                                                color = MaterialTheme.colors.primary.copy(alpha = 0.9f),
                                                modifier = Modifier.size(42.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(18.dp))
                                        Text(
                                            text = "二维码将在此显示",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.84f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "输入内容并点击生成按钮，即可在这里查看结果",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.54f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        SectionCard(
                            isDarkMode = isDarkMode,
                            fillHeight = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colors.primary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.List,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colors.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "历史记录",
                                            fontSize = 21.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colors.onSurface
                                        )
                                        if (history.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colors.primary.copy(alpha = 0.14f))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "${history.size}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colors.primary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(7.dp))
                                    Text(
                                        text = "点击记录重新载入 · 点 + 生成下一个编号",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.55f)
                                    )
                                }

                                if (history.isNotEmpty()) {
                                    TextButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                historyManager.clearHistory()
                                                history = emptyList()
                                                selectedHistoryItem = null
                                                qrCodeImage = null
                                                inputText = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colors.error.copy(alpha = 0.08f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "清空历史",
                                            modifier = Modifier.size(17.dp),
                                            tint = MaterialTheme.colors.error.copy(alpha = 0.82f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "清空",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colors.error.copy(alpha = 0.82f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (history.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(78.dp)
                                                .clip(RoundedCornerShape(22.dp))
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colors.primary.copy(alpha = 0.16f),
                                                            MaterialTheme.colors.secondary.copy(alpha = 0.10f)
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            QrGlyph(
                                                color = MaterialTheme.colors.primary.copy(alpha = 0.82f),
                                                modifier = Modifier.size(38.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "暂无历史记录",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.82f)
                                        )
                                        Spacer(modifier = Modifier.height(7.dp))
                                        Text(
                                            text = "生成过的二维码会自动保存在这里",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.52f)
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    state = historyListState,
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(history) { item ->
                                        HistoryItemCard(
                                            item = item,
                                            isSelected = selectedHistoryItem?.id == item.id,
                                            isDarkMode = isDarkMode,
                                            isBusy = isGenerating,
                                            onClick = {
                                                inputText = item.content
                                                selectedHistoryItem = item
                                                errorMessage = null

                                                coroutineScope.launch {
                                                    isGenerating = true
                                                    try {
                                                        val image = qrGenerator.generateQRCode(item.content)
                                                        qrCodeImage = image
                                                        if (image == null) {
                                                            errorMessage = "重新生成 QR 码失败"
                                                        }
                                                    } catch (e: Exception) {
                                                        errorMessage = "重新生成 QR 码时发生错误: ${e.message}"
                                                    } finally {
                                                        isGenerating = false
                                                    }
                                                }
                                            },
                                            onDelete = {
                                                coroutineScope.launch {
                                                    try {
                                                        if (selectedHistoryItem?.id == item.id) {
                                                            selectedHistoryItem = null
                                                            qrCodeImage = null
                                                            inputText = ""
                                                        }
                                                        history = historyManager.removeHistoryItem(item.id)
                                                    } catch (e: Exception) {
                                                        errorMessage = "删除历史记录失败: ${e.message}"
                                                    }
                                                }
                                            },
                                            onDuplicate = { duplicateAndIncrement(item) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    isDarkMode: Boolean,
    fillHeight: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Card(
        modifier = modifier,
        backgroundColor = Color.Transparent,
        border = BorderStroke(1.dp, panelBorderColor(isDarkMode)),
        elevation = 0.dp,
        shape = shape
    ) {
        Box(
            modifier = (if (fillHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                .background(panelBrush(isDarkMode))
        ) {
            Column(
                modifier = if (fillHeight) {
                    Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                },
                content = content
            )
        }
    }
}

@Composable
private fun GradientActionButton(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val active = enabled || loading
    val contentColor = if (active) Color.White else MaterialTheme.colors.onSurface.copy(alpha = 0.42f)
    val gradientColors = if (active) {
        listOf(Color(0xFF70B4FF), Color(0xFF3E78F2))
    } else {
        listOf(
            MaterialTheme.colors.onSurface.copy(alpha = 0.10f),
            MaterialTheme.colors.onSurface.copy(alpha = 0.07f)
        )
    }

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(Brush.horizontalGradient(gradientColors))
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                QrGlyph(
                    color = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (loading) "生成中..." else "生成 QR 码",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

/** Small QR mark drawn in code so the header/history icons match the app icon. */
@Composable
private fun QrGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val grid = 7f
        val unit = size.minDimension / 8.6f
        val startX = (size.width - unit * grid) / 2f
        val startY = (size.height - unit * grid) / 2f

        fun drawFinder(col: Int, row: Int) {
            val x = startX + col * unit
            val y = startY + row * unit
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(unit * 3f, unit * 3f),
                cornerRadius = CornerRadius(unit * 0.8f),
                style = Stroke(width = unit * 0.72f)
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(x + unit * 1.05f, y + unit * 1.05f),
                size = Size(unit * 0.9f, unit * 0.9f),
                cornerRadius = CornerRadius(unit * 0.25f)
            )
        }

        drawFinder(0, 0)
        drawFinder(4, 0)
        drawFinder(0, 4)

        listOf(4 to 4, 6 to 4, 5 to 5, 4 to 6, 6 to 6).forEach { (col, row) ->
            drawRoundRect(
                color = color,
                topLeft = Offset(startX + col * unit, startY + row * unit),
                size = Size(unit, unit),
                cornerRadius = CornerRadius(unit * 0.30f)
            )
        }
    }
}

@Composable
fun HistoryItemCard(
    item: QRCodeHistoryItem,
    isSelected: Boolean,
    isDarkMode: Boolean,
    isBusy: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    val cardBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colors.primary.copy(alpha = if (isDarkMode) 0.18f else 0.10f),
                MaterialTheme.colors.primary.copy(alpha = if (isDarkMode) 0.06f else 0.04f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colors.surface.copy(alpha = if (isDarkMode) 0.74f else 0.98f),
                MaterialTheme.colors.surface.copy(alpha = if (isDarkMode) 0.52f else 0.92f)
            )
        )
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colors.primary.copy(alpha = 0.78f)
    } else {
        panelBorderColor(isDarkMode)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = Color.Transparent,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.18f)
                            else MaterialTheme.colors.primary.copy(alpha = 0.08f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    QrGlyph(
                        color = if (isSelected) MaterialTheme.colors.primary
                        else MaterialTheme.colors.primary.copy(alpha = 0.72f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.content,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.displayTime,
                            fontSize = 12.sp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.48f)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colors.secondary.copy(alpha = 0.14f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "当前",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.secondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(
                    modifier = Modifier
                        .width(34.dp)
                        .height(72.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDelete,
                        enabled = !isBusy,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "删除此记录",
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.34f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                if (isBusy) {
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colors.onSurface.copy(alpha = 0.10f),
                                            MaterialTheme.colors.onSurface.copy(alpha = 0.07f)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colors.primary,
                                            MaterialTheme.colors.secondary
                                        )
                                    )
                                }
                            )
                            .clickable(enabled = !isBusy, onClick = onDuplicate),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "复制并生成下一个编号",
                            tint = if (isBusy) MaterialTheme.colors.onSurface.copy(alpha = 0.36f) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun panelBorderColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) DarkPanelBorder.copy(alpha = 0.90f) else LightPanelBorder
}

fun main() = application {
    try {
        CharsetCompatibilityChecker.checkAndInitializeCharsets()
    } catch (e: Exception) {
        println("字符集初始化失败: ${e.message}")
        e.printStackTrace()
    }

    val windowState = rememberWindowState(width = 1280.dp, height = 860.dp)

    val windowIcon = remember {
        Thread.currentThread().contextClassLoader
            .getResourceAsStream("applogo.svg")
            ?.use { stream ->
                @Suppress("DEPRECATION")
                loadSvgPainter(stream, Density(1f))
            }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "QR码生成器",
        state = windowState,
        icon = windowIcon
    ) {
        App()
    }
}
