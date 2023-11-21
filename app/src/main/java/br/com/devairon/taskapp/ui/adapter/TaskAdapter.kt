package br.com.devairon.taskapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.devairon.taskapp.R
import br.com.devairon.taskapp.data.model.Status
import br.com.devairon.taskapp.data.model.Task
import br.com.devairon.taskapp.databinding.ItemTaskBinding

class TaskAdapter(
    private val context: Context,
    private val taskSelected: (Task, Int) -> Unit
) :ListAdapter<Task,TaskAdapter.MyViewHolder>(DIFF_CALLBACK) {

    companion object {
        val SELECT_BACK = 1
        val SELECT_REMOVE = 2
        val SELECT_EDIT = 3
        val SELECT_DETAILS = 4
        val SELECT_NEXT = 5

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Task>(){

            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
              return oldItem.id == newItem.id && oldItem.description == newItem.description
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem && oldItem.description == newItem.description
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemTaskBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val task = getItem(position)
        holder.binding.txtDescription.text = task.description
        setIndicator(task, holder)

        with(holder.binding){
            btnRemove.setOnClickListener { taskSelected(task, SELECT_REMOVE) }
            btnEdit.setOnClickListener { taskSelected(task, SELECT_EDIT) }
            btnDetails.setOnClickListener { taskSelected(task, SELECT_DETAILS) }
        }

    }

    private fun setIndicator(task: Task, holder: MyViewHolder){
        when(task.status){
            Status.TODO -> {
                holder.binding.btnBack.isVisible = false
                holder.binding.btnNext.setOnClickListener {taskSelected(task, SELECT_NEXT)}
            }
            Status.DOING ->{
                holder.binding.btnBack.setColorFilter(ContextCompat.getColor(context, R.color.color_status_todo))
                holder.binding.btnNext.setColorFilter(ContextCompat.getColor(context, R.color.color_status_done))
                holder.binding.btnBack.setOnClickListener {taskSelected(task, SELECT_BACK)}
                holder.binding.btnNext.setOnClickListener {taskSelected(task, SELECT_NEXT)}

            }
            Status.DONE -> {
                holder.binding.btnNext.isVisible = false
                holder.binding.btnBack.setOnClickListener {taskSelected(task, SELECT_BACK)}
            }

        }
    }



    inner class MyViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

}