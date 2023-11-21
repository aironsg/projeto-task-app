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
import androidx.navigation.fragment.navArgs
import br.com.devairon.taskapp.R
import br.com.devairon.taskapp.data.model.Status
import br.com.devairon.taskapp.data.model.Task
import br.com.devairon.taskapp.databinding.FragmentFormTaskBinding
import br.com.devairon.taskapp.utils.extension.initToolbar
import br.com.devairon.taskapp.utils.extension.showBottomSheet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FormTaskFragment : Fragment() {

    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var task: Task
    private var status: Status = Status.TODO
    private var newTask: Boolean = true
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private val args: FormTaskFragmentArgs by navArgs()
    private  val viewModel : TaskViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        auth = Firebase.auth
        reference = Firebase.database.reference
        getArgs()
        initListener()
    }

    private fun getArgs() {
        args.task.let {
            if (it != null) {
                this.task = it
                configTask()
            }
        }
    }

    private fun initListener() {
        binding.btnSave.setOnClickListener { validateData() }

        binding.rgStatus.setOnCheckedChangeListener { _, Id ->
            status = when (id) {
                R.id.rbTodo -> Status.TODO
                R.id.rbDoing -> Status.DOING
                else -> Status.DONE
            }
        }
    }

    private fun configTask() {
        newTask = false
        status = task.status
        binding.editDescription.setText(task.description)
        binding.txtToolbar.setText(R.string.txt_toolbar_update_task)
        setStatus()
    }

    private fun setStatus() {
        val id = when (task.status) {
            Status.TODO -> R.id.rbTodo
            Status.DOING -> R.id.rbDoing
            else -> R.id.rbDone
        }

        binding.rgStatus.check(id)
    }

    private fun validateData() {
        val description = binding.editDescription.text.toString()
        if (description.isNotBlank()) {
            binding.progressBarFormTask.isVisible = true
            if (newTask) {
                task = Task()
                task.id = reference.database.reference.push().key ?: ""
            }
            task.description = description
            task.status = status
            saveTask()
        } else {
            showBottomSheet(message = getString(R.string.txt_description_empty))
        }
    }

    private fun saveTask() {
        reference
            .child("tasks")
            .child(auth.currentUser?.uid ?: "")
            .child(task.id)
            .setValue(task)
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    Toast.makeText(requireContext(), R.string.txt_save_task, Toast.LENGTH_SHORT)
                        .show()
                    if (newTask) {
                        findNavController().popBackStack()
                    } else {
                        binding.progressBarFormTask.isVisible = false
                        viewModel.setUpdateTask(task)
                    }
                } else {
                    binding.progressBarFormTask.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        R.string.txt_error_save_task,
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}