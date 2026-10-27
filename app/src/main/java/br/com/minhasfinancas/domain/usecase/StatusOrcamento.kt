package br.com.minhasfinancas.domain.usecase

enum class StatusOrcamento { OK, ATENCAO, ESTOURADO }

/** Regra: a partir de 80% do limite é atenção; a partir de 100% estourou. */
fun statusOrcamento(gasto: Long, limite: Long): StatusOrcamento {
    if (limite <= 0) return StatusOrcamento.OK
    val percentual = gasto * 100 / limite
    return when {
        percentual >= 100 -> StatusOrcamento.ESTOURADO
        percentual >= 80 -> StatusOrcamento.ATENCAO
        else -> StatusOrcamento.OK
    }
}
