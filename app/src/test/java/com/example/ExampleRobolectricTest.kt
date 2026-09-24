package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.HandshakeValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("E-Studio Scan", appName)
    }

    @Test
    fun `validate sha256 hashing`() {
        val hash = HandshakeValidator.sha256("test")
        assertEquals(64, hash.length)
    }

    @Test
    fun `parse valid pairing url`() {
        val url = "https://192.168.1.50:8080/?mode=pwa&inst=MAGASIN-01&tok=abc123token&pin=4321&sig=sig123"
        val result = HandshakeValidator.parsePairingUrl(url)
        assertTrue(result.isSuccess)
        val config = result.getOrThrow()
        assertEquals("192.168.1.50", config.serverHost)
        assertEquals(8080, config.serverPort)
        assertEquals("MAGASIN-01", config.instanceId)
        assertEquals("4321", config.pin)
        assertTrue(config.isPaired)
    }
}
