package br.com.minhasfinancas.ui.seguranca

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

@Composable
fun BloqueioScreen(biometria: Boolean, erro: String?, onPin: (String) -> Unit, onBiometria: () -> Unit) {
    val ctx = LocalContext.current
    var pin by remember { mutableStateOf("") }
    fun biometrico() { ctx.comoFragmentActivity()?.let { pedirBiometria(it, onBiometria) } }
    LaunchedEffect(Unit) { if (biometria) biometrico() }

    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Minhas Finanças", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = pin, onValueChange = { pin = it.filter(Char::isDigit).take(6) },
            label = { Text("PIN") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )
        erro?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(16.dp))
        Button(enabled = pin.length >= 4, onClick = { onPin(pin); pin = "" }) { Text("Entrar") }
        if (biometria) TextButton(onClick = { biometrico() }) { Text("Usar biometria") }
    }
}
