package com.example.dehaatauthsdk

object ClientInfo {
    private lateinit var deHaatAuth: DeHaatAuth

    private var isDeHaatAuthInitialized = false

    fun setAuthSDK(deHaatAuth: DeHaatAuth?) {
        if (deHaatAuth == null)
            isDeHaatAuthInitialized = false
        else {
            this.deHaatAuth = deHaatAuth
            isDeHaatAuthInitialized = true
        }
    }

    fun getAuthSDK() = deHaatAuth

    fun getIsDeHaatAuthInitialized() = isDeHaatAuthInitialized
}