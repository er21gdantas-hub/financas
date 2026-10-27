package br.com.minhasfinancas.ui.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.minhasfinancas.ui.components.EstadoVazio
import br.com.minhasfinancas.ui.components.GraficoRosca
import br.com.minhasfinancas.ui.components.TransacaoLinha
import br.com.minhasfinancas.util.Fmt

@Composable
fun InicioScreen(onAbrir: (Long) -> Unit, vm: InicioViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Saldo total", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(Fmt.moeda(ui.resumo.saldoTotal), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Resumo("Receitas do mês", ui.resumo.receitas, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                Resumo("Despesas do mês", ui.resumo.despesas, MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
        }
        if (ui.alertas.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Atenção ao orçamento", style = MaterialTheme.typography.titleSmall)
                        ui.alertas.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Gastos por categoria", style = MaterialTheme.typography.titleMedium)
                    if (ui.gastos.isEmpty()) {
                        EstadoVazio("Sem despesas neste mês ainda.")
                    } else {
                        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            GraficoRosca(ui.gastos.map { it.valor to Color(it.cor) })
                            Column(Modifier.padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ui.gastos.take(5).forEach { g ->
                                    Text("● ${g.nome}  ${Fmt.moeda(g.valor)}", color = Color(g.cor), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Text("Últimas transações", style = MaterialTheme.typography.titleMedium) }
        if (ui.ultimas.isEmpty()) {
            item { EstadoVazio("Nada por aqui ainda. Toque em + para lançar sua primeira transação.") }
        } else {
            items(ui.ultimas, key = { it.t.id }) { TransacaoLinha(it, onClick = { onAbrir(it.t.id) }) }
        }
    }
}

@Composable
private fun Resumo(titulo: String, valor: Long, cor: Color, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(Fmt.moeda(valor), color = cor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
