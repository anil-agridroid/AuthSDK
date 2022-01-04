package com.example.dehaatauthsdk

import android.content.Context
import android.net.Uri
import com.example.dehaatauthsdk.ClientInfo.getAuthClientInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.openid.appauth.*

class RenewTokenHandler constructor(private val context: Context, _clientId:String, isDebugMode:Boolean) {

    private lateinit var mAuthService: AuthorizationService

    private var _initialConfiguration: Configuration? = null
    private val initialConfiguration get() = _initialConfiguration!!

    private var _mAuthServiceConfiguration: AuthorizationServiceConfiguration? = null
    private val mAuthServiceConfiguration get() = _mAuthServiceConfiguration!!

    private lateinit var job: Job

    init {
        _initialConfiguration = Configuration.getInstance(context,_clientId,isDebugMode)
    }

    fun startRenewProcess(){
        job = CoroutineScope((Dispatchers.IO)).launch {
            startAuthorizationServiceCreation()
        }
    }


    private fun startAuthorizationServiceCreation() {
        disposeCurrentServiceIfExist()
        mAuthService = createNewAuthorizationService()
        initialConfiguration.discoveryUri?.let {
            fetchEndpointsFromDiscoveryUrl(it)
        } ?: handleAuthFailureState(KotlinNullPointerException("Discovery Url is null"))
    }

    private fun disposeCurrentServiceIfExist() {
        if (::mAuthService.isInitialized) {
            mAuthService.dispose()
        }
    }

    private fun createNewAuthorizationService() =
        AuthorizationService(
            context,
            AppAuthConfiguration.Builder()
                .setConnectionBuilder(initialConfiguration.connectionBuilder)
                .build()
        )


    private fun fetchEndpointsFromDiscoveryUrl(discoveryUrl: Uri) {
        AuthorizationServiceConfiguration.fetchFromUrl(
            discoveryUrl,
            handleConfigurationRetrievalResult,
            initialConfiguration.connectionBuilder
        )
    }

    private fun handleAuthFailureState(exception: Exception?) =
        getAuthClientInfo()?.let {
            when (it.getOperationState()) {
                DeHaatAuth.OperationState.EMAIL_LOGIN, DeHaatAuth.OperationState.MOBILE_LOGIN, DeHaatAuth.OperationState.RENEW_TOKEN -> {
                    it.getLoginCallback()
                        .onFailure(exception)
                }
                DeHaatAuth.OperationState.LOGOUT ->
                    it.getLogoutCallback().onLogoutFailure(exception)
            }
            ClientInfo.setAuthSDK(null)
            job.cancel(null)
        }

    private var handleConfigurationRetrievalResult =
        AuthorizationServiceConfiguration.RetrieveConfigurationCallback { config, exception ->
            if (config == null) {
                handleAuthFailureState(exception)
            } else {
                _mAuthServiceConfiguration = config
                startRenewAuthToken()
            }
        }

    private fun startRenewAuthToken() =
        getAuthClientInfo()?.let {
            initialConfiguration.clientId?.let { clientId ->
                val tokenRequest = TokenRequest.Builder(mAuthServiceConfiguration, clientId)
                    .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                    .setScope(null)
                    .setRefreshToken(it.getRefreshToken())
                    .setAdditionalParameters(null)
                    .build()

                performTokenRequest(tokenRequest, handleTokenResponseCallback)
            } ?: handleAuthFailureState(KotlinNullPointerException("Client id is null"))
        }

    private fun performTokenRequest(
        request: TokenRequest,
        callback: AuthorizationService.TokenResponseCallback
    ) {
        val clientAuthentication =
            ClientSecretBasic(initialConfiguration.tokenEndpointUri.toString())

        mAuthService.performTokenRequest(
            request,
            clientAuthentication,
            callback
        )
    }

    private var handleTokenResponseCallback =
        AuthorizationService.TokenResponseCallback { response, exception ->
            response?.let {
                with(it) {
                    if (accessToken != null && refreshToken != null && idToken != null) {
                        val tokenInfo = TokenInfo(
                            it.accessToken!!,
                            it.refreshToken!!,
                            it.idToken!!
                        )
                        handleTokenSuccess(tokenInfo)
                    } else {
                        handleAuthFailureState(KotlinNullPointerException("access token is null"))
                    }
                }
            } ?: kotlin.run {
                handleAuthFailureState(exception)
            }
        }

    private fun handleTokenSuccess(tokenInfo: TokenInfo) =
        getAuthClientInfo()?.let {
            it.getLoginCallback().onSuccess(tokenInfo)
            ClientInfo.setAuthSDK(null)
            job.cancel(null)
        }

}