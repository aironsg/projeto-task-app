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
import androidx.recyclerview.widget.RecyclerView
import br.com.devairon.taskapp.R
import br.com.devairon.taskapp.data.model.Status
import br.com.devairon.taskapp.data.model.Task
import br.com.devairon.taskapp.databinding.FragmentDoingBinding
import br.com.devairon.taskapp.ui.adapter.TaskAdapter
import br.com.devairon.taskapp.utils.StateView
import br.com.devairon.taskapp.utils.extension.showBottomSheet


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

        initRecyclerView()
        observerViewModel()
        viewModel.getTasks(requireContext())
    }


    private fun observerViewModel() {
        viewModel.taskList.observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.onLoading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.onSuccess -> {
                    binding.progressBar.isVisible = false
                    val taskList = stateView.data?.filter { it.status == Status.DOING }
                    tasksListEmpty(taskList ?: emptyList())
                    taskAdapter.submitList(taskList)
                }

                is StateView.onError -> {
                    Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.isVisible = false


                }
            }
        }
        viewModel.taskInsert.observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.onLoading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.onSuccess -> {
                    binding.progressBar.isVisible = false
                    if (stateView.data?.status == Status.DOING) {

                        val oldList = taskAdapter.currentList

                        val newList = oldList.toMutableList().apply {
                            add(0, stateView.data)
                        }
                        taskAdapter.submitList(newList)
                        setPositionRecyclerView()

                    }
                }

                is StateView.onError -> {
                    Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.isVisible = false
                }
            }

        }

        viewModel.taskUpdate.observe(viewLifecycleOwner) { stateView ->

            when (stateView) {
                is StateView.onLoading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.onSuccess -> {
                    binding.progressBar.isVisible = false
                    val oldList = taskAdapter.currentList

                    val newList = oldList.toMutableList().apply {
                        if (!oldList.contains(stateView.data) && stateView.data?.status == Status.DOING){
                            add(0,stateView.data)
                            setPositionRecyclerView()
                        }

                        if (stateView.data?.status == Status.DOING) {
                            find { it.id == stateView.data.id }?.description =
                                stateView.data.description
                        } else {
                            remove(stateView.data)
                        }
                    }

                    val position = newList.indexOfFirst { it.id == stateView.data?.id }

                    taskAdapter.submitList(newList)
                    taskAdapter.notifyItemChanged(position)
                    tasksListEmpty(newList)
                }

                is StateView.onError -> {
                    Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.isVisible = false
                }
            }
        }

        viewModel.taskDelete.observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.onLoading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.onSuccess -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        R.string.txt_delete_tasks_success,
                        Toast.LENGTH_SHORT
                    ).show()
                    val oldList = taskAdapter.currentList
                    val newList = oldList.toMutableList().apply { remove(stateView.data) }
                    taskAdapter.submitList(newList)
                    tasksListEmpty(newList)
                }

                is StateView.onError -> {
                    Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.isVisible = false


                }
            }


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
                viewModel.updateTask(task)
            }

            TaskAdapter.SELECT_REMOVE -> {
                showBottomSheet(
                    titleDialog = R.string.txt_info_delete_tasks,
                    message = getString(R.string.txt_confirm_delete_tasks),
                    onclick =  {viewModel.deleteTask(task)}
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
                viewModel.updateTask(task)


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