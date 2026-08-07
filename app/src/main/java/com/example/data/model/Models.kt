package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val modelName: String,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val sender: String, // "USER" or "AI"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val codeLanguage: String? = null,
    val modelUsed: String? = null
)

@Entity(tableName = "saved_prompts")
data class SavedPromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val promptText: String,
    val category: String,
    val iconType: String
)

data class UserProfile(
    val name: String = "Wasim Jafar",
    val title: String = "Founder & Owner",
    val plan: String = "Pro Enterprise",
    val computationPercent: Int = 82,
    val tokensUsedThisMonth: String = "1.42M",
    val totalTokenLimit: String = "5.00M",
    val activeModel: String = "Noxa-V4 Ultra",
    val apiKeysConfigured: Boolean = true
)

enum class IntelligenceCategory(val title: String, val subtitle: String, val iconName: String) {
    DEVELOPER("Developer", "Code Review & Logic", "code"),
    ANALYST("Analyst", "Deep Data Insights", "analytics"),
    RESEARCH("Research", "Academic & Science", "science"),
    CREATIVE("Creative", "Design & Writing", "palette"),
    VOICE("Voice AI", "Realtime Speech Mode", "mic"),
    DOCUMENT("Document AI", "PDF & File Processing", "document")
}
