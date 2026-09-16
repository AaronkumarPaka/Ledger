package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MyMoneyScreen
import com.example.ui.screens.SavingsCoachDrawer
import com.example.ui.screens.SharedTabsScreen
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LedgerLinkTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.LedgerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            LedgerLinkTheme(darkTheme = isDarkMode) {
                LedgerLinkApp(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() }
                )
            }
        }
    }
}

@Composable
fun LedgerLinkApp(
    viewModel: LedgerViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val sharedTabsState by viewModel.sharedTabsState.collectAsStateWithLifecycle()
    val myMoneyState by viewModel.myMoneyState.collectAsStateWithLifecycle()
    val isOnlineCoachMode by viewModel.isOnlineCoachMode.collectAsStateWithLifecycle()
    val coachMessages by viewModel.coachMessages.collectAsStateWithLifecycle()
    val isCoachThinking by viewModel.isCoachThinking.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBrandHeader(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) },
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onOpenSettings = { showSettingsDialog = true }
            )
        },
        bottomBar = {
            SavingsCoachDrawer(
                isOnlineMode = isOnlineCoachMode,
                messages = coachMessages,
                isThinking = isCoachThinking,
                onToggleOnlineMode = { viewModel.toggleCoachOnlineMode() },
                onAsk = { viewModel.askCoach(it) },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "MainTabsTransition"
            ) { tab ->
                when (tab) {
                    0 -> SharedTabsScreen(
                        state = sharedTabsState,
                        onLogSplit = { friendId, friendName, title, amount, isOwedToUser ->
                            viewModel.logSplit(friendId, friendName, title, amount, isOwedToUser)
                        },
                        onUpdateSplit = { updated ->
                            viewModel.updateSplit(updated)
                        },
                        onDeleteSplit = { splitId ->
                            viewModel.deleteSplit(splitId)
                        },
                        onAddFriend = { name, contact, colorHex ->
                            viewModel.addFriend(name, contact, colorHex)
                        },
                        onUpdateFriend = { updated ->
                            viewModel.updateFriend(updated)
                        },
                        onDeleteFriend = { friendId ->
                            viewModel.deleteFriend(friendId)
                        },
                        onToggleSettled = { splitId, settled ->
                            viewModel.toggleSplitSettled(splitId, settled)
                        }
                    )
                    1 -> MyMoneyScreen(
                        state = myMoneyState,
                        onSetIncome = { newIncome ->
                            viewModel.setMonthlyIncome(newIncome)
                        },
                        onLogExpense = { title, amount, category, notes ->
                            viewModel.logExpense(title, amount, category, notes)
                        },
                        onUpdateExpense = { updated ->
                            viewModel.updateExpense(updated)
                        },
                        onDeleteExpense = { id ->
                            viewModel.deleteExpense(id)
                        }
                    )
                }
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = CardSurface,
            title = {
                Text(
                    text = "LedgerLink Settings",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Currency: Indian Rupee (₹)",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• Storage: Offline-first local Room Database",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• Coach: Built-in data-driven heuristic analysis",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0 • Built with Jetpack Compose & Material 3",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Done", color = BrightBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun TopBrandHeader(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Brand row + Quick actions (Settings & Theme toggle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Modern LedgerLink icon badge
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = BrightBlue
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "L",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LedgerLink",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Shared tabs and your month, in one place",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Quick actions: Settings & Theme icon toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented top toggle switch:
        // 1. "Shared tabs" (Split expense management)
        // 2. "My money" (Personal monthly budget & projections)
        SegmentedTabs(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )
    }
}

@Composable
private fun SegmentedTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tab 1: Shared tabs
            val isTab0Selected = selectedTab == 0
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isTab0Selected) BrightBlue else Color.Transparent)
                    .clickable { onTabSelected(0) }
                    .padding(vertical = 6.dp)
                    .testTag("tab_shared_tabs"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = if (isTab0Selected) Color.White else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Shared tabs",
                        color = if (isTab0Selected) Color.White else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isTab0Selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Tab 2: My money
            val isTab1Selected = selectedTab == 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isTab1Selected) BrightBlue else Color.Transparent)
                    .clickable { onTabSelected(1) }
                    .padding(vertical = 6.dp)
                    .testTag("tab_my_money"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = if (isTab1Selected) Color.White else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "My money",
                        color = if (isTab1Selected) Color.White else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isTab1Selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
