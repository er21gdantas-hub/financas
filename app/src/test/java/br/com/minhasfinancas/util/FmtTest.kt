package br.com.minhasfinancas.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class FmtTest {
    /** Algumas versões do Java usam espaço "não quebrável" depois do R$; normalizamos para comparar. */
    private fun limpo(s: String) = s.replace('\u00A0', ' ').replace('\u202F', ' ')

    @Test fun moedaNoPadraoBrasileiro() {
        assertEquals("R$ 1.234,56", limpo(Fmt.moeda(123_456)))
        assertEquals("R$ 0,05", limpo(Fmt.moeda(5)))
    }

    @Test fun dataEMesEmPortugues() {
        assertEquals("27/10/2026", Fmt.data(LocalDate.of(2026, 10, 27)))
        assertEquals("Outubro de 2026", Fmt.mes(YearMonth.of(2026, 10)))
    }
}
