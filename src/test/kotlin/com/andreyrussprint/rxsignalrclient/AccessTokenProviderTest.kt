package com.andreyrussprint.rxsignalrclient

import org.junit.Test

class AccessTokenProviderTest {

    private val provider = AccessTokenProvider()

    @Test
    fun `Provider without token - when subscribe - return empty token`() {
        provider.test()
                .assertValue("")
                .dispose()
    }

    @Test
    fun `Provider with token - when subscribe - return this token`() {
        val token = "Test token"
        provider.authToken = token

        provider.test()
                .assertValue(token)
                .dispose()
    }
}
