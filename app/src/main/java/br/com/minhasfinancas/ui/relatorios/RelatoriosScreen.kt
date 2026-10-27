package br.com.minhasfinancas.ui.relatorios

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.minhasfinancas.domain.model.MesResumo
import br.com.minhasfinancas.ui.components.EstadoVazio
import br.com.minhasfinancas.ui.components.SeletorMes
import br.com.minhasfinancas.util.Fmt

private fun rotuloMes(m: MesResumo) = "%02d/%02d".format(m.mes.monthValue, m.mes.year % 100)

@Composable
fun RelatoriosScreen(vm: RelatoriosViewModel = hiltViewModel()) {
    val rel by vm.relatorio.collectAsStateWithLifecycle()
    val mes by vm.mes.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val resolver = ctx.contentResolver

    val csv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { vm.exportar(it, resolver, FormatoExport.CSV) }
    }
    val pdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { vm.exportar(it, resolver, FormatoExport.PDF) }
    }
    LaunchedEffect(aviso) {
        aviso?.let { Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show(); vm.aviso.value = null }
    }

    Column(Modifier.fillMaxSize()) {
        Text("Relatórios", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        SeletorMes(mes) { vm.mes.value = it }
        val r = rel
        if (r == null) {
            EstadoVazio("Carregando…")
            return@Column
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Kpi("Receitas", Fmt.moeda(r.receitas), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    Kpi("Despesas", Fmt.moeda(r.despesas), MaterialTheme.colorScheme.error, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Kpi("Resultado do mês", Fmt.moeda(r.receitas - r.despesas),
                        if (r.receitas >= r.despesas) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, Modifier.weight(1f))
                    Kpi("Saldo ao fim do mês", Fmt.moeda(r.saldoFinal), MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { csv.launch(vm.nomeArquivo(FormatoExport.CSV)) }) { Text("Exportar CSV") }
                    OutlinedButton(onClick = { pdf.launch(vm.nomeArquivo(FormatoExport.PDF)) }) { Text("Exportar PDF") }
                }
            }
            item {
                Secao("Comparação entre meses") {
                    GraficoBarrasMeses(r.meses)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
                        Legenda("Receitas", MaterialTheme.colorScheme.primary)
                        Legenda("Despesas", MaterialTheme.colorScheme.error)
                    }
                }
            }
            item {
                Secao("Evolução do saldo") {
                    GraficoLinha(r.meses.map { it.saldoFinal })
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        r.meses.forEach { Text(rotuloMes(it), style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
            item {
                Secao("Categorias mais consumidas") {
                    if (r.categorias.isEmpty()) EstadoVazio("Sem despesas neste mês.")
                    r.categorias.forEach { c ->
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Row {
                                Text("${c.icone} ${c.nome}", Modifier.weight(1f))
                                Text("${Fmt.moeda(c.valor)} · ${c.percentual}%", style = MaterialTheme.typography.bodySmall)
                            }
                            LinearProgressIndicator(
                                progress = { c.percentual / 100f }, color = Color(c.cor),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
            item {
                Secao("Maiores gastos") {
                    if (r.maiores.isEmpty()) EstadoVazio("Sem despesas neste mês.")
                    r.maiores.forEach { l ->
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(l.descricao.ifBlank { l.categoria })
                                Text("${l.categoria} · ${Fmt.data(l.data)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(Fmt.moeda(l.valor), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Kpi(titulo: String, valor: String, cor: Color, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, color = cor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Secao(titulo: String, conteudo: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            conteudo()
        }
    }
}

@Composable
private fun Legenda(texto: String, cor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(cor))
        Text("  $texto", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun GraficoBarrasMeses(meses: List<MesResumo>) {
    val maximo = meses.maxOf { maxOf(it.receitas, it.despesas) }.coerceAtLeast(1L)
    val verde = MaterialTheme.colorScheme.primary
    val vermelho = MaterialTheme.colorScheme.error
    Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        meses.forEach { m ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(Modifier.width(12.dp).fillMaxHeight(m.receitas.toFloat() / maximo).background(verde))
                    Box(Modifier.width(12.dp).fillMaxHeight(m.despesas.toFloat() / maximo).background(vermelho))
                }
                Text(rotuloMes(m), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun GraficoLinha(valores: List<Long>) {
    val cor = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(100.dp)) {
        if (valores.size < 2) return@Canvas
        val menor = valores.min()
        val faixa = (valores.max() - menor).coerceAtLeast(1L).toFloat()
        val margem = 12f
        val passo = (size.width - 2 * margem) / (valores.size - 1)
        val pontos = valores.mapIndexed { i, v ->
            Offset(margem + i * passo, size.height * 0.9f - ((v - menor) / faixa) * size.height * 0.8f)
        }
        pontos.zipWithNext().forEach { (a, b) -> drawLine(cor, a, b, strokeWidth = 4f) }
        pontos.forEach { drawCircle(cor, 7f, it) }
    }
}
