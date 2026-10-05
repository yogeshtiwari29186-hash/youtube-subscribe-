package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PromotionType
import com.example.ui.components.CreatorTopBar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CreatorDiamondViewModel
import com.example.util.YouTubeUtils

@Composable
fun ContentFeedScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val promotions by viewModel.promotions.collectAsState()
    val wallet by viewModel.walletOverview.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf<PromotionType?>(null) }

    val filteredList = promotions.filter { promo ->
        val matchesQuery = promo.title.contains(searchQuery, ignoreCase = true) ||
                promo.creatorName.contains(searchQuery, ignoreCase = true) ||
                promo.description.contains(searchQuery, ignoreCase = true)
        val matchesType = filterType == null || promo.type == filterType
        matchesQuery && matchesType
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Content Discovery Feed",
            showBack = true,
            diamonds = wallet.balance,
            onBackClick = { viewModel.navigateBack() }
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search creator videos & channels...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = DiamondCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("feed_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DiamondCyan,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Type filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == null,
                onClick = { filterType = null },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DiamondCyan,
                    selectedLabelColor = Color(0xFF0A0D14),
                    containerColor = DarkSurfaceVariant,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = filterType == PromotionType.YOUTUBE_VIDEO,
                onClick = { filterType = PromotionType.YOUTUBE_VIDEO },
                label = { Text("Videos") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DiamondCyan,
                    selectedLabelColor = Color(0xFF0A0D14),
                    containerColor = DarkSurfaceVariant,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = filterType == PromotionType.YOUTUBE_CHANNEL,
                onClick = { filterType = PromotionType.YOUTUBE_CHANNEL },
                label = { Text("Channels") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DiamondCyan,
                    selectedLabelColor = Color(0xFF0A0D14),
                    containerColor = DarkSurfaceVariant,
                    labelColor = TextSecondary
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredList) { promo ->
                DiscoverContentCard(
                    promo = promo,
                    onOpenDetails = { viewModel.openContentDetails(promo) },
                    onOpenExternal = { YouTubeUtils.openOfficialYouTube(context, promo.targetUrl) }
                )
            }
        }
    }
}
