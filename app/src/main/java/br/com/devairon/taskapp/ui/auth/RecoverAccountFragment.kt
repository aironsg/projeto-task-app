package br.com.devairon.taskapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import br.com.devairon.taskapp.R
import br.com.devairon.taskapp.databinding.FragmentRecoverAccountBinding
import br.com.devairon.taskapp.ui.BaseFragment
import br.com.devairon.taskapp.utils.FirebaseHelper
import br.com.devairon.taskapp.utils.extension.initToolbar
import br.com.devairon.taskapp.utils.extension.showBottomSheet

class RecoverAccountFragment : BaseFragment() {
    private var _binding: FragmentRecoverAccountBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverAccountBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar(binding.toolbar)
        initListener()
    }


    private fun initListener() {
        binding.btnRecover.setOnClickListener {
            validateData()

        }
    }


    private fun validateData() {
        val email = binding.editRecoverAccountEmail.text.toString().trim()
        if (email.isNotEmpty()) {
            hideKeyboard()
            binding.progressBarRecoverAccount.isVisible = true
            recoverAccountUser(email)

        } else {
            showBottomSheet(message = getString(R.string.txt_email_empty))
        }
    }

    private fun recoverAccountUser(email: String) {


      FirebaseHelper.getAuth().sendPasswordResetEmail(email).addOnCompleteListener { task ->
           binding.progressBarRecoverAccount.isVisible = false
           if(task.isSuccessful){
               showBottomSheet(message = getString(R.string.txt_message_recover_account))
           }else{
               showBottomSheet(message = getString(R.string.txt_message_email_not_found))
           }
       }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}