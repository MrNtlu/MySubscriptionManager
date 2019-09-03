package com.mrntlu.mysubscriptionmanager.interfaces

interface CoroutinesHandler {
    fun onSuccess(message:String)
    fun onError(exception:String)
}