package com.example.mobile.UserInterface

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobile.API.ApiClient
import com.example.mobile.R
import com.example.mobile.model.Task
import com.example.mobile.model.TaskRequest
import kotlinx.coroutines.launch

class TasksActivity : AppCompatActivity() {

    private lateinit var llTasks: LinearLayout
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private var tasks: List<Task> = emptyList()
    private var filterStatus = "ALL"
    private var filterPriority = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        llTasks     = findViewById(R.id.llTasks)
        tvEmpty     = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)

        supportActionBar?.title = "My Tasks"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<Button>(R.id.btnNewTask).setOnClickListener { showTaskDialog(null) }

        // Filter spinners
        val statusSpinner = findViewById<Spinner>(R.id.spinnerStatus)
        val prioritySpinner = findViewById<Spinner>(R.id.spinnerPriority)

        ArrayAdapter.createFromResource(this, R.array.task_statuses, android.R.layout.simple_spinner_item)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); statusSpinner.adapter = it }

        ArrayAdapter.createFromResource(this, R.array.task_priorities, android.R.layout.simple_spinner_item)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); prioritySpinner.adapter = it }

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                filterStatus = arrayOf("ALL", "TODO", "IN_PROGRESS", "COMPLETED", "PENDING")[pos]
                loadTasks()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                filterPriority = arrayOf("ALL", "HIGH", "MEDIUM", "LOW")[pos]
                loadTasks()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        loadTasks()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun loadTasks() {
        progressBar.visibility = View.VISIBLE
        llTasks.visibility = View.GONE
        tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val resp = ApiClient.taskService.getTasks(
                    status   = if (filterStatus == "ALL") null else filterStatus,
                    priority = if (filterPriority == "ALL") null else filterPriority
                )
                if (resp.isSuccessful) {
                    tasks = resp.body() ?: emptyList()
                    renderTasks()
                } else {
                    showError("Failed to load tasks.")
                }
            } catch (e: Exception) {
                showError("Network error.")
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun renderTasks() {
        llTasks.removeAllViews()
        if (tasks.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            llTasks.visibility = View.GONE
            return
        }
        tvEmpty.visibility = View.GONE
        llTasks.visibility = View.VISIBLE

        tasks.forEach { task ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_task, llTasks, false)

            itemView.findViewById<TextView>(R.id.tvTaskTitle).apply {
                text = task.title
                paintFlags = if (task.status == "COMPLETED")
                    paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                else
                    paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            itemView.findViewById<TextView>(R.id.tvTaskStatus).text = task.status.replace("_", " ")
            itemView.findViewById<TextView>(R.id.tvTaskPriority).text = task.priority

            val tvDue = itemView.findViewById<TextView>(R.id.tvTaskDue)
            if (task.dueDate != null) {
                val overdue = task.status != "COMPLETED" &&
                        task.dueDate < java.time.LocalDate.now().toString()
                tvDue.text = if (overdue) "⚠ Overdue: ${task.dueDate.take(10)}" else "Due: ${task.dueDate.take(10)}"
                tvDue.setTextColor(if (overdue) android.graphics.Color.RED else android.graphics.Color.GRAY)
                tvDue.visibility = View.VISIBLE
            } else {
                tvDue.visibility = View.GONE
            }

            val tvAssigned = itemView.findViewById<TextView>(R.id.tvAssigned)
            if (!task.assignedUsers.isNullOrEmpty()) {
                tvAssigned.text = "👤 " + task.assignedUsers.joinToString(", ") { it.fullName ?: it.email ?: "?" }
                tvAssigned.visibility = View.VISIBLE
            } else {
                tvAssigned.visibility = View.GONE
            }

            // Quick complete button
            val btnComplete = itemView.findViewById<Button>(R.id.btnComplete)
            if (task.status == "COMPLETED") {
                btnComplete.visibility = View.GONE
            } else {
                btnComplete.visibility = View.VISIBLE
                btnComplete.setOnClickListener { quickUpdateStatus(task, "COMPLETED") }
            }

            itemView.findViewById<ImageButton>(R.id.btnEdit).setOnClickListener { showTaskDialog(task) }
            itemView.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener { confirmDelete(task) }

            llTasks.addView(itemView)
        }
    }

    private fun quickUpdateStatus(task: Task, newStatus: String) {
        lifecycleScope.launch {
            try {
                ApiClient.taskService.updateTask(
                    task.id,
                    TaskRequest(
                        title = task.title,
                        description = task.description,
                        priority = task.priority,
                        status = newStatus,
                        dueDate = task.dueDate?.take(10),
                        groupId = task.groupId,
                        assignedUserIds = task.assignedUsers?.map { it.id } ?: emptyList()
                    )
                )
                Toast.makeText(this@TasksActivity, "Task updated!", Toast.LENGTH_SHORT).show()
                loadTasks()
            } catch (e: Exception) {
                Toast.makeText(this@TasksActivity, "Failed to update.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Delete \"${task.title}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        ApiClient.taskService.deleteTask(task.id)
                        Toast.makeText(this@TasksActivity, "Task deleted.", Toast.LENGTH_SHORT).show()
                        loadTasks()
                    } catch (e: Exception) {
                        Toast.makeText(this@TasksActivity, "Failed to delete.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTaskDialog(existingTask: Task?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_task, null)
        val etTitle       = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDesc        = dialogView.findViewById<EditText>(R.id.etDescription)
        val etDueDate     = dialogView.findViewById<EditText>(R.id.etDueDate)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val spinnerStatus   = dialogView.findViewById<Spinner>(R.id.spinnerStatus)

        val priorities = arrayOf("HIGH", "MEDIUM", "LOW")
        val statuses   = arrayOf("TODO", "IN_PROGRESS", "PENDING", "COMPLETED")

        ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPriority.adapter = it
        }
        ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerStatus.adapter = it
        }

        if (existingTask != null) {
            etTitle.setText(existingTask.title)
            etDesc.setText(existingTask.description ?: "")
            etDueDate.setText(existingTask.dueDate?.take(10) ?: "")
            spinnerPriority.setSelection(priorities.indexOf(existingTask.priority).coerceAtLeast(0))
            spinnerStatus.setSelection(statuses.indexOf(existingTask.status).coerceAtLeast(0))
        }

        AlertDialog.Builder(this)
            .setTitle(if (existingTask == null) "Create Task" else "Edit Task")
            .setView(dialogView)
            .setPositiveButton(if (existingTask == null) "Create" else "Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.length < 3) {
                    Toast.makeText(this, "Title must be at least 3 characters.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val request = TaskRequest(
                    title       = title,
                    description = etDesc.text.toString().trim().ifEmpty { null },
                    priority    = priorities[spinnerPriority.selectedItemPosition],
                    status      = statuses[spinnerStatus.selectedItemPosition],
                    dueDate     = etDueDate.text.toString().trim().ifEmpty { null },
                    groupId     = existingTask?.groupId,
                    assignedUserIds = existingTask?.assignedUsers?.map { it.id } ?: emptyList()
                )
                lifecycleScope.launch {
                    try {
                        if (existingTask == null) {
                            ApiClient.taskService.createTask(request)
                            Toast.makeText(this@TasksActivity, "Task created!", Toast.LENGTH_SHORT).show()
                        } else {
                            ApiClient.taskService.updateTask(existingTask.id, request)
                            Toast.makeText(this@TasksActivity, "Task updated!", Toast.LENGTH_SHORT).show()
                        }
                        loadTasks()
                    } catch (e: Exception) {
                        Toast.makeText(this@TasksActivity, "Failed to save task.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
