package com.qrcode.generator

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class QRCodeGenerator {
    
    init {
        // 强制设置默认字符集为UTF-8，避免ZXing库的字符集问题
        try {
            CharsetCompatibilityChecker.checkAndInitializeCharsets()
            logError("Character set initialization successful. Default charset: ${Charset.defaultCharset()}")
        } catch (e: Exception) {
            logError("Character set initialization failed", e)
        }
    }
    
    private fun logError(error: String, exception: Exception? = null) {
        try {
            val logDir = File(System.getProperty("user.home"), ".qrcode_logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            
            val logFile = File(logDir, "error.log")
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
            
            // 同时输出到控制台
            println(errorMessage)
        } catch (e: Exception) {
            // 如果日志记录失败，至少输出到控制台
            println("[$error] Failed to write log: ${e.message}")
            exception?.printStackTrace()
        }
    }
    
    suspend fun generateQRCode(
        text: String,
        size: Int = 300,
        backgroundColor: Color = Color.WHITE,
        foregroundColor: Color = Color.BLACK
    ): ImageBitmap? = withContext(Dispatchers.Default) {
        try {
            logError("Starting QR code generation for text length: ${text.length}")
            
            if (text.isBlank()) {
                logError("Input text is blank")
                return@withContext null
            }
            
            // 确保使用UTF-8编码的文本
            val utf8Text = String(text.toByteArray(StandardCharsets.UTF_8), StandardCharsets.UTF_8)
            
            val writer = QRCodeWriter()
            val hints = mutableMapOf<com.google.zxing.EncodeHintType, Any>()
            hints[com.google.zxing.EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[com.google.zxing.EncodeHintType.ERROR_CORRECTION] = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M
            
            val bitMatrix: BitMatrix = writer.encode(utf8Text, BarcodeFormat.QR_CODE, size, size, hints)
            
            val bufferedImage = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
            
            for (x in 0 until size) {
                for (y in 0 until size) {
                    val color = if (bitMatrix[x, y]) foregroundColor.rgb else backgroundColor.rgb
                    bufferedImage.setRGB(x, y, color)
                }
            }
            
            val result = bufferedImage.toComposeImageBitmap()
            logError("QR code generation successful")
            result
        } catch (e: Exception) {
            logError("Error generating QR code", e)
            null
        }
    }
    
    suspend fun generateQRCodeWithCustomization(
        text: String,
        size: Int = 300,
        errorCorrection: String = "M" // L, M, Q, H
    ): ImageBitmap? = withContext(Dispatchers.Default) {
        try {
            logError("Starting QR code generation with customization for text length: ${text.length}")
            
            if (text.isBlank()) {
                logError("Input text is blank")
                return@withContext null
            }
            
            // 确保使用UTF-8编码的文本
            val utf8Text = String(text.toByteArray(StandardCharsets.UTF_8), StandardCharsets.UTF_8)
            
            val writer = QRCodeWriter()
            val hints = mutableMapOf<com.google.zxing.EncodeHintType, Any>()
            
            // 设置错误纠正级别
            when (errorCorrection.uppercase()) {
                "L" -> hints[com.google.zxing.EncodeHintType.ERROR_CORRECTION] = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L
                "M" -> hints[com.google.zxing.EncodeHintType.ERROR_CORRECTION] = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M
                "Q" -> hints[com.google.zxing.EncodeHintType.ERROR_CORRECTION] = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q
                "H" -> hints[com.google.zxing.EncodeHintType.ERROR_CORRECTION] = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H
            }
            
            // 设置字符编码
            hints[com.google.zxing.EncodeHintType.CHARACTER_SET] = "UTF-8"
            
            val bitMatrix: BitMatrix = writer.encode(utf8Text, BarcodeFormat.QR_CODE, size, size, hints)
            
            val bufferedImage = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
            
            for (x in 0 until size) {
                for (y in 0 until size) {
                    val color = if (bitMatrix[x, y]) Color.BLACK.rgb else Color.WHITE.rgb
                    bufferedImage.setRGB(x, y, color)
                }
            }
            
            val result = bufferedImage.toComposeImageBitmap()
            logError("QR code generation with customization successful")
            result
        } catch (e: Exception) {
            logError("Error generating QR code with customization", e)
            null
        }
    }
}
