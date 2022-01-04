package com.example.dehaatauthsdk

import android.os.Handler
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.EndSessionRequest

abstract class AuthBaseActivity : AppCompatActivity() {

    protected lateinit var mAuthService: AuthorizationService

    protected var _initialConfiguration: Configuration? = null
    protected val initialConfiguration get() = _initialConfiguration!!

    protected var _mAuthServiceConfiguration: AuthorizationServiceConfiguration? = null
    protected val mAuthServiceConfiguration get() = _mAuthServiceConfiguration!!
    protected var _mAuthRequest: AuthorizationRequest? = null
    protected val mAuthRequest get() = _mAuthRequest!!
    protected var _mLogoutRequest: EndSessionRequest? = null
    protected val mLogoutRequest get() = _mLogoutRequest!!

    protected var _webView: WebView? = null
    protected val webView get() = _webView!!

    protected var isPageLoaded = false
    protected lateinit var timeoutHandler: Handler
    protected val TIMEOUT = 30L


    protected fun disposeCurrentServiceIfExist() {
        if (::mAuthService.isInitialized) {
            mAuthService.dispose()
        }
    }

    override fun onDestroy() {
        mAuthService.dispose()
        _webView = null
        _initialConfiguration = null
        _mLogoutRequest = null
        _mAuthRequest = null
        _mAuthServiceConfiguration = null
        ClientInfo.setAuthSDK(null)
        super.onDestroy()
    }
}