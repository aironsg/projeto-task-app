package br.com.devairon.taskapp.ui

import android.os.Bundle
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
import br.com.devairon.taskapp.databinding.FragmentDoingBinding
import br.com.devairon.taskapp.ui.adapter.TaskAdapter
import br.com.devairon.taskapp.utils.FirebaseHelper
import br.com.devairon.taskapp.utils.StateView
import br.com.devairon.taskapp.utils.extension.showBottomSheet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class DoingFragment : Fragment() {

    private var _binding: FragmentDoingBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: TaskAdapter
    private  val viewModel : TaskViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observerViewModel()

        initRecyclerView()
        getTasks()
    }


    private fun observerViewModel() {
        viewModel.taskUpdate.observe(viewLifecycleOwner){ stateView ->

            when (stateView) {
                is StateView.onLoading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.onSuccess -> {
                    binding.progressBar.isVisible = false
                    if (stateView.data?.status == Status.DOING){

                        val oldList = taskAdapter.currentList

                        val newList = oldList.toMutableList().apply {
                            find { it.id == stateView.data.id }?.description = stateView.data.description
                        }

                        val position = newList.indexOfFirst { it.id == stateView.data.id }

                        taskAdapter.submitList(newList)
                        taskAdapter.notifyItemChanged(position)
                    }
                }

                is StateView.onError -> {
                    Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.isVisible = false


                }
            }


        }
    }

    private fun initRecyclerView() {
        taskAdapter = TaskAdapter(requireContext()) { task, option ->
            optionSelected(task, option)
        }
        with(binding.rvTask){
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = taskAdapter
        }


    }

    private fun optionSelected(task: Task, option: Int) {
        when (option) {
            TaskAdapter.SELECT_BACK -> {
                task.status = Status.TODO
                updateTask(task)
            }

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
                task.status = Status.DONE
                updateTask(task)


            }
        }

    }



    private fun getTasks() {
        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val taskList = mutableListOf<Task>()
                    for (ds in snapshot.children) {
                        val task = ds.getValue(Task::class.java) as Task
                        if(task.status == Status.DOING){
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

    private fun updateTask(task: Task){


        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .setValue(task).addOnCompleteListener {
                    result ->
                if (result.isSuccessful){
                    Toast.makeText(requireContext(), R.string.txt_update_task, Toast.LENGTH_SHORT)
                        .show()
                }else{
                    Toast.makeText(requireContext(),R.string.txt_button_dialog_confirmation, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun deleteTask(task: Task){
        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .removeValue().addOnCompleteListener {
                    result ->
                if (result.isSuccessful){
                    Toast.makeText(requireContext(),R.string.txt_delete_tasks_success, Toast.LENGTH_SHORT).show()
                    val oldList = taskAdapter.currentList
                    val newList = oldList.toMutableList().apply { remove(task) }
                    taskAdapter.submitList(newList)
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