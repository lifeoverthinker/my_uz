package com.example.my_uz_android.data.repositories

import android.util.Log
import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.util.NetworkResult
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class TeacherDetailsDto(
    @SerialName("nazwisko_imie") val name: String,
    @SerialName("email") val email: String?,
    @SerialName("jednostka") val institute: String?
)

@Serializable
data class ClassScheduleDto(
    @SerialName("uid") val id: String,
    @SerialName("przedmiot") val subjectName: String,
    @SerialName("rodzaj_zajec") val classType: String?,
    @SerialName("poczatek") val startDateTime: String?,
    @SerialName("koniec") val endDateTime: String?,
    @SerialName("sala") val room: String?,
    @SerialName("podgrupa") val subgroup: String?,
    @SerialName("nauczyciel") val teacher: String?
)

@Serializable
data class GroupCodeDto(@SerialName("nazwa") val code: String?)

@Serializable
data class GroupIdDto(@SerialName("grupa_id") val id: String?)

@Serializable
data class SubgroupDto(@SerialName("podgrupa") val subgroup: String?)

@Serializable
data class TeacherDto(@SerialName("nazwisko_imie") val name: String?)

@Serializable
data class GroupDetailsDto(
    @SerialName("tryb") val studyMode: String?,
    @SerialName("semestr") val semester: String?,
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
    @SerialName("poczatek") val startDateTime: String?,
    @SerialName("koniec") val endDateTime: String?,
    @SerialName("sala") val room: String?,
    @SerialName("grupy") val groups: String?
)

@Serializable
data class TeacherIdDto(
    @SerialName("external_id") val id: String,
    @SerialName("email") val email: String?,
    @SerialName("jednostka") val institute: String?
)

class UniversityRepository(private val supabase: Postgrest) {

    private val scheduleColumns = Columns.list(
        "uid", "przedmiot", "rodzaj_zajec", "poczatek", "koniec", "sala", "podgrupa", "nauczyciel"
    )

    private fun parseDateSafe(dateString: String?): LocalDateTime? {
        if (dateString.isNullOrBlank()) return null
        val cleanString = dateString.substringBefore("+").replace(" ", "T")
        return try {
            LocalDateTime.parse(cleanString)
        } catch (e: Exception) {
            Log.e("UniversityRepository", "Błąd parsowania daty: $dateString", e)
            null
        }
    }

    private suspend fun getGrupaId(groupName: String): String? {
        return try {
            val result = supabase.from("grupy").select(columns = Columns.list("grupa_id")) {
                filter { eq("nazwa", groupName) }
            }.decodeList<GroupIdDto>()
            result.firstOrNull()?.id
        } catch (e: Exception) { null }
    }

