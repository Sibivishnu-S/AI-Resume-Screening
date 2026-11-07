package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.AnalysisResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN_LEFT = 36f
    private const val MARGIN_RIGHT = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT

    fun sanitizeFileName(candidateName: String, jobTitle: String): String {
        val candidateClean = candidateName.replace(Regex("[^a-zA-Z0-9_-]"), "_").replace(Regex("_+"), "_").trim('_')
        val jobClean = jobTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_").replace(Regex("_+"), "_").trim('_')
        
        val candidatePart = if (candidateClean.isNotBlank()) candidateClean else "Candidate"
        val jobPart = if (jobClean.isNotBlank()) jobClean else "Report"
        
        return "ATS_Screening_${candidatePart}_${jobPart}.pdf"
    }

    fun generatePdfReport(context: Context, analysis: AnalysisResult): File {
        val fileName = sanitizeFileName(analysis.candidateName, analysis.jobTitle)
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val pdfFile = File(exportDir, fileName)

        val document = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var currentPage = document.startPage(pageInfo)
        var canvas = currentPage.canvas

        var y = 36f

        // Helper paints
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A237E") // Deep Indigo
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#37474F") // Slate Grey
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val sectionHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0D47A1") // Royal Blue
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#212121") // Dark Grey
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#212121")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bgRectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.parseColor("#CFD8DC")
        }

        fun checkPageBreak(requiredHeight: Float) {
            if (y + requiredHeight > PAGE_HEIGHT - 50f) {
                // Draw footer
                val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#90A4AE")
                    textSize = 8f
                }
                canvas.drawText(
                    "Page $pageNumber | AI Resume Screening & ATS Evaluation System - Confidential",
                    MARGIN_LEFT,
                    PAGE_HEIGHT - 20f,
                    footerPaint
                )

                document.finishPage(currentPage)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas
                y = 36f

                // Top mini-header for subsequent pages
                canvas.drawText("ATS Evaluation Report - ${analysis.candidateName}", MARGIN_LEFT, y, subtitlePaint)
                y += 15f
                canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, borderPaint)
                y += 20f
            }
        }

        fun drawWrappedText(text: String, paint: Paint, leading: Float = 14f) {
            val words = text.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val width = paint.measureText(testLine)
                if (width > CONTENT_WIDTH) {
                    checkPageBreak(leading)
                    canvas.drawText(currentLine, MARGIN_LEFT, y, paint)
                    y += leading
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                checkPageBreak(leading)
                canvas.drawText(currentLine, MARGIN_LEFT, y, paint)
                y += leading
            }
        }

        // --- HEADER BANNER ---
        bgRectPaint.color = Color.parseColor("#1A237E")
        canvas.drawRect(RectF(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + 54f), bgRectPaint)

        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("AI RESUME SCREENING & ATS EVALUATION REPORT", MARGIN_LEFT + 14f, y + 24f, headerTextPaint)

        val headerSubTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0E0E0")
            textSize = 9f
        }
        val currentDateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
        canvas.drawText("Generated on: $currentDateStr | Powered by AI Resume Engine", MARGIN_LEFT + 14f, y + 42f, headerSubTextPaint)
        y += 68f

        // --- CANDIDATE & JOB METADATA CARD ---
        checkPageBreak(70f)
        bgRectPaint.color = Color.parseColor("#F5F7FA")
        canvas.drawRoundRect(RectF(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + 64f), 8f, 8f, bgRectPaint)
        canvas.drawRoundRect(RectF(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + 64f), 8f, 8f, borderPaint)

        canvas.drawText("CANDIDATE NAME:", MARGIN_LEFT + 12f, y + 20f, bodyBoldPaint)
        canvas.drawText(analysis.candidateName, MARGIN_LEFT + 125f, y + 20f, bodyPaint)

        canvas.drawText("TARGET POSITION:", MARGIN_LEFT + 12f, y + 36f, bodyBoldPaint)
        canvas.drawText("${analysis.jobTitle} at ${analysis.companyName}", MARGIN_LEFT + 125f, y + 36f, bodyPaint)

        canvas.drawText("EVALUATION TIER:", MARGIN_LEFT + 12f, y + 52f, bodyBoldPaint)
        val tierPaint = Paint(bodyBoldPaint).apply { color = Color.parseColor("#2E7D32") }
        canvas.drawText(analysis.scoreBreakdown.tier, MARGIN_LEFT + 125f, y + 52f, tierPaint)
        y += 78f

        // --- OVERALL ATS SCORE BANNER ---
        checkPageBreak(65f)
        bgRectPaint.color = Color.parseColor("#E8F5E9") // Soft Green
        val scoreRect = RectF(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + 55f)
        canvas.drawRoundRect(scoreRect, 8f, 8f, bgRectPaint)
        val scoreBorderPaint = Paint(borderPaint).apply { color = Color.parseColor("#81C784") }
        canvas.drawRoundRect(scoreRect, 8f, 8f, scoreBorderPaint)

        val scoreTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B5E20")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("ATS SCORE: ${analysis.scoreBreakdown.overallScore} / 100", MARGIN_LEFT + 16f, y + 34f, scoreTitlePaint)

        val salaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E7D32")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Market Benchmark: ${analysis.estimatedSalaryRange}", PAGE_WIDTH - MARGIN_RIGHT - 210f, y + 32f, salaryTextPaint)
        y += 70f

        // --- SCORE BREAKDOWN TABLE ---
        checkPageBreak(130f)
        canvas.drawText("ATS Score Breakdown (Weighted Components)", MARGIN_LEFT, y, sectionHeaderPaint)
        y += 12f

        // Table Header
        val tableHeaderY = y
        bgRectPaint.color = Color.parseColor("#ECEFF1")
        canvas.drawRect(RectF(MARGIN_LEFT, tableHeaderY, PAGE_WIDTH - MARGIN_RIGHT, tableHeaderY + 22f), bgRectPaint)
        canvas.drawRect(RectF(MARGIN_LEFT, tableHeaderY, PAGE_WIDTH - MARGIN_RIGHT, tableHeaderY + 22f), borderPaint)

        canvas.drawText("Evaluation Dimension", MARGIN_LEFT + 10f, tableHeaderY + 15f, bodyBoldPaint)
        canvas.drawText("Formula Weight", MARGIN_LEFT + 250f, tableHeaderY + 15f, bodyBoldPaint)
        canvas.drawText("Match Score", PAGE_WIDTH - MARGIN_RIGHT - 90f, tableHeaderY + 15f, bodyBoldPaint)
        y += 22f

        val breakdownItems = listOf(
            Triple("Keyword Match Density", "30%", "${analysis.scoreBreakdown.keywordMatchScore}%"),
            Triple("Technical Skill Match", "25%", "${analysis.scoreBreakdown.skillMatchScore}%"),
            Triple("Experience & Seniority Level", "20%", "${analysis.scoreBreakdown.experienceScore}%"),
            Triple("Education & Degree Fit", "10%", "${analysis.scoreBreakdown.educationScore}%"),
            Triple("Project Relevance & Impact", "10%", "${analysis.scoreBreakdown.projectScore}%"),
            Triple("Formatting & Structure Quality", "5%", "${analysis.scoreBreakdown.formattingScore}%")
        )

        for (item in breakdownItems) {
            checkPageBreak(20f)
            bgRectPaint.color = Color.WHITE
            canvas.drawRect(RectF(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + 20f), bgRectPaint)
            canvas.drawRect(RectF(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + 20f), borderPaint)

            canvas.drawText(item.first, MARGIN_LEFT + 10f, y + 14f, bodyPaint)
            canvas.drawText(item.second, MARGIN_LEFT + 250f, y + 14f, bodyPaint)
            canvas.drawText(item.third, PAGE_WIDTH - MARGIN_RIGHT - 90f, y + 14f, bodyBoldPaint)
            y += 20f
        }
        y += 16f

        // --- TECHNICAL SKILL ALIGNMENT ---
        checkPageBreak(80f)
        canvas.drawText("Technical Skill Alignment & Gap Analysis", MARGIN_LEFT, y, sectionHeaderPaint)
        y += 14f

        // Matched Skills
        canvas.drawText("Matched Core Skills (${analysis.matchedSkills.size}):", MARGIN_LEFT, y, bodyBoldPaint)
        y += 12f
        val matchedText = if (analysis.matchedSkills.isNotEmpty()) analysis.matchedSkills.joinToString(", ") else "None detected"
        val matchedPaint = Paint(bodyPaint).apply { color = Color.parseColor("#2E7D32") }
        drawWrappedText(matchedText, matchedPaint)
        y += 10f

        // Missing Skills
        canvas.drawText("Critical Missing Skills (${analysis.missingSkills.size}):", MARGIN_LEFT, y, bodyBoldPaint)
        y += 12f
        val missingText = if (analysis.missingSkills.isNotEmpty()) analysis.missingSkills.joinToString(", ") else "None missing"
        val missingPaint = Paint(bodyPaint).apply { color = Color.parseColor("#C62828") }
        drawWrappedText(missingText, missingPaint)
        y += 10f

        // Additional Skills
        if (analysis.additionalSkills.isNotEmpty()) {
            canvas.drawText("Additional Candidate Skills (${analysis.additionalSkills.size}):", MARGIN_LEFT, y, bodyBoldPaint)
            y += 12f
            val addText = analysis.additionalSkills.joinToString(", ")
            val addPaint = Paint(bodyPaint).apply { color = Color.parseColor("#00838F") }
            drawWrappedText(addText, addPaint)
            y += 10f
        }
        y += 10f

        // --- EXECUTIVE SUMMARY & AI FEEDBACK ---
        checkPageBreak(80f)
        canvas.drawText("Executive Summary & AI Feedback", MARGIN_LEFT, y, sectionHeaderPaint)
        y += 14f
        drawWrappedText(analysis.aiSummaryFeedback, bodyPaint)
        y += 14f

        // --- ATS OPTIMIZATION ACTION PLAN ---
        if (analysis.aiSuggestions.isNotEmpty()) {
            checkPageBreak(80f)
            canvas.drawText("ATS Optimization Recommendations & Action Plan", MARGIN_LEFT, y, sectionHeaderPaint)
            y += 14f

            analysis.aiSuggestions.forEachIndexed { idx, suggestion ->
                val bulletText = "${idx + 1}. $suggestion"
                drawWrappedText(bulletText, bodyPaint)
                y += 4f
            }
            y += 10f
        }

        // --- AI GENERATED INTERVIEW QUESTIONS ---
        if (analysis.interviewQuestions.isNotEmpty()) {
            checkPageBreak(80f)
            canvas.drawText("Recommended Interview Preparation Questions", MARGIN_LEFT, y, sectionHeaderPaint)
            y += 14f

            analysis.interviewQuestions.forEachIndexed { idx, q ->
                checkPageBreak(35f)
                canvas.drawText("Q${idx + 1} (${q.category}): ${q.question}", MARGIN_LEFT, y, bodyBoldPaint)
                y += 13f
                drawWrappedText("Hint: ${q.sampleAnswerHint}", subtitlePaint)
                y += 6f
            }
        }

        // Draw final page footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#90A4AE")
            textSize = 8f
        }
        canvas.drawText(
            "Page $pageNumber | AI Resume Screening & ATS Evaluation System - Confidential",
            MARGIN_LEFT,
            PAGE_HEIGHT - 20f,
            footerPaint
        )

        document.finishPage(currentPage)

        // Write document to file
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }
}
