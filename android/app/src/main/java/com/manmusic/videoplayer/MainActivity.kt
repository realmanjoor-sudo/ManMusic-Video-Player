package com.manmusic.videoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.manmusic.videoplayer.navigation.ManMusicNavGraph
import com.manmusic.videoplayer.ui.screens.music.MusicPlayerViewModel
import com.manmusic.videoplayer.ui.screens.video.VideoPlayerViewModel
import com.manmusic.videoplayer.ui.theme.ManMusicTheme

class MainActivity : ComponentActivity() {

    private val videoViewModel: VideoPlayerViewModel by viewModels {
        val app = application as ManMusicApplication
        VideoPlayerViewModel.Factory(app.videoRepository)
    }

    private val musicViewModel: MusicPlayerViewModel by viewModels {
        val app = application as ManMusicApplication
        MusicPlayerViewModel.Factory(app.audioRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ManMusicTheme {
                ManMusicNavGraph(
                    videoViewModel = videoViewModel,
                    musicViewModel = musicViewModel
                )
            }
        }
    }
}