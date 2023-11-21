package br.com.devairon.taskapp.utils.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import br.com.devairon.taskapp.R
import br.com.devairon.taskapp.databinding.BottomSheetBinding
import br.com.devairon.taskapp.databinding.BottomSheetLogoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

fun Fragment.initToolbar(toolbar: Toolbar) {
    (activity as AppCompatActivity).setSupportActionBar(toolbar)
    (activity as AppCompatActivity).title = ""
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    toolbar.setNavigationOnClickListener {
        activity?.onBackPressed()
    }

}

fun Fragment.showBottomSheet(
    titleDialog: Int? = null,
    titleButton: Int? = null,
    message: String,
    onclick: () -> Unit = {}
) {

    val bottomSheetDialog = BottomSheetDialog(requireContext(),R.style.BottomSheetDialog  )
    val bottomSheetBinding : BottomSheetBinding = BottomSheetBinding.inflate(layoutInflater,null,false)

    bottomSheetBinding.txtTitle.text = getText(titleDialog ?: R.string.txt_title_warning)
    bottomSheetBinding.txtInformation.text = message
    bottomSheetBinding.btnOK.text = getText(titleButton ?: R.string.txt_button_confirmation_warning)
    bottomSheetBinding.btnOK.setOnClickListener {
        onclick()
        bottomSheetDialog.dismiss()
    }

    bottomSheetDialog.setContentView(bottomSheetBinding.root)
    bottomSheetDialog.show()

}

fun Fragment.showBottomSheetLogout(
    titleDialog: Int? = null,
    titleButtonOk: Int? = null,
    titleButtonCancel: Int? = null,
    message: Int? = null,
    onConfirmclick: () -> Unit = {},
    onCancelclick: () -> Unit = {}
) {

    val bottomSheetDialog = BottomSheetDialog(requireContext(),R.style.BottomSheetDialog)
    val bottomSheetLogoutBinding : BottomSheetLogoutBinding = BottomSheetLogoutBinding.inflate(layoutInflater,null,false)

    bottomSheetLogoutBinding.txtTitle.text = getText(titleDialog ?: R.string.txt_title_warning)
    bottomSheetLogoutBinding.txtInformation.text = getText(message ?: R.string.txt_message_dialog_logout)
    bottomSheetLogoutBinding.btnOK.text = getText(titleButtonOk ?: R.string.txt_button_confirmation_warning)
    bottomSheetLogoutBinding.btnCancel.text = getText(titleButtonCancel ?: R.string.txt_button_cancel_warning)
    bottomSheetLogoutBinding.btnOK.setOnClickListener {
        onConfirmclick()
        bottomSheetDialog.dismiss()
    }

    bottomSheetLogoutBinding.btnCancel.setOnClickListener {
        onCancelclick()
        bottomSheetDialog.dismiss()
    }

    bottomSheetDialog.setContentView(bottomSheetLogoutBinding.root)
    bottomSheetDialog.show()

}