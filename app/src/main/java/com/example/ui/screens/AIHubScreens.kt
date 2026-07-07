package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.rotate
import coil.compose.AsyncImage
import com.example.data.database.Creation
import com.example.data.database.SavedPrompt
import com.example.ui.viewmodel.CreatorHubViewModel
import com.example.ui.viewmodel.CreatorHubViewModel.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Color constants for high-fidelity gradients
val CosmicBg = Color(0xFF090616)
val CosmicSlate = Color(0xFF141125)
val CosmicCard = Color(0xFF1D1935)
val NeonPurple = Color(0xFF9E00FF)
val NeonCyan = Color(0xFF00F0FF)
val NeonPink = Color(0xFFFF007F)
val SoftGold = Color(0xFFFFD700)

@Composable
fun MainBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CosmicBg,
                        Color(0xFF110B22),
                        Color(0xFF07040E)
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .testTag(testTag)
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = if (enabled) listOf(NeonPurple, NeonCyan) else listOf(Color.Gray, Color.DarkGray)
                )
            )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// ---------------------------------------------------------
// 1. DASHBOARD / HOME SCREEN
// ---------------------------------------------------------
@Composable
fun DashboardScreen(
    viewModel: CreatorHubViewModel,
    onNavigateToTool: (String) -> Unit,
    onShowNotifications: () -> Unit
) {
    val creations by viewModel.allCreations.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    MainBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            // Header Banner with generated Hero Image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    // Try to load the generated premium image banner
                    AsyncImage(
                        model = com.example.R.drawable.img_ai_hub_banner_1783248267612,
                        contentDescription = "AI Hub Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Translucent dark gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )
                    // Banner Title details
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = SoftGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "NEXT-GEN CREATIVITY",
                                color = SoftGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                        Text(
                            "AI Creator Hub",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    // Premium state pill on top right
                    Surface(
                        color = if (profile.isPremium) NeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (profile.isPremium) NeonCyan else Color.White.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clickable { viewModel.togglePremium() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (profile.isPremium) Icons.Rounded.Verified else Icons.Rounded.Star,
                                contentDescription = null,
                                tint = if (profile.isPremium) NeonCyan else SoftGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (profile.isPremium) "PRO PLAN" else "FREE TRIAL",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Access Tokens / Subscriptions indicator
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Daily Credits Status", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (profile.isPremium) "Unlimited AI Generations" else "${profile.tokensLeft}/${profile.maxTokens} Generations Left Today",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        IconButton(
                            onClick = { onNavigateToTool("profile") },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Rounded.AccountCircle, contentDescription = "Profile", tint = Color.White)
                        }
                    }
                }
            }

            // AI Tools Grid Sections
            item {
                Text(
                    "AI Creative Suites",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Render Tools List
            val tools = listOf(
                ToolItem("Image Generator", "image_gen", Icons.Rounded.Image, "Generate art from prompts", NeonPurple),
                ToolItem("Video Generator", "video_gen", Icons.Rounded.MovieFilter, "Text or image to video", NeonCyan),
                ToolItem("Music Generator", "music_gen", Icons.Rounded.MusicNote, "Write original songs", NeonPink),
                ToolItem("AI Chatbot", "chat_assistant", Icons.Rounded.ChatBubble, "Translate, code, assist", Color(0xFF00FF87)),
                ToolItem("AI Voice Tools", "voice_tools", Icons.Rounded.RecordVoiceOver, "TTS / Dictation", SoftGold),
                ToolItem("Photo Editor", "photo_editor", Icons.Rounded.Brush, "Background remover, upscale", Color(0xFFFF5252)),
                ToolItem("Logo & Poster", "logo_poster", Icons.Rounded.Category, "Logo & youtube thumbnail", Color(0xFF7C4DFF)),
                ToolItem("Social Media Suite", "social_media", Icons.Rounded.Share, "Captions, hashtags, titles", Color(0xFFFFE082)),
                ToolItem("AI Prompt Library", "prompt_library", Icons.Rounded.Bookmark, "Ready-made prompt ideas", Color(0xFF4DB6AC))
            )

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tools.chunked(2).forEach { rowTools ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowTools.forEach { tool ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                        .clickable { onNavigateToTool(tool.route) }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            color = tool.color.copy(alpha = 0.15f),
                                            shape = CircleShape,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Column {
                                            Text(
                                                tool.title,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                tool.desc,
                                                color = Color.LightGray.copy(alpha = 0.7f),
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowTools.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Recent Creations Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Recent Creations",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "See All",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToTool("profile") }
                    )
                }
            }

            if (creations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(CosmicSlate, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No creations yet. Fire up a tool to begin!",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(creations.take(4)) { creation ->
                    CreationHistoryRow(creation = creation, onToggleFavorite = { viewModel.toggleCreationFavorite(it) })
                }
            }
        }
    }
}

