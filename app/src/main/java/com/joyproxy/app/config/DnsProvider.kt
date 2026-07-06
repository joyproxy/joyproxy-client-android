package com.joyproxy.app.config

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joyproxy.app.R

enum class DnsProvider(
    val dohUrl: String,
    val plainDns: String,
) {
    GOOGLE("https://dns.google/dns-query", "8.8.8.8"),
    CLOUDFLARE("https://cloudflare-dns.com/dns-query", "1.1.1.1"),
    OPENDNS("https://doh.opendns.com/dns-query", "208.67.222.222"),
    QUAD9("https://dns.quad9.net/dns-query", "9.9.9.9"),
    ALIDNS("https://dns.alidns.com/dns-query", "223.5.5.5"),
    ;

    @StringRes
    fun labelRes(): Int =
        when (this) {
            GOOGLE -> R.string.dns_provider_google
            CLOUDFLARE -> R.string.dns_provider_cloudflare
            OPENDNS -> R.string.dns_provider_opendns
            QUAD9 -> R.string.dns_provider_quad9
            ALIDNS -> R.string.dns_provider_alidns
        }

    fun localizedLabel(context: Context): String = context.getString(labelRes())

    companion object {
        fun fromId(id: String?): DnsProvider =
            entries.find { it.name == id } ?: GOOGLE

        fun fromDohUrl(url: String): DnsProvider =
            entries.find { it.dohUrl == url } ?: GOOGLE

        fun fromPlainDns(dns: String): DnsProvider =
            entries.find { it.plainDns == dns } ?: GOOGLE
    }
}

@Composable
fun DnsProvider.displayLabel(): String = stringResource(labelRes())
