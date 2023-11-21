package br.com.devairon.taskapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import br.com.devairon.taskapp.R
import br.com.devairon.taskapp.databinding.FragmentLoginBinding
import br.com.devairon.taskapp.ui.BaseFragment
import br.com.devairon.taskapp.utils.FirebaseHelper
import br.com.devairon.taskapp.utils.extension.showBottomSheet


class LoginFragment : BaseFragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener() {
        binding.btnLogin.setOnClickListener {
            validateData()
        }

        binding.btnCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnRecoverAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recoverAccountFragment)
        }
    }


    private fun validateData() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (email.isNotBlank()) {
            if (password.isNotBlank()) {
                hideKeyboard()
                binding.progressBarLogin.isVisible = true
                loginUser(email, password)

            } else {
                showBottomSheet(message = getString(R.string.txt_password_empty))
            }
        } else {
            showBottomSheet(message = getString(R.string.txt_email_empty))

        }
    }

    private fun loginUser(email: String, password: String) {
        FirebaseHelper.getAuth().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_global_homeFragment)
                    Toast.makeText(requireContext(), "Login Success", Toast.LENGTH_SHORT).show()
                } else {
                    binding.progressBarLogin.isVisible = false
                    showBottomSheet(message = getString(FirebaseHelper.validateError(task.exception?.message.toString())))
                }
            }
    }

    private fun checkAuth() {
        findNavController().navigate(R.id.action_global_homeFragment)
        Toast.makeText(requireContext(), "Login Success", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}