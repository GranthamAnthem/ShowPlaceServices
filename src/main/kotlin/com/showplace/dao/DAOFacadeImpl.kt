package com.showplace.dao

import com.showplace.dao.DatabaseFactory.dbQuery
import com.showplace.model.*
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class DAOFacadeImpl : DAOFacade {

    companion object {
        const val PAGE_LIMIT = 50
        val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    fun resultRowToBand(row: ResultRow) = Band(
        id = row[BandTable.id].value,
        name = row[BandTable.name],
        genre = row[BandTable.genre],
        url = row[BandTable.url],
        isLocal = row[BandTable.isLocal],
    )

    fun resultRowToVenue(row: ResultRow) = Venue(
        id = row[VenueTable.id].value,
        name = row[VenueTable.name],
        ageLimit = row[VenueTable.ageLimit],
        capacity = row[VenueTable.capacity],
        accessibility = row[VenueTable.accessibility],
        url = row[VenueTable.url]
    )

    fun resultRowToShow(row: ResultRow) = Show(
        id = row[ShowTable.id].value,
        date = row[ShowTable.date],
        time = row[ShowTable.time],
        price = row[ShowTable.price],
        venue = resultRowToVenue(row),
        lineup = emptyList() // the bands will be added later
    )

    override suspend fun addAllBands(bands: List<Band>) {
        dbQuery {
            bands.forEach { band ->
                BandTable.insertIgnore {
                    it[name] = band.name
                    it[genre] = band.genre
                    it[url] = band.url
                    it[isLocal] = band.isLocal
                }
            }
        }
    }

    override suspend fun addAllVenues(venues: List<Venue>) {
        dbQuery {
            venues.forEach { venue ->
                VenueTable.insertIgnore {
                    it[name] = venue.name
                    it[ageLimit] = venue.ageLimit
                    it[capacity] = venue.capacity
                    it[accessibility] = venue.accessibility
                    it[url] = venue.url
                }
            }
        }
    }

    override suspend fun addAllShows(shows: List<Show>) {
        dbQuery {
            for (currentShow in shows) {
                // insert a new currentShow if not already exists
                val venueId = VenueTable
                    .select { VenueTable.name eq currentShow.venue.name }
                    .singleOrNull()?.get(VenueTable.id) // find the venue by name
                    ?: VenueTable.insertAndGetId {
                        // insert a new venue if not found
                        it[name] = currentShow.venue.name
                        it[ageLimit] = currentShow.venue.ageLimit
                        it[capacity] = currentShow.venue.capacity
                        it[accessibility] = currentShow.venue.accessibility
                        it[url] = currentShow.venue.url
                    }


                val showId = ShowTable.insertAndGetId {

                    // insert a new currentShow with the venue id
                    it[date] = currentShow.date
                    it[time] = currentShow.time
                    it[price] = currentShow.price
                    it[venue] = venueId
                }
                val lineup = ShowBandTable
                    .join(BandTable, JoinType.INNER, ShowBandTable.band, BandTable.id)
                    .select { ShowBandTable.show eq currentShow.id }
                    .map(::resultRowToBand)
                // add the lineup to the show
                currentShow.copy(lineup = lineup)

                for (currentBand in currentShow.lineup) {
                    val bandId = BandTable
                        .select { BandTable.name eq currentBand.name }
                        .singleOrNull()?.get(BandTable.id) // find the band by name
                        ?: BandTable.insertAndGetId {
                            // insert a new band if not found
                            it[name] = currentBand.name
                            it[genre] = currentBand.genre
                            it[url] = currentBand.url
                            it[isLocal] = currentBand.isLocal
                        }
                    // insert a new row in the ShowBandTable with the currentShow id and the band id


                    ShowBandTable.insert {
                        it[show] = showId
                        it[band] = bandId
                    }
                }
            }
        }
    }

    override suspend fun getAllBands(): List<Band> {
        return dbQuery {
            BandTable.selectAll().map(::resultRowToBand)
        }
    }

    override suspend fun getBand(id: Int): Band? {
        return dbQuery {
            BandTable
                .select(BandTable.id eq id)
                .map(::resultRowToBand)
                .singleOrNull()
        }
    }

    override suspend fun getAllVenues(): List<Venue> {
        return dbQuery {
            VenueTable.selectAll().map(::resultRowToVenue)
        }
    }

    override suspend fun getVenue(id: Int): Venue? {
        return dbQuery {
            VenueTable
                .select(VenueTable.id eq id)
                .map(::resultRowToVenue)
                .singleOrNull()
        }
    }

    override suspend fun getAllShowsFromToday(page: Long): List<Show> {
        return dbQuery {
            val shows = ShowTable
                .join(VenueTable, JoinType.INNER, ShowTable.venue, VenueTable.id)
                .selectAll()
                .andWhere { ShowTable.date greaterEq today }
                .limit(PAGE_LIMIT, page * PAGE_LIMIT)
                .map(::resultRowToShow)
                .map { show ->
                    val bands = ShowBandTable
                        .join(BandTable, JoinType.INNER, ShowBandTable.band, BandTable.id)
                        .select { ShowBandTable.show eq show.id }
                        .map(::resultRowToBand)
                    show.copy(lineup = bands)
                }
            shows
        }
    }

    override suspend fun getAllShows(page: Long): List<Show> {
        // get all shows, along with the venues and the lineups
        return dbQuery {
            val shows = ShowTable
                .join(VenueTable, JoinType.INNER, ShowTable.venue, VenueTable.id)
                .selectAll()
                .map(::resultRowToShow)
                .map { show ->
                    val bands = ShowBandTable
                        .join(BandTable, JoinType.INNER, ShowBandTable.band, BandTable.id)
                        .select { ShowBandTable.show eq show.id }
                        .map(::resultRowToBand)
                    show.copy(lineup = bands)
                }
            shows
        }
    }

    override suspend fun updateShow(currentShow: Show) {
        dbQuery {
            val venueId = VenueTable
                .select { VenueTable.name eq currentShow.venue.name }
                .singleOrNull()?.get(VenueTable.id) // find the venue by name
                ?: VenueTable.insertAndGetId {
                    // insert a new venue if not found
                    it[name] = currentShow.venue.name
                    it[ageLimit] = currentShow.venue.ageLimit
                    it[capacity] = currentShow.venue.capacity
                    it[accessibility] = currentShow.venue.accessibility
                    it[url] = currentShow.venue.url
                }
            val showId = ShowTable.select { ShowTable.id eq currentShow.id }.singleOrNull()?.get(ShowTable.id)
                ?: throw IllegalArgumentException("Show with id ${currentShow.id} not found")

            ShowTable.update({ ShowTable.id eq showId }) {
                it[date] = currentShow.date
                it[time] = currentShow.time
                it[price] = currentShow.price
                it[venue] = venueId
            }
            ShowBandTable.deleteWhere { ShowBandTable.show eq showId }
            for (currentBand in currentShow.lineup) {
                val bandId = BandTable
                    .select { BandTable.name eq currentBand.name }
                    .singleOrNull()?.get(BandTable.id) // find the band by name
                    ?: BandTable.insertAndGetId {
                        // insert a new band if not found
                        it[name] = currentBand.name
                        it[genre] = currentBand.genre
                        it[url] = currentBand.url
                        it[isLocal] = currentBand.isLocal
                    }
                // insert a new row in the ShowBandTable with the show id and the band id
                ShowBandTable.insert {
                    it[show] = showId
                    it[band] = bandId
                }
            }
        }
    }

    override suspend fun addShow(currentShow: Show) {
        dbQuery {
            val venueId = VenueTable
                .select { VenueTable.name eq currentShow.venue.name }
                .singleOrNull()?.get(VenueTable.id) // find the venue by name
                ?: VenueTable.insertAndGetId {
                    // insert a new venue if not found
                    it[name] = currentShow.venue.name
                    it[ageLimit] = currentShow.venue.ageLimit
                    it[capacity] = currentShow.venue.capacity
                    it[accessibility] = currentShow.venue.accessibility
                    it[url] = currentShow.venue.url
                }
            val showId = ShowTable.insertAndGetId {
                // insert a new show with the venue id
                it[date] = currentShow.date
                it[time] = currentShow.time
                it[price] = currentShow.price
                it[venue] = venueId
            }
            for (currentBand in currentShow.lineup) {
                val bandId = BandTable
                    .select { BandTable.name eq currentBand.name }
                    .singleOrNull()?.get(BandTable.id) // find the band by name
                    ?: BandTable.insertAndGetId {
                        // insert a new band if not found
                        it[name] = currentBand.name
                        it[genre] = currentBand.genre
                        it[url] = currentBand.url
                        it[isLocal] = currentBand.isLocal
                    }
                // insert a new row in the ShowBandTable with the show id and the band id
                ShowBandTable.insert {
                    it[show] = showId
                    it[band] = bandId
                }
            }
        }
    }

    override suspend fun getShowById(id: Int): Show? {
        // get the show by id, along with the venue and the bands
        return dbQuery {
            val show = ShowTable
                .join(VenueTable, JoinType.INNER, ShowTable.venue, VenueTable.id)
                .select { ShowTable.id eq id }
                .map(::resultRowToShow)
                .singleOrNull()

            if (show != null) {
                // get the bands for the show
                val bands = ShowBandTable
                    .join(BandTable, JoinType.INNER, ShowBandTable.band, BandTable.id)
                    .select { ShowBandTable.show eq id }
                    .map(::resultRowToBand)
                // add the bands to the show
                val show = show.copy(lineup = bands)
                show
            } else {
                null
            }
        }
    }

    override suspend fun deleteShowById(id: Int): Boolean {
        return dbQuery {
            ShowBandTable.deleteWhere { ShowBandTable.show eq id } > 0
            ShowTable.deleteWhere { ShowTable.id eq id } > 0
        }
    }
}