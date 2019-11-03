package com.example.lifeorganizer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun searchAverageAmazonPrice(view: View) {
        val searchTerms = findViewById<EditText>(R.id.amazonSearchInputText).text
        val intent = Intent(this, DisplayAmazonPriceActivity::class.java).apply {
            putExtra("searchTerms", searchTerms.toString())
        }
        startActivity(intent)
    }

}
