package com.mrntlu.mysubscriptionmanager.interfaces

interface CoroutinesHandler {
    fun onSuccess()
    fun onError(exception:String)
}