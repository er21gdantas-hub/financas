package br.com.minhasfinancas.domain.model

/** Resumo exibido no Início (valores em centavos). */
data class ResumoMensal(val saldoTotal: Long = 0, val receitas: Long = 0, val despesas: Long = 0)
