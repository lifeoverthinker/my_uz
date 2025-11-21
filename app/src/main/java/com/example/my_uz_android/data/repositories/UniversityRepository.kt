package com.example.my_uz_android.data.repositories

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- 1. INTERFEJS (Kontrakt) ---
interface UniversityRepository {
    suspend fun getGroupCodes(): List<String>
    suspend fun getSubgroups(groupCode: String): List<String>
}

// --- 2. IMPLEMENTACJA (Supabase) ---

// Modele danych (DTO) widoczne tylko w tym pliku
@Serializable
data class GroupCodeDto(@SerialName("kod_grupy") val code: String)

@Serializable
data class SubgroupDto(@SerialName("podgrupa") val subgroup: String)

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
        // Używamy 'grupy!inner(kod_grupy)', co odpowiada INNER JOIN w SQL
        return supabase.from("zajecia_grupy")
            .select(columns = Columns.list("podgrupa", "grupy!inner(kod_grupy)")) {
                filter {
                    // WHERE g.kod_grupy = groupCode
                    eq("grupy.kod_grupy", groupCode)

                    // WHERE zg.podgrupa != ''
                    neq("podgrupa", "")

                    // WHERE zg.podgrupa IS NOT NULL (w PostgREST API null to specjalna wartość)
                    neq("podgrupa", "null")
                }
            }
            .decodeList<SubgroupDto>()
            .map { it.subgroup }
            .distinct()
            .sorted()
    }
}