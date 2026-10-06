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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EarnTask
import com.example.ui.components.ComplianceNoticeCard
import com.example.ui.components.CreatorTopBar
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderGlow
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.DiamondGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CreatorDiamondViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun EarnScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val wallet by viewModel.walletOverview.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifs = notifications.count { !it.isRead }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Earn Diamonds",
            showBack = false,
            diamonds = wallet.balance,
            unreadNotifications = unreadNotifs,
            avatarUrl = user?.profileImage.orEmpty(),
            onDiamondClick = { viewModel.navigateTo(Screen.Wallet) },
            onNotificationClick = { viewModel.navigateTo(Screen.Notifications) },
            onAvatarClick = { viewModel.navigateTo(Screen.CreatorProfile) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Compliance policy notice
            item {
                ComplianceNoticeCard()
            }

            // Section Header
            item {
                Text(
                    text = "In-App Activities & Quests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Tasks List
            items(tasks.filterNot { it.isDaily }) { task ->
                val isCompleted = task.isCompleted

                TaskCard(
                    task = task,
                    isCompleted = isCompleted,
                    onOpenDetails = { viewModel.openTaskDetails(task) },
                    onClaim = { viewModel.claimTaskReward(task.taskId) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

@Composable
fun TaskCard(
    task: EarnTask,
    isCompleted: Boolean,
    onOpenDetails: () -> Unit,
    onClaim: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.taskId}"),
        shape = RoundedCornerShape(16.dp),
        onClick = onOpenDetails
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Task Category Icon Box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) DarkSurfaceHighlight else DiamondCyan.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (isCompleted) DarkBorder else DiamondCyan.copy(alpha = 0.4f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (task.actionType) {
                        "DAILY_CHECKIN" -> Icons.Filled.FlashOn
                        "PROFILE_COMPLETE" -> Icons.Filled.Star
                        "COMMUNITY_QUIZ" -> Icons.Filled.Quiz
                        "LINK_YOUTUBE" -> Icons.Filled.PlayArrow
                        "APP_TOUR" -> Icons.Filled.Info
                        "REFERRAL_CODE" -> Icons.Filled.Share
                        else -> Icons.Filled.CardGiftcard
                    },
                    contentDescription = null,
                    tint = if (isCompleted) TextMuted else DiamondCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Title & Description
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) TextSecondary else TextPrimary
                    )
                    if (task.isDaily) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DarkSurfaceHighlight
                        ) {
                            Text(
                                text = "DAILY",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DiamondCyan,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+${task.reward} 💎",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCompleted) TextMuted else DiamondCyan
                )
            }

            // Action or Status
            if (isCompleted) {
                Surface(
                    shape = CircleShape,
                    color = SuccessGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SuccessGreen)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = SuccessGreen,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp)
                    )
                }
            } else {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiamondCyan,
                        contentColor = Color(0xFF0A0D14)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("task_start_button_${task.taskId}")
                ) {
                    Text(
                        text = "Claim",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDetailsScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val task = viewModel.selectedTask.collectAsState().value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Task Details",
            showBack = true,
            onBackClick = { viewModel.navigateBack() }
        )

        if (task == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No task selected.", color = TextSecondary)
            }
            return
        }

        val isCompleted = task.isCompleted

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderGlow = true
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DiamondCyan.copy(alpha = 0.2f))
                                .border(2.dp, DiamondCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Diamond,
                                contentDescription = null,
                                tint = DiamondCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "+${task.reward} 💎 Reward",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = DiamondCyan
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            // Task ID and Rule details
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Activity Parameters",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Task Identifier", color = TextSecondary, fontSize = 12.sp)
                            Text(task.taskId, color = DiamondCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Reward Frequency", color = TextSecondary, fontSize = 12.sp)
                            Text(if (task.isDaily) "Once every 24 Hours" else "One-Time Activity", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Verification Mechanism", color = TextSecondary, fontSize = 12.sp)
                            Text("Atomic Server Validation", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Status", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                if (isCompleted) "Completed ✅" else "Ready to Claim 💎",
                                color = if (isCompleted) SuccessGreen else DiamondCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // YouTube Non-Incentivization Guarantee
            item {
                ComplianceNoticeCard()
            }

            // Button
            item {
                if (isCompleted) {
                    OutlinedButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SuccessGreen)
                    ) {
                        Text("Task Completed - Back", color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.claimTaskReward(task.taskId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("task_details_claim_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DiamondCyan,
                            contentColor = Color(0xFF0A0D14)
                        )
                    ) {
                        Text("Claim +${task.reward} Diamonds 💎", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
