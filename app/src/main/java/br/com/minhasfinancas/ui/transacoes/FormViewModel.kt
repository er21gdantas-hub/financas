package br.com.minhasfinancas.ui.transacoes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.local.entity.FormaPagamento
import br.com.minhasfinancas.data.local.entity.TipoTransacao
import br.com.minhasfinancas.data.local.entity.TransacaoEntity
import br.com.minhasfinancas.data.repository.Repositorio
import br.com.minhasfinancas.domain.usecase.GerarLancamentos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FormEstado(
    val id: Long = 0,
    val centavos: Long = 0,
    val tipo: TipoTransacao = TipoTransacao.DESPESA,
    val categoriaId: Long? = null,
    val contaId: Long? = null,
    val data: LocalDate = LocalDate.now(),
    val descricao: String = "",
    val forma: FormaPagamento = FormaPagamento.PIX,
    val paga: Boolean = true,
    val parcelas: Int = 1,
    val fixa: Boolean = false,
    val erro: String? = null,
    val original: TransacaoEntity? = null,
)

@HiltViewModel
class FormViewModel @Inject constructor(private val repo: Repositorio, saved: SavedStateHandle) : ViewModel() {
    private val id: Long = saved.get<Long>("id") ?: 0L

    var estado by mutableStateOf(FormEstado())
        private set

    val categorias = repo.categorias.todas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val contas = repo.contas.todas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (id != 0L) {
                repo.transacoes.buscar(id)?.let { t ->
                    estado = FormEstado(
                        id = t.id, centavos = t.valor, tipo = t.tipo, categoriaId = t.categoriaId, contaId = t.contaId,
                        data = LocalDate.ofEpochDay(t.data), descricao = t.descricao, forma = t.forma, paga = t.paga, original = t,
                    )
                }
            } else {
                // Aguarda a criação das contas padrão na primeira abertura.
                val primeira = repo.contas.todas().first { it.isNotEmpty() }.first()
                estado = estado.copy(contaId = primeira.id)
            }
        }
    }

    fun editar(bloco: FormEstado.() -> FormEstado) {
        estado = estado.bloco().copy(erro = null)
    }

    fun salvar(aoConcluir: () -> Unit) {
        val e = estado
        if (e.centavos <= 0) { estado = e.copy(erro = "Informe um valor maior que zero"); return }
        val conta = e.contaId
        if (conta == null) { estado = e.copy(erro = "Escolha uma conta"); return }
        viewModelScope.launch {
            runCatching {
                val base = TransacaoEntity(
                    id = e.id, valor = e.centavos, tipo = e.tipo, categoriaId = e.categoriaId, contaId = conta,
                    data = e.data.toEpochDay(), descricao = e.descricao.trim(), forma = e.forma, paga = e.paga,
                )
                if (e.id == 0L) {
                    val n = if (e.fixa) 12 else e.parcelas.coerceIn(1, 60)
                    repo.transacoes.inserirTodas(GerarLancamentos.repetir(base, n, sufixo = !e.fixa))
                } else {
                    val o = e.original
                    repo.transacoes.atualizar(
                        base.copy(grupoId = o?.grupoId, parcela = o?.parcela, totalParcelas = o?.totalParcelas, transferenciaId = o?.transferenciaId)
                    )
                }
            }.onSuccess { aoConcluir() }
                .onFailure { estado = estado.copy(erro = "Não foi possível salvar. Tente novamente.") }
        }
    }

    fun excluir(aoConcluir: () -> Unit) {
        viewModelScope.launch {
            runCatching { repo.transacoes.excluir(id) }
                .onSuccess { aoConcluir() }
                .onFailure { estado = estado.copy(erro = "Não foi possível excluir.") }
        }
    }
}
