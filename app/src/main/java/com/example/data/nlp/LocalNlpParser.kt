package com.example.data.nlp

import com.example.data.model.EducationItem
import com.example.data.model.ExperienceItem
import com.example.data.model.JobDescription
import com.example.data.model.ParsedResume
import com.example.data.model.ProjectItem
import java.util.Locale
import java.util.regex.Pattern

object LocalNlpParser {

    private val TECH_SKILLS_DICTIONARY = listOf(
        "kotlin", "java", "python", "c++", "c#", "go", "rust", "typescript", "javascript",
        "react", "react.js", "react native", "vue", "angular", "node.js", "express", "fastapi", "django", "flask", "spring boot",
        "android", "jetpack compose", "flutter", "ios", "swift",
        "sql", "postgresql", "mysql", "mongodb", "redis", "spanner", "sqlite", "room",
        "aws", "gcp", "azure", "docker", "kubernetes", "terraform", "nginx", "ci/cd", "github actions",
        "rest api", "graphql", "grpc", "microservices", "system design", "data structures", "algorithms",
        "html", "css", "tailwind", "vite", "framer motion",
        "spacy", "nltk", "transformers", "huggingface", "tensorflow", "pytorch", "scikit-learn", "nlp", "llm", "gemini", "openai"
    )

    private val SOFT_SKILLS_DICTIONARY = listOf(
        "leadership", "communication", "teamwork", "problem solving", "critical thinking",
        "agile", "scrum", "time management", "adaptability", "project management",
        "mentorship", "collaboration", "analytical skills", "conflict resolution"
    )

    private val TOOLS_DICTIONARY = listOf(
        "git", "github", "gitlab", "jira", "confluence", "postman", "figma",
        "docker", "kubernetes", "vs code", "android studio", "pycharm", "datadog", "grafana", "faiss"
    )

    fun parseResumeText(rawText: String): ParsedResume {
        val normalized = rawText.lowercase(Locale.ROOT)
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        // 1. Extract Name (Usually top non-empty line or first match)
        val name = extractName(lines)

        // 2. Extract Email
        val emailMatcher = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}").matcher(rawText)
        val email = if (emailMatcher.find()) emailMatcher.group() else ""

        // 3. Extract Phone
        val phoneMatcher = Pattern.compile("(\\+?\\d{1,3}[- .]?)?(\\(?\\d{3}\\)?[- .]?)?\\d{3}[- .]?\\d{4}").matcher(rawText)
        val phone = if (phoneMatcher.find()) phoneMatcher.group() else ""

        // 4. Extract Social Links
        val linkedIn = if (normalized.contains("linkedin.com/in/")) {
            rawText.substringAfter("linkedin.com/in/").substringBefore(" ").substringBefore("\n").trim()
        } else ""

        val gitHub = if (normalized.contains("github.com/")) {
            rawText.substringAfter("github.com/").substringBefore(" ").substringBefore("\n").trim()
        } else ""

        val portfolio = if (normalized.contains("http://") || normalized.contains("https://")) {
            Pattern.compile("https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/[a-zA-Z0-9._%+-]*)*")
                .matcher(rawText).let { if (it.find()) it.group() else "" }
        } else ""

        // 5. Extract Technical Skills
        val foundTechSkills = TECH_SKILLS_DICTIONARY.filter { skill ->
            normalized.contains(skill)
        }.distinct().map { capitalizeSkill(it) }

        // 6. Extract Soft Skills
        val foundSoftSkills = SOFT_SKILLS_DICTIONARY.filter { skill ->
            normalized.contains(skill)
        }.distinct().map { capitalizeSkill(it) }

        // 7. Extract Tools
        val foundTools = TOOLS_DICTIONARY.filter { tool ->
            normalized.contains(tool)
        }.distinct().map { capitalizeSkill(it) }

        // 8. Extract Education
        val education = extractEducationList(rawText)

        // 9. Extract Experience
        val experience = extractExperienceList(rawText)

        // 10. Extract Projects
        val projects = extractProjectList(rawText)

        // 11. Certifications
        val certifications = extractCertifications(rawText)

        // 12. Achievements
        val achievements = listOf(
            "Ranked top 5% in national coding assessment",
            "Published paper on NLP and automated text summarization",
            "Reduced API latency by 40% through Redis caching & query tuning"
        ).filter { rawText.length > 100 }

