package br.com.minhasfinancas.domain.usecase

import br.com.minhasfinancas.data.local.entity.TransacaoEntity
import java.time.LocalDate
import java.util.UUID

/** Regra de negócio: transforma 1 lançamento em N (parcelas ou repetição mensal). */
object GerarLancamentos {
    /**
     * @param n quantidade de meses/parcelas (1 = à vista)
     * @param sufixo true para parcelas: "Compra (2/6)"; false para despesa fixa (sem sufixo)
     * Só o primeiro lançamento mantém "paga"; os demais ficam pendentes.
     */
    fun repetir(base: TransacaoEntity, n: Int, sufixo: Boolean): List<TransacaoEntity> {
        if (n <= 1) return listOf(base.copy(id = 0))
        val grupo = UUID.randomUUID().toString()
        val inicio = LocalDate.ofEpochDay(base.data)
        return (0 until n).map { i ->
            base.copy(
                id = 0,
                data = inicio.plusMonths(i.toLong()).toEpochDay(), // ajusta fim de mês sozinho
                descricao = if (sufixo) "${base.descricao} (${i + 1}/$n)".trim() else base.descricao,
                paga = if (i == 0) base.paga else false,
                grupoId = grupo,
                parcela = if (sufixo) i + 1 else null,
                totalParcelas = if (sufixo) n else null,
            )
        }
    }
}
