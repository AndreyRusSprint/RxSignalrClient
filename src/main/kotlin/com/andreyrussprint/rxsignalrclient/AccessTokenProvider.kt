package com.andreyrussprint.rxsignalrclient

import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposables

/**
 * Token provider for [RxSignalrClient].
 *
 * Allows to change [authToken] without having to reinitialize the SignalR client.
 */
class AccessTokenProvider : Single<String>() {
    var authToken: String? = null

    override fun subscribeActual(observer: SingleObserver<in String>) {
        observer.onSubscribe(Disposables.disposed())
        observer.onSuccess(authToken ?: "")
    }
}
