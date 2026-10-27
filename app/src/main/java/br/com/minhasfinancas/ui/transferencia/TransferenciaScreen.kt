package br.com.minhasfinancas.ui.transferencia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.minhasfinancas.ui.components.CampoValor
import br.com.minhasfinancas.ui.components.ChipsOpcoes
import br.com.minhasfinancas.ui.components.DialogData
import br.com.minhasfinancas.util.Fmt

@Composable
fun TransferenciaScreen(onFechar: () -> Unit, vm: TransferenciaViewModel = hiltViewModel()) {
    val e = vm.estado
    val contas by vm.contas.collectAsStateWithLifecycle()
    var mostrarData by remember { mutableStateOf(false) }
    val h = Modifier.padding(horizontal = 16.dp)
    val opcoes = contas.map { it.nome to it.id }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(h.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Transferir entre contas", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onFechar) { Text("Cancelar") }
        }
        Text("De", h, style = MaterialTheme.typography.labelLarge)
        ChipsOpcoes(opcoes, e.origem) { c -> vm.editar { copy(origem = c) } }
        Text("Para", h, style = MaterialTheme.typography.labelLarge)
        ChipsOpcoes(opcoes, e.destino) { c -> vm.editar { copy(destino = c) } }
        CampoValor(e.centavos, { v -> vm.editar { copy(centavos = v) } }, h)
        OutlinedButton(onClick = { mostrarData = true }, modifier = h.fillMaxWidth()) { Text("Data: ${Fmt.data(e.data)}") }
        OutlinedTextField(
            value = e.descricao, onValueChange = { d -> vm.editar { copy(descricao = d) } },
            label = { Text("Descrição (opcional)") }, singleLine = true, modifier = h.fillMaxWidth(),
        )
        e.erro?.let { Text(it, h, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { vm.salvar(onFechar) }, modifier = h.fillMaxWidth()) { Text("Transferir") }
    }
    if (mostrarData) DialogData(e.data, onEscolher = { d -> vm.editar { copy(data = d) } }, onFechar = { mostrarData = false })
}
