package com.rahoolll.tanyasmbok.views.main

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rahoolll.tanyasmbok.data.Message
import com.rahoolll.tanyasmbok.databinding.ActivityMainBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val messageList = mutableListOf<Message>()
    private lateinit var messageAdapter: MessageAdapter

    private val client = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageAdapter = MessageAdapter(messageList)
        binding.rvAiChat.adapter = messageAdapter
        binding.rvAiChat.layoutManager = LinearLayoutManager(this)

        binding.btnChatSend.setOnClickListener {
            val question = binding.etChatMessage.text.toString().trim()
            addToChat(question, Message.SENT_BY_ME)
            binding.etChatMessage.text.clear()
            binding.animationView.visibility = View.GONE
            callAPI(question)
        }

        hideSystemUI()
    }

    private fun addToChat(message: String, sentBy: String) {
        runOnUiThread {
            messageList.add(Message(message, sentBy))
            messageAdapter.notifyDataSetChanged()
            binding.rvAiChat.smoothScrollToPosition(messageAdapter.itemCount)
        }
    }

    private fun addResponse(response: String) {
        messageList.removeAt(messageList.size - 1)
        addToChat(response, Message.SENT_BY_BOT)
    }

    private fun callAPI(question: String) {
        messageList.add(Message("Typing... ", Message.SENT_BY_BOT))

        val jsonBody = JSONObject()
        try {
            jsonBody.put("message", question)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val JSON: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()

        val body = RequestBody.create(JSON, jsonBody.toString())
        val request = Request.Builder()
            .url("https://chatbot-api-rhl.vercel.app/api/chat")
            .header("Authorization", "Bearer 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")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val responseBodyString = response.body?.string()
                        val jsonObject = JSONObject(responseBodyString)
                        val metaObject = jsonObject.getJSONObject("meta")
                        val status = metaObject.getInt("status")

                        if (status == 200) {
                            val dataObject = jsonObject.getJSONObject("data")
                            val payloadObject = dataObject.getJSONObject("payload")
                            val result = payloadObject.getString("message")
                            addResponse(result.trim())
                        } else {
                            addResponse("Failed to load response. Status: $status")
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    val errorBody = response.body?.string() ?: "Empty error body"
                    addResponse("Failed to load response due to $errorBody")
                }
            }
        })
    }


    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}
