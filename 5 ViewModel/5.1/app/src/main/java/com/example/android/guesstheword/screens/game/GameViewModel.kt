/*
 * Copyright (C) 2019 Google Inc.
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

package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

/**
 * Contains all the logic needed to run the game
 */
class GameViewModel : ViewModel() {
    private val timer: CountDownTimer

    //create a companion obj to hold the timer - CountDownTimer class
    companion object {

        // Time when the game is over
        private const val DONE = 0L

        // Countdown time interval
        private const val ONE_SECOND = 1000L

        // Total time
        private const val COUNTDOWN_TIME = 60000L

    }

    // current word
    private val _word = MutableLiveData<String>()
    val word: LiveData<String>
        get() = _word

    // score
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score

    val hint = Transformations.map(word) {word ->
        val randomLetter = (1..word.length).random()
        "Current word has " + word.length + " letters " +
        "\nThe letter at position " + randomLetter + " is " +
        word.get(randomLetter - 1).toUpperCase()
    }


    // Countdown time
    private val _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long>
        get() = _currentTime


    // string for current time -> can be bind in xml
    val currentTimeString = Transformations.map(currentTime) { time ->
        DateUtils.formatElapsedTime(time)
    }

    // Event which triggers the end of the game
    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish: LiveData<Boolean>
        get() = _eventGameFinish


    private lateinit var wordList: MutableList<String>


    private fun resetList() {
        wordList = mutableListOf(
                "queen",
                "hospital",
                "basketball",
                "cat",
                "change",
                "snail",
                "soup",
                "calendar",
                "sad",
                "desk",
                "guitar",
                "home",
                "railway",
                "zebra",
                "jelly",
                "car",
                "crow",
                "trade",
                "bag",
                "roll",
                "bubble"
        )
        wordList.shuffle()
    }

    init {
        _word.value = ""
        _score.value = 0
        // Log.i("GameViewModel", "GameViewModel created!")
        resetList()
        nextWord()

        // create timer that will finish the game
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = millisUntilFinished / ONE_SECOND
            }

            override fun onFinish() {
                _currentTime.value = DONE
                onGameFinish()
            }
        }

        timer.start()
    }

    //View model is destroyed
    override fun onCleared() {
        super.onCleared()
        //Log.i("GameViewModel", "GameViewModel destroyed!")
        timer.cancel()
    }

    /** Methods for updating the UI **/
    fun onSkip() {
        _score.value = (_score.value)?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _score.value = (_score.value)?.plus(1)
        nextWord()
    }


    private fun nextWord() {
        if (wordList.isEmpty()) {
            resetList()

        } else {
            //remove from the list
            _word.value = wordList.removeAt(0)
        }
    }


    //complete game
    fun onGameFinishComplete() {
        _eventGameFinish.value = false
    }

    fun onGameFinish() {
        _eventGameFinish.value = true
    }

}
