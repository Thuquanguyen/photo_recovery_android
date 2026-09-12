package com.mobile.photo.recovery.io.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appadskit.AdPlacement
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ads.NativeAdLayout
import com.mobile.photo.recovery.io.ads.NativeAdSlot
import com.mobile.photo.recovery.io.data.Prefs
import com.mobile.photo.recovery.io.ui.theme.LavenderBackground
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.Secondary
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

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Secondary, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Language, contentDescription = null, tint = Color.White)
            }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
            Text(
                stringResource(R.string.language_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                stringResource(R.string.language_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                placeholder = { Text(stringResource(R.string.language_search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            if (popular.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.language_popular), Icons.Filled.Star) }
                items(popular, key = { it.code }) { lang ->
                    LanguageRow(lang, selected == lang.code) { onSelect(lang) }
                }
            }
            if (all.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.language_all), Icons.Filled.Language) }
                items(all, key = { it.code }) { lang ->
                    LanguageRow(lang, selected == lang.code) { onSelect(lang) }
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            NativeAdSlot(
                placement = AdPlacement.NATIVE_LANGUAGE,
                layout = NativeAdLayout.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = onConfirm,
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.language_confirm), style = MaterialTheme.typography.labelLarge)
                androidx.compose.foundation.layout.Spacer(Modifier.padding(start = 8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
        androidx.compose.foundation.layout.Spacer(Modifier.padding(start = 6.dp))
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LanguageRow(lang: AppLanguage, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Secondary.copy(alpha = 0.12f) else LavenderBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(lang.flagEmoji, style = MaterialTheme.typography.titleLarge)
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(lang.nativeName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        lang.englishName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) Secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (isSelected) Secondary else LavenderBackground,
                        androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
