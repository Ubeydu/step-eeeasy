package com.example.stepeeeasy.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stepeeeasy.domain.model.Walk
import com.example.stepeeeasy.util.FormatUtils
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * Walk list item composable.
 *
 * Layout:
 * ┌────────────────────────────────────────────┐
 * │  #1    │  5.8 km        │  Sunday         │
 * │        │  7,860 steps   │  Oct 29, 2025   │
 * └────────────────────────────────────────────┘
 *
 * Three columns:
 * - Left: Walk ID
 * - Middle: Distance + Steps
 * - Right: Day of week + Date
 */
@Composable
fun WalkListItem(
    walk: Walk,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Walk ID
            Text(
                text = "#${walk.id}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Middle: Distance + Steps (stacked)
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            ) {
                Text(
                    text = FormatUtils.formatDistance(walk.distanceKm),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${FormatUtils.formatSteps(walk.totalSteps)} steps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right: Day of week + Date (stacked)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = walk.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = walk.startTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
