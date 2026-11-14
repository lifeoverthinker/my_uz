package com.example.my_uz_android.util

import com.example.my_uz_android.data.models.ClassEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class EventOverlapTest {

    private fun createMockClass(id: Int, startTime: String, endTime: String): ClassEntity {
        return ClassEntity(
            id = id,
            subjectName = "Przedmiot $id",
            classType = "Wykład",
            startTime = startTime,
            endTime = endTime,
            dayOfWeek = 1,
            date = "2026-03-16",
            groupCode = "GrupaTestowa",
            subgroup = null
        )
    }

    @Test
    fun `test brak nakladania sie zajec`() {
        val class1 = createMockClass(1, "08:00", "09:30")
        val class2 = createMockClass(2, "10:00", "11:30")
        val classes = listOf(class1, class2)

        val result = calculateEventLayouts(classes)

        assertEquals(2, result.size)

        val layout1 = result.find { it.classEntity.id == 1 }!!
        assertEquals("Klasa 1 - zły indeks kolumny", 0, layout1.colIndex)
        assertEquals("Klasa 1 - zła liczba całkowitych kolumn", 1, layout1.totalCols)

        val layout2 = result.find { it.classEntity.id == 2 }!!
        assertEquals("Klasa 2 - zły indeks kolumny", 0, layout2.colIndex)
        assertEquals("Klasa 2 - zła liczba całkowitych kolumn", 1, layout2.totalCols)
    }

    @Test
    fun `test dwa zajecia o tej samej godzinie`() {
        val class1 = createMockClass(1, "08:00", "09:30")
        val class2 = createMockClass(2, "08:00", "09:30")
        val classes = listOf(class1, class2)

        val result = calculateEventLayouts(classes)

        assertEquals(2, result.size)

        val layout1 = result.find { it.classEntity.id == 1 }!!
        val layout2 = result.find { it.classEntity.id == 2 }!!

        assertEquals(2, layout1.totalCols)
        assertEquals(2, layout2.totalCols)

        val columns = listOf(layout1.colIndex, layout2.colIndex)
        assert(columns.contains(0))
        assert(columns.contains(1))
    }

    @Test
    fun `test trzy zajecia nakladajace sie czesciowo`() {
        val class1 = createMockClass(1, "08:00", "10:00")
        val class2 = createMockClass(2, "09:00", "11:00")
        val class3 = createMockClass(3, "09:30", "10:30")
        val classes = listOf(class1, class2, class3)

        val result = calculateEventLayouts(classes)

        assertEquals(3, result.size)

        result.forEach { layout ->
            assertEquals("Błędna liczba całkowitych kolumn w klastrze", 3, layout.totalCols)
        }

        val layout1 = result.find { it.classEntity.id == 1 }!!
        assertEquals(0, layout1.colIndex)

        val layout2 = result.find { it.classEntity.id == 2 }!!
        assertEquals(1, layout2.colIndex)

        val layout3 = result.find { it.classEntity.id == 3 }!!
        assertEquals(2, layout3.colIndex)
    }
}