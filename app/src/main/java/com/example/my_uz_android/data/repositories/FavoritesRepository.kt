package com.example.my_uz_android.data.repositories

import com.example.my_uz_android.data.daos.FavoritesDao
import com.example.my_uz_android.data.models.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val favoritesDao: FavoritesDao) {

    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoritesDao.getAllFavorites()

    fun isFavorite(code: String, type: String): Flow<Boolean> = favoritesDao.isFavorite(code, type)

    suspend fun addFavorite(name: String, type: String, code: String) {
        val favorite = FavoriteEntity(name = name, type = type, code = code)
        favoritesDao.insertFavorite(favorite)
    }

    suspend fun removeFavorite(favorite: FavoriteEntity) {
        favoritesDao.deleteFavorite(favorite)
    }
}