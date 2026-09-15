package com.mobile.photo.recovery.io.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appadskit.AdPlacement
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ads.NativeAdLayout
import com.mobile.photo.recovery.io.ads.NativeAdSlot
import com.mobile.photo.recovery.io.data.Prefs
import com.mobile.photo.recovery.io.ui.components.PrimaryPillButton
import com.mobile.photo.recovery.io.ui.components.SectionCapsHeader
import com.mobile.photo.recovery.io.ui.theme.OnSurface
import com.mobile.photo.recovery.io.ui.theme.OnSurfaceVariant
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.SecondaryContainer
import com.mobile.photo.recovery.io.ui.theme.Surface
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainer
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLowest
import com.mobile.photo.recovery.io.ui.theme.TertiaryFixedDim
import com.mobile.photo.recovery.io.ui.theme.OnTertiaryFixed
import com.mobile.photo.recovery.io.util.AppLanguage
import com.mobile.photo.recovery.io.util.LocaleHelper

@Composable
fun LanguageScreen(onConfirm: () -> Unit) {
    val appContext = LocalContext.current
    val prefs = remember { Prefs.get(appContext) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(prefs.languageCode ?: java.util.Locale.getDefault().language) }
    // Live preview (spec 4.2): re-derive a Context configured with the tapped locale so this
    // screen's own text updates immediately, without persisting anything until Confirm and
    // without recreating the Activity (which would restart the whole nav graph from Splash).
    var previewContext by remember(selected) { mutableStateOf(LocaleHelper.applyLocale(appContext, selected)) }

    fun selectLanguage(lang: AppLanguage) {
        selected = lang.code
        previewContext = LocaleHelper.applyLocale(appContext, lang.code)
    }

    CompositionLocalProvider(LocalContext provides previewContext) {
        LanguageScreenContent(
            query = query,
            onQueryChange = { query = it },
            selected = selected,
            onSelect = ::selectLanguage,
            onConfirm = {
                prefs.languageCode = selected
                onConfirm()
            }
        )
    }
}

@Composable
private fun LanguageScreenContent(
    query: String,
    onQueryChange: (String) -> Unit,
    selected: String,
    onSelect: (AppLanguage) -> Unit,
    onConfirm: () -> Unit
) {
    val filtered = LocaleHelper.languages.filter {
        query.isBlank() ||
            it.englishName.contains(query, ignoreCase = true) ||
            it.nativeName.contains(query, ignoreCase = true)
    }
    val popular = filtered.filter { it.popular }
    val all = filtered.filter { !it.popular }

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            // Leaves room for the docked confirm deck below.
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(68.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(Secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Public,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(TertiaryFixedDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Translate,
                                contentDescription = null,
                                tint = OnTertiaryFixed,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.language_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stringResource(R.string.language_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item { LanguageSearchField(query, onQueryChange) }

            if (popular.isNotEmpty()) {
                item {
                    SectionCapsHeader(
                        stringResource(R.string.language_popular),
                        icon = Icons.Filled.Star,
                        iconTint = TertiaryFixedDim
                    )
                }
                items(popular, key = { it.code }) { lang ->
                    LanguageRow(lang, selected == lang.code) { onSelect(lang) }
                }
            }
            if (all.isNotEmpty()) {
                item {
                    SectionCapsHeader(
                        stringResource(R.string.language_all),
                        icon = Icons.Filled.Language,
                        iconTint = Primary,
                        trailing = stringResource(R.string.language_available_count, LocaleHelper.languages.size)
                    )
                }
                items(all, key = { it.code }) { lang ->
                    LanguageRow(lang, selected == lang.code) { onSelect(lang) }
                }
            }
        }

        // Persistent docked bottom confirm deck.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Surface.copy(alpha = 0.92f))
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Column {
                NativeAdSlot(
                    placement = AdPlacement.NATIVE_LANGUAGE,
                    layout = NativeAdLayout.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                PrimaryPillButton(
                    text = stringResource(R.string.language_confirm),
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    containerColor = Secondary,
                    height = 56.dp,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
private fun LanguageSearchField(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainer)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    stringResource(R.string.language_search_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                cursorBrush = SolidColor(Primary),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = OnSurface),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                Icons.Filled.Close,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(18.dp).clickable { onQueryChange("") }
            )
        }
    }
}

@Composable
private fun LanguageRow(lang: AppLanguage, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) SecondaryContainer.copy(alpha = 0.3f) else SurfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(lang.flagEmoji, style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(lang.nativeName, style = MaterialTheme.typography.titleSmall, color = OnSurface, maxLines = 1)
                Text(
                    lang.englishName,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Secondary else OnSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isSelected) Secondary else SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
