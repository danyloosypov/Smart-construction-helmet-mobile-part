package com.example.obviouscontrol

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.obviouscontrol.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            //startActivity(Intent(this, MainActivity::class.java))
            performLogin()
        }
    }

    private fun performLogin() {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        val url = "http://192.168.0.151:8080/api/workers/login"
        val queue = Volley.newRequestQueue(applicationContext)

        val jsonObject = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,
            Response.Listener { response ->
                val correctedResponse = checkAndCorrectResponse(response.toString())
                Log.d("LoginActivity", "Response: $correctedResponse")

                // Process the response
                val correctedJsonResponse = JSONObject(correctedResponse)
                val status = correctedJsonResponse.optString("status")
                if (status == "success") {
                    val data = correctedJsonResponse.optJSONObject("data")
                    val token = data?.optString("token")
                    val workerId = data?.optString("worker_id")
                    if (token != null && workerId != null) {

                        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit()
                            .putString("token", token)
                            .putString("worker_id", workerId)
                            .apply()

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish() // Optional: Finish the LoginActivity to prevent going back on back press
                    } else {
                        // Token or worker_id is missing in the response, handle the error
                        showToast(getString(R.string.operationfailed))
                    }
                } else {
                    // Login failed, display an error message
                    val message = correctedJsonResponse.optString("message")
                    showToast(message)
                }
            },
            Response.ErrorListener { error ->
                Log.e("LoginActivity", "Error: ${error.message}", error)
                // Handle the error
                showToast(getString(R.string.erroroccured))
            })

        queue.add(request)
    }

    private fun checkAndCorrectResponse(response: String): String {
        // Check if the response ends with '}' character
        if (!response.trim().endsWith("}")) {
            // Append '}' character to the response
            return "$response}"
        }
        return response
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }





}