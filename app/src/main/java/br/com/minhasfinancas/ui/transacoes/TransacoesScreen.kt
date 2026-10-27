package br.com.minhasfinancas.ui.transacoes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.minhasfinancas.data.local.entity.TipoTransacao
import br.com.minhasfinancas.ui.components.ChipsOpcoes
import br.com.minhasfinancas.ui.components.EstadoVazio
import br.com.minhasfinancas.ui.components.SeletorMes
import br.com.minhasfinancas.ui.components.TransacaoLinha
import br.com.minhasfinancas.util.Fmt

@Composable
fun TransacoesScreen(onAbrir: (Long) -> Unit, vm: TransacoesViewModel = hiltViewModel()) {
    val itens by vm.itens.collectAsStateWithLifecycle()
    val mes by vm.mes.collectAsStateWithLifecycle()
    val tipo by vm.tipo.collectAsStateWithLifecycle()
    val categoria by vm.categoria.collectAsStateWithLifecycle()
    val conta by vm.conta.collectAsStateWithLifecycle()
    val categorias by vm.categorias.collectAsStateWithLifecycle()
    val contas by vm.contas.collectAsStateWithLifecycle()

    val receitas = itens.filter { it.t.tipo == TipoTransacao.RECEITA }.sumOf { it.t.valor }
    val despesas = itens.filter { it.t.tipo == TipoTransacao.DESPESA }.sumOf { it.t.valor }

    Column(Modifier.fillMaxSize()) {
        Text("Transações", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        SeletorMes(mes) { vm.mes.value = it }
        Spacer(Modifier.height(8.dp))
        ChipsOpcoes(
            listOf("Todas" to null, "Receitas" to TipoTransacao.RECEITA, "Despesas" to TipoTransacao.DESPESA),
            tipo,
        ) { vm.tipo.value = it }
        ChipsOpcoes(listOf<Pair<String, Long?>>("Todas as categorias" to null) + categorias.map { it.nome to it.id }, categoria) { vm.categoria.value = it }
        ChipsOpcoes(listOf<Pair<String, Long?>>("Todas as contas" to null) + contas.map { it.nome to it.id }, conta) { vm.conta.value = it }
        Text(
            "Receitas ${Fmt.moeda(receitas)}  ·  Despesas ${Fmt.moeda(despesas)}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(16.dp),
        )
        if (itens.isEmpty()) {
            EstadoVazio("Nenhuma transação neste filtro. Toque em + para lançar.")
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp)) {
                items(itens, key = { it.t.id }) { i ->
                    TransacaoLinha(i, onClick = { onAbrir(i.t.id) }, onPaga = { vm.alternarPaga(i.t) })
                    HorizontalDivider()
                }
            }
        }
    }
}
