package br.com.minhasfinancas.ui.categorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.local.entity.CategoriaEntity
import br.com.minhasfinancas.data.repository.Repositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriasViewModel @Inject constructor(private val repo: Repositorio) : ViewModel() {
    val categorias = repo.categorias.todas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun salvar(c: CategoriaEntity) {
        viewModelScope.launch { runCatching { if (c.id == 0L) repo.categorias.inserir(c) else repo.categorias.atualizar(c) } }
    }

    /** Só exclui se nenhuma transação usar a categoria; devolve false quando está em uso. */
    fun excluir(c: CategoriaEntity, resultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val livre = runCatching { repo.categorias.usos(c.id) == 0 }.getOrDefault(false)
            if (livre) runCatching { repo.categorias.excluir(c.id) }
            resultado(livre)
        }
    }
}
