package com.jummania.youtubevideoplayer

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.jummania.jplayer.JPlayer

/**
 * MainActivity class for the YouTube Video Player app.
 *
 * This activity displays a JPlayer instance for playing YouTube videos.
 */
class MainActivity : AppCompatActivity() {

    // Button instance for obtaining player status
    private lateinit var button: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize JPlayer instance
        val jPlayer: JPlayer = findViewById(R.id.jPlayer)

        // Find and initialize the button view for obtaining player status
        button = findViewById(R.id.button)


        // Enables auto-playing for the jPlayer instance.
        // When set to true, it allows the player to automatically start playing videos without user interaction.
        jPlayer.enableAutoPlaying(true)

        // Checks for any errors, such as jPlayer not being initialized automatically,
        // and initializes the jPlayer manually if necessary.
        // Call jPlayer.initialize() to ensure proper initialization.


        // Add event listener to handle player events
        jPlayer.addOnEventListener(object : JPlayer.OnEventListener {
            override fun onPlayerReady() {
                // Show toast message when the player is ready
                showMessage("JPlayer is ready to play video")

                // Load a YouTube video when the player is ready
                jPlayer.loadVideo("nXvIAJOcCNY")
            }

            override fun onPlaybackStateChange(playerState: Int) {
                // Show playback state changes using Toast
                when (playerState) {
                    JPlayer.ENDED -> showMessage("Video ended")
                    JPlayer.PLAYING -> showMessage("Video playing")
                    JPlayer.PAUSED -> showMessage("Video paused")
                    JPlayer.BUFFERING -> showMessage("Video buffering")
                    JPlayer.CUED -> showMessage("Video cued")
                }
            }
        })

        // Set fullscreen button click listener
        jPlayer.setFullscreenButtonClickListener(object : JPlayer.FullscreenButtonClickListener {
            override fun onFullscreenButtonClick(isFullScreen: Boolean) {
                // Toggle system UI visibility based on fullscreen mode
                if (isFullScreen) hideSystemUI()
                else showSystemUI()
            }
        })

        // Set click listener for the button to toggle player playback
        button.setOnClickListener {
            // Check if the player is currently playing
            if (jPlayer.isPlaying()) {
                // If playing, pause the player
                jPlayer.pause()
            } else {
                // If not playing, start playing the player
                jPlayer.play()
            }
        }

    }

    /**
     * Displays a toast message and sets the button text.
     *
     * @param string The message to be displayed and set as button text.
     */
    private fun showMessage(string: String) {
        // Show toast message with the provided string
        Toast.makeText(this@MainActivity, string, Toast.LENGTH_SHORT).show()

        // Set button text with the provided string
        button.text = string
    }

    /**
     * Hides the system UI (status bar and navigation bar), sets the screen orientation to landscape,
     * and adjusts system window insets behavior.
     */
    private fun hideSystemUI() {
        // Set screen orientation to landscape
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        // Set system windows to not fit system windows to allow full-screen mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Hide system bars
        WindowInsetsControllerCompat(window, findViewById(android.R.id.content)).let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            // Adjust system bars behavior to show transient bars by swipe
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /**
     * Shows the system UI (status bar and navigation bar), sets the screen orientation to portrait,
     * and adjusts system window insets behavior.
     */
    private fun showSystemUI() {
        // Set screen orientation to portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Set system windows to fit system windows
        WindowCompat.setDecorFitsSystemWindows(window, true)
        // Show system bars
        WindowInsetsControllerCompat(window, findViewById(android.R.id.content)).show(
            WindowInsetsCompat.Type.systemBars()
        )
    }

}
