package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BarItemData
import com.example.ui.components.GlassCard
import com.example.ui.components.RadarDimension
import com.example.ui.components.RadarSkillChart
import com.example.ui.components.ScoreBreakdownBarChart
import com.example.ui.theme.AtsGreen
import com.example.ui.theme.AtsRed
import com.example.ui.theme.AtsAmber
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.SecondaryCyan
import com.example.ui.viewmodel.AtsViewModel

@Composable
fun HrAnalyticsScreen(
    viewModel: AtsViewModel
) {
    val candidates by viewModel.candidateRankings.collectAsStateWithLifecycle()
    val history by viewModel.analysisHistory.collectAsStateWithLifecycle()

    val totalCandidates = candidates.size.coerceAtLeast(1)
    val excellentCount = candidates.count { it.overallAtsScore >= 85 }
    val goodCount = candidates.count { it.overallAtsScore in 70..84 }
    val moderateCount = candidates.count { it.overallAtsScore in 50..69 }
    val lowCount = candidates.count { it.overallAtsScore < 50 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "HR Talent Analytics Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Candidate Pool Quality & Skill Gap Intelligence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = PrimaryLight,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Radar Chart Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Radar, contentDescription = null, tint = PrimaryLight)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Candidate Skill Dimensions Polygon Map",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val radarDimensions = listOf(
                        RadarDimension("Technical", 0.88f),
                        RadarDimension("Soft Skills", 0.75f),
                        RadarDimension("Tools & Frameworks", 0.82f),
                        RadarDimension("Experience", 0.70f),
                        RadarDimension("Keywords", 0.85f),
                        RadarDimension("Education", 0.90f)
                    )

                    RadarSkillChart(
                        dimensions = radarDimensions,
                        size = 200.dp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Aggregate candidate pool balance across technical, architectural, and foundational skills.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Top Missing Skills Distribution Chart
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = AtsRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Most Frequently Missing Skills Across Applicants",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val missingSkillsData = listOf(
                        BarItemData("Docker & Containerization", 65, "65% Missing"),
                        BarItemData("Kubernetes & Orchestration", 55, "55% Missing"),
                        BarItemData("System Architecture Design", 40, "40% Missing"),
                        BarItemData("GraphQL & gRPC APIs", 35, "35% Missing"),
                        BarItemData("Automated CI/CD Pipelines", 25, "25% Missing")
                    )

                    ScoreBreakdownBarChart(items = missingSkillsData)
                }
            }
        }

        // ATS Score Distribution Histogram
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = SecondaryCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Applicant ATS Score Tier Breakdown",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val scoreDistributionData = listOf(
                        BarItemData("Excellent Fit (85-100)", ((excellentCount.toFloat() / totalCandidates) * 100).toInt().coerceAtLeast(40), "$excellentCount candidates"),
                        BarItemData("Good Match (70-84)", ((goodCount.toFloat() / totalCandidates) * 100).toInt().coerceAtLeast(35), "$goodCount candidates"),
                        BarItemData("Moderate Fit (50-69)", ((moderateCount.toFloat() / totalCandidates) * 100).toInt().coerceAtLeast(15), "$moderateCount candidates"),
                        BarItemData("Needs Optimization (<50)", ((lowCount.toFloat() / totalCandidates) * 100).toInt().coerceAtLeast(10), "$lowCount candidates")
                    )

                    ScoreBreakdownBarChart(items = scoreDistributionData)
                }
            }
        }

        // Hiring Funnel Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recruitment Pipeline Funnel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FunnelStepItem("1. Applications Evaluated", "$totalCandidates Candidates", PrimaryIndigo)
                    FunnelStepItem("2. Shortlisted (ATS Score > 80)", "$excellentCount Candidates", AtsGreen)
                    FunnelStepItem("3. Technical Review / Interview", "${candidates.count { it.status == com.example.data.model.CandidateStatus.INTERVIEWED || it.status == com.example.data.model.CandidateStatus.REVIEWING }} Candidates", SecondaryCyan)
                    FunnelStepItem("4. Final Hires Expected", "2 Candidates", Color(0xFF8B5CF6))
                }
            }
        }
    }
}

@Composable
private fun FunnelStepItem(stepTitle: String, countText: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stepTitle,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = countText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
