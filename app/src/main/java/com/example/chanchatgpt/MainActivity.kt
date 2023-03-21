@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.chanchatgpt

import android.R
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chanchatgpt.ui.theme.ChanChatGPTTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    companion object {
        private const val OPENAI_API_KEY = "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"
        private const val OPENAI_API_URL =
            "https://api.openai.com/v1/chat/completions"
        //"https://api.openai.com/v1/engines/davinci-codex/completions"
    }

    private val loading = mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChanChatGPTTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val inputString = remember {
                        mutableStateOf<String>("")
                    }
                    Greeting(inputString.value) { inputText ->
                        talkChatGPT(inputText) {
                            inputString.value = it
                        }
                    }
                }
            }
        }
    }


    private fun talkChatGPT(inputText: String, onResult: (String) -> Unit) {
        loading.value = true
        GlobalScope.launch(Dispatchers.IO) {
            var response: String? = null
            try {
                val url = URL(MainActivity.OPENAI_API_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer " + MainActivity.OPENAI_API_KEY)
                conn.setRequestProperty("Content-Type", "application/json")
                /*{
                    "model": "text-davinci-003",
                    "prompt": "Say this is a test",
                    "max_tokens": 7,
                    "temperature": 0,
                    "top_p": 1,
                    "n": 1,
                    "stream": false,
                    "logprobs": null,
                    "stop": "\n"
                }*/

                val json = JSONObject()
                json.put("model", "gpt-3.5-turbo")
                val messages = JSONArray()
                val contentMessage = JSONObject()
                contentMessage.put("role", "user")
                contentMessage.put("content", inputText)
                messages.put(contentMessage)
                json.put("messages", messages)
                /*json.put("max_tokens", 7)
                json.put("temperature", 0)
                json.put("top_p", 1)
                json.put("n", 1)
                json.put("stream", false)
                json.put("logprobs", null)
                json.put("stop", "\n")*/
                val postData = json.toString()
                conn.doOutput = true
                val os = conn.outputStream
                os.write(postData.toByteArray())
                os.flush()
                os.close()
                val responseCode = conn.responseCode
                val inputStream = if (responseCode == 200) conn.inputStream else conn.errorStream
                val result: JSONObject = inputStream.bufferedReader().use {
                    JSONObject(it.readLine())
                }
                /*val `in` = BufferedReader(InputStreamReader(inputStream))
                var inputLine: String?
                val sb = StringBuffer()
                while (`in`.readLine().also { inputLine = it } != null) {
                    sb.append(inputLine)
                }
                `in`.close()
                response = sb.toString()*/
                var resultStr = ""
                result.optJSONArray("choices")?.let { choices ->
                    for (index in 0 until choices.length()) {
                        val message = choices.optJSONObject(index).optJSONObject("message")
                        resultStr += message.optString("content") + "\n"
                    }
                }
                Log.d("ChanLog", "talkChatGPT: $result, String: $resultStr")
                loading.value = false
                onResult(resultStr)
            } catch (e: IOException) {
                Log.e("OpenAIChatTask", e.message!!)
                loading.value = false
            } catch (e: JSONException) {
                Log.e("OpenAIChatTask", e.message!!)
                loading.value = false
            }
        }
    }


    @Composable
    fun Greeting(name: String, onClick: (String) -> Unit) {
        val scrollState = rememberScrollState(0)
        Column(modifier = Modifier.padding(all = 16.dp)) {
            val text = remember { mutableStateOf("") }
            TextField(
                value = text.value,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                onValueChange = {
                    text.value = it
                },
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu_send),
                        contentDescription = "Send",
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                onClick(text.value)
                            },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
            if (loading.value) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Text(
                text = name,
                modifier = Modifier.padding(bottom = 16.dp).verticalScroll(scrollState)
            )
        }

    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        ChanChatGPTTheme {
            Greeting("Android") {

            }
        }
    }

}