    suspend fun searchGroups(query: String): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("grupy").select(columns = Columns.list("nazwa")) {
                filter { ilike("nazwa", "%$query%") }
            }.decodeList<GroupCodeDto>().mapNotNull { it.code }.distinct().sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) { NetworkResult.Error("Błąd wyszukiwania grup.") }
    }

    suspend fun searchTeachers(query: String): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("nauczyciele").select(columns = Columns.list("nazwisko_imie")) {
                filter { ilike("nazwisko_imie", "%$query%") }
            }.decodeList<TeacherDto>().mapNotNull { it.name }.distinct().sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) { NetworkResult.Error("Błąd wyszukiwania nauczycieli.") }
    }

    suspend fun getAllTeachers(): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("nauczyciele").select(columns = Columns.list("nazwisko_imie"))
                .decodeList<TeacherDto>().mapNotNull { it.name }.distinct().sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error("Błąd pobierania nauczycieli.")
        }
    }
    suspend fun getGroupCodes(): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("grupy").select(columns = Columns.list("nazwa"))
                .decodeList<GroupCodeDto>().mapNotNull { it.code }.distinct().sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) { NetworkResult.Error("Błąd pobierania grup.") }
    }

    suspend fun getSubgroups(groupCode: String): NetworkResult<List<String>> {
        return try {
            val grupaId = getGrupaId(groupCode) ?: return NetworkResult.Error("Nie znaleziono grupy.")
            val result = supabase.from("zajecia_grupy").select(columns = Columns.list("podgrupa")) {
                filter { eq("grupa_id", grupaId) }
            }.decodeList<SubgroupDto>()

            // Ignorujemy zepsute wpisy takie jak "empty", "brak", "-" aby nie pokazywały się jako opcje wyboru podgrupy
            val safeSubgroups = result.mapNotNull { it.subgroup }
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.equals("empty", ignoreCase = true) && it != "-" && !it.equals("brak", ignoreCase = true) }
                .distinct()
                .sorted()

            NetworkResult.Success(safeSubgroups)
        } catch (e: Exception) { NetworkResult.Error("Błąd podgrup.") }
    }

    suspend fun getSchedule(groupCode: String, subgroups: List<String>): NetworkResult<List<ClassEntity>> {
        return try {
            val grupaId = getGrupaId(groupCode) ?: return NetworkResult.Error("Nie znaleziono grupy.")
            val dtoList = supabase.from("zajecia_grupy").select(columns = scheduleColumns) {
                filter { eq("grupa_id", grupaId) }
            }.decodeList<ClassScheduleDto>()

            val safeSubgroups = subgroups.map { it.trim() }.filter { it.isNotBlank() }

            val filtered = if (safeSubgroups.isNotEmpty()) {
                dtoList.filter { dto ->
                    val sub = dto.subgroup?.trim()?.lowercase()
                    // Przepuszczamy faktycznie puste ORAZ te z fałszywym "empty" wpisanym w bazę
                    val isCommonClass = sub.isNullOrBlank() || sub == "empty" || sub == "-" || sub == "brak"

                    isCommonClass || safeSubgroups.contains(dto.subgroup?.trim())
                }
            } else {
                dtoList
            }

            val teacherNames = filtered.mapNotNull { it.teacher?.trim() }.filter { it.isNotBlank() }.distinct()
            val teacherMap = fetchTeacherDetails(teacherNames)

            val entities = mapDtoToEntity(filtered, groupCode, teacherMap)
            NetworkResult.Success(entities)

        } catch (e: Exception) {
            Log.e("UniversityRepository", "Błąd pobierania planu", e)
            NetworkResult.Error("Błąd planu: ${e.message}")
        }
    }

    suspend fun getScheduleForTeacher(teacherName: String): NetworkResult<List<ClassEntity>> {
        return try {
            // Używamy ilike i trim, aby dopasować nazwisko nawet jeśli ma spacje na końcu/początku
            val teachers = supabase.from("nauczyciele").select(columns = Columns.list("external_id", "email", "jednostka")) {
                filter { ilike("nazwisko_imie", teacherName.trim()) }
            }.decodeList<TeacherIdDto>()

            val info = teachers.firstOrNull() ?: return NetworkResult.Error("Nie znaleziono nauczyciela o tym nazwisku.")

            // Pobieramy zajęcia korzystając z uzyskanego ID zewnętrznego
            val dtoList = supabase.from("zajecia_nauczyciela").select {
                filter { eq("nauczyciel_id", info.id) }
            }.decodeList<TeacherClassScheduleDto>()

            val entities = dtoList.mapNotNull { dto ->
                val startDT = parseDateSafe(dto.startDateTime) ?: return@mapNotNull null
                val endDT = parseDateSafe(dto.endDateTime) ?: return@mapNotNull null
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
                    teacherEmail = info.email,
                    teacherInstitute = info.institute,
                    room = dto.room ?: "Brak"
                )
            }.sortedWith(compareBy({ it.date }, { it.startTime }))

            NetworkResult.Success(entities)
        } catch (e: Exception) {
            Log.e("UniversityRepository", "Błąd pobierania planu nauczyciela: ${e.message}", e)
            NetworkResult.Error("Błąd serwera przy pobieraniu planu.")
        }
    }

    suspend fun getGroupDetails(groupCode: String): NetworkResult<GroupDetailsDto> {
        return try {
            val details = supabase.from("grupy")
                .select(columns = Columns.raw("tryb, semestr, kierunki:kierunek_id(wydzial, nazwa)")) {
                    filter { eq("nazwa", groupCode) }
                }.decodeList<GroupDetailsDto>()
            details.firstOrNull()?.let { NetworkResult.Success(it) } ?: NetworkResult.Error("Brak danych.")
        } catch (e: Exception) { NetworkResult.Error("Błąd komunikacji z bazą.") }
    }

    private suspend fun fetchTeacherDetails(teacherNames: List<String>): Map<String, TeacherDetailsDto> {
        if (teacherNames.isEmpty()) return emptyMap()
        return try {
            supabase.from("nauczyciele").select(columns = Columns.list("nazwisko_imie", "email", "jednostka")) {
                filter { isIn("nazwisko_imie", teacherNames) }
            }.decodeList<TeacherDetailsDto>().associateBy { it.name }
        } catch (e: Exception) { emptyMap() }
    }

    private fun mapDtoToEntity(
        dtoList: List<ClassScheduleDto>,
        groupCode: String,
        teacherMap: Map<String, TeacherDetailsDto>
    ): List<ClassEntity> {
        return dtoList.mapNotNull { dto ->
            val startDT = parseDateSafe(dto.startDateTime) ?: return@mapNotNull null
            val endDT = parseDateSafe(dto.endDateTime) ?: return@mapNotNull null

            ClassEntity(
                supabaseId = dto.id,
                subjectName = dto.subjectName,
                classType = dto.classType ?: "Inne",
                startTime = startDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                endTime = endDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                dayOfWeek = startDT.dayOfWeek.value,
                date = startDT.toLocalDate().toString(),
                groupCode = groupCode,
                subgroup = dto.subgroup,
                teacherName = dto.teacher ?: "Brak danych",
                teacherEmail = teacherMap[dto.teacher]?.email,
                teacherInstitute = teacherMap[dto.teacher]?.institute,
                room = dto.room ?: "Brak"
            )
        }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }
}