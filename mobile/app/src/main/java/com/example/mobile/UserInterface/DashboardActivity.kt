package com.example.mobile.UserInterface

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobile.API.ApiClient
import com.example.mobile.R
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore token from prefs if app was killed
        val prefs = getSharedPreferences("taskflow_prefs", MODE_PRIVATE)
        val savedToken = prefs.getString("token", null)
        if (savedToken != null && ApiClient.getToken() == null) {
            ApiClient.setToken(savedToken)
        } else if (ApiClient.getToken() == null) {
            // Not logged in — go back to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_dashboard)

        val userName = prefs.getString("user_full_name", null)
            ?: prefs.getString("user_email", "User")

        findViewById<TextView>(R.id.tvWelcome).text = "Welcome back, $userName 👋"

        // Navigation buttons
        findViewById<Button>(R.id.btnGoTasks).setOnClickListener {
            startActivity(Intent(this, TasksActivity::class.java))
        }
        findViewById<Button>(R.id.btnGoGroups).setOnClickListener {
            startActivity(Intent(this, GroupsActivity::class.java))
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }

        loadStats()
    }

    override fun onResume() {
        super.onResume()
        loadStats() // Refresh when returning from Tasks/Groups
    }

    private fun loadStats() {
        lifecycleScope.launch {
            try {
                val tasksResp = ApiClient.taskService.getTasks()
                val tasks = if (tasksResp.isSuccessful) tasksResp.body() ?: emptyList() else emptyList()

                val total     = tasks.size
                val completed = tasks.count { it.status == "COMPLETED" }
                val inProgress = tasks.count { it.status == "IN_PROGRESS" }
                val pending   = tasks.count { it.status == "PENDING" || it.status == "TODO" }
                val overdue   = tasks.count { t ->
                    t.dueDate != null && t.status != "COMPLETED" &&
                            t.dueDate < java.time.LocalDate.now().toString()
                }

                findViewById<TextView>(R.id.tvStatTotal).text     = total.toString()
                findViewById<TextView>(R.id.tvStatDone).text      = completed.toString()
                findViewById<TextView>(R.id.tvStatProgress).text  = inProgress.toString()
                findViewById<TextView>(R.id.tvStatPending).text   = pending.toString()

                val tvOverdue = findViewById<TextView>(R.id.tvOverdue)
                if (overdue > 0) {
                    tvOverdue.text = "⚠ You have $overdue overdue task${if (overdue != 1) "s" else ""}!"
                    tvOverdue.visibility = android.view.View.VISIBLE
                } else {
                    tvOverdue.visibility = android.view.View.GONE
                }

                // Recent tasks (latest 5)
                val recent = tasks.sortedByDescending { it.createdAt }.take(5)
                val recentContainer = findViewById<android.widget.LinearLayout>(R.id.llRecentTasks)
                recentContainer.removeAllViews()
                if (recent.isEmpty()) {
                    val tv = TextView(this@DashboardActivity)
                    tv.text = "No tasks yet. Tap 'My Tasks' to create one!"
                    tv.setTextColor(android.graphics.Color.GRAY)
                    recentContainer.addView(tv)
                } else {
                    recent.forEach { task ->
                        val tv = TextView(this@DashboardActivity)
                        val statusIcon = when (task.status) {
                            "COMPLETED"   -> "✅"
                            "IN_PROGRESS" -> "⏳"
                            else          -> "📌"
                        }
                        val priorityTag = when (task.priority) {
                            "HIGH"   -> "[HIGH]"
                            "MEDIUM" -> "[MED]"
                            else     -> "[LOW]"
                        }
                        tv.text = "$statusIcon ${task.title}  $priorityTag"
                        tv.setPadding(0, 8, 0, 8)
                        tv.textSize = 14f
                        recentContainer.addView(tv)

                        // divider
                        val div = android.view.View(this@DashboardActivity)
                        div.layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1
                        )
                        div.setBackgroundColor(android.graphics.Color.parseColor("#EEEEEE"))
                        recentContainer.addView(div)
                    }
                }

                // Groups count
                val groupsResp = ApiClient.groupService.getGroups()
                val groupCount = if (groupsResp.isSuccessful) groupsResp.body()?.size ?: 0 else 0
                findViewById<TextView>(R.id.tvGroupCount).text = "$groupCount group${if (groupCount != 1) "s" else ""}"

            } catch (e: Exception) {
                // Silently fail — user will see zeros
            }
        }
    }

    private fun logout() {
        ApiClient.setToken(null)
        getSharedPreferences("taskflow_prefs", MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
