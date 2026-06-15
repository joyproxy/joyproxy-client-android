package com.joyproxy.app.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import com.joyproxy.app.config.DnsMode
import com.joyproxy.app.config.ProxyProtocol
import com.joyproxy.app.config.ProxyScope
import com.joyproxy.app.config.ProxySettings

object VpnController {
    const val EXTRA_PROTOCOL = "protocol"
    const val EXTRA_HOST = "host"
    const val EXTRA_PORT = "port"
    const val EXTRA_USERNAME = "username"
    const val EXTRA_PASSWORD = "password"
    const val EXTRA_SCOPE = "scope"
    const val EXTRA_SELECTED_APPS = "selected_apps"
    const val EXTRA_DNS_MODE = "dns_mode"
    const val EXTRA_CUSTOM_DNS = "custom_dns"
    const val EXTRA_DOH_URL = "doh_url"

    fun start(context: Context, settings: ProxySettings) {
        val intent =
            Intent(context, ProxyVpnService::class.java).apply {
                putProxySettings(settings)
            }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
        val intent =
            Intent(context, ProxyVpnService::class.java).apply {
                action = ProxyVpnService.ACTION_STOP
            }
        context.startService(intent)
    }

    fun prepare(context: Context): Intent? = VpnService.prepare(context)
}

fun Intent.putProxySettings(settings: ProxySettings) {
    putExtra(VpnController.EXTRA_PROTOCOL, settings.protocol.name)
    putExtra(VpnController.EXTRA_HOST, settings.host)
    putExtra(VpnController.EXTRA_PORT, settings.port)
    putExtra(VpnController.EXTRA_USERNAME, settings.username)
    putExtra(VpnController.EXTRA_PASSWORD, settings.password)
    putExtra(VpnController.EXTRA_SCOPE, settings.scope.name)
    putStringArrayListExtra(VpnController.EXTRA_SELECTED_APPS, ArrayList(settings.selectedApps))
    putExtra(VpnController.EXTRA_DNS_MODE, settings.dnsMode.name)
    putExtra(VpnController.EXTRA_CUSTOM_DNS, settings.customDns)
    putExtra(VpnController.EXTRA_DOH_URL, settings.dohUrl)
}

fun Intent.readProxySettings(): ProxySettings? {
    if (!hasExtra(VpnController.EXTRA_HOST)) return null
    return ProxySettings(
        protocol =
            runCatching {
                ProxyProtocol.valueOf(getStringExtra(VpnController.EXTRA_PROTOCOL) ?: ProxyProtocol.SOCKS5.name)
            }.getOrDefault(ProxyProtocol.SOCKS5),
        host = getStringExtra(VpnController.EXTRA_HOST) ?: return null,
        port = getIntExtra(VpnController.EXTRA_PORT, 0),
        username = getStringExtra(VpnController.EXTRA_USERNAME) ?: "",
        password = getStringExtra(VpnController.EXTRA_PASSWORD) ?: "",
        scope =
            runCatching {
                ProxyScope.valueOf(getStringExtra(VpnController.EXTRA_SCOPE) ?: ProxyScope.GLOBAL.name)
            }.getOrDefault(ProxyScope.GLOBAL),
        selectedApps = getStringArrayListExtra(VpnController.EXTRA_SELECTED_APPS)?.toSet() ?: emptySet(),
        dnsMode =
            runCatching {
                DnsMode.valueOf(getStringExtra(VpnController.EXTRA_DNS_MODE) ?: DnsMode.FAKE_IP.name)
            }.getOrDefault(DnsMode.FAKE_IP),
        customDns = getStringExtra(VpnController.EXTRA_CUSTOM_DNS) ?: "223.5.5.5",
        dohUrl = getStringExtra(VpnController.EXTRA_DOH_URL) ?: "https://dns.alidns.com/dns-query",
        connected = true,
    )
}
