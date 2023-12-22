package com.showplace.model


import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date


@Serializable
data class Show(
    val id: Int? = null,
    val date: LocalDate,
    val time: String?,
    val price: String?,
    val venue: Venue,
    val lineup: List<Band> = listOf(),
) : java.io.Serializable

object ShowTable : IntIdTable("show") {
    val date = date("date")
    val time = varchar("time", 50).nullable()
    val price = varchar("string", 50).nullable()
    val venue = reference("venue", VenueTable.id)
}

object ShowBandTable : Table("show_band") {
    val show = reference("show", ShowTable.id)
    val band = reference("band", BandTable.id)
    override val primaryKey = PrimaryKey(show, band)
}