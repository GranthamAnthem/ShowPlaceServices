package com.showplace.webScraper

import kotlinx.datetime.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object Date {
    private val today = LocalDate.now().toKotlinLocalDate()
    private val monthYearFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")

    fun isCurrentMonthYear(localDate: kotlinx.datetime.LocalDate?): Boolean {
        return localDate?.month == today.month && localDate.year == today.year
    }

    fun extractMonthYear(input: String): kotlinx.datetime.LocalDate? {
        return try {
            LocalDate.parse(input, monthYearFormatter).toKotlinLocalDate()
        } catch (e: DateTimeParseException) {
            null
        }
    }
}