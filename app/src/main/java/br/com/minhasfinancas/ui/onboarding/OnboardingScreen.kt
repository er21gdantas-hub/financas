package br.com.minhasfinancas.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Pagina(val emoji: String, val titulo: String, val texto: String)

private val paginas = listOf(
    Pagina("🔒", "Seus dados ficam com você", "O Minhas Finanças funciona sem internet e guarda tudo só no seu celular. Nada é enviado para servidores."),
    Pagina("⚡", "Lance em poucos toques", "Toque no + para registrar uma receita ou despesa. Parcelamentos e despesas fixas geram os meses seguintes automaticamente."),
    Pagina("📊", "Planeje e acompanhe", "Defina limites por categoria, crie metas e veja relatórios do mês. Faça backups com frequência em Mais > Backup."),
)

@Composable
fun OnboardingScreen(onConcluir: () -> Unit) {
    var i by remember { mutableIntStateOf(0) }
    val p = paginas[i]
    val ultima = i == paginas.lastIndex
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onConcluir) { Text("Pular") }
        }
        Spacer(Modifier.weight(1f))
        Text(p.emoji, fontSize = 72.sp)
        Spacer(Modifier.height(24.dp))
        Text(p.titulo, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(p.texto, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            paginas.indices.forEach { k ->
                Text(if (k == i) "●" else "○", color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if (ultima) onConcluir() else i++ }, modifier = Modifier.fillMaxWidth()) {
            Text(if (ultima) "Começar" else "Próximo")
        }
    }
}
