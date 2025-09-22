package com.qrcode.generator

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CharsetCompatibilityChecker {
    
    private fun logError(error: String, exception: Exception? = null) {
        try {
            val logDir = File(System.getProperty("user.home"), ".qrcode_logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            
            val logFile = File(logDir, "charset_compatibility.log")
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            
            val errorMessage = buildString {
                append("[$timestamp] $error\n")
                if (exception != null) {
                    append("Exception: ${exception.javaClass.simpleName}: ${exception.message}\n")
                }
                append("---\n")
            }
            
            logFile.appendText(errorMessage)
            println(errorMessage)
        } catch (e: Exception) {
            println("[$error] Failed to write charset compatibility log: ${e.message}")
        }
    }
    
    fun checkAndInitializeCharsets() {
        try {
            logError("开始字符集兼容性检查...")
            
            // 检查默认字符集
            val defaultCharset = Charset.defaultCharset()
            logError("默认字符集: $defaultCharset")
            
            // 检查UTF-8是否可用
            try {
                val utf8 = StandardCharsets.UTF_8
                logError("UTF-8字符集可用: $utf8")
            } catch (e: Exception) {
                logError("UTF-8字符集不可用", e)
                throw e
            }
            
            // 检查所有可用字符集
            val availableCharsets = Charset.availableCharsets()
            logError("可用字符集数量: ${availableCharsets.size}")
            
            // 检查ZXing可能需要的字符集
            val requiredCharsets = listOf(
                "UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE",
                "ISO-8859-1", "US-ASCII"
            )
            
            val missingCharsets = mutableListOf<String>()
            for (charsetName in requiredCharsets) {
                try {
                    val charset = Charset.forName(charsetName)
                    logError("字符集 $charsetName 可用: $charset")
                } catch (e: Exception) {
                    missingCharsets.add(charsetName)
                    logError("字符集 $charsetName 不可用", e)
                }
            }
            
            // 特别检查可能导致问题的字符集
            val problematicCharsets = listOf(
                "EUC_JP", "EUC-JP", "Shift_JIS", "ISO-2022-JP"
            )
            
            for (charsetName in problematicCharsets) {
                try {
                    Charset.forName(charsetName)
                    logError("潜在问题字符集 $charsetName 存在")
                } catch (e: Exception) {
                    logError("潜在问题字符集 $charsetName 不存在（这是好事）")
                }
            }
            
            if (missingCharsets.isNotEmpty()) {
                logError("警告: 缺少以下字符集: $missingCharsets")
            }
            
            // 强制设置系统属性
            System.setProperty("file.encoding", "UTF-8")
            System.setProperty("sun.jnu.encoding", "UTF-8")
            System.setProperty("user.language", "zh")
            System.setProperty("user.country", "CN")
            
            logError("字符集兼容性检查完成")
            
        } catch (e: Exception) {
            logError("字符集兼容性检查失败", e)
            throw e
        }
    }
}