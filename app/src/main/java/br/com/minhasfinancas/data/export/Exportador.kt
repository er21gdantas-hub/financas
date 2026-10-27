package br.com.minhasfinancas.data.export

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import br.com.minhasfinancas.data.local.entity.TipoTransacao
import br.com.minhasfinancas.domain.model.LinhaRelatorio
import br.com.minhasfinancas.domain.model.Relatorio
import br.com.minhasfinancas.util.Fmt
import java.io.OutputStream
import java.util.Locale

/** Gera CSV (para Excel/Planilhas) e PDF do relatório mensal, sem bibliotecas externas. */
object Exportador {
    private fun assinado(l: LinhaRelatorio): Long =
        if (l.tipo == TipoTransacao.DESPESA || l.tipo == TipoTransacao.TRANSFERENCIA_SAIDA) -l.valor else l.valor

    private fun rotuloTipo(t: TipoTransacao) = when (t) {
        TipoTransacao.RECEITA -> "Receita"
        TipoTransacao.DESPESA -> "Despesa"
        TipoTransacao.TRANSFERENCIA_SAIDA -> "Transferência (saída)"
        TipoTransacao.TRANSFERENCIA_ENTRADA -> "Transferência (entrada)"
    }

    /** Número com vírgula decimal e sem separador de milhar, para abrir certo no Excel em pt-BR. */
    private fun numero(centavos: Long): String {
        val sinal = if (centavos < 0) "-" else ""
        val a = kotlin.math.abs(centavos)
        return String.format(Locale.ROOT, "%s%d,%02d", sinal, a / 100, a % 100)
    }

    /** Protege contra fórmulas (=, +, -, @) e escapa ; " e quebras de linha. */
    private fun campo(texto: String): String {
        var t = texto
        if (t.isNotEmpty() && t[0] in "=+-@") t = "'$t"
        return if (t.any { it == ';' || it == '"' || it == '\n' || it == '\r' }) "\"" + t.replace("\"", "\"\"") + "\"" else t
    }

    fun csv(r: Relatorio): String {
        val sb = StringBuilder("\uFEFF") // BOM: o Excel reconhece os acentos
        sb.append("Data;Tipo;Categoria;Conta;Descrição;Valor;Situação\n")
        r.lancamentos.forEach { l ->
            sb.append(Fmt.data(l.data)).append(';')
                .append(rotuloTipo(l.tipo)).append(';')
                .append(campo(l.categoria)).append(';')
                .append(campo(l.conta)).append(';')
                .append(campo(l.descricao)).append(';')
                .append(numero(assinado(l))).append(';')
                .append(if (l.paga) "Pago/Recebido" else "Pendente").append('\n')
        }
        return sb.toString()
    }

    fun pdf(r: Relatorio, saida: OutputStream) {
        val e = EscritorPdf()
        val titulo = Paint().apply { textSize = 18f; isFakeBoldText = true; isAntiAlias = true }
        val normal = Paint().apply { textSize = 10f; isAntiAlias = true }
        val negrito = Paint(normal).apply { isFakeBoldText = true }
        val direita = Paint(normal).apply { textAlign = Paint.Align.RIGHT }

        e.texto("Minhas Finanças · ${Fmt.mes(r.mes)}", titulo); e.avancar(26f)
        e.texto("Receitas: ${Fmt.moeda(r.receitas)}", normal); e.avancar(14f)
        e.texto("Despesas: ${Fmt.moeda(r.despesas)}", normal); e.avancar(14f)
        e.texto("Resultado do mês: ${Fmt.moeda(r.receitas - r.despesas)}", negrito); e.avancar(14f)
        e.texto("Saldo ao fim do mês: ${Fmt.moeda(r.saldoFinal)}", negrito); e.avancar(24f)

        e.texto("Despesas por categoria", negrito); e.avancar(16f)
        if (r.categorias.isEmpty()) { e.texto("Sem despesas no mês.", normal); e.avancar(14f) }
        r.categorias.forEach {
            e.texto("${it.nome} (${it.percentual}%)", normal, 40f)
            e.texto(Fmt.moeda(it.valor), direita, 555f); e.avancar(14f)
        }
        e.avancar(10f)

        e.texto("Maiores gastos", negrito); e.avancar(16f)
        r.maiores.forEach {
            e.texto(Fmt.data(it.data), normal, 40f)
            e.texto(it.descricao.ifBlank { it.categoria }.take(45), normal, 110f)
            e.texto(Fmt.moeda(it.valor), direita, 555f); e.avancar(14f)
        }
        e.avancar(10f)

        e.texto("Lançamentos do mês", negrito); e.avancar(16f)
        r.lancamentos.forEach {
            e.texto(Fmt.data(it.data), normal, 40f)
            e.texto(it.categoria.take(18), normal, 105f)
            e.texto(it.descricao.take(32), normal, 200f)
            e.texto(Fmt.moeda(assinado(it)), direita, 555f); e.avancar(14f)
        }
        e.fechar(saida)
    }
}

/** Cuida das páginas A4 (595x842 pt): abre uma nova quando acaba o espaço. */
private class EscritorPdf {
    private val doc = PdfDocument()
    private var pagina: PdfDocument.Page? = null
    private var numero = 0
    private var y = 0f

    private fun novaPagina() {
        pagina?.let { doc.finishPage(it) }
        numero++
        pagina = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, numero).create())
        y = 50f
    }

    fun texto(t: String, p: Paint, x: Float = 40f) {
        if (pagina == null || y > 800f) novaPagina()
        val canvas: Canvas = pagina!!.canvas
        canvas.drawText(t, x, y, p)
    }

    fun avancar(dy: Float) { y += dy }

    fun fechar(saida: OutputStream) {
        pagina?.let { doc.finishPage(it) }
        doc.writeTo(saida)
        doc.close()
    }
}
