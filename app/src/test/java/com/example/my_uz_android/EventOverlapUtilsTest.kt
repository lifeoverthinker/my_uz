package com.example.my_uz_android.util

import com.example.my_uz_android.data.models.ClassEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class EventOverlapUtilsTest {

    // Metoda pomocnicza do szybkiego tworzenia obiektów zajęć na potrzeby testów
    private fun createMockClass(id: Int, startTime: String, endTime: String): ClassEntity {
        return ClassEntity(
            id = id,
            subjectName = "Przedmiot $id",
            classType = "Wykład",
            startTime = startTime,
            endTime = endTime,
            dayOfWeek = 1,
            date = "2026-03-16", // Przykładowa data
            groupCode = "GrupaTestowa",
            subgroup = null
        )
    }

    @Test
    fun `test brak nakladania sie zajec`() {
        // Given (Przygotowanie danych)
        val class1 = createMockClass(1, "08:00", "09:30")
        val class2 = createMockClass(2, "10:00", "11:30")
        val classes = listOf(class1, class2)

        // When (Wykonanie algorytmu)
        val result = calculateEventLayouts(classes)

        // Then (Sprawdzenie wyników)
        assertEquals(2, result.size)

        // Klasa 1 powinna zajmować całą szerokość (totalCols = 1)
        val layout1 = result.find { it.classEntity.id == 1 }!!
        assertEquals("Klasa 1 - zły indeks kolumny", 0, layout1.colIndex)
        assertEquals("Klasa 1 - zła liczba całkowitych kolumn", 1, layout1.totalCols)

        // Klasa 2 również powinna zajmować całą szerokość (totalCols = 1)
        val layout2 = result.find { it.classEntity.id == 2 }!!
        assertEquals("Klasa 2 - zły indeks kolumny", 0, layout2.colIndex)
        assertEquals("Klasa 2 - zła liczba całkowitych kolumn", 1, layout2.totalCols)
    }

    @Test
    fun `test dwa zajecia o tej samej godzinie`() {
        // Given
        val class1 = createMockClass(1, "08:00", "09:30")
        val class2 = createMockClass(2, "08:00", "09:30")
        val classes = listOf(class1, class2)

        // When
        val result = calculateEventLayouts(classes)

        // Then
        assertEquals(2, result.size)

        // Obie klasy powinny dzielić przestrzeń na pół (totalCols = 2)
        val layout1 = result.find { it.classEntity.id == 1 }!!
        val layout2 = result.find { it.classEntity.id == 2 }!!

        assertEquals(2, layout1.totalCols)
        assertEquals(2, layout2.totalCols)

        // Jeden element musi być w kolumnie 0, drugi w kolumnie 1
        val columns = listOf(layout1.colIndex, layout2.colIndex)
        assert(columns.contains(0))
        assert(columns.contains(1))
    }

    @Test
    fun `test trzy zajecia nakladajace sie czesciowo`() {
        // Given: Trzy zajęcia tworzące kaskadę
        val class1 = createMockClass(1, "08:00", "10:00")
        val class2 = createMockClass(2, "09:00", "11:00")
        val class3 = createMockClass(3, "09:30", "10:30")
        val classes = listOf(class1, class2, class3)

        // When
        val result = calculateEventLayouts(classes)

        // Then
        assertEquals(3, result.size)

        // Algorytm powinien zgrupować je wszystkie w jeden klaster o 3 kolumnach (totalCols = 3)
        result.forEach { layout ->
            assertEquals("Błędna liczba całkowitych kolumn w klastrze", 3, layout.totalCols)
        }

        // Klasa 1 zaczyna się najwcześniej (kolumna 0)
        val layout1 = result.find { it.classEntity.id == 1 }!!
        assertEquals(0, layout1.colIndex)

        // Klasa 2 jest druga (kolumna 1)
        val layout2 = result.find { it.classEntity.id == 2 }!!
        assertEquals(1, layout2.colIndex)

        // Klasa 3 startuje, gdy obie wcześniejsze wciąż trwają (kolumna 2)
        val layout3 = result.find { it.classEntity.id == 3 }!!
        assertEquals(2, layout3.colIndex)
    }
}