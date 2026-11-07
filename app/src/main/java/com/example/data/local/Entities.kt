package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AtsScoreBreakdown
import com.example.data.model.CandidateStatus
import com.example.data.model.EducationItem
import com.example.data.model.ExperienceItem
import com.example.data.model.InterviewQuestion
import com.example.data.model.ProjectItem

@Entity(tableName = "resumes")
data class ResumeEntity(
    @PrimaryKey val id: String,
    val candidateName: String,
    val email: String,
    val phone: String,
    val linkedIn: String,
    val gitHub: String,
    val portfolio: String,
    val technicalSkills: List<String>,
    val softSkills: List<String>,
    val toolsAndFrameworks: List<String>,
    val education: List<EducationItem>,
    val experience: List<ExperienceItem>,
    val projects: List<ProjectItem>,
    val certifications: List<String>,
    val languages: List<String>,
    val achievements: List<String>,
    val rawText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "job_descriptions")
data class JobDescriptionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val company: String,
    val department: String,
    val requiredSkills: List<String>,
    val preferredSkills: List<String>,
    val requiredExperienceYears: Float,
    val requiredEducation: String,
    val toolsAndFrameworks: List<String>,
    val responsibilities: List<String>,
    val keywords: List<String>,
    val rawText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "analysis_results")
data class AnalysisResultEntity(
    @PrimaryKey val id: String,
    val resumeId: String,
    val jobId: String,
    val candidateName: String,
    val jobTitle: String,
    val companyName: String,
    val scoreBreakdown: AtsScoreBreakdown,
    val matchedSkills: List<String>,
    val missingSkills: List<String>,
    val additionalSkills: List<String>,
    val skillGapPercentage: Int,
    val matchedKeywords: List<String>,
    val missingKeywords: List<String>,
    val aiSuggestions: List<String>,
    val aiSummaryFeedback: String,
    val estimatedSalaryRange: String,
    val interviewQuestions: List<InterviewQuestion>,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "candidates")
data class CandidateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val jobTitle: String,
    val overallAtsScore: Int,
    val skillMatchPercent: Int,
    val experienceYears: Float,
    val matchedSkillsCount: Int,
    val totalRequiredSkills: Int,
    val status: CandidateStatus = CandidateStatus.NEW,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "candidate_summaries")
data class CandidateSummaryEntity(
    @PrimaryKey val id: String,
    val resumeId: String,
    val candidateName: String,
    val headlineRole: String,
    val email: String,
    val experienceYears: Float,
    val experienceYearsDisplay: String,
    val experienceLevel: String,
    val keyQualifications: List<String>,
    val technicalSkills: List<String>,
    val toolsAndFrameworks: List<String>,
    val softSkills: List<String>,
    val educationSummary: String,
    val executiveSummary: String,
    val domains: List<String>,
    val parsedViaAi: Boolean,
    val rawResumeText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

