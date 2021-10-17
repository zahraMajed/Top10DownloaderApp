package com.example.top10downloaderapp

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class fParser {
    private val TAG = "FeedParser"
    private val applications = ArrayList<FeedEntry>()

    fun parse(xmlData: String): Boolean {
        Log.d(TAG, "parse called with $xmlData")
        var status = true
        var inEntry = false
        var textValue = ""

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(xmlData.reader())
            var eventType = parser.eventType
            var currentRecord = FeedEntry()
            while (eventType != XmlPullParser.END_DOCUMENT) {

                val tagName = parser.name?.toLowerCase()
                Log.d(TAG, "parse: tag for " + tagName)
                when (eventType) {

                    XmlPullParser.START_TAG -> {
                        Log.d(TAG, "parse: Starting tag for " + tagName)
                        if (tagName == "entry") {
                            inEntry = true
                        }
                    }

                    XmlPullParser.TEXT -> textValue = parser.text

                    XmlPullParser.END_TAG -> {
                        Log.d(TAG, "parse: Ending tag for " + tagName)
                        if (inEntry) {
                            when (tagName) {
                                "entry" -> {
                                    applications.add(currentRecord)
                                    inEntry = false
                                    currentRecord = FeedEntry()
                                }

                                "name" -> currentRecord.name = textValue

                            }
                        }//if


                    }//
                }//when
                eventType = parser.next()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        }

        return status
    }

    fun getParsedList(): ArrayList<FeedEntry> {

        return applications
    }
}