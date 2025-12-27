package com.example.my_uz_android.data.repositories

import android.util.Log
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.util.NetworkResult
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// --- DTO (Data Transfer Objects) ---

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
data class TeacherDto(@SerialName("nauczyciel") val name: String?)

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

// --- REPOSITORY ---

class UniversityRepository(private val supabase: Postgrest) {

    /**
     * Pobiera listę unikalnych kodów grup
     */
    suspend fun getGroupCodes(): NetworkResult<List<String>> {
        return try {
            val codes = supabase.from("grupy")
                .select(columns = Columns.list("kod_grupy"))
                .decodeList<GroupCodeDto>()
                .map { it.code }
                .distinct()
                .sorted()
            NetworkResult.Success(codes)
        } catch (e: Exception) {
            Log.e("UniversityRepo", "Błąd pobierania kodów grup", e)
            NetworkResult.Error("Nie udało się pobrać listy grup.")
        }
    }

    /**
     * Pobiera listę wszystkich nauczycieli (do wyszukiwarki)
     */
    suspend fun getTeachers(): NetworkResult<List<String>> {
        return try {
            val teachers = supabase.from("zajecia_grupy")
                .select(columns = Columns.list("nauczyciel"))
                .decodeList<TeacherDto>()
                .mapNotNull { it.name }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
            NetworkResult.Success(teachers)
        } catch (e: Exception) {
            Log.e("UniversityRepo", "Błąd pobierania nauczycieli", e)
            NetworkResult.Error("Nie udało się pobrać listy nauczycieli.")
        }
    }

    /**
     * Pobiera dostępne podgrupy dla danej grupy
     */
    suspend fun getSubgroups(groupCode: String): NetworkResult<List<String>> {
        return try {
            val grupaId = getGrupaId(groupCode) ?: return NetworkResult.Error("Nie znaleziono grupy.")

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

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error("Błąd pobierania podgrup.")
        }
    }

    /**
     * Pobiera szczegółowe informacje o kierunku i wydziale
     */
    suspend fun getGroupDetails(groupCode: String): NetworkResult<GroupDetailsDto> {
        return try {
            val details = supabase.from("grupy")
                .select(columns = Columns.raw("tryb_studiow, kierunki(wydzial, nazwa)")) {
                    filter { eq("kod_grupy", groupCode) }
                }
                .decodeSingleOrNull<GroupDetailsDto>()

            if (details != null) NetworkResult.Success(details)
            else NetworkResult.Error("Brak szczegółów grupy.")
        } catch (e: Exception) {
            NetworkResult.Error("Błąd pobierania szczegółów.")
        }
    }

    /**
     * GŁÓWNA FUNKCJA: Pobiera plan dla grupy z filtrowaniem podgrup
     */
    suspend fun getSchedule(
        groupCode: String,
        subgroups: List<String>
    ): NetworkResult<List<ClassEntity>> {
        return try {
            val grupaId = getGrupaId(groupCode) ?: return NetworkResult.Error("Nie znaleziono ID grupy.")

            val scheduleDto = supabase.from("zajecia_grupy")
                .select(columns = Columns.list("id", "przedmiot", "rz", "od", "do_", "miejsce", "podgrupa", "nauczyciel")) {
                    filter {
                        eq("grupa_id", grupaId)
                        neq("rz", "E") // Wyklucz egzaminy

                        // Odwzorowanie logiki SQL: (podgrupa='' OR podgrupa IS NULL OR podgrupa IN (...))
                        if (subgroups.isNotEmpty()) {
                            or {
                                eq("podgrupa", "")
                                // NAPRAWIONO: Użycie FilterOperator.IS zamiast Stringa "is"
                                filter("podgrupa", FilterOperator.IS, "null")
                                isIn("podgrupa", subgroups)
                            }
                        }
                    }
                }
                .decodeList<ClassScheduleDto>()

            NetworkResult.Success(mapDtoToEntity(scheduleDto, groupCode))
        } catch (e: Exception) {
            Log.e("UniversityRepo", "Błąd getSchedule", e)
            NetworkResult.Error("Nie udało się pobrać planu.")
        }
    }

    /**
     * Pobiera plan dla wybranego nauczyciela (wyszukiwanie częściowe)
     */
    suspend fun getScheduleForTeacher(teacherName: String): NetworkResult<List<ClassEntity>> {
        return try {
            val searchPattern = "%$teacherName%"
            val scheduleDto = supabase.from("zajecia_grupy")
                .select(columns = Columns.list("id", "przedmiot", "rz", "od", "do_", "miejsce", "podgrupa", "nauczyciel")) {
                    filter {
                        ilike("nauczyciel", searchPattern)
                        neq("rz", "E")
                    }
                }
                .decodeList<ClassScheduleDto>()

            NetworkResult.Success(mapDtoToEntity(scheduleDto, "Nauczyciel: $teacherName"))
        } catch (e: Exception) {
            Log.e("UniversityRepo", "Błąd getScheduleForTeacher", e)
            NetworkResult.Error("Nie udało się pobrać planu nauczyciela.")
        }
    }

    // --- PRYWATNE FUNKCJE POMOCNICZE ---

    private suspend fun getGrupaId(groupCode: String): String? {
        val result = supabase.from("grupy")
            .select(columns = Columns.list("id")) {
                filter { eq("kod_grupy", groupCode) }
            }
            .decodeSingleOrNull<Map<String, String>>()
        return result?.get("id")
    }

    private fun mapDtoToEntity(dtoList: List<ClassScheduleDto>, groupCode: String): List<ClassEntity> {
        return dtoList.mapNotNull { dto ->
            try {
                // Konwersja daty - obsługa formatu ISO z bazy
                val startDT = LocalDateTime.parse(dto.startDateTime.replace(" ", "T"))
                val endDT = LocalDateTime.parse(dto.endDateTime.replace(" ", "T"))

                ClassEntity(
                    supabaseId = dto.id,
                    subjectName = dto.subjectName,
                    classType = dto.classType,
                    startTime = startDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = endDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                    dayOfWeek = startDT.dayOfWeek.value,
                    date = startDT.toLocalDate().toString(), // Format YYYY-MM-DD
                    groupCode = groupCode,
                    subgroup = dto.subgroup ?: "",
                    teacherName = dto.teacher ?: "Brak danych",
                    room = dto.room ?: "Brak sali"
                )
            } catch (e: Exception) {
                null
            }
        }
            // KLUCZOWE: Sortowanie po dacie (dzień po dniu) i godzinie (chronologicznie w ciągu dnia)
            .sortedWith(compareBy({ it.date }, { it.startTime }))
            // Usuwanie duplikatów (identyczne zajęcia o tej samej godzinie w tej samej sali)
            .distinctBy { "${it.date}_${it.startTime}_${it.subjectName}_${it.room}" }
    }
}