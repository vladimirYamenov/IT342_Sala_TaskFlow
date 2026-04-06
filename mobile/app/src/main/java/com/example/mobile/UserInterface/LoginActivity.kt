package com.example.mobile.UserInterface

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobile.API.ApiClient
import com.example.mobile.MainActivity
import com.example.mobile.R
import com.example.mobile.model.LoginRequest
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvError = findViewById<TextView>(R.id.tvError)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                tvError.text = "Email and password are required."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = View.GONE
            btnLogin.isEnabled = false
            btnLogin.text = "Logging in..."

            lifecycleScope.launch {
                try {
                    val response = ApiClient.authService.login(
                        LoginRequest(email, password)
                    )
                    if (response.isSuccessful && response.body() != null) {
                        getSharedPreferences("taskflow_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("user_email", response.body()?.email)
                            .apply()

                        Toast.makeText(
                            this@LoginActivity,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        tvError.text = "Invalid email or password."
                        tvError.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    tvError.text = "Network error. Check your connection."
                    tvError.visibility = View.VISIBLE
                } finally {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Log In"
                }
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}