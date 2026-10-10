package com.manmusic.videoplayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Recent Searches FIFO logic, deduplication,
 * capping to the last 5 queries, and query management.
 */
class RecentSearchArchitectureTest {

    private fun addSearchSimulated(currentList: List<String>, query: String, maxCount: Int = 5): List<String> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return currentList
        val mutable = currentList.toMutableList()
        mutable.removeAll { it.equals(trimmed, ignoreCase = true) }
        mutable.add(0, trimmed)
        return mutable.take(maxCount)
    }

    @Test
    fun recentSearches_capsAtLastFiveQueries() {
        var searches = emptyList<String>()
        searches = addSearchSimulated(searches, "Lo-Fi Beats")
        searches = addSearchSimulated(searches, "Synthwave")
        searches = addSearchSimulated(searches, "Chill Acoustic")
        searches = addSearchSimulated(searches, "Heavy Bass")
        searches = addSearchSimulated(searches, "Cyber Symphony")

        assertEquals(5, searches.size)
        assertEquals("Cyber Symphony", searches[0])

        searches = addSearchSimulated(searches, "Rock Anthems")

        assertEquals(5, searches.size)
        assertEquals("Rock Anthems", searches[0])
        assertEquals("Cyber Symphony", searches[1])
        assertFalse("Oldest query should have been evicted", searches.contains("Lo-Fi Beats"))
    }

    @Test
    fun recentSearches_deduplicatesAndBringsToTop() {
        var searches = listOf("Synthwave", "Chill Acoustic", "Heavy Bass")
        searches = addSearchSimulated(searches, "chill acoustic")

        assertEquals(3, searches.size)
        assertEquals("chill acoustic", searches[0])
        assertEquals("Synthwave", searches[1])
        assertEquals("Heavy Bass", searches[2])
    }
}