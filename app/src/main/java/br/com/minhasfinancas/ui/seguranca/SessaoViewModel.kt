package br.com.minhasfinancas.ui.seguranca

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.prefs.Config
import br.com.minhasfinancas.data.prefs.ConfigDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Controla onboarding e bloqueio. Ao reabrir o processo o app volta bloqueado (se houver PIN). */
@HiltViewModel
class SessaoViewModel @Inject constructor(private val cfg: ConfigDataStore) : ViewModel() {
    companion object {
        private const val TOLERANCIA_MS = 60_000L // tempo fora do app antes de bloquear de novo
        private const val MAX_FALHAS = 5
        private const val ESPERA_MS = 30_000L
    }

    val config: StateFlow<Config?> = cfg.config.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val desbloqueado = MutableStateFlow(false)
    val erro = MutableStateFlow<String?>(null)

    private var falhas = 0
    private var bloqueadoAte = 0L
    private var parouEm = 0L

    init {
        // Sem PIN ao abrir o app: nada a desbloquear.
        viewModelScope.launch {
            if (!config.filterNotNull().first().pinAtivo) desbloqueado.value = true
        }
    }

    fun concluirOnboarding() { viewModelScope.launch { cfg.concluirOnboarding() } }

    fun tentarPin(pin: String) {
        val agora = SystemClock.elapsedRealtime()
        if (agora < bloqueadoAte) { erro.value = "Aguarde ${(bloqueadoAte - agora) / 1000 + 1}s para tentar de novo."; return }
        viewModelScope.launch {
            if (cfg.verificarPin(pin)) {
                falhas = 0; erro.value = null; desbloqueado.value = true
            } else {
                falhas++
                if (falhas >= MAX_FALHAS) {
                    falhas = 0
                    bloqueadoAte = SystemClock.elapsedRealtime() + ESPERA_MS
                    erro.value = "Muitas tentativas. Aguarde 30 segundos."
                } else erro.value = "PIN incorreto."
            }
        }
    }

    fun desbloquearPorBiometria() { erro.value = null; desbloqueado.value = true }

    fun aoParar() { parouEm = SystemClock.elapsedRealtime() }

    fun aoIniciar() {
        val pinAtivo = config.value?.pinAtivo == true
        if (pinAtivo && parouEm > 0 && SystemClock.elapsedRealtime() - parouEm > TOLERANCIA_MS) desbloqueado.value = false
        parouEm = 0
    }
}
