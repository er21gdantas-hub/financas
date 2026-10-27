package br.com.minhasfinancas

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import br.com.minhasfinancas.navigation.Raiz
import br.com.minhasfinancas.ui.theme.MinhasFinancasTheme
import dagger.hilt.android.AndroidEntryPoint

/** FragmentActivity (e não ComponentActivity) porque o BiometricPrompt exige. */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MinhasFinancasTheme { Raiz() } }
    }
}
