package br.com.devairon.taskapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.devairon.taskapp.R
import br.com.devairon.taskapp.data.model.Status
import br.com.devairon.taskapp.data.model.Task
import br.com.devairon.taskapp.databinding.FragmentTodoBinding
import br.com.devairon.taskapp.ui.adapter.TaskAdapter
import br.com.devairon.taskapp.utils.extension.showBottomSheet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TodoFragment : Fragment() {
    private var _binding: FragmentTodoBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private  val viewModel : TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        reference = Firebase.database.reference
        initListener()
        initRecyclerView()
        getTasks()
    }

    private fun initListener() {
        binding.fabAdd.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToFormTaskFragment(null)
            findNavController().navigate(action)
        }

        observerViewModel()
    }

    private fun observerViewModel() {
        viewModel.taskUpdate.observe(viewLifecycleOwner){ updateTask ->
            if (updateTask.status == Status.TODO){

                val oldList = taskAdapter.currentList

                val newList = oldList.toMutableList().apply {
                    find { it.id == updateTask.id }?.description = updateTask.description
                }

                val position = newList.indexOfFirst { it.id == updateTask.id }

                taskAdapter.submitList(newList)
                taskAdapter.notifyItemChanged(position)
            }
        }
    }

    private fun initRecyclerView() {
        taskAdapter = TaskAdapter(requireContext()) { task, option ->
            optionSelected(task, option)
        }

        with(binding.rvTask) {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = taskAdapter
            Log.i("TESTE", "initRecyclerView: ${taskAdapter}")
        }
    }

    private fun optionSelected(task: Task, option: Int) {
        when (option) {
            TaskAdapter.SELECT_REMOVE -> {
                showBottomSheet(
                    titleDialog = R.string.txt_info_delete_tasks,
                    message = getString(R.string.txt_confirm_delete_tasks),
                    onclick =  {deleteTask(task)}
                )
            }

            TaskAdapter.SELECT_EDIT -> {
                val action = HomeFragmentDirections.actionHomeFragmentToFormTaskFragment(task)
                findNavController().navigate(action)
            }

            TaskAdapter.SELECT_DETAILS -> {
                Toast.makeText(
                    requireContext(),
                    "Detalhes: ${task.description}",
                    Toast.LENGTH_SHORT
                ).show()

            }

            TaskAdapter.SELECT_NEXT -> {
                Toast.makeText(requireContext(), "Proxima Sessão", Toast.LENGTH_SHORT).show()


            }
        }

    }

    private fun getTasks() {
        reference
            .child("tasks")
            .child(auth.currentUser?.uid ?: "")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val taskList = mutableListOf<Task>()
                    for (ds in snapshot.children) {
                        val task = ds.getValue(Task::class.java) as Task
                        if(task.status == Status.TODO){
                            taskList.add(task)
                        }
                    }
                    tasksListEmpty(taskList)
                    taskList.reverse()
                    taskAdapter.submitList(taskList)
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), R.string.txt_error_generic, Toast.LENGTH_SHORT)
                        .show()
                }
            })

    }

    private fun deleteTask(task: Task){
        reference
            .child("tasks")
            .child(auth.currentUser?.uid ?: "")
            .child(task.id)
            .removeValue().addOnCompleteListener {
                result ->
                if (result.isSuccessful){
                    Toast.makeText(requireContext(),R.string.txt_delete_tasks_success, Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(),R.string.txt_button_dialog_confirmation, Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun tasksListEmpty(tasks: List<Task>){
        binding.progressBar.isVisible = false
        binding.textInfo.text = if(tasks.isEmpty()){
            getString(R.string.txt_list_tasks_empty)
        }else{
            ""
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}