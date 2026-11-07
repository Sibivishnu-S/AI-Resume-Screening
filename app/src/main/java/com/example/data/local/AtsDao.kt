package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AtsDao {

    // Resumes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: ResumeEntity)

    @Query("SELECT * FROM resumes ORDER BY createdAt DESC")
    fun getAllResumes(): Flow<List<ResumeEntity>>

    @Query("SELECT * FROM resumes WHERE id = :id")
    suspend fun getResumeById(id: String): ResumeEntity?

    // Job Descriptions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobDescriptionEntity)

    @Query("SELECT * FROM job_descriptions ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<JobDescriptionEntity>>

    @Query("SELECT * FROM job_descriptions WHERE id = :id")
    suspend fun getJobById(id: String): JobDescriptionEntity?

    // Analysis Results
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisResultEntity)

    @Query("SELECT * FROM analysis_results ORDER BY createdAt DESC")
    fun getAllAnalyses(): Flow<List<AnalysisResultEntity>>

    @Query("SELECT * FROM analysis_results WHERE id = :id")
    suspend fun getAnalysisById(id: String): AnalysisResultEntity?

    @Query("SELECT * FROM analysis_results WHERE resumeId = :resumeId AND jobId = :jobId LIMIT 1")
    suspend fun getAnalysisForResumeAndJob(resumeId: String, jobId: String): AnalysisResultEntity?

    // Candidate Ranking
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: CandidateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidates(candidates: List<CandidateEntity>)

    @Query("SELECT * FROM candidates ORDER BY overallAtsScore DESC")
    fun getAllCandidatesSortedByScore(): Flow<List<CandidateEntity>>

    @Update
    suspend fun updateCandidate(candidate: CandidateEntity)

    @Query("DELETE FROM candidates WHERE id = :id")
    suspend fun deleteCandidate(id: String)

    // Candidate Summaries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidateSummary(summary: CandidateSummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidateSummaries(summaries: List<CandidateSummaryEntity>)

    @Query("SELECT * FROM candidate_summaries ORDER BY timestamp DESC")
    fun getAllCandidateSummaries(): Flow<List<CandidateSummaryEntity>>

    @Query("SELECT * FROM candidate_summaries WHERE id = :id OR resumeId = :id LIMIT 1")
    suspend fun getCandidateSummaryById(id: String): CandidateSummaryEntity?

    @Query("DELETE FROM candidate_summaries WHERE id = :id")
    suspend fun deleteCandidateSummary(id: String)
}

