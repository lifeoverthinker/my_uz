package com.example.my_uz_android.sync

import com.example.my_uz_android.data.daos.ClassDao
import com.example.my_uz_android.data.models.ClassEntity
import kotlinx.coroutines.flow.first

// 1. Definicja możliwych zmian w planie
sealed class ScheduleChange {
    abstract val classEntity: ClassEntity

    data class Added(override val classEntity: ClassEntity) : ScheduleChange()
    data class Canceled(override val classEntity: ClassEntity) : ScheduleChange()
    data class Modified(
        val oldClass: ClassEntity,
        val newClass: ClassEntity
    ) : ScheduleChange() {
        override val classEntity: ClassEntity get() = newClass
    }
}

// 2. Właściwy menedżer synchronizacji
class ScheduleSyncManager(private val classDao: ClassDao) {

    suspend fun compareSchedules(remoteClasses: List<ClassEntity>): List<ScheduleChange> {
        val changes = mutableListOf<ScheduleChange>()

        // Pobieramy obecny stan z bazy Room
        val localClasses = classDao.getAllClasses().first()

        // Tworzymy mapy dla szybkiego wyszukiwania po unikalnym supabaseId
        val localMap = localClasses.associateBy { it.supabaseId }
        val remoteMap = remoteClasses.associateBy { it.supabaseId }

        // Szukamy DODANYCH i ZMODYFIKOWANYCH zajęć
        for (remoteClass in remoteClasses) {
            val id = remoteClass.supabaseId ?: continue
            val localClass = localMap[id]

            if (localClass == null) {
                // Nie było takiego ID w bazie - nowe zajęcia
                changes.add(ScheduleChange.Added(remoteClass))
            } else {
                // ID istnieje - sprawdzamy, czy jakieś kluczowe dane uległy zmianie
                if (hasDifferences(localClass, remoteClass)) {
                    changes.add(ScheduleChange.Modified(oldClass = localClass, newClass = remoteClass))
                }
            }
        }

        // Szukamy ODWOŁANYCH (usuniętych) zajęć
        for (localClass in localClasses) {
            val id = localClass.supabaseId
            if (id != null && !remoteMap.containsKey(id)) {
                // Zajęcia są w bazie lokalnej, ale zniknęły z planu uczelni
                changes.add(ScheduleChange.Canceled(localClass))
            }
        }

        return changes
    }

    private fun hasDifferences(local: ClassEntity, remote: ClassEntity): Boolean {
        // Zwraca true, jeśli wykryje jakąkolwiek różnicę w kluczowych polach
        return local.room != remote.room ||
                local.startTime != remote.startTime ||
                local.endTime != remote.endTime ||
                local.date != remote.date ||
                local.teacherName != remote.teacherName ||
                local.subjectName != remote.subjectName ||
                local.classType != remote.classType
    }
}