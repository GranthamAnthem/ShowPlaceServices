package com.showplace.routes

import com.showplace.dao.DAOFacade
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.venueRouting(dao: DAOFacade) {
    route("/venues") {
        get {
            try {
                call.respond(dao.getAllVenues())
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, "No venues listed")
            }
        }
    }
    route("/venue") {
        get("/{id?}") {
            val id = call.parameters["id"]?.toInt() ?: return@get call.respondText(
                "Venue id not exist", status = HttpStatusCode.NotFound
            )

            val venue = dao.getVenue(id) ?: return@get call.respondText(
                "No venue with id $id", status = HttpStatusCode.NotFound
            )

            call.respond(venue)
        }
    }
}