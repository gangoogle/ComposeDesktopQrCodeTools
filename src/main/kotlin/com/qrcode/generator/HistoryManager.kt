package com.qrcode.generator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

class HistoryManager {
    private val historyFile = File(System.getProperty("user.home"), ".qrcode_history.json")
    private val json = Json { prettyPrint = true }
    
    private fun logError(error: String, exception: Exception? = null) {
        try {
            val logDir = File(System.getProperty("user.home"), ".qrcode_logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            
            val logFile = File(logDir, "history_error.log")
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            
            val errorMessage = buildString {
                append("[$timestamp] $error\n")
                if (exception != null) {
                    append("Exception: ${exception.javaClass.simpleName}: ${exception.message}\n")
                    val sw = StringWriter()
                    exception.printStackTrace(PrintWriter(sw))
                    append("Stack trace:\n$sw\n")
                }
                append("---\n")
            }
            
            logFile.appendText(errorMessage)
            println(errorMessage)
        } catch (e: Exception) {
            println("[$error] Failed to write history log: ${e.message}")
            exception?.printStackTrace()
        }
    }
    
    suspend fun loadHistory(): List<QRCodeHistoryItem> = withContext(Dispatchers.IO) {
        try {
            logError("Loading history from: ${historyFile.absolutePath}")
            
            if (!historyFile.exists()) {
                logError("History file does not exist")
                return@withContext emptyList()
            }
            
            val jsonText = historyFile.readText()
            if (jsonText.isBlank()) {
                logError("History file is blank")
                return@withContext emptyList()
            }
            
            val result = json.decodeFromString<List<QRCodeHistoryItem>>(jsonText)
            logError("Successfully loaded ${result.size} history items")
            result
        } catch (e: Exception) {
            logError("Error loading history", e)
            emptyList()
        }
    }
    
    suspend fun saveHistory(history: List<QRCodeHistoryItem>) = withContext(Dispatchers.IO) {
        try {
            logError("Saving ${history.size} history items to: ${historyFile.absolutePath}")
            
            // 确保父目录存在
            historyFile.parentFile?.let { parent ->
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }
            
            val jsonText = json.encodeToString(history)
            historyFile.writeText(jsonText)
            logError("Successfully saved history")
        } catch (e: IOException) {
            logError("Error saving history", e)
        }
    }
    
    suspend fun addHistoryItem(item: QRCodeHistoryItem): List<QRCodeHistoryItem> {
        val currentHistory = loadHistory().toMutableList()
        
        // 检查是否已存在相同内容的记录
        val existingIndex = currentHistory.indexOfFirst { it.content == item.content }
        if (existingIndex != -1) {
            // 如果存在，移除旧记录
            currentHistory.removeAt(existingIndex)
        }
        
        // 添加新记录到开头
        currentHistory.add(0, item)
        
        // 限制历史记录数量（保留最近100条）
        if (currentHistory.size > 100) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        saveHistory(currentHistory)
        return currentHistory
    }
    
    suspend fun clearHistory() {
        saveHistory(emptyList())
    }
    
    suspend fun removeHistoryItem(itemId: String): List<QRCodeHistoryItem> {
        val currentHistory = loadHistory().toMutableList()
        
        // 查找并移除指定ID的记录
        val indexToRemove = currentHistory.indexOfFirst { it.id == itemId }
        if (indexToRemove != -1) {
            currentHistory.removeAt(indexToRemove)
            logError("Removed history item with ID: $itemId")
        } else {
            logError("History item with ID $itemId not found")
        }
        
        saveHistory(currentHistory)
        return currentHistory
    }
}
