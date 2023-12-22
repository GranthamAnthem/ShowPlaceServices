package com.showplace.dao

import com.showplace.model.Band
import com.showplace.model.Show
import com.showplace.model.Venue

interface DAOFacade {
    suspend fun getBand(id: Int): Band?
    suspend fun getAllBands(): List<Band>
    suspend fun addAllBands(bands: List<Band>)

    suspend fun getVenue(id: Int): Venue?
    suspend fun getAllVenues(): List<Venue>
    suspend fun addAllVenues(venues: List<Venue>)

    suspend fun getShowById(id: Int): Show?
    suspend fun addAllShows(shows: List<Show>)
    suspend fun getAllShows(): List<Show>
    suspend fun getAllShowsFromToday(page: Long = 0): List<Show>
    suspend fun updateShow(show: Show)
    suspend fun addShow(show: Show)
    suspend fun deleteShowById(id: Int): Boolean
}