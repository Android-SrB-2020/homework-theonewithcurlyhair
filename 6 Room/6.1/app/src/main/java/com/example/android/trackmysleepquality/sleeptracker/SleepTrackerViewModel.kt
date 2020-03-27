/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    //instance of Job for coroutine
    private var viewModelJob = Job()

    //scope where coroutine will be run on
    //main thread
    private val uiScope = CoroutineScope(Dispatchers.Main
     + viewModelJob)

    //all nights from DB
    private val nights = database.getAllNights()

    //transform object so that properties are displayed
    //not the reference
    val nightsString = Transformations.map(nights) {
        nights -> formatNights(nights, application.resources)
    }

    //hold current night record from DB
    private var tonight = MutableLiveData<SleepNight?>()


    //navigate to sleep quality
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
    get() = _navigateToSleepQuality

    fun doneNavigating(){
        _navigateToSleepQuality.value = null
    }

    val startButtonVisible = Transformations.map(tonight){
        it == null
    }

    val stopButtonVisible = Transformations.map(tonight){
        it != null
    }

    val clearButtonVisible = Transformations.map(nights){
        it?.isNotEmpty()
    }

    //show Snackbar
    private var _showSnackBarEvent = MutableLiveData<Boolean>()
    val showSnackBarEvent: LiveData<Boolean>
    get() = _showSnackBarEvent

    fun doneShowingSnackbar(){
        _showSnackBarEvent.value = false
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight(){
        //launch coroutine to get a record from DB async
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    //get record from DB function using another coroutine
    //on another thread
    private suspend fun getTonightFromDatabase():
    SleepNight?{
        //use IO dispatcher to get data
        return withContext(Dispatchers.IO){
            //get the newest record from DB
            var night = database.getTonight()
            if(night?.endTimeMilli != night?.startTimeMilli){
                night = null
            }
            night
        }
    }

    //UI click handlers
    //when Start clicked
    fun onStartTracking(){
        uiScope.launch {
            val newNight = SleepNight()
            insert(newNight)

            //update record
            tonight.value  = getTonightFromDatabase()
         }
    }

    //insert into db
    private suspend fun insert(night: SleepNight){
        //coroutine func
        withContext(Dispatchers.IO){
            database.insert(night)
        }
    }

    //Stop is clicked -> update record in DB with end time
    fun onStopTracking(){
        uiScope.launch {
            //return@label syntax specifies the function from which this statement returns, among several nested functions.
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)

            //trigger navigation -> when prop has a value navigation is triggered and
            //value is passed
            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(night: SleepNight){
        withContext(Dispatchers.IO){
            database.update(night)
        }
    }

    //clear btn clicked
    fun onClear(){
        uiScope.launch {
            clear()
            tonight.value = null
            _showSnackBarEvent.value = true
        }
    }

    suspend fun clear(){
        withContext(Dispatchers.IO){
            database.clear()
        }
    }

    //cancel coroutines when VM is destroyed
    override fun onCleared(){
        super.onCleared()
        viewModelJob.cancel()
    }





}

