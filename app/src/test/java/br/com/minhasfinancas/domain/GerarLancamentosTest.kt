package br.com.minhasfinancas.domain

import br.com.minhasfinancas.data.local.entity.TipoTransacao
import br.com.minhasfinancas.data.local.entity.TransacaoEntity
import br.com.minhasfinancas.domain.usecase.GerarLancamentos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class GerarLancamentosTest {
    private val base = TransacaoEntity(
        valor = 10_000, tipo = TipoTransacao.DESPESA, contaId = 1,
        data = LocalDate.of(2026, 1, 31).toEpochDay(), descricao = "Notebook", paga = true,
    )

    @Test fun aVistaNaoGeraParcelas() {
        val r = GerarLancamentos.repetir(base, 1, sufixo = true)
        assertEquals(1, r.size)
        assertEquals("Notebook", r[0].descricao)
        assertNull(r[0].grupoId)
    }

    @Test fun parcelasTemSufixoDatasMensaisEApenasAPrimeiraPaga() {
        val r = GerarLancamentos.repetir(base, 3, sufixo = true)
        assertEquals(listOf("Notebook (1/3)", "Notebook (2/3)", "Notebook (3/3)"), r.map { it.descricao })
        // 31/01 -> 28/02 (fevereiro de 2026 tem 28 dias) -> 31/03
        assertEquals(
            listOf(LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 31)),
            r.map { LocalDate.ofEpochDay(it.data) },
        )
        assertEquals(listOf(true, false, false), r.map { it.paga })
        assertEquals(listOf(1, 2, 3), r.map { it.parcela })
        assertEquals(3, r.map { it.totalParcelas }.toSet().single())
        assertNotNull(r[0].grupoId)
        assertEquals(1, r.map { it.grupoId }.toSet().size)
        assertEquals(setOf(0L), r.map { it.id }.toSet())
    }

    @Test fun despesaFixaRepeteSemSufixo() {
        val r = GerarLancamentos.repetir(base, 12, sufixo = false)
        assertEquals(12, r.size)
        assertEquals(setOf("Notebook"), r.map { it.descricao }.toSet())
        assertNull(r[0].parcela)
    }
}
