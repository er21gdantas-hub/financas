package br.com.minhasfinancas.data.local

import br.com.minhasfinancas.data.local.entity.CategoriaEntity
import br.com.minhasfinancas.data.local.entity.ContaEntity
import br.com.minhasfinancas.data.local.entity.TipoCategoria
import br.com.minhasfinancas.data.local.entity.TipoConta

/** Dados criados na primeira abertura do app. */
object Seed {
    val contas = listOf(
        ContaEntity(nome = "Carteira", tipo = TipoConta.CARTEIRA, cor = 0xFF4DB6AC),
        ContaEntity(nome = "Banco", tipo = TipoConta.BANCO, cor = 0xFF64B5F6),
    )

    val categorias = listOf(
        CategoriaEntity(nome = "Alimentação", tipo = TipoCategoria.DESPESA, icone = "🍽️", cor = 0xFFE57373),
        CategoriaEntity(nome = "Transporte", tipo = TipoCategoria.DESPESA, icone = "🚌", cor = 0xFF64B5F6),
        CategoriaEntity(nome = "Moradia", tipo = TipoCategoria.DESPESA, icone = "🏠", cor = 0xFFA1887F),
        CategoriaEntity(nome = "Saúde", tipo = TipoCategoria.DESPESA, icone = "💊", cor = 0xFF81C784),
        CategoriaEntity(nome = "Lazer", tipo = TipoCategoria.DESPESA, icone = "🎬", cor = 0xFFBA68C8),
        CategoriaEntity(nome = "Educação", tipo = TipoCategoria.DESPESA, icone = "📚", cor = 0xFFFFB74D),
        CategoriaEntity(nome = "Compras", tipo = TipoCategoria.DESPESA, icone = "🛍️", cor = 0xFFF06292),
        CategoriaEntity(nome = "Salário", tipo = TipoCategoria.RECEITA, icone = "💰", cor = 0xFF4DB6AC),
        CategoriaEntity(nome = "Investimentos", tipo = TipoCategoria.RECEITA, icone = "📈", cor = 0xFF7986CB),
        CategoriaEntity(nome = "Outros", tipo = TipoCategoria.AMBAS, icone = "📌", cor = 0xFF90A4AE),
    )
}
