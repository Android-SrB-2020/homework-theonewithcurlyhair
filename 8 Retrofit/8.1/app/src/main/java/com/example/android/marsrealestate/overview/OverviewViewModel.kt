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
 *
 */

package com.example.android.marsrealestate.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.marsrealestate.network.MarsApi
import com.example.android.marsrealestate.network.MarsProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback


/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 */
enum class MarsApiStatus { LOADING, ERROR, DONE }

class OverviewViewModel : ViewModel() {

    //keep track of the current status,
    private val _status = MutableLiveData<MarsApiStatus>()

    val status: LiveData<MarsApiStatus>
        get() = _status

    //for list of MArsProperty obj
    private val _properties = MutableLiveData<List<MarsProperty>>()

    val properties: LiveData<List<MarsProperty>>
        get() = _properties

    //coroutine
    private var viewModelJob = Job()

    //coroutine scope
    private val coroutineScope = CoroutineScope(
            viewModelJob + Dispatchers.Main )
    /**
     * Call getMarsRealEstateProperties() on init so we can display status immediately.
     */
    init {
        getMarsRealEstateProperties()
    }

    /**
     * Sets the value of the status LiveData to the Mars API status.
     */
    private fun getMarsRealEstateProperties() {
        coroutineScope.launch {

            //props of retrofit service
            var getPropertiesDeferred = MarsApi.retrofitService.getProperties()

            try {
                //awaits deferred list
                var listResult = getPropertiesDeferred.await()
                _status.value = MarsApiStatus.LOADING
                if (listResult.isNotEmpty()) {
                    _properties.value = listResult
                    _status.value = MarsApiStatus.DONE
                }
            } catch (e: Exception) {
                _status.value = MarsApiStatus.ERROR
                //set to empty
                _properties.value = ArrayList()
            }
        }
    }

    //stop loading data when VM is destroyed
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
