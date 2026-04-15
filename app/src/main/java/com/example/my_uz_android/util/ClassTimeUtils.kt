package com.example.my_uz_android.util

import com.example.my_uz_android.data.models.ClassEntity
import java.time.LocalDate
import java.time.LocalTime

fun isClassStillRemainingToday(
    classItem: ClassEntity,
    today: LocalDate,
    nowTime: LocalTime
): Boolean {
    if (classItem.date != today.toString()) return false
    val endTime = runCatching { LocalTime.parse(classItem.endTime) }.getOrNull() ?: return false
    return endTime.isAfter(nowTime)
}

fun classesStillRemainingToday(
    classes: List<ClassEntity>,
    today: LocalDate,
    nowTime: LocalTime
): List<ClassEntity> {
    return classes.filter { isClassStillRemainingToday(it, today, nowTime) }
}

