package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.models.ClassEntity
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- KONTRAKT ---
interface UniversityRepository {
    suspend fun getGroupCodes(): List<String>
    suspend fun getSubgroups(groupCode: String): List<String>
    suspend fun getGroupDetails(groupCode: String): GroupDetailsDto?

    // NOWA METODA
    suspend fun getSchedule(groupCode: String): List<ClassEntity>
}

// --- MODELE DANYCH (DTO) ---

@Serializable
data class GroupCodeDto(@SerialName("kod_grupy") val code: String)

@Serializable
data class SubgroupDto(@SerialName("podgrupa") val subgroup: String)

@Serializable
data class GroupDetailsDto(
    @SerialName("kod_grupy") val groupCode: String,
    @SerialName("tryb_studiow") val studyMode: String,
    @SerialName("kierunki") val fieldInfo: FieldOfStudyDto? = null
)

@Serializable
data class FieldOfStudyDto(
    @SerialName("nazwa") val name: String,
    @SerialName("wydzial") val faculty: String
)

// DTO dla Planu Zajęć (Dopasuj nazwy kolumn do swojej bazy Supabase!)
@Serializable
data class ClassDto(
    @SerialName("nazwa_przedmiotu") val subjectName: String,
    @SerialName("typ_zajec") val type: String,
    @SerialName("prowadzacy") val teacher: String?,
    @SerialName("sala") val room: String?,
    @SerialName("dzien_tygodnia") val dayOfWeek: Int, // 1=Pon, 5=Pt
    @SerialName("godzina_start") val startTime: String,
    @SerialName("godzina_koniec") val endTime: String,
    @SerialName("podgrupa") val subgroup: String?
)

// --- IMPLEMENTACJA ---
class SupabaseUniversityRepository(private val supabase: Postgrest) : UniversityRepository {

    override suspend fun getGroupCodes(): List<String> {
        return supabase.from("grupy")
            .select(columns = Columns.list("kod_grupy"))
            .decodeList<GroupCodeDto>()
            .map { it.code }
            .distinct()
            .sorted()
    }

    override suspend fun getSubgroups(groupCode: String): List<String> {
        return supabase.from("zajecia_grupy")
            .select(columns = Columns.list("podgrupa", "grupy!inner(kod_grupy)")) {
                filter {
                    eq("grupy.kod_grupy", groupCode)
                    neq("podgrupa", "")
                    neq("podgrupa", "null")
                }
            }
            .decodeList<SubgroupDto>()
            .map { it.subgroup }
            .distinct()
            .sorted()
    }

    override suspend fun getGroupDetails(groupCode: String): GroupDetailsDto? {
        return try {
            supabase.from("grupy")
                .select(
                    columns = Columns.list(
                        "kod_grupy",
                        "tryb_studiow",
                        "kierunki(nazwa, wydzial)"
                    )
                ) {
                    filter { eq("kod_grupy", groupCode) }
                }
                .decodeSingleOrNull<GroupDetailsDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Pobieranie planu i mapowanie na encje lokalne
    override suspend fun getSchedule(groupCode: String): List<ClassEntity> {
        return try {
            val result = supabase.from("zajecia_grupy")
                .select(
                    columns = Columns.list(
                        "nazwa_przedmiotu", "typ_zajec", "prowadzacy", "sala",
                        "dzien_tygodnia", "godzina_start", "godzina_koniec", "podgrupa"
                    )
                ) {
                    filter {
                        // Zakładając, że tabela 'zajecia_grupy' ma relację do 'grupy'
                        // lub kolumnę 'kod_grupy'. Dostosuj jeśli w bazie jest inaczej.
                        eq("kod_grupy", groupCode)
                    }
                }
                .decodeList<ClassDto>()

            result.map { dto ->
                ClassEntity(
                    subjectName = dto.subjectName,
                    startTime = dto.startTime.take(5),
                    endTime = dto.endTime.take(5),
                    dayOfWeek = dto.dayOfWeek,
                    room = dto.room ?: "",
                    classType = dto.type,
                    // POPRAWKA 1: teacherName zamiast lecturer
                    teacherName = dto.teacher ?: "",
                    subgroup = dto.subgroup,
                    // POPRAWKA 2: Przekazujemy groupCode otrzymany w argumencie funkcji
                    groupCode = groupCode
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}