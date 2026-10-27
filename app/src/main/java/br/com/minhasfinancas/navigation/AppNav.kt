package br.com.minhasfinancas.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.minhasfinancas.ui.categorias.CategoriasScreen
import br.com.minhasfinancas.ui.configuracoes.BackupScreen
import br.com.minhasfinancas.ui.configuracoes.SegurancaScreen
import br.com.minhasfinancas.ui.contas.ContasScreen
import br.com.minhasfinancas.ui.inicio.InicioScreen
import br.com.minhasfinancas.ui.metas.MetasScreen
import br.com.minhasfinancas.ui.mais.EmBreveScreen
import br.com.minhasfinancas.ui.orcamento.OrcamentoScreen
import br.com.minhasfinancas.ui.relatorios.RelatoriosScreen
import br.com.minhasfinancas.ui.transferencia.TransferenciaScreen
import br.com.minhasfinancas.ui.mais.MaisScreen
import br.com.minhasfinancas.ui.transacoes.FormScreen
import br.com.minhasfinancas.ui.transacoes.TransacoesScreen

private data class Aba(val rota: String, val rotulo: String, val icone: ImageVector)

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val rota = nav.currentBackStackEntryAsState().value?.destination?.route
    val abas = listOf(
        Aba(Rotas.INICIO, "Início", Icons.Filled.Home),
        Aba(Rotas.TRANSACOES, "Transações", Icons.AutoMirrored.Filled.List),
        Aba(Rotas.RELATORIOS, "Relatórios", Icons.Filled.DateRange),
        Aba(Rotas.METAS, "Metas", Icons.Filled.Star),
        Aba(Rotas.MAIS, "Mais", Icons.Filled.Menu),
    )
    Scaffold(
        bottomBar = {
            if (abas.any { it.rota == rota }) {
                NavigationBar {
                    abas.forEach { a ->
                        NavigationBarItem(
                            selected = rota == a.rota,
                            onClick = {
                                nav.navigate(a.rota) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(a.icone, contentDescription = null) },
                            label = { Text(a.rotulo) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (rota == Rotas.INICIO || rota == Rotas.TRANSACOES) {
                FloatingActionButton(onClick = { nav.navigate(Rotas.form()) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nova transação")
                }
            }
        },
    ) { pad ->
        NavHost(nav, startDestination = Rotas.INICIO, modifier = Modifier.padding(pad)) {
            composable(Rotas.INICIO) { InicioScreen(onAbrir = { nav.navigate(Rotas.form(it)) }) }
            composable(Rotas.TRANSACOES) { TransacoesScreen(onAbrir = { nav.navigate(Rotas.form(it)) }) }
            composable(Rotas.RELATORIOS) { RelatoriosScreen() }
            composable(Rotas.METAS) { MetasScreen() }
            composable(Rotas.MAIS) { MaisScreen(onIr = { nav.navigate(it) }) }
            composable(Rotas.CONTAS) { ContasScreen(onVoltar = { nav.popBackStack() }) }
            composable(Rotas.ORCAMENTO) { OrcamentoScreen(onVoltar = { nav.popBackStack() }) }
            composable(Rotas.TRANSFERENCIA) { TransferenciaScreen(onFechar = { nav.popBackStack() }) }
            composable(Rotas.BACKUP) { BackupScreen(onVoltar = { nav.popBackStack() }) }
            composable(Rotas.SEGURANCA) { SegurancaScreen(onVoltar = { nav.popBackStack() }) }
            composable(Rotas.CATEGORIAS) { CategoriasScreen(onVoltar = { nav.popBackStack() }) }
            composable(Rotas.FORM, arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                FormScreen(onFechar = { nav.popBackStack() })
            }
        }
    }
}
