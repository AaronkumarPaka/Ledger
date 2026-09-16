package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ExpenseEntity
import com.example.data.FriendEntity
import com.example.data.SplitEntity
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryFun
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryRent
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryTravel
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LogSplitDialog(
    friends: List<FriendEntity>,
    onDismiss: () -> Unit,
    onConfirm: (friendId: Long, friendName: String, title: String, amount: Double, isOwedToUser: Boolean) -> Unit
) {
    var selectedFriend by remember { mutableStateOf(friends.firstOrNull()) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    // Split mode: true = Split Equally (50/50 between 2 people), false = Exact share / Direct amount
    var isSplitEqually by remember { mutableStateOf(true) }
    // Who paid the bill: true = "I paid" (so friend owes their share), false = "Friend paid" (so I owe friend my share)
    var isUserPaid by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf("") }

    val friendNameShort = selectedFriend?.name?.substringBefore(" ") ?: "Friend"
    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val friendShare = if (isSplitEqually) parsedAmount / 2.0 else parsedAmount
    val userShare = if (isSplitEqually) parsedAmount / 2.0 else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log a split",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Friend selector
                Text(
                    text = "Split with",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(friends) { friend ->
                        val isSelected = selectedFriend?.id == friend.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) BrightBlue.copy(alpha = 0.2f) else Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) BrightBlue else CardBorder
                            ),
                            modifier = Modifier
                                .width(72.dp)
                                .clickable { selectedFriend = friend }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = CircleShape,
                                    color = Color(friend.avatarColorHex)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = friend.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = friend.name.substringBefore(" "),
                                    color = if (isSelected) TextPrimary else TextMuted,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. Dinner, Uber)", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("split_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Split Mode: Split equally 50/50 vs Exact share
                Text("Split Method", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSplitEqually) BrightBlue.copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(1.dp, if (isSplitEqually) BrightBlue else CardBorder, RoundedCornerShape(8.dp))
                            .clickable { isSplitEqually = true }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Split 50 / 50",
                            color = if (isSplitEqually) BrightBlue else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isSplitEqually) CyanAccent.copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(1.dp, if (!isSplitEqually) CyanAccent else CardBorder, RoundedCornerShape(8.dp))
                            .clickable { isSplitEqually = false }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Exact Share",
                            color = if (!isSplitEqually) CyanAccent else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount input in INR
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = {
                        Text(
                            if (isSplitEqually) "Total Bill Amount (₹)" else "Amount to Split (₹)",
                            color = TextSecondary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Text("₹", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("split_amount_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Who paid for it
                Text("Who paid the bill?", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Paid by user -> Friend owes user
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isUserPaid) BrightBlue.copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(1.dp, if (isUserPaid) BrightBlue else CardBorder, RoundedCornerShape(8.dp))
                            .clickable { isUserPaid = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Paid by you",
                                color = if (isUserPaid) BrightBlue else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$friendNameShort owes",
                                color = if (isUserPaid) BrightBlue.copy(alpha = 0.8f) else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Paid by friend -> User owes friend
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isUserPaid) CoralRed.copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(1.dp, if (!isUserPaid) CoralRed else CardBorder, RoundedCornerShape(8.dp))
                            .clickable { isUserPaid = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Paid by $friendNameShort",
                                color = if (!isUserPaid) CoralRed else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "You owe",
                                color = if (!isUserPaid) CoralRed.copy(alpha = 0.8f) else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Visual split breakdown card
                if (parsedAmount > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0A0F1D),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isSplitEqually) "Split equally (2 people)" else "Exact share",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (isSplitEqually) "Total: ₹${"%,.0f".format(parsedAmount)}" else "",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column {
                                        Text("Your share", color = TextSecondary, fontSize = 11.sp)
                                        Text(
                                            "₹${"%,.0f".format(if (isSplitEqually) userShare else (if (isUserPaid) parsedAmount else 0.0))}",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Column {
                                        Text("$friendNameShort's share", color = TextSecondary, fontSize = 11.sp)
                                        Text(
                                            "₹${"%,.0f".format(friendShare)}",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isUserPaid) BrightBlue.copy(alpha = 0.15f) else CoralRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isUserPaid) "$friendNameShort owes you ₹${"%,.0f".format(friendShare)}" else "You owe $friendNameShort ₹${"%,.0f".format(friendShare)}",
                                        color = if (isUserPaid) BrightBlue else CoralRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText, color = CoralRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        val friend = selectedFriend
                        if (title.isBlank()) {
                            errorText = "Please enter an expense title"
                        } else if (amount == null || amount <= 0) {
                            errorText = "Please enter a valid amount"
                        } else if (friend == null) {
                            errorText = "Please select a friend"
                        } else {
                            val finalSplitAmount = if (isSplitEqually) amount / 2.0 else amount
                            onConfirm(friend.id, friend.name, title.trim(), finalSplitAmount, isUserPaid)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("split_confirm_button")
                ) {
                    val amount = amountText.toDoubleOrNull()
                    val buttonShare = if (amount != null && amount > 0) {
                        if (isSplitEqually) " (₹${"%,.0f".format(amount / 2.0)})" else " (₹${"%,.0f".format(amount)})"
                    } else ""
                    Text("Save Split$buttonShare", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, contact: String, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    val colors = listOf(0xFF3B82F6, 0xFFEC4899, 0xFF10B981, 0xFFF59E0B, 0xFF8B5CF6, 0xFF06B6D4)
    var selectedColor by remember { mutableStateOf(colors.first()) }
    var errorText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add friend",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Friend's Name", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friend_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Phone or Email", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Pick Avatar Accent", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.forEach { hex ->
                        val isSelected = selectedColor == hex
                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { selectedColor = hex },
                            shape = CircleShape,
                            color = Color(hex),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color.White) else null
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText, color = CoralRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorText = "Please enter a name"
                        } else {
                            onConfirm(name.trim(), contact.trim(), selectedColor)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_friend_confirm_button")
                ) {
                    Text("Add Friend", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EditFriendDialog(
    friend: FriendEntity,
    onDismiss: () -> Unit,
    onConfirm: (FriendEntity) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(friend.name) }
    var contact by remember { mutableStateOf(friend.contact) }
    val colors = listOf(0xFF3B82F6, 0xFFEC4899, 0xFF10B981, 0xFFF59E0B, 0xFF8B5CF6, 0xFF06B6D4)
    var selectedColor by remember { mutableStateOf(friend.avatarColorHex) }
    var errorText by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit person",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Person's Name (e.g. fix typo)", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_friend_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Phone or Email", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Pick Avatar Accent", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.forEach { hex ->
                        val isSelected = selectedColor == hex
                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { selectedColor = hex },
                            shape = CircleShape,
                            color = Color(hex),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color.White) else null
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText, color = CoralRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions: Delete person / Save changes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("delete_friend_modal_button")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = CoralRed
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorText = "Please enter a name"
                            } else {
                                onConfirm(
                                    friend.copy(
                                        name = name.trim(),
                                        contact = contact.trim(),
                                        avatarColorHex = selectedColor
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("save_friend_edit_button")
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Delete Person?",
            message = "Are you sure you want to remove '${friend.name}'? This will also clear any splits associated with them.",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            }
        )
    }
}

@Composable
fun SetIncomeDialog(
    currentIncome: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var incomeText by remember { mutableStateOf(currentIncome.toInt().toString()) }
    var errorText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set monthly income",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This sets your baseline for monthly pacing and savings projections.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = incomeText,
                    onValueChange = { incomeText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Monthly Income (₹)", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Text("₹", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_input")
                )

                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorText, color = CoralRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = incomeText.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            errorText = "Please enter a valid positive income"
                        } else {
                            onConfirm(amount)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("set_income_confirm_button")
                ) {
                    Text("Save Income", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LogExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, category: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val categories = listOf("Food", "Travel", "Rent", "Study", "Fun", "Other")
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    val categoryColors = mapOf(
        "Food" to CategoryFood,
        "Travel" to CategoryTravel,
        "Rent" to CategoryRent,
        "Study" to CategoryStudy,
        "Fun" to CategoryFun,
        "Other" to CategoryOther
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log expense",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Grocery run, Metro)", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount (₹)", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Text("₹", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Category", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.take(3).forEach { cat ->
                        CategoryChip(
                            name = cat,
                            color = categoryColors[cat] ?: CategoryOther,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.drop(3).forEach { cat ->
                        CategoryChip(
                            name = cat,
                            color = categoryColors[cat] ?: CategoryOther,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText, color = CoralRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (title.isBlank()) {
                            errorText = "Please enter expense title"
                        } else if (amount == null || amount <= 0) {
                            errorText = "Please enter a valid amount"
                        } else {
                            onConfirm(title.trim(), amount, selectedCategory, notes.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_confirm_button")
                ) {
                    Text("Save Expense", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    name: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else CardBorder),
        modifier = modifier
            .padding(horizontal = 3.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(6.dp),
                shape = CircleShape,
                color = color
            ) {}
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = name,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun EditExpenseDialog(
    expense: ExpenseEntity,
    onDismiss: () -> Unit,
    onConfirm: (ExpenseEntity) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(expense.title) }
    var amountText by remember {
        mutableStateOf(
            if (expense.amount % 1.0 == 0.0) expense.amount.toLong().toString() else expense.amount.toString()
        )
    }
    val categories = listOf("Food", "Travel", "Rent", "Study", "Fun", "Other")
    var selectedCategory by remember {
        mutableStateOf(
            categories.find { it.equals(expense.category, ignoreCase = true) } ?: categories.first()
        )
    }
    var notes by remember { mutableStateOf(expense.notes) }
    var errorText by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val categoryColors = mapOf(
        "Food" to CategoryFood,
        "Travel" to CategoryTravel,
        "Rent" to CategoryRent,
        "Study" to CategoryStudy,
        "Fun" to CategoryFun,
        "Other" to CategoryOther
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit expense",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Grocery run, Metro)", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_expense_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount (₹)", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Text("₹", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_expense_amount_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Category", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.take(3).forEach { cat ->
                        CategoryChip(
                            name = cat,
                            color = categoryColors[cat] ?: CategoryOther,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.drop(3).forEach { cat ->
                        CategoryChip(
                            name = cat,
                            color = categoryColors[cat] ?: CategoryOther,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_expense_notes_input")
                )

                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText, color = CoralRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("delete_expense_button")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (title.isBlank()) {
                                errorText = "Please enter an expense title"
                            } else if (amount == null || amount <= 0) {
                                errorText = "Please enter a valid amount"
                            } else {
                                onConfirm(
                                    expense.copy(
                                        title = title.trim(),
                                        amount = amount,
                                        category = selectedCategory,
                                        notes = notes.trim()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("save_expense_edit_button")
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Delete Expense?",
            message = "Are you sure you want to delete '${expense.title}' (₹${expense.amount.toInt()})?",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            }
        )
    }
}

@Composable
fun EditSplitDialog(
    split: SplitEntity,
    friends: List<FriendEntity>,
    onDismiss: () -> Unit,
    onConfirm: (SplitEntity) -> Unit,
    onDelete: () -> Unit
) {
    var selectedFriend by remember {
        mutableStateOf(friends.find { it.id == split.friendId } ?: friends.firstOrNull())
    }
    var title by remember { mutableStateOf(split.title) }
    var amountText by remember {
        mutableStateOf(
            if (split.amount % 1.0 == 0.0) split.amount.toLong().toString() else split.amount.toString()
        )
    }
    // Split mode: true = Split 50/50 (entered amount is total bill), false = Exact share (entered amount is exact split)
    var isSplitEqually by remember { mutableStateOf(false) }
    var isUserPaid by remember { mutableStateOf(split.isOwedToUser) }
    var isSettled by remember { mutableStateOf(split.isSettled) }
    var errorText by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val friendNameShort = selectedFriend?.name?.substringBefore(" ") ?: "Friend"
    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val friendShare = if (isSplitEqually) parsedAmount / 2.0 else parsedAmount
    val userShare = if (isSplitEqually) parsedAmount / 2.0 else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit split",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Friend selector
                Text(
                    text = "Split with",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(friends) { friend ->
                        val isSelected = selectedFriend?.id == friend.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) BrightBlue.copy(alpha = 0.2f) else Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) BrightBlue else CardBorder
                            ),
                            modifier = Modifier
                                .width(72.dp)
                                .clickable { selectedFriend = friend }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = CircleShape,
                                    color = Color(friend.avatarColorHex)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = friend.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = friend.name.substringBefore(" "),
                                    color = if (isSelected) TextPrimary else TextMuted,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. Dinner, Uber)", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_split_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Split Mode: Split equally 50/50 vs Exact share
                Text("Split Method", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSplitEqually) BrightBlue.copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(1.dp, if (isSplitEqually) BrightBlue else CardBorder, RoundedCornerShape(8.dp))
                            .clickable { isSplitEqually = true }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Split 50 / 50",
                            color = if (isSplitEqually) BrightBlue else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isSplitEqually) CyanAccent.copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(1.dp, if (!isSplitEqually) CyanAccent else CardBorder, RoundedCornerShape(8.dp))
                            .clickable { isSplitEqually = false }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Exact Share",
                            color = if (!isSplitEqually) CyanAccent else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = {
                        Text(
                            if (isSplitEqually) "Total Bill Amount (₹)" else "Amount to Split (₹)",
                            color = TextSecondary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Text("₹", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrightBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_split_amount_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Who paid for it
                Text("Who paid the bill?", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isUserPaid) BrightBlue.copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(1.dp, if (isUserPaid) BrightBlue else CardBorder, RoundedCornerShape(8.dp))
                            .clickable { isUserPaid = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Paid by you",
                                color = if (isUserPaid) BrightBlue else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$friendNameShort owes",
                                color = if (isUserPaid) BrightBlue.copy(alpha = 0.8f) else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isUserPaid) CoralRed.copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(1.dp, if (!isUserPaid) CoralRed else CardBorder, RoundedCornerShape(8.dp))
                            .clickable { isUserPaid = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Paid by $friendNameShort",
                                color = if (!isUserPaid) CoralRed else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "You owe",
                                color = if (!isUserPaid) CoralRed.copy(alpha = 0.8f) else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Visual split breakdown card
                if (parsedAmount > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0A0F1D),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isSplitEqually) "Split equally (2 people)" else "Exact share",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (isSplitEqually) "Total: ₹${"%,.0f".format(parsedAmount)}" else "",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column {
                                        Text("Your share", color = TextSecondary, fontSize = 11.sp)
                                        Text(
                                            "₹${"%,.0f".format(if (isSplitEqually) userShare else (if (isUserPaid) parsedAmount else 0.0))}",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Column {
                                        Text("$friendNameShort's share", color = TextSecondary, fontSize = 11.sp)
                                        Text(
                                            "₹${"%,.0f".format(friendShare)}",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isUserPaid) BrightBlue.copy(alpha = 0.15f) else CoralRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isUserPaid) "$friendNameShort owes you ₹${"%,.0f".format(friendShare)}" else "You owe $friendNameShort ₹${"%,.0f".format(friendShare)}",
                                        color = if (isUserPaid) BrightBlue else CoralRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Settlement status
                Text("Settlement Status", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isSettled) Color(0xFF1E293B) else Color(0xFF0F172A))
                            .border(1.dp, if (!isSettled) CyanAccent else CardBorder, RoundedCornerShape(8.dp))
                            .clickable { isSettled = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pending",
                            color = if (!isSettled) CyanAccent else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSettled) EmeraldGreen.copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(1.dp, if (isSettled) EmeraldGreen else CardBorder, RoundedCornerShape(8.dp))
                            .clickable { isSettled = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Settled",
                            color = if (isSettled) EmeraldGreen else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText, color = CoralRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("delete_split_button")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            val friend = selectedFriend
                            if (friend == null) {
                                errorText = "Please select a friend"
                            } else if (title.isBlank()) {
                                errorText = "Please enter an expense title"
                            } else if (amount == null || amount <= 0) {
                                errorText = "Please enter a valid amount"
                            } else {
                                val finalSplitAmount = if (isSplitEqually) amount / 2.0 else amount
                                onConfirm(
                                    split.copy(
                                        friendId = friend.id,
                                        friendName = friend.name,
                                        title = title.trim(),
                                        amount = finalSplitAmount,
                                        isOwedToUser = isUserPaid,
                                        isSettled = isSettled
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("save_split_edit_button")
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Delete Split?",
            message = "Are you sure you want to delete '${split.title}' (₹${split.amount.toInt()}) with ${split.friendName}?",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            }
        )
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = CardSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = CoralRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Text(
                text = message,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
