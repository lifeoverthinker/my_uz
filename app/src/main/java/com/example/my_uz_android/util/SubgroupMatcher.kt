package com.example.my_uz_android.util

import com.example.my_uz_android.data.models.SettingsEntity
import com.example.my_uz_android.data.models.UserCourseEntity

object SubgroupMatcher {

    private val COMMON_TOKENS = setOf("all", "-", "brak", "w", "wyk", "wyklad", "wykład", "sem", "seminarium")

    fun matchesSubgroups(classSubgroupRaw: String?, selectedSubgroupsRaw: List<String?>): Boolean {
        val classTokens = classSubgroupRaw?.split(Regex("[,;/|\\s]+"))
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        if (classTokens.isEmpty() || classTokens.any { it in COMMON_TOKENS }) return true

        val allowedTokens = selectedSubgroupsRaw.mapNotNull { it?.trim()?.lowercase() }
            .flatMap { it.split(Regex("[,;/|\\s]+")) }
            .filter { it.isNotBlank() }

        if (allowedTokens.isEmpty()) return true

        return classTokens.any { classToken ->
            allowedTokens.any { allowedToken ->
                classToken == allowedToken || classToken.contains(allowedToken)
            }
        }
    }

    fun matches(classSubgroupRaw: String?, selectedSubgroupsRaw: List<String?>): Boolean =
        matchesSubgroups(classSubgroupRaw, selectedSubgroupsRaw)

    fun buildUserEnrollments(
        settings: SettingsEntity?,
        courses: List<UserCourseEntity>,
        activeGroupCodesLower: Set<String>? = null
    ): Map<String, List<String>> {
        val enrollments = mutableMapOf<String, MutableList<String>>()

        fun addEnrollment(groupCode: String?, subgroupRaw: String?) {
            val code = groupCode?.trim()?.lowercase() ?: return
            if (code.isBlank()) return
            if (activeGroupCodesLower != null && !activeGroupCodesLower.contains(code)) return

            val tokens = subgroupRaw?.split(Regex("[,;/|\\s]+"))
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()

            enrollments.getOrPut(code) { mutableListOf() }.addAll(tokens)
        }

        addEnrollment(settings?.selectedGroupCode, settings?.selectedSubgroup)
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
        val userSubgroups = userEnrollments[code] ?: return false

        if (userSubgroups.isEmpty()) return true

        val classTokens = classSubgroup?.split(Regex("[,;/|\\s]+"))
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        if (classTokens.isEmpty() || classTokens.any { it in COMMON_TOKENS }) return true

        val typeName = classType?.trim()?.lowercase() ?: ""
        if (typeName.contains("wykład") || typeName.contains("wyklad") ||
            typeName.contains("sem") || typeName.contains("egzamin") ||
            typeName.contains("samokształcenie")) {
            return true
        }

        return classTokens.any { classToken ->
            userSubgroups.any { userToken ->
                classToken == userToken || classToken.contains(userToken)
            }
        }
    }
}
