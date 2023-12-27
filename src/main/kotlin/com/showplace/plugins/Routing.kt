package com.showplace.plugins

import com.showplace.dao.DAOFacade
import com.showplace.dao.DAOFacadeCacheImpl
import com.showplace.dao.DAOFacadeImpl
import com.showplace.routes.bandRouting
import com.showplace.routes.showRouting
import com.showplace.routes.venueRouting
import com.showplace.routes.webScrapeRoutes
import com.showplace.util.shouldUpdateNewShows
import com.showplace.webScraper.getBandsFromWeb
import com.showplace.webScraper.getShowsFromWeb
import com.showplace.webScraper.getVenuesFromWeb
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

fun Application.configureRouting() {
    val dao: DAOFacade = DAOFacadeCacheImpl(
        delegate = DAOFacadeImpl(),
        storagePath = File(environment.config.property("storage.ehcacheFilePath").toString())
    )

    routing {
        get("/") {
            call.respondText("Welcome to Showplace")
        }
        bandRouting(dao)
        showRouting(dao)
        venueRouting(dao)
        webScrapeRoutes(dao)
    }
}