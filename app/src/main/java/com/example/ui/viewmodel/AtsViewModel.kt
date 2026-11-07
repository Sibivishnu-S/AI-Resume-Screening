package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.AnalysisResult
import com.example.data.model.CandidateRankingItem
import com.example.data.model.CandidateStatus
import com.example.data.model.CandidateSummary
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSender
import com.example.data.repository.AtsRepository
import com.example.util.ItextPdfReportGenerator
import com.example.util.PdfReportGenerator
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class CandidateSortOption {
    ATS_SCORE_DESC, EXPERIENCE_DESC, SKILL_MATCH_DESC, NAME_ASC
}

class AtsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AtsRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = AtsRepository(database.atsDao())

        // Seed default sample data on launch if DB is empty
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    // Input States
    val resumeInputText = MutableStateFlow(
        """
        ALEX MERCER
        alex.mercer@dev.io | +1 (555) 234-5678 | San Francisco, CA
        LinkedIn: linkedin.com/in/alexmercer-dev | GitHub: github.com/alexmercer | Portfolio: alexmercer.dev

        PROFESSIONAL SUMMARY
        Senior Software Engineer with 4.5+ years of experience specializing in Kotlin, Android development, Jetpack Compose, and cloud backend microservices. Proven track record in building offline-first architectures, Room local databases, REST APIs, and leading cross-functional engineering squads.

        TECHNICAL SKILLS
        • Languages: Kotlin, Java, Python, SQL, C++, TypeScript
        • Mobile & Frameworks: Jetpack Compose, Android SDK, Room DB, Coroutines, Flow, KSP, Retrofit, Moshi
        • Backend & Cloud: Node.js, Spring Boot, REST APIs, PostgreSQL, Redis, Docker, Kubernetes, AWS, CI/CD
        • Tools & Practices: Git, GitHub Actions, Jira, Unit Testing, Robolectric, MVVM Architecture, Clean Code

        EXPERIENCE
        Senior Android Engineer | TechCorp Inc. | 2022 – Present
        • Speared the migration of legacy Java codebase to 100% Kotlin with Jetpack Compose, reducing UI render cycles by 35%.
        • Architected offline-first local cache using Room and Kotlin Coroutines Flow, improving app response time during network drops.
        • Mentored 4 junior developers and established CI/CD automated testing standards using GitHub Actions and Robolectric.

        Mobile Software Developer | AppStudio Solutions | 2020 – 2022
        • Designed and integrated high-throughput RESTful API endpoints for financial payment mobile apps.
        • Optimized SQL database queries and local storage indexes, reducing crash rates by 25%.

        EDUCATION
        B.S. in Computer Science | State Tech University | GPA: 3.8/4.0 | Graduated 2020

        PROJECTS
        AI Resume Screening & ATS System | Kotlin, Jetpack Compose, Room, Gemini API
        • Developed full-stack resume parser with local NLP dictionary matching, circular ATS score metrics, and Gemini LLM advisor.
        """.trimIndent()
    )

    val jobInputText = MutableStateFlow(
        """
        JOB TITLE: Senior Mobile & Android Engineer (Kotlin & AI Solutions)
        COMPANY: Apex Systems Inc.
        DEPARTMENT: Core Mobile & Platform Engineering

        JOB DESCRIPTION
        Apex Systems is seeking a talented Senior Android Engineer to lead the architecture and development of our next-generation mobile applications. You will work closely with product and AI teams to deliver seamless, ultra-responsive user interfaces and robust local persistence models.

        REQUIRED SKILLS & QUALIFICATIONS
        • 3+ years of professional experience in Kotlin and modern Android SDK development.
        • Expertise in Jetpack Compose, Reactive UI, and MVVM / Clean Architecture.
        • Deep knowledge of Room database, Kotlin Coroutines, Flow, and local offline persistence.
        • Experience with RESTful APIs, Retrofit, Moshi, JSON serialization, and OkHttp.
        • Proficient with Git version control, CI/CD pipelines, and automated testing frameworks.
        • Strong problem-solving, communication, and agile team collaboration skills.

        PREFERRED SKILLS & NICE-TO-HAVES
        • Familiarity with Docker, Kubernetes, AWS cloud services, or FastAPI backend services.
        • Hands-on experience with AI model integration (Gemini API, ONNX, or TensorFlow Lite).
        • Experience with KSP annotation processing and version catalog Gradle management.

        RESPONSIBILITIES
        • Design, build, and maintain production-grade Android features in 100% Kotlin.
        • Collaborate with design and product stakeholders to ship responsive, accessible UI layouts.
        • Ensure optimal memory footprint, rendering speed, and network caching performance.
        """.trimIndent()
    )

    // Current Analysis Result
    private val _currentAnalysis = MutableStateFlow<AnalysisResult?>(null)
    val currentAnalysis: StateFlow<AnalysisResult?> = _currentAnalysis.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _userMessageToast = MutableStateFlow<String?>(null)
    val userMessageToast: StateFlow<String?> = _userMessageToast.asStateFlow()

    // Sorting Option for Candidates
    private val _sortOption = MutableStateFlow(CandidateSortOption.ATS_SCORE_DESC)
    val sortOption: StateFlow<CandidateSortOption> = _sortOption.asStateFlow()

    // Candidates List with Reactive Sorting
    val candidateRankings: StateFlow<List<CandidateRankingItem>> = combine(
        repository.candidateRankings,
        _sortOption
    ) { candidates, sort ->
        when (sort) {
            CandidateSortOption.ATS_SCORE_DESC -> candidates.sortedByDescending { it.overallAtsScore }
            CandidateSortOption.EXPERIENCE_DESC -> candidates.sortedByDescending { it.experienceYears }
            CandidateSortOption.SKILL_MATCH_DESC -> candidates.sortedByDescending { it.skillMatchPercent }
            CandidateSortOption.NAME_ASC -> candidates.sortedBy { it.name }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analysisHistory: StateFlow<List<AnalysisResult>> = repository.allAnalyses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Candidate Summaries parsed from Room via Gemini
    val candidateSummaries: StateFlow<List<CandidateSummary>> = repository.candidateSummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isParsingCandidate = MutableStateFlow<String?>(null)
    val isParsingCandidate: StateFlow<String?> = _isParsingCandidate.asStateFlow()

    private val _isBatchParsing = MutableStateFlow(false)
    val isBatchParsing: StateFlow<Boolean> = _isBatchParsing.asStateFlow()

    private val _selectedCandidateSummary = MutableStateFlow<CandidateSummary?>(null)
    val selectedCandidateSummary: StateFlow<CandidateSummary?> = _selectedCandidateSummary.asStateFlow()

    private val _summaryFilterQuery = MutableStateFlow("")
    val summaryFilterQuery: StateFlow<String> = _summaryFilterQuery.asStateFlow()

    private val _summaryFilterCategory = MutableStateFlow("All")
    val summaryFilterCategory: StateFlow<String> = _summaryFilterCategory.asStateFlow()

    val filteredCandidateSummaries: StateFlow<List<CandidateSummary>> = combine(
        candidateSummaries,
        _summaryFilterQuery,
        _summaryFilterCategory
    ) { summaries, query, category ->
        summaries.filter { candidate ->
            val matchesCategory = when (category) {
                "All" -> true
                "Senior" -> candidate.experienceYears >= 4f
                "Mobile" -> candidate.domains.any { it.contains("Mobile", ignoreCase = true) } ||
                        candidate.technicalSkills.any { it.contains("Android", true) || it.contains("Compose", true) || it.contains("Flutter", true) }
                "Backend / AI" -> candidate.domains.any { it.contains("AI", true) || it.contains("Cloud", true) } ||
                        candidate.technicalSkills.any { it.contains("Python", true) || it.contains("FastAPI", true) || it.contains("Go", true) }
                "Lead" -> candidate.experienceLevel.contains("Lead", ignoreCase = true) || candidate.experienceLevel.contains("Staff", ignoreCase = true)
                else -> true
            }

            val matchesQuery = if (query.isBlank()) true else {
                candidate.name.contains(query, ignoreCase = true) ||
                candidate.headlineRole.contains(query, ignoreCase = true) ||
                candidate.technicalSkills.any { it.contains(query, ignoreCase = true) } ||
                candidate.toolsAndFrameworks.any { it.contains(query, ignoreCase = true) } ||
                candidate.keyQualifications.any { it.contains(query, ignoreCase = true) }
            }

            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Chat Assistant Messages
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "msg_1",
                sender = ChatSender.AI_ASSISTANT,
                text = "Hello! I am your AI Career & HR Advisor. Ask me anything about optimizing your resume for ATS scanners, addressing skill gaps, or preparing for interviews!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun analyzeResume() {
        val resumeText = resumeInputText.value.trim()
        val jobText = jobInputText.value.trim()

        if (resumeText.isBlank()) {
            _userMessageToast.value = "Please enter or select a Resume to evaluate."
            return
        }
        if (jobText.isBlank()) {
            _userMessageToast.value = "Please enter or select a Job Description."
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val result = repository.analyzeResumeAgainstJob(resumeText, jobText)
                _currentAnalysis.value = result
                _userMessageToast.value = "ATS Evaluation Complete! Score: ${result.scoreBreakdown.overallScore}/100"
            } catch (e: Exception) {
                _userMessageToast.value = "Error during analysis: ${e.localizedMessage}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun updateCandidateStatus(candidateId: String, newStatus: CandidateStatus) {
        viewModelScope.launch {
            repository.updateCandidateStatus(candidateId, newStatus)
            _userMessageToast.value = "Candidate status updated to ${newStatus.name}"
        }
    }

    fun setSortOption(option: CandidateSortOption) {
        _sortOption.value = option
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = ChatSender.USER,
            text = text
        )
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isChatLoading.value = true
            try {
                val response = repository.askAiAdvisor(text, resumeInputText.value, jobInputText.value)
                val aiMsg = ChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    sender = ChatSender.AI_ASSISTANT,
                    text = response
                )
                _chatMessages.value = _chatMessages.value + aiMsg
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    sender = ChatSender.AI_ASSISTANT,
                    text = "Sorry, I ran into an error: ${e.localizedMessage}"
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun selectSampleResume(type: String) {
        resumeInputText.value = when (type) {
            "Senior Mobile Developer" -> """
                ALEX MERCER
                alex.mercer@dev.io | +1 (555) 234-5678 | San Francisco, CA
                LinkedIn: linkedin.com/in/alexmercer-dev | GitHub: github.com/alexmercer

                SUMMARY: Senior Android & Mobile Engineer with 4.5+ years experience in Kotlin, Jetpack Compose, Room, MVVM, REST APIs, and Coroutines.
                SKILLS: Kotlin, Java, Python, SQL, Jetpack Compose, Room, Docker, Git, CI/CD, REST APIs, Android SDK, Coroutines.
                EXPERIENCE: Senior Android Engineer at TechCorp (3 yrs), Mobile Developer at AppStudio (1.5 yrs).
                EDUCATION: B.S. Computer Science, State Tech University.
            """.trimIndent()
            "Full Stack AI Developer" -> """
                SARAH CONNOR
                sarah.connor@ai-tech.org | +1 (555) 987-6543 | Austin, TX
                GitHub: github.com/sconnor | Portfolio: sconnor.ai

                SUMMARY: Full Stack Engineer with 5 years in Python, FastAPI, React, TypeScript, PostgreSQL, Redis, spaCy, and Gemini AI integrations.
                SKILLS: Python, FastAPI, React, TypeScript, PostgreSQL, Redis, Docker, Kubernetes, PyTorch, Tailwind CSS, System Design.
                EXPERIENCE: Lead AI Full Stack Developer at AI Core Labs (3 yrs), Backend Software Developer at CloudScale (2 yrs).
                EDUCATION: M.S. Software Engineering, National Institute.
            """.trimIndent()
            else -> """
                JORDAN LEE
                jordan.lee@dev.net | +1 (555) 345-6789 | Seattle, WA

                SUMMARY: Junior Software Engineer with 1 year experience in Java, HTML, CSS, JavaScript, and basic SQL databases.
                SKILLS: Java, C++, JavaScript, HTML, CSS, Git, MySQL, Problem Solving.
                EXPERIENCE: Software Intern at ByteWorks (1 yr).
                EDUCATION: B.S. Information Technology, City College.
            """.trimIndent()
        }
    }

    fun selectSampleJob(type: String) {
        jobInputText.value = when (type) {
            "Senior Android Engineer" -> """
                JOB TITLE: Senior Android Engineer
                COMPANY: Apex Systems Inc.
                REQUIRED SKILLS: Kotlin, Jetpack Compose, Room, Coroutines, REST APIs, Git, MVVM Architecture.
                PREFERRED SKILLS: Docker, KSP, Firebase, CI/CD, Unit Testing.
                EXPERIENCE: 3.5+ years in Android mobile development.
                DUTIES: Lead Android app architecture, optimize Room local storage, and ship clean Jetpack Compose UI.
            """.trimIndent()
            "AI Full Stack Developer" -> """
                JOB TITLE: AI Full Stack Software Engineer
                COMPANY: MindScale AI
                REQUIRED SKILLS: Python, FastAPI, React, TypeScript, PostgreSQL, Redis, Docker, System Design.
                PREFERRED SKILLS: spaCy, NLP, Vector DB, HuggingFace, Tailwind CSS, Gemini API.
                EXPERIENCE: 4+ years in full stack and AI application engineering.
                DUTIES: Build scalable FastAPI backends, integrate LLM services, and construct responsive React UI dashboards.
            """.trimIndent()
            else -> """
                JOB TITLE: Backend Java Developer
                COMPANY: Enterprise Financial Group
                REQUIRED SKILLS: Java, Spring Boot, SQL, PostgreSQL, REST APIs, Git.
                PREFERRED SKILLS: Microservices, Redis, Kafka, Docker.
                EXPERIENCE: 2+ years in Java enterprise backends.
                DUTIES: Maintain high-throughput Spring Boot microservices and optimize SQL database queries.
            """.trimIndent()
        }
    }

    fun clearToast() {
        _userMessageToast.value = null
    }

    fun setCurrentAnalysis(analysis: AnalysisResult) {
        _currentAnalysis.value = analysis
    }

    fun exportPdfReport(context: Context, analysis: AnalysisResult, onResult: (File?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingPdf.value = true
            try {
                val pdfFile = ItextPdfReportGenerator.generateAnalysisReport(context, analysis)
                
                // Verify binary PDF blob format (%PDF- magic header)
                if (pdfFile.exists() && pdfFile.length() >= 4) {
                    val headerBytes = ByteArray(5)
                    pdfFile.inputStream().use { input -> input.read(headerBytes) }
                    val headerStr = String(headerBytes, Charsets.US_ASCII)
                    if (!headerStr.startsWith("%PDF")) {
                        throw IllegalStateException("Server or generator returned invalid MIME content type. Expected binary application/pdf blob.")
                    }
                } else {
                    throw IllegalStateException("Generated file is empty or missing.")
                }

                withContext(Dispatchers.Main) {
                    _userMessageToast.value = "PDF Exported: ${pdfFile.name}"
                    onResult(pdfFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _userMessageToast.value = "Failed to export PDF: ${e.localizedMessage}"
                    onResult(null)
                }
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    fun exportCandidateSummaryPdf(context: Context, summary: CandidateSummary, onResult: (File?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingPdf.value = true
            try {
                val matchedAnalysis = analysisHistory.value.find {
                    it.candidateName.equals(summary.name, ignoreCase = true)
                }
                val pdfFile = ItextPdfReportGenerator.generateCandidateReport(context, summary, matchedAnalysis)

                if (pdfFile.exists() && pdfFile.length() >= 4) {
                    val headerBytes = ByteArray(5)
                    pdfFile.inputStream().use { input -> input.read(headerBytes) }
                    val headerStr = String(headerBytes, Charsets.US_ASCII)
                    if (!headerStr.startsWith("%PDF")) {
                        throw IllegalStateException("Generated file is not a valid binary application/pdf.")
                    }
                } else {
                    throw IllegalStateException("Generated file is empty or missing.")
                }

                withContext(Dispatchers.Main) {
                    _userMessageToast.value = "Candidate Dossier Exported: ${pdfFile.name}"
                    onResult(pdfFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _userMessageToast.value = "Failed to export PDF: ${e.localizedMessage}"
                    onResult(null)
                }
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    fun parseCandidateWithGemini(resumeId: String) {
        viewModelScope.launch {
            _isParsingCandidate.value = resumeId
            try {
                val summary = repository.parseAndSaveCandidateSummary(resumeId)
                _userMessageToast.value = "Parsed ${summary.name} with Gemini AI!"
            } catch (e: Exception) {
                _userMessageToast.value = "Parsing error: ${e.localizedMessage}"
            } finally {
                _isParsingCandidate.value = null
            }
        }
    }

    fun parseAllResumesWithGemini() {
        viewModelScope.launch {
            _isBatchParsing.value = true
            try {
                val list = repository.parseAllResumesFromDb()
                _userMessageToast.value = "Parsed ${list.size} candidate resumes with Gemini AI!"
            } catch (e: Exception) {
                _userMessageToast.value = "Batch parsing error: ${e.localizedMessage}"
            } finally {
                _isBatchParsing.value = false
            }
        }
    }

    fun selectCandidateSummary(summary: CandidateSummary?) {
        _selectedCandidateSummary.value = summary
    }

    fun setSummaryFilterQuery(query: String) {
        _summaryFilterQuery.value = query
    }

    fun setSummaryFilterCategory(category: String) {
        _summaryFilterCategory.value = category
    }

    fun loadCandidateIntoAnalyzer(summary: CandidateSummary) {
        if (summary.rawResumeText.isNotBlank()) {
            resumeInputText.value = summary.rawResumeText
        } else {
            resumeInputText.value = """
                ${summary.name.uppercase()}
                ${summary.email}
                ROLE: ${summary.headlineRole} (${summary.experienceYearsDisplay})

                QUALIFICATIONS:
                ${summary.keyQualifications.joinToString("\n• ", prefix = "• ")}

                SKILLS:
                ${summary.technicalSkills.joinToString(", ")}
            """.trimIndent()
        }
        _userMessageToast.value = "Loaded ${summary.name}'s resume into ATS Analyzer."
    }
}

