package br.com.minhasfinancas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TipoConta(val rotulo: String) {
    CARTEIRA("Carteira"), BANCO("Banco"), PIX("Pix"), CARTAO_CREDITO("Cartão de crédito"), POUPANCA("Poupança")
}

enum class TipoCategoria { RECEITA, DESPESA, AMBAS }

/** Transferência vira duas linhas (saída e entrada) com o mesmo transferenciaId. */
enum class TipoTransacao { RECEITA, DESPESA, TRANSFERENCIA_SAIDA, TRANSFERENCIA_ENTRADA }

enum class FormaPagamento(val rotulo: String) {
    DINHEIRO("Dinheiro"), PIX("Pix"), DEBITO("Débito"), CREDITO("Crédito"), BOLETO("Boleto"), OUTRO("Outro")
}

@Entity(tableName = "conta")
data class ContaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val tipo: TipoConta,
    val saldoInicial: Long = 0, // centavos
    val cor: Long = 0xFF0F6B5C,
    val arquivada: Boolean = false,
)

@Entity(tableName = "categoria")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val tipo: TipoCategoria,
    val icone: String, // emoji
    val cor: Long,
)

@Entity(tableName = "transacao", indices = [Index("data"), Index("contaId"), Index("categoriaId")])
data class TransacaoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val valor: Long, // centavos, sempre positivo
    val tipo: TipoTransacao,
    val categoriaId: Long? = null,
    val contaId: Long,
    val data: Long, // LocalDate.toEpochDay()
    val descricao: String = "",
    val forma: FormaPagamento = FormaPagamento.OUTRO,
    val paga: Boolean = true,
    val grupoId: String? = null, // liga as parcelas/repetições
    val parcela: Int? = null,
    val totalParcelas: Int? = null,
    val transferenciaId: String? = null,
)

/** Limite mensal de gasto de uma categoria (vale para todos os meses). */
@Entity(tableName = "orcamento")
data class OrcamentoEntity(
    @PrimaryKey val categoriaId: Long,
    val limite: Long, // centavos
)

@Entity(tableName = "meta")
data class MetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val valorAlvo: Long,
    val valorGuardado: Long = 0,
    val prazo: Long? = null, // LocalDate.toEpochDay()
)
