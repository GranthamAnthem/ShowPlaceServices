package com.showplace.webScraper

import com.showplace.Constants.BANDS_URL
import com.showplace.Constants.BASE_URL
import com.showplace.Constants.VENUES_URL
import com.showplace.model.*
import com.showplace.webScraper.Date.extractMonthYear
import com.showplace.webScraper.Date.isCurrentMonthYear
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import com.showplace.webScraper.JsoupHelper.extractShowDetails
import com.showplace.webScraper.JsoupHelper.extractVenueDetails
import com.showplace.webScraper.JsoupHelper.fromItalicized
import com.showplace.webScraper.JsoupHelper.fromParagraph
import com.showplace.webScraper.JsoupHelper.fromStrong
import com.showplace.webScraper.JsoupHelper.isHeadingTwo
import com.showplace.webScraper.JsoupHelper.isParagraph
import kotlinx.datetime.LocalDate


fun getShowsFromWeb(): List<Show> {
    val doc = Jsoup.connect(BASE_URL).get()
    val jsoupShows: Elements = doc.select("div.body-text, h2, p")

    var localDate: LocalDate? = null

    val shows = jsoupShows.mapNotNull {
        if (it.isHeadingTwo()) {
            localDate = extractMonthYear(it.text())
        }
        if (it.isParagraph() && isCurrentMonthYear(localDate)) {
            localDate?.let { localDate ->
                val show = extractShowDetails(it.text(), localDate)
                show
            }
        } else null
    }
    return shows
}


fun getVenuesFromWeb(): List<Venue> {
    val doc = Jsoup.connect(VENUES_URL).get()
    val jsoupVenues: Elements = doc.select("div.body-text p")

    val venues = jsoupVenues.mapNotNull {

        val name = it.fromStrong().trim()
        val rules = it.fromItalicized()
        val url = it.fromParagraph().removePrefix("-").trim()

        if (name.isNotBlank() && rules.isNotBlank() && url.isNotBlank()) {
            val venue = extractVenueDetails(name, rules, url)
            venue
        } else {
            null
        }
    }
    return venues
}

fun getBandsFromWeb(): List<Band> {
    val doc = Jsoup.connect(BANDS_URL).get()
    val jsoupBands: Elements = doc.select("div.body-text p")

    val bands = jsoupBands.mapNotNull {
        val name = it.fromStrong()
        if (name.isNotBlank()) {
            Band(
                name = name,
                genre = it.fromItalicized(),
                url = it.fromParagraph(),
                isLocal = true
            )

        } else null
    }
    return bands
}