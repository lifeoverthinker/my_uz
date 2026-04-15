package com.example.my_uz_android

import com.example.my_uz_android.data.models.ClassEntity
import com.example.my_uz_android.util.classesStillRemainingToday
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ClassTimeUtilsTest {

    @Test
    fun `filtr endTime zwraca tylko zajecia ktore jeszcze trwaja dzisiaj`() {
        val today = LocalDate.of(2026, 4, 15)
        val now = LocalTime.of(10, 0)

        val classes = listOf(
            baseClass(id = 1, date = today.toString(), endTime = "09:30"),
            baseClass(id = 2, date = today.toString(), endTime = "10:30"),
            baseClass(id = 3, date = today.plusDays(1).toString(), endTime = "12:00"),
            baseClass(id = 4, date = today.toString(), endTime = "BAD")
        )

        val result = classesStillRemainingToday(classes, today, now)

        assertEquals(1, result.size)
        assertEquals(2, result.first().id)
    }

    private fun baseClass(id: Int, date: String, endTime: String): ClassEntity {
        return ClassEntity(
            id = id,
            subjectName = "Test",
            classType = "W",
            startTime = "08:00",
            endTime = endTime,
            dayOfWeek = 3,
            date = date,
            groupCode = "G1",
            subgroup = null
        )
    }
}

