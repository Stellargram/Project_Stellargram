package com.ssafy.stellargram.data.db.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ssafy.stellargram.data.db.dao.StarDAO
import com.ssafy.stellargram.data.db.entity.Star
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StarRepository (private val starDAO: StarDAO) {




    val allstars = MutableLiveData<List<Star>>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

//    fun updateAllStars() {
//        coroutineScope.launch(Dispatchers.IO) {
//            starDAO.findAll()
//        }
//    }

    fun getAllStars() {
        coroutineScope.launch(Dispatchers.IO) {
            allstars.postValue(starDAO.findAll())
            Log.d("GETSTAR", "$allstars")
        }
    }
}


