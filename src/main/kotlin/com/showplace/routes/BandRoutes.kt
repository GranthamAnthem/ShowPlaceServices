package com.showplace.routes

import com.showplace.dao.DAOFacade
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bandRouting(dao: DAOFacade) {
    route("/bands") {
        get {
            try {
                call.respond(dao.getAllBands())
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, "No bands listed")
            }
        }
    }
    route("/band") {
        get("/{id?}") {

            val id = call.parameters["id"]?.toInt() ?: return@get call.respondText(
                "Band id does not exist", status = HttpStatusCode.NotFound
            )

            val band = dao.getBand(id) ?: return@get call.respondText(
                "No band with id $id", status = HttpStatusCode.NotFound
            )

            call.respond(band)
        }
    }
}