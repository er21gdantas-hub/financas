package br.com.minhasfinancas.ui.transacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.local.entity.TipoTransacao
import br.com.minhasfinancas.data.local.entity.TransacaoEntity
import br.com.minhasfinancas.data.repository.Repositorio
import br.com.minhasfinancas.ui.components.ItemTransacao
import br.com.minhasfinancas.ui.components.item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

private data class Filtro(val mes: YearMonth, val tipo: TipoTransacao?, val categoria: Long?, val conta: Long?)

@HiltViewModel
class TransacoesViewModel @Inject constructor(private val repo: Repositorio) : ViewModel() {
    val mes = MutableStateFlow(YearMonth.now())
    val tipo = MutableStateFlow<TipoTransacao?>(null)
    val categoria = MutableStateFlow<Long?>(null)
    val conta = MutableStateFlow<Long?>(null)

    val categorias = repo.categorias.todas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val contas = repo.contas.todas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val itens: StateFlow<List<ItemTransacao>> =
        combine(mes, tipo, categoria, conta) { m, t, c, a -> Filtro(m, t, c, a) }
            .flatMapLatest { f ->
                combine(
                    repo.transacoes.doPeriodo(f.mes.atDay(1).toEpochDay(), f.mes.atEndOfMonth().toEpochDay()),
                    repo.categorias.todas(),
                ) { lista, cats ->
                    val mapa = cats.associateBy { it.id }
                    lista
                        .filter { (f.tipo == null || it.tipo == f.tipo) && (f.categoria == null || it.categoriaId == f.categoria) && (f.conta == null || it.contaId == f.conta) }
                        .map { it.item(mapa) }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun alternarPaga(t: TransacaoEntity) {
        viewModelScope.launch { runCatching { repo.transacoes.atualizar(t.copy(paga = !t.paga)) } }
    }
}
