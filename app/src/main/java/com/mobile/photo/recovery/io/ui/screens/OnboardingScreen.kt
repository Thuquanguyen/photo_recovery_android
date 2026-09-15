package com.mobile.photo.recovery.io.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appadskit.AdPlacement
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ads.InterstitialAdHelper
import com.mobile.photo.recovery.io.ads.NativeAdLayout
import com.mobile.photo.recovery.io.ads.NativeAdSlot
import com.mobile.photo.recovery.io.ads.rememberHostActivity
import com.mobile.photo.recovery.io.ads.showInterstitial
import com.mobile.photo.recovery.io.data.Prefs
import com.mobile.photo.recovery.io.ui.components.PrimaryPillButton
import com.mobile.photo.recovery.io.ui.theme.OnSurface
import com.mobile.photo.recovery.io.ui.theme.OnSurfaceVariant
import com.mobile.photo.recovery.io.ui.theme.OutlineVariant
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.PrimaryContainer
import com.mobile.photo.recovery.io.ui.theme.PrimaryFixedDim
import com.mobile.photo.recovery.io.ui.theme.SuccessGreen
import com.mobile.photo.recovery.io.ui.theme.Surface
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainer
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerHighest
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLow
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLowest
import kotlinx.coroutines.launch

// Feature-tile accent pairs used by the mockups: violet, mint and amber.
private val TileViolet = Color(0xFFF0ECFC) to Primary
private val TileMint = Color(0xFFDCFCE7) to SuccessGreen
private val TileAmber = Color(0xFFFEF3C7) to Color(0xFFB45309)
private val AmberFolder = Color(0xFFB27B16)
private val StepPillFill = Color(0xFFE8F8EC)
private val StepPillStroke = Color(0xFFD2F3D9)
private val StepPillText = Color(0xFF006E1C)

private data class OnboardingFeature(
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int,
    val tile: Pair<Color, Color>,
    val descAccented: Boolean = false
)

private data class OnboardingPage(
    val titleRes: Int,
    val descRes: Int,
    val trustIcon: ImageVector,
    val trustRes: Int,
    val features: List<OnboardingFeature>
)

