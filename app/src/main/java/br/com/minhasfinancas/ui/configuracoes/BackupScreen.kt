package br.com.minhasfinancas.ui.configuracoes

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BackupScreen(onVoltar: () -> Unit, vm: BackupViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    var confirmar by remember { mutableStateOf(false) }

    val criar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { vm.exportar(it, ctx.contentResolver) }
    }
    val abrir = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.restaurar(it, ctx.contentResolver) }
    }
    LaunchedEffect(aviso) {
        aviso?.let { Toast.makeText(ctx, it, Toast.LENGTH_LONG).show(); vm.aviso.value = null }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Backup e restauração", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onVoltar) { Text("Voltar") }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Salvar backup", style = MaterialTheme.typography.titleMedium)
                Text("Cria um arquivo .json com contas, categorias, transações, orçamentos e metas. Escolha onde guardar, por exemplo no Google Drive ou no seu computador.")
                Button(enabled = !ocupado, onClick = { criar.launch(vm.nomeArquivo()) }) { Text("Salvar backup") }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Restaurar backup", style = MaterialTheme.typography.titleMedium)
                Text("Substitui TODOS os dados atuais pelos do arquivo. O arquivo é conferido antes; se estiver inválido, nada é alterado.")
                OutlinedButton(enabled = !ocupado, onClick = { confirmar = true }) { Text("Restaurar de um arquivo") }
            }
        }
        Text(
            "O arquivo não é criptografado e contém todas as suas finanças. Guarde em local seguro. PIN e biometria não entram no backup.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Substituir os dados atuais?") },
            text = { Text("Tudo o que está no app agora será trocado pelo conteúdo do backup. Se não tiver certeza, salve um backup antes.") },
            confirmButton = { TextButton(onClick = { confirmar = false; abrir.launch(arrayOf("*/*")) }) { Text("Escolher arquivo") } },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } },
        )
    }
}
