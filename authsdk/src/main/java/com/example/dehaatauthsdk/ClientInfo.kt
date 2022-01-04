package com.example.dehaatauthsdk

object ClientInfo {
    private var deHaatAuth: DeHaatAuth? = null

    fun setAuthSDK(deHaatAuth: DeHaatAuth?) {
        this.deHaatAuth = deHaatAuth
    }

    fun getAuthClientInfo() = deHaatAuth
}