package com.example.data.repository

import com.example.data.api.GeminiApiClient
import com.example.data.db.NoxaDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.SavedPromptEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class NoxaRepository(private val noxaDao: NoxaDao) {

    val allConversations: Flow<List<ConversationEntity>> = noxaDao.getAllConversations()
    val savedPrompts: Flow<List<SavedPromptEntity>> = noxaDao.getAllSavedPrompts()

    fun getMessages(conversationId: String): Flow<List<ChatMessageEntity>> {
        return noxaDao.getMessagesForConversation(conversationId)
    }

    suspend fun createNewConversation(title: String, category: String, modelName: String): String {
        val id = UUID.randomUUID().toString()
        val conversation = ConversationEntity(
            id = id,
            title = title,
            category = category,
            modelName = modelName
        )
        noxaDao.insertConversation(conversation)
        return id
    }

    suspend fun sendMessage(
        conversationId: String,
        userMessageText: String,
        modelName: String
    ): String {
        // 1. Save user message
        val userMsgId = UUID.randomUUID().toString()
        val userEntity = ChatMessageEntity(
            id = userMsgId,
            conversationId = conversationId,
            sender = "USER",
            content = userMessageText,
            timestamp = System.currentTimeMillis()
        )
        noxaDao.insertMessage(userEntity)

        // 2. Fetch AI response
        val aiResponse = GeminiApiClient.generateContent(
            prompt = userMessageText,
            modelName = modelName
        )

        // 3. Save AI message
        val aiMsgId = UUID.randomUUID().toString()
        val aiEntity = ChatMessageEntity(
            id = aiMsgId,
            conversationId = conversationId,
            sender = "AI",
            content = aiResponse,
            timestamp = System.currentTimeMillis(),
            modelUsed = modelName
        )
        noxaDao.insertMessage(aiEntity)

        return aiResponse
    }

    suspend fun savePrompt(title: String, text: String, category: String, iconType: String) {
        noxaDao.insertSavedPrompt(
            SavedPromptEntity(
                title = title,
                promptText = text,
                category = category,
                iconType = iconType
            )
        )
    }

    suspend fun deleteConversation(id: String) {
        noxaDao.deleteConversation(id)
        noxaDao.clearMessagesForConversation(id)
    }
}
