package br.com.minhasfinancas.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Versão 1 -> 2: adiciona orçamento e metas SEM apagar os dados já lançados. */
val MIGRACAO_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `orcamento` (`categoriaId` INTEGER NOT NULL, `limite` INTEGER NOT NULL, PRIMARY KEY(`categoriaId`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `meta` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `valorAlvo` INTEGER NOT NULL, `valorGuardado` INTEGER NOT NULL, `prazo` INTEGER)")
    }
}
