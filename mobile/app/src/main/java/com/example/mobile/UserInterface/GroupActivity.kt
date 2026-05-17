package com.example.mobile.UserInterface

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobile.API.ApiClient
import com.example.mobile.R
import com.example.mobile.model.Group
import com.example.mobile.model.GroupRequest
import kotlinx.coroutines.launch

class GroupsActivity : AppCompatActivity() {

    private lateinit var llGroups: LinearLayout
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    // Current logged-in user's email — used to check ownership
    private var currentUserEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        llGroups    = findViewById(R.id.llGroups)
        tvEmpty     = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)

        // Read the saved email so we can compare against member roles
        val prefs = getSharedPreferences("taskflow_prefs", MODE_PRIVATE)
        currentUserEmail = prefs.getString("user_email", "") ?: ""

        supportActionBar?.title = "Groups"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<Button>(R.id.btnNewGroup).setOnClickListener { showCreateDialog() }

        loadGroups()
    }

    override fun onResume() { super.onResume(); loadGroups() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun loadGroups() {
        progressBar.visibility = View.VISIBLE
        llGroups.visibility = View.GONE
        tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val resp = ApiClient.groupService.getGroups()
                if (resp.isSuccessful) {
                    val groups = resp.body() ?: emptyList()
                    renderGroups(groups)
                } else {
                    Toast.makeText(this@GroupsActivity, "Failed to load groups.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupsActivity, "Network error.", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun renderGroups(groups: List<Group>) {
        llGroups.removeAllViews()
        if (groups.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            return
        }
        tvEmpty.visibility = View.GONE
        llGroups.visibility = View.VISIBLE

        groups.forEach { group ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_group, llGroups, false)

            // Avatar letter
            itemView.findViewById<TextView>(R.id.tvGroupAvatar).text =
                group.name.firstOrNull()?.uppercaseChar()?.toString() ?: "G"

            // Name & member count
            itemView.findViewById<TextView>(R.id.tvGroupName).text = group.name
            val memberCount = group.members?.size ?: 0
            itemView.findViewById<TextView>(R.id.tvMemberCount).text =
                "$memberCount member${if (memberCount != 1) "s" else ""}"

            // Determine if the current user is the OWNER of this group
            val isOwner = group.members?.any { member ->
                member.email.equals(currentUserEmail, ignoreCase = true) &&
                        member.role.equals("OWNER", ignoreCase = true)
            } ?: false

            val btnDelete = itemView.findViewById<ImageButton>(R.id.btnDeleteGroup)
            val btnLeave  = itemView.findViewById<Button>(R.id.btnLeaveGroup)

            if (isOwner) {
                // Owner: show delete, hide leave
                btnDelete.visibility = View.VISIBLE
                btnLeave.visibility  = View.GONE
                btnDelete.setOnClickListener { confirmDeleteGroup(group) }
            } else {
                // Member/joined: show leave, hide delete
                btnLeave.visibility  = View.VISIBLE
                btnDelete.visibility = View.GONE
                btnLeave.setOnClickListener { confirmLeaveGroup(group) }
            }

            // Tap the card to open group detail
            itemView.setOnClickListener {
                startActivity(Intent(this, GroupDetailActivity::class.java).apply {
                    putExtra("group_id", group.id)
                    putExtra("group_name", group.name)
                })
            }

            llGroups.addView(itemView)
        }
    }

    private fun showCreateDialog() {
        val et = EditText(this).apply { hint = "Group name (e.g. Project Alpha)" }
        AlertDialog.Builder(this)
            .setTitle("Create Group")
            .setView(et)
            .setPositiveButton("Create") { _, _ ->
                val name = et.text.toString().trim()
                if (name.length < 2) {
                    Toast.makeText(this, "Name must be at least 2 characters.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    try {
                        ApiClient.groupService.createGroup(GroupRequest(name))
                        Toast.makeText(this@GroupsActivity, "Group \"$name\" created!", Toast.LENGTH_SHORT).show()
                        loadGroups()
                    } catch (e: Exception) {
                        Toast.makeText(this@GroupsActivity, "Failed to create group.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteGroup(group: Group) {
        AlertDialog.Builder(this)
            .setTitle("Delete Group")
            .setMessage("Delete \"${group.name}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        ApiClient.groupService.deleteGroup(group.id)
                        Toast.makeText(this@GroupsActivity, "Group deleted.", Toast.LENGTH_SHORT).show()
                        loadGroups()
                    } catch (e: Exception) {
                        Toast.makeText(this@GroupsActivity, "Failed to delete group.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmLeaveGroup(group: Group) {
        AlertDialog.Builder(this)
            .setTitle("Leave Group")
            .setMessage("Leave \"${group.name}\"? You will need to be re-added to rejoin.")
            .setPositiveButton("Leave") { _, _ ->
                lifecycleScope.launch {
                    try {
                        ApiClient.groupService.leaveGroup(group.id)
                        Toast.makeText(this@GroupsActivity, "You left \"${group.name}\".", Toast.LENGTH_SHORT).show()
                        loadGroups()
                    } catch (e: Exception) {
                        Toast.makeText(this@GroupsActivity, "Failed to leave group.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
