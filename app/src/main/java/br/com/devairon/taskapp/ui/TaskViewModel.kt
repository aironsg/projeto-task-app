package br.com.devairon.taskapp.ui

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.devairon.taskapp.R
import br.com.devairon.taskapp.data.model.Status
import br.com.devairon.taskapp.data.model.Task
import br.com.devairon.taskapp.utils.FirebaseHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class TaskViewModel : ViewModel() {

    private val _taskList = MutableLiveData<List<Task>>()
    val taskList : LiveData<List<Task>> =  _taskList

    private val _taskInsert = MutableLiveData<Task>()
    val taskInsert : LiveData<Task> =  _taskInsert

    private  val _taskUpdate = MutableLiveData<Task>()
    val taskUpdate : LiveData<Task> = _taskUpdate


     fun getTasks(context: Context, status: Status) {
        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val taskList = mutableListOf<Task>()
                    for (ds in snapshot.children) {
                        val task = ds.getValue(Task::class.java) as Task
                        if(task.status == status){
                            taskList.add(task)
                        }
                    }
                    taskList.reverse()
                    _taskList.postValue(taskList)
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, R.string.txt_error_generic, Toast.LENGTH_SHORT)
                        .show()
                }
            })

    }

    fun updateTask(task: Task){

        val map = mapOf(
            "description" to task.description,
            "status" to task.status
        )

        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .updateChildren(map)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    _taskUpdate.postValue(task)
                }
            }
    }


     fun insertTask(task: Task) {
        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .setValue(task)
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    _taskInsert.postValue(task)


                }
            }
    }

}