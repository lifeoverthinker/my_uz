package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.FavoritesDao
import com.example.my_uz_android.data.models.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(
    private val favoritesDao: FavoritesDao
) {
    // ✅ Nazwa metody: getAllFavoritesStream
    fun getAllFavoritesStream(): Flow<List<FavoriteEntity>> = favoritesDao.getAllFavoritesStream()

    suspend fun addFavorite(name: String, type: String, resourceId: String) {
        val entity = FavoriteEntity(name = name, type = type, resourceId = resourceId)
        favoritesDao.insertFavorite(entity)
    }

    suspend fun removeFavorite(resourceId: String) {
        favoritesDao.deleteByResourceId(resourceId)
    }

    suspend fun isFavorite(resourceId: String): Boolean = favoritesDao.isFavorite(resourceId)
}