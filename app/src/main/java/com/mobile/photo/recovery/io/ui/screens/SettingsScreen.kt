package com.mobile.photo.recovery.io.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appadskit.AdPlacement
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ads.NativeAdLayout
import com.mobile.photo.recovery.io.ads.NativeAdSlot
import com.mobile.photo.recovery.io.ui.components.SectionCapsHeader
import com.mobile.photo.recovery.io.ui.theme.OnSurface
import com.mobile.photo.recovery.io.ui.theme.OnSurfaceVariant
import com.mobile.photo.recovery.io.ui.theme.OnTertiaryFixed
import com.mobile.photo.recovery.io.ui.theme.Outline
import com.mobile.photo.recovery.io.ui.theme.OutlineVariant
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.PrimaryFixed
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.SecondaryContainer
import com.mobile.photo.recovery.io.ui.theme.Surface
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainer
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerHigh
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLow
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLowest
import com.mobile.photo.recovery.io.ui.theme.Tertiary
import com.mobile.photo.recovery.io.ui.theme.TertiaryFixed

// Both raw.githubusercontent.com and jsdelivr's GitHub mirror force Content-Type: text/plain
// (with nosniff) on every file regardless of extension, so an .html page there shows as raw
// markup instead of rendering. htmlpreview.github.io fetches a raw GitHub file client-side and
// renders it as a real HTML page, which is what actually lets these open readable in a browser.
private const val PRIVACY_POLICY_URL =
    "https://htmlpreview.github.io/?https://raw.githubusercontent.com/Thuquanguyen/photo_recovery_android/main/policy/privacy_policy.html"
private const val TERMS_OF_SERVICE_URL =
    "https://htmlpreview.github.io/?https://raw.githubusercontent.com/Thuquanguyen/photo_recovery_android/main/policy/terms_of_service.html"
private const val SUPPORT_EMAIL = "support@photorecovery.app"

/**
 * Guards against crashing when no app can handle the intent (e.g. no browser/mail client), and
 * against "Calling startActivity() from outside of an Activity context requires
 * FLAG_ACTIVITY_NEW_TASK" — LocalContext here can be the locale-wrapped Context from
 * MainActivity's createConfigurationContext() rather than the Activity itself.
 */
private fun safeStartActivity(context: android.content.Context, intent: Intent) {
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (_: android.content.ActivityNotFoundException) {
        // No app available to handle this action; silently ignore rather than crash.
    }
}

@Composable
fun SettingsScreen(onChangeLanguage: () -> Unit, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val packageInfo = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "1.0.0"
    val playStoreUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"

    Column(modifier = Modifier.fillMaxSize().background(Surface)) {
        // Top app bar — a solid primary bar, per this screen's own mockup.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = stringResource(R.string.action_back),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(8.dp).background(SecondaryContainer, CircleShape))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // App info bento header card.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
            ) {
                // Ambient glow bloom.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = (-30).dp)
                        .size(140.dp)
                        .background(PrimaryFixed.copy(alpha = 0.5f), CircleShape)
                )
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceContainer)
                                .padding(4.dp)
                        ) {
                            // R.mipmap.ic_launcher is an <adaptive-icon> XML — painterResource()
                            // can't load that (only VectorDrawables/raster assets), hence the
                            // raster foreground layer here instead (see SplashScreen.kt).
                            androidx.compose.foundation.Image(
                                painter = painterResource(R.mipmap.ic_launcher_foreground),
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .scale(1.55f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Column(Modifier.padding(start = 16.dp).weight(1f)) {
                            Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineMedium,
                                color = OnSurface,
                                maxLines = 1
                            )
                            Text(
                                stringResource(R.string.settings_version, versionName),
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.settings_free_plan),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SurfaceContainerHigh)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(TertiaryFixed)
                                        .padding(horizontal = 10.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = OnTertiaryFixed,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        stringResource(R.string.settings_upgrade_pro),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnTertiaryFixed
                                    )
                                }
                            }
                        }
                    }

                    // Quick storage status strip.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerLow)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Text(
                            stringResource(R.string.settings_deep_scan_engine),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurface,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                        Box(modifier = Modifier.size(8.dp).background(Secondary, CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.settings_ready),
                            style = MaterialTheme.typography.labelMedium,
                            color = Secondary
                        )
                    }
                }
            }

            SectionCapsHeader(stringResource(R.string.settings_section_general))
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.Language,
                    iconTint = Primary,
                    iconContainer = SurfaceContainerHigh,
                    label = stringResource(R.string.settings_language),
                    onClick = onChangeLanguage
                )
            }

            SectionCapsHeader(stringResource(R.string.settings_section_about))
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.History,
                    iconTint = OnTertiaryFixed,
                    iconContainer = TertiaryFixed,
                    label = stringResource(R.string.settings_restore_purchases),
                    subtitle = stringResource(R.string.settings_restore_purchases_desc),
                    trailingIcon = Icons.Filled.Cached
                ) { /* no billing integration wired yet */ }
                SettingsDivider()
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.Article,
                    iconTint = OnSurfaceVariant,
                    iconContainer = SurfaceContainer,
                    label = stringResource(R.string.settings_terms_of_service),
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew
                ) {
                    safeStartActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_OF_SERVICE_URL)))
                }
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Filled.Shield,
                    iconTint = OnSurfaceVariant,
                    iconContainer = SurfaceContainer,
                    label = stringResource(R.string.settings_privacy_policy),
                    subtitle = stringResource(R.string.settings_privacy_policy_desc),
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew
                ) {
                    safeStartActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
                }
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Filled.Star,
                    iconTint = Tertiary,
                    iconContainer = TertiaryFixed.copy(alpha = 0.4f),
                    label = stringResource(R.string.settings_rate_us),
                    subtitle = stringResource(R.string.settings_rate_us_desc),
                    badge = "5.0 ★"
                ) {
                    safeStartActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl)))
                }
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Filled.Share,
                    iconTint = Primary,
                    iconContainer = PrimaryFixed,
                    label = stringResource(R.string.settings_share_app),
                    subtitle = stringResource(R.string.settings_share_app_desc),
                    trailingIcon = Icons.Filled.Share
                ) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out Photo Recovery: $playStoreUrl")
                    }
                    safeStartActivity(context, Intent.createChooser(shareIntent, null))
                }
            }

            SectionCapsHeader(stringResource(R.string.settings_section_support))
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.SupportAgent,
                    iconTint = Secondary,
                    iconContainer = SecondaryContainer,
                    label = stringResource(R.string.settings_contact_support),
                    subtitle = SUPPORT_EMAIL,
                    trailingIcon = Icons.Filled.MailOutline
                ) {
                    safeStartActivity(context, Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL")))
                }
            }

            // App brand signoff footer.
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Outline, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.settings_privacy_guaranteed).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Outline
                    )
                }
                Text(
                    stringResource(R.string.settings_copyright),
                    style = MaterialTheme.typography.bodySmall,
                    color = OutlineVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            NativeAdSlot(
                placement = AdPlacement.NATIVE_SETTINGS,
                layout = NativeAdLayout.Default,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
    ) { content() }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp)
            .height(1.dp)
            .background(OutlineVariant.copy(alpha = 0.3f))
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconContainer: Color,
    label: String,
    subtitle: String? = null,
    badge: String? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = Icons.Filled.ChevronRight,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = OnSurface)
                if (badge != null) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnTertiaryFixed,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TertiaryFixed)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
        if (trailingIcon != null) {
            Icon(trailingIcon, contentDescription = null, tint = Outline, modifier = Modifier.size(20.dp))
        }
    }
}
