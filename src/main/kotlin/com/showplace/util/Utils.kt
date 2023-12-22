package com.showplace.util

import com.showplace.model.Show
import io.ktor.server.config.*
import kotlinx.datetime.*

fun shouldUpdateNewShows(shows: List<Show>): Boolean {
    val lastShowDate = shows.last().date
    val today: LocalDate =  Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysRemaining = lastShowDate.minus(today).days
    return daysRemaining <= 0
}

fun dbUrlBuilder(config: ApplicationConfig): String {
    val jdbc = config.property("storage.jdbc").getString()
    val host = config.property("storage.host").getString()
    val password = config.property("storage.password").getString()
    val user = config.property("storage.user").getString()
    val jdbcURL = "${jdbc}${host}?user=$user&password=$password"
    return jdbcURL
}