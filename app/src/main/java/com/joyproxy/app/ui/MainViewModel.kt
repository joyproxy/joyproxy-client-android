package com.joyproxy.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joyproxy.app.R
import com.joyproxy.app.config.AppLanguage
import com.joyproxy.app.config.DnsMode
import com.joyproxy.app.config.DnsProvider
import com.joyproxy.app.config.ProxyProtocol
import com.joyproxy.app.config.ProxyScope
import com.joyproxy.app.config.ProxySettings
import com.joyproxy.app.config.SavedProxy
import com.joyproxy.app.data.LanguageRepository
import com.joyproxy.app.data.ProxyHistoryRepository
import com.joyproxy.app.data.SettingsRepository
import com.joyproxy.app.network.ProxyTester
import com.joyproxy.app.vpn.VpnController
import com.joyproxy.app.vpn.VpnStatusBus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class ProxyTestStatus {
    Idle,
    Testing,
    Success,
    Failed,
}

data class ProxyTestState(
    val status: ProxyTestStatus = ProxyTestStatus.Idle,
    val message: String = "",
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    private val historyRepository = ProxyHistoryRepository(application)
    private val languageRepository = LanguageRepository(application)
    private var saveJob: Job? = null

    private val _settings = MutableStateFlow(ProxySettings())
    val settings: StateFlow<ProxySettings> = _settings.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _testState = MutableStateFlow(ProxyTestState())
    val testState: StateFlow<ProxyTestState> = _testState.asStateFlow()

    private val _connecting = MutableStateFlow(false)
    val connecting: StateFlow<Boolean> = _connecting.asStateFlow()

    private val _savedProxies = MutableStateFlow<List<SavedProxy>>(emptyList())
    val savedProxies: StateFlow<List<SavedProxy>> = _savedProxies.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.DEFAULT)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.history.collect { _savedProxies.value = it }
        }
        viewModelScope.launch {
            languageRepository.language.collect { _language.value = it }
        }
        viewModelScope.launch {
            val saved = repository.settings.first()
            _settings.value = saved.copy(connected = false)
            if (saved.connected) {
                repository.setConnected(false)
            }
        }
        viewModelScope.launch {
            VpnStatusBus.connected.collect { connected ->
                _settings.value = _settings.value.copy(connected = connected)
            }
        }
        viewModelScope.launch {
            VpnStatusBus.events.collect { event ->
                when (event) {
                    is VpnStatusBus.Event.Connecting -> _connecting.value = true
                    is VpnStatusBus.Event.Connected -> {
                        _connecting.value = false
                        saveHistoryOnConnect()
                    }
                    is VpnStatusBus.Event.Disconnected -> _connecting.value = false
                    is VpnStatusBus.Event.Failed -> {
                        _connecting.value = false
                        _message.value = event.message
                    }
                }
            }
        }
    }

    private fun saveHistoryOnConnect() {
        viewModelScope.launch {
            val current = _settings.value
            if (current.isValid()) {
                historyRepository.upsert(current)
            }
        }
    }

    private fun update(block: (ProxySettings) -> ProxySettings) {
        val updated = block(_settings.value)
        _settings.value = updated
        saveJob?.cancel()
        saveJob =
            viewModelScope.launch {
                delay(400)
                repository.save(updated.copy(connected = _settings.value.connected))
            }
    }

    suspend fun flushSettings() {
        saveJob?.cancel()
        repository.save(_settings.value.copy(connected = VpnStatusBus.connected.value))
    }

    fun setProtocol(protocol: ProxyProtocol) = update { it.copy(protocol = protocol) }
    fun setHost(host: String) = update { it.copy(host = host) }
    fun setPort(port: Int) = update { it.copy(port = port) }
    fun setUsername(username: String) = update { it.copy(username = username) }
    fun setPassword(password: String) = update { it.copy(password = password) }
    fun setScope(scope: ProxyScope) {
        update { it.copy(scope = scope) }
        notifyReconnectIfConnected(R.string.setting_proxy_scope)
    }

    fun setSelectedApps(apps: Set<String>) {
        update { it.copy(selectedApps = apps) }
        notifyReconnectIfConnected(R.string.setting_app_list)
    }

    fun setDnsMode(mode: DnsMode) {
        update { it.copy(dnsMode = mode) }
        notifyReconnectIfConnected(R.string.setting_dns_mode)
    }

    fun setDnsProvider(provider: DnsProvider) {
        update {
            it.copy(
                dnsProvider = provider,
                dohUrl = provider.dohUrl,
                customDns = provider.plainDns,
            )
        }
        notifyReconnectIfConnected(R.string.setting_dns_provider)
    }

    fun applySavedProxy(proxy: SavedProxy) {
        update {
            it.copy(
                protocol = proxy.protocol,
                host = proxy.host,
                port = proxy.port,
                username = proxy.username,
                password = proxy.password,
            )
        }
    }

    fun deleteSavedProxy(id: String) {
        viewModelScope.launch {
            historyRepository.delete(id)
        }
    }

    fun setLanguage(language: AppLanguage, onChanged: () -> Unit) {
        viewModelScope.launch {
            if (_language.value == language) return@launch
            languageRepository.setLanguage(language)
            onChanged()
        }
    }

    private fun notifyReconnectIfConnected(settingNameRes: Int) {
        if (_settings.value.connected) {
            val app = getApplication<Application>()
            _message.value =
                app.getString(
                    R.string.setting_changed_reconnect,
                    app.getString(settingNameRes),
                )
        }
    }

    fun testProxy() {
        val current = _settings.value
        val app = getApplication<Application>()
        if (current.connected || _connecting.value) {
            _testState.value = ProxyTestState(ProxyTestStatus.Failed, app.getString(R.string.test_while_connected))
            return
        }
        if (!current.isValid()) {
            _testState.value = ProxyTestState(ProxyTestStatus.Failed, app.getString(R.string.fill_valid_proxy))
            return
        }

        viewModelScope.launch {
            _testState.value = ProxyTestState(ProxyTestStatus.Testing, app.getString(R.string.testing_proxy))
            val result = ProxyTester.test(app, current)
            _testState.value =
                ProxyTestState(
                    status = if (result.success) ProxyTestStatus.Success else ProxyTestStatus.Failed,
                    message = result.message,
                )
        }
    }

    fun connect(onNeedPermission: () -> Unit) {
        val current = _settings.value
        val app = getApplication<Application>()
        if (!current.isValid()) {
            _message.value = app.getString(R.string.connect_fill_valid)
            return
        }
        if (current.scope != ProxyScope.GLOBAL && current.selectedApps.isEmpty()) {
            _message.value = app.getString(R.string.select_at_least_one_app)
            return
        }
        onNeedPermission()
    }

    fun startVpn() {
        val current = _settings.value
        VpnController.start(getApplication(), current)
    }

    fun disconnect() {
        VpnController.stop(getApplication())
    }

    fun clearMessage() {
        _message.value = null
    }

    fun showMessage(message: String) {
        _message.value = message
    }
}
