package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.database.Creation
import com.example.data.database.SavedPrompt
import com.example.data.repository.CreationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class CreatorHubViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val database = AppDatabase.getDatabase(application)
    private val repository = CreationRepository(database.creationDao())

    private val _allCreations = MutableStateFlow<List<Creation>>(emptyList())
    val allCreations: StateFlow<List<Creation>> = _allCreations.asStateFlow()

    private val _favoriteCreations = MutableStateFlow<List<Creation>>(emptyList())
    val favoriteCreations: StateFlow<List<Creation>> = _favoriteCreations.asStateFlow()

    private val _savedPrompts = MutableStateFlow<List<SavedPrompt>>(emptyList())
    val savedPrompts: StateFlow<List<SavedPrompt>> = _savedPrompts.asStateFlow()

    // UI States
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationResult = MutableStateFlow<String?>(null)
    val generationResult: StateFlow<String?> = _generationResult.asStateFlow()

    // Chat State
    data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Hello! I am your AI Creator assistant. How can I help you build today?", false)
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // User State
    data class UserProfile(
        val username: String = "Creator Pro",
        val email: String = "kaleshan42@gmail.com",
        val isPremium: Boolean = false,
        val tokensLeft: Int = 10,
        val maxTokens: Int = 10,
        val creationsCount: Int = 0
    )
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Native TTS Engine
    private var tts: TextToSpeech? = null
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    // Notifications state
    private val _notifications = MutableStateFlow<List<String>>(listOf(
        "Welcome to AI Creator Hub! Generate images, videos, and music instantly.",
        "Tip: Upgrade to Premium to unlock unlimited high-fidelity 4K exports."
    ))
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    init {
        // Initialize Room Database observing
        viewModelScope.launch {
            repository.allCreations.collect {
                _allCreations.value = it
                _userProfile.update { current ->
                    current.copy(creationsCount = it.size)
                }
            }
        }
        viewModelScope.launch {
            repository.favoriteCreations.collect {
                _favoriteCreations.value = it
            }
        }
        viewModelScope.launch {
            repository.allSavedPrompts.collect {
                _savedPrompts.value = it
            }
        }

        // Initialize Native Text-To-Speech
        try {
            tts = TextToSpeech(application, this)
        } catch (e: Exception) {
            Log.e("CreatorHubVM", "Failed to initialize TTS engine", e)
        }

        // Setup some default prompt categories if empty
        viewModelScope.launch {
            repository.allSavedPrompts.first().let { current ->
                if (current.isEmpty()) {
                    val defaultPrompts = listOf(
                        SavedPrompt(title = "Cinematic Neo-Noir", category = "Image", promptText = "A futuristic cyberpunk detective standing under neon rain, cinematic lighting, 8k resolution, photorealistic style."),
                        SavedPrompt(title = "Lofi Sunset Train", category = "Video", promptText = "Anime girl sitting on a train during golden hour sunset, cinematic camera pan, slow dust particle animations."),
                        SavedPrompt(title = "Retro Synthwave Track", category = "Music", promptText = "Generate a fast retro synthwave instrumental beat with heavy bass, upbeat neon gaming vibe, 120 bpm."),
                        SavedPrompt(title = "SEO Captions", category = "Social", promptText = "Write a high-converting Instagram caption for a new AI-powered art app, including tags and emojis.")
                    )
                    defaultPrompts.forEach { repository.insertSavedPrompt(it) }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("CreatorHubVM", "US English is not supported for Speech Synthesis.")
            } else {
                _isTtsReady.value = true
            }
        } else {
            Log.e("CreatorHubVM", "TTS Initialization failed.")
        }
    }

    // Call native Text To Speech
    fun speak(text: String) {
        if (_isTtsReady.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CreatorHubSpeech")
        }
    }

    // Toggle Premium Status
    fun togglePremium() {
        _userProfile.update { it.copy(isPremium = !it.isPremium, tokensLeft = if (!it.isPremium) 999 else 10, maxTokens = if (!it.isPremium) 999 else 10) }
    }

    // Reset daily tokens
    fun resetTokens() {
        _userProfile.update { it.copy(tokensLeft = if (it.isPremium) 999 else 10) }
    }

    // Log a Creation
    fun saveCreation(type: String, title: String, style: String, aspect: String, details: String, result: String) {
        viewModelScope.launch {
            repository.insertCreation(
                Creation(
                    type = type,
                    title = title,
                    styleOrGenre = style,
                    aspectRatioOrMood = aspect,
                    details = details,
                    resultUrlOrText = result
                )
            )
            // Decrement tokens if not premium
            if (!_userProfile.value.isPremium && _userProfile.value.tokensLeft > 0) {
                _userProfile.update { it.copy(tokensLeft = it.tokensLeft - 1) }
            }
        }
    }

    fun deleteCreation(creation: Creation) {
        viewModelScope.launch {
            repository.deleteCreation(creation)
        }
    }

    fun toggleCreationFavorite(creation: Creation) {
        viewModelScope.launch {
            repository.updateCreation(creation.copy(isFavorite = !creation.isFavorite))
        }
    }

    // 1. Image Generator Call
    fun generateImage(prompt: String, style: String, ratio: String, negativePrompt: String, hd: Boolean) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationResult.value = null

            // Prompt Enhancement with Gemini
            val systemInstruction = "You are an expert AI prompt engineer. Take the user's brief prompt and enhance it into a high-fidelity visual composition description based on the style '$style' and aspect ratio '$ratio'. Keep it compact, vivid, and detailed."
            val enhanced = GeminiClient.generateContent(
                prompt = "Enhance this prompt: $prompt. Negative prompt: $negativePrompt",
                systemInstruction = systemInstruction
            )

            // Dynamic mock selection of premium graphics
            val randomSeed = (1..1000).random()
            val imageUrl = "https://picsum.photos/seed/$randomSeed/1024/1024" // Reliable random visual placeholder

            saveCreation(
                type = "image",
                title = prompt,
                style = style,
                aspect = ratio,
                details = "Negative: $negativePrompt\nEnhanced: $enhanced",
                result = imageUrl
            )

            _generationResult.value = imageUrl
            _isGenerating.value = false
        }
    }

    // 2. Video Generator Call
    fun generateVideo(prompt: String, cameraMovement: String, duration: Int, resolution: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationResult.value = null

            val systemInstruction = "You are an AI video screenplay writer. Summarize the video composition, key animations, camera trajectory ($cameraMovement) and visual style for a video prompt: '$prompt' with duration $duration seconds."
            val visualScript = GeminiClient.generateContent(prompt = prompt, systemInstruction = systemInstruction)

            // Mock video identifier matching camera movement
            val videoMockId = when(cameraMovement.lowercase()) {
                "zoom in" -> "zoom_in_orbit"
                "pan left" -> "cinematic_pan_landscape"
                "orbit" -> "dynamic_360_orbit"
                else -> "cinematic_tilt_up"
            }

            saveCreation(
                type = "video",
                title = prompt,
                style = cameraMovement,
                aspect = resolution,
                details = visualScript,
                result = videoMockId
            )

            _generationResult.value = visualScript
            _isGenerating.value = false
        }
    }

    // 3. Music Generator Call
    fun generateMusic(prompt: String, genre: String, mood: String, withLyrics: Boolean) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationResult.value = null

            val systemInstruction = "You are an AI songwriter. Write a brief music metadata package including: 1. Song Title, 2. Dynamic BPM, 3. Chord Progressions, 4. Verse & Chorus Lyrics matching the prompt '$prompt' in the genre '$genre' and mood '$mood'."
            val songDetails = GeminiClient.generateContent(prompt = prompt, systemInstruction = systemInstruction)

            saveCreation(
                type = "music",
                title = prompt,
                style = genre,
                aspect = mood,
                details = if (withLyrics) songDetails else "Instrumental track generated successfully.\nChords: Em - C - G - D\nBPM: 120",
                result = "music_track_${genre.lowercase()}_${mood.lowercase()}"
            )

            _generationResult.value = songDetails
            _isGenerating.value = false
        }
    }

    // 4. Prompt Library Actions
    fun addPromptToLibrary(title: String, category: String, promptText: String) {
        viewModelScope.launch {
            repository.insertSavedPrompt(SavedPrompt(title = title, category = category, promptText = promptText))
        }
    }

    fun deletePromptFromLibrary(prompt: SavedPrompt) {
        viewModelScope.launch {
            repository.deleteSavedPrompt(prompt)
        }
    }

    // 5. AI Chat Assistant Call
    fun sendChatMessage(messageText: String) {
        if (messageText.isBlank()) return
        val userMsg = ChatMessage(messageText, true)
        _chatMessages.update { it + userMsg }

        viewModelScope.launch {
            _isGenerating.value = true
            val conversationContext = _chatMessages.value.takeLast(6).joinToString("\n") { 
                "${if (it.isUser) "User" else "Assistant"}: ${it.text}" 
            }
            
            val systemInstruction = "You are an expert conversational AI built inside AI Creator Hub. Provide concise, smart, and beautifully formatted answers. Aid the user with coding, translation, copywriting, or general creation advice."
            val reply = GeminiClient.generateContent(prompt = conversationContext, systemInstruction = systemInstruction)
            
            _chatMessages.update { it + ChatMessage(reply, false) }
            _isGenerating.value = false

            saveCreation(
                type = "chat",
                title = messageText,
                style = "Chat Assistant",
                aspect = "Direct Text",
                details = "",
                result = reply
            )
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage("Hello! I am your AI Creator assistant. How can I help you build today?", false)
        )
    }

    // 6. AI Voice Tools
    fun generateSpeech(text: String, voiceName: String, speed: Float) {
        viewModelScope.launch {
            _isGenerating.value = true
            // Save the log
            saveCreation(
                type = "voice",
                title = "Text-to-Speech: ${text.take(30)}...",
                style = voiceName,
                aspect = "Speed: ${speed}x",
                details = text,
                result = "voice_out"
            )
            // Trigger actual Speech
            speak(text)
            _isGenerating.value = false
        }
    }

    // 7. AI Photo Editor Call
    fun editPhoto(prompt: String, action: String, originalUri: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationResult.value = null

            val systemInstruction = "You are an expert AI photo retoucher. Describe in detail the photo enhancement adjustments (brightness, contrast, saturation, masking, or filters) required to achieve action '$action' with context '$prompt'."
            val logs = GeminiClient.generateContent(prompt = "Apply photo action: $action with description: $prompt", systemInstruction = systemInstruction)

            saveCreation(
                type = "photo",
                title = "Photo Edit: $action",
                style = action,
                aspect = "HD Render",
                details = logs,
                result = originalUri // We show a styled, modified filter representation in UI
            )

            _generationResult.value = logs
            _isGenerating.value = false
        }
    }

    // 8. AI Logo & Poster Maker
    fun makeLogoOrPoster(category: String, title: String, subtitle: String, style: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationResult.value = null

            val systemInstruction = "You are an expert graphic designer. Describe a professional poster design configuration (color palette, typography pairings, grid structure, focal points, and graphical assets) for $category titled '$title' with subtitle '$subtitle' in style '$style'."
            val designGuide = GeminiClient.generateContent(prompt = "Design a $category titled '$title'", systemInstruction = systemInstruction)

            saveCreation(
                type = "logo",
                title = title,
                style = category,
                aspect = style,
                details = "$subtitle\n\n$designGuide",
                result = "logo_out"
            )

            _generationResult.value = designGuide
            _isGenerating.value = false
        }
    }

    // 9. AI Social Media Tools
    fun generateSocialMedia(prompt: String, toolType: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationResult.value = null

            val systemInstruction = when(toolType.lowercase()) {
                "captions" -> "You are a viral social media strategist. Generate 3 engaging caption options (with emojis, hooks, and line breaks) for: '$prompt'."
                "hashtags" -> "You are a social SEO expert. Generate 25 highly relevant, high-traffic hashtags grouped by category (niche, broad, viral) for: '$prompt'."
                "youtube titles" -> "You are a YouTube thumbnail & click-through-rate specialist. Generate 5 attention-grabbing, highly clickable YouTube titles for: '$prompt'."
                "description" -> "You are a professional YouTube SEO optimizer. Generate a rich video description with timestamps, keywords, and call-to-actions for: '$prompt'."
                else -> "Generate SEO-optimized meta keywords, tags, and terms separated by commas for: '$prompt'."
            }

            val socialText = GeminiClient.generateContent(prompt = prompt, systemInstruction = systemInstruction)

            saveCreation(
                type = "social",
                title = prompt,
                style = toolType,
                aspect = "SEO Enhanced",
                details = socialText,
                result = socialText
            )

            _generationResult.value = socialText
            _isGenerating.value = false
        }
    }

    // Admin commands
    fun sendNotification(text: String) {
        _notifications.update { listOf(text) + it }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("CreatorHubVM", "Error shutting down TTS engine", e)
        }
    }
}
