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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PromotionType
import com.example.ui.components.ComplianceNoticeCard
import com.example.ui.components.CreatorTopBar
import com.example.ui.components.DiamondBadge
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderGlow
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.DiamondGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CreatorDiamondViewModel
import com.example.ui.viewmodel.Screen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SelectPromotionTypeScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val wallet by viewModel.walletOverview.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Create Promotion",
            showBack = true,
            diamonds = wallet.balance,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Select Promotion Objective",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Spend your earned diamonds to amplify discovery across the creator community.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Option 1: Promote YouTube Video
            item {
                PromotionTypeOptionCard(
                    title = "Promote YouTube Video",
                    subtitle = "Feature an official video link in the discovery feed for community viewing.",
                    icon = Icons.Filled.PlayArrow,
                    accentColor = Color(0xFFFF3D71),
                    tag = "High Discovery",
                    testTag = "promo_opt_video",
                    onClick = { viewModel.setPromotionType(PromotionType.YOUTUBE_VIDEO) }
                )
            }

            // Option 2: Promote YouTube Channel
            item {
                PromotionTypeOptionCard(
                    title = "Promote YouTube Channel",
                    subtitle = "Promote your channel identity, bio, and content themes to new creators.",
                    icon = Icons.Filled.Subscriptions,
                    accentColor = Color(0xFF2979FF),
                    tag = "Channel Growth",
                    testTag = "promo_opt_channel",
                    onClick = { viewModel.setPromotionType(PromotionType.YOUTUBE_CHANNEL) }
                )
            }

            // Option 3: Promote Creator Profile
            item {
                PromotionTypeOptionCard(
                    title = "Promote Creator Profile",
                    subtitle = "Boost your CreatorDiamond community profile and showcase your portfolio.",
                    icon = Icons.Filled.AccountCircle,
                    accentColor = DiamondCyan,
                    tag = "Community Spotlight",
                    testTag = "promo_opt_profile",
                    onClick = { viewModel.setPromotionType(PromotionType.CREATOR_PROFILE) }
                )
            }

            item {
                ComplianceNoticeCard()
            }
        }
    }
}

