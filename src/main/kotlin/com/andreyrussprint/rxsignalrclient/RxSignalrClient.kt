package com.andreyrussprint.rxsignalrclient

import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.Subscription
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy

open class RxSignalrClient(
    protected open val hubUrl: String
) {
    protected open val hubConnection: HubConnection by lazy {
        buildHubConnection()
    }

    protected open val accessTokenProvider = AccessTokenProvider()

    open fun setAuthToken(authToken: String?) {
        if (accessTokenProvider.authToken != authToken) {
            accessTokenProvider.authToken = authToken

            if (isConnected()) {
                stopConnection()
                        .subscribeBy(onError = { it.printStackTrace() })
            }
        }
    }

    open fun startConnection(): Completable = hubConnection.start()

    open fun stopConnection(): Completable = hubConnection.stop()

    open fun isConnected(): Boolean = hubConnection.connectionState == HubConnectionState.CONNECTED

    open fun <T> subscribeOn(hubMethod: String, param: Class<T>): Observable<T> {
        var subscription: Subscription? = null

        return Observable.create<T> { emitter ->
            subscription = hubConnection.on(hubMethod, emitter::onNext, param)
        }
                .doOnDispose { subscription?.unsubscribe() }
    }

    protected open fun buildHubConnection(): HubConnection = HubConnectionBuilder.create(hubUrl)
        .withAccessTokenProvider(accessTokenProvider)
        .build()
}
