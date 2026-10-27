package br.com.minhasfinancas.data.repository

import androidx.room.withTransaction
import br.com.minhasfinancas.data.local.AppDatabase
import br.com.minhasfinancas.data.local.Seed
import br.com.minhasfinancas.data.local.entity.FormaPagamento
import br.com.minhasfinancas.data.local.entity.TipoTransacao
import br.com.minhasfinancas.data.local.entity.TransacaoEntity
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Único ponto de acesso aos dados. Por enquanto é uma classe concreta (simples);
 * se precisar testar com dados falsos, extraia uma interface em domain/repository.
 */
@Singleton
class Repositorio @Inject constructor(private val db: AppDatabase) {
    val contas = db.contaDao()
    val categorias = db.categoriaDao()
    val transacoes = db.transacaoDao()
    val orcamentos = db.orcamentoDao()
    val metas = db.metaDao()

    suspend fun semearSeVazio() {
        if (contas.contar() == 0) contas.inserirTodas(Seed.contas)
        if (categorias.contar() == 0) categorias.inserirTodas(Seed.categorias)
    }

    /** Cria saída e entrada de forma atômica: ou grava as duas, ou nenhuma. */
    suspend fun transferir(origem: Long, destino: Long, valor: Long, data: LocalDate, descricao: String) {
        val id = UUID.randomUUID().toString()
        db.withTransaction {
            transacoes.inserirTodas(
                listOf(
                    TransacaoEntity(valor = valor, tipo = TipoTransacao.TRANSFERENCIA_SAIDA, contaId = origem,
                        data = data.toEpochDay(), descricao = descricao, forma = FormaPagamento.OUTRO, transferenciaId = id),
                    TransacaoEntity(valor = valor, tipo = TipoTransacao.TRANSFERENCIA_ENTRADA, contaId = destino,
                        data = data.toEpochDay(), descricao = descricao, forma = FormaPagamento.OUTRO, transferenciaId = id),
                )
            )
        }
    }
}
