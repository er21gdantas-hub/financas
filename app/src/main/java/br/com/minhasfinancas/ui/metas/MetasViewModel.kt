package br.com.minhasfinancas.ui.metas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.minhasfinancas.data.local.entity.MetaEntity
import br.com.minhasfinancas.data.repository.Repositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MetasViewModel @Inject constructor(private val repo: Repositorio) : ViewModel() {
    val metas = repo.metas.todas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun salvar(m: MetaEntity) {
        viewModelScope.launch { runCatching { if (m.id == 0L) repo.metas.inserir(m) else repo.metas.atualizar(m) } }
    }

    fun guardar(m: MetaEntity, valor: Long) {
        if (valor > 0) salvar(m.copy(valorGuardado = m.valorGuardado + valor))
    }

    fun excluir(m: MetaEntity) {
        viewModelScope.launch { runCatching { repo.metas.excluir(m.id) } }
    }
}
