package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GlassBottomNav
import com.example.ui.components.GlassTopBar
import com.example.ui.components.MeshBackground
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProductivityScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.NoxaEuroTheme
import com.example.viewmodel.NoxaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoxaEuroTheme(darkTheme = true) {
                NoxaEuroApp()
            }
        }
    }
}

@Composable
fun NoxaEuroApp(
    viewModel: NoxaViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    MeshBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(modifier = Modifier.statusBarsPadding()) {
                    GlassTopBar(
                        onProfileClick = { viewModel.selectTab(3) }
                    )
                }
            },
            bottomBar = {
                GlassBottomNav(
                    selectedTab = selectedTab,
                    onTabSelected = { tab -> viewModel.selectTab(tab) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(
                    targetState = selectedTab,
                    animationSpec = tween(durationMillis = 220),
                    label = "ScreenTransition"
                ) { tab ->
                    when (tab) {
                        0 -> DashboardScreen(viewModel = viewModel)
                        1 -> ChatScreen(viewModel = viewModel)
                        2 -> ProductivityScreen(viewModel = viewModel)
                        3 -> ProfileScreen(viewModel = viewModel)
                        else -> DashboardScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
