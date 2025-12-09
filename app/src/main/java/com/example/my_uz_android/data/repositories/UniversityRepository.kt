package com.example.my_uz_android.data.repositories

import android.util.Log
import com.example.my_uz_android.data.models.ClassEntity
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface UniversityRepository {
    suspend fun getGroupCodes(): List<String>
    suspend fun getSubgroups(groupCode: String): List<String>
    suspend fun getSchedule(groupCode: String, subgroups: List<String>): List<ClassEntity>
    suspend fun getGroupDetails(groupCode: String): GroupDetailsDto?
}

// --- DTO ---
@Serializable
data class ClassScheduleDto(
    @SerialName("id") val id: String,
    @SerialName("przedmiot") val subjectName: String,
    @SerialName("rz") val classType: String,
    @SerialName("od") val startDateTime: String,
    @SerialName("do_") val endDateTime: String,
    @SerialName("miejsce") val room: String?,
    @SerialName("podgrupa") val subgroup: String?,
    @SerialName("nauczyciel") val teacher: String?
)

@Serializable
data class GroupCodeDto(@SerialName("kod_grupy") val code: String)

@Serializable
data class SubgroupDto(@SerialName("podgrupa") val subgroup: String)

@Serializable
data class GroupDetailsDto(
    @SerialName("tryb_studiow") val studyMode: String?,
    @SerialName("kierunki") val fieldInfo: FieldOfStudyDto?
)

@Serializable
data class FieldOfStudyDto(
    @SerialName("wydzial") val faculty: String?,
    @SerialName("nazwa") val name: String?
)

// --- Implementacja ---
class SupabaseUniversityRepository(private val supabase: Postgrest) : UniversityRepository {

    override suspend fun getGroupCodes(): List<String> {
        return try {
            supabase.from("grupy")
                .select(columns = Columns.list("kod_grupy"))
                .decodeList<GroupCodeDto>()
                .map { it.code }
                .distinct()
                .sorted()
        } catch (e: Exception) {
            Log.e("UniversityRepo", "Błąd pobierania kodów grup", e)
            emptyList()
        }
    }

    override suspend fun getSubgroups(groupCode: String): List<String> {
        return try {
            val grupaIdResult = supabase.from("grupy")
                .select(columns = Columns.list("id")) {
                    filter { eq("kod_grupy", groupCode) }
                }
                .decodeSingleOrNull<Map<String, String>>()

            val grupaId = grupaIdResult?.get("id") ?: return emptyList()

            val result = supabase.from("zajecia_grupy")
                .select(columns = Columns.list("podgrupa")) {
                    filter {
                        eq("grupa_id", grupaId)
                        neq("podgrupa", "")
                    }
                }
                .decodeList<SubgroupDto>()
                .map { it.subgroup }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

            Log.d("UniversityRepo", "Pobrano podgrupy dla $groupCode: $result")
            result
        } catch (e: Exception) {
            Log.e("UniversityRepo", "Błąd pobierania podgrup", e)
            emptyList()
        }
    }

    override suspend fun getGroupDetails(groupCode: String): GroupDetailsDto? {
        return try {
            supabase.from("grupy")
                .select(columns = Columns.raw("tryb_studiow, kierunki(wydzial, nazwa)")) {
                    filter { eq("kod_grupy", groupCode) }
                }
                .decodeSingleOrNull<GroupDetailsDto>()
        } catch (e: Exception) {
            Log.e("UniversityRepo", "Błąd pobierania szczegółów grupy: $groupCode", e)
            null
        }
    }

    override suspend fun getSchedule(
        groupCode: String,
        subgroups: List<String>
    ): List<ClassEntity> {
        return try {
            Log.d("UniversityRepo", "🔍 Pobieram zajęcia dla $groupCode, podgrupy: $subgroups")

            // 1. Pobierz ID grupy
            val grupaIdResult = supabase.from("grupy")
                .select(columns = Columns.list("id")) {
                    filter { eq("kod_grupy", groupCode) }
                }
                .decodeSingleOrNull<Map<String, String>>()

            val grupaId = grupaIdResult?.get("id")
                ?: throw Exception("Nie znaleziono grupy: $groupCode")

            // 2. POPRAWIONE: Pobierz zajęcia z filtrowaniem podgrup w SQL
            val scheduleDto = if (subgroups.isEmpty()) {
                // Jeśli nie wybrano podgrup - pokaż WSZYSTKIE (łącznie z ogólnymi)
                supabase.from("zajecia_grupy")
                    .select(
                        columns = Columns.list(
                            "id", "przedmiot", "rz", "od", "do_",
                            "miejsce", "podgrupa", "nauczyciel"
                        )
                    ) {
                        filter {
                            eq("grupa_id", grupaId)
                            neq("rz", "E") // Bez egzaminów
                        }
                    }
                    .decodeList<ClassScheduleDto>()
            } else {
                // Jeśli wybrano podgrupy - filtruj w SQL
                supabase.from("zajecia_grupy")
                    .select(
                        columns = Columns.list(
                            "id", "przedmiot", "rz", "od", "do_",
                            "miejsce", "podgrupa", "nauczyciel"
                        )
                    ) {
                        filter {
                            eq("grupa_id", grupaId)
                            neq("rz", "E")
                            or {
                                // Ogólne zajęcia (bez podgrupy) LUB wybrane podgrupy
                                eq("podgrupa", "")
                                isIn("podgrupa", subgroups) // ✅ POPRAWIONE: `in` → isIn
                            }
                        }
                    }
                    .decodeList<ClassScheduleDto>()
            }

            Log.d("UniversityRepo", "📦 Pobrano ${scheduleDto.size} zajęć")

            // 3. Mapowanie do ClassEntity
            val entities = scheduleDto
                .mapNotNull { dto ->
                    try {
                        val startDT = LocalDateTime.parse(dto.startDateTime.replace(" ", "T"))
                        val endDT = LocalDateTime.parse(dto.endDateTime.replace(" ", "T"))

                        ClassEntity(
                            supabaseId = dto.id,
                            subjectName = dto.subjectName,
                            classType = dto.classType,
                            startTime = startDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                            endTime = endDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                            dayOfWeek = startDT.dayOfWeek.value,
                            date = startDT.toLocalDate().toString(), // ← DODANE: "2025-12-09"
                            groupCode = groupCode,
                            subgroup = dto.subgroup,
                            teacherName = dto.teacher,
                            room = dto.room
                        )

                    } catch (e: Exception) {
                        Log.e("UniversityRepo", "Błąd parsowania rekordu id=${dto.id}", e)
                        null
                    }
                }
                .sortedWith(compareBy({ it.dayOfWeek }, { it.startTime }))

            entities
        } catch (e: Exception) {
            Log.e("UniversityRepo", "❌ Błąd pobierania planu", e)
            emptyList()
        }
    }
}
