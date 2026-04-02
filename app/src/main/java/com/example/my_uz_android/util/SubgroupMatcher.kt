package com.example.my_uz_android.util

/**
 * Zielony komentarz:
 * Wspólny matcher podgrup dla całej appki (kalendarz + indeks).
 * Dzięki temu unikamy sytuacji, że jeden ekran pokazuje zajęcia, a drugi je gubi.
 */
object SubgroupMatcher {

private val COMMON_TOKENS = setOf(
        "", "all", "brak", "empty", "-", "wyk", "w", "wyklad", "wykład", "sem", "seminarium"
)

/**
 * True, jeśli dana podgrupa zajęć jest "wspólna" dla wszystkich.
 */
fun isCommonClassSubgroup(raw: String?): Boolean {
    val norm = normalize(raw)
    if (norm.isBlank()) return true
    if (norm in COMMON_TOKENS) return true
    if (norm.startsWith("wyk")) return true
    if (norm.startsWith("sem")) return true
    return false
}

/**
 * True, jeśli zajęcia (classSubgroupRaw) pasują do dowolnej wybranej podgrupy usera.
 *
 * selectedSubgroupsRaw może zawierać:
 * - pojedyncze wartości: "B"
 * - wiele wartości: "B,II", "I/II", "A|B"
 */
fun matches(classSubgroupRaw: String?, selectedSubgroupsRaw: List<String?>): Boolean {
    if (isCommonClassSubgroup(classSubgroupRaw)) return true

    val classTokens = tokenize(classSubgroupRaw).toSet()
    if (classTokens.isEmpty()) return false

    val userTokens = selectedSubgroupsRaw
            .flatMap { tokenize(it) }
            .toSet()

    if (userTokens.isEmpty()) return false
    if ("all" in userTokens) return true

    return classTokens.any { it in userTokens }
}

fun tokenize(raw: String?): List<String> {
    val normalized = normalize(raw)
    if (normalized.isBlank()) return emptyList()

    return normalized
            .split(Regex("[,;/|\\s]+"))
            .map { alias(it.trim()) }
            .filter { it.isNotBlank() }
}

private fun normalize(raw: String?): String = raw?.trim()?.lowercase().orEmpty()

private fun alias(token: String): String {
    return when (token) {
        "wyklad", "wykład", "wyk", "w" -> "wyk"
        "seminarium", "sem" -> "sem"
            else -> token
    }
}
}