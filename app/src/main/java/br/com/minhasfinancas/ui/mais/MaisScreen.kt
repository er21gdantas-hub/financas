package br.com.minhasfinancas.ui.mais

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.minhasfinancas.navigation.Rotas

@Composable
fun MaisScreen(onIr: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text("Mais", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        ListItem(
            headlineContent = { Text("Contas") },
            supportingContent = { Text("Carteira, banco, Pix, cartão e poupança") },
            modifier = Modifier.clickable { onIr(Rotas.CONTAS) },
        )
        ListItem(
            headlineContent = { Text("Categorias") },
            supportingContent = { Text("Nome, ícone e cor") },
            modifier = Modifier.clickable { onIr(Rotas.CATEGORIAS) },
        )
        ListItem(
            headlineContent = { Text("Orçamento mensal") },
            supportingContent = { Text("Limite de gasto por categoria") },
            modifier = Modifier.clickable { onIr(Rotas.ORCAMENTO) },
        )
        ListItem(
            headlineContent = { Text("Transferir entre contas") },
            supportingContent = { Text("Move dinheiro sem contar como receita ou despesa") },
            modifier = Modifier.clickable { onIr(Rotas.TRANSFERENCIA) },
        )
        ListItem(
            headlineContent = { Text("Backup e restauração") },
            supportingContent = { Text("Salvar e recuperar seus dados em arquivo") },
            modifier = Modifier.clickable { onIr(Rotas.BACKUP) },
        )
        ListItem(
            headlineContent = { Text("Segurança: PIN e biometria") },
            supportingContent = { Text("Bloqueie o app ao abrir") },
            modifier = Modifier.clickable { onIr(Rotas.SEGURANCA) },
        )
    }
}

@Composable
fun EmBreveScreen(titulo: String, texto: String) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(texto, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
