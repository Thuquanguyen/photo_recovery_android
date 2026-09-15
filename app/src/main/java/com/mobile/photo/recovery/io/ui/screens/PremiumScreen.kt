package com.mobile.photo.recovery.io.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ui.theme.AccentAmber
import com.mobile.photo.recovery.io.ui.theme.AccentEmerald
import com.mobile.photo.recovery.io.ui.theme.GlassFill
import com.mobile.photo.recovery.io.ui.theme.GlassStroke
import kotlinx.coroutines.launch

/**
 * Premium screen — built per spec 4.10, but intentionally has ZERO navigation entry points
 * anywhere else in this app (matching the original's unreachable "dead screen" behavior).
 * Everything here is cosmetic: no billing library, no real purchase or restore logic, no
 * feature gating anywhere in the app depends on this screen.
 */
private val PremiumGradient = listOf(
    Color(0xFF1E1035),
    Color(0xFF2B1055),
    Color(0xFF44107A),
    Color(0xFF6B1182)
)
private val AmberDeep = Color(0xFFF59E0B)
private val AmberLight = Color(0xFFFDE68A)
private val ZincInk = Color(0xFF18181B)

@Composable
fun PremiumScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val trialMessage = stringResource(R.string.premium_trial_snackbar)
    val restoreMessage = stringResource(R.string.premium_restore_snackbar)
    var yearlySelected by remember { mutableStateOf(true) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(PremiumGradient))) {
            // Background glow accents.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(240.dp)
                    .background(Color(0xFFC026D3).copy(alpha = 0.25f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(240.dp)
                    .background(Color(0xFF7C3AED).copy(alpha = 0.2f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top bar: close + restore.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GlassFill)
                            .border(1.dp, GlassStroke, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GlassFill)
                            .border(1.dp, GlassStroke, CircleShape)
                            .clickable { scope.launch { snackbarHostState.showSnackbar(restoreMessage) } }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.premium_restore_purchases),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // Hero title & badge.
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(AmberDeep, AccentAmber, AmberLight)))
                            .padding(2.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF31115E), Color(0xFF1C0B36)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AccentAmber.copy(alpha = 0.15f))
                            .border(1.dp, AccentAmber.copy(alpha = 0.3f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Stars, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            stringResource(R.string.premium_unlocked_badge).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentAmber
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.premium_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.premium_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }

                // Visual showcase: recovered-media reel.
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassFill)
                        .border(1.dp, GlassStroke, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("HEIC • 100%", "RAW • Lossless", "4K VIDEO").forEach { tag ->
                        ShowcaseTile(tag, Modifier.weight(1f))
                    }
                }

                // Key benefits checklist.
                Spacer(Modifier.height(14.dp))
                listOf(
                    R.string.premium_benefit_1,
                    R.string.premium_benefit_2,
                    R.string.premium_benefit_3,
                    R.string.premium_benefit_4
                ).forEach { res ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald.copy(alpha = 0.2f))
                                .border(1.dp, AccentEmerald.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(13.dp))
                        }
                        Text(
                            stringResource(res),
                            modifier = Modifier.padding(start = 10.dp),
                            color = Color.White.copy(alpha = 0.95f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Pricing cards.
                Spacer(Modifier.height(16.dp))
                PlanCard(
                    title = stringResource(R.string.premium_yearly_title),
                    subtitle = stringResource(R.string.premium_yearly_sub),
                    price = stringResource(R.string.premium_yearly),
                    badge = stringResource(R.string.premium_best_value_badge),
                    selected = yearlySelected,
                    onClick = { yearlySelected = true }
                )
                Spacer(Modifier.height(10.dp))
                PlanCard(
                    title = stringResource(R.string.premium_monthly_title),
                    subtitle = stringResource(R.string.premium_monthly_sub),
                    price = stringResource(R.string.premium_monthly),
                    badge = null,
                    selected = !yearlySelected,
                    onClick = { yearlySelected = false }
                )

                // Primary action & footer disclosures.
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(AccentAmber, AmberDeep, Color(0xFFD97706))))
                        .clickable { scope.launch { snackbarHostState.showSnackbar(trialMessage) } },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LockOpen, contentDescription = null, tint = ZincInk, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(if (yearlySelected) R.string.premium_start_trial else R.string.premium_continue_monthly),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = ZincInk
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ZincInk, modifier = Modifier.size(16.dp))
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        stringResource(R.string.premium_trust_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.settings_terms_privacy),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ShowcaseTile(tag: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF3B1D6E), Color(0xFF1C0B36))))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Icon(
            Icons.Filled.Image,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.25f),
            modifier = Modifier.align(Alignment.Center).size(32.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(AccentEmerald.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
        }
        Text(
            tag,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.95f),
            modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    subtitle: String,
    price: String,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = if (badge != null) 8.dp else 0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (selected) Color.White.copy(alpha = 0.15f) else GlassFill)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) AccentAmber.copy(alpha = 0.9f) else GlassStroke,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onClick)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (selected) AccentAmber else Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (selected) AccentAmber else Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = ZincInk, modifier = Modifier.size(14.dp))
                    }
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    price,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (selected) AccentAmber else Color.White.copy(alpha = 0.9f)
                )
            }
        }
        if (badge != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-16).dp, y = (-8).dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(AccentAmber, AmberDeep)))
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = ZincInk, modifier = Modifier.size(12.dp))
                Text(badge.uppercase(), style = MaterialTheme.typography.labelSmall, color = ZincInk)
            }
        }
    }
}
