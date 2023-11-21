package br.com.devairon.taskapp.ui

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {

   fun hideKeyboard() {//função para esconder o teclado
       val view = activity?.currentFocus
       if (view != null) {
           val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
           imm.hideSoftInputFromWindow(view.windowToken, 0)
       }
   }
}