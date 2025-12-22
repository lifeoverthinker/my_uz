package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.FavoritesDao
import com.example.my_uz_android.data.models.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(
    private val favoritesDao: FavoritesDao
) {
    // Główna metoda
    fun getAllFavoritesStream(): Flow<List<FavoriteEntity>> = favoritesDao.getAllFavoritesStream()

    // ✅ FIX: Dodano alias, o który prosi kompilator
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoritesDao.getAllFavoritesStream()

    suspend fun addFavorite(name: String, type: String, resourceId: String) {
        val entity = FavoriteEntity(name = name, type = type, resourceId = resourceId)
        favoritesDao.insertFavorite(entity)
    }

    // ✅ FIX: Aliasy dla insert/delete, jeśli gdzieś są używane bezpośrednio
    suspend fun insert(entity: FavoriteEntity) = favoritesDao.insertFavorite(entity)
    suspend fun delete(entity: FavoriteEntity) = favoritesDao.deleteFavorite(entity)

    suspend fun removeFavorite(resourceId: String) {
        favoritesDao.deleteByResourceId(resourceId)
    }

    suspend fun isFavorite(resourceId: String): Boolean = favoritesDao.isFavorite(resourceId)
}