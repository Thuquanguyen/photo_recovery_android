package com.mobile.photo.recovery.io.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ui.components.OutlinedPillButton
import com.mobile.photo.recovery.io.ui.components.PrimaryPillButton
import com.mobile.photo.recovery.io.ui.theme.NeutralDark
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.Tertiary
import kotlinx.coroutines.launch

/**
 * Premium screen — built per spec 4.10, but intentionally has ZERO navigation entry points
 * anywhere else in this app (matching the original's unreachable "dead screen" behavior).
 * Everything here is cosmetic: no billing library, no real purchase or restore logic, no
 * feature gating anywhere in the app depends on this screen.
 */
@Composable
fun PremiumScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val trialMessage = stringResource(R.string.premium_trial_snackbar)
    val restoreMessage = stringResource(R.string.premium_restore_snackbar)
    var yearlySelected by remember { mutableStateOf(true) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralDark)
                .padding(padding)
                .padding(24.dp)
        ) {
            // Crown badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Tertiary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = NeutralDark, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.padding(top = 16.dp))
            Text(stringResource(R.string.premium_title), style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.padding(top = 20.dp))

            // Benefits checklist
            listOf(
                R.string.premium_benefit_1,
                R.string.premium_benefit_2,
                R.string.premium_benefit_3,
                R.string.premium_benefit_4
            ).forEach { res ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Secondary, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        stringResource(res),
                        modifier = Modifier.padding(start = 12.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.padding(top = 20.dp))

            // Plan cards
            PlanCard(
                title = stringResource(R.string.premium_yearly_title),
                subtitle = stringResource(R.string.premium_yearly_sub),
                price = stringResource(R.string.premium_yearly),
                badge = stringResource(R.string.premium_best_value_badge),
                selected = yearlySelected,
                onClick = { yearlySelected = true }
            )
            Spacer(Modifier.padding(top = 10.dp))
            PlanCard(
                title = stringResource(R.string.premium_monthly_title),
                subtitle = stringResource(R.string.premium_monthly_sub),
                price = stringResource(R.string.premium_monthly),
                badge = null,
                selected = !yearlySelected,
                onClick = { yearlySelected = false }
            )

            Spacer(Modifier.padding(top = 24.dp))
            PrimaryPillButton(
                text = if (yearlySelected) stringResource(R.string.premium_start_trial) else stringResource(R.string.premium_continue_monthly),
                modifier = Modifier.fillMaxWidth(),
                onClick = { scope.launch { snackbarHostState.showSnackbar(trialMessage) } }
            )
            Spacer(Modifier.padding(top = 12.dp))
            OutlinedPillButton(
                text = stringResource(R.string.premium_restore_purchases),
                modifier = Modifier.fillMaxWidth(),
                onClick = { scope.launch { snackbarHostState.showSnackbar(restoreMessage) } }
            )
        }
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
    Column {
        if (badge != null) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Tertiary)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(badge, style = MaterialTheme.typography.labelSmall, color = NeutralDark)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) Tertiary else Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (selected) Tertiary else Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = NeutralDark, modifier = Modifier.size(14.dp))
                    }
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                }
            }
            Text(price, style = MaterialTheme.typography.titleMedium, color = Tertiary)
        }
    }
}
