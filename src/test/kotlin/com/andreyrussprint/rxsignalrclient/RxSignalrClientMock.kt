package com.andreyrussprint.rxsignalrclient

import com.microsoft.signalr.HubConnection

open class RxSignalrClientMock : RxSignalrClient("Test url") {

    public override var hubConnection: HubConnection = super.hubConnection
    public override var accessTokenProvider: AccessTokenProvider = super.accessTokenProvider
}
