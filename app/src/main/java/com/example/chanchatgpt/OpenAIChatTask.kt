package com.example.chanchatgpt

import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by chandra-1765$ on 10/03/23$.
 */


class OpenAIChatTask : AsyncTask<String?, Void?, String?>() {
    override fun doInBackground(vararg inputText: String?): String? {
        var response: String? = null
        try {
            val url = URL(OPENAI_API_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY)
            conn.setRequestProperty("Content-Type", "application/json")
            //val requestBody: MutableMap<String?, String?> = java.util.HashMap()
            val json = JSONObject()
            json.put("prompt", inputText[0])
            json.put("max_tokens", "100")
            json.put("temperature", "0.7")
            json.put("n", "1")
            json.put("stop", "\n")
            val postData = json.toString()
            conn.doOutput = true
            val os = conn.outputStream
            os.write(postData.toByteArray())
            os.flush()
            os.close()
            val responseCode = conn.responseCode
            val `is` = if (responseCode == 200) conn.inputStream else conn.errorStream
            val `in` = BufferedReader(InputStreamReader(`is`))
            var inputLine: String?
            val sb = StringBuffer()
            while (`in`.readLine().also { inputLine = it } != null) {
                sb.append(inputLine)
            }
            `in`.close()
            response = sb.toString()
        } catch (e: IOException) {
            Log.e("OpenAIChatTask", e.message!!)
        } catch (e: JSONException) {
            Log.e("OpenAIChatTask", e.message!!)
        }
        return response
    }

    override fun onPostExecute(result: String?) {
        // Handle the result here
    }

    companion object {
        private const val OPENAI_API_KEY = "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"
        private const val OPENAI_API_URL =
            "https://api.openai.com/v1/engines/davinci-codex/completions"
    }
}
