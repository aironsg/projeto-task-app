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
import androidx.recyclerview.widget.RecyclerView
import br.com.devairon.taskapp.R
import br.com.devairon.taskapp.data.model.Status
import br.com.devairon.taskapp.data.model.Task
import br.com.devairon.taskapp.databinding.FragmentTodoBinding
import br.com.devairon.taskapp.ui.adapter.TaskAdapter
import br.com.devairon.taskapp.utils.StateView
import br.com.devairon.taskapp.utils.extension.showBottomSheet

class TodoFragment : Fragment() {
    private var _binding: FragmentTodoBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: TaskAdapter
    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initRecyclerView()
        observerViewModel()
        viewModel.getTasks(requireContext(), Status.TODO)

    }

    private fun initListener() {
        binding.fabAdd.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToFormTaskFragment(null)
            findNavController().navigate(action)
        }

    }

    private fun observerViewModel() {
        viewModel.taskList.observe(viewLifecycleOwner) { stateView ->
           when(stateView){
               is StateView.onLoading ->{
                   binding.progressBar.isVisible = true
               }
               is StateView.onSuccess -> {
                   binding.progressBar.isVisible = false
                   tasksListEmpty(stateView.data ?: emptyList() )
                   taskAdapter.submitList(stateView.data)
               }
               is StateView.onError -> {
                   Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                   binding.progressBar.isVisible = false


               }
           }
        }
        viewModel.taskInsert.observe(viewLifecycleOwner) { task ->
            if (task.status == Status.TODO) {

                val oldList = taskAdapter.currentList

                val newList = oldList.toMutableList().apply {
                    add(0, task)
                }
                taskAdapter.submitList(newList)
                setPositionRecyclerView()

            }
        }

        viewModel.taskUpdate.observe(viewLifecycleOwner) { updateTask ->
            val oldList = taskAdapter.currentList

            val newList = oldList.toMutableList().apply {
                if (updateTask.status == Status.TODO) {
                    find { it.id == updateTask.id }?.description = updateTask.description
                } else {
                    remove(updateTask)
                }
            }

            val position = newList.indexOfFirst { it.id == updateTask.id }

            taskAdapter.submitList(newList)
            taskAdapter.notifyItemChanged(position)
        }

        viewModel.taskDelete.observe(viewLifecycleOwner) { task ->
            Toast.makeText(
                requireContext(),
                R.string.txt_delete_tasks_success,
                Toast.LENGTH_SHORT
            ).show()
            val oldList = taskAdapter.currentList
            val newList = oldList.toMutableList().apply { remove(task) }
            taskAdapter.submitList(newList)
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

    private fun setPositionRecyclerView() {
        taskAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {}

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {}

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {}

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.rvTask.scrollToPosition(0)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {}

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {}
        })
    }

    private fun optionSelected(task: Task, option: Int) {
        when (option) {
            TaskAdapter.SELECT_REMOVE -> {
                showBottomSheet(
                    titleDialog = R.string.txt_info_delete_tasks,
                    message = getString(R.string.txt_confirm_delete_tasks),
                    onclick = { viewModel.deleteTask(task) }
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
                task.status = Status.DOING
                viewModel.updateTask(task)


            }
        }

    }


    private fun tasksListEmpty(tasks: List<Task>) {
        binding.progressBar.isVisible = false
        binding.textInfo.text = if (tasks.isEmpty()) {
            getString(R.string.txt_list_tasks_empty)
        } else {
            ""
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}