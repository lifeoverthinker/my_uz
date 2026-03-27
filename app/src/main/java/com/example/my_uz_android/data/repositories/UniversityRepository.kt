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
    @SerialName("nazwisko_imie") val name: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("jednostka") val institute: String? = null
)

@Serializable
data class ClassScheduleDto(
    @SerialName("uid") val id: String? = null,
    @SerialName("przedmiot") val subjectName: String? = null,
    @SerialName("rodzaj_zajec") val classType: String? = null,
    @SerialName("poczatek") val startDateTime: String? = null,
    @SerialName("koniec") val endDateTime: String? = null,
    @SerialName("sala") val room: String? = null,
    @SerialName("podgrupa") val subgroup: String? = null,
    @SerialName("nauczyciel") val teacher: String? = null
)

@Serializable
data class GroupCodeDto(@SerialName("nazwa") val code: String? = null)

@Serializable
data class GroupIdDto(@SerialName("grupa_id") val id: Int? = null)

@Serializable
data class SubgroupDto(@SerialName("podgrupa") val subgroup: String? = null)

@Serializable
data class TeacherDto(@SerialName("nazwisko_imie") val name: String? = null)

@Serializable
data class GroupDetailsDto(
    @SerialName("tryb") val studyMode: String? = null,
    @SerialName("semestr") val semester: String? = null,
    @SerialName("kierunki") val fieldInfo: FieldOfStudyDto? = null
)

@Serializable
data class FieldOfStudyDto(
    @SerialName("wydzial") val faculty: String? = null,
    @SerialName("nazwa") val name: String? = null
)

@Serializable
data class TeacherClassScheduleDto(
    @SerialName("uid") val id: String? = null,
    @SerialName("przedmiot") val subjectName: String? = null,
    @SerialName("rodzaj_zajec") val classType: String? = null,
    @SerialName("poczatek") val startDateTime: String? = null,
    @SerialName("koniec") val endDateTime: String? = null,
    @SerialName("sala") val room: String? = null,
    @SerialName("grupy") val groups: String? = null
)

