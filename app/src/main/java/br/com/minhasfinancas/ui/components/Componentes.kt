package br.com.minhasfinancas.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.minhasfinancas.data.local.entity.CategoriaEntity
import br.com.minhasfinancas.data.local.entity.TipoTransacao
import br.com.minhasfinancas.data.local.entity.TransacaoEntity
import br.com.minhasfinancas.util.Fmt
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.YearMonth

/** Transação + dados da categoria já resolvidos, prontos para exibir. */
data class ItemTransacao(val t: TransacaoEntity, val categoria: String, val icone: String, val cor: Long)

fun TransacaoEntity.item(cats: Map<Long, CategoriaEntity>): ItemTransacao {
    val c = categoriaId?.let { cats[it] }
    val transf = tipo == TipoTransacao.TRANSFERENCIA_SAIDA || tipo == TipoTransacao.TRANSFERENCIA_ENTRADA
    return ItemTransacao(
        this,
        c?.nome ?: if (transf) "Transferência" else "Sem categoria",
        c?.icone ?: if (transf) "🔁" else "❔",
        c?.cor ?: 0xFF90A4AE,
    )
}

@Composable
fun TransacaoLinha(item: ItemTransacao, onClick: () -> Unit, onPaga: (() -> Unit)? = null) {
    val t = item.t
    val entrada = t.tipo == TipoTransacao.RECEITA || t.tipo == TipoTransacao.TRANSFERENCIA_ENTRADA
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).background(Color(item.cor).copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(item.icone) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(t.descricao.ifBlank { item.categoria }, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${item.categoria} · ${Fmt.data(LocalDate.ofEpochDay(t.data))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                (if (entrada) "+ " else "− ") + Fmt.moeda(t.valor),
                color = if (entrada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
            )
            if (onPaga != null) {
                Text(
                    if (t.paga) (if (entrada) "Recebido" else "Pago") else "Pendente",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onPaga() }.padding(horizontal = 4.dp, vertical = 14.dp), // alvo de toque ~48dp
                )
            }
        }
    }
}

@Composable
fun EstadoVazio(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        modifier = modifier.fillMaxWidth().padding(24.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Gráfico de rosca simples: cada fatia = (valor, cor). */
@Composable
fun GraficoRosca(fatias: List<Pair<Long, Color>>, modifier: Modifier = Modifier.size(120.dp)) {
    val total = fatias.sumOf { it.first }.coerceAtLeast(1L)
    Canvas(modifier) {
        val traco = size.minDimension * 0.22f
        var inicio = -90f
        fatias.forEach { (valor, cor) ->
            val varredura = 360f * valor / total
            drawArc(
                color = cor, startAngle = inicio, sweepAngle = varredura, useCenter = false,
                topLeft = Offset(traco / 2, traco / 2),
                size = Size(size.width - traco, size.height - traco),
                style = Stroke(width = traco),
            )
            inicio += varredura
        }
    }
}

@Composable
fun SeletorMes(mes: YearMonth, onMudar: (YearMonth) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onMudar(mes.minusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mês anterior")
        }
        Text(Fmt.mes(mes), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { onMudar(mes.plusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo mês")
        }
    }
}

/** Linha rolável de chips para escolher uma opção (usada em filtros e formulários). */
@Composable
fun <T> ChipsOpcoes(
    opcoes: List<Pair<String, T?>>,
    selecionado: T?,
    onSelecionar: (T?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(opcoes) { (rotulo, valor) ->
            FilterChip(selected = valor == selecionado, onClick = { onSelecionar(valor) }, label = { Text(rotulo) })
        }
    }
}

/** Campo de valor com máscara: o usuário digita só números e o campo preenche os centavos. */
@Composable
fun CampoValor(centavos: Long, onMudar: (Long) -> Unit, modifier: Modifier = Modifier, rotulo: String = "Valor") {
    OutlinedTextField(
        value = Fmt.moeda(centavos),
        onValueChange = { s -> onMudar(s.filter { it.isDigit() }.takeLast(11).toLongOrNull() ?: 0L) },
        label = { Text(rotulo) },
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineSmall,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Diálogo de calendário reutilizável. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogData(inicial: LocalDate?, onEscolher: (LocalDate) -> Unit, onFechar: () -> Unit) {
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = (inicial ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onFechar,
        confirmButton = {
            TextButton(onClick = {
                estado.selectedDateMillis?.let { onEscolher(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                onFechar()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } },
    ) { DatePicker(state = estado) }
}
