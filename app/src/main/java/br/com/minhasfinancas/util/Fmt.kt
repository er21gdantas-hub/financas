package br.com.minhasfinancas.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatadores no padrão brasileiro: R$ 1.234,56 e dd/MM/aaaa. */
object Fmt {
    private val br: Locale = Locale.forLanguageTag("pt-BR")
    private val formatoMoeda: NumberFormat = NumberFormat.getCurrencyInstance(br)
    private val formatoData: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val formatoMes: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", br)

    /** Recebe o valor em CENTAVOS (Long) e devolve o texto em reais. */
    fun moeda(centavos: Long): String = formatoMoeda.format(centavos / 100.0)

    fun data(d: LocalDate): String = d.format(formatoData)

    fun mes(m: YearMonth): String = m.format(formatoMes).replaceFirstChar { it.uppercase() }
}
