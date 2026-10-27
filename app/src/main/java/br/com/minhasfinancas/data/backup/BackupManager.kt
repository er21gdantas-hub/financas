package br.com.minhasfinancas.data.backup

import androidx.room.withTransaction
import br.com.minhasfinancas.data.local.AppDatabase
import br.com.minhasfinancas.data.local.entity.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** Erro com mensagem pronta para mostrar ao usuário. */
class BackupInvalido(mensagem: String) : Exception(mensagem)

data class DadosBackup(
    val contas: List<ContaEntity>,
    val categorias: List<CategoriaEntity>,
    val transacoes: List<TransacaoEntity>,
    val orcamentos: List<OrcamentoEntity>,
    val metas: List<MetaEntity>,
)

private fun JSONObject.longOuNulo(k: String): Long? = if (has(k) && !isNull(k)) getLong(k) else null
private fun JSONObject.intOuNulo(k: String): Int? = if (has(k) && !isNull(k)) getInt(k) else null
private fun JSONObject.textoOuNulo(k: String): String? = if (has(k) && !isNull(k)) getString(k) else null
private fun <T> JSONArray.mapear(f: (JSONObject) -> T): List<T> = (0 until length()).map { f(getJSONObject(it)) }

/** Converte dados <-> JSON e valida o arquivo. Não depende de Android/banco, então é testável na JVM. */
object BackupParser {
    const val VERSAO = 1
    private const val APP = "MinhasFinancas"

    fun escrever(d: DadosBackup, geradoEm: String = LocalDateTime.now().toString()): String {
        val raiz = JSONObject()
        raiz.put("app", APP)
        raiz.put("versao", VERSAO)
        raiz.put("geradoEm", geradoEm)
        raiz.put("contas", JSONArray().also { a ->
            d.contas.forEach { c ->
                a.put(JSONObject().put("id", c.id).put("nome", c.nome).put("tipo", c.tipo.name)
                    .put("saldoInicial", c.saldoInicial).put("cor", c.cor).put("arquivada", c.arquivada))
            }
        })
        raiz.put("categorias", JSONArray().also { a ->
            d.categorias.forEach { c ->
                a.put(JSONObject().put("id", c.id).put("nome", c.nome).put("tipo", c.tipo.name)
                    .put("icone", c.icone).put("cor", c.cor))
            }
        })
        raiz.put("transacoes", JSONArray().also { a ->
            d.transacoes.forEach { t ->
                a.put(JSONObject().put("id", t.id).put("valor", t.valor).put("tipo", t.tipo.name)
                    .put("categoriaId", t.categoriaId ?: JSONObject.NULL).put("contaId", t.contaId)
                    .put("data", t.data).put("descricao", t.descricao).put("forma", t.forma.name)
                    .put("paga", t.paga).put("grupoId", t.grupoId ?: JSONObject.NULL)
                    .put("parcela", t.parcela ?: JSONObject.NULL).put("totalParcelas", t.totalParcelas ?: JSONObject.NULL)
                    .put("transferenciaId", t.transferenciaId ?: JSONObject.NULL))
            }
        })
        raiz.put("orcamentos", JSONArray().also { a ->
            d.orcamentos.forEach { o -> a.put(JSONObject().put("categoriaId", o.categoriaId).put("limite", o.limite)) }
        })
        raiz.put("metas", JSONArray().also { a ->
            d.metas.forEach { m ->
                a.put(JSONObject().put("id", m.id).put("nome", m.nome).put("valorAlvo", m.valorAlvo)
                    .put("valorGuardado", m.valorGuardado).put("prazo", m.prazo ?: JSONObject.NULL))
            }
        })
        return raiz.toString(2)
    }

