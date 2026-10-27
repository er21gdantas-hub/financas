package br.com.minhasfinancas.ui.metas

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
import br.com.minhasfinancas.data.local.entity.MetaEntity
import br.com.minhasfinancas.ui.components.CampoValor
import br.com.minhasfinancas.ui.components.DialogData
import br.com.minhasfinancas.ui.components.EstadoVazio
import br.com.minhasfinancas.util.Fmt
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun MetasScreen(vm: MetasViewModel = hiltViewModel()) {
    val metas by vm.metas.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<MetaEntity?>(null) }
    var guardando by remember { mutableStateOf<MetaEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Metas", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            Button(onClick = { editando = MetaEntity(nome = "", valorAlvo = 0) }) { Text("Nova meta") }
        }
        if (metas.isEmpty()) EstadoVazio("Nenhuma meta ainda. Crie uma, como Reserva de emergência ou Viagem.")
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(metas, key = { it.id }) { m ->
                val frac = if (m.valorAlvo > 0) (m.valorGuardado.toFloat() / m.valorAlvo).coerceIn(0f, 1f) else 0f
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(m.nome, style = MaterialTheme.typography.titleMedium)
                        Text("${Fmt.moeda(m.valorGuardado)} de ${Fmt.moeda(m.valorAlvo)} (${(frac * 100).toInt()}%)")
                        LinearProgressIndicator(progress = { frac }, modifier = Modifier.fillMaxWidth())
                        m.prazo?.let { p ->
                            val dias = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.ofEpochDay(p))
                            Text(
                                "Prazo: ${Fmt.data(LocalDate.ofEpochDay(p))} · " + if (dias >= 0) "faltam $dias dias" else "prazo vencido",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row {
                            TextButton(onClick = { guardando = m }) { Text("Guardar valor") }
                            TextButton(onClick = { editando = m }) { Text("Editar") }
                        }
                    }
                }
            }
        }
    }

    editando?.let { meta ->
        var nome by remember(meta) { mutableStateOf(meta.nome) }
        var alvo by remember(meta) { mutableLongStateOf(meta.valorAlvo) }
        var guardado by remember(meta) { mutableLongStateOf(meta.valorGuardado) }
        var prazo by remember(meta) { mutableStateOf(meta.prazo?.let { LocalDate.ofEpochDay(it) }) }
        var mostrarData by remember(meta) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { editando = null },
            title = { Text(if (meta.id == 0L) "Nova meta" else "Editar meta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, singleLine = true)
                    CampoValor(alvo, { alvo = it }, rotulo = "Valor-alvo")
                    CampoValor(guardado, { guardado = it }, rotulo = "Já guardado")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { mostrarData = true }) { Text(prazo?.let { "Prazo: ${Fmt.data(it)}" } ?: "Definir prazo") }
                        if (prazo != null) TextButton(onClick = { prazo = null }) { Text("Sem prazo") }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = nome.isNotBlank() && alvo > 0, onClick = {
                    vm.salvar(meta.copy(nome = nome.trim(), valorAlvo = alvo, valorGuardado = guardado, prazo = prazo?.toEpochDay())); editando = null
                }) { Text("Salvar") }
            },
            dismissButton = {
                Row {
                    if (meta.id != 0L) TextButton(onClick = { vm.excluir(meta); editando = null }) { Text("Excluir") }
                    TextButton(onClick = { editando = null }) { Text("Cancelar") }
                }
            },
        )
        if (mostrarData) DialogData(prazo, onEscolher = { prazo = it }, onFechar = { mostrarData = false })
    }

    guardando?.let { meta ->
        var valor by remember(meta) { mutableLongStateOf(0L) }
        AlertDialog(
            onDismissRequest = { guardando = null },
            title = { Text("Guardar em ${meta.nome}") },
            text = { CampoValor(valor, { valor = it }, rotulo = "Valor a guardar") },
            confirmButton = { TextButton(enabled = valor > 0, onClick = { vm.guardar(meta, valor); guardando = null }) { Text("Guardar") } },
            dismissButton = { TextButton(onClick = { guardando = null }) { Text("Cancelar") } },
        )
    }
}
