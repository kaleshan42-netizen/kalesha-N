package com.example.data.repository

import com.example.data.database.Creation
import com.example.data.database.CreationDao
import com.example.data.database.SavedPrompt
import kotlinx.coroutines.flow.Flow

class CreationRepository(private val creationDao: CreationDao) {
    val allCreations: Flow<List<Creation>> = creationDao.getAllCreations()
    val favoriteCreations: Flow<List<Creation>> = creationDao.getFavoriteCreations()
    val allSavedPrompts: Flow<List<SavedPrompt>> = creationDao.getAllSavedPrompts()

    fun getCreationsByType(type: String): Flow<List<Creation>> =
        creationDao.getCreationsByType(type)

    suspend fun insertCreation(creation: Creation) =
        creationDao.insertCreation(creation)

    suspend fun updateCreation(creation: Creation) =
        creationDao.updateCreation(creation)

    suspend fun deleteCreation(creation: Creation) =
        creationDao.deleteCreation(creation)

    suspend fun deleteCreationById(id: Int) =
        creationDao.deleteCreationById(id)

    suspend fun insertSavedPrompt(prompt: SavedPrompt) =
        creationDao.insertSavedPrompt(prompt)

    suspend fun deleteSavedPrompt(prompt: SavedPrompt) =
        creationDao.deleteSavedPrompt(prompt)
}
