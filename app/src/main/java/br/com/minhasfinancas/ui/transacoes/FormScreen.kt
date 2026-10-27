package br.com.minhasfinancas.ui.transacoes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.minhasfinancas.data.local.entity.FormaPagamento
import br.com.minhasfinancas.data.local.entity.TipoCategoria
import br.com.minhasfinancas.data.local.entity.TipoTransacao
import br.com.minhasfinancas.ui.components.CampoValor
import br.com.minhasfinancas.ui.components.ChipsOpcoes
import br.com.minhasfinancas.util.Fmt
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(onFechar: () -> Unit, vm: FormViewModel = hiltViewModel()) {
    val e = vm.estado
    val categorias by vm.categorias.collectAsStateWithLifecycle()
    val contas by vm.contas.collectAsStateWithLifecycle()
    var mostrarData by remember { mutableStateOf(false) }
    val h = Modifier.padding(horizontal = 16.dp)
    val novo = e.id == 0L
    val alvo = if (e.tipo == TipoTransacao.RECEITA) TipoCategoria.RECEITA else TipoCategoria.DESPESA

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(h.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (novo) "Nova transação" else "Editar transação", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onFechar) { Text("Cancelar") }
        }
        ChipsOpcoes(
            listOf("Despesa" to TipoTransacao.DESPESA, "Receita" to TipoTransacao.RECEITA), e.tipo,
        ) { t -> vm.editar { copy(tipo = t ?: tipo, categoriaId = null) } }
        CampoValor(e.centavos, { v -> vm.editar { copy(centavos = v) } }, h)
        Text("Categoria", h, style = MaterialTheme.typography.labelLarge)
        ChipsOpcoes(
            categorias.filter { it.tipo == TipoCategoria.AMBAS || it.tipo == alvo }.map { "${it.icone} ${it.nome}" to it.id },
            e.categoriaId,
        ) { c -> vm.editar { copy(categoriaId = c) } }
        Text("Conta", h, style = MaterialTheme.typography.labelLarge)
        ChipsOpcoes(contas.map { it.nome to it.id }, e.contaId) { c -> vm.editar { copy(contaId = c) } }
        OutlinedButton(onClick = { mostrarData = true }, modifier = h.fillMaxWidth()) { Text("Data: ${Fmt.data(e.data)}") }
        OutlinedTextField(
            value = e.descricao, onValueChange = { d -> vm.editar { copy(descricao = d) } },
            label = { Text("Descrição (opcional)") }, singleLine = true, modifier = h.fillMaxWidth(),
        )
        Text("Forma de pagamento", h, style = MaterialTheme.typography.labelLarge)
        ChipsOpcoes(FormaPagamento.entries.map { it.rotulo to it }, e.forma) { f -> vm.editar { copy(forma = f ?: forma) } }
        Row(h.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (e.tipo == TipoTransacao.RECEITA) "Já recebido" else "Já pago",
                Modifier.weight(1f),
            )
            Switch(checked = e.paga, onCheckedChange = { p -> vm.editar { copy(paga = p) } })
        }
        if (novo) {
            Row(h.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Fixa (repete por 12 meses)", Modifier.weight(1f))
                Switch(checked = e.fixa, onCheckedChange = { f -> vm.editar { copy(fixa = f) } })
            }
            if (!e.fixa) {
                OutlinedTextField(
                    value = e.parcelas.toString(),
                    onValueChange = { s -> vm.editar { copy(parcelas = s.filter { it.isDigit() }.take(2).toIntOrNull() ?: 1) } },
                    label = { Text("Parcelas (valor acima é de cada parcela)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = h.fillMaxWidth(),
                )
            }
        }
        e.erro?.let { Text(it, h, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { vm.salvar(onFechar) }, modifier = h.fillMaxWidth()) { Text("Salvar") }
        if (!novo) {
            TextButton(onClick = { vm.excluir(onFechar) }, modifier = h) {
                Text("Excluir transação", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (mostrarData) {
        val estadoData = rememberDatePickerState(
            initialSelectedDateMillis = e.data.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { mostrarData = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoData.selectedDateMillis?.let { ms ->
                        vm.editar { copy(data = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()) }
                    }
                    mostrarData = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { mostrarData = false }) { Text("Cancelar") } },
        ) { DatePicker(state = estadoData) }
    }
}
