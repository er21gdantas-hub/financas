package br.com.minhasfinancas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.minhasfinancas.data.local.dao.CategoriaDao
import br.com.minhasfinancas.data.local.dao.ContaDao
import br.com.minhasfinancas.data.local.dao.MetaDao
import br.com.minhasfinancas.data.local.dao.OrcamentoDao
import br.com.minhasfinancas.data.local.dao.TransacaoDao
import br.com.minhasfinancas.data.local.entity.CategoriaEntity
import br.com.minhasfinancas.data.local.entity.ContaEntity
import br.com.minhasfinancas.data.local.entity.MetaEntity
import br.com.minhasfinancas.data.local.entity.OrcamentoEntity
import br.com.minhasfinancas.data.local.entity.TransacaoEntity

@Database(
    entities = [ContaEntity::class, CategoriaEntity::class, TransacaoEntity::class, OrcamentoEntity::class, MetaEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contaDao(): ContaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun transacaoDao(): TransacaoDao
    abstract fun orcamentoDao(): OrcamentoDao
    abstract fun metaDao(): MetaDao
}
