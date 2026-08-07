package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.NoxaDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.SavedPromptEntity
import com.example.data.model.UserProfile
import com.example.data.repository.NoxaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoxaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoxaRepository

    val userProfile = MutableStateFlow(UserProfile())
    val selectedTab = MutableStateFlow(0) // 0: Home, 1: Chat, 2: Productivity, 3: Profile

    val availableModels = listOf("Noxa-V4 Ultra", "Noxa-Reasoning", "Noxa-Creative Image")
    val selectedModel = MutableStateFlow("Noxa-V4 Ultra")

    val conversations: StateFlow<List<ConversationEntity>>
    val savedPrompts: StateFlow<List<SavedPromptEntity>>

    val activeConversationId = MutableStateFlow<String?>(null)
    val activeMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())

    val isGenerating = MutableStateFlow(false)
    val inputText = MutableStateFlow("")

    // Voice AI simulation state
    val isVoiceActive = MutableStateFlow(false)
    val voiceTranscript = MutableStateFlow("")

    init {
        val database = NoxaDatabase.getDatabase(application)
        repository = NoxaRepository(database.noxaDao())

        conversations = repository.allConversations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savedPrompts = repository.savedPrompts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Pre-populate default seed prompts if empty
        viewModelScope.launch {
            savedPrompts.collectLatest { prompts ->
                if (prompts.isEmpty()) {
                    seedDefaultPrompts()
                }
            }
        }

        // Pre-populate default initial conversation if empty
        viewModelScope.launch {
            conversations.collectLatest { convs ->
                if (convs.isEmpty() && activeConversationId.value == null) {
                    val newId = repository.createNewConversation(
                        title = "Enterprise Architecture Strategy",
                        category = "Developer",
                        modelName = selectedModel.value
                    )
                    activeConversationId.value = newId
                } else if (convs.isNotEmpty() && activeConversationId.value == null) {
                    activeConversationId.value = convs.first().id
                }
            }
        }

        // Observe active conversation messages
        viewModelScope.launch {
            activeConversationId.collectLatest { convId ->
                if (convId != null) {
                    repository.getMessages(convId).collectLatest { msgs ->
                        activeMessages.value = msgs
                    }
                }
            }
        }
    }

    private suspend fun seedDefaultPrompts() {
        repository.savePrompt(
            title = "Code Refactoring & Security Audit",
            text = "Perform a thorough security and performance audit on this Kotlin / Jetpack Compose codebase. Point out bottlenecks, memory leaks, and thread concurrency improvements.",
            category = "Developer",
            iconType = "code"
        )
        repository.savePrompt(
            title = "Executive Market Analysis",
            text = "Generate a comprehensive SWOT and competitive intelligence analysis for launching an enterprise AI platform targeting Fortune 500 decision makers.",
            category = "Analyst",
            iconType = "analytics"
        )
        repository.savePrompt(
            title = "Research Paper Summarizer",
            text = "Synthesize the core methodology, mathematical formulas, key findings, and practical applications from this AI research document.",
            category = "Research",
            iconType = "science"
        )
        repository.savePrompt(
            title = "Luxury Design System Brief",
            text = "Draft a detailed UI/UX specification for a high-contrast dark theme with glassmorphism micro-interactions and gold/indigo accents.",
            category = "Creative",
            iconType = "palette"
        )
    }

    fun selectTab(tabIndex: Int) {
        selectedTab.value = tabIndex
    }

    fun selectModel(modelName: String) {
        selectedModel.value = modelName
    }

    fun startNewChat(category: String = "General", initialPrompt: String = "") {
        viewModelScope.launch {
            val title = if (initialPrompt.isNotBlank()) {
                if (initialPrompt.length > 28) initialPrompt.take(28) + "..." else initialPrompt
            } else "New Enterprise Session"

            val newId = repository.createNewConversation(
                title = title,
                category = category,
                modelName = selectedModel.value
            )
            activeConversationId.value = newId
            selectedTab.value = 1 // Navigate to Chat

            if (initialPrompt.isNotBlank()) {
                sendMessage(initialPrompt)
            }
        }
    }

    fun selectConversation(conversationId: String) {
        activeConversationId.value = conversationId
        selectedTab.value = 1
    }

    fun sendMessage(text: String) {
        val prompt = text.ifBlank { inputText.value.trim() }
        if (prompt.isBlank()) return

        val convId = activeConversationId.value ?: return

        inputText.value = ""
        isGenerating.value = true

        viewModelScope.launch {
            try {
                repository.sendMessage(
                    conversationId = convId,
                    userMessageText = prompt,
                    modelName = selectedModel.value
                )
            } finally {
                isGenerating.value = false
            }
        }
    }

    fun toggleVoiceAI() {
        isVoiceActive.value = !isVoiceActive.value
        if (isVoiceActive.value) {
            voiceTranscript.value = "Listening to Wasim Jafar..."
        } else {
            voiceTranscript.value = ""
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (activeConversationId.value == id) {
                val remaining = conversations.value.filter { it.id != id }
                activeConversationId.value = remaining.firstOrNull()?.id
            }
        }
    }
}
