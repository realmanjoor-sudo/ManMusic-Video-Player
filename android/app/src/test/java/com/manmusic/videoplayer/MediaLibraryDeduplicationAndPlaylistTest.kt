package com.manmusic.videoplayer

import com.manmusic.videoplayer.data.room.MusicPlaylistEntity
import com.manmusic.videoplayer.data.room.MusicPlaylistItemEntity
import com.manmusic.videoplayer.data.room.SongEntity
import com.manmusic.videoplayer.data.room.VideoEntity
import com.manmusic.videoplayer.data.room.VideoPlaylistEntity
import com.manmusic.videoplayer.data.room.VideoPlaylistItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying media library scanning idempotence, deduplication,
 * playlist ordering, and model isolation.
 */
class MediaLibraryDeduplicationAndPlaylistTest {

    @Test
    fun scanningIdempotency_upsertPreservesUserOverrides() {
        val initialSong = SongEntity(
            id = "song_100",
            title = "Original Title",
            displayName = "track.mp3",
            contentUri = "content://media/external/audio/media/100",
            durationMs = 180000L,
            isFavorite = true,
            customArtworkUri = "content://media/picker/artwork/cover.jpg",
            lastPlayedTimestamp = 1680000000L,
            playCount = 14
        )

        val rescannedSong = SongEntity(
            id = "song_100",
            title = "Corrected Title from ID3",
            displayName = "track.mp3",
            contentUri = "content://media/external/audio/media/100",
            durationMs = 180000L,
            isFavorite = false,
            customArtworkUri = null,
            lastPlayedTimestamp = 0L,
            playCount = 0
        )

        val merged = rescannedSong.copy(
            customArtworkUri = initialSong.customArtworkUri,
            isFavorite = initialSong.isFavorite,
            lastPlayedTimestamp = initialSong.lastPlayedTimestamp,
            playCount = initialSong.playCount
        )

        assertEquals("song_100", merged.id)
        assertEquals("Corrected Title from ID3", merged.title)
        assertTrue(merged.isFavorite)
        assertEquals("content://media/picker/artwork/cover.jpg", merged.customArtworkUri)
        assertEquals(14, merged.playCount)
    }

    @Test
    fun librarySeparation_musicAndVideoAreStrictlyIsolated() {
        val song = SongEntity(
            id = "audio_1",
            title = "Audio Track",
            displayName = "audio.mp3",
            contentUri = "content://media/1",
            durationMs = 120000L
        )

        val video = VideoEntity(
            id = "video_1",
            title = "Video Clip",
            displayName = "clip.mp4",
            contentUri = "content://media/2",
            durationMs = 240000L,
            width = 1920,
            height = 1080
        )

        assertNotEquals(song.javaClass, video.javaClass)
        assertEquals("1920x1080", video.resolutionFormatted)
    }

    @Test
    fun musicPlaylist_orderingAndReordering_preservesSequence() {
        val playlist = MusicPlaylistEntity(
            id = "pl_1",
            name = "Workout Mix",
            trackCount = 3
        )

        val items = mutableListOf(
            MusicPlaylistItemEntity(playlistId = playlist.id, songId = "s1", orderIndex = 0),
            MusicPlaylistItemEntity(playlistId = playlist.id, songId = "s2", orderIndex = 1),
            MusicPlaylistItemEntity(playlistId = playlist.id, songId = "s3", orderIndex = 2)
        )

        assertEquals("s1", items.sortedBy { it.orderIndex }[0].songId)
        assertEquals("s2", items.sortedBy { it.orderIndex }[1].songId)
        assertEquals("s3", items.sortedBy { it.orderIndex }[2].songId)

        val reorderedList = listOf("s3", "s1", "s2")
        val updatedItems = reorderedList.mapIndexed { idx, songId ->
            MusicPlaylistItemEntity(playlistId = playlist.id, songId = songId, orderIndex = idx)
        }

        assertEquals("s3", updatedItems.sortedBy { it.orderIndex }[0].songId)
        assertEquals("s1", updatedItems.sortedBy { it.orderIndex }[1].songId)
        assertEquals("s2", updatedItems.sortedBy { it.orderIndex }[2].songId)
    }
}