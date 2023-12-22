package com.showplace.routes

import com.showplace.dao.DAOFacade
import com.showplace.model.Show
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.showRouting(dao: DAOFacade) {
    route("/shows") {
        get {
            try {
                val page = call.parameters["page"]?.toLong() ?: 0L
                call.respond(dao.getAllShowsFromToday(page))
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, "No shows listed")
            }
        }
    }
    route("/allShows") {
        get {
            try {
                call.respond(dao.getAllShows())
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, "No shows listed")
            }
        }
    }
    route("/show") {
        get("/{id?}") {
            val showId = call.parameters["id"]?.toInt() ?: return@get call.respondText(
                "Show does not exist", status = HttpStatusCode.NotFound
            )
            val show = dao.getShowById(showId) ?: return@get call.respondText(
                "No show with id $showId", status = HttpStatusCode.NotFound
            )

            call.respond(show)
        }
        post("/{id?}") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val showBody = call.receive<Show>()
            call.respondText("post received for id: $id: ${showBody}")
            dao.updateShow(showBody)
        }
        post("/remove/{id?}") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            println("id: $id")
            call.respondText("post deleted for id: $id")
            dao.deleteShowById(id)
        }
        post("/add") {
            val showBOdy = call.receive<Show>()
            call.respondText("show added deleted for: $showBOdy")
            dao.addShow(showBOdy)
        }
    }
}