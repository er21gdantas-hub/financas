package br.com.minhasfinancas.ui.contas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.local.entity.ContaEntity
import br.com.minhasfinancas.data.repository.Repositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContaComSaldo(val conta: ContaEntity, val saldo: Long)

@HiltViewModel
class ContasViewModel @Inject constructor(private val repo: Repositorio) : ViewModel() {
    val contas = combine(repo.contas.todas(), repo.contas.saldos()) { contas, saldos ->
        val mapa = saldos.associate { it.contaId to it.saldo }
        contas.map { ContaComSaldo(it, mapa[it.id] ?: it.saldoInicial) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun salvar(c: ContaEntity) {
        viewModelScope.launch { runCatching { if (c.id == 0L) repo.contas.inserir(c) else repo.contas.atualizar(c) } }
    }

    /** Arquiva em vez de apagar: preserva o histórico de lançamentos da conta. */
    fun arquivar(c: ContaEntity) {
        viewModelScope.launch { runCatching { repo.contas.atualizar(c.copy(arquivada = true)) } }
    }
}
