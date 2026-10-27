package br.com.minhasfinancas.data

import br.com.minhasfinancas.data.backup.BackupInvalido
import br.com.minhasfinancas.data.backup.BackupParser
import br.com.minhasfinancas.data.backup.DadosBackup
import br.com.minhasfinancas.data.local.entity.*
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupParserTest {
    private fun exemplo(valor: Long = 12_345, contaId: Long = 1, categoriaId: Long? = 1) = DadosBackup(
        contas = listOf(ContaEntity(1, "Carteira", TipoConta.CARTEIRA, 1_000, 0xFF4DB6AC, false)),
        categorias = listOf(CategoriaEntity(1, "Alimentação", TipoCategoria.DESPESA, "🍽️", 0xFFE57373)),
        transacoes = listOf(
            TransacaoEntity(
                id = 1, valor = valor, tipo = TipoTransacao.DESPESA, categoriaId = categoriaId, contaId = contaId,
                data = 20_000, descricao = "Mercado; \"ótimo\"", forma = FormaPagamento.PIX, paga = true,
                grupoId = "g1", parcela = 1, totalParcelas = 3, transferenciaId = null,
            )
        ),
        orcamentos = listOf(OrcamentoEntity(1, 50_000)),
        metas = listOf(MetaEntity(1, "Viagem", 300_000, 20_000, prazo = 21_000)),
    )

    @Test fun escreverELerDevolveOsMesmosDados() {
        val d = exemplo()
        assertEquals(d, BackupParser.ler(BackupParser.escrever(d)))
    }

    @Test fun camposNulosSobrevivemAoVaiEVem() {
        val d = exemplo(categoriaId = null).let { it.copy(metas = it.metas.map { m -> m.copy(prazo = null) }) }
        assertEquals(d, BackupParser.ler(BackupParser.escrever(d)))
    }

    @Test fun rejeitaTextoQueNaoEhJson() {
        assertThrows(BackupInvalido::class.java) { BackupParser.ler("isto não é json") }
    }

    @Test fun rejeitaArquivoDeOutroApp() {
        val texto = JSONObject(BackupParser.escrever(exemplo())).put("app", "Outro").toString()
        val e = assertThrows(BackupInvalido::class.java) { BackupParser.ler(texto) }
        assertTrue(e.message!!.contains("não é um backup"))
    }

    @Test fun rejeitaVersaoFutura() {
        val texto = JSONObject(BackupParser.escrever(exemplo())).put("versao", 99).toString()
        assertThrows(BackupInvalido::class.java) { BackupParser.ler(texto) }
    }

    @Test fun rejeitaValorZeroOuNegativo() {
        assertThrows(BackupInvalido::class.java) { BackupParser.ler(BackupParser.escrever(exemplo(valor = 0))) }
        assertThrows(BackupInvalido::class.java) { BackupParser.ler(BackupParser.escrever(exemplo(valor = -5))) }
    }

    @Test fun rejeitaLancamentoDeContaInexistente() {
        assertThrows(BackupInvalido::class.java) { BackupParser.ler(BackupParser.escrever(exemplo(contaId = 99))) }
    }

    @Test fun rejeitaLancamentoDeCategoriaInexistente() {
        assertThrows(BackupInvalido::class.java) { BackupParser.ler(BackupParser.escrever(exemplo(categoriaId = 99))) }
    }

    @Test fun rejeitaIdsRepetidos() {
        val d = exemplo()
        val repetido = d.copy(contas = d.contas + d.contas)
        assertThrows(BackupInvalido::class.java) { BackupParser.ler(BackupParser.escrever(repetido)) }
    }
}
