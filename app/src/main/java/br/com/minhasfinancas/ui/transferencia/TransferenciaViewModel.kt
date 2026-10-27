package br.com.minhasfinancas.ui.transferencia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.repository.Repositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TransferenciaEstado(
    val origem: Long? = null,
    val destino: Long? = null,
    val centavos: Long = 0,
    val data: LocalDate = LocalDate.now(),
    val descricao: String = "",
    val erro: String? = null,
)

@HiltViewModel
class TransferenciaViewModel @Inject constructor(private val repo: Repositorio) : ViewModel() {
    var estado by mutableStateOf(TransferenciaEstado())
        private set

    val contas = repo.contas.todas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun editar(bloco: TransferenciaEstado.() -> TransferenciaEstado) {
        estado = estado.bloco().copy(erro = null)
    }

    fun salvar(aoConcluir: () -> Unit) {
        val e = estado
        val origem = e.origem
        val destino = e.destino
        when {
            origem == null || destino == null -> { estado = e.copy(erro = "Escolha a conta de origem e a de destino"); return }
            origem == destino -> { estado = e.copy(erro = "Origem e destino precisam ser contas diferentes"); return }
            e.centavos <= 0 -> { estado = e.copy(erro = "Informe um valor maior que zero"); return }
        }
        viewModelScope.launch {
            runCatching { repo.transferir(origem!!, destino!!, e.centavos, e.data, e.descricao.trim().ifBlank { "Transferência" }) }
                .onSuccess { aoConcluir() }
                .onFailure { estado = estado.copy(erro = "Não foi possível transferir. Tente novamente.") }
        }
    }
}
