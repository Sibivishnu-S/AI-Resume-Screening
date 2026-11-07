package com.example

import com.example.data.nlp.AtsCalculatorEngine
import com.example.data.nlp.LocalNlpParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AtsEngineUnitTest {

    @Test
    fun testNlpParserExtractsSkillsAndContactInfo() {
        val sampleResumeText = """
            JOHN DOE
            john.doe@tech.com | +1 (555) 123-4567 | San Francisco, CA
            
            SKILLS
            Kotlin, Java, Python, SQL, Docker, Jetpack Compose, REST APIs, Git
            
            EXPERIENCE
            Senior Software Engineer at ACME Corp (3 years)
        """.trimIndent()

        val parsedResume = LocalNlpParser.parseResumeText(sampleResumeText)

        assertEquals("JOHN DOE", parsedResume.name)
        assertEquals("john.doe@tech.com", parsedResume.email)
        assertTrue(parsedResume.technicalSkills.contains("Kotlin"))
        assertTrue(parsedResume.technicalSkills.contains("Docker"))
    }

    @Test
    fun testAtsScoreCalculationHighMatch() {
        val sampleResumeText = """
            ALEX MERCER
            alex@dev.io | +1 (555) 234-5678
            SKILLS: Kotlin, Jetpack Compose, Room, Coroutines, REST APIs, Git, Docker
            EXPERIENCE: Senior Android Developer at TechCorp (4 years)
            EDUCATION: B.S. in Computer Science, State Tech University
            PROJECTS: AI Resume Screening Mobile App with Compose and Room
        """.trimIndent()

        val sampleJobText = """
            Senior Android Engineer
            REQUIRED SKILLS: Kotlin, Jetpack Compose, Room, Coroutines, REST APIs
            PREFERRED SKILLS: Docker, Git
            EXPERIENCE: 3+ years required
        """.trimIndent()

        val parsedResume = LocalNlpParser.parseResumeText(sampleResumeText)
        val parsedJob = LocalNlpParser.parseJobDescriptionText(sampleJobText)

        val result = AtsCalculatorEngine.calculateAtsScore(parsedResume, parsedJob)

        assertNotNull(result)
        assertTrue("ATS score should be high for matching skills", result.scoreBreakdown.overallScore >= 75)
        assertTrue(result.matchedSkills.contains("Kotlin"))
    }

    @Test
    fun testPdfSanitizeFileName() {
        val fileName = com.example.util.PdfReportGenerator.sanitizeFileName("John Doe, Jr.", "Software Engineer / Lead!")
        assertEquals("ATS_Screening_John_Doe_Jr_Software_Engineer_Lead.pdf", fileName)
        assertTrue(fileName.endsWith(".pdf"))
    }

    @Test
    fun testCandidateSummaryParsing() {
        val sampleResumeText = """
            ALEX MERCER
            alex.mercer@dev.io | San Francisco, CA
            SUMMARY: Senior Android Developer with 4.5 years experience in Kotlin, Jetpack Compose, Room, MVVM, and REST APIs.
            EDUCATION: B.S. in Computer Science, State Tech University (2021)
            EXPERIENCE: Senior Android Engineer at TechCorp (3 yrs), Mobile Developer at AppStudio (1.5 yrs).
            SKILLS: Kotlin, Java, Python, Room, Jetpack Compose, Docker, Git.
        """.trimIndent()

        val parsed = LocalNlpParser.parseResumeText(sampleResumeText)
        assertTrue(parsed.name.equals("Alex Mercer", ignoreCase = true))
        assertTrue(parsed.technicalSkills.contains("Kotlin"))
        assertTrue(parsed.technicalSkills.contains("Room"))
        assertTrue(parsed.technicalSkills.contains("Jetpack Compose"))
        assertTrue(parsed.experience.isNotEmpty())
    }

    @Test
    fun testItextPdfSanitizeFileName() {
        val fileName = com.example.util.ItextPdfReportGenerator.sanitizeFileName("Sarah Connor, Ph.D.", "Lead AI Engineer / Architect!")
        assertEquals("ATS_Executive_Report_Sarah_Connor_Ph_D_Lead_AI_Engineer_Architect.pdf", fileName)
        assertTrue(fileName.endsWith(".pdf"))
    }
}



