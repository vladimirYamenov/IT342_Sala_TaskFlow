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
import com.example.mobile.R
import com.example.mobile.model.RegisterRequest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvError = findViewById<TextView>(R.id.tvError)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Input validation
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                tvError.text = "All fields are required."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tvError.text = "Please enter a valid email."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (password.length < 6) {
                tvError.text = "Password must be at least 6 characters."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = View.GONE
            btnRegister.isEnabled = false
            btnRegister.text = "Creating account..."

            lifecycleScope.launch {
                try {
                    val response = ApiClient.authService.register(
                        RegisterRequest(name, email, password, password)
                    )
                    if (response.isSuccessful || response.code() == 200) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Account created! Please log in.",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else if (response.code() == 409) {
                        tvError.text = "Email already registered."
                        tvError.visibility = View.VISIBLE
                    } else {
                        tvError.text = "Registration failed. Try again."
                        tvError.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    tvError.text = "Network error. Check your connection."
                    tvError.visibility = View.VISIBLE
                } finally {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Create Account"
                }
            }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}