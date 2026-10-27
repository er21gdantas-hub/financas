package br.com.minhasfinancas.ui.orcamento

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.minhasfinancas.domain.usecase.StatusOrcamento
import br.com.minhasfinancas.domain.usecase.statusOrcamento
import br.com.minhasfinancas.ui.components.CampoValor
import br.com.minhasfinancas.ui.components.SeletorMes
import br.com.minhasfinancas.util.Fmt

@Composable
fun OrcamentoScreen(onVoltar: () -> Unit, vm: OrcamentoViewModel = hiltViewModel()) {
    val linhas by vm.linhas.collectAsStateWithLifecycle()
    val mes by vm.mes.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<LinhaOrcamento?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Orçamento mensal", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onVoltar) { Text("Voltar") }
        }
        SeletorMes(mes) { vm.mes.value = it }
        Text(
            "Toque em uma categoria para definir o limite mensal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
            items(linhas, key = { it.categoria.id }) { l ->
                val limite = l.limite
                val status = if (limite != null) statusOrcamento(l.gasto, limite) else StatusOrcamento.OK
                val cor = when (status) {
                    StatusOrcamento.OK -> MaterialTheme.colorScheme.primary
                    StatusOrcamento.ATENCAO -> Color(0xFFE0A53A)
                    StatusOrcamento.ESTOURADO -> MaterialTheme.colorScheme.error
                }
                Column(Modifier.fillMaxWidth().clickable { editando = l }.padding(vertical = 10.dp)) {
                    Row {
                        Text("${l.categoria.icone} ${l.categoria.nome}", Modifier.weight(1f))
                        Text(if (limite != null) "${Fmt.moeda(l.gasto)} de ${Fmt.moeda(limite)}" else "Sem limite · gasto ${Fmt.moeda(l.gasto)}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (limite != null) {
                        LinearProgressIndicator(
                            progress = { (l.gasto.toFloat() / limite).coerceIn(0f, 1f) },
                            color = cor,
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        )
                        when (status) {
                            StatusOrcamento.ATENCAO -> Text("Perto do limite", color = cor, style = MaterialTheme.typography.labelMedium)
                            StatusOrcamento.ESTOURADO -> Text("Acima do limite em ${Fmt.moeda(l.gasto - limite)}", color = cor, style = MaterialTheme.typography.labelMedium)
                            StatusOrcamento.OK -> {}
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }

    editando?.let { l ->
        var valor by remember(l) { mutableLongStateOf(l.limite ?: 0L) }
        AlertDialog(
            onDismissRequest = { editando = null },
            title = { Text("Limite de ${l.categoria.nome}") },
            text = { CampoValor(valor, { valor = it }, rotulo = "Limite mensal") },
            confirmButton = { TextButton(onClick = { vm.definirLimite(l.categoria.id, valor); editando = null }) { Text("Salvar") } },
            dismissButton = {
                Row {
                    if (l.limite != null) TextButton(onClick = { vm.definirLimite(l.categoria.id, 0); editando = null }) { Text("Remover") }
                    TextButton(onClick = { editando = null }) { Text("Cancelar") }
                }
            },
        )
    }
}
