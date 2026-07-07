package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CreatorHubViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CreatorHubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: CreatorHubViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    var showNotificationDrawer by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Set up standard navigation bottom items
    val bottomNavItems = listOf(
        NavigationItem("dashboard", "Dashboard", Icons.Rounded.Dashboard),
        NavigationItem("chat_assistant", "AI Chat", Icons.Rounded.ChatBubble),
        NavigationItem("prompt_library", "Library", Icons.Rounded.Bookmark),
        NavigationItem("profile", "Profile", Icons.Rounded.Person),
        NavigationItem("settings", "Settings", Icons.Rounded.Settings)
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBg),
        topBar = {
            // Render Top App Bar only if on standard dashboard views to maximize visual density
            if (currentRoute == "dashboard" || currentRoute == "profile" || currentRoute == "settings") {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Glowing Dual Star app icon
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Brush.horizontalGradient(listOf(NeonPurple, NeonCyan)),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "AI Creator Hub",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    },
                    actions = {
                        // Premium status badge
                        Surface(
                            color = if (profile.isPremium) NeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (profile.isPremium) NeonCyan else Color.White.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    viewModel.togglePremium()
                                    Toast.makeText(
                                        context,
                                        if (!profile.isPremium) "Premium Mode Activated!" else "Returned to Free Tier",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = if (profile.isPremium) NeonCyan else SoftGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (profile.isPremium) "PRO" else "FREE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Notifications indicator with badge
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            IconButton(onClick = { showNotificationDrawer = !showNotificationDrawer }) {
                                Icon(Icons.Rounded.Notifications, contentDescription = "Alerts", tint = Color.White)
                            }
                            if (notifications.isNotEmpty()) {
                                Surface(
                                    color = NeonPink,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                ) {}
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicBg)
                )
            }
        },
        bottomBar = {
            // Adaptive Bottom Bar: Show only when on home bottom destinations
            val shouldShowBottomBar = bottomNavItems.any { it.route == currentRoute }
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = CosmicSlate,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) NeonCyan else Color.Gray
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NeonPurple.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CosmicBg)
        ) {
            // Main Navigation Router
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("dashboard") {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTool = { route -> navController.navigate(route) },
                        onShowNotifications = { showNotificationDrawer = true }
                    )
                }
                composable("image_gen") {
                    ImageGeneratorScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("video_gen") {
                    VideoGeneratorScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("music_gen") {
                    MusicGeneratorScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("prompt_library") {
                    PromptLibraryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("chat_assistant") {
                    ChatAssistantScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("voice_tools") {
                    VoiceToolsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("photo_editor") {
                    PhotoEditorScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("logo_poster") {
                    LogoPosterScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("social_media") {
                    SocialMediaScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("profile") {
                    UserProfileScreen(
                        viewModel = viewModel,
                        onBack = { navController.navigate("dashboard") },
                        onNavigateToAdmin = { navController.navigate("admin") }
                    )
                }
                composable("settings") {
                    SettingsScreen(viewModel = viewModel, onBack = { navController.navigate("dashboard") })
                }
                composable("admin") {
                    AdminPanelScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
            }

            // Notification Center slide-down overlay
            if (showNotificationDrawer) {
                Surface(
                    color = CosmicSlate.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.TopCenter)
                        .padding(bottom = 40.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = SoftGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Notification Center", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            IconButton(onClick = { showNotificationDrawer = false }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        notifications.forEach { note ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    note,
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
