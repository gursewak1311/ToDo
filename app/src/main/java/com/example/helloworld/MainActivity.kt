package com.example.helloworld

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

data class Task(val id: String = "", val title: String = "")

class MainActivity : AppCompatActivity() {

    private lateinit var taskInput: EditText
    private lateinit var addTaskButton: Button
    private lateinit var taskListView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ArrayAdapter<String>
    private val taskTitles = mutableListOf<String>()
    private val taskIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskInput = findViewById(R.id.taskInput)
        addTaskButton = findViewById(R.id.addTaskButton)
        taskListView = findViewById(R.id.taskListView)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("tasks")

        // Set up the ListView adapter
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskTitles)
        taskListView.adapter = adapter

        // Load existing tasks from Firebase
        loadTasks()

        // Add a new task to Firebase
        addTaskButton.setOnClickListener {
            val taskTitle = taskInput.text.toString()
            if (taskTitle.isNotEmpty()) {
                addTask(taskTitle)
                taskInput.text.clear()
            }
        }

        // Delete task on long click
        taskListView.setOnItemLongClickListener { _, _, position, _ ->
            deleteTask(taskIds[position])
            true
        }
    }

    private fun loadTasks() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskTitles.clear()
                taskIds.clear()
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    if (task != null) {
                        taskTitles.add(task.title)
                        taskIds.add(task.id)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load tasks: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addTask(title: String) {
        val taskId = database.push().key ?: return
        val task = Task(id = taskId, title = title)
        database.child(taskId).setValue(task).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteTask(taskId: String) {
        database.child(taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete task", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
