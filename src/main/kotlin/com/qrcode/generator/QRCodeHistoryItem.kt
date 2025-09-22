package com.qrcode.generator

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Serializable
data class QRCodeHistoryItem(
    val id: String,
    val content: String,
    val timestamp: String,
    val displayTime: String
) {
    companion object {
        fun create(content: String): QRCodeHistoryItem {
            val now = Clock.System.now()
            return QRCodeHistoryItem(
                id = generateId(),
                content = content,
                timestamp = now.toString(),
                displayTime = formatDisplayTime(now)
            )
        }
        
        private fun generateId(): String {
            return System.currentTimeMillis().toString() + (0..999).random()
        }
        
        private fun formatDisplayTime(instant: Instant): String {
            // 简单的时间格式化，实际项目中可能需要更复杂的本地化格式
            return instant.toString().substring(0, 19).replace('T', ' ')
        }
    }
}
