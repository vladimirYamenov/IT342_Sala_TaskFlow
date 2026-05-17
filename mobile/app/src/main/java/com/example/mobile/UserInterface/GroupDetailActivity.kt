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
import com.example.mobile.model.*
import kotlinx.coroutines.launch

class GroupDetailActivity : AppCompatActivity() {

    private var groupId: Long = 0
    private var groupName: String = ""
    private var currentGroup: Group? = null

    // Current logged-in user's email — used to check ownership
    private var currentUserEmail: String = ""

    private lateinit var llMembers: LinearLayout
    private lateinit var llTasks: LinearLayout
    private lateinit var tvTaskCount: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        groupId   = intent.getLongExtra("group_id", 0)
        groupName = intent.getStringExtra("group_name") ?: "Group"

        // Read saved email to determine if the current user is the owner
        val prefs = getSharedPreferences("taskflow_prefs", MODE_PRIVATE)
        currentUserEmail = prefs.getString("user_email", "") ?: ""

        supportActionBar?.title = groupName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        llMembers   = findViewById(R.id.llMembers)
        llTasks     = findViewById(R.id.llGroupTasks)
        tvTaskCount = findViewById(R.id.tvTaskCount)
        progressBar = findViewById(R.id.progressBar)

        findViewById<Button>(R.id.btnAddMember).setOnClickListener { showAddMemberDialog() }
        findViewById<Button>(R.id.btnAddGroupTask).setOnClickListener { showCreateTaskDialog() }

