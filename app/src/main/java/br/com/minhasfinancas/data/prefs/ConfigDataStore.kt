package br.com.minhasfinancas.data.prefs

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "config")

data class Config(val onboardingConcluido: Boolean, val pinAtivo: Boolean, val biometriaAtiva: Boolean)

/** Preferências locais. O PIN nunca é guardado: só um hash PBKDF2 com sal aleatório. */
@Singleton
class ConfigDataStore @Inject constructor(@ApplicationContext private val ctx: Context) {
    private object Chaves {
        val onboarding = booleanPreferencesKey("onboarding")
        val pinHash = stringPreferencesKey("pin_hash")
        val pinSal = stringPreferencesKey("pin_sal")
        val biometria = booleanPreferencesKey("biometria")
    }

    val config: Flow<Config> = ctx.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { p: Preferences ->
            val pin = p[Chaves.pinHash] != null
            Config(p[Chaves.onboarding] == true, pin, pin && p[Chaves.biometria] == true)
        }

    suspend fun concluirOnboarding() { ctx.dataStore.edit { it[Chaves.onboarding] = true } }

    suspend fun definirPin(pin: String) {
        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val h = withContext(Dispatchers.Default) { hash(pin, sal) }
        ctx.dataStore.edit {
            it[Chaves.pinSal] = Base64.encodeToString(sal, Base64.NO_WRAP)
            it[Chaves.pinHash] = Base64.encodeToString(h, Base64.NO_WRAP)
        }
    }

    suspend fun removerPin() {
        ctx.dataStore.edit { it.remove(Chaves.pinHash); it.remove(Chaves.pinSal); it[Chaves.biometria] = false }
    }

    suspend fun definirBiometria(ativa: Boolean) { ctx.dataStore.edit { it[Chaves.biometria] = ativa } }

    suspend fun verificarPin(pin: String): Boolean {
        val p = ctx.dataStore.data.first()
        val salTxt = p[Chaves.pinSal] ?: return false
        val hashTxt = p[Chaves.pinHash] ?: return false
        return withContext(Dispatchers.Default) {
            val calculado = hash(pin, Base64.decode(salTxt, Base64.NO_WRAP))
            MessageDigest.isEqual(calculado, Base64.decode(hashTxt, Base64.NO_WRAP))
        }
    }

    private fun hash(pin: String, sal: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), sal, 100_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}