// Content per spec 4.3 — kept verbatim for brand consistency, though the underlying
// "deep sector scan / zero data loss / lossless guarantee" claims are marketing copy
// with no special technical mechanism behind them (see PROJECT_SUMMARY.md section 7).
private val pages = listOf(
    OnboardingPage(
        titleRes = R.string.onboarding1_title,
        descRes = R.string.onboarding1_desc,
        trustIcon = Icons.Filled.Lock,
        trustRes = R.string.onboarding1_trust,
        features = listOf(
            OnboardingFeature(Icons.Filled.Layers, R.string.onboarding1_feature1_title, R.string.onboarding1_feature1_desc, TileViolet),
            OnboardingFeature(Icons.Filled.PhotoLibrary, R.string.onboarding1_feature2_title, R.string.onboarding1_feature2_desc, TileViolet),
            OnboardingFeature(Icons.Filled.Bolt, R.string.onboarding1_feature3_title, R.string.onboarding1_feature3_desc, TileViolet)
        )
    ),
    OnboardingPage(
        titleRes = R.string.onboarding2_title,
        descRes = R.string.onboarding2_desc,
        trustIcon = Icons.Filled.Lock,
        trustRes = R.string.onboarding2_trust,
        features = listOf(
            OnboardingFeature(Icons.AutoMirrored.Filled.ManageSearch, R.string.onboarding2_feature1_title, R.string.onboarding2_feature1_desc, TileViolet),
            OnboardingFeature(Icons.Filled.Security, R.string.onboarding2_feature2_title, R.string.onboarding2_feature2_desc, TileMint, descAccented = true),
            OnboardingFeature(Icons.Filled.FolderSpecial, R.string.onboarding2_feature3_title, R.string.onboarding2_feature3_desc, TileAmber)
        )
    ),
    OnboardingPage(
        titleRes = R.string.onboarding3_title,
        descRes = R.string.onboarding3_desc,
        trustIcon = Icons.Filled.Verified,
        trustRes = R.string.onboarding3_trust,
        features = listOf(
            OnboardingFeature(Icons.Filled.HighQuality, R.string.onboarding3_feature1_title, R.string.onboarding3_feature1_desc, TileViolet),
            OnboardingFeature(Icons.Filled.MoveToInbox, R.string.onboarding3_feature2_title, R.string.onboarding3_feature2_desc, TileMint, descAccented = true),
            OnboardingFeature(Icons.Filled.CleaningServices, R.string.onboarding3_feature3_title, R.string.onboarding3_feature3_desc, TileAmber)
        )
    )
)

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = rememberHostActivity()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        InterstitialAdHelper.preload(context, AdPlacement.INTER_ONBOARDING_DONE)
    }

    fun finish() {
        Prefs.get(context).onboardingCompleted = true
        val host = activity
        if (host != null) {
            host.showInterstitial(AdPlacement.INTER_ONBOARDING_DONE, onGetStarted)
        } else {
            onGetStarted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Top bar: back circle (start) + STEP N OF 3 pill (center) + Skip pill (end).
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pagerState.currentPage > 0) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainer)
                        .clickable { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = OnSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(Modifier.size(36.dp))
            }

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(StepPillFill)
                    .border(1.dp, StepPillStroke, CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.onboarding_step_of, pagerState.currentPage + 1, pages.size).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = StepPillText
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceContainer)
                    .clickable { finish() }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    stringResource(R.string.action_skip),
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurface
                )
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { pageIndex ->
            OnboardingPageContent(pages[pageIndex], pageIndex)
        }

        // Single full-width CTA with a trailing arrow, matching the mockup's bottom action.
        val isLast = pagerState.currentPage == pages.lastIndex
        PrimaryPillButton(
            text = stringResource(if (isLast) R.string.onboarding_get_started else R.string.action_continue),
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            height = 54.dp,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            onClick = {
                if (isLast) finish() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }
        )
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, pageIndex: Int) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (pageIndex) {
            0 -> Step1Hero()
            1 -> Step2Hero()
            else -> Step3Hero()
        }

        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(page.descRes),
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // 3 pagination dots, the current one widened to a bar.
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            pages.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(if (index == pageIndex) 24.dp else 8.dp)
                        .background(
                            if (index == pageIndex) Primary else OutlineVariant.copy(alpha = 0.6f),
                            CircleShape
                        )
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            pages[pageIndex].features.forEach { feature ->
                FeatureTile(feature, Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(14.dp))
        NativeAdSlot(
            placement = AdPlacement.NATIVE_ONBOARD,
            layout = NativeAdLayout.Medium
        )

        // Trust badge banner.
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceContainer)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(pages[pageIndex].trustIcon, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(pages[pageIndex].trustRes),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatureTile(feature: OnboardingFeature, modifier: Modifier = Modifier) {
    val (tileBg, tileTint) = feature.tile
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, SurfaceContainer, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(tileBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(feature.icon, contentDescription = null, tint = tileTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(feature.titleRes),
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
            color = OnSurface,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(feature.descRes),
            style = MaterialTheme.typography.bodySmall,
            color = if (feature.descAccented) SuccessGreen else OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** A rounded-square "recovered photo" placeholder chip with a green check badge (no bundled photos). */
@Composable
private fun PhotoChip(size: androidx.compose.ui.unit.Dp, rotationDeg: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .graphicsRotate(rotationDeg)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerHighest)
            .border(2.dp, Color.White, RoundedCornerShape(14.dp))
    ) {
        Icon(
            Icons.Filled.Image,
            contentDescription = null,
            tint = Primary.copy(alpha = 0.4f),
            modifier = Modifier.align(Alignment.Center).size(size / 2.2f)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .size(16.dp)
                .background(SuccessGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
        }
    }
}

private fun Modifier.graphicsRotate(deg: Float): Modifier = this.then(
    Modifier.rotate(deg)
)

/**
 * Step 1 hero — matches onboarding_step_1/code.html: a rotating "radar" disc inside concentric
 * rings, with 3 recovered-photo chips orbiting it, and a floating "3 Sectors Restored" pill.
 */
@Composable
private fun Step1Hero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceContainerLow),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.align(Alignment.TopStart).offset((-40).dp, (-40).dp).size(176.dp).background(Primary.copy(alpha = 0.08f), CircleShape))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(32.dp, 32.dp).size(176.dp).background(SuccessGreen.copy(alpha = 0.1f), CircleShape))
        Box(modifier = Modifier.size(192.dp).background(PrimaryFixedDim.copy(alpha = 0.15f), CircleShape))

        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(192.dp, 168.dp)) {
                // Concentric rings around the radar disc.
                Box(modifier = Modifier.size(192.dp).border(1.dp, Primary.copy(alpha = 0.2f), CircleShape))
                Box(modifier = Modifier.size(168.dp).border(1.dp, Primary.copy(alpha = 0.3f), CircleShape))
                Box(modifier = Modifier.size(144.dp).border(1.dp, OutlineVariant.copy(alpha = 0.6f), CircleShape))

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Brush.linearGradient(listOf(Primary, PrimaryContainer)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Radar, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
                }

                PhotoChip(56.dp, -6f, Modifier.align(Alignment.TopStart).offset((-4).dp, (-8).dp))
                PhotoChip(56.dp, 6f, Modifier.align(Alignment.TopEnd).offset(4.dp, 4.dp))
                PhotoChip(48.dp, 12f, Modifier.align(Alignment.BottomStart).offset(24.dp, (-8).dp))
            }
            Spacer(Modifier.height(4.dp))
            // Floating "3 Sectors Restored" status pill.
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceContainerLowest)
                    .border(1.dp, OutlineVariant.copy(alpha = 0.3f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(SuccessGreen, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.onboarding1_sectors_restored),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurface
                )
            }
        }
    }
}