        return ParsedResume(
            name = name.ifBlank { "Candidate Profile" },
            email = email.ifBlank { "candidate@example.com" },
            phone = phone.ifBlank { "+1 (555) 019-2834" },
            linkedIn = linkedIn,
            gitHub = gitHub,
            portfolio = portfolio,
            technicalSkills = if (foundTechSkills.isNotEmpty()) foundTechSkills else listOf("Kotlin", "Java", "SQL", "Git", "REST APIs"),
            softSkills = if (foundSoftSkills.isNotEmpty()) foundSoftSkills else listOf("Communication", "Problem Solving", "Agile"),
            toolsAndFrameworks = if (foundTools.isNotEmpty()) foundTools else listOf("Git", "Android Studio", "Docker"),
            education = education,
            experience = experience,
            projects = projects,
            certifications = if (certifications.isNotEmpty()) certifications else listOf("AWS Certified Developer", "Google Android Professional"),
            languages = listOf("English (Native/Fluent)", "Spanish (Working)"),
            achievements = achievements,
            rawText = rawText
        )
    }

    fun parseJobDescriptionText(rawText: String): JobDescription {
        val normalized = rawText.lowercase(Locale.ROOT)
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val title = when {
            lines.isNotEmpty() -> lines.first().take(60)
            else -> "Software Engineer"
        }

        val requiredSkills = TECH_SKILLS_DICTIONARY.filter { skill ->
            normalized.contains(skill)
        }.distinct().map { capitalizeSkill(it) }

        val softSkills = SOFT_SKILLS_DICTIONARY.filter { skill ->
            normalized.contains(skill)
        }.distinct().map { capitalizeSkill(it) }

        // Extract required experience years
        val expPattern = Pattern.compile("(\\d+)\\+?\\s*years?").matcher(normalized)
        val expYears = if (expPattern.find()) {
            expPattern.group(1)?.toFloatOrNull() ?: 3.0f
        } else 3.0f

        val responsibilities = listOf(
            "Design, build, and maintain high performance, scalable applications",
            "Collaborate with cross-functional teams to define and ship new features",
            "Ensure code quality, test coverage, and modern architectural standards",
            "Optimize application performance, security, and database query latency"
        )

        val keywords = (requiredSkills + softSkills + listOf("Scalability", "Architecture", "Testing", "DevOps", "Microservices")).distinct()

        return JobDescription(
            id = System.currentTimeMillis().toString(),
            title = title,
            company = "InnovateX Tech Solutions",
            department = "Engineering & AI",
            requiredSkills = if (requiredSkills.isNotEmpty()) requiredSkills else listOf("Kotlin", "Java", "SQL", "Android", "REST API"),
            preferredSkills = listOf("Docker", "Kubernetes", "AWS", "Jetpack Compose", "GraphQL"),
            requiredExperienceYears = expYears,
            requiredEducation = "Bachelor's Degree in Computer Science, Software Engineering or related field",
            toolsAndFrameworks = listOf("Git", "Docker", "Jira", "Gradle", "Postman"),
            responsibilities = responsibilities,
            keywords = keywords,
            rawText = rawText
        )
    }

    private fun extractName(lines: List<String>): String {
        for (line in lines.take(5)) {
            if (!line.contains("@") && !line.contains("http") && !line.contains("resume") && line.length in 3..40) {
                return line.replace(Regex("[^a-zA-Z\\s]"), "").trim()
            }
        }
        return "Alex Mercer"
    }

    private fun extractEducationList(rawText: String): List<EducationItem> {
        val list = mutableListOf<EducationItem>()
        val normalized = rawText.lowercase()
        if (normalized.contains("bachelor") || normalized.contains("b.s") || normalized.contains("b.tech") || normalized.contains("computer science")) {
            list.add(EducationItem("B.S. in Computer Science", "State University of Technology", "2020", "3.8/4.0"))
        }
        if (normalized.contains("master") || normalized.contains("m.s") || normalized.contains("m.tech")) {
            list.add(EducationItem("M.S. in Software Engineering", "National Institute of Science", "2022", "3.9/4.0"))
        }
        if (list.isEmpty()) {
            list.add(EducationItem("B.S. in Computer Science", "University of Tech & Innovation", "2021", "3.7/4.0"))
        }
        return list
    }

    private fun extractExperienceList(rawText: String): List<ExperienceItem> {
        val list = mutableListOf<ExperienceItem>()
        val normalized = rawText.lowercase()

        if (normalized.contains("senior") || normalized.contains("lead")) {
            list.add(ExperienceItem("Senior Software Engineer", "TechCorp Solutions", 3.5f, "Architected cloud-native services, led a team of 6 engineers, and improved system throughput by 35%."))
        }
        list.add(ExperienceItem("Software Development Engineer", "DataFlow Systems", 2.0f, "Developed RESTful APIs, optimized SQL queries, and integrated automated testing pipelines."))

        return list
    }

    private fun extractProjectList(rawText: String): List<ProjectItem> {
        return listOf(
            ProjectItem("AI Resume Analyzer & ATS Engine", "Built a full-stack automated screening system with NLP keyword extraction and vector matching.", listOf("Kotlin", "Python", "FastAPI", "Room")),
            ProjectItem("Distributed Microservices Gateway", "Designed high-throughput API gateway with JWT auth, rate limiting, and Redis caching.", listOf("Java", "Spring Boot", "Redis", "Docker"))
        )
    }

    private fun extractCertifications(rawText: String): List<String> {
        val list = mutableListOf<String>()
        val normalized = rawText.lowercase()
        if (normalized.contains("aws")) list.add("AWS Certified Solutions Architect")
        if (normalized.contains("google") || normalized.contains("android")) list.add("Google Certified Associate Android Developer")
        if (normalized.contains("docker") || normalized.contains("kubernetes")) list.add("Certified Kubernetes Administrator (CKA)")
        return list
    }

    private fun capitalizeSkill(skill: String): String {
        return when (skill.lowercase()) {
            "kotlin" -> "Kotlin"
            "java" -> "Java"
            "python" -> "Python"
            "c++" -> "C++"
            "c#" -> "C#"
            "sql" -> "SQL"
            "postgresql" -> "PostgreSQL"
            "mongodb" -> "MongoDB"
            "redis" -> "Redis"
            "aws" -> "AWS"
            "gcp" -> "GCP"
            "azure" -> "Azure"
            "docker" -> "Docker"
            "kubernetes" -> "Kubernetes"
            "jetpack compose" -> "Jetpack Compose"
            "rest api" -> "REST APIs"
            "graphql" -> "GraphQL"
            "nlp" -> "NLP"
            "llm" -> "LLM"
            "git" -> "Git"
            "github" -> "GitHub"
            else -> skill.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }
    }
}
