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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appadskit.AdPlacement
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ads.BannerAdSlot
import com.mobile.photo.recovery.io.ads.NativeAdLayout
import com.mobile.photo.recovery.io.ads.NativeAdSlot
import com.mobile.photo.recovery.io.ui.theme.CardSurface
import com.mobile.photo.recovery.io.ui.theme.LavenderBackground
import com.mobile.photo.recovery.io.ui.theme.NeutralDark
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.Tertiary

private const val PRIVACY_POLICY_URL =
    "https://raw.githubusercontent.com/Thuquanguyen/photo_recovery_restore_new/refs/heads/main/policy"
private const val SUPPORT_EMAIL = "support@photorecovery.app"

/** Guards against ActivityNotFoundException when no app can handle the intent (e.g. no browser/mail client). */
private fun safeStartActivity(context: android.content.Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (_: android.content.ActivityNotFoundException) {
        // No app available to handle this action; silently ignore rather than crash.
    }
}

@Composable
fun SettingsScreen(onChangeLanguage: () -> Unit, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val versionName = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LavenderBackground)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            // App info card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Icon(
                                painterResource(R.mipmap.ic_launcher),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
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
                            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                            Text(
                                stringResource(R.string.settings_version, versionName),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Row(
                                modifier = Modifier.padding(top = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(LavenderBackground)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(stringResource(R.string.settings_free_plan), style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(Modifier.size(8.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Tertiary)
                                        .padding(horizontal = 10.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = NeutralDark, modifier = Modifier.size(12.dp))
                                    Text(
                                        stringResource(R.string.settings_upgrade_pro),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeutralDark,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(LavenderBackground)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Text(
                            stringResource(R.string.settings_deep_scan_engine),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                        Text(stringResource(R.string.settings_ready), style = MaterialTheme.typography.labelMedium, color = Secondary)
                    }
                }
            }

            SectionHeader(stringResource(R.string.settings_section_general))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        Icons.Filled.Language,
                        Primary,
                        stringResource(R.string.settings_language),
                        onClick = onChangeLanguage
                    )
                }
            }

            SectionHeader(stringResource(R.string.settings_section_about))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        Icons.Filled.History,
                        Tertiary,
                        stringResource(R.string.settings_restore_purchases),
                        subtitle = stringResource(R.string.settings_restore_purchases_desc),
                        trailingIcon = null
                    ) { /* no billing integration wired yet */ }
                    SettingsRow(
                        Icons.AutoMirrored.Filled.Article,
                        NeutralDark,
                        stringResource(R.string.settings_terms_of_service)
                    ) {
                        safeStartActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
                    }
                    SettingsRow(
                        Icons.Filled.Shield,
                        NeutralDark,
                        stringResource(R.string.settings_privacy_policy),
                        subtitle = stringResource(R.string.settings_privacy_policy_desc)
                    ) {
                        safeStartActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
                    }
                    SettingsRow(
                        Icons.Filled.Share,
                        NeutralDark,
                        stringResource(R.string.settings_share_app),
                        subtitle = stringResource(R.string.settings_share_app_desc)
                    ) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check out Photo Recovery: https://play.google.com/store/apps/details?id=${context.packageName}")
                        }
                        safeStartActivity(context, Intent.createChooser(shareIntent, null))
                    }
                }
            }

            SectionHeader(stringResource(R.string.settings_section_support))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        Icons.Filled.MailOutline,
                        Secondary,
                        stringResource(R.string.settings_contact_support),
                        subtitle = SUPPORT_EMAIL
                    ) {
                        safeStartActivity(context, Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL")))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = NeutralDark, modifier = Modifier.size(14.dp))
                    Text(
                        stringResource(R.string.settings_privacy_guaranteed),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Text(
                    stringResource(R.string.settings_copyright),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            NativeAdSlot(
                placement = AdPlacement.NATIVE_SETTINGS,
                layout = NativeAdLayout.Default,
                modifier = Modifier.padding(top = 16.dp)
            )
            BannerAdSlot(
                placement = AdPlacement.BANNER_SETTINGS,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = NeutralDark,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    subtitle: String? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = Icons.Filled.ChevronRight,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (trailingIcon != null) {
            Icon(trailingIcon, contentDescription = null, tint = NeutralDark, modifier = Modifier.size(20.dp))
        }
    }
}
