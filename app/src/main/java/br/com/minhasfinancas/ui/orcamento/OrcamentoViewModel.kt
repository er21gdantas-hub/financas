package br.com.minhasfinancas.ui.orcamento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.local.entity.CategoriaEntity
import br.com.minhasfinancas.data.local.entity.OrcamentoEntity
import br.com.minhasfinancas.data.local.entity.TipoCategoria
import br.com.minhasfinancas.data.repository.Repositorio
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

data class LinhaOrcamento(val categoria: CategoriaEntity, val limite: Long?, val gasto: Long)

@HiltViewModel
class OrcamentoViewModel @Inject constructor(private val repo: Repositorio) : ViewModel() {
    val mes = MutableStateFlow(YearMonth.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val linhas: StateFlow<List<LinhaOrcamento>> = mes.flatMapLatest { m ->
        combine(
            repo.transacoes.gastosPorCategoria(m.atDay(1).toEpochDay(), m.atEndOfMonth().toEpochDay()),
            repo.orcamentos.todos(),
            repo.categorias.todas(),
        ) { gastos, orcs, cats ->
            val g = gastos.associate { it.categoriaId to it.total }
            val o = orcs.associate { it.categoriaId to it.limite }
            cats.filter { it.tipo != TipoCategoria.RECEITA }
                .map { LinhaOrcamento(it, o[it.id], g[it.id] ?: 0L) }
                .sortedWith(compareBy({ it.limite == null }, { it.categoria.nome }))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Limite zero remove o orçamento da categoria. */
    fun definirLimite(categoriaId: Long, limite: Long) {
        viewModelScope.launch {
            runCatching {
                if (limite <= 0) repo.orcamentos.remover(categoriaId)
                else repo.orcamentos.salvar(OrcamentoEntity(categoriaId, limite))
            }
        }
    }
}
