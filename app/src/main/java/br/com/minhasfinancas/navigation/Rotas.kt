package br.com.minhasfinancas.navigation

object Rotas {
    const val INICIO = "inicio"
    const val TRANSACOES = "transacoes"
    const val RELATORIOS = "relatorios"
    const val METAS = "metas"
    const val MAIS = "mais"
    const val CONTAS = "contas"
    const val CATEGORIAS = "categorias"
    const val ORCAMENTO = "orcamento"
    const val BACKUP = "backup"
    const val SEGURANCA = "seguranca"
    const val TRANSFERENCIA = "transferencia"
    const val FORM = "form/{id}"
    fun form(id: Long = 0) = "form/$id"
}
