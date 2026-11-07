package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Parsed Resume Data Structure containing all extracted entities.
 */
data class ParsedResume(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val linkedIn: String = "",
    val gitHub: String = "",
    val portfolio: String = "",
    val technicalSkills: List<String> = emptyList(),
    val softSkills: List<String> = emptyList(),
    val toolsAndFrameworks: List<String> = emptyList(),
    val education: List<EducationItem> = emptyList(),
    val experience: List<ExperienceItem> = emptyList(),
    val projects: List<ProjectItem> = emptyList(),
    val certifications: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    val rawText: String = ""
)

data class EducationItem(
    val degree: String,
    val institution: String,
    val year: String = "",
    val gpa: String = ""
)

data class ExperienceItem(
    val role: String,
    val company: String,
    val durationYears: Float = 0f,
    val description: String = ""
)

data class ProjectItem(
    val title: String,
    val description: String,
    val techStack: List<String> = emptyList()
)

/**
 * Parsed Job Description Data Structure.
 */
data class JobDescription(
    val id: String = "",
    val title: String = "",
    val company: String = "",
    val department: String = "",
    val requiredSkills: List<String> = emptyList(),
    val preferredSkills: List<String> = emptyList(),
    val requiredExperienceYears: Float = 0f,
    val requiredEducation: String = "",
    val toolsAndFrameworks: List<String> = emptyList(),
    val responsibilities: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val rawText: String = ""
)

/**
 * ATS Score Breakdown out of 100%.
 */
data class AtsScoreBreakdown(
    val overallScore: Int = 0,
    val keywordMatchScore: Int = 0, // 30% weight
    val skillMatchScore: Int = 0,   // 25% weight
    val experienceScore: Int = 0,   // 20% weight
    val educationScore: Int = 0,    // 10% weight
    val projectScore: Int = 0,      // 10% weight
    val formattingScore: Int = 0    // 5% weight
) {
    val tier: String
        get() = when {
            overallScore >= 85 -> "Excellent Fit"
            overallScore >= 70 -> "Good Match"
            overallScore >= 50 -> "Moderate Fit"
            else -> "Needs Optimization"
        }
}

/**
 * Comprehensive Analysis Result for a Candidate vs Job Description.
 */
data class AnalysisResult(
    val id: String = "",
    val candidateName: String = "",
    val jobTitle: String = "",
    val companyName: String = "",
    val scoreBreakdown: AtsScoreBreakdown = AtsScoreBreakdown(),
    val matchedSkills: List<String> = emptyList(),
    val missingSkills: List<String> = emptyList(),
    val additionalSkills: List<String> = emptyList(),
    val skillGapPercentage: Int = 0,
    val matchedKeywords: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList(),
    val aiSuggestions: List<String> = emptyList(),
    val aiSummaryFeedback: String = "",
    val estimatedSalaryRange: String = "",
    val interviewQuestions: List<InterviewQuestion> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class InterviewQuestion(
    val question: String,
    val category: String, // Technical, Behavioral, System Design
    val sampleAnswerHint: String
)

data class CandidateRankingItem(
    val id: String,
    val name: String,
    val email: String,
    val jobTitle: String,
    val overallAtsScore: Int,
    val skillMatchPercent: Int,
    val experienceYears: Float,
    val matchedSkillsCount: Int,
    val totalRequiredSkills: Int,
    val status: CandidateStatus = CandidateStatus.NEW
)

enum class CandidateStatus {
    NEW, SHORTLISTED, REVIEWING, INTERVIEWED, REJECTED
}

data class ChatMessage(
    val id: String,
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ChatSender {
    USER, AI_ASSISTANT
}

/**
 * AI-Parsed Candidate Summary from Room Database.
 * Contains key qualifications, experience years, skill tags, and executive summary.
 */
data class CandidateSummary(
    val id: String = "",
    val resumeId: String = "",
    val name: String = "",
    val headlineRole: String = "",
    val email: String = "",
    val experienceYears: Float = 0f,
    val experienceYearsDisplay: String = "",
    val experienceLevel: String = "",
    val keyQualifications: List<String> = emptyList(),
    val technicalSkills: List<String> = emptyList(),
    val toolsAndFrameworks: List<String> = emptyList(),
    val softSkills: List<String> = emptyList(),
    val educationSummary: String = "",
    val executiveSummary: String = "",
    val domains: List<String> = emptyList(),
    val parsedViaAi: Boolean = false,
    val rawResumeText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

