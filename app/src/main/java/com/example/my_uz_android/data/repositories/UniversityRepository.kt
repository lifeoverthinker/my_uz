package com.example.my_uz_android.data.repositories

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- KONTRAKT ---
interface UniversityRepository {
    suspend fun getGroupCodes(): List<String>
    suspend fun getSubgroups(groupCode: String): List<String>
    suspend fun getGroupDetails(groupCode: String): GroupDetailsDto?
}

// --- MODELE DANYCH (DTO) ---

@Serializable
data class GroupCodeDto(@SerialName("kod_grupy") val code: String)

@Serializable
data class SubgroupDto(@SerialName("podgrupa") val subgroup: String)

// Struktura do pobrania szczegółów grupy (z relacją do kierunków)
@Serializable
data class GroupDetailsDto(
    @SerialName("kod_grupy") val groupCode: String,
    @SerialName("tryb_studiow") val studyMode: String,
    // Relacja z tabelą kierunki
    @SerialName("kierunki") val fieldInfo: FieldOfStudyDto? = null
)

@Serializable
data class FieldOfStudyDto(
    @SerialName("nazwa") val name: String,
    @SerialName("wydzial") val faculty: String
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

    // NOWA METODA: Pobiera szczegóły grupy + kierunek + wydział
    override suspend fun getGroupDetails(groupCode: String): GroupDetailsDto? {
        return try {
            // Select: wybierz kod_grupy, tryb_studiow oraz złączoną tabelę kierunki (nazwa, wydzial)
            // Zakładamy, że w Supabase relacja nazywa się 'kierunki' (zgodnie z nazwą tabeli)
            val result = supabase.from("grupy")
                .select(columns = Columns.list("kod_grupy", "tryb_studiow", "kierunki(nazwa, wydzial)")) {
                    filter {
                        eq("kod_grupy", groupCode)
                    }
                }
                .decodeSingleOrNull<GroupDetailsDto>()

            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}