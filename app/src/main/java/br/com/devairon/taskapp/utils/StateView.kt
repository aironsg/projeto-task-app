package br.com.devairon.taskapp.utils

sealed class StateView<T>(val data:T? = null, val message:String? = null){
    class onLoading<T>:StateView<T>()
    class onSuccess<T>(data: T, message: String? = null):StateView<T>(data, message)
    class onError<T>(message : String? = null):StateView<T>(null, message )
}
