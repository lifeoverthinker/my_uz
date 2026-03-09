package com.example.my_uz_android.util

import com.example.my_uz_android.data.models.ClassEntity
import kotlin.math.max

data class EventLayoutInfo(
    val classEntity: ClassEntity,
    val colIndex: Int,
    val totalCols: Int
)

fun calculateEventLayouts(classes: List<ClassEntity>): List<EventLayoutInfo> {
    if (classes.isEmpty()) return emptyList()

    // Pomocnicza funkcja: "HH:mm" -> minuty od północy
    fun timeToMins(time: String): Int {
        val parts = time.split(":")
        if (parts.size != 2) return 0
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    // 1. Sortowanie: Najpierw te, co zaczynają się wcześniej.
    // Jeśli startują tak samo, to najpierw dłuższe zajęcia.
    val sorted = classes.sortedWith(
        compareBy({ timeToMins(it.startTime) }, { -timeToMins(it.endTime) })
    )

    val result = mutableListOf<EventLayoutInfo>()
    val clusters = mutableListOf<List<ClassEntity>>()
    var currentCluster = mutableListOf<ClassEntity>()
    var clusterEnd = 0

    // 2. Klastrowanie (grupowanie połączonych czasowo zajęć)
    for (c in sorted) {
        val start = timeToMins(c.startTime)
        val end = timeToMins(c.endTime)

        if (currentCluster.isEmpty()) {
            currentCluster.add(c)
            clusterEnd = end
        } else {
            if (start < clusterEnd) { // Overlap!
                currentCluster.add(c)
                clusterEnd = max(clusterEnd, end)
            } else { // Brak nachodzenia, nowy klaster
                clusters.add(currentCluster)
                currentCluster = mutableListOf(c)
                clusterEnd = end
            }
        }
    }
    if (currentCluster.isNotEmpty()) clusters.add(currentCluster)

    // 3. Przydział do kolumn w każdym klastrze (Zachłannie / Greedy)
    for (cluster in clusters) {
        val columns = mutableListOf<MutableList<ClassEntity>>()
        val classToCol = mutableMapOf<ClassEntity, Int>()

        for (c in cluster) {
            val start = timeToMins(c.startTime)
            var placed = false

            // Szukamy pierwszej kolumny, w której ostatnie zajęcia skończyły się przed startem obecnych
            for (i in columns.indices) {
                val lastEventEnd = timeToMins(columns[i].last().endTime)
                if (start >= lastEventEnd) {
                    columns[i].add(c)
                    classToCol[c] = i
                    placed = true
                    break
                }
            }
            // Jeśli nie ma wolnej, tworzymy nową kolumnę
            if (!placed) {
                columns.add(mutableListOf(c))
                classToCol[c] = columns.size - 1
            }
        }

        val totalCols = columns.size
        for (c in cluster) {
            result.add(EventLayoutInfo(c, classToCol[c] ?: 0, totalCols))
        }
    }

    return result
}