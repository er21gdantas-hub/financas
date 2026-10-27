package br.com.minhasfinancas.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.minhasfinancas.ui.onboarding.OnboardingScreen
import br.com.minhasfinancas.ui.seguranca.BloqueioScreen
import br.com.minhasfinancas.ui.seguranca.SessaoViewModel

/** Decide o que mostrar: carregando, onboarding, bloqueio ou o app. */
@Composable
fun Raiz(vm: SessaoViewModel = hiltViewModel()) {
    val config by vm.config.collectAsStateWithLifecycle()
    val desbloqueado by vm.desbloqueado.collectAsStateWithLifecycle()
    val erro by vm.erro.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.aoParar() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.aoIniciar() }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        val c = config
        when {
            c == null -> Box(Modifier.fillMaxSize())
            !c.onboardingConcluido -> OnboardingScreen(onConcluir = vm::concluirOnboarding)
            c.pinAtivo && !desbloqueado -> BloqueioScreen(c.biometriaAtiva, erro, onPin = vm::tentarPin, onBiometria = vm::desbloquearPorBiometria)
            else -> AppNav()
        }
    }
}
