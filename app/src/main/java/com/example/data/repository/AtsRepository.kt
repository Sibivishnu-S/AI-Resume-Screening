package com.example.data.repository

import com.example.data.local.AnalysisResultEntity
import com.example.data.local.AtsDao
import com.example.data.local.CandidateEntity
import com.example.data.local.CandidateSummaryEntity
import com.example.data.local.JobDescriptionEntity
import com.example.data.local.ResumeEntity
import com.example.data.model.AnalysisResult
import com.example.data.model.CandidateRankingItem
import com.example.data.model.CandidateStatus
import com.example.data.model.CandidateSummary
import com.example.data.model.JobDescription
import com.example.data.model.ParsedResume
import com.example.data.nlp.AtsCalculatorEngine
import com.example.data.nlp.LocalNlpParser
import com.example.data.remote.GeminiAiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AtsRepository(
    private val atsDao: AtsDao,
    private val geminiAiService: GeminiAiService = GeminiAiService()
) {

    val allResumes: Flow<List<ParsedResume>> = atsDao.getAllResumes().map { list ->
        list.map { it.toModel() }
    }

    val allJobs: Flow<List<JobDescription>> = atsDao.getAllJobs().map { list ->
        list.map { it.toModel() }
    }

    val allAnalyses: Flow<List<AnalysisResult>> = atsDao.getAllAnalyses().map { list ->
        list.map { it.toModel() }
    }

    val candidateRankings: Flow<List<CandidateRankingItem>> = atsDao.getAllCandidatesSortedByScore().map { list ->
        list.map { it.toModel() }
    }

    val candidateSummaries: Flow<List<CandidateSummary>> = atsDao.getAllCandidateSummaries().map { list ->
        list.map { it.toModel() }
    }

    suspend fun analyzeResumeAgainstJob(resumeText: String, jobText: String): AnalysisResult {
        val parsedResume = LocalNlpParser.parseResumeText(resumeText)
        val parsedJob = LocalNlpParser.parseJobDescriptionText(jobText)

        // Save resume and job to local DB
        val resumeId = "res_${System.currentTimeMillis()}"
        val jobId = "job_${System.currentTimeMillis()}"

        atsDao.insertResume(parsedResume.toEntity(resumeId))
        atsDao.insertJob(parsedJob.toEntity(jobId))

        // Calculate baseline algorithmic ATS score
        var result = AtsCalculatorEngine.calculateAtsScore(parsedResume, parsedJob)

        // Try enhancing with Gemini AI
        try {
            val aiFeedback = geminiAiService.generateAiFeedback(resumeText, jobText)
            if (aiFeedback.isNotBlank() && !aiFeedback.contains("unconfigured")) {
                result = result.copy(aiSummaryFeedback = aiFeedback)
            }
        } catch (e: Exception) {
            // Keep rule-based feedback on network failure
        }

        val analysisId = "anl_${System.currentTimeMillis()}"
        val finalResult = result.copy(id = analysisId)

        // Save Analysis to DB
        atsDao.insertAnalysis(finalResult.toEntity(resumeId, jobId))

        // Save to Candidates Ranking Table
        val candidate = CandidateEntity(
            id = "cand_${System.currentTimeMillis()}",
            name = parsedResume.name,
            email = parsedResume.email,
            jobTitle = parsedJob.title,
            overallAtsScore = finalResult.scoreBreakdown.overallScore,
            skillMatchPercent = 100 - finalResult.skillGapPercentage,
            experienceYears = parsedResume.experience.sumOf { it.durationYears.toDouble() }.toFloat(),
            matchedSkillsCount = finalResult.matchedSkills.size,
            totalRequiredSkills = finalResult.matchedSkills.size + finalResult.missingSkills.size,
            status = CandidateStatus.NEW
        )
        atsDao.insertCandidate(candidate)

        return finalResult
    }

    suspend fun updateCandidateStatus(candidateId: String, newStatus: CandidateStatus) {
        val existing = atsDao.getAllCandidatesSortedByScore().first().find { it.id == candidateId }
        if (existing != null) {
            atsDao.updateCandidate(existing.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun askAiAdvisor(userQuery: String, resumeText: String, jobText: String): String {
        return geminiAiService.chatWithAiAdvisor(userQuery, resumeText, jobText)
    }

    suspend fun parseAndSaveCandidateSummary(resumeId: String): CandidateSummary {
        val resumeEntity = atsDao.getResumeById(resumeId)
            ?: atsDao.getAllResumes().first().firstOrNull()
            ?: throw IllegalArgumentException("No resume found in Room database for id $resumeId")

        val (aiData, isAi) = geminiAiService.parseResumeWithGemini(resumeEntity.rawText)
        val summaryEntity = CandidateSummaryEntity(
            id = "sum_${resumeEntity.id}",
            resumeId = resumeEntity.id,
            candidateName = aiData.candidateName ?: resumeEntity.candidateName,
            headlineRole = aiData.headlineRole ?: "Senior Software Engineer",
            email = aiData.email ?: resumeEntity.email,
            experienceYears = aiData.experienceYears ?: 4.0f,
            experienceYearsDisplay = aiData.experienceYearsDisplay ?: "4.0+ Years",
            experienceLevel = aiData.experienceLevel ?: "Senior",
            keyQualifications = if (!aiData.keyQualifications.isNullOrEmpty()) aiData.keyQualifications else listOf(
                resumeEntity.education.firstOrNull()?.let { "${it.degree}, ${it.institution}" } ?: "B.S. in Computer Science",
                "Demonstrated background in scalable mobile and backend systems",
                "Proven proficiency in cross-functional agile development"
            ),
            technicalSkills = if (!aiData.technicalSkills.isNullOrEmpty()) aiData.technicalSkills else resumeEntity.technicalSkills,
            toolsAndFrameworks = if (!aiData.toolsAndFrameworks.isNullOrEmpty()) aiData.toolsAndFrameworks else resumeEntity.toolsAndFrameworks,
            softSkills = if (!aiData.softSkills.isNullOrEmpty()) aiData.softSkills else resumeEntity.softSkills,
            educationSummary = aiData.educationSummary ?: resumeEntity.education.firstOrNull()?.let { "${it.degree}, ${it.institution}" } ?: "",
            executiveSummary = aiData.executiveSummary ?: "Accomplished engineer with proven capability in scalable architecture.",
            domains = if (!aiData.domains.isNullOrEmpty()) aiData.domains else listOf("Software Engineering"),
            parsedViaAi = isAi,
            rawResumeText = resumeEntity.rawText,
            timestamp = System.currentTimeMillis()
        )
        atsDao.insertCandidateSummary(summaryEntity)
        return summaryEntity.toModel()
    }

    suspend fun parseAllResumesFromDb(): List<CandidateSummary> {
        val resumes = atsDao.getAllResumes().first()
        val results = mutableListOf<CandidateSummary>()
        for (resume in resumes) {
            results.add(parseAndSaveCandidateSummary(resume.id))
        }
        return results
    }

    suspend fun seedSampleDataIfEmpty() {
        val currentJobs = atsDao.getAllJobs().first()
        if (currentJobs.isEmpty()) {
            val sampleJob1 = JobDescription(
                id = "job_sample_1",
                title = "Senior Mobile & Android Engineer (Kotlin)",
                company = "Apex Systems",
                department = "Mobile Engineering",
                requiredSkills = listOf("Kotlin", "Jetpack Compose", "Android", "Room", "REST APIs", "Git", "Coroutines"),
                preferredSkills = listOf("Docker", "CI/CD", "KSP", "Moshi", "Firebase", "Unit Testing"),
                requiredExperienceYears = 3.5f,
                requiredEducation = "B.S. in Computer Science or Software Engineering",
                toolsAndFrameworks = listOf("Android Studio", "Git", "Postman", "Gradle"),
                responsibilities = listOf(
                    "Architect and maintain high-volume Android client applications",
                    "Implement clean MVVM architecture with Kotlin Coroutines & Flow",
                    "Optimize local Room persistence and network caching layer"
                ),
                keywords = listOf("Kotlin", "Compose", "Android", "MVVM", "Coroutines", "Room", "REST", "Architecture", "Testing"),
                rawText = "Senior Android Engineer needed with 3+ years in Kotlin, Jetpack Compose, Room DB, Coroutines, and MVVM architecture."
            )

            val sampleResume1 = ParsedResume(
                name = "Alex Mercer",
                email = "alex.mercer@dev.io",
                phone = "+1 (555) 234-5678",
                linkedIn = "linkedin.com/in/alexmercer-dev",
                gitHub = "github.com/alexmercer",
                portfolio = "alexmercer.dev",
                technicalSkills = listOf("Kotlin", "Jetpack Compose", "Android SDK", "Room DB", "Coroutines", "Flow", "Java", "SQL", "REST APIs"),
                softSkills = listOf("Squad Leadership", "System Design", "Agile Collaboration", "Mentorship"),
                toolsAndFrameworks = listOf("Android Studio", "Git", "Gradle", "Docker", "Postman", "KSP", "Moshi"),
                education = listOf(com.example.data.model.EducationItem("B.S. in Computer Science", "State Tech University", "2021", "3.8")),
                experience = listOf(
                    com.example.data.model.ExperienceItem("Senior Android Engineer", "TechCorp Inc.", 3.0f, "Spearheaded migration of legacy codebase to 100% Kotlin & Jetpack Compose, cutting UI render latency by 35%."),
                    com.example.data.model.ExperienceItem("Mobile Software Developer", "AppStudio Solutions", 1.5f, "Engineered offline-first local cache using Room and Kotlin Flow.")
                ),
                projects = listOf(
                    com.example.data.model.ProjectItem("AI Resume ATS Analyzer", "Created Android app with Room local database and Gemini AI integration.", listOf("Kotlin", "Compose", "Room")),
                    com.example.data.model.ProjectItem("Offline First Mobile Reader", "Implemented reactive database cache layer using Flow.", listOf("Kotlin", "Coroutines"))
                ),
                certifications = listOf("Google Certified Associate Android Developer"),
                languages = listOf("English", "Spanish"),
                achievements = listOf("Top 5% performer on LeetCode Android assessments"),
                rawText = """
                    ALEX MERCER | alex.mercer@dev.io | San Francisco, CA | linkedin.com/in/alexmercer-dev | github.com/alexmercer
                    SUMMARY: Senior Android & Mobile Engineer with 4.5+ years experience in Kotlin, Jetpack Compose, Room, MVVM, REST APIs, and Coroutines.
                    SKILLS: Kotlin, Java, Python, SQL, Jetpack Compose, Room, Docker, Git, CI/CD, REST APIs, Android SDK, Coroutines.
                    EXPERIENCE: Senior Android Engineer at TechCorp (3 yrs), Mobile Developer at AppStudio (1.5 yrs).
                    EDUCATION: B.S. Computer Science, State Tech University (GPA: 3.8/4.0).
                    CERTIFICATIONS: Google Certified Associate Android Developer.
                """.trimIndent()
            )

            val sampleResume2 = ParsedResume(
                name = "Sarah Connor",
                email = "sarah.connor@ai-tech.org",
                phone = "+1 (555) 987-6543",
                linkedIn = "linkedin.com/in/sarah-connor-ai",
                gitHub = "github.com/sconnor",
                portfolio = "sconnor.ai",
                technicalSkills = listOf("Python", "FastAPI", "React", "TypeScript", "PostgreSQL", "Redis", "Docker", "Kubernetes", "PyTorch", "NLP", "LLM"),
                softSkills = listOf("Tech Leadership", "Cross-Team Communication", "Problem Solving", "Strategic Planning"),
                toolsAndFrameworks = listOf("Docker", "Kubernetes", "AWS", "Git", "GitHub Actions", "Terraform", "Postman"),
                education = listOf(com.example.data.model.EducationItem("M.S. in Software Engineering", "National Institute of Technology", "2019", "3.9")),
                experience = listOf(
                    com.example.data.model.ExperienceItem("Lead AI Full Stack Developer", "AI Core Labs", 3.0f, "Architected high-throughput FastAPI microservices serving LLM inference to 200k+ MAU with Redis caching."),
                    com.example.data.model.ExperienceItem("Backend Software Developer", "CloudScale Networks", 2.0f, "Maintained distributed PostgreSQL pipelines and Dockerized microservices.")
                ),
                projects = listOf(
                    com.example.data.model.ProjectItem("Multimodal Resume Parser", "Built automated resume vector retrieval with PyTorch & FastAPI.", listOf("Python", "FastAPI", "Redis"))
                ),
                certifications = listOf("AWS Certified Solutions Architect - Associate"),
                languages = listOf("English", "French"),
                achievements = listOf("Published conference paper on generative AI summarization pipelines"),
                rawText = """
                    SARAH CONNOR | sarah.connor@ai-tech.org | Austin, TX | github.com/sconnor | sconnor.ai
                    SUMMARY: Lead Full Stack & AI Engineer with 5.0 years experience in Python, FastAPI, React, TypeScript, PostgreSQL, Redis, and Gemini AI.
                    SKILLS: Python, FastAPI, React, TypeScript, PostgreSQL, Redis, Docker, Kubernetes, PyTorch, Tailwind CSS, System Design.
                    EXPERIENCE: Lead AI Full Stack Developer at AI Core Labs (3 yrs), Backend Software Developer at CloudScale (2 yrs).
                    EDUCATION: M.S. Software Engineering, National Institute of Technology (GPA: 3.9/4.0).
                    CERTIFICATIONS: AWS Certified Solutions Architect.
                """.trimIndent()
            )

            val sampleResume3 = ParsedResume(
                name = "David Chen",
                email = "dchen@cloud.net",
                phone = "+1 (555) 345-6789",
                linkedIn = "linkedin.com/in/david-chen-cloud",
                gitHub = "github.com/dchen-dev",
                portfolio = "davidchen.io",
                technicalSkills = listOf("Go", "Kubernetes", "AWS", "gRPC", "Kafka", "Docker", "PostgreSQL", "Terraform", "Distributed Systems"),
                softSkills = listOf("Architecture Governance", "Incident Response", "Mentorship", "Documentation"),
                toolsAndFrameworks = listOf("Kubernetes", "AWS", "Terraform", "Prometheus", "Grafana", "Git", "CI/CD"),
                education = listOf(com.example.data.model.EducationItem("B.S. in Computer Engineering", "Pacific University", "2018", "3.7")),
                experience = listOf(
                    com.example.data.model.ExperienceItem("Senior Cloud Backend Architect", "CloudScale Infra", 4.0f, "Designed fault-tolerant event streams with Kafka and Go gRPC services with 99.99% uptime."),
                    com.example.data.model.ExperienceItem("DevOps Systems Engineer", "FinTech Cloud Inc.", 2.0f, "Automated multi-region Kubernetes deployments using Terraform & GitHub Actions.")
                ),
                projects = listOf(
                    com.example.data.model.ProjectItem("Kubernetes Mesh Gateway", "Open-source gRPC load balancer deployed across multi-cluster k8s.", listOf("Go", "Kubernetes"))
                ),
                certifications = listOf("Certified Kubernetes Administrator (CKA)"),
                languages = listOf("English", "Mandarin"),
                achievements = listOf("Reduced cloud compute spend by 28% through autoscaling optimization"),
                rawText = """
                    DAVID CHEN | dchen@cloud.net | Seattle, WA | github.com/dchen-dev | davidchen.io
                    SUMMARY: Senior Cloud Backend Architect with 6.0 years experience in Go, Kubernetes, AWS, gRPC, Kafka, Docker, and distributed microservices.
                    SKILLS: Go, Kubernetes, AWS, gRPC, Kafka, Docker, PostgreSQL, Terraform, Prometheus, CI/CD, Microservices.
                    EXPERIENCE: Senior Cloud Backend Architect at CloudScale Infra (4 yrs), DevOps Engineer at FinTech Cloud (2 yrs).
                    EDUCATION: B.S. Computer Engineering, Pacific University.
                    CERTIFICATIONS: Certified Kubernetes Administrator (CKA).
                """.trimIndent()
            )

            val sampleResume4 = ParsedResume(
                name = "Priya Patel",
                email = "priya.patel@mobile.dev",
                phone = "+1 (555) 456-7890",
                linkedIn = "linkedin.com/in/priya-patel-mobile",
                gitHub = "github.com/priyapatel",
                portfolio = "priyapatel.design",
                technicalSkills = listOf("Kotlin", "Jetpack Compose", "Flutter", "Dart", "Android SDK", "Figma", "REST APIs", "Room DB", "SQLite"),
                softSkills = listOf("UI/UX Design Thinking", "Team Collaboration", "Agile/Scrum", "Fast Prototyping"),
                toolsAndFrameworks = listOf("Android Studio", "VS Code", "Figma", "Git", "Postman", "Firebase"),
                education = listOf(com.example.data.model.EducationItem("B.S. in Information Systems", "Metro Tech College", "2022", "3.85")),
                experience = listOf(
                    com.example.data.model.ExperienceItem("Mobile UI Engineer", "DesignCraft Mobile", 2.5f, "Created accessible Material 3 interfaces in Jetpack Compose and Flutter for top retail clients."),
                    com.example.data.model.ExperienceItem("Junior Android Developer", "PixelWorks Apps", 1.0f, "Implemented responsive phone and tablet layouts with SQLite caching.")
                ),
                projects = listOf(
                    com.example.data.model.ProjectItem("Ecommerce Shopping Kit", "Material 3 Compose template with full cart animations.", listOf("Kotlin", "Compose", "Figma"))
                ),
                certifications = listOf("Meta Certified Front-End Developer"),
                languages = listOf("English", "Hindi"),
                achievements = listOf("Designed app that achieved 4.8-star rating on Google Play with 1M+ downloads"),
                rawText = """
                    PRIYA PATEL | priya.patel@mobile.dev | Chicago, IL | github.com/priyapatel | priyapatel.design
                    SUMMARY: Mobile UI Engineer with 3.5 years experience in Kotlin, Jetpack Compose, Flutter, Figma, Dart, and responsive Material Design.
                    SKILLS: Kotlin, Jetpack Compose, Flutter, Dart, Android SDK, Figma, REST APIs, Room DB, SQLite, Material 3.
                    EXPERIENCE: Mobile UI Engineer at DesignCraft Mobile (2.5 yrs), Junior Android Developer at PixelWorks (1 yr).
                    EDUCATION: B.S. Information Systems, Metro Tech College (GPA: 3.85/4.0).
                """.trimIndent()
            )

            atsDao.insertJob(sampleJob1.toEntity("job_sample_1"))
            atsDao.insertResume(sampleResume1.toEntity("res_sample_1"))
            atsDao.insertResume(sampleResume2.toEntity("res_sample_2"))
            atsDao.insertResume(sampleResume3.toEntity("res_sample_3"))
            atsDao.insertResume(sampleResume4.toEntity("res_sample_4"))

            // Seed initial Candidate Summaries
            val initialSummaries = listOf(
                CandidateSummaryEntity(
                    id = "sum_res_sample_1",
                    resumeId = "res_sample_1",
                    candidateName = "Alex Mercer",
                    headlineRole = "Senior Android Engineer",
                    email = "alex.mercer@dev.io",
                    experienceYears = 4.5f,
                    experienceYearsDisplay = "4.5+ Years",
                    experienceLevel = "Senior",
                    keyQualifications = listOf(
                        "B.S. in Computer Science with 3.8 GPA (State Tech University)",
                        "Spearheaded migration of legacy Java codebase to 100% Kotlin & Jetpack Compose (-35% latency)",
                        "Google Certified Associate Android Developer",
                        "Architected offline-first local cache using Room and Kotlin Coroutines Flow"
                    ),
                    technicalSkills = listOf("Kotlin", "Jetpack Compose", "Android SDK", "Room DB", "Coroutines", "Flow", "REST APIs"),
                    toolsAndFrameworks = listOf("Android Studio", "Git", "Gradle", "Docker", "Postman", "CI/CD"),
                    softSkills = listOf("Squad Leadership", "System Design", "Agile Collaboration", "Mentorship"),
                    educationSummary = "B.S. Computer Science, State Tech University (3.8 GPA)",
                    executiveSummary = "High-performing Senior Android Engineer with 4.5+ years delivering reactive, offline-first mobile solutions in Kotlin and Jetpack Compose.",
                    domains = listOf("Mobile Engineering", "Android Architecture"),
                    parsedViaAi = true,
                    rawResumeText = sampleResume1.rawText
                ),
                CandidateSummaryEntity(
                    id = "sum_res_sample_2",
                    resumeId = "res_sample_2",
                    candidateName = "Sarah Connor",
                    headlineRole = "Lead AI Full Stack Developer",
                    email = "sarah.connor@ai-tech.org",
                    experienceYears = 5.0f,
                    experienceYearsDisplay = "5.0 Years",
                    experienceLevel = "Lead",
                    keyQualifications = listOf(
                        "M.S. in Software Engineering with 3.9 GPA (National Institute)",
                        "Architected high-throughput FastAPI LLM inference services for 200k+ MAU",
                        "AWS Certified Solutions Architect - Associate",
                        "End-to-end React + TypeScript full-stack product engineering"
                    ),
                    technicalSkills = listOf("Python", "FastAPI", "React", "TypeScript", "PostgreSQL", "Redis", "Docker", "Kubernetes", "PyTorch"),
                    toolsAndFrameworks = listOf("Docker", "Kubernetes", "AWS", "Terraform", "GitHub Actions"),
                    softSkills = listOf("Tech Leadership", "Cross-Team Communication", "Problem Solving"),
                    educationSummary = "M.S. Software Engineering, National Institute (3.9 GPA)",
                    executiveSummary = "Lead Full Stack & AI engineer specialized in high-performance Python microservices, distributed caching, and conversational AI integrations.",
                    domains = listOf("AI & Full-Stack", "Cloud Microservices"),
                    parsedViaAi = true,
                    rawResumeText = sampleResume2.rawText
                ),
                CandidateSummaryEntity(
                    id = "sum_res_sample_3",
                    resumeId = "res_sample_3",
                    candidateName = "David Chen",
                    headlineRole = "Senior Cloud Backend Architect",
                    email = "dchen@cloud.net",
                    experienceYears = 6.0f,
                    experienceYearsDisplay = "6.0 Years",
                    experienceLevel = "Staff / Architect",
                    keyQualifications = listOf(
                        "Certified Kubernetes Administrator (CKA)",
                        "Designed fault-tolerant event streams with Kafka and Go gRPC (99.99% uptime)",
                        "Reduced cloud compute infrastructure spend by 28% through dynamic autoscaling",
                        "B.S. in Computer Engineering (Pacific University)"
                    ),
                    technicalSkills = listOf("Go", "Kubernetes", "AWS", "gRPC", "Kafka", "Docker", "PostgreSQL", "Terraform"),
                    toolsAndFrameworks = listOf("Kubernetes", "AWS", "Terraform", "Prometheus", "Grafana", "Git"),
                    softSkills = listOf("Architecture Governance", "Incident Response", "Mentorship"),
                    educationSummary = "B.S. Computer Engineering, Pacific University",
                    executiveSummary = "Distributed systems architect with 6.0 years expertise in ultra-resilient Go microservices, Kubernetes orchestration, and event-driven backends.",
                    domains = listOf("Cloud & Distributed Systems", "DevOps & SRE"),
                    parsedViaAi = true,
                    rawResumeText = sampleResume3.rawText
                ),
                CandidateSummaryEntity(
                    id = "sum_res_sample_4",
                    resumeId = "res_sample_4",
                    candidateName = "Priya Patel",
                    headlineRole = "Mobile UI Engineer",
                    email = "priya.patel@mobile.dev",
                    experienceYears = 3.5f,
                    experienceYearsDisplay = "3.5 Years",
                    experienceLevel = "Senior",
                    keyQualifications = listOf(
                        "B.S. in Information Systems with 3.85 GPA (Metro Tech College)",
                        "Created accessible Material 3 interfaces in Compose and Flutter for top enterprise clients",
                        "Meta Certified Front-End Developer",
                        "Led mobile frontend for application with 1M+ downloads and 4.8 rating"
                    ),
                    technicalSkills = listOf("Kotlin", "Jetpack Compose", "Flutter", "Dart", "Android SDK", "Figma", "REST APIs", "Room DB"),
                    toolsAndFrameworks = listOf("Android Studio", "VS Code", "Figma", "Git", "Postman", "Firebase"),
                    softSkills = listOf("UI/UX Design Thinking", "Team Collaboration", "Agile/Scrum"),
                    educationSummary = "B.S. Information Systems, Metro Tech College (3.85 GPA)",
                    executiveSummary = "Design-focused Mobile Engineer with 3.5 years experience crafting pixel-perfect, accessible, and fluid animations in Compose and Flutter.",
                    domains = listOf("Mobile Engineering", "UI/UX & Design Systems"),
                    parsedViaAi = true,
                    rawResumeText = sampleResume4.rawText
                )
            )
            atsDao.insertCandidateSummaries(initialSummaries)

            // Seed initial ranking candidates
            val sampleCandidates = listOf(
                CandidateEntity("c_1", "Alex Mercer", "alex.mercer@dev.io", "Senior Android Engineer", 88, 90, 4.5f, 7, 8, CandidateStatus.SHORTLISTED),
                CandidateEntity("c_2", "Sarah Connor", "sarah.connor@ai-tech.org", "Lead AI Developer", 84, 85, 5.0f, 6, 8, CandidateStatus.SHORTLISTED),
                CandidateEntity("c_3", "David Chen", "dchen@cloud.net", "Senior Cloud Architect", 76, 75, 6.0f, 5, 8, CandidateStatus.REVIEWING),
                CandidateEntity("c_4", "Priya Patel", "priya.patel@mobile.dev", "Mobile UI Engineer", 82, 80, 3.5f, 6, 8, CandidateStatus.NEW)
            )
            atsDao.insertCandidates(sampleCandidates)
        }
    }


    // Mapping Extension Functions
    private fun ParsedResume.toEntity(id: String) = ResumeEntity(
        id = id,
        candidateName = name,
        email = email,
        phone = phone,
        linkedIn = linkedIn,
        gitHub = gitHub,
        portfolio = portfolio,
        technicalSkills = technicalSkills,
        softSkills = softSkills,
        toolsAndFrameworks = toolsAndFrameworks,
        education = education,
        experience = experience,
        projects = projects,
        certifications = certifications,
        languages = languages,
        achievements = achievements,
        rawText = rawText
    )

    private fun ResumeEntity.toModel() = ParsedResume(
        name = candidateName,
        email = email,
        phone = phone,
        linkedIn = linkedIn,
        gitHub = gitHub,
        portfolio = portfolio,
        technicalSkills = technicalSkills,
        softSkills = softSkills,
        toolsAndFrameworks = toolsAndFrameworks,
        education = education,
        experience = experience,
        projects = projects,
        certifications = certifications,
        languages = languages,
        achievements = achievements,
        rawText = rawText
    )

    private fun JobDescription.toEntity(id: String) = JobDescriptionEntity(
        id = id,
        title = title,
        company = company,
        department = department,
        requiredSkills = requiredSkills,
        preferredSkills = preferredSkills,
        requiredExperienceYears = requiredExperienceYears,
        requiredEducation = requiredEducation,
        toolsAndFrameworks = toolsAndFrameworks,
        responsibilities = responsibilities,
        keywords = keywords,
        rawText = rawText
    )

    private fun JobDescriptionEntity.toModel() = JobDescription(
        id = id,
        title = title,
        company = company,
        department = department,
        requiredSkills = requiredSkills,
        preferredSkills = preferredSkills,
        requiredExperienceYears = requiredExperienceYears,
        requiredEducation = requiredEducation,
        toolsAndFrameworks = toolsAndFrameworks,
        responsibilities = responsibilities,
        keywords = keywords,
        rawText = rawText
    )

    private fun AnalysisResult.toEntity(resumeId: String, jobId: String) = AnalysisResultEntity(
        id = id,
        resumeId = resumeId,
        jobId = jobId,
        candidateName = candidateName,
        jobTitle = jobTitle,
        companyName = companyName,
        scoreBreakdown = scoreBreakdown,
        matchedSkills = matchedSkills,
        missingSkills = missingSkills,
        additionalSkills = additionalSkills,
        skillGapPercentage = skillGapPercentage,
        matchedKeywords = matchedKeywords,
        missingKeywords = missingKeywords,
        aiSuggestions = aiSuggestions,
        aiSummaryFeedback = aiSummaryFeedback,
        estimatedSalaryRange = estimatedSalaryRange,
        interviewQuestions = interviewQuestions
    )

    private fun AnalysisResultEntity.toModel() = AnalysisResult(
        id = id,
        candidateName = candidateName,
        jobTitle = jobTitle,
        companyName = companyName,
        scoreBreakdown = scoreBreakdown,
        matchedSkills = matchedSkills,
        missingSkills = missingSkills,
        additionalSkills = additionalSkills,
        skillGapPercentage = skillGapPercentage,
        matchedKeywords = matchedKeywords,
        missingKeywords = missingKeywords,
        aiSuggestions = aiSuggestions,
        aiSummaryFeedback = aiSummaryFeedback,
        estimatedSalaryRange = estimatedSalaryRange,
        interviewQuestions = interviewQuestions,
        timestamp = createdAt
    )

    private fun CandidateEntity.toModel() = CandidateRankingItem(
        id = id,
        name = name,
        email = email,
        jobTitle = jobTitle,
        overallAtsScore = overallAtsScore,
        skillMatchPercent = skillMatchPercent,
        experienceYears = experienceYears,
        matchedSkillsCount = matchedSkillsCount,
        totalRequiredSkills = totalRequiredSkills,
        status = status
    )

    private fun CandidateSummaryEntity.toModel() = CandidateSummary(
        id = id,
        resumeId = resumeId,
        name = candidateName,
        headlineRole = headlineRole,
        email = email,
        experienceYears = experienceYears,
        experienceYearsDisplay = experienceYearsDisplay,
        experienceLevel = experienceLevel,
        keyQualifications = keyQualifications,
        technicalSkills = technicalSkills,
        toolsAndFrameworks = toolsAndFrameworks,
        softSkills = softSkills,
        educationSummary = educationSummary,
        executiveSummary = executiveSummary,
        domains = domains,
        parsedViaAi = parsedViaAi,
        rawResumeText = rawResumeText,
        timestamp = timestamp
    )

    private fun CandidateSummary.toEntity() = CandidateSummaryEntity(
        id = id,
        resumeId = resumeId,
        candidateName = name,
        headlineRole = headlineRole,
        email = email,
        experienceYears = experienceYears,
        experienceYearsDisplay = experienceYearsDisplay,
        experienceLevel = experienceLevel,
        keyQualifications = keyQualifications,
        technicalSkills = technicalSkills,
        toolsAndFrameworks = toolsAndFrameworks,
        softSkills = softSkills,
        educationSummary = educationSummary,
        executiveSummary = executiveSummary,
        domains = domains,
        parsedViaAi = parsedViaAi,
        rawResumeText = rawResumeText,
        timestamp = timestamp
    )
}

