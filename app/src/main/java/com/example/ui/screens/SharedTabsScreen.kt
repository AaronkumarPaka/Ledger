package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.FriendEntity
import com.example.data.SplitEntity
import com.example.ui.components.AddFriendDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EditFriendDialog
import com.example.ui.components.EditSplitDialog
import com.example.ui.components.GaugeRing
import com.example.ui.components.LogSplitDialog
import com.example.ui.components.StackedBalanceBar
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightCoral
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.FriendWithBalance
import com.example.viewmodel.SharedTabsUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun SharedTabsScreen(
    state: SharedTabsUiState,
    onLogSplit: (Long, String, String, Double, Boolean) -> Unit,
    onUpdateSplit: (SplitEntity) -> Unit,
    onDeleteSplit: (Long) -> Unit,
    onAddFriend: (String, String, Long) -> Unit,
    onUpdateFriend: (FriendEntity) -> Unit = {},
    onDeleteFriend: (Long) -> Unit = {},
    onToggleSettled: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogSplitDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var editingSplit by remember { mutableStateOf<SplitEntity?>(null) }
    var splitToDelete by remember { mutableStateOf<SplitEntity?>(null) }
    var editingFriend by remember { mutableStateOf<FriendEntity?>(null) }
    var friendToDelete by remember { mutableStateOf<FriendEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("shared_tabs_scroll"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. "Where you stand" Summary Card
        item {
            WhereYouStandCard(state = state)
        }

        // 2. "Friends" List Card with Quick Actions
        item {
            FriendsCard(
                friends = state.friendsWithBalances,
                onLogSplitClick = { showLogSplitDialog = true },
                onAddFriendClick = { showAddFriendDialog = true },
                onEditFriend = { editingFriend = it },
                onDeleteFriend = { friendToDelete = it }
            )
        }

        // 3. "Biggest open balances" Section
        item {
            BiggestOpenBalancesCard(
                openBalances = state.openBalances,
                onItemClick = { item ->
                    // Open log split with friend
                    showLogSplitDialog = true
                }
            )
        }

        // 4. "Recent splits" Card
        item {
            RecentSplitsCard(
                splits = state.recentSplits,
                onLogSplitClick = { showLogSplitDialog = true },
                onToggleSettled = onToggleSettled,
                onEditSplit = { editingSplit = it },
                onDeleteSplit = { splitToDelete = it }
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showLogSplitDialog) {
        LogSplitDialog(
            friends = state.allFriends,
            onDismiss = { showLogSplitDialog = false },
            onConfirm = { friendId, friendName, title, amount, isOwedToUser ->
                onLogSplit(friendId, friendName, title, amount, isOwedToUser)
                showLogSplitDialog = false
            }
        )
    }

    if (showAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { showAddFriendDialog = false },
            onConfirm = { name, contact, colorHex ->
                onAddFriend(name, contact, colorHex)
                showAddFriendDialog = false
            }
        )
    }

    editingFriend?.let { friend ->
        EditFriendDialog(
            friend = friend,
            onDismiss = { editingFriend = null },
            onConfirm = { updated ->
                onUpdateFriend(updated)
                editingFriend = null
            },
            onDelete = {
                onDeleteFriend(friend.id)
                editingFriend = null
            }
        )
    }

    friendToDelete?.let { friend ->
        ConfirmDeleteDialog(
            title = "Delete Person?",
            message = "Are you sure you want to remove '${friend.name}'? This will also remove any active splits associated with them.",
            onDismiss = { friendToDelete = null },
            onConfirm = {
                onDeleteFriend(friend.id)
                friendToDelete = null
            }
        )
    }

    editingSplit?.let { split ->
        EditSplitDialog(
            split = split,
            friends = state.allFriends,
            onDismiss = { editingSplit = null },
            onConfirm = { updated ->
                onUpdateSplit(updated)
                editingSplit = null
            },
            onDelete = {
                onDeleteSplit(split.id)
                editingSplit = null
            }
        )
    }

    splitToDelete?.let { split ->
        ConfirmDeleteDialog(
            title = "Delete Split?",
            message = "Are you sure you want to delete '${split.title}' (₹${"%,d".format(split.amount.toInt())}) with ${split.friendName}?",
            onDismiss = { splitToDelete = null },
            onConfirm = {
                onDeleteSplit(split.id)
                splitToDelete = null
            }
        )
    }
}

@Composable
private fun WhereYouStandCard(state: SharedTabsUiState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("where_you_stand_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Where you stand",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Gauge ring showing "% settled"
                GaugeRing(
                    settledPercent = state.settledPercent,
                    ringSize = 88.dp,
                    strokeWidth = 8.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Main dynamic net balance text
                Column(modifier = Modifier.weight(1f)) {
                    val absBalance = abs(state.netBalance).toInt()
                    val signPrefix = if (state.netBalance < 0) "-₹" else "+₹"

                    Text(
                        text = "$signPrefix${"%,d".format(absBalance)}",
                        color = if (state.netBalance < 0) CoralRed else if (state.netBalance > 0) BrightBlue else TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val contextDesc = when {
                        state.netBalance < 0 -> "You owe more than is coming back to you"
                        state.netBalance > 0 -> "You are owed overall across tabs"
                        else -> "All tabs are balanced and settled"
                    }
                    Text(
                        text = contextDesc,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Breakdown badges: Coming to you (blue) and You still owe (orange/red)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Coming to you badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrightBlue.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, BrightBlue.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = BrightBlue.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CallReceived,
                                    contentDescription = null,
                                    tint = BrightBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Coming to you",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "₹${"%,d".format(state.comingToYou.toInt())}",
                                color = BrightBlue,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // You still owe badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CoralRed.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = CoralRed.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CallMade,
                                    contentDescription = null,
                                    tint = LightCoral,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "You still owe",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "₹${"%,d".format(state.youStillOwe.toInt())}",
                                color = LightCoral,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsCard(
    friends: List<FriendWithBalance>,
    onLogSplitClick: () -> Unit,
    onAddFriendClick: () -> Unit,
    onEditFriend: (FriendEntity) -> Unit,
    onDeleteFriend: (FriendEntity) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friends_list_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with action buttons: "Log a split" and "Add friend"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Friends",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onAddFriendClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_friend_action_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Add friend", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onLogSplitClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BrightBlue.copy(alpha = 0.15f),
                            contentColor = CyanAccent
                        ),
                        border = BorderStroke(1.dp, BrightBlue.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("log_split_action_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyanAccent)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Log a split", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // List of friends
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                friends.forEach { item ->
                    FriendListItem(
                        item = item,
                        onEdit = { onEditFriend(item.friend) },
                        onDelete = { onDeleteFriend(item.friend) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendListItem(
    item: FriendWithBalance,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color(item.friend.avatarColorHex)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.friend.name.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = item.friend.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.friend.contact.ifEmpty { "No contact listed" },
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Balance status badge
            val net = item.netBalance
            val absVal = abs(net).toInt()
            val (badgeText, badgeColor) = when {
                net < 0 -> "you owe ₹${"%,d".format(absVal)}" to LightCoral
                net > 0 -> "owes you ₹${"%,d".format(absVal)}" to BrightBlue
                else -> "settled" to TextMuted
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }

            // Edit button to change name/contact or fix typos
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("edit_friend_button_${item.friend.id}")
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit ${item.friend.name}",
                    tint = TextMuted,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Delete button to delete person / unknown entry
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("delete_friend_button_${item.friend.id}")
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete ${item.friend.name}",
                    tint = CoralRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun BiggestOpenBalancesCard(
    openBalances: List<com.example.ui.components.OpenBalanceItem>,
    onItemClick: (com.example.ui.components.OpenBalanceItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("biggest_open_balances_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Biggest open balances",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ranked by magnitude of unsettled split tabs",
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            StackedBalanceBar(
                items = openBalances,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
private fun RecentSplitsCard(
    splits: List<SplitEntity>,
    onLogSplitClick: () -> Unit,
    onToggleSettled: (Long, Boolean) -> Unit,
    onEditSplit: (SplitEntity) -> Unit,
    onDeleteSplit: (SplitEntity) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_splits_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recent splits",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${splits.size} transactions logged",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                OutlinedButton(
                    onClick = onLogSplitClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BrightBlue.copy(alpha = 0.15f),
                        contentColor = CyanAccent
                    ),
                    border = BorderStroke(1.dp, BrightBlue.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("log_split_button_in_card")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyanAccent)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Log split", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (splits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No split transactions logged yet", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    splits.forEach { split ->
                        SplitItemRow(
                            split = split,
                            onToggleSettled = { onToggleSettled(split.id, !split.isSettled) },
                            onEdit = { onEditSplit(split) },
                            onDelete = { onDeleteSplit(split) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitItemRow(
    split: SplitEntity,
    onToggleSettled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isOwedToUser = split.isOwedToUser
    val sign = if (isOwedToUser) "+₹" else "-₹"
    val color = if (split.isSettled) TextMuted else if (isOwedToUser) BrightBlue else LightCoral

    val dateFormatted = remember(split.timestamp) {
        val diff = System.currentTimeMillis() - split.timestamp
        val days = (diff / (1000 * 60 * 60 * 24)).toInt()
        when {
            days == 0 -> "Today"
            days == 1 -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(split.timestamp))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (split.isSettled) Color(0xFF090D15) else Color(0xFF0F172A))
            .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { onEdit() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = onToggleSettled,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("toggle_settle_${split.id}")
            ) {
                Icon(
                    imageVector = if (split.isSettled) Icons.Default.CheckCircle else Icons.Default.ReceiptLong,
                    contentDescription = if (split.isSettled) "Settled" else "Mark as settled",
                    tint = if (split.isSettled) EmeraldGreen else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = split.title,
                    color = if (split.isSettled) TextMuted else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "with ${split.friendName} • $dateFormatted",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$sign${"%,d".format(split.amount.toInt())}",
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (split.isSettled) {
                    Text(
                        text = "Settled",
                        color = EmeraldGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("edit_split_${split.id}")
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit split",
                    tint = TextSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_split_${split.id}")
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete split",
                    tint = CoralRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
