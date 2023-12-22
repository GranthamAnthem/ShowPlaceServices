package com.showplace.model

import com.showplace.webScraper.getVenuesFromWeb
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class Venue(
    val id: Int? = null,
    val name: String,
    val ageLimit: String?,
    val capacity: String?,
    val accessibility: String? = null,
    val url: String?
) : java.io.Serializable

object VenueTable : IntIdTable() {
    val name = varchar("name", 255)
    val ageLimit = varchar("ageLimit", 255).nullable()
    val capacity = text("capacity").nullable()
    val accessibility = text("accessibility").nullable()
    val url = varchar("url", 128).nullable()
}

val venues: List<Venue> = getVenuesFromWeb()