    /** Lê e valida tudo. Lança [BackupInvalido] com mensagem clara se algo estiver errado. */
    fun ler(texto: String): DadosBackup {
        val dados: DadosBackup
        try {
            val raiz = JSONObject(texto)
            if (raiz.optString("app") != APP) throw BackupInvalido("Este arquivo não é um backup do Minhas Finanças.")
            if (raiz.optInt("versao", 0) !in 1..VERSAO) throw BackupInvalido("Versão de backup não suportada. Atualize o app.")
            dados = DadosBackup(
                contas = raiz.getJSONArray("contas").mapear {
                    ContaEntity(it.getLong("id"), it.getString("nome"), enumValueOf<TipoConta>(it.getString("tipo")),
                        it.getLong("saldoInicial"), it.getLong("cor"), it.getBoolean("arquivada"))
                },
                categorias = raiz.getJSONArray("categorias").mapear {
                    CategoriaEntity(it.getLong("id"), it.getString("nome"), enumValueOf<TipoCategoria>(it.getString("tipo")),
                        it.getString("icone"), it.getLong("cor"))
                },
                transacoes = raiz.getJSONArray("transacoes").mapear {
                    TransacaoEntity(
                        id = it.getLong("id"), valor = it.getLong("valor"), tipo = enumValueOf<TipoTransacao>(it.getString("tipo")),
                        categoriaId = it.longOuNulo("categoriaId"), contaId = it.getLong("contaId"), data = it.getLong("data"),
                        descricao = it.optString("descricao", ""), forma = enumValueOf<FormaPagamento>(it.getString("forma")),
                        paga = it.getBoolean("paga"), grupoId = it.textoOuNulo("grupoId"), parcela = it.intOuNulo("parcela"),
                        totalParcelas = it.intOuNulo("totalParcelas"), transferenciaId = it.textoOuNulo("transferenciaId"),
                    )
                },
                orcamentos = raiz.getJSONArray("orcamentos").mapear { OrcamentoEntity(it.getLong("categoriaId"), it.getLong("limite")) },
                metas = raiz.getJSONArray("metas").mapear {
                    MetaEntity(it.getLong("id"), it.getString("nome"), it.getLong("valorAlvo"), it.getLong("valorGuardado"), it.longOuNulo("prazo"))
                },
            )
        } catch (e: BackupInvalido) {
            throw e
        } catch (e: Exception) {
            throw BackupInvalido("Arquivo de backup inválido ou corrompido.")
        }
        validar(dados)
        return dados
    }

    /** Integridade: ids únicos, valores positivos e referências existentes. */
    private fun validar(d: DadosBackup) {
        val idsConta = d.contas.map { it.id }.toSet()
        val idsCategoria = d.categorias.map { it.id }.toSet()
        if (idsConta.size != d.contas.size || idsCategoria.size != d.categorias.size ||
            d.transacoes.map { it.id }.toSet().size != d.transacoes.size
        ) throw BackupInvalido("Backup com identificadores repetidos.")
        if (d.transacoes.any { it.valor <= 0 || it.contaId !in idsConta || (it.categoriaId != null && it.categoriaId !in idsCategoria) })
            throw BackupInvalido("Backup com lançamentos inconsistentes (valor inválido ou conta/categoria inexistente).")
        if (d.orcamentos.any { it.categoriaId !in idsCategoria })
            throw BackupInvalido("Backup com orçamento de categoria inexistente.")
    }
}

/** Liga o parser ao banco: lê todas as tabelas para gerar e grava tudo numa transação ao restaurar. */
@Singleton
class BackupManager @Inject constructor(private val db: AppDatabase) {
    suspend fun gerar(): String = BackupParser.escrever(
        DadosBackup(
            db.contaDao().listar(), db.categoriaDao().listar(), db.transacaoDao().listar(),
            db.orcamentoDao().listar(), db.metaDao().listar(),
        )
    )

    /** Valida o arquivo ANTES de tocar no banco; depois troca tudo de uma vez (ou nada). */
    suspend fun restaurar(texto: String) {
        val d = BackupParser.ler(texto)
        db.withTransaction {
            db.transacaoDao().limpar()
            db.orcamentoDao().limpar()
            db.metaDao().limpar()
            db.categoriaDao().limpar()
            db.contaDao().limpar()
            db.contaDao().inserirTodas(d.contas)
            db.categoriaDao().inserirTodas(d.categorias)
            db.transacaoDao().inserirTodas(d.transacoes)
            db.orcamentoDao().inserirTodos(d.orcamentos)
            db.metaDao().inserirTodas(d.metas)
        }
    }
}
