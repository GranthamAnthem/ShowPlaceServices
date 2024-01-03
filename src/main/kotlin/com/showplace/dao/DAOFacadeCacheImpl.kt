package com.showplace.dao

import com.showplace.model.*
import com.showplace.util.shouldUpdateNewShows
import com.showplace.webScraper.getShowsFromWeb
import kotlinx.datetime.*
import org.ehcache.PersistentCacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.EntryUnit
import org.ehcache.config.units.MemoryUnit
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration
import java.io.File

class DAOFacadeCacheImpl(
    private val delegate: DAOFacade,
    storagePath: File
) : DAOFacade {

    enum class CacheName(val value: String) {
        BAND("bandCache"), VENUE("venueCache"), SHOW("showCache")
    }

    private val cacheManager = cacheBuilder(storagePath)

    private val bandCache = cacheManager.getCache(CacheName.BAND.value, Int::class.javaObjectType, Band::class.java)
    private val venueCache = cacheManager.getCache(CacheName.VENUE.value, Int::class.javaObjectType, Venue::class.java)
    private val showCache = cacheManager.getCache(CacheName.SHOW.value, Int::class.javaObjectType, Show::class.java)

    override suspend fun addAllBands(bands: List<Band>) {
        delegate.addAllBands(bands)
    }

    override suspend fun addAllVenues(venues: List<Venue>) {
        delegate.addAllVenues(venues)
    }

    override suspend fun addAllShows(shows: List<Show>) {
        delegate.addAllShows(shows)
    }

    override suspend fun getAllBands(): List<Band> {
        return delegate.getAllBands()
    }

    override suspend fun getAllVenues(): List<Venue> {
        return delegate.getAllVenues()
    }

    override suspend fun getAllShows(): List<Show> {
        var shows = delegate.getAllShows()
        if (shouldUpdateNewShows(shows.last())) {
            addAllShows(getShowsFromWeb())
            shows = delegate.getAllShows()
        }
        return shows
    }
    override suspend fun getLatestShow(): Show? {
        return delegate.getLatestShow()
    }

    override suspend fun getAllShowsFromToday(page: Long): List<Show> {
        return delegate.getAllShowsFromToday(page)
    }

    override suspend fun updateShow(show: Show) {
        delegate.updateShow(show)
    }

    override suspend fun addShow(show: Show) {
        delegate.addShow(show)
    }

    override suspend fun getShowById(id: Int): Show? {
        return showCache[id]
            ?: delegate.getShowById(id)
                .also { show -> showCache.put(id, show) }
    }

    override suspend fun deleteShowById(id: Int): Boolean {
        showCache.remove(id)
        return delegate.deleteShowById(id)
    }

    override suspend fun getBand(id: Int): Band? {
        return bandCache[id]
            ?: delegate.getBand(id)
                .also { band -> bandCache.put(id, band) }
    }

    override suspend fun getVenue(id: Int): Venue? {
        return venueCache[id]
            ?: delegate.getVenue(id)
                .also { venue -> venueCache.put(id, venue) }
    }

    private fun cacheBuilder(storagePath: File): PersistentCacheManager {
        return CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerPersistenceConfiguration(storagePath))
            .withCache(
                CacheName.BAND.value,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    Int::class.javaObjectType,
                    Band::class.java,
                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(1000, EntryUnit.ENTRIES)
                        .offheap(10, MemoryUnit.MB)
                        .disk(100, MemoryUnit.MB, true)
                )
            )
            .withCache(
                CacheName.VENUE.value,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    Int::class.javaObjectType,
                    Venue::class.java,
                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(1000, EntryUnit.ENTRIES)
                        .offheap(10, MemoryUnit.MB)
                        .disk(100, MemoryUnit.MB, true)
                )
            )
            .withCache(
                CacheName.SHOW.value,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    Int::class.javaObjectType,
                    Show::class.java,
                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(1000, EntryUnit.ENTRIES)
                        .offheap(10, MemoryUnit.MB)
                        .disk(100, MemoryUnit.MB, true)
                )
            )
            .build(true)
    }
}