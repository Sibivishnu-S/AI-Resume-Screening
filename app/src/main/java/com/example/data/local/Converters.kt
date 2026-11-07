package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AtsScoreBreakdown
import com.example.data.model.CandidateStatus
import com.example.data.model.EducationItem
import com.example.data.model.ExperienceItem
import com.example.data.model.InterviewQuestion
import com.example.data.model.ProjectItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class RoomConverters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return try {
            moshi.adapter<List<String>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromEducationList(list: List<EducationItem>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, EducationItem::class.java)
        return moshi.adapter<List<EducationItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toEducationList(json: String?): List<EducationItem> {
        if (json.isNullOrBlank()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, EducationItem::class.java)
        return try {
            moshi.adapter<List<EducationItem>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromExperienceList(list: List<ExperienceItem>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, ExperienceItem::class.java)
        return moshi.adapter<List<ExperienceItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toExperienceList(json: String?): List<ExperienceItem> {
        if (json.isNullOrBlank()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, ExperienceItem::class.java)
        return try {
            moshi.adapter<List<ExperienceItem>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromProjectList(list: List<ProjectItem>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, ProjectItem::class.java)
        return moshi.adapter<List<ProjectItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toProjectList(json: String?): List<ProjectItem> {
        if (json.isNullOrBlank()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, ProjectItem::class.java)
        return try {
            moshi.adapter<List<ProjectItem>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromInterviewQuestionList(list: List<InterviewQuestion>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, InterviewQuestion::class.java)
        return moshi.adapter<List<InterviewQuestion>>(type).toJson(list)
    }

    @TypeConverter
    fun toInterviewQuestionList(json: String?): List<InterviewQuestion> {
        if (json.isNullOrBlank()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, InterviewQuestion::class.java)
        return try {
            moshi.adapter<List<InterviewQuestion>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromAtsBreakdown(breakdown: AtsScoreBreakdown?): String {
        if (breakdown == null) return ""
        return moshi.adapter(AtsScoreBreakdown::class.java).toJson(breakdown)
    }

    @TypeConverter
    fun toAtsBreakdown(json: String?): AtsScoreBreakdown {
        if (json.isNullOrBlank()) return AtsScoreBreakdown()
        return try {
            moshi.adapter(AtsScoreBreakdown::class.java).fromJson(json) ?: AtsScoreBreakdown()
        } catch (e: Exception) {
            AtsScoreBreakdown()
        }
    }

    @TypeConverter
    fun fromCandidateStatus(status: CandidateStatus): String = status.name

    @TypeConverter
    fun toCandidateStatus(name: String): CandidateStatus {
        return try {
            CandidateStatus.valueOf(name)
        } catch (e: Exception) {
            CandidateStatus.NEW
        }
    }
}
