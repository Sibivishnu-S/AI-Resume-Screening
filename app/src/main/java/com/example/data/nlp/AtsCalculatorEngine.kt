package com.example.data.nlp

import com.example.data.model.AnalysisResult
import com.example.data.model.AtsScoreBreakdown
import com.example.data.model.InterviewQuestion
import com.example.data.model.JobDescription
import com.example.data.model.ParsedResume
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

object AtsCalculatorEngine {

    fun calculateAtsScore(resume: ParsedResume, job: JobDescription): AnalysisResult {
        val resumeSkills = (resume.technicalSkills + resume.softSkills + resume.toolsAndFrameworks)
            .map { it.lowercase(Locale.ROOT).trim() }.toSet()

        val jobRequiredSkills = job.requiredSkills.map { it.lowercase(Locale.ROOT).trim() }.toSet()
        val jobPreferredSkills = job.preferredSkills.map { it.lowercase(Locale.ROOT).trim() }.toSet()
        val allJobSkills = jobRequiredSkills + jobPreferredSkills

        // 1. Skill Match (25% Weight)
        val matchedSkillsSet = allJobSkills.filter { skill ->
            resumeSkills.any { rs -> rs == skill || rs.contains(skill) || skill.contains(rs) }
        }.toSet()

        val missingSkillsSet = allJobSkills - matchedSkillsSet
        val additionalSkillsSet = resumeSkills - matchedSkillsSet

        val skillMatchPercentage = if (allJobSkills.isNotEmpty()) {
            ((matchedSkillsSet.size.toFloat() / allJobSkills.size) * 100).roundToInt()
        } else 80

        val skillMatchScore = min(100, skillMatchPercentage)

        // 2. Keyword Match (30% Weight)
        val jobKeywords = job.keywords.map { it.lowercase(Locale.ROOT).trim() }.filter { it.length > 2 }
        val resumeRawLower = resume.rawText.lowercase(Locale.ROOT)

        val matchedKeywordsList = jobKeywords.filter { kw ->
            resumeRawLower.contains(kw) || resumeSkills.any { s -> s.contains(kw) }
        }
        val missingKeywordsList = jobKeywords - matchedKeywordsList.toSet()

        val keywordMatchScore = if (jobKeywords.isNotEmpty()) {
            ((matchedKeywordsList.size.toFloat() / jobKeywords.size) * 100).roundToInt()
        } else 75

        // 3. Experience Match (20% Weight)
        val candidateYears = resume.experience.sumOf { it.durationYears.toDouble() }.toFloat()
        val requiredYears = job.requiredExperienceYears

        val experienceScore = when {
            requiredYears <= 0f -> 100
            candidateYears >= requiredYears -> 100
            candidateYears >= requiredYears * 0.75f -> 85
            candidateYears >= requiredYears * 0.5f -> 65
            candidateYears > 0f -> 45
            else -> 30
        }

        // 4. Education Match (10% Weight)
        val hasDegree = resume.education.isNotEmpty()
        val educationScore = if (hasDegree) 100 else 50

        // 5. Projects Match (10% Weight)
        val hasProjects = resume.projects.isNotEmpty()
        val projectScore = if (hasProjects) 100 else 40

        // 6. Formatting & Structure (5% Weight)
        var formattingScore = 100
        if (resume.email.isBlank()) formattingScore -= 20
        if (resume.phone.isBlank()) formattingScore -= 10
        if (resume.technicalSkills.isEmpty()) formattingScore -= 30
        if (resume.experience.isEmpty()) formattingScore -= 20
        if (formattingScore < 0) formattingScore = 20

        // Overall Weighted Score (0 to 100)
        val overallScore = (
            (keywordMatchScore * 0.30) +
            (skillMatchScore * 0.25) +
            (experienceScore * 0.20) +
            (educationScore * 0.10) +
            (projectScore * 0.10) +
            (formattingScore * 0.05)
        ).roundToInt().coerceIn(0, 100)

        val breakdown = AtsScoreBreakdown(
            overallScore = overallScore,
            keywordMatchScore = keywordMatchScore,
            skillMatchScore = skillMatchScore,
            experienceScore = experienceScore,
            educationScore = educationScore,
            projectScore = projectScore,
            formattingScore = formattingScore
        )

        val skillGapPercent = 100 - skillMatchPercentage.coerceIn(0, 100)

        // Generate AI / Algorithmic Suggestions
        val suggestions = mutableListOf<String>()
        val missingSkillsFormatted = missingSkillsSet.map { it.replaceFirstChar { c -> c.uppercase() } }

        if (missingSkillsFormatted.isNotEmpty()) {
            val top3Missing = missingSkillsFormatted.take(3).joinToString(", ")
            suggestions.add("Add missing core technologies: $top3Missing to your skills section.")
        }
        if (missingKeywordsList.isNotEmpty()) {
            val top3Keywords = missingKeywordsList.take(3).joinToString(", ")
            suggestions.add("Include ATS keywords: $top3Keywords in your project or experience descriptions.")
        }
        if (candidateYears < requiredYears) {
            suggestions.add("Highlight hands-on project accomplishments to offset the experience gap ($candidateYears yrs vs $requiredYears yrs required).")
        }
        if (resume.achievements.isEmpty()) {
            suggestions.add("Incorporate quantifiable metrics (e.g. 'Improved latency by 30%', 'Increased user engagement by 25%') under past roles.")
        }
        suggestions.add("Use strong action verbs like 'Architected', 'Spearheaded', 'Optimized', and 'Deployed' in role descriptions.")
        suggestions.add("Ensure standard section headers (e.g. 'Skills', 'Experience', 'Education') for 100% ATS scanner compatibility.")

        val summaryFeedback = buildString {
            append("The candidate demonstrates a ${breakdown.tier} with an overall ATS score of $overallScore/100. ")
            append("Technical skill alignment is at $skillMatchPercentage%, matching ${matchedSkillsSet.size} out of ${allJobSkills.size} required competencies. ")
            if (missingSkillsFormatted.isNotEmpty()) {
                append("Key skill gaps identified include: ${missingSkillsFormatted.take(4).joinToString(", ")}. ")
            } else {
                append("Candidate possesses all primary required technical skills. ")
            }
            append("Resume formatting and section structure scored $formattingScore/100.")
        }

        val salaryRange = when {
            candidateYears >= 5f || overallScore >= 85 -> "$135,000 - $175,000 USD / yr"
            candidateYears >= 2f || overallScore >= 70 -> "$100,000 - $135,000 USD / yr"
            else -> "$75,000 - $100,000 USD / yr"
        }

        val sampleQuestions = listOf(
            InterviewQuestion(
                question = "How have you implemented ${matchedSkillsSet.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "system architecture"} in a production setting?",
                category = "Technical Architecture",
                sampleAnswerHint = "Structure your answer using the STAR method (Situation, Task, Action, Result), highlighting scale, concurrency, and trade-offs."
            ),
            InterviewQuestion(
                question = "Can you describe how you would bridge your current gap in ${missingSkillsFormatted.firstOrNull() ?: "cloud deployment"} if hired for this role?",
                category = "Growth & Adaptability",
                sampleAnswerHint = "Emphasize past fast-learning achievements, related technologies you already know, and your proactive learning roadmap."
            ),
            InterviewQuestion(
                question = "Tell me about a complex technical challenge you solved in your past project.",
                category = "Behavioral & Problem Solving",
                sampleAnswerHint = "Focus on the root cause analysis, collaborative troubleshooting, and measurable performance improvements achieved."
            )
        )

        return AnalysisResult(
            id = System.currentTimeMillis().toString(),
            candidateName = resume.name,
            jobTitle = job.title,
            companyName = job.company,
            scoreBreakdown = breakdown,
            matchedSkills = matchedSkillsSet.map { it.replaceFirstChar { c -> c.uppercase() } },
            missingSkills = missingSkillsFormatted,
            additionalSkills = additionalSkillsSet.map { it.replaceFirstChar { c -> c.uppercase() } },
            skillGapPercentage = skillGapPercent,
            matchedKeywords = matchedKeywordsList.map { it.replaceFirstChar { c -> c.uppercase() } },
            missingKeywords = missingKeywordsList.map { it.replaceFirstChar { c -> c.uppercase() } },
            aiSuggestions = suggestions,
            aiSummaryFeedback = summaryFeedback,
            estimatedSalaryRange = salaryRange,
            interviewQuestions = sampleQuestions
        )
    }
}
