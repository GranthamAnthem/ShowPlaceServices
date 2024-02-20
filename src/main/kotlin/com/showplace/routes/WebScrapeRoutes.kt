package com.showplace.routes

import com.showplace.dao.DAOFacade
import com.showplace.util.shouldUpdateNewShows
import com.showplace.webScraper.getBandsFromWeb
import com.showplace.webScraper.getShowsFromWeb
import com.showplace.webScraper.getVenuesFromWeb
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch

fun Route.webScrapeRoutes(dao: DAOFacade) {
    route("/scrape") {
        get("/prepopulate") {
            launch {
                try {
                    call.respondText("populating database...")
                    if (dao.getAllBands().isEmpty()) {
                        dao.addAllBands(getBandsFromWeb())
                    }
                    if (dao.getAllVenues().isEmpty()) {
                        dao.addAllVenues(getVenuesFromWeb())
                    }
                    if (dao.getAllShows().isEmpty()) {
                        dao.addAllShows(getShowsFromWeb())
                    }
                } catch (ex: Exception) {
                    call.respondText("Error populating database")
                }
            }
        }
        get("/update") {
            launch {
                try {
                    val show = dao.getLatestShow()
                    show?.let {
                        if (shouldUpdateNewShows(it)) {
                            call.respondText("Adding new shows...")
                            val showsFromWeb = getShowsFromWeb()
                            dao.addAllShows(showsFromWeb)
                        } else {
                            call.respondText("No new shows to add")
                        }
                    } ?: run {
                        call.respondText("No new shows to add")
                    }
                } catch (ex: Exception) {
                    call.respondText("Error populating database")
                }
            }
        }
    }
}