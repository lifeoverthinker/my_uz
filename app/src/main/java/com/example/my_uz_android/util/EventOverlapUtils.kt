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

    val sortedClasses = classes.sortedWith(
        compareBy({ it.startTime.toMinutes() }, { -it.endTime.toMinutes() })
    )

    val result = mutableListOf<EventLayoutInfo>()
    val clusters = mutableListOf<List<ClassEntity>>()
    var currentCluster = mutableListOf<ClassEntity>()
    var clusterEnd = 0

    for (classItem in sortedClasses) {
        val start = classItem.startTime.toMinutes()
        val end = classItem.endTime.toMinutes()

        if (currentCluster.isEmpty()) {
            currentCluster.add(classItem)
            clusterEnd = end
        } else if (start < clusterEnd) {
            currentCluster.add(classItem)
            clusterEnd = max(clusterEnd, end)
        } else {
            clusters.add(currentCluster)
            currentCluster = mutableListOf(classItem)
            clusterEnd = end
        }
    }

    if (currentCluster.isNotEmpty()) {
        clusters.add(currentCluster)
    }

    for (cluster in clusters) {
        val columns = mutableListOf<MutableList<ClassEntity>>()
        val classToColumnMap = mutableMapOf<ClassEntity, Int>()

        for (classItem in cluster) {
            val start = classItem.startTime.toMinutes()
            var isPlaced = false

            for (i in columns.indices) {
                if (start >= columns[i].last().endTime.toMinutes()) {
                    columns[i].add(classItem)
                    classToColumnMap[classItem] = i
                    isPlaced = true
                    break
                }
            }

            if (!isPlaced) {
                columns.add(mutableListOf(classItem))
                classToColumnMap[classItem] = columns.size - 1
            }
        }

        val totalCols = columns.size
        for (classItem in cluster) {
            result.add(
                EventLayoutInfo(
                    classEntity = classItem,
                    colIndex = classToColumnMap[classItem] ?: 0,
                    totalCols = totalCols
                )
            )
        }
    }

    return result
}

private fun String.toMinutes(): Int {
    val parts = this.split(":")
    if (parts.size != 2) return 0
    return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
}
