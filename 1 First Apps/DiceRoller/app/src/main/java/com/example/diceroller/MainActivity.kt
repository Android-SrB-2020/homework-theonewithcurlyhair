package com.example.diceroller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var rollButton: Button = findViewById(R.id.roll_button)
        rollButton.setOnClickListener { rollDice() }

        var countUpButton: Button = findViewById(R.id.count_up)
        countUpButton.setOnClickListener { countUp() }

        var resetButton: Button = findViewById(R.id.reset_button)
        resetButton.setOnClickListener { reset() }
    }

    private fun rollDice() {
        var randomInt = Random().nextInt(6) + 1

        var resultText: TextView = findViewById(R.id.result_text)
        resultText.text = randomInt.toString()
        //Toast.makeText(this, "button clicked", Toast.LENGTH_SHORT).show()
    }

    private fun countUp() {
        var value = result_text.text
        var resultText: TextView = findViewById(R.id.result_text)
        if (value == "Hello World!") {
            resultText.text = "1"
        } else if (value != "6") {
            resultText.text = (Integer.parseInt(resultText.text.toString()) + 1).toString()
        }
    }

    private fun reset() {
        var resultText: TextView = findViewById(R.id.result_text)
        resultText.text = "0"
    }
}
