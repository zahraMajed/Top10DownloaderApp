package com.example.top10downloaderapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    lateinit var tvfeed : TextView
    lateinit var feedBtn: Button
    lateinit var itemsList:ArrayList<String>
    val feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate called")
        tvfeed = findViewById<TextView>(R.id.tv_feed)
        feedBtn = findViewById(R.id.btnFetch)
        feedBtn.setOnClickListener{
            requestApi(feedURL)
            initializeRV()
        }
        Log.d(TAG, "onCreate: done")
    }
    /////////////////////////////////
    fun initializeRV()
    {
        rv_main.layoutManager = LinearLayoutManager(this)
        rv_main.setHasFixedSize(true)
    }
    ///////////////////////////////
    private fun downloadXML(urlPath: String?): String {
        //The principal operations on a StringBuilder are the append and insert methods,
        //which are overloaded so as to accept data of any type.
        val xmlResult = StringBuilder()
        try {
            //this code to convert inputStream to String
            val url = URL(urlPath)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            //Gets the status code from an HTTP response message.
            val response = connection.responseCode
            Log.d(TAG, "downloadXML: The response code was $response")
            //to read input form
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val inputBuffer = CharArray(500)
            var charsRead = 0
            while (charsRead >= 0) {
                //The number of characters read,
                // or -1 if the end of the stream has been reached
                charsRead = reader.read(inputBuffer)
                if (charsRead > 0) {
                    xmlResult.append(String(inputBuffer, 0, charsRead))
                }
            }
            reader.close()

            Log.d(TAG, "Received ${xmlResult.length} bytes")
            return xmlResult.toString()

        } catch (e: MalformedURLException) {
            Log.e(TAG, "downloadXML: Invalid URL ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "downloadXML: IO Exception reading data: ${e.message}")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Log.e(TAG, "downloadXML: Security exception.  Needs permissions? ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error: ${e.message}")
        }
        return ""
    }

    private fun requestApi(url:String){

        var listItems = ArrayList<FeedEntry>()

        CoroutineScope(Dispatchers.IO).launch {
            val rssFeed = async {
                downloadXML(url)
            }.await()

            if (rssFeed.isEmpty()) {
                Log.e(TAG, "requestApi fun: Error downloading")
            } else {
                val parseApplications = async {
                    fParser()
                }.await()

                parseApplications.parse(rssFeed)
                listItems = parseApplications.getParsedList()
                withContext(Dispatchers.Main) {
                    rv_main.adapter = RecycelrAdapter(listItems)

                }
            }
        }

    }//

}//end class