package br.com.minhasfinancas

import android.app.Application
import br.com.minhasfinancas.data.repository.Repositorio
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Ponto de entrada do app: liga o Hilt e cria contas/categorias padrão na 1ª abertura. */
@HiltAndroidApp
class MinhasFinancasApp : Application() {
    @Inject lateinit var repo: Repositorio

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch { runCatching { repo.semearSeVazio() } }
    }
}