        loadGroupDetail()
        loadGroupTasks()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun loadGroupDetail() {
        lifecycleScope.launch {
            try {
                val resp = ApiClient.groupService.getGroup(groupId)
                if (resp.isSuccessful) {
                    currentGroup = resp.body()
                    renderMembers(currentGroup?.members ?: emptyList())
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupDetailActivity, "Failed to load group.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderMembers(members: List<GroupMember>) {
        llMembers.removeAllViews()

        // Check if the current logged-in user is the OWNER of this group
        val isOwner = members.any { m ->
            m.email.equals(currentUserEmail, ignoreCase = true) &&
                    m.role.equals("OWNER", ignoreCase = true)
        }

        // Also hide the "+ Add" button from non-owners
        findViewById<Button>(R.id.btnAddMember).visibility =
            if (isOwner) View.VISIBLE else View.GONE

        members.forEach { m ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_member, llMembers, false)

            val initials = (m.fullName ?: m.email ?: "U")
                .split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.take(2).joinToString("")
            row.findViewById<TextView>(R.id.tvMemberAvatar).text = initials.ifEmpty { "?" }
            row.findViewById<TextView>(R.id.tvMemberName).text = m.fullName ?: m.email ?: "Unknown"
            row.findViewById<TextView>(R.id.tvMemberRole).text = m.role ?: ""

            val btnRemove = row.findViewById<ImageButton>(R.id.btnRemoveMember)

            if (isOwner && !m.role.equals("OWNER", ignoreCase = true)) {
                // Owner can remove any member except themselves (the owner row)
                btnRemove.visibility = View.VISIBLE
                btnRemove.setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("Remove Member")
                        .setMessage("Remove ${m.fullName ?: m.email} from the group?")
                        .setPositiveButton("Remove") { _, _ ->
                            lifecycleScope.launch {
                                try {
                                    ApiClient.groupService.removeMember(groupId, m.userId)
                                    Toast.makeText(this@GroupDetailActivity, "Member removed.", Toast.LENGTH_SHORT).show()
                                    loadGroupDetail()
                                } catch (e: Exception) {
                                    Toast.makeText(this@GroupDetailActivity, "Failed to remove.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            } else {
                // Regular members see no remove button at all
                btnRemove.visibility = View.GONE
            }

            llMembers.addView(row)
        }
    }

    private fun loadGroupTasks() {
        lifecycleScope.launch {
            try {
                val resp = ApiClient.groupService.getGroupTasks(groupId)
                if (resp.isSuccessful) {
                    val tasks = resp.body() ?: emptyList()
                    tvTaskCount.text = "${tasks.size} task${if (tasks.size != 1) "s" else ""}"
                    renderGroupTasks(tasks)
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupDetailActivity, "Failed to load tasks.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderGroupTasks(tasks: List<Task>) {
        llTasks.removeAllViews()
        if (tasks.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No tasks yet. Add one!"
            tv.setTextColor(android.graphics.Color.GRAY)
            tv.setPadding(0, 8, 0, 8)
            llTasks.addView(tv)
            return
        }
        tasks.forEach { task ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_task, llTasks, false)

            row.findViewById<TextView>(R.id.tvTaskTitle).apply {
                text = task.title
                paintFlags = if (task.status == "COMPLETED")
                    paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                else paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            row.findViewById<TextView>(R.id.tvTaskStatus).text = task.status.replace("_", " ")
            row.findViewById<TextView>(R.id.tvTaskPriority).text = task.priority

            val tvDue = row.findViewById<TextView>(R.id.tvTaskDue)
            if (task.dueDate != null) {
                tvDue.text = "Due: ${task.dueDate.take(10)}"
                tvDue.visibility = View.VISIBLE
            } else tvDue.visibility = View.GONE

            val tvAssigned = row.findViewById<TextView>(R.id.tvAssigned)
            if (!task.assignedUsers.isNullOrEmpty()) {
                tvAssigned.text = "👤 " + task.assignedUsers.joinToString(", ") { it.fullName ?: it.email ?: "?" }
                tvAssigned.visibility = View.VISIBLE
            } else tvAssigned.visibility = View.GONE

            val btnComplete = row.findViewById<Button>(R.id.btnComplete)
            if (task.status == "COMPLETED") btnComplete.visibility = View.GONE
            else {
                btnComplete.visibility = View.VISIBLE
                btnComplete.setOnClickListener { markComplete(task) }
            }

            row.findViewById<ImageButton>(R.id.btnEdit).setOnClickListener { showEditTaskDialog(task) }
            row.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Delete \"${task.title}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                ApiClient.taskService.deleteTask(task.id)
                                Toast.makeText(this@GroupDetailActivity, "Task deleted.", Toast.LENGTH_SHORT).show()
                                loadGroupTasks()
                            } catch (e: Exception) {
                                Toast.makeText(this@GroupDetailActivity, "Failed.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null).show()
            }
            llTasks.addView(row)
        }
    }

    private fun markComplete(task: Task) {
        lifecycleScope.launch {
            try {
                ApiClient.taskService.updateTask(task.id, TaskRequest(
                    title = task.title, description = task.description,
                    priority = task.priority, status = "COMPLETED",
                    dueDate = task.dueDate?.take(10), groupId = groupId,
                    assignedUserIds = task.assignedUsers?.map { it.id } ?: emptyList()
                ))
                Toast.makeText(this@GroupDetailActivity, "Marked complete!", Toast.LENGTH_SHORT).show()
                loadGroupTasks()
            } catch (e: Exception) {
                Toast.makeText(this@GroupDetailActivity, "Failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddMemberDialog() {
        val et = EditText(this).apply { hint = "member@email.com"; inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS }
        AlertDialog.Builder(this)
            .setTitle("Add Member")
            .setView(et)
            .setPositiveButton("Add") { _, _ ->
                val email = et.text.toString().trim()
                if (email.isEmpty()) return@setPositiveButton
                lifecycleScope.launch {
                    try {
                        ApiClient.groupService.addMember(groupId, AddMemberRequest(email))
                        Toast.makeText(this@GroupDetailActivity, "Member added!", Toast.LENGTH_SHORT).show()
                        loadGroupDetail()
                    } catch (e: Exception) {
                        Toast.makeText(this@GroupDetailActivity, "Failed. Check the email.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCreateTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_task, null)
        val etTitle      = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDesc       = dialogView.findViewById<EditText>(R.id.etDescription)
        val etDueDate    = dialogView.findViewById<EditText>(R.id.etDueDate)
        val spinnerP     = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val spinnerS     = dialogView.findViewById<Spinner>(R.id.spinnerStatus)

        val priorities = arrayOf("HIGH", "MEDIUM", "LOW")
        val statuses   = arrayOf("TODO", "IN_PROGRESS", "PENDING", "COMPLETED")
        ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinnerP.adapter = it }
        ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinnerS.adapter = it }

        AlertDialog.Builder(this)
            .setTitle("Add Task to $groupName")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.length < 3) { Toast.makeText(this, "Title too short.", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                lifecycleScope.launch {
                    try {
                        ApiClient.taskService.createTask(TaskRequest(
                            title = title,
                            description = etDesc.text.toString().trim().ifEmpty { null },
                            priority = priorities[spinnerP.selectedItemPosition],
                            status = statuses[spinnerS.selectedItemPosition],
                            dueDate = etDueDate.text.toString().trim().ifEmpty { null },
                            groupId = groupId,
                            assignedUserIds = emptyList()
                        ))
                        Toast.makeText(this@GroupDetailActivity, "Task created!", Toast.LENGTH_SHORT).show()
                        loadGroupTasks()
                    } catch (e: Exception) {
                        Toast.makeText(this@GroupDetailActivity, "Failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_task, null)
        val etTitle   = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDesc    = dialogView.findViewById<EditText>(R.id.etDescription)
        val etDueDate = dialogView.findViewById<EditText>(R.id.etDueDate)
        val spinnerP  = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val spinnerS  = dialogView.findViewById<Spinner>(R.id.spinnerStatus)

        val priorities = arrayOf("HIGH", "MEDIUM", "LOW")
        val statuses   = arrayOf("TODO", "IN_PROGRESS", "PENDING", "COMPLETED")
        ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinnerP.adapter = it }
        ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinnerS.adapter = it }

        etTitle.setText(task.title)
        etDesc.setText(task.description ?: "")
        etDueDate.setText(task.dueDate?.take(10) ?: "")
        spinnerP.setSelection(priorities.indexOf(task.priority).coerceAtLeast(0))
        spinnerS.setSelection(statuses.indexOf(task.status).coerceAtLeast(0))

        AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                lifecycleScope.launch {
                    try {
                        ApiClient.taskService.updateTask(task.id, TaskRequest(
                            title = etTitle.text.toString().trim(),
                            description = etDesc.text.toString().trim().ifEmpty { null },
                            priority = priorities[spinnerP.selectedItemPosition],
                            status = statuses[spinnerS.selectedItemPosition],
                            dueDate = etDueDate.text.toString().trim().ifEmpty { null },
                            groupId = groupId,
                            assignedUserIds = task.assignedUsers?.map { it.id } ?: emptyList()
                        ))
                        Toast.makeText(this@GroupDetailActivity, "Task updated!", Toast.LENGTH_SHORT).show()
                        loadGroupTasks()
                    } catch (e: Exception) {
                        Toast.makeText(this@GroupDetailActivity, "Failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}