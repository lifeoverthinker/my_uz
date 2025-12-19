package com.example.my_uz_android.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.my_uz_android.data.models.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    // ✅ Poprawne zapytanie o wszystkie ulubione
    @Query("SELECT * FROM favorites ORDER BY name ASC")
    fun getAllFavoritesStream(): Flow<List<FavoriteEntity>>

    // ✅ Poprawne zapytanie sprawdzające istnienie (używamy resource_id)
    // Zwraca Boolean (suspend), co jest łatwiejsze w obsłudze niż Flow<Boolean> w tym przypadku
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE resource_id = :resourceId LIMIT 1)")
    suspend fun isFavorite(resourceId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    // ✅ Poprawne usuwanie po ID zasobu (używamy resource_id)
    @Query("DELETE FROM favorites WHERE resource_id = :resourceId")
    suspend fun deleteByResourceId(resourceId: String)
}