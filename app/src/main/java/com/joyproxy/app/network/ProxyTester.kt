package com.joyproxy.app.network

import android.content.Context
import com.joyproxy.app.R
import com.joyproxy.app.config.ProxyProtocol
import com.joyproxy.app.config.ProxySettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlin.math.min

object ProxyTester {
    data class Result(
        val success: Boolean,
        val message: String,
        val latencyMs: Long = 0,
    )

    private const val TEST_HOST = "www.baidu.com"
    private const val TEST_PORT = 443
    private const val TIMEOUT_MS = 8000

    suspend fun test(context: Context, settings: ProxySettings): Result = withContext(Dispatchers.IO) {
        if (!settings.isValid()) {
            return@withContext Result(false, context.getString(R.string.fill_valid_proxy))
        }

        val start = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(settings.host, settings.port), TIMEOUT_MS)
                socket.soTimeout = TIMEOUT_MS
                val input = socket.getInputStream()
                val output = socket.getOutputStream()

                when (settings.protocol) {
                    ProxyProtocol.SOCKS5 -> testSocks5(context, settings, input, output)
                    ProxyProtocol.HTTP -> testHttp(context, settings, input, output)
                }
            }
            val latency = System.currentTimeMillis() - start
            Result(true, context.getString(R.string.test_success, latency), latency)
        } catch (e: Exception) {
            Result(false, context.getString(R.string.connection_failed, friendlyMessage(context, e)))
        }
    }

    private fun testSocks5(context: Context, settings: ProxySettings, input: InputStream, output: OutputStream) {
        val hasAuth = settings.username.isNotBlank()
        if (hasAuth) {
            output.write(byteArrayOf(0x05, 0x02, 0x00, 0x02))
        } else {
            output.write(byteArrayOf(0x05, 0x01, 0x00))
        }
        output.flush()

        val methodResponse = readExact(context, input, 2)
        if (methodResponse[0] != 0x05.toByte()) error(context.getString(R.string.socks5_handshake_failed))
        if (methodResponse[1] == 0xFF.toByte()) error(context.getString(R.string.proxy_auth_not_supported))

        if (methodResponse[1] == 0x02.toByte()) {
            val user = settings.username.toByteArray(StandardCharsets.UTF_8)
            val pass = settings.password.toByteArray(StandardCharsets.UTF_8)
            val auth = ByteArray(3 + user.size + pass.size)
            auth[0] = 0x01
            auth[1] = user.size.toByte()
            System.arraycopy(user, 0, auth, 2, user.size)
            auth[2 + user.size] = pass.size.toByte()
            System.arraycopy(pass, 0, auth, 3 + user.size, pass.size)
            output.write(auth)
            output.flush()

            val authResponse = readExact(context, input, 2)
            if (authResponse[1] != 0x00.toByte()) error(context.getString(R.string.proxy_auth_failed))
        }

        val hostBytes = TEST_HOST.toByteArray(StandardCharsets.UTF_8)
        val request = ByteArray(7 + hostBytes.size)
        request[0] = 0x05
        request[1] = 0x01
        request[2] = 0x00
        request[3] = 0x03
        request[4] = hostBytes.size.toByte()
        System.arraycopy(hostBytes, 0, request, 5, hostBytes.size)
        request[5 + hostBytes.size] = (TEST_PORT shr 8).toByte()
        request[6 + hostBytes.size] = (TEST_PORT and 0xFF).toByte()
        output.write(request)
        output.flush()

        val connectResponse = readExact(context, input, 4)
        if (connectResponse[1] != 0x00.toByte()) {
            error(
                context.getString(
                    R.string.proxy_forward_failed,
                    socks5Error(context, connectResponse[1].toInt() and 0xFF),
                ),
            )
        }
        skipSocks5Address(context, input, connectResponse[3])
    }

    private fun testHttp(context: Context, settings: ProxySettings, input: InputStream, output: OutputStream) {
        val target = "$TEST_HOST:$TEST_PORT"
        val builder = StringBuilder()
            .append("CONNECT ").append(target).append(" HTTP/1.1\r\n")
            .append("Host: ").append(target).append("\r\n")
        if (settings.username.isNotBlank()) {
            val token =
                Base64.getEncoder().encodeToString(
                    "${settings.username}:${settings.password}".toByteArray(StandardCharsets.UTF_8),
                )
            builder.append("Proxy-Authorization: Basic ").append(token).append("\r\n")
        }
        builder.append("Connection: close\r\n\r\n")
        output.write(builder.toString().toByteArray(StandardCharsets.UTF_8))
        output.flush()

        val response = readLine(input)
        if (!response.startsWith("HTTP/1.")) error(context.getString(R.string.http_response_invalid))
        if (!response.contains(" 200 ")) error(context.getString(R.string.http_proxy_rejected, response))
    }

    private fun readExact(context: Context, input: InputStream, size: Int): ByteArray {
        val buffer = ByteArray(size)
        var offset = 0
        while (offset < size) {
            val read = input.read(buffer, offset, size - offset)
            if (read < 0) error(context.getString(R.string.connection_closed))
            offset += read
        }
        return buffer
    }

    private fun readLine(input: InputStream): String {
        val buffer = StringBuilder()
        while (true) {
            val ch = input.read()
            if (ch < 0) break
            if (ch == '\n'.code) break
            if (ch != '\r'.code) buffer.append(ch.toChar())
        }
        return buffer.toString()
    }

    private fun skipSocks5Address(context: Context, input: InputStream, addressType: Byte) {
        when (addressType) {
            0x01.toByte() -> readExact(context, input, 4 + 2)
            0x03.toByte() -> {
                val length = input.read()
                readExact(context, input, length + 2)
            }
            0x04.toByte() -> readExact(context, input, 16 + 2)
            else -> error(context.getString(R.string.socks5_address_invalid))
        }
    }

    private fun socks5Error(context: Context, code: Int): String =
        when (code) {
            1 -> context.getString(R.string.socks5_error_general)
            2 -> context.getString(R.string.socks5_error_not_allowed)
            3 -> context.getString(R.string.socks5_error_network_unreachable)
            4 -> context.getString(R.string.socks5_error_host_unreachable)
            5 -> context.getString(R.string.socks5_error_refused)
            6 -> context.getString(R.string.socks5_error_ttl_expired)
            7 -> context.getString(R.string.socks5_error_command_not_supported)
            8 -> context.getString(R.string.socks5_error_address_not_supported)
            else -> context.getString(R.string.socks5_error_code, code)
        }

    private fun friendlyMessage(context: Context, error: Exception): String {
        val message = error.message ?: error.javaClass.simpleName
        return when {
            message.contains("ECONNREFUSED", ignoreCase = true) ->
                context.getString(R.string.error_econnrefused)
            message.contains("timed out", ignoreCase = true) ->
                context.getString(R.string.error_timed_out)
            message.contains("Unable to resolve host", ignoreCase = true) ->
                context.getString(R.string.error_resolve_host)
            message.contains("Network is unreachable", ignoreCase = true) ->
                context.getString(R.string.error_network_unreachable)
            else -> message.take(min(message.length, 120))
        }
    }
}
