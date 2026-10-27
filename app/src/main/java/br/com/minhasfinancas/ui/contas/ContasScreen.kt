package br.com.minhasfinancas.ui.contas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.minhasfinancas.data.local.entity.ContaEntity
import br.com.minhasfinancas.data.local.entity.TipoConta
import br.com.minhasfinancas.ui.components.CampoValor
import br.com.minhasfinancas.ui.components.ChipsOpcoes
import br.com.minhasfinancas.ui.components.EstadoVazio
import br.com.minhasfinancas.util.Fmt

@Composable
fun ContasScreen(onVoltar: () -> Unit, vm: ContasViewModel = hiltViewModel()) {
    val contas by vm.contas.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<ContaEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Contas", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = { editando = ContaEntity(nome = "", tipo = TipoConta.BANCO) }) { Text("Nova conta") }
            TextButton(onClick = onVoltar) { Text("Voltar") }
        }
        if (contas.isEmpty()) EstadoVazio("Nenhuma conta. Crie uma para começar.")
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
            items(contas, key = { it.conta.id }) { c ->
                Row(Modifier.fillMaxWidth().clickable { editando = c.conta }.padding(vertical = 12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(c.conta.nome)
                        Text(c.conta.tipo.rotulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(Fmt.moeda(c.saldo), color = if (c.saldo < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider()
            }
        }
    }

    editando?.let { conta ->
        var nome by remember(conta) { mutableStateOf(conta.nome) }
        var tipo by remember(conta) { mutableStateOf(conta.tipo) }
        var saldo by remember(conta) { mutableLongStateOf(conta.saldoInicial) }
        AlertDialog(
            onDismissRequest = { editando = null },
            title = { Text(if (conta.id == 0L) "Nova conta" else "Editar conta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, singleLine = true)
                    ChipsOpcoes(TipoConta.entries.map { it.rotulo to it }, tipo) { t -> tipo = t ?: tipo }
                    CampoValor(saldo, { saldo = it }, rotulo = "Saldo inicial")
                    if (conta.id != 0L) Text(
                        "Arquivar esconde a conta das listas, mas o saldo dela continua no saldo total.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(enabled = nome.isNotBlank(), onClick = {
                    vm.salvar(conta.copy(nome = nome.trim(), tipo = tipo, saldoInicial = saldo)); editando = null
                }) { Text("Salvar") }
            },
            dismissButton = {
                Row {
                    if (conta.id != 0L) TextButton(onClick = { vm.arquivar(conta); editando = null }) { Text("Arquivar") }
                    TextButton(onClick = { editando = null }) { Text("Cancelar") }
                }
            },
        )
    }
}
