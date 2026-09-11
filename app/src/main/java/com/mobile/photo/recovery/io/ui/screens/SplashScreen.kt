package com.mobile.photo.recovery.io.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.data.Prefs
import com.mobile.photo.recovery.io.ui.theme.Primary
import kotlinx.coroutines.delay

/**
 * Branded splash. The 2200ms delay and its progress bar are purely cosmetic (spec 4.1) —
 * the only real work done here is reading the onboarding-completed flag to decide where to go.
 */
private const val SPLASH_DURATION_MS = 2200

@Composable
fun SplashScreen(onFinished: (onboardingCompleted: Boolean) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = SPLASH_DURATION_MS, easing = LinearEasing),
        label = "splashProgress"
    )

    LaunchedEffect(Unit) {
        progress = 1f
        delay(SPLASH_DURATION_MS.toLong())
        val onboardingCompleted = Prefs.get(context).onboardingCompleted
        onFinished(onboardingCompleted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(Color(0xFF140B24), Color(0xFF240C4A), Color(0xFF0E061A))
                )
            )
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(96.dp)
                .fillMaxWidth(0.4f)
                .background(Primary, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.height(40.dp))
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
        Text(
            buildAnnotatedTitle(),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.splash_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = Primary
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(4.dp)
                    .background(Primary, RoundedCornerShape(2.dp))
            )
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.splash_initializing),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
        androidx.compose.material3.HorizontalDivider(
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth(0.7f)
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Shield,
                contentDescription = null,
                tint = Color(0xFF34D399).copy(alpha = 0.8f),
                modifier = Modifier.height(14.dp)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.width(6.dp))
            Text(
                stringResource(R.string.splash_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun buildAnnotatedTitle() =
    stringResource(R.string.splash_title_part1) + stringResource(R.string.splash_title_part2)
