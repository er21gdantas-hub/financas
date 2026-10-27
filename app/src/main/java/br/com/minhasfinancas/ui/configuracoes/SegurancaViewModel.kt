package br.com.minhasfinancas.ui.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.prefs.Config
import br.com.minhasfinancas.data.prefs.ConfigDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SegurancaViewModel @Inject constructor(private val cfg: ConfigDataStore) : ViewModel() {
    val config: StateFlow<Config> = cfg.config.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config(true, false, false))

    fun definirPin(pin: String) { viewModelScope.launch { cfg.definirPin(pin) } }
    fun removerPin() { viewModelScope.launch { cfg.removerPin() } }
    fun definirBiometria(ativa: Boolean) { viewModelScope.launch { cfg.definirBiometria(ativa) } }
}
