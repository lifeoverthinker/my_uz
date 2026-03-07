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
    @SerialName("nazwisko_imie") val name: String, // Zmienione z "nazwa"
    @SerialName("email") val email: String?,
    @SerialName("jednostka") val institute: String? // Zmienione z "instytut"
)

@Serializable
data class ClassScheduleDto(
    @SerialName("uid") val id: String,             // Unikalny ID zajęć
    @SerialName("przedmiot") val subjectName: String,
    @SerialName("rodzaj_zajec") val classType: String, // Wykłady/Laboratoria
    @SerialName("poczatek") val startDateTime: String, // ISO timestamp
    @SerialName("koniec") val endDateTime: String,
    @SerialName("sala") val room: String?,             // Nowa kolumna 'sala'
    @SerialName("podgrupa") val subgroup: String?,     // Wyciągnięte z 'PG'
    @SerialName("nauczyciel") val teacher: String?      // Wyciągnięte z 'SORT'
)

@Serializable
data class GroupCodeDto(@SerialName("grupa_id") val code: String) // Zmiana z kod_grupy na grupa_id

@Serializable
data class SubgroupDto(@SerialName("podgrupa") val subgroup: String)

@Serializable
data class TeacherDto(@SerialName("nazwisko_imie") val name: String?) // Zmienione z "nazwa"

@Serializable
data class GroupDetailsDto(
    @SerialName("tryb") val studyMode: String?, // Zmienione z "tryb_studiow" zgodnie z tabelą grupy
    @SerialName("kierunki") val fieldInfo: FieldOfStudyDto?
)

@Serializable
data class FieldOfStudyDto(
    @SerialName("wydzial") val faculty: String?,
    @SerialName("nazwa") val name: String?
)

@Serializable
data class TeacherClassScheduleDto(
    @SerialName("uid") val id: String,
    @SerialName("przedmiot") val subjectName: String,
    @SerialName("rodzaj_zajec") val classType: String?,
    @SerialName("poczatek") val startDateTime: String,
    @SerialName("koniec") val endDateTime: String,
    @SerialName("sala") val room: String?,
    @SerialName("grupy") val groups: String?
)

@Serializable
data class TeacherIdDto(
    @SerialName("external_id") val id: String,
    @SerialName("email") val email: String?,
    @SerialName("jednostka") val institute: String?
)

// --- REPOSITORY ---

class UniversityRepository(private val supabase: Postgrest) {

