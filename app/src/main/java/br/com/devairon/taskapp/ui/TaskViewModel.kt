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
import br.com.devairon.taskapp.utils.StateView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class TaskViewModel : ViewModel() {

    private val _taskList = MutableLiveData<StateView<List<Task>>>()
    val taskList: LiveData<StateView<List<Task>>> = _taskList

    private val _taskInsert = MutableLiveData<Task>()
    val taskInsert: LiveData<Task> = _taskInsert

    private val _taskUpdate = MutableLiveData<Task>()
    val taskUpdate: LiveData<Task> = _taskUpdate

    private val _taskDelete = MutableLiveData<Task>()
    val taskDelete: LiveData<Task> = _taskDelete


    fun getTasks(context: Context, status: Status) {
        try {
            _taskList.postValue(StateView.onLoading())
            FirebaseHelper.getDatabase()
                .child("tasks")
                .child(FirebaseHelper.getIdUser())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val taskList = mutableListOf<Task>()
                        for (ds in snapshot.children) {
                            val task = ds.getValue(Task::class.java) as Task
                            if (task.status == status) {
                                taskList.add(task)
                            }
                        }
                        taskList.reverse()
                        _taskList.postValue(StateView.onSuccess(data = taskList))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, R.string.txt_error_generic, Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        } catch (ex: Exception) {

            _taskList.postValue(StateView.onError(ex.message.toString()))
        }

    }

    fun updateTask(task: Task) {

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
                if (it.isSuccessful) {
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

    fun deleteTask(task: Task) {
        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .removeValue().addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    _taskDelete.postValue(task)

                }
            }
    }

}