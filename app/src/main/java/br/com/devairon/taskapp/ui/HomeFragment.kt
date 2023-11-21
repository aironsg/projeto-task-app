package br.com.devairon.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.devairon.taskapp.R
import br.com.devairon.taskapp.databinding.FragmentHomeBinding
import br.com.devairon.taskapp.ui.adapter.ViewPageAdapter
import br.com.devairon.taskapp.utils.extension.showBottomSheetLogout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        initListener()
        initTabs()

    }

    private fun initTabs() {
        val pageAdapter = ViewPageAdapter(requireActivity())
        binding.viewPager.adapter = pageAdapter
        pageAdapter.addFragment(TodoFragment(), R.string.status_task_todo)
        pageAdapter.addFragment(DoingFragment(), R.string.status_task_doing)
        pageAdapter.addFragment(DoneFragment(), R.string.status_task_done)
        binding.viewPager.offscreenPageLimit = pageAdapter.itemCount
        TabLayoutMediator(binding.tabsHome, binding.viewPager) { tab, position ->
            tab.text = getString(pageAdapter.getTitle(position))
        }.attach()
    }

    private fun initListener() {
        binding.btnLogout.setOnClickListener {
            showBottomSheetLogout(
                titleDialog = R.string.txt_title_dialog_logout,
                titleButtonOk = R.string.txt_button_dialog_confirmation,
                titleButtonCancel = R.string.txt_button_dialog_cancel_logout,
                message = R.string.txt_message_dialog_logout,
                onConfirmclick = { logout() },
                onCancelclick = {  }
            )
        }
    }

    private fun logout() {
        auth.signOut()
        findNavController().navigate(R.id.action_homeFragment_to_authentication)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}