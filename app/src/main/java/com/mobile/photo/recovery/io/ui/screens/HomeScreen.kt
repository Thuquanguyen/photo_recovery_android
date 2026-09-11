package com.mobile.photo.recovery.io.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ui.components.PrimaryPillButton
import com.mobile.photo.recovery.io.ui.theme.CardSurface
import com.mobile.photo.recovery.io.ui.theme.LavenderBackground
import com.mobile.photo.recovery.io.ui.theme.NeutralDark
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.Tertiary
import com.mobile.photo.recovery.io.ui.vm.HomeViewModel
import com.mobile.photo.recovery.io.util.formatBytes

@Composable
fun HomeScreen(
    onStartScan: () -> Unit,
    onQuickClean: () -> Unit,
    onDuplicate: () -> Unit,
    onScreenshot: () -> Unit,
    onRecentlyDeleted: () -> Unit, // aliases to Photo Recovery, spec 4.4
    onVault: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LavenderBackground)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        // Header: logo + title/badge + Pro/Settings actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Primary, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeutralDark
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Tertiary.copy(alpha = 0.2f), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                stringResource(R.string.home_ai_pro_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeutralDark
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.home_deep_storage_engine),
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralDark.copy(alpha = 0.6f)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .background(Tertiary.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = NeutralDark, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.home_pro_badge),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_title), tint = NeutralDark)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Hero core recovery card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .background(LavenderBackground, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Secondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.home_secure_banner),
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralDark
                    )
                }

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Radar, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Text(
                            "SCAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    stringResource(R.string.home_ready_to_scan),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeutralDark
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.home_ready_to_scan_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralDark.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                PrimaryPillButton(
                    text = "${stringResource(R.string.home_start_scan)} • ${formatBytes(state.freeSpaceBytes)} ${stringResource(R.string.home_scan_ready_suffix)}",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onStartScan
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Quick Swipe Clean shortcut card
        Card(
            onClick = onQuickClean,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Secondary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = Secondary)
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        stringResource(R.string.home_quick_clean_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                    Text(
                        stringResource(R.string.home_quick_clean_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralDark.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            stringResource(R.string.home_section_smart_clean).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = NeutralDark.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                listOf(
                    HomeGridItem(Icons.Filled.ContentCopy, R.string.home_grid_duplicate, R.string.home_grid_duplicate_desc, Primary, onDuplicate),
                    HomeGridItem(Icons.Filled.PhotoCamera, R.string.home_grid_screenshot, R.string.home_grid_screenshot_desc, Tertiary, onScreenshot),
                    HomeGridItem(Icons.Filled.Restore, R.string.home_grid_recently_deleted, R.string.home_grid_recently_deleted_desc, Secondary, onRecentlyDeleted),
                    HomeGridItem(Icons.Filled.Lock, R.string.home_grid_vault, R.string.home_grid_vault_desc, NeutralDark, onVault)
                )
            ) { item ->
                HomeGridCard(item)
            }
        }
    }
}

private data class HomeGridItem(
    val icon: ImageVector,
    val labelRes: Int,
    val descRes: Int,
    val tint: Color,
    val onClick: () -> Unit
)

@Composable
private fun HomeGridCard(item: HomeGridItem) {
    Card(
        onClick = item.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.3f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(item.tint.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = item.tint)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(item.labelRes),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeutralDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(item.descRes),
                style = MaterialTheme.typography.bodySmall,
                color = NeutralDark.copy(alpha = 0.6f)
            )
        }
    }
}
