package br.com.devairon.taskapp.utils

import br.com.devairon.taskapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseHelper {

    companion object{
        fun getDatabase() = Firebase.database.reference
        fun getAuth() = FirebaseAuth.getInstance()

        fun getIdUser() = getAuth().currentUser?.uid ?: ""

        fun isAuthenticated() = getAuth().currentUser  != null

        fun validateError(error: String) : Int{
            return when {
               error.contains("An internal error has occurred. [ INVALID_LOGIN_CREDENTIALS ]") -> {
                   R.string.txt_account_not_registered_register_fragment
               }
                error.contains("The email address is badly formatted") -> {
                   R.string.txt_invalid_email_register_fragment
               }
                error.contains("The password in invalid or the user does  not have a password") -> {
                   R.string.txt_invalid_password_register_fragment
               }
                error.contains("The email address is already in use by another account") -> {
                   R.string.txt_email_in_use_register_fragment
               }
                error.contains("Password should be at least 6 characters") -> {
                   R.string.txt_account_not_registered_register_fragment
               }
                else -> R.string.txt_error_generic
            }

        }
    }
}