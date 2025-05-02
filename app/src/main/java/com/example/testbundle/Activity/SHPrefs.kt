package com.example.testbundle.Activity

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "settings")


public class DataStoreRepo private constructor(
    applicationContext : Context
){

    val dataStoreFlow = applicationContext.dataStore.data



    companion object{

        val USER_ID_KEY = intPreferencesKey("userID")
        private var instance : DataStoreRepo? = null

        fun createInstance(context : Context){
            instance = DataStoreRepo(context)
        }

        fun getInstance() : DataStoreRepo = instance ?: throw NotImplementedError("")
    }
}
