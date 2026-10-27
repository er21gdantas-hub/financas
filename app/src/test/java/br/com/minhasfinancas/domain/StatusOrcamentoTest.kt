package br.com.minhasfinancas.domain

import br.com.minhasfinancas.domain.usecase.StatusOrcamento
import br.com.minhasfinancas.domain.usecase.statusOrcamento
import org.junit.Assert.assertEquals
import org.junit.Test

class StatusOrcamentoTest {
    @Test fun abaixoDe80PorCentoEstaOk() = assertEquals(StatusOrcamento.OK, statusOrcamento(7_999, 10_000))
    @Test fun a80PorCentoEhAtencao() = assertEquals(StatusOrcamento.ATENCAO, statusOrcamento(8_000, 10_000))
    @Test fun logoAbaixoDoLimiteAindaEhAtencao() = assertEquals(StatusOrcamento.ATENCAO, statusOrcamento(9_999, 10_000))
    @Test fun noLimiteEstoura() = assertEquals(StatusOrcamento.ESTOURADO, statusOrcamento(10_000, 10_000))
    @Test fun acimaDoLimiteEstoura() = assertEquals(StatusOrcamento.ESTOURADO, statusOrcamento(25_000, 10_000))
    @Test fun semLimiteNuncaAlerta() = assertEquals(StatusOrcamento.OK, statusOrcamento(500, 0))
}