@Serializable
data class TeacherIdDto(
    @SerialName("id") val id: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("jednostka") val institute: String? = null
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

    private suspend fun getGrupaId(groupName: String): Int? {
        return try {
            val result = supabase.from("grupy").select(columns = Columns.list("grupa_id")) {
                filter { eq("nazwa", groupName) }
            }.decodeList<GroupIdDto>()
            result.firstOrNull()?.id
        } catch (e: Exception) {
            Log.e("UniversityRepository", "Błąd pobierania ID grupy: ${e.message}", e)
            null
        }
    }

    suspend fun searchGroups(query: String): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("grupy")
                .select(columns = Columns.raw("nazwa, kierunki!inner(nazwa)")) {
                    filter {
                        or {
                            ilike("nazwa", "%$query%")
                            ilike("kierunki.nazwa", "%$query%")
                        }
                    }
                }
                .decodeList<GroupCodeDto>()
                .mapNotNull { it.code }
                .distinct()
                .sorted()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            Log.e("UniversityRepository", "Błąd wyszukiwania grup: ${e.message}", e)
            NetworkResult.Error("Błąd wyszukiwania grup.")
        }
    }
    suspend fun searchTeachers(query: String): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("nauczyciele").select(columns = Columns.list("nazwisko_imie")) {
                filter { ilike("nazwisko_imie", "%$query%") }
            }.decodeList<TeacherDto>().mapNotNull { it.name }.distinct().sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) { NetworkResult.Error("Błąd wyszukiwania wykładowców.") }
    }

    suspend fun getAllTeachers(): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("nauczyciele").select(columns = Columns.list("nazwisko_imie"))
                .decodeList<TeacherDto>().mapNotNull { it.name }.distinct().sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error("Błąd pobierania wykładowców.")
        }
    }

    suspend fun getGroupCodes(): NetworkResult<List<String>> {
        return try {
            val result = supabase.from("grupy").select(columns = Columns.list("nazwa"))
                .decodeList<GroupCodeDto>().mapNotNull { it.code }.distinct().sorted()
            NetworkResult.Success(result)
        } catch (e: Exception) {
            Log.e("UniversityRepository", "Błąd pobierania listy grup: ${e.message}", e)
            NetworkResult.Error("Błąd pobierania grup.")
        }
    }

    suspend fun getSubgroups(groupCode: String): NetworkResult<List<String>> {
        return try {
            val grupaId = getGrupaId(groupCode) ?: return NetworkResult.Error("Nie znaleziono grupy.")
            val result = supabase.from("zajecia_grupy").select(columns = Columns.list("podgrupa")) {
                filter { eq("grupa_id", grupaId) }
            }.decodeList<SubgroupDto>()

            val safeSubgroups = result.mapNotNull { it.subgroup }
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.equals("empty", ignoreCase = true) && it != "-" && !it.equals("brak", ignoreCase = true) && !it.equals("all", ignoreCase = true) }
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
                    val sub = dto.subgroup?.trim()?.uppercase()
                    // ALL (Wykłady i Seminaria) przepuszczamy zawsze
                    val isCommonClass = sub.isNullOrBlank() || sub == "EMPTY" || sub == "-" || sub == "BRAK" || sub == "ALL"
                    isCommonClass || safeSubgroups.contains(dto.subgroup?.trim())
                }
            } else {
                dtoList
            }

            val teacherNames = filtered.mapNotNull { it.teacher?.trim() }.filter { it.isNotBlank() }.distinct()
            val teacherMap = fetchTeacherDetails(teacherNames)

            val entities = mapDtoToEntity(filtered, groupCode, teacherMap)
            Log.d("UniversityRepository", "Pomyślnie przeparsowano ${entities.size} zajęć.")
            NetworkResult.Success(entities)

        } catch (e: Exception) {
            Log.e("UniversityRepository", "Błąd pobierania planu", e)
            NetworkResult.Error("Błąd planu: ${e.message}")
        }
    }

    suspend fun getScheduleForTeacher(teacherName: String): NetworkResult<List<ClassEntity>> {
        return try {
            val teachers = supabase.from("nauczyciele").select(columns = Columns.list("id", "email", "jednostka")) {
                filter { eq("nazwisko_imie", teacherName.trim()) }
            }.decodeList<TeacherIdDto>()

            if (teachers.isEmpty()) return NetworkResult.Error("Nie znaleziono wykładowcy o tym nazwisku.")

            val aggregatedEmail = teachers.mapNotNull { it.email }.filter { it.isNotBlank() }.distinct().joinToString(" • ")
            val aggregatedInstitute = teachers.mapNotNull { it.institute }.filter { it.isNotBlank() }.distinct().joinToString(" • ")

            val teacherIds = teachers.mapNotNull { it.id }.filter { it.isNotBlank() }.distinct()
            if (teacherIds.isEmpty()) return NetworkResult.Success(emptyList())

            val dtoList = supabase.from("zajecia_nauczyciela").select {
                filter { isIn("nauczyciel_id", teacherIds) }
            }.decodeList<TeacherClassScheduleDto>()

            val entities = dtoList.mapNotNull { dto ->
                val startDT = parseDateSafe(dto.startDateTime) ?: return@mapNotNull null
                val endDT = parseDateSafe(dto.endDateTime) ?: return@mapNotNull null
                ClassEntity(
                    supabaseId = dto.id ?: java.util.UUID.randomUUID().toString(),
                    subjectName = dto.subjectName ?: "Brak nazwy",
                    classType = dto.classType ?: "Inne",
                    startTime = startDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = endDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                    dayOfWeek = startDT.dayOfWeek.value,
                    date = startDT.toLocalDate().toString(),
                    groupCode = dto.groups ?: "",
                    subgroup = null,
                    teacherName = teacherName,
                    teacherEmail = aggregatedEmail.ifBlank { null },
                    teacherInstitute = aggregatedInstitute.ifBlank { null },
                    room = dto.room ?: "Brak"
                )
            }.sortedWith(compareBy({ it.date }, { it.startTime }))

            NetworkResult.Success(entities)
        } catch (e: Exception) {
            Log.e("UniversityRepository", "Błąd pobierania planu wykładowcy: ${e.message}", e)
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
        } catch (e: Exception) {
            Log.e("UniversityRepository", "Błąd detali grupy: ${e.message}", e)
            NetworkResult.Error("Błąd komunikacji z bazą.")
        }
    }

    private suspend fun fetchTeacherDetails(teacherNames: List<String>): Map<String, TeacherDetailsDto> {
        if (teacherNames.isEmpty()) return emptyMap()
        return try {
            val list = supabase.from("nauczyciele").select(columns = Columns.list("nazwisko_imie", "email", "jednostka")) {
                filter { isIn("nazwisko_imie", teacherNames) }
            }.decodeList<TeacherDetailsDto>()

            list.groupBy { it.name ?: "" }.filterKeys { it.isNotBlank() }.mapValues { (name, dtos) ->
                TeacherDetailsDto(
                    name = name,
                    email = dtos.mapNotNull { it.email }.filter { it.isNotBlank() }.distinct().joinToString(" • ").ifBlank { null },
                    institute = dtos.mapNotNull { it.institute }.filter { it.isNotBlank() }.distinct().joinToString(" • ").ifBlank { null }
                )
            }
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

            val rawSub = dto.subgroup?.trim()
            val isCommon = rawSub.isNullOrBlank() || rawSub.equals("empty", ignoreCase = true) || rawSub == "-" || rawSub.equals("brak", ignoreCase = true) || rawSub.equals("all", ignoreCase = true)

            ClassEntity(
                supabaseId = dto.id ?: java.util.UUID.randomUUID().toString(),
                subjectName = dto.subjectName ?: "Brak nazwy",
                classType = dto.classType ?: "Inne",
                startTime = startDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                endTime = endDT.format(DateTimeFormatter.ofPattern("HH:mm")),
                dayOfWeek = startDT.dayOfWeek.value,
                date = startDT.toLocalDate().toString(),
                groupCode = groupCode,
                subgroup = if (isCommon) null else rawSub,
                teacherName = dto.teacher ?: "Brak danych",
                teacherEmail = teacherMap[dto.teacher]?.email,
                teacherInstitute = teacherMap[dto.teacher]?.institute,
                room = dto.room ?: "Brak"
            )
        }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }

    suspend fun getAllTeachersWithDetails(): NetworkResult<List<TeacherDetailsDto>> {
        return try {
            val result = supabase.from("nauczyciele")
                .select(columns = Columns.list("nazwisko_imie", "email", "jednostka"))
                .decodeList<TeacherDetailsDto>()

            val aggregated = result.groupBy { it.name ?: "" }.filterKeys { it.isNotBlank() }.map { (name, dtos) ->
                TeacherDetailsDto(
                    name = name,
                    email = dtos.mapNotNull { it.email }.filter { it.isNotBlank() }.distinct().joinToString(" • ").ifBlank { null },
                    institute = dtos.mapNotNull { it.institute }.filter { it.isNotBlank() }.distinct().joinToString(" • ").ifBlank { null }
                )
            }.sortedBy { it.name }

            NetworkResult.Success(aggregated)
        } catch (e: Exception) {
            NetworkResult.Error("Błąd pobierania danych wykładowców.")
        }
    }

    /**
     * Pobiera plan z Supabase na żądanie i zapisuje do lokalnej bazy Room.
     * Wywołuj to z CalendarViewModel przy wejściu użytkownika na ekran.
     */
    suspend fun refreshSchedule(
        groupCode: String,
        subgroup: String?,           // pojedynczy String, nie List
        classRepository: ClassRepository
    ): NetworkResult<Unit> {
        // Zamieniamy pojedynczą podgrupę na listę której oczekuje getSchedule()
        val subgroups = if (subgroup.isNullOrBlank()) emptyList() else listOf(subgroup)

        return when (val result = getSchedule(groupCode, subgroups)) {
            is NetworkResult.Success -> {
                classRepository.syncGroupClasses(groupCode, result.data ?: emptyList())
                NetworkResult.Success(Unit)
            }
            is NetworkResult.Error -> NetworkResult.Error(result.message ?: "Nieznany błąd")
        }
    }
}