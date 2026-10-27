package br.com.minhasfinancas.ui.categorias

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.minhasfinancas.data.local.entity.CategoriaEntity
import br.com.minhasfinancas.data.local.entity.TipoCategoria
import br.com.minhasfinancas.ui.components.ChipsOpcoes

private val Paleta = listOf(0xFFE57373, 0xFF64B5F6, 0xFF81C784, 0xFFFFB74D, 0xFFBA68C8, 0xFF4DB6AC, 0xFFA1887F, 0xFF90A4AE)

@Composable
fun CategoriasScreen(onVoltar: () -> Unit, vm: CategoriasViewModel = hiltViewModel()) {
    val categorias by vm.categorias.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<CategoriaEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Categorias", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = { editando = CategoriaEntity(nome = "", tipo = TipoCategoria.DESPESA, icone = "📌", cor = Paleta[0]) }) { Text("Nova") }
            TextButton(onClick = onVoltar) { Text("Voltar") }
        }
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
            items(categorias, key = { it.id }) { c ->
                Row(Modifier.fillMaxWidth().clickable { editando = c }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).background(Color(c.cor).copy(alpha = 0.25f), CircleShape), contentAlignment = Alignment.Center) { Text(c.icone) }
                    Spacer(Modifier.width(12.dp))
                    Text(c.nome, Modifier.weight(1f))
                    Text(c.tipo.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider()
            }
        }
    }

    editando?.let { cat ->
        var nome by remember(cat) { mutableStateOf(cat.nome) }
        var icone by remember(cat) { mutableStateOf(cat.icone) }
        var cor by remember(cat) { mutableLongStateOf(cat.cor) }
        var tipo by remember(cat) { mutableStateOf(cat.tipo) }
        var erro by remember(cat) { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { editando = null },
            title = { Text(if (cat.id == 0L) "Nova categoria" else "Editar categoria") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, singleLine = true)
                    OutlinedTextField(icone, { icone = it.take(2) }, label = { Text("Ícone (emoji)") }, singleLine = true)
                    ChipsOpcoes(
                        listOf("Despesa" to TipoCategoria.DESPESA, "Receita" to TipoCategoria.RECEITA, "Ambas" to TipoCategoria.AMBAS),
                        tipo,
                    ) { t -> tipo = t ?: tipo }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Paleta.forEach { p ->
                            Box(
                                Modifier.size(28.dp).background(Color(p), CircleShape)
                                    .border(if (p == cor) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    .clickable { cor = p },
                            )
                        }
                    }
                    erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(enabled = nome.isNotBlank(), onClick = {
                    vm.salvar(cat.copy(nome = nome.trim(), icone = icone.ifBlank { "📌" }, cor = cor, tipo = tipo)); editando = null
                }) { Text("Salvar") }
            },
            dismissButton = {
                Row {
                    if (cat.id != 0L) TextButton(onClick = {
                        vm.excluir(cat) { ok -> if (ok) editando = null else erro = "Categoria em uso por transações. Edite essas transações antes de excluir." }
                    }) { Text("Excluir") }
                    TextButton(onClick = { editando = null }) { Text("Cancelar") }
                }
            },
        )
    }
}
