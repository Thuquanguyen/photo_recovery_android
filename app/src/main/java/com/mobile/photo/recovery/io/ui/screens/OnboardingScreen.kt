package com.mobile.photo.recovery.io.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appadskit.AdPlacement
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ads.InterstitialAdHelper
import com.mobile.photo.recovery.io.ads.NativeAdLayout
import com.mobile.photo.recovery.io.ads.NativeAdSlot
import com.mobile.photo.recovery.io.ads.rememberHostActivity
import com.mobile.photo.recovery.io.ads.showInterstitial
import com.mobile.photo.recovery.io.data.Prefs
import com.mobile.photo.recovery.io.ui.components.PrimaryPillButton
import com.mobile.photo.recovery.io.ui.components.TextOnlyButton
import com.mobile.photo.recovery.io.ui.theme.LavenderBackground
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.Tertiary
import kotlinx.coroutines.launch

private data class OnboardingFeature(val icon: ImageVector, val titleRes: Int, val descRes: Int)
private data class OnboardingPage(
    val heroIcon: ImageVector,
    val heroGradient: List<Color>,
    val satelliteIcons: Pair<ImageVector, ImageVector>,
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
        heroIcon = Icons.Filled.Search,
        heroGradient = listOf(Primary, Primary.copy(alpha = 0.7f)),
        satelliteIcons = Icons.Filled.Bolt to Icons.Filled.PhotoLibrary,
        titleRes = R.string.onboarding1_title,
        descRes = R.string.onboarding1_desc,
        trustIcon = Icons.Filled.Lock,
        trustRes = R.string.onboarding1_trust,
        features = listOf(
            OnboardingFeature(Icons.Filled.Layers, R.string.onboarding1_feature1_title, R.string.onboarding1_feature1_desc),
            OnboardingFeature(Icons.Filled.PhotoLibrary, R.string.onboarding1_feature2_title, R.string.onboarding1_feature2_desc),
            OnboardingFeature(Icons.Filled.Bolt, R.string.onboarding1_feature3_title, R.string.onboarding1_feature3_desc)
        )
    ),
    OnboardingPage(
        heroIcon = Icons.Filled.VerifiedUser,
        heroGradient = listOf(Secondary, Secondary.copy(alpha = 0.7f)),
        satelliteIcons = Icons.Filled.Search to Icons.Filled.FolderSpecial,
        titleRes = R.string.onboarding2_title,
        descRes = R.string.onboarding2_desc,
        trustIcon = Icons.Filled.Lock,
        trustRes = R.string.onboarding2_trust,
        features = listOf(
            OnboardingFeature(Icons.Filled.ManageSearch, R.string.onboarding2_feature1_title, R.string.onboarding2_feature1_desc),
            OnboardingFeature(Icons.Filled.Security, R.string.onboarding2_feature2_title, R.string.onboarding2_feature2_desc),
            OnboardingFeature(Icons.Filled.FolderSpecial, R.string.onboarding2_feature3_title, R.string.onboarding2_feature3_desc)
        )
    ),
    OnboardingPage(
        heroIcon = Icons.Filled.SwapHoriz,
        heroGradient = listOf(Tertiary, Tertiary.copy(alpha = 0.7f)),
        satelliteIcons = Icons.Filled.Check to Icons.Filled.Image,
        titleRes = R.string.onboarding3_title,
        descRes = R.string.onboarding3_desc,
        trustIcon = Icons.Filled.Verified,
        trustRes = R.string.onboarding3_trust,
        features = listOf(
            OnboardingFeature(Icons.Filled.HighQuality, R.string.onboarding3_feature1_title, R.string.onboarding3_feature1_desc),
            OnboardingFeature(Icons.Filled.MoveToInbox, R.string.onboarding3_feature2_title, R.string.onboarding3_feature2_desc),
            OnboardingFeature(Icons.Filled.CleaningServices, R.string.onboarding3_feature3_title, R.string.onboarding3_feature3_desc)
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

    Column(modifier = Modifier.fillMaxSize().background(LavenderBackground).statusBarsPadding()) {
        // Top bar: back icon button (start) + step tracker pill (center) + Skip (end),
        // matching the UI spec's header across all 3 steps.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pagerState.currentPage > 0) {
                IconButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            } else {
                Box(modifier = Modifier.size(36.dp))
            }

            Card(
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(containerColor = Secondary.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(Secondary, CircleShape))
                    Text(
                        stringResource(R.string.onboarding_step_of, pagerState.currentPage + 1, pages.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary
                    )
                }
            }

            TextOnlyButton(text = stringResource(R.string.action_skip), onClick = { finish() })
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { pageIndex ->
            OnboardingPageContent(pages[pageIndex])
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            pages.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(8.dp)
                        .width(if (index == pagerState.currentPage) 24.dp else 8.dp)
                        .background(
                            if (index == pagerState.currentPage) Primary else Primary.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
            }
        }

        NativeAdSlot(
            placement = AdPlacement.NATIVE_ONBOARD,
            layout = NativeAdLayout.Medium,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // Single full-width CTA with a trailing arrow, matching the UI spec's bottom action.
        if (pagerState.currentPage == pages.lastIndex) {
            PrimaryPillButton(
                text = stringResource(R.string.onboarding_get_started),
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                onClick = { finish() }
            )
        } else {
            PrimaryPillButton(
                text = stringResource(R.string.action_continue),
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OnboardingHero(page)

        androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
        Text(
            stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
        Text(
            stringResource(page.descRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))

        // Trust badge banner (spec 4.3).
        Card(
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(page.trustIcon, contentDescription = null, tint = Secondary, modifier = Modifier.size(16.dp))
                Text(
                    stringResource(page.trustRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Secondary
                )
            }
        }

        if (page.features.isNotEmpty()) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                page.features.forEach { feature ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(feature.icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            }
                            androidx.compose.foundation.layout.Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(feature.titleRes),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                stringResource(feature.descRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hero illustration card matching the UI spec's mockups: a soft rounded card containing a
 * central gradient circle with the page's icon, two small "satellite" badges orbiting it, and
 * two decorative "recovered item" chips with a check badge below — built entirely from local
 * vector icons/shapes (no bundled or remote photos) so it works fully offline.
 */
@Composable
private fun OnboardingHero(page: OnboardingPage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Brush.linearGradient(page.heroGradient), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(page.heroIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(32.dp)
                        .background(Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(page.satelliteIcons.first, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(32.dp)
                        .background(Tertiary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(page.satelliteIcons.second, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(LavenderBackground, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null, tint = Primary.copy(alpha = 0.5f))
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-2).dp, y = (-2).dp)
                                .size(16.dp)
                                .background(Secondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }
        }
    }
}
