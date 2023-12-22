package com.showplace.webScraper


import com.showplace.model.*
import kotlinx.datetime.LocalDate
import org.jsoup.nodes.Element

object JsoupHelper {

    private val timeRegex = Regex("\\d{1,2}(:\\d{2})?(AM|PM)")
    private val priceRegex = Regex("\\$((\\d+(\\.\\d{2})?)|free|FREE|sold out|SOLD OUT)")
    private val venueRegex =   Regex("@\\s*(.*)")

    fun extractShowDetails(input: String, localDate: LocalDate): Show? {
        val bandNames = input.substringBefore(".").split(",").map { it.trim() }
        val time = getTimeFromRegex(input)
        val price = getPriceFromRegex(input)
        val currentVenue = getVenueFromRegex(input)

        if (bandNames.isNullOrEmpty() || time.isBlank()) return null

        val venue = findOrAddVenue(currentVenue)
        val lineup = findOrAddBands(bandNames) ?: listOf()

        return Show(
            date = localDate,
            lineup = lineup,
            time = time,
            price = price,
            venue = venue
        )
    }

    fun findOrAddBands(bandName: List<String>?): List<Band>? {
        val bands = bandName?.map { name ->
            bands.find { it.name == name } ?: Band(
                name = name,
                url = null,
                genre = null,
                isLocal = false
            )
        }
        return bands
    }

    fun findOrAddVenue(currentVenue: String): Venue {
        var venue = venues.find {
            (it.name == currentVenue) || it.name.contains(currentVenue)
        }

        if (currentVenue.equals("Ottobar Upstairs")) {
            venue = venues.find { it.name.contains("Ottobar")}
        }

        return venue ?: Venue(
            name = currentVenue,
            ageLimit = null, capacity = null, accessibility = null, url = null
        )
    }

    fun extractVenueDetails(name: String, input: String, url: String): Venue {
        val rules = ruleSplitter(input).toMutableList()

        val (age, capacity, accessibility) = if (rules.size < 3) {
            rules.add("not available")
            rules
        } else {
            rules
        }

        return Venue(
            name = name,
            ageLimit = age.trim(),
            capacity = capacity.substringAfter("Capacity: ").trim(),
            accessibility = accessibility.substringAfter("Accessibility: ").trim(),
            url = url
        )
    }

    fun ruleSplitter(rules: String?): List<String> {
        return rules?.let { it.split("-").filter { it.isNotBlank() } } ?: listOf("empty", "empty", "empty")
    }

    fun getTimeFromRegex(input: String): String {
        return timeRegex.find(input)?.value ?: ""
    }

    fun extractAllTimes(input: String): List<String> {
        val matches = timeRegex.findAll(input)
        val times = matches.map { it.value }.toList()
        return times
    }

    fun getPriceFromRegex(input: String): String? = when {
        input.contains(NoPrice.FREE.value) -> NoPrice.FREE.value
        input.contains(NoPrice.SOLD_OUT.value) -> NoPrice.SOLD_OUT.value
        else -> priceRegex.find(input)?.value
    }

    fun getVenueFromRegex(input: String): String {
        return venueRegex.find(input)?.value?.trim('@', ' ') ?: ""
    }

    enum class NoPrice(val value: String) {
        FREE("FREE"), SOLD_OUT("SOLD OUT")
    }

    fun Element.isParagraph(): Boolean {
        return normalName() == "p"
    }

    fun Element.isHeadingTwo(): Boolean {
        return normalName() == "h2"
    }

    fun Element.fromParagraph(): String {
        return tagName("p").ownText()
    }

    fun Element.fromStrong(): String {
        return tagName("p").getElementsByTag("strong").text()
    }

    fun Element.fromItalicized(): String {
        return tagName("p").getElementsByTag("em").text()
    }
}