data class ToolItem(val title: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val desc: String, val color: Color)

@Composable
fun CreationHistoryRow(
    creation: Creation,
    onToggleFavorite: (Creation) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSlate),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (creation.type) {
                    "image" -> Icons.Rounded.Image
                    "video" -> Icons.Rounded.MovieFilter
                    "music" -> Icons.Rounded.MusicNote
                    "chat" -> Icons.Rounded.ChatBubble
                    "voice" -> Icons.Rounded.RecordVoiceOver
                    "photo" -> Icons.Rounded.Brush
                    "logo" -> Icons.Rounded.Category
                    "social" -> Icons.Rounded.Share
                    else -> Icons.Rounded.AutoAwesome
                }
                Icon(icon, contentDescription = null, tint = NeonCyan)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    creation.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = NeonPurple.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            creation.type.uppercase(),
                            color = NeonPurple,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (creation.styleOrGenre.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(creation.styleOrGenre, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }

            IconButton(onClick = { onToggleFavorite(creation) }) {
                Icon(
                    imageVector = if (creation.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (creation.isFavorite) NeonPink else Color.Gray
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 2. AI IMAGE GENERATOR SCREEN
// ---------------------------------------------------------
@Composable
fun ImageGeneratorScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit) {
    var prompt by remember { mutableStateOf("") }
    var negativePrompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Cinematic") }
    var selectedRatio by remember { mutableStateOf("1:1") }
    var hdOption by remember { mutableStateOf(true) }

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val creations by viewModel.allCreations.collectAsStateWithLifecycle()
    val imageCreations = creations.filter { it.type == "image" }

    val styles = listOf("Cinematic", "Realistic", "Anime", "Ghibli", "3D", "Watercolor", "Cyberpunk", "Pixar", "Fantasy")
    val ratios = listOf("1:1", "9:16", "16:9", "4:5")

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Image Generator", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Main visual canvas loader
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(CosmicSlate, RoundedCornerShape(20.dp))
                            .border(1.dp, Brush.horizontalGradient(listOf(NeonPurple, NeonCyan)), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = NeonCyan)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("AI is thinking & rendering...", color = Color.White, fontSize = 14.sp)
                            }
                        } else if (imageCreations.isNotEmpty()) {
                            AsyncImage(
                                model = imageCreations.first().resultUrlOrText,
                                contentDescription = "Generated Output",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Your generated masterpiece will appear here", color = Color.LightGray, fontSize = 13.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                // Text Prompt Field
                item {
                    Text("Describe what you want to generate", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("A futuristic digital explorer finding an ancient portal...", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("image_prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CosmicSlate,
                            unfocusedContainerColor = CosmicSlate,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Style selection
                item {
                    Text("Select Artistic Style", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(styles) { style ->
                            val isSelected = selectedStyle == style
                            Surface(
                                color = if (isSelected) NeonPurple else CosmicSlate,
                                border = BorderStroke(1.dp, if (isSelected) NeonCyan else Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { selectedStyle = style }
                            ) {
                                Text(
                                    style,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Aspect Ratio Selection
                item {
                    Text("Select Aspect Ratio", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ratios.forEach { ratio ->
                            val isSelected = selectedRatio == ratio
                            Surface(
                                color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else CosmicSlate,
                                border = BorderStroke(1.dp, if (isSelected) NeonCyan else Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRatio = ratio }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text(
                                        ratio,
                                        color = if (isSelected) NeonCyan else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Optional Advanced Settings
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Advanced Controls", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = negativePrompt,
                                onValueChange = { negativePrompt = it },
                                label = { Text("Negative Prompt (elements to avoid)", color = Color.Gray, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonPurple,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("HD Super-Resolution Enhancement", color = Color.LightGray, fontSize = 12.sp)
                                Switch(
                                    checked = hdOption,
                                    onCheckedChange = { hdOption = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonPurple)
                                )
                            }
                        }
                    }
                }

                // Actions & Generate
                item {
                    GlowButton(
                        text = if (isGenerating) "AI generating artwork..." else "Generate Art",
                        onClick = {
                            if (prompt.isNotBlank()) {
                                viewModel.generateImage(prompt, selectedStyle, selectedRatio, negativePrompt, hdOption)
                            }
                        },
                        enabled = !isGenerating && prompt.isNotBlank(),
                        testTag = "generate_image_btn",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Image History List
                if (imageCreations.isNotEmpty()) {
                    item {
                        Text("Generation Log", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    items(imageCreations) { creation ->
                        CreationHistoryRow(creation = creation, onToggleFavorite = { viewModel.toggleCreationFavorite(it) })
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 3. AI VIDEO GENERATOR SCREEN
// ---------------------------------------------------------
@Composable
fun VideoGeneratorScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit) {
    var prompt by remember { mutableStateOf("") }
    var cameraMovement by remember { mutableStateOf("Zoom In") }
    var selectedDuration by remember { mutableStateOf(5f) }
    var resolution by remember { mutableStateOf("1080p") }

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val creations by viewModel.allCreations.collectAsStateWithLifecycle()
    val videoCreations = creations.filter { it.type == "video" }

    val cameraMoves = listOf("Zoom In", "Pan Left", "Orbit", "Tilt Up", "Static Depth")

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Video Generator", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Video Screen Playback Box Mock
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(CosmicSlate, RoundedCornerShape(20.dp))
                            .border(1.dp, NeonCyan, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = NeonCyan)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("AI is composing cinematic frames...", color = Color.White, fontSize = 13.sp)
                            }
                        } else if (videoCreations.isNotEmpty()) {
                            // Render a gorgeous cinematic poster representation with playback overlays
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = painterResource(id = com.example.R.drawable.ic_launcher_background),
                                    contentDescription = "Playback preview",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f))
                                )
                                Icon(
                                    Icons.Rounded.PlayCircleFilled,
                                    contentDescription = "Play",
                                    tint = NeonCyan,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .align(Alignment.Center)
                                )
                                // Video length/resolution tag
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                ) {
                                    Surface(color = Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            "${videoCreations.first().details.take(20)}...",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                                Icon(Icons.Rounded.MovieFilter, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Your generated cinematic video screenplay appears here", color = Color.LightGray, fontSize = 13.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                // Prompt
                item {
                    Text("Describe Video Action Screenplay", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("A slow drone shot flying over ancient majestic ruins...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().testTag("video_prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CosmicSlate,
                            unfocusedContainerColor = CosmicSlate,
                            focusedBorderColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Camera trajectory select
                item {
                    Text("Cinematic Camera Motion", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cameraMoves) { move ->
                            val isSelected = cameraMovement == move
                            Surface(
                                color = if (isSelected) NeonCyan else CosmicSlate,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { cameraMovement = move }
                            ) {
                                Text(
                                    move,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Duration Slider
                item {
                    Text("Duration: ${selectedDuration.toInt()} Seconds", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = selectedDuration,
                        onValueChange = { selectedDuration = it },
                        valueRange = 3f..15f,
                        steps = 4,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonPurple)
                    )
                }

                // Export Resolution Option
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Export Quality Resolution", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("720p", "1080p").forEach { res ->
                                val isSelected = resolution == res
                                Surface(
                                    color = if (isSelected) NeonPurple.copy(alpha = 0.3f) else CosmicSlate,
                                    border = BorderStroke(1.dp, if (isSelected) NeonPurple else Color.Transparent),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { resolution = res }
                                ) {
                                    Text(
                                        res,
                                        color = if (isSelected) NeonPurple else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Generate button
                item {
                    GlowButton(
                        text = if (isGenerating) "AI processing screenplay..." else "Generate Cinematic Video",
                        onClick = {
                            if (prompt.isNotBlank()) {
                                viewModel.generateVideo(prompt, cameraMovement, selectedDuration.toInt(), resolution)
                            }
                        },
                        enabled = !isGenerating && prompt.isNotBlank(),
                        testTag = "generate_video_btn",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Display video history
                if (videoCreations.isNotEmpty()) {
                    item {
                        Text("Video Master Log", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    items(videoCreations) { creation ->
                        CreationHistoryRow(creation = creation, onToggleFavorite = { viewModel.toggleCreationFavorite(it) })
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 4. AI MUSIC & SONG GENERATOR SCREEN
// ---------------------------------------------------------
@Composable
fun MusicGeneratorScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit) {
    var prompt by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("Pop") }
    var selectedMood by remember { mutableStateOf("Energetic") }
    var lyricsRequired by remember { mutableStateOf(true) }

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val creations by viewModel.allCreations.collectAsStateWithLifecycle()
    val musicCreations = creations.filter { it.type == "music" }

    val genres = listOf("Pop", "Romantic", "Devotional", "Folk", "Hip-Hop", "EDM", "Classical", "Cinematic")
    val moods = listOf("Energetic", "Chill", "Romantic", "Melancholy", "Inspiring", "Dreamy")

    // Cassette/record spin animation
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Song Generator", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // vinyl spinning player mock
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(CosmicSlate, RoundedCornerShape(20.dp))
                            .border(1.dp, NeonPink, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = NeonPink)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Composing lyrics & melodies...", color = Color.White, fontSize = 13.sp)
                            }
                        } else if (musicCreations.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Spinning vinyl
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black,
                                    border = BorderStroke(3.dp, NeonPink),
                                    modifier = Modifier
                                        .size(100.dp)
                                        .rotate(rotationAngle)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Now Playing", color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(musicCreations.first().title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Genre: ${musicCreations.first().styleOrGenre} | Mood: ${musicCreations.first().aspectRatioOrMood}", color = Color.Gray, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(onClick = { viewModel.speak("Playing custom song: " + musicCreations.first().title) }) {
                                            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = NeonCyan)
                                        }
                                        IconButton(onClick = { }) {
                                            Icon(Icons.Rounded.Download, contentDescription = "Download", tint = Color.White)
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = NeonPink, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No tracks generated yet.", color = Color.LightGray, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Prompt text
                item {
                    Text("Describe Song Subject / Inspiration", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("Write a love song about coding under stars...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().testTag("music_prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonPink
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Select genre
                item {
                    Text("Select Genre", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(genres) { genre ->
                            val isSelected = selectedGenre == genre
                            Surface(
                                color = if (isSelected) NeonPink else CosmicSlate,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { selectedGenre = genre }
                            ) {
                                Text(
                                    genre,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Select mood
                item {
                    Text("Select Song Mood", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(moods) { mood ->
                            val isSelected = selectedMood == mood
                            Surface(
                                color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else CosmicSlate,
                                border = BorderStroke(1.dp, if (isSelected) NeonCyan else Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { selectedMood = mood }
                            ) {
                                Text(
                                    mood,
                                    color = if (isSelected) NeonCyan else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Lyrics Toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-Generate Song Lyrics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = lyricsRequired,
                            onCheckedChange = { lyricsRequired = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonPink)
                        )
                    }
                }

                // Generate button
                item {
                    GlowButton(
                        text = if (isGenerating) "AI orchestrating track..." else "Generate Track",
                        onClick = {
                            if (prompt.isNotBlank()) {
                                viewModel.generateMusic(prompt, selectedGenre, selectedMood, lyricsRequired)
                            }
                        },
                        enabled = !isGenerating && prompt.isNotBlank(),
                        testTag = "generate_music_btn",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Lyric Sheet Display
                if (musicCreations.isNotEmpty() && musicCreations.first().details.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Generated Lyrics", color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    musicCreations.first().details,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Display music history
                if (musicCreations.isNotEmpty()) {
                    item {
                        Text("Orchestra Archive", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    items(musicCreations) { creation ->
                        CreationHistoryRow(creation = creation, onToggleFavorite = { viewModel.toggleCreationFavorite(it) })
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 5. AI PROMPT LIBRARY SCREEN
// ---------------------------------------------------------
@Composable
fun PromptLibraryScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit) {
    val savedPrompts by viewModel.savedPrompts.collectAsStateWithLifecycle()
    var searchCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var newTitle by remember { mutableStateOf("") }
    var newPromptText by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Image") }

    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    val categories = listOf("All", "Image", "Video", "Music", "Social")

    val filteredPrompts = savedPrompts.filter {
        (searchCategory == "All" || it.category.equals(searchCategory, ignoreCase = true)) &&
        (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.promptText.contains(searchQuery, ignoreCase = true))
    }

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Prompt Library", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Search bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray) },
                        placeholder = { Text("Search ready-made prompts...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Category chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val isSelected = searchCategory == cat
                            Surface(
                                color = if (isSelected) NeonCyan else CosmicSlate,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { searchCategory = cat }
                            ) {
                                Text(
                                    cat,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Prompt Cards List
                if (filteredPrompts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No prompts found matching filter.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(filteredPrompts) { promptItem ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(promptItem.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Surface(color = NeonPurple.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            promptItem.category.uppercase(),
                                            color = NeonPurple,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(promptItem.promptText, color = Color.LightGray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(promptItem.promptText))
                                            Toast.makeText(context, "Copied prompt to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deletePromptFromLibrary(promptItem) }
                                    ) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = NeonPink, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Create custom prompt template section
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicCard),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Create Custom Prompt Template", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                label = { Text("Title", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newPromptText,
                                onValueChange = { newPromptText = it },
                                label = { Text("Prompt Text Template", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text("Category: ", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                listOf("Image", "Video", "Music", "Social").forEach { cat ->
                                    val isSelected = newCategory == cat
                                    Surface(
                                        color = if (isSelected) NeonPurple else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clickable { newCategory = cat }
                                    ) {
                                        Text(
                                            cat,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            GlowButton(
                                text = "Add to Library",
                                onClick = {
                                    if (newTitle.isNotBlank() && newPromptText.isNotBlank()) {
                                        viewModel.addPromptToLibrary(newTitle, newCategory, newPromptText)
                                        newTitle = ""
                                        newPromptText = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 6. AI CHAT ASSISTANT SCREEN
// ---------------------------------------------------------
@Composable
fun ChatAssistantScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Chat Assistant", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Clear Chat", tint = NeonPink)
                }
            }

            // Message Board List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                if (isGenerating) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            Surface(color = CosmicSlate, shape = RoundedCornerShape(12.dp)) {
                                Text(
                                    "AI is typing thoughts...",
                                    color = NeonCyan,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                items(chatMessages.reversed()) { message ->
                    val isUser = message.isUser
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            color = if (isUser) NeonPurple else CosmicSlate,
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = message.text,
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Chat input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Ask me to write code, translate or brainstorm...", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendChatMessage(messageText)
                            messageText = ""
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = CosmicSlate,
                        unfocusedContainerColor = CosmicSlate,
                        focusedBorderColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(24.dp)
                )

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendChatMessage(messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier
                        .background(NeonCyan, CircleShape)
                        .testTag("send_chat_btn")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 7. AI VOICE TOOLS SCREEN
// ---------------------------------------------------------
@Composable
fun VoiceToolsScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit) {
    var textInput by remember { mutableStateOf("") }
    var selectedVoice by remember { mutableStateOf("Atlas") }
    var speedSetting by remember { mutableStateOf(1.0f) }

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isTtsReady by viewModel.isTtsReady.collectAsStateWithLifecycle()

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Voice Tools", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Info Banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.RecordVoiceOver, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Native Text-To-Speech integration active. Type text, choose a voice, and generate speech live!",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Text Input
                item {
                    Text("Type Text to Synthesize", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Welcome to the AI Creator Hub. Synthesizing voice live on your Android device.", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Voice Options
                item {
                    Text("Select Voice Character Model", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Atlas", "Nova", "Luna", "Kore").forEach { voice ->
                            val isSelected = selectedVoice == voice
                            Surface(
                                color = if (isSelected) NeonPurple else CosmicSlate,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedVoice = voice }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text(voice, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Speed Slider
                item {
                    Text("Voice Speed: ${"%.1f".format(speedSetting)}x", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = speedSetting,
                        onValueChange = { speedSetting = it },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonPurple)
                    )
                }

                // Speech Synthesis Actions
                item {
                    GlowButton(
                        text = if (isGenerating) "AI speaking text..." else "Synthesize & Speak live",
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.generateSpeech(textInput, selectedVoice, speedSetting)
                            }
                        },
                        enabled = !isGenerating && textInput.isNotBlank(),
                        testTag = "generate_speech_btn",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Speech recognition input trigger (Dictation)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicCard),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Speech-to-Text Dictator", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Translate spoken voice into editable text instantly", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(14.dp))
                            IconButton(
                                onClick = { textInput = "Voice recorded: AI platform with speech recognition features" },
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(NeonPink, CircleShape)
                            ) {
                                Icon(Icons.Rounded.Mic, contentDescription = "Listen", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap mic to dictate mock input", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 8. AI PHOTO EDITOR SCREEN
// ---------------------------------------------------------
@Composable
fun PhotoEditorScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit) {
    var prompt by remember { mutableStateOf("") }
    var selectedAction by remember { mutableStateOf("Background Remover") }
    var mockImageLoaded by remember { mutableStateOf(false) }

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val creations by viewModel.allCreations.collectAsStateWithLifecycle()
    val photoCreations = creations.filter { it.type == "photo" }

    val actions = listOf("Background Remover", "Upscale Image", "Object Remover", "Color Correction", "Blur Background")

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Photo Editor", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Main Graphic Board
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(CosmicSlate, RoundedCornerShape(20.dp))
                            .border(1.dp, NeonPurple, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = NeonPurple)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Refining and upscaling image pixel matrices...", color = Color.White, fontSize = 13.sp)
                            }
                        } else if (photoCreations.isNotEmpty()) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = painterResource(id = com.example.R.drawable.ic_launcher_background),
                                    contentDescription = "Edited Output",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp))
                                )
                                // Render transparent or blur visual filters in Compose!
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (photoCreations.first().styleOrGenre == "Background Remover") Color.Black.copy(alpha = 0.8f) else Color.Transparent
                                        )
                                )
                                Surface(
                                    color = NeonCyan,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        "Filter Active: ${photoCreations.first().styleOrGenre}",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                                Icon(Icons.Rounded.Brush, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { mockImageLoaded = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                                ) {
                                    Text("Load Photo to Edit")
                                }
                            }
                        }
                    }
                }

                // Actions Selection
                item {
                    Text("Select Photo Operation", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(actions) { act ->
                            val isSelected = selectedAction == act
                            Surface(
                                color = if (isSelected) NeonPurple else CosmicSlate,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { selectedAction = act }
                            ) {
                                Text(
                                    act,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Description prompt
                item {
                    Text("Instruction Details (Optional)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("E.g. Remove the red cup, restore skin clarity...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Edit Button
                item {
                    GlowButton(
                        text = if (isGenerating) "AI editing pixels..." else "Apply Photo Retouch",
                        onClick = {
                            viewModel.editPhoto(prompt, selectedAction, "loaded_uri")
                        },
                        enabled = !isGenerating,
                        testTag = "edit_photo_btn",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 9. AI LOGO & POSTER MAKER SCREEN
// ---------------------------------------------------------
@Composable
fun LogoPosterScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit) {
    var title by remember { mutableStateOf("Tech Summit") }
    var subtitle by remember { mutableStateOf("Shaping Future AI") }
    var selectedCategory by remember { mutableStateOf("YouTube Thumbnail") }
    var selectedStyle by remember { mutableStateOf("Vaporwave Glow") }

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val creations by viewModel.allCreations.collectAsStateWithLifecycle()
    val logoCreations = creations.filter { it.type == "logo" }

    val categories = listOf("Logo", "YouTube Thumbnail", "Instagram Post", "Business Poster", "Flyer")
    val designStyles = listOf("Vaporwave Glow", "Minimal Corporate", "Comic PopArt", "Retro Neon", "Brutalist Steel")

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logo & Poster Maker", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Interactive dynamic visual drawing board! Uses real Jetpack Compose Drawing Canvas
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = when (selectedStyle.lowercase()) {
                                        "retro neon" -> listOf(Color(0xFF1E0B36), Color(0xFFFF007F))
                                        "comic popart" -> listOf(Color(0xFFFFD700), Color(0xFFFF5252))
                                        "brutalist steel" -> listOf(Color(0xFF37474F), Color(0xFF90A4AE))
                                        else -> listOf(NeonPurple, NeonCyan) // Vaporwave Glow default
                                    }
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing overlay shapes for futuristic art
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.1f),
                                radius = size.minDimension / 2.5f,
                                center = Offset(size.width * 0.8f, size.height * 0.2f)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = selectedCategory.uppercase(),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2
                            )
                        }
                    }
                }

                // Category select
                item {
                    Text("Select Template Category", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                color = if (isSelected) NeonCyan else CosmicSlate,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    cat,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Layout details fields
                item {
                    Text("Headline Title", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                item {
                    Text("Sub-Headline / Slogan", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                // Layout color styles select
                item {
                    Text("Select Design Theme Palette", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(designStyles) { designStyle ->
                            val isSelected = selectedStyle == designStyle
                            Surface(
                                color = if (isSelected) NeonPurple else CosmicSlate,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { selectedStyle = designStyle }
                            ) {
                                Text(
                                    designStyle,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Make & log button
                item {
                    GlowButton(
                        text = "Publish Logo & Poster Design",
                        onClick = {
                            viewModel.makeLogoOrPoster(selectedCategory, title, subtitle, selectedStyle)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 10. AI SOCIAL MEDIA TOOLS SCREEN
// ---------------------------------------------------------
@Composable
fun SocialMediaScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit) {
    var prompt by remember { mutableStateOf("") }
    var selectedTool by remember { mutableStateOf("Captions") }

    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val creations by viewModel.allCreations.collectAsStateWithLifecycle()
    val socialCreations = creations.filter { it.type == "social" }

    val tools = listOf("Captions", "Hashtags", "YouTube Titles", "Description", "Keywords")
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Social Media Suite", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Select tool type
                item {
                    Text("Select Generation Tool", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tools) { tool ->
                            val isSelected = selectedTool == tool
                            Surface(
                                color = if (isSelected) NeonCyan else CosmicSlate,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { selectedTool = tool }
                            ) {
                                Text(
                                    tool,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Describe content
                item {
                    Text("Enter Topic / Product Description", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("E.g. A review of a sleek smart headphone, launching tomorrow...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Generate Button
                item {
                    GlowButton(
                        text = if (isGenerating) "AI formulating tags..." else "Generate Social Content",
                        onClick = {
                            if (prompt.isNotBlank()) {
                                viewModel.generateSocialMedia(prompt, selectedTool)
                            }
                        },
                        enabled = !isGenerating && prompt.isNotBlank(),
                        testTag = "generate_social_btn",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Display response details
                if (socialCreations.isNotEmpty() && socialCreations.first().resultUrlOrText.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("AI Output Summary", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    IconButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(socialCreations.first().resultUrlOrText))
                                            Toast.makeText(context, "Copied text to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    socialCreations.first().resultUrlOrText,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 11. USER ACCOUNT & PROFILE SCREEN
// ---------------------------------------------------------
@Composable
fun UserProfileScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit, onNavigateToAdmin: () -> Unit) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteCreations.collectAsStateWithLifecycle()

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Your Profile Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Profile Card details
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicCard),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(NeonPurple.copy(alpha = 0.2f), CircleShape)
                                    .border(2.dp, NeonCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(profile.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(profile.email, color = Color.Gray, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(profile.creationsCount.toString(), color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Text("Creations", color = Color.Gray, fontSize = 10.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(favorites.size.toString(), color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Text("Favorites", color = Color.Gray, fontSize = 10.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (profile.isPremium) "Unlimited" else "${profile.tokensLeft}", color = SoftGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Text("Tokens", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Premium Unlock Subscription banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { viewModel.togglePremium() }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CardMembership, contentDescription = null, tint = SoftGold, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (profile.isPremium) "Premium Active! Tap to downgrade" else "Unlock Premium Access!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    if (profile.isPremium) "You have unlimited high-speed generations" else "Faster rendering speed & HD Exports",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Saved Creations Favorite Section
                item {
                    Text("Saved Favorite Creations", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                if (favorites.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No favorite creations saved yet.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(favorites) { favorite ->
                        CreationHistoryRow(creation = favorite, onToggleFavorite = { viewModel.toggleCreationFavorite(it) })
                    }
                }

                // Link to Admin
                item {
                    OutlinedButton(
                        onClick = onNavigateToAdmin,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.SupervisorAccount, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Admin Control Panel Portal")
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 12. SETTINGS SCREEN
// ---------------------------------------------------------
@Composable
fun SettingsScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit) {
    var darkMode by remember { mutableStateOf(true) }
    var selectedLang by remember { mutableStateOf("English") }
    var notifyOption by remember { mutableStateOf(true) }

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("System Preferences", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Text("Display Theme Controls", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Card(colors = CardDefaults.cardColors(containerColor = CosmicSlate), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Force Cinematic Dark Theme", color = Color.White, fontSize = 13.sp)
                            Switch(checked = darkMode, onCheckedChange = { darkMode = it }, colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan))
                        }
                    }
                }

                item {
                    Text("App Localization Languages", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Card(colors = CardDefaults.cardColors(containerColor = CosmicSlate), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            listOf("English", "Spanish", "French", "German", "Hindi").forEach { lang ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedLang = lang }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(lang, color = Color.White, fontSize = 13.sp)
                                    if (selectedLang == lang) {
                                        Icon(Icons.Rounded.Check, contentDescription = null, tint = NeonCyan)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Notification Control Settings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Card(colors = CardDefaults.cardColors(containerColor = CosmicSlate), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enable Push Notifications Alerts", color = Color.White, fontSize = 13.sp)
                            Switch(checked = notifyOption, onCheckedChange = { notifyOption = it }, colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 13. ADMIN PANEL SCREEN
// ---------------------------------------------------------
@Composable
fun AdminPanelScreen(viewModel: CreatorHubViewModel, onBack: () -> Unit) {
    var notificationInput by remember { mutableStateOf("") }
    val creations by viewModel.allCreations.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Admin Console Portal", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Platform statistics cards
                item {
                    Text("Platform Analytics Summary", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Card(colors = CardDefaults.cardColors(containerColor = CosmicSlate), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Total Registrations", color = Color.Gray, fontSize = 11.sp)
                                Text("1,482 Users", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = CosmicSlate), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Generations Volume", color = Color.Gray, fontSize = 11.sp)
                                Text("${creations.size} Local", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Dispatch global notification push trigger
                item {
                    Text("Broadcast Alert Notification", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Card(colors = CardDefaults.cardColors(containerColor = CosmicCard), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            OutlinedTextField(
                                value = notificationInput,
                                onValueChange = { notificationInput = it },
                                placeholder = { Text("E.g. Server maintenance scheduled for tomorrow...", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            GlowButton(
                                text = "Send System Notification Broadcast",
                                onClick = {
                                    if (notificationInput.isNotBlank()) {
                                        viewModel.sendNotification(notificationInput)
                                        notificationInput = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Subscriptions overview
                item {
                    Text("Subscription & Verification Tier", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Card(colors = CardDefaults.cardColors(containerColor = CosmicSlate)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Active User", color = Color.White, fontSize = 13.sp)
                                Text(profile.username, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Premium Plan Status", color = Color.White, fontSize = 13.sp)
                                Text(if (profile.isPremium) "ACTIVE" else "INACTIVE", color = if (profile.isPremium) Color.Green else Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
