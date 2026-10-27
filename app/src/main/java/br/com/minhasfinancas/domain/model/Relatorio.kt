package br.com.minhasfinancas.domain.model

import br.com.minhasfinancas.data.local.entity.TipoTransacao
import java.time.LocalDate
import java.time.YearMonth

data class MesResumo(val mes: YearMonth, val receitas: Long, val despesas: Long, val saldoFinal: Long)

data class TotalCategoria(val nome: String, val icone: String, val cor: Long, val valor: Long, val percentual: Int)

/** Uma linha de lançamento já com nomes (categoria e conta) resolvidos. */
data class LinhaRelatorio(
    val data: LocalDate,
    val tipo: TipoTransacao,
    val categoria: String,
    val conta: String,
    val descricao: String,
    val valor: Long,
    val paga: Boolean,
)

/** Tudo o que a tela de relatórios e as exportações (CSV/PDF) precisam. Valores em centavos. */
data class Relatorio(
    val mes: YearMonth,
    val receitas: Long,
    val despesas: Long,
    val saldoFinal: Long,
    val meses: List<MesResumo>,            // últimos 6 meses, o selecionado por último
    val categorias: List<TotalCategoria>,  // despesas do mês por categoria
    val maiores: List<LinhaRelatorio>,     // 5 maiores despesas do mês
    val lancamentos: List<LinhaRelatorio>, // todos os lançamentos do mês
)
