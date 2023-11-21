package br.com.devairon.taskapp.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import br.com.devairon.taskapp.R
import br.com.devairon.taskapp.databinding.FragmentLoginBinding
import br.com.devairon.taskapp.databinding.FragmentRegisterBinding
import br.com.devairon.taskapp.utils.extension.initToolbar
import br.com.devairon.taskapp.utils.extension.showBottomSheet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        initListener()
        auth = Firebase.auth
    }

    private fun initListener() {
        binding.btnCreateAccount.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        val email = binding.editRegisterEmail.text.toString().trim()
        val password = binding.editRegisterPassword.text.toString().trim()

        if (email.isNotBlank()) {
            if (password.isNotBlank()) {
                userRegister(email, password)
                binding.progressBarRegister.isVisible = true
            } else {

                showBottomSheet(message = getString(R.string.txt_password_required))
            }
        } else {
            showBottomSheet(message = getString(R.string.txt_email_required))

        }
    }

    private fun userRegister(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_global_homeFragment)
                } else {
                    Toast.makeText(
                        requireContext(), task.exception?.message, Toast.LENGTH_SHORT,
                    ).show()
                    binding.progressBarRegister.isVisible = false
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}