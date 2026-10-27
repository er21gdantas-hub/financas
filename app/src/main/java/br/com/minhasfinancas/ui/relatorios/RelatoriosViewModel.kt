package br.com.minhasfinancas.ui.relatorios

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.export.Exportador
import br.com.minhasfinancas.data.repository.Repositorio
import br.com.minhasfinancas.domain.model.Relatorio
import br.com.minhasfinancas.domain.usecase.MontarRelatorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth
import javax.inject.Inject

enum class FormatoExport { CSV, PDF }

@HiltViewModel
class RelatoriosViewModel @Inject constructor(private val repo: Repositorio) : ViewModel() {
    val mes = MutableStateFlow(YearMonth.now())
    val aviso = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val relatorio: StateFlow<Relatorio?> = mes.flatMapLatest { m ->
        val ini = m.minusMonths(5).atDay(1).toEpochDay()
        val fim = m.atEndOfMonth().toEpochDay()
        combine(
            repo.transacoes.doPeriodo(ini, fim),
            repo.transacoes.movimentoPagoAntes(ini),
            repo.contas.saldoInicialTotal(),
            repo.categorias.todas(),
            repo.contas.todasComArquivadas(),
        ) { transacoes, movAntes, saldoInicial, cats, contas ->
            MontarRelatorio.montar(
                m, transacoes, saldoInicial + movAntes,
                cats.associateBy { it.id }, contas.associate { it.id to it.nome },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun nomeArquivo(formato: FormatoExport) =
        "relatorio-${mes.value}.${if (formato == FormatoExport.CSV) "csv" else "pdf"}"

    /** Grava o relatório atual no arquivo que o usuário escolheu (Storage Access Framework). */
    fun exportar(uri: Uri, resolver: ContentResolver, formato: FormatoExport) {
        val r = relatorio.value ?: return
        viewModelScope.launch {
            val ok = runCatching {
                withContext(Dispatchers.IO) {
                    resolver.openOutputStream(uri)?.use { saida ->
                        if (formato == FormatoExport.CSV) saida.write(Exportador.csv(r).toByteArray(Charsets.UTF_8))
                        else Exportador.pdf(r, saida)
                    } ?: error("sem saída")
                }
            }.isSuccess
            aviso.value = if (ok) "Arquivo salvo." else "Não foi possível salvar o arquivo."
        }
    }
}
