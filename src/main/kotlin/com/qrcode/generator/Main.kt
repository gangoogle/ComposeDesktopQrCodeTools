package com.qrcode.generator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    var inputText by remember { mutableStateOf("") }
    var qrCodeImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var history by remember { mutableStateOf<List<QRCodeHistoryItem>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }
    var selectedHistoryItem by remember { mutableStateOf<QRCodeHistoryItem?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val qrGenerator = remember { QRCodeGenerator() }
    val historyManager = remember { HistoryManager() }
    val coroutineScope = rememberCoroutineScope()

    // 启动时加载历史记录
    LaunchedEffect(Unit) {
        try {
            history = historyManager.loadHistory()
        } catch (e: Exception) {
            errorMessage = "加载历史记录失败: ${e.message}"
        }
    }

    // QR码生成函数
    fun generateQRCode() {
        if (inputText.isNotBlank()) {
            coroutineScope.launch {
                isGenerating = true
                errorMessage = null
                try {
                    val image = qrGenerator.generateQRCode(inputText.trim())
                    qrCodeImage = image

                    if (image != null) {
                        // 添加到历史记录
                        val historyItem = QRCodeHistoryItem.create(inputText.trim())
                        history = historyManager.addHistoryItem(historyItem)
                        selectedHistoryItem = historyItem
                    } else {
                        errorMessage = "QR码生成失败，请检查输入内容并重试"
                    }
                } catch (e: Exception) {
                    errorMessage = "生成QR码时发生错误: ${e.message}"
                } finally {
                    isGenerating = false
                }
            }
        }
    }

    MaterialTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 左侧：输入和QR码显示区域
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .padding(end = 16.dp)
            ) {
                // 标题
                Text(
                    text = "QR码生成器",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 输入框
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("输入文本内容") },
                    placeholder = { Text("在此粘贴或输入要生成QR码的文本... (按回车键快速生成)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp) {
                                if (inputText.isNotBlank() && !isGenerating) {
                                    generateQRCode()
                                    inputText = inputText.dropLast(1)
                                }
                                true
                            } else {
                                false
                            }
                        },
                    textStyle = TextStyle(fontSize = 23.sp, fontWeight = FontWeight.Bold),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 生成按钮
                Button(
                    onClick = { generateQRCode() },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成中...", color = Color.White)
                    } else {
                        Icon(Icons.Default.Create, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成QR码", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 错误消息显示
                errorMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.Red.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message,
                                color = Color.Red,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { errorMessage = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "关闭",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // QR码显示区域
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        qrCodeImage?.let { image ->
                            Image(
                                bitmap = image,
                                contentDescription = "生成的QR码",
                                modifier = Modifier
                                    .size(300.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } ?: run {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "QR码将在此显示",
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // 右侧：历史记录
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "历史记录",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                historyManager.clearHistory()
                                history = emptyList()
                                selectedHistoryItem = null
                            }
                        },
                        enabled = history.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "清空历史",
                            tint = if (history.isNotEmpty()) Color.Red else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 历史记录列表
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = 2.dp
                ) {
                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.List,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "暂无历史记录",
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(history) { item ->
                                HistoryItemCard(
                                    item = item,
                                    isSelected = selectedHistoryItem?.id == item.id,
                                    onClick = {
                                        inputText = item.content
                                        selectedHistoryItem = item
                                        errorMessage = null

                                        // 重新生成QR码
                                        coroutineScope.launch {
                                            isGenerating = true
                                            try {
                                                val image = qrGenerator.generateQRCode(item.content)
                                                qrCodeImage = image
                                                if (image == null) {
                                                    errorMessage = "重新生成QR码失败"
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = "重新生成QR码时发生错误: ${e.message}"
                                            } finally {
                                                isGenerating = false
                                            }
                                        }
                                    },
                                    onDelete = {
                                        coroutineScope.launch {
                                            try {
                                                // 如果删除的是当前选中的项目，清除选中状态
                                                if (selectedHistoryItem?.id == item.id) {
                                                    selectedHistoryItem = null
                                                    qrCodeImage = null
                                                    inputText = ""
                                                }

                                                // 删除历史记录项目
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

@Composable
fun HistoryItemCard(
    item: QRCodeHistoryItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().border(
            if (isSelected) BorderStroke(2.dp, MaterialTheme.colors.primary) else BorderStroke(0.dp, Color.Transparent)
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧内容区域（可点击）
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = item.content,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 18.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Bold
                )

            }

            // 右侧删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "删除此记录",
                    tint = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth(1f).height(1.dp).background(Color.LightGray)) {

        }
    }
}

fun main() = application {
    // 在启动应用前进行字符集兼容性检查
    try {
        CharsetCompatibilityChecker.checkAndInitializeCharsets()
    } catch (e: Exception) {
        println("字符集初始化失败: ${e.message}")
        e.printStackTrace()
        // 不要因为字符集问题而阻止应用启动，但要记录错误
    }

    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "QR码生成器",
        state = windowState
    ) {
        App()
    }
}
