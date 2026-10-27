package br.com.minhasfinancas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.minhasfinancas.data.local.entity.CategoriaEntity
import br.com.minhasfinancas.data.local.entity.ContaEntity
import br.com.minhasfinancas.data.local.entity.MetaEntity
import br.com.minhasfinancas.data.local.entity.OrcamentoEntity
import br.com.minhasfinancas.data.local.entity.TransacaoEntity
import kotlinx.coroutines.flow.Flow

data class SaldoConta(val contaId: Long, val saldo: Long)
data class GastoCategoria(val categoriaId: Long?, val total: Long)

@Dao
interface ContaDao {
    @Query("SELECT * FROM conta WHERE arquivada = 0 ORDER BY nome")
    fun todas(): Flow<List<ContaEntity>>

    @Query("SELECT * FROM conta")
    suspend fun listar(): List<ContaEntity>

    @Query("DELETE FROM conta")
    suspend fun limpar()

    @Query("SELECT COUNT(*) FROM conta")
    suspend fun contar(): Int

    @Query("SELECT * FROM conta ORDER BY nome")
    fun todasComArquivadas(): Flow<List<ContaEntity>>

    @Query("SELECT COALESCE(SUM(saldoInicial), 0) FROM conta")
    fun saldoInicialTotal(): Flow<Long>

    @Insert suspend fun inserir(conta: ContaEntity): Long
    @Insert suspend fun inserirTodas(contas: List<ContaEntity>)
    @Update suspend fun atualizar(conta: ContaEntity)

    /** Saldo = saldo inicial + movimentos JÁ PAGOS (receitas/entradas somam, despesas/saídas subtraem). */
    @Query(
        """SELECT c.id AS contaId,
            c.saldoInicial + COALESCE(SUM(CASE
                WHEN t.tipo IN ('RECEITA','TRANSFERENCIA_ENTRADA') THEN t.valor
                WHEN t.tipo IN ('DESPESA','TRANSFERENCIA_SAIDA') THEN -t.valor
                ELSE 0 END), 0) AS saldo
        FROM conta c LEFT JOIN transacao t ON t.contaId = c.id AND t.paga = 1
        GROUP BY c.id"""
    )
    fun saldos(): Flow<List<SaldoConta>>
}

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categoria ORDER BY nome")
    fun todas(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categoria")
    suspend fun listar(): List<CategoriaEntity>

    @Query("DELETE FROM categoria")
    suspend fun limpar()

    @Query("SELECT COUNT(*) FROM categoria")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM transacao WHERE categoriaId = :id")
    suspend fun usos(id: Long): Int

    @Insert suspend fun inserir(c: CategoriaEntity): Long
    @Insert suspend fun inserirTodas(l: List<CategoriaEntity>)
    @Update suspend fun atualizar(c: CategoriaEntity)

    @Query("DELETE FROM categoria WHERE id = :id")
    suspend fun excluir(id: Long)
}

@Dao
interface TransacaoDao {
    @Query("SELECT * FROM transacao WHERE data BETWEEN :ini AND :fim ORDER BY data DESC, id DESC")
    fun doPeriodo(ini: Long, fim: Long): Flow<List<TransacaoEntity>>

    @Query("SELECT * FROM transacao ORDER BY data DESC, id DESC LIMIT :n")
    fun ultimas(n: Int): Flow<List<TransacaoEntity>>

    @Query("SELECT COALESCE(SUM(valor), 0) FROM transacao WHERE tipo = :tipo AND data BETWEEN :ini AND :fim")
    fun total(tipo: String, ini: Long, fim: Long): Flow<Long>

    @Query(
        """SELECT categoriaId, SUM(valor) AS total FROM transacao
        WHERE tipo = 'DESPESA' AND data BETWEEN :ini AND :fim
        GROUP BY categoriaId ORDER BY total DESC"""
    )
    fun gastosPorCategoria(ini: Long, fim: Long): Flow<List<GastoCategoria>>

    /** Soma assinada dos movimentos já pagos antes de uma data (base para a evolução do saldo). */
    @Query(
        """SELECT COALESCE(SUM(CASE
            WHEN tipo IN ('RECEITA','TRANSFERENCIA_ENTRADA') THEN valor
            WHEN tipo IN ('DESPESA','TRANSFERENCIA_SAIDA') THEN -valor
            ELSE 0 END), 0) FROM transacao WHERE paga = 1 AND data < :antesDe"""
    )
    fun movimentoPagoAntes(antesDe: Long): Flow<Long>

    @Query("SELECT * FROM transacao")
    suspend fun listar(): List<TransacaoEntity>

    @Query("DELETE FROM transacao")
    suspend fun limpar()

    @Query("SELECT * FROM transacao WHERE id = :id")
    suspend fun buscar(id: Long): TransacaoEntity?

    @Insert suspend fun inserirTodas(l: List<TransacaoEntity>)
    @Update suspend fun atualizar(t: TransacaoEntity)

    @Query("DELETE FROM transacao WHERE id = :id")
    suspend fun excluir(id: Long)
}

@Dao
interface OrcamentoDao {
    @Query("SELECT * FROM orcamento")
    fun todos(): Flow<List<OrcamentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(o: OrcamentoEntity)

    @Query("SELECT * FROM orcamento")
    suspend fun listar(): List<OrcamentoEntity>

    @Query("DELETE FROM orcamento")
    suspend fun limpar()

    @Insert suspend fun inserirTodos(l: List<OrcamentoEntity>)

    @Query("DELETE FROM orcamento WHERE categoriaId = :categoriaId")
    suspend fun remover(categoriaId: Long)
}

@Dao
interface MetaDao {
    @Query("SELECT * FROM meta ORDER BY nome")
    fun todas(): Flow<List<MetaEntity>>

    @Insert suspend fun inserir(m: MetaEntity): Long
    @Update suspend fun atualizar(m: MetaEntity)

    @Query("SELECT * FROM meta")
    suspend fun listar(): List<MetaEntity>

    @Query("DELETE FROM meta")
    suspend fun limpar()

    @Insert suspend fun inserirTodas(l: List<MetaEntity>)

    @Query("DELETE FROM meta WHERE id = :id")
    suspend fun excluir(id: Long)
}
