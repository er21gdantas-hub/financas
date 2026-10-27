package br.com.minhasfinancas.ui.configuracoes

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.backup.BackupInvalido
import br.com.minhasfinancas.data.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(private val backup: BackupManager) : ViewModel() {
    val aviso = MutableStateFlow<String?>(null)
    val ocupado = MutableStateFlow(false)

    fun nomeArquivo() = "minhas-financas-backup-${LocalDate.now()}.json"

    fun exportar(uri: Uri, resolver: ContentResolver) {
        viewModelScope.launch {
            ocupado.value = true
            val ok = runCatching {
                val json = backup.gerar()
                withContext(Dispatchers.IO) {
                    resolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) } ?: error("sem saída")
                }
            }.isSuccess
            aviso.value = if (ok) "Backup salvo." else "Não foi possível salvar o backup."
            ocupado.value = false
        }
    }

    fun restaurar(uri: Uri, resolver: ContentResolver) {
        viewModelScope.launch {
            ocupado.value = true
            aviso.value = try {
                val texto = withContext(Dispatchers.IO) {
                    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: error("sem leitura")
                    if (bytes.size > 50_000_000) throw BackupInvalido("Arquivo grande demais para ser um backup.")
                    String(bytes, Charsets.UTF_8)
                }
                backup.restaurar(texto)
                "Backup restaurado com sucesso."
            } catch (e: BackupInvalido) {
                e.message ?: "Backup inválido."
            } catch (e: Exception) {
                "Não foi possível restaurar. Seus dados atuais foram mantidos."
            }
            ocupado.value = false
        }
    }
}
