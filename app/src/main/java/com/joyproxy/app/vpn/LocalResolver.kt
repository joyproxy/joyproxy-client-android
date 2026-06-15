package com.joyproxy.app.vpn

import android.net.DnsResolver
import android.os.Build
import android.os.CancellationSignal
import android.system.ErrnoException
import androidx.annotation.RequiresApi
import io.nekohasekai.libbox.ExchangeContext
import io.nekohasekai.libbox.LocalDNSTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch

object LocalResolver : LocalDNSTransport {
    private const val RCODE_NXDOMAIN = 3
    private const val RCODE_SERVFAIL = 2

    override fun raw(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun exchange(ctx: ExchangeContext, message: ByteArray) {
        // native 热路径：绝不能向上抛异常，否则整个进程会 abort。
        try {
            val defaultNetwork = DefaultNetworkMonitor.defaultNetwork
            if (defaultNetwork == null) {
                ctx.errorCode(RCODE_SERVFAIL)
                return
            }
            val latch = CountDownLatch(1)
            val signal = CancellationSignal()
            ctx.onCancel(signal::cancel)
            val callback =
                object : DnsResolver.Callback<ByteArray> {
                    override fun onAnswer(answer: ByteArray, rcode: Int) {
                        if (rcode == 0) {
                            ctx.rawSuccess(answer)
                        } else {
                            ctx.errorCode(rcode)
                        }
                        latch.countDown()
                    }

                    override fun onError(error: DnsResolver.DnsException) {
                        when (val cause = error.cause) {
                            is ErrnoException -> ctx.errnoCode(cause.errno)
                            else -> ctx.errorCode(RCODE_SERVFAIL)
                        }
                        latch.countDown()
                    }
                }
            DnsResolver.getInstance().rawQuery(
                defaultNetwork,
                message,
                DnsResolver.FLAG_NO_RETRY,
                Dispatchers.IO.asExecutor(),
                signal,
                callback,
            )
            latch.await()
        } catch (t: Throwable) {
            runCatching { ctx.errorCode(RCODE_SERVFAIL) }
        }
    }

    override fun lookup(ctx: ExchangeContext, network: String, domain: String) {
        try {
            val defaultNetwork = DefaultNetworkMonitor.defaultNetwork
            if (defaultNetwork == null) {
                ctx.errorCode(RCODE_SERVFAIL)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val latch = CountDownLatch(1)
                val signal = CancellationSignal()
                ctx.onCancel(signal::cancel)
                val callback =
                    object : DnsResolver.Callback<Collection<InetAddress>> {
                        override fun onAnswer(answer: Collection<InetAddress>, rcode: Int) {
                            if (rcode == 0) {
                                ctx.success(answer.mapNotNull { it.hostAddress }.joinToString("\n"))
                            } else {
                                ctx.errorCode(rcode)
                            }
                            latch.countDown()
                        }

                        override fun onError(error: DnsResolver.DnsException) {
                            when (val cause = error.cause) {
                                is ErrnoException -> ctx.errnoCode(cause.errno)
                                else -> ctx.errorCode(RCODE_SERVFAIL)
                            }
                            latch.countDown()
                        }
                    }
                val type =
                    when {
                        network.endsWith("4") -> DnsResolver.TYPE_A
                        network.endsWith("6") -> DnsResolver.TYPE_AAAA
                        else -> null
                    }
                if (type != null) {
                    DnsResolver.getInstance().query(
                        defaultNetwork,
                        domain,
                        type,
                        DnsResolver.FLAG_NO_RETRY,
                        Dispatchers.IO.asExecutor(),
                        signal,
                        callback,
                    )
                } else {
                    DnsResolver.getInstance().query(
                        defaultNetwork,
                        domain,
                        DnsResolver.FLAG_NO_RETRY,
                        Dispatchers.IO.asExecutor(),
                        signal,
                        callback,
                    )
                }
                latch.await()
            } else {
                val answer =
                    try {
                        InetAddress.getAllByName(domain)
                    } catch (e: UnknownHostException) {
                        ctx.errorCode(RCODE_NXDOMAIN)
                        return
                    }
                ctx.success(answer.mapNotNull { it.hostAddress }.joinToString("\n"))
            }
        } catch (t: Throwable) {
            runCatching { ctx.errorCode(RCODE_SERVFAIL) }
        }
    }
}