/**
 * Step 2 hero — matches onboarding_step_2_1/code.html: a green verified-shield disc with an outer
 * ring, a purple "search" badge and an amber "folder" badge orbiting it, and 3 recovered-photo
 * chips below.
 */
@Composable
private fun Step2Hero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceContainerLow),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.align(Alignment.TopStart).offset((-40).dp, (-40).dp).size(176.dp).background(StepPillFill.copy(alpha = 0.6f), CircleShape))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(32.dp, 32.dp).size(176.dp).background(StepPillFill.copy(alpha = 0.5f), CircleShape))
        Box(modifier = Modifier.size(192.dp).background(PrimaryFixedDim.copy(alpha = 0.15f), CircleShape))

        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(144.dp)) {
                Box(modifier = Modifier.size(144.dp).background(SurfaceContainerLowest.copy(alpha = 0.8f), CircleShape))
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Brush.linearGradient(listOf(SuccessGreen, Color(0xFF4ADE80))), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
                }
                Box(
                    modifier = Modifier.align(Alignment.TopStart).size(32.dp).background(Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).background(AmberFolder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FolderSpecial, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                PhotoChip(56.dp, 0f)
                PhotoChip(64.dp, 0f)
                PhotoChip(56.dp, 0f)
            }
        }
    }
}

/**
 * Step 3 hero — matches onboarding_step_3/code.html: 3 fanned "restored" cards (a bigger center
 * card tagged "HD RAW" with a photo/quality summary, flanked by 2 tilted side cards), a floating
 * "+3.4 GB Reclaimed" pill, and a live storage bar with Restored/Cleaned stats.
 */
@Composable
private fun Step3Hero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceContainerLow)
    ) {
        Box(modifier = Modifier.align(Alignment.TopStart).offset((-40).dp, (-40).dp).size(176.dp).background(StepPillFill.copy(alpha = 0.5f), CircleShape))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(32.dp, 32.dp).size(176.dp).background(PrimaryFixedDim.copy(alpha = 0.2f), CircleShape))

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(150.dp)) {
                SideCard(Modifier.align(Alignment.Center).offset((-56).dp, (-2).dp), rotationDeg = -9f, labelRes = R.string.onboarding3_card_vacation)
                SideCard(Modifier.align(Alignment.Center).offset(56.dp, (-2).dp), rotationDeg = 9f, labelRes = R.string.onboarding3_card_portraits)
                VaultCard(Modifier.align(Alignment.Center))
                // Floating "+3.4 GB Reclaimed" pill overlapping the bottom edge of the vault card.
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 8.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLowest)
                        .border(1.dp, OutlineVariant.copy(alpha = 0.3f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.onboarding3_reclaimed_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurface
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Live storage bar.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest.copy(alpha = 0.9f))
                    .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PieChart, contentDescription = null, tint = Primary, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.onboarding3_storage_optimized),
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurface
                        )
                    }
                    Text(
                        stringResource(R.string.onboarding3_storage_free),
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHighest)
                ) {
                    Box(modifier = Modifier.weight(0.65f).fillMaxSize().background(Primary))
                    Box(modifier = Modifier.weight(0.25f).fillMaxSize().background(SuccessGreen))
                    Box(modifier = Modifier.weight(0.10f).fillMaxSize())
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(R.string.onboarding3_storage_restored),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.onboarding3_storage_cleaned),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SideCard(modifier: Modifier, rotationDeg: Float, labelRes: Int) {
    Box(
        modifier = modifier
            .graphicsRotate(rotationDeg)
            .size(96.dp, 112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Image, contentDescription = null, tint = Primary.copy(alpha = 0.35f))
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(labelRes),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = OnSurface,
                    maxLines = 1
                )
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun VaultCard(modifier: Modifier) {
    Box(
        modifier = modifier
            .size(128.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, Color.White, RoundedCornerShape(18.dp))
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHighest)
            ) {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = null,
                    tint = Primary.copy(alpha = 0.35f),
                    modifier = Modifier.align(Alignment.Center)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(8.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(
                        stringResource(R.string.onboarding3_hd_raw_badge),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                        color = Color.White
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.onboarding3_photos_count),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = OnSurface
                )
                Text(
                    stringResource(R.string.onboarding3_photos_ok),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = SuccessGreen
                )
            }
            Text(
                stringResource(R.string.onboarding3_photos_caption),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = OnSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
