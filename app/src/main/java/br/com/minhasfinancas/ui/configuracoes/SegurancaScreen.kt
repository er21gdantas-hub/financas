package br.com.minhasfinancas.ui.configuracoes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.minhasfinancas.ui.seguranca.biometriaDisponivel

@Composable
fun SegurancaScreen(onVoltar: () -> Unit, vm: SegurancaViewModel = hiltViewModel()) {
    val cfg by vm.config.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var definindo by remember { mutableStateOf(false) }
    var removendo by remember { mutableStateOf(false) }
    val temBiometria = remember { biometriaDisponivel(ctx) }

    Column(Modifier.fillMaxSize().padding(vertical = 16.dp)) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Segurança", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onVoltar) { Text("Voltar") }
        }
        ListItem(
            headlineContent = { Text("PIN de acesso") },
            supportingContent = { Text(if (cfg.pinAtivo) "Ativado" else "Desativado") },
            trailingContent = { OutlinedButton(onClick = { definindo = true }) { Text(if (cfg.pinAtivo) "Alterar" else "Definir") } },
        )
        if (cfg.pinAtivo) {
            ListItem(
                headlineContent = { Text("Remover PIN") },
                trailingContent = { TextButton(onClick = { removendo = true }) { Text("Remover", color = MaterialTheme.colorScheme.error) } },
            )
        }
        ListItem(
            headlineContent = { Text("Desbloquear com biometria") },
            supportingContent = {
                Text(when {
                    !cfg.pinAtivo -> "Defina um PIN primeiro"
                    !temBiometria -> "Biometria não disponível ou não cadastrada neste aparelho"
                    else -> "Digital ou rosto, com o PIN como alternativa"
                })
            },
            trailingContent = {
                Switch(checked = cfg.biometriaAtiva, enabled = cfg.pinAtivo && temBiometria, onCheckedChange = vm::definirBiometria)
            },
        )
        Text(
            "O app bloqueia ao abrir e depois de 1 minuto fora dele. O PIN impede o acesso ao app, mas os dados guardados no aparelho não são criptografados: mantenha também o bloqueio de tela do celular ativo. Se esquecer o PIN, será preciso limpar os dados do app e restaurar um backup.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
    }

    if (definindo) {
        var pin by remember { mutableStateOf("") }
        var confirma by remember { mutableStateOf("") }
        val valido = pin.length in 4..6 && pin == confirma
        AlertDialog(
            onDismissRequest = { definindo = false },
            title = { Text(if (cfg.pinAtivo) "Novo PIN" else "Definir PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val teclado = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(6) }, label = { Text("PIN (4 a 6 dígitos)") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(), keyboardOptions = teclado)
                    OutlinedTextField(confirma, { confirma = it.filter(Char::isDigit).take(6) }, label = { Text("Repita o PIN") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(), keyboardOptions = teclado,
                        isError = confirma.isNotEmpty() && pin != confirma)
                }
            },
            confirmButton = { TextButton(enabled = valido, onClick = { vm.definirPin(pin); definindo = false }) { Text("Salvar") } },
            dismissButton = { TextButton(onClick = { definindo = false }) { Text("Cancelar") } },
        )
    }
    if (removendo) {
        AlertDialog(
            onDismissRequest = { removendo = false },
            title = { Text("Remover o PIN?") },
            text = { Text("O app deixará de pedir PIN e biometria ao abrir.") },
            confirmButton = { TextButton(onClick = { vm.removerPin(); removendo = false }) { Text("Remover") } },
            dismissButton = { TextButton(onClick = { removendo = false }) { Text("Cancelar") } },
        )
    }
}
