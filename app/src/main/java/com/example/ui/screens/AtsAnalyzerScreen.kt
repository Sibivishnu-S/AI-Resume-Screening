package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.BarItemData
import com.example.ui.components.CircularAtsGauge
import com.example.ui.components.GlassCard
import com.example.ui.components.ScoreBreakdownBarChart
import com.example.ui.components.SkillPill
import com.example.ui.components.SkillType
import com.example.ui.theme.AtsGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.SecondaryCyan
import com.example.ui.viewmodel.AtsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AtsAnalyzerScreen(
    viewModel: AtsViewModel,
    onNavigateToReport: () -> Unit
) {
    val resumeText by viewModel.resumeInputText.collectAsStateWithLifecycle()
    val jobText by viewModel.jobInputText.collectAsStateWithLifecycle()
    val currentAnalysis by viewModel.currentAnalysis.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()

    var selectedInputTab by remember { mutableIntStateOf(0) } // 0: Resume, 1: Job Description
    var selectedResultTab by remember { mutableIntStateOf(0) } // 0: Breakdown, 1: Skill Gap, 2: AI Suggestions, 3: Interview Qs

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "ATS Evaluation Engine",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Resume vs Job Description Match",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = PrimaryLight,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Input Editor Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    // Input Selector Tabs
                    TabRow(selectedTabIndex = selectedInputTab) {
                        Tab(
                            selected = selectedInputTab == 0,
                            onClick = { selectedInputTab = 0 },
                            text = { Text("1. Candidate Resume", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Description, contentDescription = null) }
                        )
                        Tab(
                            selected = selectedInputTab == 1,
                            onClick = { selectedInputTab = 1 },
                            text = { Text("2. Job Description", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Work, contentDescription = null) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Templates Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sample Role Presets:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SampleButton("Android Engineer") {
                                viewModel.selectSampleJob("Senior Android Engineer")
                                viewModel.selectSampleResume("Senior Mobile Developer")
                            }
                            SampleButton("AI Full Stack") {
                                viewModel.selectSampleJob("AI Full Stack Developer")
                                viewModel.selectSampleResume("Full Stack AI Developer")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Field for Selected Tab
                    if (selectedInputTab == 0) {
                        OutlinedTextField(
                            value = resumeText,
                            onValueChange = { viewModel.resumeInputText.value = it },
                            label = { Text("Paste Resume Text") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = jobText,
                            onValueChange = { viewModel.jobInputText.value = it },
                            label = { Text("Paste Job Description Text") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Evaluate Action Button
                    Button(
                        onClick = { viewModel.analyzeResume() },
                        enabled = !isAnalyzing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Parsing & Calculating ATS Score...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Evaluate ATS Compatibility", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // RESULTS SECTION (If analysis exists)
        currentAnalysis?.let { analysis ->
            // Circular Meter Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ATS Compatibility Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        CircularAtsGauge(
                            score = analysis.scoreBreakdown.overallScore,
                            size = 170.dp,
                            strokeWidth = 16.dp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Estimated Salary Range: ${analysis.estimatedSalaryRange}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = AtsGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onNavigateToReport,
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryCyan)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Full ATS Report", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Results Details Tabs
            item {
                Column {
                    TabRow(selectedTabIndex = selectedResultTab) {
                        Tab(
                            selected = selectedResultTab == 0,
                            onClick = { selectedResultTab = 0 },
                            text = { Text("Score Breakdown", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedResultTab == 1,
                            onClick = { selectedResultTab = 1 },
                            text = { Text("Skill Gap Matrix", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedResultTab == 2,
                            onClick = { selectedResultTab = 2 },
                            text = { Text("AI Suggestions", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedResultTab == 3,
                            onClick = { selectedResultTab = 3 },
                            text = { Text("Interview Prep", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        when (selectedResultTab) {
                            0 -> {
                                Column {
                                    Text(
                                        text = "ATS Score Breakdown (Weighted Formula)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    val items = listOf(
                                        BarItemData("Keyword Match", analysis.scoreBreakdown.keywordMatchScore, "30% weight"),
                                        BarItemData("Skill Match", analysis.scoreBreakdown.skillMatchScore, "25% weight"),
                                        BarItemData("Experience Level", analysis.scoreBreakdown.experienceScore, "20% weight"),
                                        BarItemData("Education Degree", analysis.scoreBreakdown.educationScore, "10% weight"),
                                        BarItemData("Project Overlap", analysis.scoreBreakdown.projectScore, "10% weight"),
                                        BarItemData("Formatting & Structure", analysis.scoreBreakdown.formattingScore, "5% weight")
                                    )
                                    ScoreBreakdownBarChart(items = items)
                                }
                            }

                            1 -> {
                                Column {
                                    Text(
                                        text = "Matched vs Missing Skills Matrix",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Skill Gap: ${analysis.skillGapPercentage}% Missing Competencies",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("Matched Core Skills (${analysis.matchedSkills.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AtsGreen)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        analysis.matchedSkills.forEach { skill ->
                                            SkillPill(skillName = skill, type = SkillType.MATCHED)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text("Missing Required Skills (${analysis.missingSkills.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        analysis.missingSkills.forEach { skill ->
                                            SkillPill(skillName = skill, type = SkillType.MISSING)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text("Additional Candidate Skills (${analysis.additionalSkills.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecondaryCyan)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        analysis.additionalSkills.forEach { skill ->
                                            SkillPill(skillName = skill, type = SkillType.ADDITIONAL)
                                        }
                                    }
                                }
                            }

                            2 -> {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = PrimaryLight)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AI Improvement Suggestions",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    analysis.aiSuggestions.forEach { suggestion ->
                                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = suggestion, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Executive AI Feedback:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryLight
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = analysis.aiSummaryFeedback,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            3 -> {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Psychology, contentDescription = null, tint = PrimaryLight)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AI Generated Interview Questions",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    analysis.interviewQuestions.forEachIndexed { idx, q ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = "Q${idx + 1}: ${q.question}",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Category: ${q.category}",
                                                    fontSize = 11.sp,
                                                    color = PrimaryLight,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Hint: ${q.sampleAnswerHint}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleButton(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.15f))
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryIndigo,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
