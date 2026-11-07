package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CandidateRankingItem
import com.example.data.model.CandidateStatus
import com.example.ui.components.GlassCard
import com.example.ui.theme.AtsGreen
import com.example.ui.theme.AtsRed
import com.example.ui.theme.AtsAmber
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.SecondaryCyan
import com.example.ui.viewmodel.AtsViewModel
import com.example.ui.viewmodel.CandidateSortOption

@Composable
fun CandidateRankingScreen(
    viewModel: AtsViewModel
) {
    val candidates by viewModel.candidateRankings.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    val filteredCandidates = candidates.filter { candidate ->
        candidate.name.contains(searchQuery, ignoreCase = true) ||
        candidate.jobTitle.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Candidate Ranking & HR Hub",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Multi-Resume Automated Screening Matrix",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = PrimaryLight,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Search & Sorting Bar
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search candidates by name or role...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort Candidates By:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { sortDropdownExpanded = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (sortOption) {
                                        CandidateSortOption.ATS_SCORE_DESC -> "ATS Score (High → Low)"
                                        CandidateSortOption.EXPERIENCE_DESC -> "Experience (Years)"
                                        CandidateSortOption.SKILL_MATCH_DESC -> "Skill Match %"
                                        CandidateSortOption.NAME_ASC -> "Candidate Name (A → Z)"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = PrimaryIndigo)
                            }

                            DropdownMenu(
                                expanded = sortDropdownExpanded,
                                onDismissRequest = { sortDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("ATS Score (High → Low)") },
                                    onClick = {
                                        viewModel.setSortOption(CandidateSortOption.ATS_SCORE_DESC)
                                        sortDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Experience (Years)") },
                                    onClick = {
                                        viewModel.setSortOption(CandidateSortOption.EXPERIENCE_DESC)
                                        sortDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Skill Match %") },
                                    onClick = {
                                        viewModel.setSortOption(CandidateSortOption.SKILL_MATCH_DESC)
                                        sortDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Candidate Name (A → Z)") },
                                    onClick = {
                                        viewModel.setSortOption(CandidateSortOption.NAME_ASC)
                                        sortDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Candidate List Items
        if (filteredCandidates.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No candidate profiles match your search filter.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredCandidates, key = { it.id }) { candidate ->
                CandidateRankCard(
                    candidate = candidate,
                    onStatusChange = { newStatus ->
                        viewModel.updateCandidateStatus(candidate.id, newStatus)
                    }
                )
            }
        }
    }
}

@Composable
private fun CandidateRankCard(
    candidate: CandidateRankingItem,
    onStatusChange: (CandidateStatus) -> Unit
) {
    var statusMenuExpanded by remember { mutableStateOf(false) }

    val statusColor = when (candidate.status) {
        CandidateStatus.SHORTLISTED -> AtsGreen
        CandidateStatus.REVIEWING -> AtsAmber
        CandidateStatus.INTERVIEWED -> SecondaryCyan
        CandidateStatus.REJECTED -> AtsRed
        CandidateStatus.NEW -> PrimaryLight
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = candidate.name.take(1),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryIndigo
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = candidate.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = candidate.jobTitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ATS Score Badge
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AtsGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${candidate.overallAtsScore}/100",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (candidate.overallAtsScore >= 80) AtsGreen else PrimaryLight
                        )
                    }
                    Text(
                        text = "Skill Match: ${candidate.skillMatchPercent}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row: Experience, Skills Matched, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exp: ${candidate.experienceYears} yrs  •  Skills: ${candidate.matchedSkillsCount}/${candidate.totalRequiredSkills}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Status chip with dropdown selector
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .clickable { statusMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = candidate.status.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        CandidateStatus.values().forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.name, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    onStatusChange(st)
                                    statusMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
