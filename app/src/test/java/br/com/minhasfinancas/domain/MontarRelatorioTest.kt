package br.com.minhasfinancas.domain

import br.com.minhasfinancas.data.local.entity.CategoriaEntity
import br.com.minhasfinancas.data.local.entity.TipoCategoria
import br.com.minhasfinancas.data.local.entity.TipoTransacao
import br.com.minhasfinancas.data.local.entity.TransacaoEntity
import br.com.minhasfinancas.domain.usecase.MontarRelatorio
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class MontarRelatorioTest {
    private val outubro = YearMonth.of(2026, 10)
    private val cats = mapOf(
        1L to CategoriaEntity(1, "Alimentação", TipoCategoria.DESPESA, "🍽️", 0xFFE57373),
        2L to CategoriaEntity(2, "Transporte", TipoCategoria.DESPESA, "🚌", 0xFF64B5F6),
        8L to CategoriaEntity(8, "Salário", TipoCategoria.RECEITA, "💰", 0xFF4DB6AC),
    )
    private val contas = mapOf(1L to "Carteira", 2L to "Banco")

    private fun t(id: Long, tipo: TipoTransacao, valor: Long, dia: LocalDate, paga: Boolean = true, cat: Long? = null, conta: Long = 1) =
        TransacaoEntity(id = id, valor = valor, tipo = tipo, categoriaId = cat, contaId = conta, data = dia.toEpochDay(), paga = paga)

    private val dados = listOf(
        t(1, TipoTransacao.RECEITA, 500_000, LocalDate.of(2026, 10, 5), cat = 8),
        t(2, TipoTransacao.DESPESA, 30_000, LocalDate.of(2026, 10, 10), cat = 1),
        t(3, TipoTransacao.DESPESA, 10_000, LocalDate.of(2026, 10, 12), paga = false, cat = 2), // pendente
        t(4, TipoTransacao.TRANSFERENCIA_SAIDA, 20_000, LocalDate.of(2026, 10, 15), conta = 1),
        t(5, TipoTransacao.TRANSFERENCIA_ENTRADA, 20_000, LocalDate.of(2026, 10, 15), conta = 2),
        t(6, TipoTransacao.DESPESA, 5_000, LocalDate.of(2026, 9, 20), cat = 1),
    )

    private val r = MontarRelatorio.montar(outubro, dados, saldoAntes = 10_000, categorias = cats, contas = contas)

    @Test fun devolveSeisMesesTerminandoNoSelecionado() {
        assertEquals(6, r.meses.size)
        assertEquals(YearMonth.of(2026, 5), r.meses.first().mes)
        assertEquals(outubro, r.meses.last().mes)
    }

    @Test fun totaisDoMesIgnoramTransferencias() {
        assertEquals(500_000, r.receitas)
        assertEquals(40_000, r.despesas) // inclui a despesa pendente
    }

    @Test fun saldoConsideraSoOQueFoiPagoETransferenciaNaoAlteraOTotal() {
        assertEquals(10_000, r.meses.first().saldoFinal)          // sem movimento: mantém o saldo anterior
        assertEquals(5_000, r.meses[4].saldoFinal)                 // setembro: 10.000 - 5.000
        assertEquals(475_000, r.saldoFinal)                        // outubro: +500.000 -30.000 (pendente fora; transferência soma zero)
    }

    @Test fun categoriasTemPercentualEOrdemPorValor() {
        assertEquals(listOf("Alimentação", "Transporte"), r.categorias.map { it.nome })
        assertEquals(listOf(75, 25), r.categorias.map { it.percentual })
    }

    @Test fun maioresGastosVemEmOrdemDecrescente() {
        assertEquals(listOf(30_000L, 10_000L), r.maiores.map { it.valor })
    }

    @Test fun lancamentosDoMesEstaoOrdenadosPorDataETransferenciaTemRotulo() {
        assertEquals(5, r.lancamentos.size)
        assertEquals(LocalDate.of(2026, 10, 5), r.lancamentos.first().data)
        assertEquals("Transferência", r.lancamentos.first { it.tipo == TipoTransacao.TRANSFERENCIA_SAIDA }.categoria)
        assertEquals("Banco", r.lancamentos.first { it.tipo == TipoTransacao.TRANSFERENCIA_ENTRADA }.conta)
    }

    @Test fun mesSemMovimentoDevolveZerosSemErro() {
        val vazio = MontarRelatorio.montar(YearMonth.of(2027, 1), emptyList(), 7_000, cats, contas)
        assertEquals(0, vazio.receitas)
        assertEquals(0, vazio.despesas)
        assertEquals(7_000, vazio.saldoFinal)
        assertEquals(emptyList<Any>(), vazio.categorias)
    }
}
