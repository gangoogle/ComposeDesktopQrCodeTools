package com.qrcode.generator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
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

private val DarkColorPalette = darkColors(
    primary = Color(0xFF6AA7FF),
    primaryVariant = Color(0xFF4F7DE8),
    secondary = Color(0xFF7CE2C7),
    background = Color(0xFF0C1220),
    surface = Color(0xFF111A2D),
    onPrimary = Color.White,
    onSecondary = Color(0xFF06201A),
    onBackground = Color(0xFFF3F7FF),
    onSurface = Color(0xFFE2E8F6),
    error = Color(0xFFFF8A8A),
    onError = Color.White
)

private val LightColorPalette = lightColors(
    primary = Color(0xFF2F6FED),
    primaryVariant = Color(0xFF2255BF),
    secondary = Color(0xFF12B886),
    background = Color(0xFFF4F7FB),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF122033),
    onSurface = Color(0xFF24344D),
    error = Color(0xFFD9485F),
    onError = Color.White
)

private val DarkPanelColor = Color(0xFF121D33)
private val DarkPanelBorder = Color(0xFF273553)
private val LightPanelColor = Color(0xFFFDFEFF)
private val LightPanelBorder = Color(0xFFD7E1F0)

@Composable
@Preview
fun App() {
    var inputText by remember { mutableStateOf("") }
    var qrCodeImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var history by remember { mutableStateOf<List<QRCodeHistoryItem>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }
    var selectedHistoryItem by remember { mutableStateOf<QRCodeHistoryItem?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDarkMode by remember { mutableStateOf(true) }

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

    MaterialTheme(colors = colors) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDarkMode) {
                            listOf(
                                Color(0xFF0A1020),
                                Color(0xFF132242),
                                Color(0xFF0C1220)
                            )
                        } else {
                            listOf(
                                Color(0xFFF6F9FE),
                                Color(0xFFEAF2FF),
                                Color(0xFFF8FBFF)
                            )
                        }
                    )
                )
        ) {
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
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colors.primary.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Create,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colors.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column {
                                            Text(
                                                text = "QR码生成器",
                                                fontSize = 30.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colors.onBackground
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "更大的输入区，更清晰的历史记录，更舒服的桌面阅读体验",
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colors.onBackground.copy(alpha = 0.66f)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { isDarkMode = !isDarkMode },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colors.surface.copy(alpha = 0.82f))
                                        .border(
                                            width = 1.dp,
                                            color = panelBorderColor(isDarkMode),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = if (isDarkMode) "切换到浅色模式" else "切换到深色模式",
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
                            Text(
                                text = "输入内容",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "支持粘贴长文本，按回车可快速生成",
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.62f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                label = {
                                    Text(
                                        text = "输入文本内容",
                                        fontSize = 15.sp
                                    )
                                },
                                placeholder = {
                                    Text(
                                        text = "在此粘贴或输入要生成 QR 码的文本...",
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(106.dp)
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
                                    fontSize = 22.sp,
                                    lineHeight = 23.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colors.onSurface
                                ),
                                maxLines = 6,
                                shape = RoundedCornerShape(18.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.15f),
                                    focusedBorderColor = MaterialTheme.colors.primary,
                                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.18f),
                                    focusedLabelColor = MaterialTheme.colors.primary,
                                    unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.56f),
                                    cursorColor = MaterialTheme.colors.primary,
                                    placeholderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.42f),
                                    textColor = MaterialTheme.colors.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { generateQRCode() },
                                enabled = inputText.isNotBlank() && !isGenerating,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary,
                                    disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                                ),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colors.onPrimary,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "生成中...",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colors.onPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Create,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "生成 QR 码",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colors.onPrimary
                                    )
                                }
                            }
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
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (selectedHistoryItem != null) "当前展示已选内容对应的二维码" else "生成后将在这里展示二维码结果",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.62f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

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
                                        ) {
                                            Image(
                                                bitmap = image,
                                                contentDescription = "生成的 QR 码",
                                                modifier = Modifier
                                                    .padding(18.dp)
                                                    .size(300.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(18.dp))
                                        Text(
                                            text = selectedHistoryItem?.content ?: inputText,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.74f)
                                        )
                                    }
                                } ?: run {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(88.dp)
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(MaterialTheme.colors.primary.copy(alpha = 0.10f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Create,
                                                contentDescription = null,
                                                modifier = Modifier.size(42.dp),
                                                tint = MaterialTheme.colors.primary.copy(alpha = 0.85f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(18.dp))
                                        Text(
                                            text = "QR码将在此显示",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.84f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "输入内容并点击生成按钮，即可在这里查看结果",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.58f)
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
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp),
                                            tint = MaterialTheme.colors.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "历史记录",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colors.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "点击历史项可重新加载内容和二维码",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.60f)
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
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "清空历史",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colors.error.copy(alpha = 0.82f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "清空",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colors.error.copy(alpha = 0.82f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

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
                                                .background(MaterialTheme.colors.primary.copy(alpha = 0.08f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.List,
                                                contentDescription = null,
                                                modifier = Modifier.size(38.dp),
                                                tint = MaterialTheme.colors.primary.copy(alpha = 0.75f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "暂无历史记录",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.82f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "生成过的二维码会自动保存在这里",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.56f)
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    state = historyListState,
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(history) { item ->
                                        HistoryItemCard(
                                            item = item,
                                            isSelected = selectedHistoryItem?.id == item.id,
                                            isDarkMode = isDarkMode,
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
                                            }
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
    Card(
        modifier = modifier,
        backgroundColor = panelBackgroundColor(isDarkMode),
        border = BorderStroke(1.dp, panelBorderColor(isDarkMode)),
        elevation = 0.dp,
        shape = RoundedCornerShape(24.dp)
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

@Composable
fun HistoryItemCard(
    item: QRCodeHistoryItem,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColor = if (isSelected) {
        MaterialTheme.colors.primary.copy(alpha = if (isDarkMode) 0.14f else 0.09f)
    } else {
        MaterialTheme.colors.surface.copy(alpha = if (isDarkMode) 0.68f else 0.95f)
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colors.primary.copy(alpha = 0.85f)
    } else {
        panelBorderColor(isDarkMode)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = cardColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        shape = RoundedCornerShape(20.dp),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.18f)
                        else MaterialTheme.colors.primary.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.72f)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.content,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.displayTime,
                    fontSize = 13.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.54f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除此记录",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.40f),
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun panelBackgroundColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) DarkPanelColor.copy(alpha = 0.96f) else LightPanelColor.copy(alpha = 0.98f)
}

@Composable
private fun panelBorderColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) DarkPanelBorder.copy(alpha = 0.85f) else LightPanelBorder
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
