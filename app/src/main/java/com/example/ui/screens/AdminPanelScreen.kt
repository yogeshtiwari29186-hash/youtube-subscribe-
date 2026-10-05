package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.AuditLog
import com.example.data.model.EconomySettings
import com.example.data.model.PromotionItem
import com.example.data.model.PromotionStatus
import com.example.data.model.ReportItem
import com.example.data.model.UserAccount
import com.example.ui.components.CreatorTopBar
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
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
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.CreatorDiamondViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.allUsers.collectAsState()
    val promotions by viewModel.promotions.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val audits by viewModel.auditLogs.collectAsState()
    val economy by viewModel.economySettings.collectAsState()

    val tabs = listOf("Overview", "Users", "Promotions", "Audit Logs", "Economy", "Reports")
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog state for diamond adjustment
    var showAdjustDialog by remember { mutableStateOf(false) }
    var adjustTargetUser by remember { mutableStateOf<UserAccount?>(null) }
    var adjustDelta by remember { mutableStateOf("100") }
    var adjustReason by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Admin Console",
            showBack = true,
            onBackClick = { viewModel.navigateBack() }
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = DiamondCyan,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = DiamondCyan,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, name ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = name,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) DiamondCyan else TextSecondary
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> AdminOverviewTab(users, promotions, audits, reports)
            1 -> AdminUsersTab(
                users = users,
                onAdjustDiamonds = { user ->
                    adjustTargetUser = user
                    showAdjustDialog = true
                },
                onToggleSuspension = { user ->
                    viewModel.adminToggleUserSuspension(user.userId, !user.isSuspended, "Admin console security action")
                }
            )
            2 -> AdminPromotionsTab(
                promotions = promotions,
                onApprove = { promo ->
                    viewModel.adminModeratePromotion(promo.promotionId, PromotionStatus.ACTIVE, "Community standards verified")
                },
                onReject = { promo ->
                    viewModel.adminModeratePromotion(promo.promotionId, PromotionStatus.REJECTED, "Policy violation")
                }
            )
            3 -> AdminAuditLogsTab(audits)
            4 -> AdminEconomyTab(economy, onSave = { viewModel.updateEconomySettings(it) })
            5 -> AdminReportsTab(reports)
        }
    }

    // Mandatory Audit Reason Diamond Adjustment Dialog
    if (showAdjustDialog && adjustTargetUser != null) {
        val target = adjustTargetUser!!
        AlertDialog(
            onDismissRequest = { showAdjustDialog = false },
            containerColor = DarkSurfaceVariant,
            title = {
                Text("Adjust Diamonds (${target.username})", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Balance: ${target.diamonds} 💎", color = DiamondCyan, fontSize = 12.sp)
                    OutlinedTextField(
                        value = adjustDelta,
                        onValueChange = { adjustDelta = it },
                        label = { Text("Delta (+/- amount)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = adjustReason,
                        onValueChange = { adjustReason = it },
                        label = { Text("Mandatory Audit Reason") },
                        placeholder = { Text("e.g. Promotional grant, Refund, Correction") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val delta = adjustDelta.toLongOrNull() ?: 0L
                        if (adjustReason.isNotBlank()) {
                            viewModel.adminAdjustDiamonds(target.userId, delta, adjustReason)
                            showAdjustDialog = false
                            adjustReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan, contentColor = Color(0xFF0A0D14))
                ) {
                    Text("Apply & Log", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAdjustDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun AdminOverviewTab(
    users: List<UserAccount>,
    promotions: List<PromotionItem>,
    audits: List<AuditLog>,
    reports: List<ReportItem>
) {
    val totalDiamonds = users.sumOf { it.diamonds }
    val activePromos = promotions.count { it.status == PromotionStatus.ACTIVE }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard("Total Users", "${users.size}", Icons.Filled.People, DiamondCyan, Modifier.weight(1f))
                AdminStatCard("In Circulation", "${NumberFormat.getNumberInstance(Locale.US).format(totalDiamonds)} 💎", Icons.Filled.Diamond, DiamondGold, Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard("Active Campaigns", "$activePromos", Icons.Filled.Campaign, ElectricBlue, Modifier.weight(1f))
                AdminStatCard("Pending Reports", "${reports.size}", Icons.Filled.Report, ErrorRed, Modifier.weight(1f))
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Anti-Fraud & Security Telemetry", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("· Device & IP rate limiting: ACTIVE", color = SuccessGreen, fontSize = 12.sp)
                    Text("· Atomic transaction ledger: VERIFIED", color = SuccessGreen, fontSize = 12.sp)
                    Text("· YouTube incentivization filter: ENFORCED", color = DiamondCyan, fontSize = 12.sp)
                    Text("· Duplicate task claim blocker: ACTIVE", color = SuccessGreen, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = TextSecondary, fontSize = 11.sp)
            Text(text = value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun AdminUsersTab(
    users: List<UserAccount>,
    onAdjustDiamonds: (UserAccount) -> Unit,
    onToggleSuspension: (UserAccount) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(users) { user ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(user.username, fontWeight = FontWeight.Bold, color = TextPrimary)
                            if (user.isSuspended) {
                                Surface(shape = RoundedCornerShape(4.dp), color = ErrorRed) {
                                    Text("SUSPENDED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                                }
                            }
                        }
                        Text("${user.diamonds} 💎 · ${user.email}", color = DiamondCyan, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onAdjustDiamonds(user) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight, contentColor = DiamondCyan),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("± 💎", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onToggleSuspension(user) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (user.isSuspended) SuccessGreen else ErrorRed),
                        border = BorderStroke(1.dp, if (user.isSuspended) SuccessGreen else ErrorRed),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (user.isSuspended) "Restore" else "Suspend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPromotionsTab(
    promotions: List<PromotionItem>,
    onApprove: (PromotionItem) -> Unit,
    onReject: (PromotionItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(promotions) { promo ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(promo.title, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                        Text(promo.status.name, color = DiamondCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Budget: ${promo.budget} 💎 · Creator: ${promo.creatorName}", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onApprove(promo) },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onReject(promo) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = BorderStroke(1.dp, ErrorRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAuditLogsTab(audits: List<AuditLog>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (audits.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No audit logs recorded yet.", color = TextSecondary)
                    }
                }
            }
        } else {
            items(audits) { log ->
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(log.timestamp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.action, fontWeight = FontWeight.Bold, color = DiamondGold, fontSize = 12.sp)
                            Text(dateStr, color = TextMuted, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Delta: ${log.diamondDelta} 💎 · Target: ${log.targetUserId}", color = DiamondCyan, fontSize = 11.sp)
                        Text("Reason: ${log.reason}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminEconomyTab(
    currentSettings: EconomySettings,
    onSave: (EconomySettings) -> Unit
) {
    var dailyLimit by remember { mutableStateOf(currentSettings.dailyLimit.toString()) }
    var minBudget by remember { mutableStateOf(currentSettings.minimumPromotionBudget.toString()) }
    var referralReward by remember { mutableStateOf(currentSettings.referralReward.toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Dynamic Diamond Economy Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("All economic parameters are hot-reconfigurable on the backend.", color = TextSecondary, fontSize = 12.sp)
        }

        item {
            OutlinedTextField(
                value = dailyLimit,
                onValueChange = { dailyLimit = it },
                label = { Text("Daily Earn Limit (💎)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
        }

        item {
            OutlinedTextField(
                value = minBudget,
                onValueChange = { minBudget = it },
                label = { Text("Minimum Promotion Budget (💎)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
        }

        item {
            OutlinedTextField(
                value = referralReward,
                onValueChange = { referralReward = it },
                label = { Text("Referral Bonus Reward (💎)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
        }

        item {
            Button(
                onClick = {
                    val settings = EconomySettings(
                        dailyLimit = dailyLimit.toLongOrNull() ?: 500L,
                        minimumPromotionBudget = minBudget.toLongOrNull() ?: 100L,
                        referralReward = referralReward.toLongOrNull() ?: 30L
                    )
                    onSave(settings)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan, contentColor = Color(0xFF0A0D14)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Configuration", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminReportsTab(reports: List<ReportItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (reports.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No reports pending review. Community is clean! ✨", color = TextSecondary)
                    }
                }
            }
        } else {
            items(reports) { rep ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(rep.reason, fontWeight = FontWeight.Bold, color = ErrorRed)
                            Text(rep.status, color = WarningAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Target: ${rep.targetTitle}", color = TextPrimary, fontSize = 12.sp)
                        Text("Details: ${rep.details}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
