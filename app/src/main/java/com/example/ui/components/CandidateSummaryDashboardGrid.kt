package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CandidateSummary
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AtsGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.SecondaryCyan
import com.example.ui.viewmodel.AtsViewModel

/**
 * Candidate Summary Dashboard Component.
 * Parses resume data from the local Room database using Gemini AI,
 * displaying key qualifications, experience years, and skill tags in an organized UI grid.
 */
@Composable
fun CandidateSummaryDashboardGrid(
    viewModel: AtsViewModel,
    modifier: Modifier = Modifier,
    onScreenCandidate: ((CandidateSummary) -> Unit)? = null
) {
    val candidates by viewModel.filteredCandidateSummaries.collectAsStateWithLifecycle()
    val isBatchParsing by viewModel.isBatchParsing.collectAsStateWithLifecycle()
    val isParsingId by viewModel.isParsingCandidate.collectAsStateWithLifecycle()
    val searchQuery by viewModel.summaryFilterQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.summaryFilterCategory.collectAsStateWithLifecycle()
    val selectedDetailCandidate by viewModel.selectedCandidateSummary.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsStateWithLifecycle()
    var isCompactGridMode by remember { mutableStateOf(false) }

    fun exportPdfDossier(candidate: CandidateSummary) {
        viewModel.exportCandidateSummaryPdf(context, candidate) { pdfFile ->
            if (pdfFile != null && pdfFile.exists() && pdfFile.length() > 0) {
                try {
                    val authority = "${context.packageName}.fileprovider"
                    val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_SUBJECT, "ATS Candidate Dossier - ${candidate.name}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export Candidate Dossier (PDF)"))
                } catch (e: Exception) {
                    Toast.makeText(context, "Error sharing PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val categories = listOf("All", "Senior", "Mobile", "Backend / AI", "Lead")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("candidate_summary_dashboard")
    ) {
        // Dashboard Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryIndigo.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Candidate Talent Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SecondaryCyan.copy(alpha = 0.18f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Gemini + Room",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SecondaryCyan
                                    )
                                }
                            }
                            Text(
                                text = "Parsed qualifications & skill tags from Room DB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Layout Toggle (Compact Grid vs Detailed List)
                    IconButton(onClick = { isCompactGridMode = !isCompactGridMode }) {
                        Icon(
                            imageVector = if (isCompactGridMode) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                            contentDescription = "Toggle Grid Mode",
                            tint = PrimaryIndigo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Row: Batch parse & search
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSummaryFilterQuery(it) },
                        placeholder = { Text("Filter skills, name, qualification...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSummaryFilterQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_candidates_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { viewModel.parseAllResumesWithGemini() },
                        enabled = !isBatchParsing,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("parse_all_gemini_button")
                    ) {
                        if (isBatchParsing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Parsing...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Parse All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSummaryFilterCategory(cat) },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryIndigo.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryIndigo
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // UI Grid / List of Candidate Summaries
        if (candidates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No candidates match this filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = {
                        viewModel.setSummaryFilterQuery("")
                        viewModel.setSummaryFilterCategory("All")
                    }) {
                        Text("Reset Filters")
                    }
                }
            }
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 600.dp || isCompactGridMode

                if (isWide) {
                    // 2-Column Grid Layout
                    val chunkedCandidates = candidates.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        chunkedCandidates.forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (candidate in pair) {
                                    CandidateSummaryCard(
                                        candidate = candidate,
                                        isParsing = isParsingId == candidate.resumeId || isParsingId == candidate.id,
                                        isGeneratingPdf = isGeneratingPdf,
                                        onReparse = { viewModel.parseCandidateWithGemini(candidate.resumeId.ifBlank { candidate.id }) },
                                        onViewDetails = { viewModel.selectCandidateSummary(candidate) },
                                        onExportPdf = { exportPdfDossier(candidate) },
                                        onScreen = {
                                            viewModel.loadCandidateIntoAnalyzer(candidate)
                                            onScreenCandidate?.invoke(candidate)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    // Full-width Rich Cards
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        candidates.forEach { candidate ->
                            CandidateSummaryCard(
                                candidate = candidate,
                                isParsing = isParsingId == candidate.resumeId || isParsingId == candidate.id,
                                isGeneratingPdf = isGeneratingPdf,
                                onReparse = { viewModel.parseCandidateWithGemini(candidate.resumeId.ifBlank { candidate.id }) },
                                onViewDetails = { viewModel.selectCandidateSummary(candidate) },
                                onExportPdf = { exportPdfDossier(candidate) },
                                onScreen = {
                                    viewModel.loadCandidateIntoAnalyzer(candidate)
                                    onScreenCandidate?.invoke(candidate)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedDetailCandidate?.let { candidate ->
        CandidateSummaryDetailDialog(
            candidate = candidate,
            isGeneratingPdf = isGeneratingPdf,
            onDismiss = { viewModel.selectCandidateSummary(null) },
            onExportPdf = { exportPdfDossier(candidate) },
            onScreen = {
                viewModel.loadCandidateIntoAnalyzer(candidate)
                viewModel.selectCandidateSummary(null)
                onScreenCandidate?.invoke(candidate)
            }
        )
    }
}

/**
 * Individual Candidate Summary Card displaying qualifications, experience, and skill tags.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CandidateSummaryCard(
    candidate: CandidateSummary,
    isParsing: Boolean,
    isGeneratingPdf: Boolean = false,
    onReparse: () -> Unit,
    onViewDetails: () -> Unit,
    onExportPdf: () -> Unit,
    onScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("candidate_card_${candidate.id}")
            .clip(RoundedCornerShape(16.dp))
            .clickable { onViewDetails() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Avatar, Name, Role, AI Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.15f))
                            .border(1.5.dp, PrimaryLight.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = candidate.name.take(1).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryIndigo,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = candidate.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = candidate.headlineRole,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // AI Engine Indicator Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (candidate.parsedViaAi) SecondaryCyan.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (candidate.parsedViaAi) SecondaryCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (candidate.parsedViaAi) "Gemini" else "Room DB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (candidate.parsedViaAi) SecondaryCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Experience Years Badge Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Experience Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BusinessCenter,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = candidate.experienceYearsDisplay.ifBlank { "${candidate.experienceYears} Years" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }

                // Level Pill (Senior, Lead, etc.)
                if (candidate.experienceLevel.isNotBlank()) {
                    val levelColor = when {
                        candidate.experienceLevel.contains("Lead", true) || candidate.experienceLevel.contains("Staff", true) -> AtsGreen
                        candidate.experienceLevel.contains("Senior", true) -> PrimaryLight
                        else -> SecondaryCyan
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(levelColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = candidate.experienceLevel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelColor
                        )
                    }
                }

                // Education Degree snippet
                if (candidate.educationSummary.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = candidate.educationSummary,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            // Key Qualifications Section
            Text(
                text = "Key Qualifications",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            candidate.keyQualifications.take(3).forEach { qual ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AtsGreen,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = qual,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skill Tags Section (Organized Grid/Flow of chips)
            Text(
                text = "Skill Tags",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Show technical skills in Primary Indigo/Cyan
                candidate.technicalSkills.take(5).forEach { skill ->
                    TagChip(text = skill, color = PrimaryIndigo)
                }

                // Show tools/frameworks in Accent Purple
                candidate.toolsAndFrameworks.take(3).forEach { tool ->
                    TagChip(text = tool, color = AccentPurple)
                }

                // Show soft skills in AtsGreen
                candidate.softSkills.take(2).forEach { soft ->
                    TagChip(text = soft, color = AtsGreen)
                }

                val totalSkills = candidate.technicalSkills.size + candidate.toolsAndFrameworks.size + candidate.softSkills.size
                val displayed = candidate.technicalSkills.take(5).size + candidate.toolsAndFrameworks.take(3).size + candidate.softSkills.take(2).size
                if (totalSkills > displayed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "+${totalSkills - displayed} more",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReparse,
                    enabled = !isParsing,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isParsing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryIndigo
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Parsing...", fontSize = 11.sp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gemini", fontSize = 11.sp)
                    }
                }

                OutlinedButton(
                    onClick = onExportPdf,
                    enabled = !isGeneratingPdf,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryIndigo),
                    modifier = Modifier.weight(0.9f)
                ) {
                    if (isGeneratingPdf) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryIndigo
                        )
                    } else {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Report", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onScreen,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Screen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

}

@Composable
private fun TagChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.7.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * Detailed Dialog displaying the full Gemini-parsed Candidate Summary.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CandidateSummaryDetailDialog(
    candidate: CandidateSummary,
    isGeneratingPdf: Boolean = false,
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onScreen: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = candidate.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = candidate.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = candidate.headlineRole, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bio / Executive Summary Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Executive AI Summary", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SecondaryCyan)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = candidate.executiveSummary.ifBlank { "Experienced professional with rich technical background." },
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Experience & Education
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Experience", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(candidate.experienceYearsDisplay, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryIndigo)
                            Text(candidate.experienceLevel, fontSize = 11.sp, color = AtsGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Education", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(candidate.educationSummary.ifBlank { "B.S. Degree" }, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 2)
                        }
                    }
                }

                // All Key Qualifications
                Text("Key Qualifications & Achievements", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                candidate.keyQualifications.forEach { qual ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AtsGreen, modifier = Modifier.size(15.dp).padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(qual, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }

                // Technical Skills Matrix
                Text("Technical Skills", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    candidate.technicalSkills.forEach { skill ->
                        TagChip(text = skill, color = PrimaryIndigo)
                    }
                }

                // Tools & Frameworks
                if (candidate.toolsAndFrameworks.isNotEmpty()) {
                    Text("Tools & Cloud Stack", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        candidate.toolsAndFrameworks.forEach { tool ->
                            TagChip(text = tool, color = AccentPurple)
                        }
                    }
                }

                // Soft Skills
                if (candidate.softSkills.isNotEmpty()) {
                    Text("Leadership & Soft Skills", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        candidate.softSkills.forEach { soft ->
                            TagChip(text = soft, color = AtsGreen)
                        }
                    }
                }

                // Contact
                if (candidate.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(candidate.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onExportPdf,
                    enabled = !isGeneratingPdf,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryIndigo)
                ) {
                    if (isGeneratingPdf) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryIndigo
                        )
                    } else {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PDF")
                    }
                }

                Button(
                    onClick = onScreen,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Screen ATS")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

