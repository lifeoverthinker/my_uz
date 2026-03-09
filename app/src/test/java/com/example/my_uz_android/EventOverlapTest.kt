package com.example.my_uz_android.util

import com.example.my_uz_android.data.models.ClassEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class EventOverlapTest {

    private fun createMockClass(start: String, end: String): ClassEntity {
        return ClassEntity(
            id = 0, supabaseId = "", subjectName = "Test", classType = "L",
            startTime = start, endTime = end, date = "2026-03-08",
            dayOfWeek = 1, groupCode = "", subgroup = "", teacherName = "", room = ""
        )
    }

    @Test
    fun `Case 1 Brak nachodzenia - zajecia jedne po drugich`() {
        val classes = listOf(
            createMockClass("08:00", "09:00"),
            createMockClass("09:00", "10:00") // Dokładnie stykają się, to nie jest overlap
        )
        val layouts = calculateEventLayouts(classes)
        assertEquals(2, layouts.size)
        assertEquals(1, layouts[0].totalCols)
        assertEquals(1, layouts[1].totalCols)
        assertEquals(0, layouts[0].colIndex)
    }

    @Test
    fun `Case 2 Dwa idealnie nakladajace sie wyklady`() {
        val classes = listOf(
            createMockClass("08:00", "09:30"),
            createMockClass("08:00", "09:30")
        )
        val layouts = calculateEventLayouts(classes)
        assertEquals(2, layouts[0].totalCols)
        assertEquals(2, layouts[1].totalCols)
        assertEquals(0, layouts[0].colIndex)
        assertEquals(1, layouts[1].colIndex)
    }

    @Test
    fun `Case 3 Lancuch zazebiajacy sie (A nachodzi na B, B na C, ale A nie na C)`() {
        // A: 08-10, B: 09-11, C: 10-12
        val classes = listOf(
            createMockClass("08:00", "10:00"), // A -> Kolumna 0
            createMockClass("09:00", "11:00"), // B -> Kolumna 1
            createMockClass("10:00", "12:00")  // C -> Kolumna 0 (bo A juz sie skonczylo!)
        )
        val layouts = calculateEventLayouts(classes)
        assertEquals(3, layouts.size)
        // Wszystkie należą do jednego klastra, więc totalCols = 2
        layouts.forEach { assertEquals(2, it.totalCols) }
        assertEquals(0, layouts[0].colIndex) // A
        assertEquals(1, layouts[1].colIndex) // B
        assertEquals(0, layouts[2].colIndex) // C
    }

    @Test
    fun `Case 4 Edge case - jedno wewnatrz drugiego`() {
        val classes = listOf(
            createMockClass("08:00", "12:00"), // Ogromny blok (np. sesja)
            createMockClass("09:00", "10:00")  // Małe zajęcia w środku
        )
        val layouts = calculateEventLayouts(classes)
        assertEquals(2, layouts[0].totalCols)
        assertEquals(0, layouts.find { it.classEntity.startTime == "08:00" }?.colIndex)
        assertEquals(1, layouts.find { it.classEntity.startTime == "09:00" }?.colIndex)
    }
}