package com.example.my_uz_android.util

import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.UserCourseEntity

/**
 * Zielony komentarz:
 * Wspólny matcher podgrup dla całej appki (kalendarz + indeks).
 * Dzięki temu unikamy sytuacji, że jeden ekran pokazuje zajęcia, a drugi je gubi.
 */
object SubgroupMatcher {

    private val COMMON_TOKENS = setOf("all", "-", "brak", "w", "wyk")

    fun isCommonClassSubgroup(raw: String?): Boolean {
        val tokens = tokenizeCsv(raw)
        if (tokens.isEmpty()) return true
        return tokens.any { it in COMMON_TOKENS }
    }

    fun matchesSubgroups(classSubgroupRaw: String?, allowedSubgroupsRaw: List<String?>): Boolean {
        val classTokens = tokenizeCsv(classSubgroupRaw)
        val allowedTokens = allowedSubgroupsRaw.flatMap { tokenizeCsv(it) }.toSet()

        // 1) Brak podgrupy w zajeciach -> zajecia dla calego kierunku/roku
        if (classTokens.isEmpty()) return true

        // 2) Znaczniki zajec ogolnych
        if (classTokens.any { it in COMMON_TOKENS }) return true

        // 3) Brak filtrowania po stronie usera -> pokaz wszystko
        if (allowedTokens.isEmpty()) return true

        // 4) Najwazniejsze: czesc wspolna zbiorow
        return classTokens.any { it in allowedTokens }
    }

    fun matches(classSubgroupRaw: String?, selectedSubgroupsRaw: List<String?>): Boolean {
        return matchesSubgroups(classSubgroupRaw, selectedSubgroupsRaw)
    }

    fun tokenize(raw: String?): List<String> {
        val normalized = normalize(raw)
        if (normalized.isBlank()) return emptyList()

        return normalized
            .split(Regex("[,;/|\\s]+"))
            .map { alias(it.trim()) }
            .filter { it.isNotBlank() }
    }

    private fun tokenizeCsv(raw: String?): List<String> {
        return raw
            ?.split(Regex("[,;/|\\s]+"))
            ?.map { alias(it.trim().lowercase()) }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    private fun normalize(raw: String?): String = raw?.trim()?.lowercase().orEmpty()

    private fun alias(token: String): String {
        return when (token) {
            "wyklad", "wykład", "wyk", "w" -> "wyk"
            "seminarium", "sem" -> "sem"
            else -> token
        }
    }

    // ==========================================
    // PANCERNA REGUŁA FILTROWANIA (ZINTEGROWANA)
    // ==========================================

    fun buildUserEnrollments(
        settings: SettingsEntity?,
        courses: List<UserCourseEntity>,
        activeGroupCodesLower: Set<String>? = null
    ): Map<String, List<String>> {
        val enrollments = mutableMapOf<String, MutableList<String>>()

        fun addEnrollment(groupCode: String?, subgroupRaw: String?) {
            val code = groupCode?.trim()?.lowercase() ?: return
            if (code.isBlank()) return
            if (activeGroupCodesLower != null && code !in activeGroupCodesLower) return

            if (!enrollments.containsKey(code)) enrollments[code] = mutableListOf()

            val tokens = tokenizeCsv(subgroupRaw)
            if (tokens.isNotEmpty()) enrollments[code]!!.addAll(tokens)
        }

        settings?.selectedGroupCode?.let { addEnrollment(it, settings.selectedSubgroup) }
        courses.forEach { addEnrollment(it.groupCode, it.selectedSubgroup) }

        return enrollments
    }

    fun isClassVisible(
        classGroupCode: String?,
        classType: String?,
        classSubgroup: String?,
        userEnrollments: Map<String, List<String>>
    ): Boolean {
        val code = classGroupCode?.trim()?.lowercase() ?: return false
        if (!userEnrollments.containsKey(code)) return false

        val userSubgroups = userEnrollments[code] ?: emptyList()

        // a) Rozszerzone sprawdzanie typu zajęć (Wykłady, Seminaria, Egzaminy są dla całej grupy)
        val fullTypeName = ClassTypeUtils.getFullName(classType?.trim()).lowercase()
        val isCommonType = fullTypeName.contains("wykład") ||
                fullTypeName.contains("seminarium") ||
                fullTypeName.contains("egzamin") ||
                fullTypeName.contains("samokształcenie")

        if (isCommonType) return true

        // b/c/d) Regula intersection + warunki brzegowe podgrup
        return matchesSubgroups(classSubgroup, userSubgroups)
    }
}