    // Poprawione nazwy kolumn zgodnie ze schematem SQL scrapera
    private val scheduleColumns = Columns.list(
        "uid", "przedmiot", "rodzaj_zajec", "poczatek", "koniec", "sala", "podgrupa", "nauczyciel", "id_semestru"
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
            NetworkResult.Error("Błąd wyszukiwania grup.")
        }
    }

    suspend fun searchTeachers(query: String): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("nauczyciele")
                .select(columns = Columns.list("nazwisko_imie")) { // Zmiana na nazwisko_imie
                    filter { ilike("nazwisko_imie", "%$query%") }
                }
                .decodeList<TeacherDto>()
                .mapNotNull { it.name }
                .distinct()
                .sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error("Błąd wyszukiwania nauczycieli.")
        }
    }

    suspend fun getGroupCodes(): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("grupy")
                .select(columns = Columns.list("grupa_id"))
                .decodeList<GroupCodeDto>()
                .map { it.code }
                .distinct()
                .sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error("Błąd pobierania grup.")
        }
    }

    suspend fun getSubgroups(groupCode: String): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("zajecia_grupy")
                .select(columns = Columns.list("podgrupa")) {
                    filter { eq("grupa_id", groupCode) }
                }
                .decodeList<SubgroupDto>()
                .map { it.subgroup ?: "" }
                .distinct()
                .sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error("Błąd pobierania podgrup.")
        }
    }

    suspend fun getSchedule(groupCode: String, subgroups: List<String>): NetworkResult<List<ClassEntity>> {
        return try {
            val dtoList = supabase.from("zajecia_grupy")
                .select(columns = scheduleColumns) {
                    filter { eq("grupa_id", groupCode) }
                }
                .decodeList<ClassScheduleDto>()

            val filteredList = if (subgroups.isNotEmpty()) {
                dtoList.filter { it.subgroup.isNullOrBlank() || subgroups.contains(it.subgroup) }
            } else {
                dtoList
            }

            val teacherNames = filteredList.mapNotNull { it.teacher }.distinct()
            val teacherMap = fetchTeacherDetails(teacherNames)

            NetworkResult.Success(mapDtoToEntity(filteredList, groupCode, teacherMap))
        } catch (e: Exception) {
            NetworkResult.Error("Błąd pobierania planu zajęć.")
        }
    }

    suspend fun getScheduleForTeacher(teacherName: String): NetworkResult<List<ClassEntity>> {
        return try {
            val teachers = supabase.from("nauczyciele")
                .select(columns = Columns.list("external_id", "email", "jednostka")) {
                    filter { eq("nazwisko_imie", teacherName) }
                }
                .decodeList<TeacherIdDto>()

            val teacherInfo = teachers.firstOrNull() ?: return NetworkResult.Error("Nie znaleziono nauczyciela.")

            val dtoList = supabase.from("zajecia_nauczyciela")
                .select() { filter { eq("nauczyciel_id", teacherInfo.id) } }
                .decodeList<TeacherClassScheduleDto>()

            val entities = dtoList.mapNotNull { dto ->
                try {
                    val startDT = LocalDateTime.parse(dto.startDateTime)
                    val endDT = LocalDateTime.parse(dto.endDateTime)
                    ClassEntity(
                        supabaseId = dto.id,
                        subjectName = dto.subjectName,
                        classType = dto.classType ?: "Inne",
                        startTime = startDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                        endTime = endDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                        dayOfWeek = startDT.dayOfWeek.value,
                        date = startDT.toLocalDate().toString(),
                        groupCode = dto.groups ?: "",
                        subgroup = null,
                        teacherName = teacherName,
                        teacherEmail = teacherInfo.email,
                        teacherInstitute = teacherInfo.institute,
                        room = dto.room ?: "Brak sali"
                    )
                } catch (e: Exception) { null }
            }.sortedWith(compareBy({ it.date }, { it.startTime }))

            NetworkResult.Success(entities)
        } catch (e: Exception) {
            NetworkResult.Error("Błąd pobierania planu nauczyciela.")
        }
    }

    // Pomocnicza do pobierania detali nauczyciela (email, jednostka)
    private suspend fun fetchTeacherDetails(teacherNames: List<String>): Map<String, TeacherDetailsDto> {
        if (teacherNames.isEmpty()) return emptyMap()
        return try {
            val uniqueNames = teacherNames.distinct()
            supabase.from("nauczyciele")
                .select(columns = Columns.list("nazwisko_imie", "email", "jednostka")) {
                    filter {
                        isIn("nazwisko_imie", uniqueNames)
                    }
                }
                .decodeList<TeacherDetailsDto>()
                .associateBy { it.name }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // Pozostała logika mapDtoToEntity powinna używać nowych nazw pól z ClassScheduleDto
    private fun mapDtoToEntity(
        dtoList: List<ClassScheduleDto>,
        groupCode: String,
        teacherMap: Map<String, TeacherDetailsDto>
    ): List<ClassEntity> {
        return dtoList.mapNotNull { dto ->
            try {
                // ISO format z bazy: "2024-03-07T13:00:00"
                val startDT = LocalDateTime.parse(dto.startDateTime)
                val endDT = LocalDateTime.parse(dto.endDateTime)

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
                    subgroup = dto.subgroup,
                    teacherName = dto.teacher ?: "Brak danych",
                    teacherEmail = teacherInfo?.email,
                    teacherInstitute = teacherInfo?.institute, // Teraz pobierane z 'jednostka'
                    room = dto.room ?: "Brak sali"
                )
            } catch (e: Exception) { null }
        }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }

    suspend fun getGroupDetails(groupCode: String): NetworkResult<GroupDetailsDto> {
        return try {
            val details = supabase.from("grupy")
                .select(columns = Columns.raw("tryb, kierunki(wydzial, nazwa)")) { // Zmiana tryb_studiow na tryb
                    filter { eq("grupa_id", groupCode) }
                }
                .decodeSingleOrNull<GroupDetailsDto>()
            if (details != null) NetworkResult.Success(details)
            else NetworkResult.Error("Brak szczegółów grupy.")
        } catch (e: Exception) {
            NetworkResult.Error("Błąd pobierania szczegółów.")
        }
    }
}