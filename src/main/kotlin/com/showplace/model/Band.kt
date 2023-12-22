package com.showplace.model

import com.showplace.webScraper.getBandsFromWeb
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class Band(
    val id: Int? = null,
    val name: String,
    val genre: String?,
    val url: String?,
    val isLocal: Boolean
) : java.io.Serializable

object BandTable : IntIdTable() {
    val name = varchar("name", 128)
    val genre = varchar("genre", 1024).nullable()
    val url = varchar("url", 1024).nullable()
    val isLocal = bool("isLocal")
}

val bands: List<Band> = getBandsFromWeb()