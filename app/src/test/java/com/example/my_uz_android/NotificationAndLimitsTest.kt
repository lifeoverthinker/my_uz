package com.example.my_uz_android.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalTime

class NotificationAndLimitsTest {

    private fun calculateNotificationDelayInMinutes(
        currentTime: LocalTime,
        classTime: LocalTime,
        notifyBeforeMinutes: Long
    ): Long {
        val notificationTime = classTime.minusMinutes(notifyBeforeMinutes)
        return Duration.between(currentTime, notificationTime).toMinutes()
    }

    private fun checkAbsenceStatus(currentAbsences: Int, limit: Int): String {
        return when {
            currentAbsences < limit -> "OK"
            currentAbsences == limit -> "Ostrzeżenie/Limit"
            else -> "Przekroczono limit"
        }
    }

    @Test
    fun `calculateNotificationDelayInMinutes returns 105 minutes for 1 hour and 45 minutes difference`() {
        val currentTime = LocalTime.of(8, 0)
        val classTime = LocalTime.of(10, 0)
        val notifyBeforeMinutes = 15L

        val delay = calculateNotificationDelayInMinutes(currentTime, classTime, notifyBeforeMinutes)

        assertEquals(105L, delay)
    }

    @Test
    fun `calculateNotificationDelayInMinutes handles zero delay correctly`() {
        val currentTime = LocalTime.of(9, 45)
        val classTime = LocalTime.of(10, 0)
        val notifyBeforeMinutes = 15L

        val delay = calculateNotificationDelayInMinutes(currentTime, classTime, notifyBeforeMinutes)

        assertEquals(0L, delay)
    }

    @Test
    fun `checkAbsenceStatus returns OK when absences are below limit`() {
        val status = checkAbsenceStatus(currentAbsences = 1, limit = 2)

        assertEquals("OK", status)
    }

    @Test
    fun `checkAbsenceStatus returns Ostrzeżenie Limit when absences are exactly at the limit`() {
        val status = checkAbsenceStatus(currentAbsences = 2, limit = 2)

        assertEquals("Ostrzeżenie/Limit", status)
    }

    @Test
    fun `checkAbsenceStatus returns Przekroczono limit when absences exceed the limit`() {
        val status = checkAbsenceStatus(currentAbsences = 3, limit = 2)

        assertEquals("Przekroczono limit", status)
    }

    @Test
    fun `getNotificationIdForClass returns identical ID before and after Room DB insert`() {
        val classBeforeInsert = com.example.my_uz_android.data.models.ClassEntity(
            id = 0,
            subjectName = "Analiza Matematyczna",
            classType = "Wykład",
            startTime = "08:15",
            endTime = "09:45",
            dayOfWeek = 1,
            date = "2025-05-10",
            groupCode = "12-MAT-S",
            subgroup = null
        )
        val classAfterInsert = classBeforeInsert.copy(id = 42)

        val idBefore = NotificationHelper.getNotificationIdForClass(classBeforeInsert)
        val idAfter = NotificationHelper.getNotificationIdForClass(classAfterInsert)

        assertEquals(idBefore, idAfter)
    }

    @Test
    fun `getNotificationIdForClass returns different IDs for different classes`() {
        val class1 = com.example.my_uz_android.data.models.ClassEntity(
            id = 1,
            subjectName = "Analiza Matematyczna",
            classType = "Wykład",
            startTime = "08:15",
            endTime = "09:45",
            dayOfWeek = 1,
            date = "2025-05-10",
            groupCode = "12-MAT-S",
            subgroup = null
        )
        val class2 = com.example.my_uz_android.data.models.ClassEntity(
            id = 2,
            subjectName = "Fizyka",
            classType = "Laboratorium",
            startTime = "10:00",
            endTime = "11:30",
            dayOfWeek = 1,
            date = "2025-05-10",
            groupCode = "12-MAT-S",
            subgroup = null
        )

        val id1 = NotificationHelper.getNotificationIdForClass(class1)
        val id2 = NotificationHelper.getNotificationIdForClass(class2)

        org.junit.Assert.assertNotEquals(id1, id2)
    }

    @Test
    fun `getNotificationIdForClass normalizes groupCode and subjectName case and spaces`() {
        val class1 = com.example.my_uz_android.data.models.ClassEntity(
            id = 0,
            subjectName = "  Programowanie  ",
            classType = "Wykład",
            startTime = "08:15",
            endTime = "09:45",
            dayOfWeek = 1,
            date = "2025-05-10",
            groupCode = " 31-INF-S ",
            subgroup = null
        )
        val class2 = com.example.my_uz_android.data.models.ClassEntity(
            id = 99,
            subjectName = "programowanie",
            classType = "Wykład",
            startTime = "08:15",
            endTime = "09:45",
            dayOfWeek = 1,
            date = "2025-05-10",
            groupCode = "31-inf-s",
            subgroup = null
        )

        val id1 = NotificationHelper.getNotificationIdForClass(class1)
        val id2 = NotificationHelper.getNotificationIdForClass(class2)

        assertEquals(id1, id2)
    }
}