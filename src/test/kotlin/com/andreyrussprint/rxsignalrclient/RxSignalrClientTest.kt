package com.andreyrussprint.rxsignalrclient

import com.microsoft.signalr.Action1
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.Subscription
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import org.junit.Assert.*
import org.junit.Test

class RxSignalrClientTest {

    private val clientMock = RxSignalrClientMock()
    private val client: RxSignalrClient = clientMock

    private val exception = mock<Throwable>()

    @Test
    fun `Start connection - if success - return complete`() {
        clientMock.hubConnection = mock {
            on { start() } doReturn Completable.complete()
        }

        client.startConnection()
                .test()
                .assertComplete()
                .dispose()
    }

    @Test
    fun `Start connection - if failure - return exception`() {
        clientMock.hubConnection = mock {
            on { start() } doReturn Completable.error(exception)
        }

        client.startConnection()
                .test()
                .assertNotComplete()
                .assertError(exception)
                .dispose()
    }

    @Test
    fun `Stop connection - if success - return complete`() {
        clientMock.hubConnection = mock {
            on { stop() } doReturn Completable.complete()
        }

        client.stopConnection()
                .test()
                .assertComplete()
                .dispose()
    }

    @Test
    fun `Stop connection - if failure - return exception`() {
        clientMock.hubConnection = mock {
            on { stop() } doReturn Completable.error(exception)
        }

        client.stopConnection()
                .test()
                .assertNotComplete()
                .assertError(exception)
                .dispose()
    }

    @Test
    fun `Is connected - if connection state connected - return true`() {
        clientMock.hubConnection = mock {
            on { connectionState } doReturn HubConnectionState.CONNECTED
        }

        assertTrue(client.isConnected())
    }

    @Test
    fun `Is connected - if connection state disconnected - return false`() {
        clientMock.hubConnection = mock {
            on { connectionState } doReturn HubConnectionState.DISCONNECTED
        }

        assertFalse(client.isConnected())
    }

    @Test
    fun `Subscribe on - subscribed to hub connection method`() {
        val hubMethod = "Test hub method"
        val param = Int::class.java

        clientMock.hubConnection = mock {
            on { on(eq(hubMethod), any(), eq(param)) } doReturn mock()
        }

        client.subscribeOn(hubMethod, param)
                .test()
                .dispose()

        verify(clientMock.hubConnection).on(eq(hubMethod), any(), eq(param))
    }

    @Test
    fun `Subscribe on - if new item received - observable emit`() {
        lateinit var newItemsCallback: Action1<Int>
        clientMock.hubConnection = mock {
            on { on(any(), any(), eq(Int::class.java)) } doAnswer {
                newItemsCallback = it.getArgument<Action1<Int>>(1)
                null
            }
        }

        val updates = client.subscribeOn("Test", Int::class.java)
                .test()
                .assertNoValues()

        newItemsCallback.invoke(5)

        updates.assertValue(5)
                .dispose()
    }

    @Test
    fun `Subscribe on - if observable disposed - unsubscribe`() {
        val subscription = mock<Subscription>()
        clientMock.hubConnection = mock {
            on { on(any(), any(), eq(Int::class.java)) } doReturn subscription
        }

        client.subscribeOn("Test", Int::class.java)
                .test()
                .dispose()

        verify(subscription).unsubscribe()
    }

    @Test
    fun `Set auth token - if new token - change token in access token provider`() {
        val token = "Test token"

        client.setAuthToken(token)

        assertEquals(token, clientMock.accessTokenProvider.authToken)
    }

    @Test
    fun `Set auth token - if new token - if is connected - stop connection`() {
        val client = spy<RxSignalrClientMock> {
            on { isConnected() } doReturn true
        }

        client.setAuthToken("New token")

        verify(client).stopConnection()
    }

    @Test
    fun `Set auth token - if new token - if is disconnected - don't stop connection`() {
        val client = spy<RxSignalrClientMock> {
            on { isConnected() } doReturn false
        }

        client.setAuthToken("New token")

        verify(client, never()).stopConnection()
    }

    @Test
    fun `Set auth token - if the same - do nothing`() {
        val client = spy<RxSignalrClientMock>()

        client.setAuthToken(client.accessTokenProvider.authToken)

        verify(client, never()).isConnected()
        verify(client, never()).stopConnection()
    }
}
