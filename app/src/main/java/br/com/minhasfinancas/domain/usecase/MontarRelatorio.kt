package br.com.minhasfinancas.domain.usecase

import br.com.minhasfinancas.data.local.entity.CategoriaEntity
import br.com.minhasfinancas.data.local.entity.TipoTransacao
import br.com.minhasfinancas.data.local.entity.TransacaoEntity
import br.com.minhasfinancas.domain.model.LinhaRelatorio
import br.com.minhasfinancas.domain.model.MesResumo
import br.com.minhasfinancas.domain.model.Relatorio
import br.com.minhasfinancas.domain.model.TotalCategoria
import java.time.LocalDate
import java.time.YearMonth

/** Função pura (sem Android): fácil de testar. Recebe os dados e devolve o relatório pronto. */
object MontarRelatorio {
    private fun movimento(t: TransacaoEntity): Long = when (t.tipo) {
        TipoTransacao.RECEITA, TipoTransacao.TRANSFERENCIA_ENTRADA -> t.valor
        TipoTransacao.DESPESA, TipoTransacao.TRANSFERENCIA_SAIDA -> -t.valor
    }

    /**
     * @param transacoes lançamentos dos 6 meses que terminam em [mes]
     * @param saldoAntes saldo (saldos iniciais + movimentos pagos) imediatamente antes do 1º dos 6 meses
     */
    fun montar(
        mes: YearMonth,
        transacoes: List<TransacaoEntity>,
        saldoAntes: Long,
        categorias: Map<Long, CategoriaEntity>,
        contas: Map<Long, String>,
    ): Relatorio {
        val porMes = transacoes.groupBy { YearMonth.from(LocalDate.ofEpochDay(it.data)) }
        var saldo = saldoAntes
        val meses = (5 downTo 0).map { k ->
            val m = mes.minusMonths(k.toLong())
            val l = porMes[m].orEmpty()
            saldo += l.filter { it.paga }.sumOf { movimento(it) }
            MesResumo(
                m,
                l.filter { it.tipo == TipoTransacao.RECEITA }.sumOf { it.valor },
                l.filter { it.tipo == TipoTransacao.DESPESA }.sumOf { it.valor },
                saldo,
            )
        }
        val doMes = porMes[mes].orEmpty().sortedBy { it.data }

        fun linha(t: TransacaoEntity): LinhaRelatorio {
            val transf = t.tipo == TipoTransacao.TRANSFERENCIA_ENTRADA || t.tipo == TipoTransacao.TRANSFERENCIA_SAIDA
            return LinhaRelatorio(
                LocalDate.ofEpochDay(t.data), t.tipo,
                t.categoriaId?.let { categorias[it]?.nome } ?: if (transf) "Transferência" else "Sem categoria",
                contas[t.contaId] ?: "—", t.descricao, t.valor, t.paga,
            )
        }

        val despesasMes = doMes.filter { it.tipo == TipoTransacao.DESPESA }
        val totalDespesas = despesasMes.sumOf { it.valor }
        val porCategoria = despesasMes.groupBy { it.categoriaId }.map { (id, l) ->
            val v = l.sumOf { it.valor }
            val c = id?.let { categorias[it] }
            TotalCategoria(c?.nome ?: "Sem categoria", c?.icone ?: "❔", c?.cor ?: 0xFF90A4AE, v,
                if (totalDespesas > 0) (v * 100 / totalDespesas).toInt() else 0)
        }.sortedByDescending { it.valor }

        val atual = meses.last()
        return Relatorio(
            mes = mes,
            receitas = atual.receitas,
            despesas = atual.despesas,
            saldoFinal = atual.saldoFinal,
            meses = meses,
            categorias = porCategoria,
            maiores = despesasMes.sortedByDescending { it.valor }.take(5).map(::linha),
            lancamentos = doMes.map(::linha),
        )
    }
}