@Composable
fun PromotionTypeOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    tag: String,
    testTag: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = tag,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun AddYouTubeVideoScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val creationState by viewModel.creationState.collectAsState()
    var url by remember { mutableStateOf("https://www.youtube.com/watch?v=a1B2c3D4e5F") }
    var title by remember { mutableStateOf("Epic Cinematic Lighting Breakdown") }
    var description by remember { mutableStateOf("Deep dive tutorial on 3-point neon lighting workflows for indie video creators.") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "YouTube Video Details",
            showBack = true,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Add YouTube Video",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Enter a valid official YouTube link. Metadata and thumbnail are verified.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            item {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("YouTube Video URL") },
                    placeholder = { Text("https://www.youtube.com/watch?v=...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_youtube_video_url"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiamondCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = DiamondCyan,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    isError = creationState.resolutionError != null
                )
            }

            if (creationState.resolutionError != null) {
                item {
                    Text(
                        text = creationState.resolutionError ?: "",
                        color = ErrorRed,
                        fontSize = 12.sp
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Promotion Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_promo_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiamondCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = DiamondCyan,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_promo_desc"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiamondCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = DiamondCyan,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                ComplianceNoticeCard()
            }

            item {
                Button(
                    onClick = {
                        viewModel.resolveYouTubeVideo(url, title, description)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("continue_to_budget_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiamondCyan,
                        contentColor = Color(0xFF0A0D14)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Validate & Set Budget ➡️", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddYouTubeChannelScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val creationState by viewModel.creationState.collectAsState()
    var url by remember { mutableStateOf("https://www.youtube.com/@AlexCreatorTech") }
    var title by remember { mutableStateOf("Alex Creator Tech Channel") }
    var description by remember { mutableStateOf("Weekly devlogs, camera gears, and cinematic storytelling.") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "YouTube Channel Details",
            showBack = true,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Add YouTube Channel",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Promote your official channel handle (e.g. @MyHandle or channel link).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            item {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Channel URL or Handle") },
                    placeholder = { Text("https://www.youtube.com/@CreatorHandle") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_channel_url"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiamondCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = DiamondCyan,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (creationState.resolutionError != null) {
                item {
                    Text(
                        text = creationState.resolutionError ?: "",
                        color = ErrorRed,
                        fontSize = 12.sp
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Promotion Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_channel_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiamondCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = DiamondCyan,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_channel_desc"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiamondCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = DiamondCyan,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                ComplianceNoticeCard()
            }

            item {
                Button(
                    onClick = {
                        viewModel.resolveYouTubeChannel(url, title, description)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("continue_channel_budget_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiamondCyan,
                        contentColor = Color(0xFF0A0D14)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Validate & Set Budget ➡️", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PromotionBudgetScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val wallet by viewModel.walletOverview.collectAsState()
    val creationState by viewModel.creationState.collectAsState()

    var budget by remember { mutableFloatStateOf(500f) }
    var durationDays by remember { mutableIntStateOf(3) }

    // Estimated reach calculation based on platform allocation formula
    // (Never guarantee views or subscribers - clear compliance disclaimer)
    val estimatedReach = (budget * 2.4).toInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Promotion Budget",
            showBack = true,
            diamonds = wallet.balance,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderGlow = true
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Available Balance", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = "${NumberFormat.getNumberInstance(Locale.US).format(wallet.balance)} 💎",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceHighlight
                        ) {
                            Text(
                                text = "Min Budget: 100 💎",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = DiamondCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Budget Selector
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Promotion Budget", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = "${budget.toInt()} 💎",
                                fontWeight = FontWeight.ExtraBold,
                                color = DiamondCyan,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = budget,
                            onValueChange = { budget = it },
                            valueRange = 100f..2000f,
                            steps = 18,
                            colors = SliderDefaults.colors(
                                thumbColor = DiamondCyan,
                                activeTrackColor = DiamondCyan,
                                inactiveTrackColor = DarkBorder
                            ),
                            modifier = Modifier.testTag("budget_slider")
                        )

                        // Quick presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(100, 250, 500, 1000).forEach { preset ->
                                OutlinedButton(
                                    onClick = { budget = preset.toFloat() },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (budget.toInt() == preset) DiamondCyan else DarkBorder
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (budget.toInt() == preset) DiamondCyan else TextSecondary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("$preset 💎", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Duration Selector
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Campaign Duration", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1, 3, 7, 14).forEach { days ->
                                Button(
                                    onClick = { durationDays = days },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (durationDays == days) DiamondCyan else DarkSurfaceHighlight,
                                        contentColor = if (durationDays == days) Color(0xFF0A0D14) else TextSecondary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("${days}d", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Estimated Reach Calculation & Compliance Disclaimer
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Estimated Community Reach",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "~$estimatedReach Impressions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = DiamondGold
                        )

                        Text(
                            text = "Estimated impression distribution within the community based on current active creators. We never guarantee or sell views or subscribers.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Next Button
            item {
                Button(
                    onClick = {
                        viewModel.setBudgetAndDuration(budget.toLong(), durationDays)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("budget_confirm_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiamondCyan,
                        contentColor = Color(0xFF0A0D14)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Review Promotion ➡️", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PromotionConfirmationScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val creationState by viewModel.creationState.collectAsState()
    val wallet by viewModel.walletOverview.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Confirm Promotion",
            showBack = true,
            diamonds = wallet.balance,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Confirmation alert card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderGlow = true
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Diamond,
                            contentDescription = null,
                            tint = DiamondCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "You are about to spend ${creationState.budget} 💎.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Diamonds will be deducted from your wallet ledger atomically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Summary Details
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Campaign Summary", fontWeight = FontWeight.Bold, color = TextPrimary)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Title", color = TextSecondary, fontSize = 12.sp)
                            Text(creationState.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Type", color = TextSecondary, fontSize = 12.sp)
                            Text(creationState.type.name, color = DiamondCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Duration", color = TextSecondary, fontSize = 12.sp)
                            Text("${creationState.durationDays} Days", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cost", color = TextSecondary, fontSize = 12.sp)
                            Text("-${creationState.budget} 💎", color = Color(0xFFFF5252), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance After", color = TextSecondary, fontSize = 12.sp)
                            Text("${wallet.balance - creationState.budget} 💎", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (creationState.submitError != null) {
                item {
                    Text(
                        text = creationState.submitError ?: "",
                        color = ErrorRed,
                        fontSize = 12.sp
                    )
                }
            }

            // Actions: CANCEL, CREATE PROMOTION (Section 7)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("promo_cancel_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text("CANCEL", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.confirmAndLaunchPromotion() },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("promo_confirm_launch_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DiamondCyan,
                            contentColor = Color(0xFF0A0D14)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !creationState.isSubmitting
                    ) {
                        if (creationState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF0A0D14),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("CREATE PROMOTION", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromotionSuccessScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val creationState by viewModel.creationState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(DiamondCyan.copy(alpha = 0.2f))
                .border(2.dp, DiamondCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = DiamondCyan,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Promotion Created Successfully",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your campaign is now live in the community discovery feed.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("PROMOTION IDENTIFIER", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    text = creationState.createdPromotionId ?: "promo_live",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = DiamondCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { viewModel.navigateTo(Screen.MyContent) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("success_view_my_content_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = DiamondCyan,
                contentColor = Color(0xFF0A0D14)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("View in My Content 🚀", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { viewModel.navigateTo(Screen.Home) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("success_back_home_btn"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Text("Back to Home", color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
