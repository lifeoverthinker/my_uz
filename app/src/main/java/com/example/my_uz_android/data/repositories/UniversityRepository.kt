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

// --- DTO ---

@Serializable
data class TeacherDetailsDto(
    @SerialName("nazwa") val name: String,
    @SerialName("email") val email: String?,
    @SerialName("instytut") val institute: String?
)

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
    // Usunąłem zagnieżdżone 'nauczyciele', pobieramy to osobno
)

@Serializable
data class GroupCodeDto(@SerialName("kod_grupy") val code: String)

@Serializable
data class SubgroupDto(@SerialName("podgrupa") val subgroup: String)

@Serializable
data class TeacherDto(@SerialName("nazwa") val name: String?)

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

    // Pobieramy tylko kolumny z tabeli zajecia_grupy, bez JOINa
    private val scheduleColumns = Columns.list(
        "id", "przedmiot", "rz", "od", "do_", "miejsce", "podgrupa", "nauczyciel"
    )

    suspend fun searchGroups(query: String): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("grupy")
                .select(columns = Columns.list("kod_grupy")) {
                    filter { ilike("kod_grupy", "%$query%") }
                }
                .decodeList<GroupCodeDto>()
                .map { it.code }
                .distinct()
                .sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            Log.e("UniversityRepo", "Błąd wyszukiwania grup: $query", e)
            NetworkResult.Error("Błąd wyszukiwania grup.")
        }
    }

    suspend fun searchTeachers(query: String): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("nauczyciele")
                .select(columns = Columns.list("nazwa")) {
                    filter { ilike("nazwa", "%$query%") }
                }
                .decodeList<TeacherDto>()
                .mapNotNull { it.name }
                .distinct()
                .sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            Log.e("UniversityRepo", "Błąd wyszukiwania nauczycieli: $query", e)
            NetworkResult.Error("Błąd wyszukiwania nauczycieli.")
        }
    }

    suspend fun getSubgroups(groupCode: String): NetworkResult<List<String>> {
        return try {
            val grupaId = getGrupaId(groupCode) ?: return NetworkResult.Error("Nie znaleziono grupy.")
            val result = supabase.from("zajecia_grupy")
                .select(columns = Columns.list("podgrupa")) {
                    filter {
                        eq("grupa_id", grupaId)
                    }
                }
                .decodeList<SubgroupDto>()
                .map { it.subgroup }
                // Nie filtrujemy tutaj pustych, bo chcemy wiedzieć czy jest "Cała grupa"
                .distinct()
                .sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error("Błąd pobierania podgrup.")
        }
    }

    suspend fun getSchedule(groupCode: String, subgroups: List<String>): NetworkResult<List<ClassEntity>> {
        return try {
            val grupaId = getGrupaId(groupCode) ?: return NetworkResult.Error("Nie znaleziono ID grupy.")

            // 1. Pobierz plan zajęć
            val scheduleDto = supabase.from("zajecia_grupy")
                .select(columns = scheduleColumns) {
                    filter {
                        eq("grupa_id", grupaId)
                        neq("rz", "E")
                        if (subgroups.isNotEmpty()) {
                            or {
                                eq("podgrupa", "")
                                filter("podgrupa", FilterOperator.IS, "null")
                                isIn("podgrupa", subgroups)
                            }
                        }
                    }
                }
                .decodeList<ClassScheduleDto>()

            // 2. Pobierz szczegóły nauczycieli dla pobranych zajęć
            val teacherDetailsMap = fetchTeacherDetails(scheduleDto.mapNotNull { it.teacher })

            NetworkResult.Success(mapDtoToEntity(scheduleDto, groupCode, teacherDetailsMap))
        } catch (e: Exception) {
            Log.e("UniversityRepo", "Błąd pobierania planu", e)
            NetworkResult.Error("Nie udało się pobrać planu.")
        }
    }

    suspend fun getScheduleForTeacher(teacherName: String): NetworkResult<List<ClassEntity>> {
        return try {
            // 1. Pobierz plan
            val scheduleDto = supabase.from("zajecia_grupy")
                .select(columns = scheduleColumns) {
                    filter {
                        ilike("nauczyciel", "%$teacherName%")
                        neq("rz", "E")
                    }
                }
                .decodeList<ClassScheduleDto>()

            // 2. Pobierz szczegóły (email, instytut)
            val teacherDetailsMap = fetchTeacherDetails(scheduleDto.mapNotNull { it.teacher })

            NetworkResult.Success(mapDtoToEntity(scheduleDto, "Nauczyciel: $teacherName", teacherDetailsMap))
        } catch (e: Exception) {
            NetworkResult.Error("Nie udało się pobrać planu nauczyciela.")
        }
    }

    // Funkcja pomocnicza do "dociągania" danych nauczycieli
    private suspend fun fetchTeacherDetails(teacherNames: List<String>): Map<String, TeacherDetailsDto> {
        if (teacherNames.isEmpty()) return emptyMap()

        return try {
            val uniqueNames = teacherNames.distinct()
            supabase.from("nauczyciele")
                .select(columns = Columns.list("nazwa", "email", "instytut")) {
                    filter {
                        isIn("nazwa", uniqueNames)
                    }
                }
                .decodeList<TeacherDetailsDto>()
                .associateBy { it.name }
        } catch (e: Exception) {
            Log.e("UniversityRepo", "Błąd pobierania detali nauczycieli", e)
            emptyMap()
        }
    }

    private suspend fun getGrupaId(groupCode: String): String? {
        val result = supabase.from("grupy")
            .select(columns = Columns.list("id")) {
                filter { eq("kod_grupy", groupCode) }
            }
            .decodeSingleOrNull<Map<String, String>>()
        return result?.get("id")
    }

    private fun mapDtoToEntity(
        dtoList: List<ClassScheduleDto>,
        groupCode: String,
        teacherMap: Map<String, TeacherDetailsDto>
    ): List<ClassEntity> {
        return dtoList.mapNotNull { dto ->
            try {
                val startDT = LocalDateTime.parse(dto.startDateTime.replace(" ", "T"))
                val endDT = LocalDateTime.parse(dto.endDateTime.replace(" ", "T"))

                // Pobierz detale z mapy, jeśli istnieją
                val teacherInfo = teacherMap[dto.teacher]

                ClassEntity(
                    supabaseId = dto.id,
                    subjectName = dto.subjectName,
                    classType = dto.classType,
                    startTime = startDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = endDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                    dayOfWeek = startDT.dayOfWeek.value,
                    date = startDT.toLocalDate().toString(),
                    groupCode = groupCode,
                    subgroup = dto.subgroup, // null zostanie zamieniony na "" w UI
                    teacherName = dto.teacher ?: "Brak danych",
                    teacherEmail = teacherInfo?.email,       // Teraz poprawnie zmapowane
                    teacherInstitute = teacherInfo?.institute, // Teraz poprawnie zmapowane
                    room = dto.room ?: "Brak sali"
                )
            } catch (e: Exception) { null }
        }.sortedWith(compareBy({ it.date }, { it.startTime }))
            .distinctBy { "${it.date}_${it.startTime}_${it.subjectName}_${it.room}" }
    }

    // Pozostałe metody (getGroupDetails, getGroupCodes, getTeachers) bez zmian...
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
            NetworkResult.Error("Nie udało się pobrać listy grup.")
        }
    }

    suspend fun getTeachers(): NetworkResult<List<String>> {
        return try {
            val teachers = supabase.from("nauczyciele")
                .select(columns = Columns.list("nazwa"))
                .decodeList<TeacherDto>()
                .mapNotNull { it.name }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
            NetworkResult.Success(teachers)
        } catch (e: Exception) {
            NetworkResult.Error("Nie udało się pobrać listy nauczycieli.")
        }
    }
}