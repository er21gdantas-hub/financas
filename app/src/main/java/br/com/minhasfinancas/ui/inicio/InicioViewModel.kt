package br.com.minhasfinancas.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.repository.Repositorio
import br.com.minhasfinancas.domain.model.ResumoMensal
import br.com.minhasfinancas.domain.usecase.StatusOrcamento
import br.com.minhasfinancas.domain.usecase.statusOrcamento
import br.com.minhasfinancas.util.Fmt
import br.com.minhasfinancas.ui.components.ItemTransacao
import br.com.minhasfinancas.ui.components.item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

data class GastoUi(val nome: String, val cor: Long, val valor: Long)
data class InicioUi(
    val resumo: ResumoMensal = ResumoMensal(),
    val gastos: List<GastoUi> = emptyList(),
    val ultimas: List<ItemTransacao> = emptyList(),
    val alertas: List<String> = emptyList(),
)

@HiltViewModel
class InicioViewModel @Inject constructor(repo: Repositorio) : ViewModel() {
    private val mes = YearMonth.now()
    private val ini = mes.atDay(1).toEpochDay()
    private val fim = mes.atEndOfMonth().toEpochDay()

    private val resumo = combine(
        repo.contas.saldos(),
        repo.transacoes.total("RECEITA", ini, fim),
        repo.transacoes.total("DESPESA", ini, fim),
    ) { saldos, receitas, despesas -> ResumoMensal(saldos.sumOf { it.saldo }, receitas, despesas) }

    val ui: StateFlow<InicioUi> = combine(
        resumo,
        repo.transacoes.gastosPorCategoria(ini, fim),
        repo.transacoes.ultimas(8),
        repo.categorias.todas(),
        repo.orcamentos.todos(),
    ) { r, gastos, ultimas, cats, orcs ->
        val mapa = cats.associateBy { it.id }
        InicioUi(
            resumo = r,
            gastos = gastos.map { g ->
                val c = g.categoriaId?.let { mapa[it] }
                GastoUi(c?.nome ?: "Sem categoria", c?.cor ?: 0xFF90A4AE, g.total)
            },
            ultimas = ultimas.map { it.item(mapa) },
            alertas = orcs.mapNotNull { o ->
                val gasto = gastos.firstOrNull { it.categoriaId == o.categoriaId }?.total ?: 0L
                val nome = mapa[o.categoriaId]?.nome ?: return@mapNotNull null
                when (statusOrcamento(gasto, o.limite)) {
                    StatusOrcamento.ESTOURADO -> "$nome: acima do limite (${Fmt.moeda(gasto)} de ${Fmt.moeda(o.limite)})"
                    StatusOrcamento.ATENCAO -> "$nome: ${gasto * 100 / o.limite}% do limite usado"
                    StatusOrcamento.OK -> null
                }
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InicioUi())
}
