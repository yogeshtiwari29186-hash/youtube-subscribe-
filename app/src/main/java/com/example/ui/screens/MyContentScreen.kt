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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PromotionItem
import com.example.data.model.PromotionStatus
import com.example.data.model.PromotionType
import com.example.ui.components.CreatorTopBar
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.DiamondGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.CreatorDiamondViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.YouTubeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyContentScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val myPromotions by viewModel.myPromotions.collectAsState()
    val wallet by viewModel.walletOverview.collectAsState()
    val context = LocalContext.current

    val tabs = listOf("Active", "Pending", "Completed", "Rejected")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val filteredList = when (selectedTabIndex) {
        0 -> myPromotions.filter { it.status == PromotionStatus.ACTIVE }
        1 -> myPromotions.filter { it.status == PromotionStatus.PENDING }
        2 -> myPromotions.filter { it.status == PromotionStatus.COMPLETED }
        3 -> myPromotions.filter { it.status == PromotionStatus.REJECTED }
        else -> myPromotions
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "My Content",
            showBack = true,
            diamonds = wallet.balance,
            onBackClick = { viewModel.navigateBack() }
        )

        // Tabs Row (Active, Pending, Completed, Rejected)
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DarkSurface,
            contentColor = DiamondCyan,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = DiamondCyan,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) DiamondCyan else TextSecondary
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No ${tabs[selectedTabIndex]} promotions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Create a campaign to promote your videos or channel.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.startPromotionCreation() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DiamondCyan,
                                    contentColor = Color(0xFF0A0D14)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Promotion", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredList) { promo ->
                    MyContentCard(
                        promo = promo,
                        onOpen = { YouTubeUtils.openOfficialYouTube(context, promo.targetUrl) },
                        onDetails = { viewModel.openContentDetails(promo) }
                    )
                }
            }
        }
    }
}

@Composable
fun MyContentCard(
    promo: PromotionItem,
    onOpen: () -> Unit,
    onDetails: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(promo.createdAt))

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("my_content_card_${promo.promotionId}"),
        shape = RoundedCornerShape(16.dp),
        onClick = onDetails
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thumbnail
                AsyncImage(
                    model = promo.thumbnailUrl,
                    contentDescription = promo.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (promo.status) {
                                PromotionStatus.ACTIVE -> SuccessGreen.copy(alpha = 0.2f)
                                PromotionStatus.PENDING -> WarningAmber.copy(alpha = 0.2f)
                                PromotionStatus.COMPLETED -> DiamondCyan.copy(alpha = 0.2f)
                                else -> ErrorRed.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = promo.status.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (promo.status) {
                                    PromotionStatus.ACTIVE -> SuccessGreen
                                    PromotionStatus.PENDING -> WarningAmber
                                    PromotionStatus.COMPLETED -> DiamondCyan
                                    else -> ErrorRed
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = when (promo.type) {
                                PromotionType.YOUTUBE_VIDEO -> "YouTube Video"
                                PromotionType.YOUTUBE_CHANNEL -> "YouTube Channel"
                                PromotionType.CREATOR_PROFILE -> "Creator Profile"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = promo.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Created $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            // Metrics / Analytics Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurfaceHighlight
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Diamonds Spent", color = TextSecondary, fontSize = 10.sp)
                        Text("${promo.budget} 💎", color = DiamondCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Column {
                        Text("Remaining", color = TextSecondary, fontSize = 10.sp)
                        Text("${promo.remainingBudget} 💎", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Column {
                        Text("Impressions", color = TextSecondary, fontSize = 10.sp)
                        Text("${promo.impressions}", color = DiamondGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Surface(
                        shape = CircleShape,
                        color = DarkSurfaceVariant,
                        modifier = Modifier
                            .clickable { onOpen() }
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInNew,
                            contentDescription = "Open Link",
                            tint = DiamondCyan,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
