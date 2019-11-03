package com.example.lifeorganizer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.AsyncTask
import android.widget.TextView
import java.io.InputStreamReader
import java.lang.Exception
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import java.util.concurrent.TimeUnit


class DisplayAmazonPriceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_amazon_price)

        val searchTerms = intent.getStringExtra("searchTerms")

        var amazonDetails = getAmazonDetails(searchTerms)

        val textView1 = findViewById<TextView>(R.id.amazonResultsProductName).apply {
            text = amazonDetails["productName"]
        }

        val downloadImageTask = DownloadImageTask(findViewById<ImageView>(R.id.amazonResultsProductImage)).execute(amazonDetails["imgUrl"]).get(4, TimeUnit.SECONDS)

        val textView2 = findViewById<TextView>(R.id.amazonResultsProductPrice).apply {
            text = "Price: $${amazonDetails["price"]}"
        }

    }

    private fun getAmazonDetails(searchTerms: String): Map<String, String> {

        val amazonSearchTerms = searchTerms.replace(" ", "+").toLowerCase()
        val amazonHtmlTask = GetAmazonHtmlTask()
        val amazonReturnText = amazonHtmlTask.execute(amazonSearchTerms).get().toString()

        val multiRegex: Regex = """widgetId=search-results index=0.*?<img.*?alt="(.*?)".*?2\.5x, (https.*?) 3x.*?<span class="a-price".*?\$(\d*\.\d{2})""".toRegex(setOf(RegexOption.DOT_MATCHES_ALL))
        var multiMatch = multiRegex.find(amazonReturnText)

        return mapOf("productName" to multiMatch!!.groupValues[1], "imgUrl" to multiMatch!!.groupValues[2], "price" to multiMatch!!.groupValues[3])

        /*
         *  TODO: Implement average price with all matches
         */

        //val newPriceString = StringBuilder(matchResult!!.groupValues[1] + matchResult!!.groupValues[2] + matchResult!!.groupValues[3])

        /*
        for (i in 0..4) {
            try {
                val newPriceString = StringBuilder(matchResult.elementAt(i).groupValues[1] + matchResult.elementAt(i).groupValues[2] + matchResult.elementAt(i).groupValues[3])
                val newPrice = newPriceString.toString()
                amazonDetailText.append("Returned price $i: \$$newPrice\n")
                sumOfPrices += newPrice.toDouble()
            }
            catch (e: Exception) {
                break
            }

        }
        */

        //val roundedAverage: Double = ((((sumOfPrices / 5) * 100).roundToInt()).toDouble() / 100)
        //amazonDetailText.append("Average price for $searchTerms: \$$roundedAverage")
    }

    private class GetAmazonHtmlTask : AsyncTask<String, Int, String>() {

        override fun doInBackground(vararg amazonSearchTerms: String?): String {
            val returnText: StringBuilder = StringBuilder("")

            retry(5) {
                val url = URL("https://www.amazon.com/s?k=${amazonSearchTerms[0]}")
                val urlConnection = url.openConnection() as HttpsURLConnection
                urlConnection.setRequestProperty("Accept-Encoding", "gzip")

                val reader: InputStreamReader

                if ("gzip" == urlConnection.contentEncoding) {
                    reader = InputStreamReader(GZIPInputStream(urlConnection.inputStream))
                }
                else {
                    reader = InputStreamReader(urlConnection.inputStream)
                }

                while (true) {
                    val ch: Int = reader.read()
                    if (ch == -1) {
                        break
                    }
                    returnText.append(ch.toChar())
                }
            }

            return returnText.toString()
        }

        @Throws(Throwable::class)
        fun <T> retry(numOfRetries: Int, block: () -> T): T {
            var throwable: Throwable? = null
            (1..numOfRetries).forEach { attempt ->
                try {
                    return block()
                } catch (e: Throwable) {
                    throwable = e
                    println("Failed attempt $attempt / $numOfRetries")
                }
            }
            throw throwable!!
        }

    }

    private inner class DownloadImageTask(internal var bmImage: ImageView) :
        AsyncTask<String, Void, Bitmap>() {

        override fun doInBackground(vararg urls: String): Bitmap? {
            val urldisplay = urls[0]
            var mIcon11: Bitmap? = null
            try {
                val `in` = java.net.URL(urldisplay).openStream()
                mIcon11 = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.e("Error", e.message)
                e.printStackTrace()
            }

            return mIcon11
        }

        override fun onPostExecute(result: Bitmap) {
            bmImage.setImageBitmap(result)
        }
    }

}
