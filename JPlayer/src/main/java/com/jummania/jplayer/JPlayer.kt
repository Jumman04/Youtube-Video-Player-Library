package com.jummania.jplayer

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout

/**
 * Custom WebView-based player for handling YouTube videos.
 *
 * This class extends WebView and provides methods to control YouTube video playback.
 * It also includes an interface for handling various player events such as player readiness,
 * playback state changes, receiving video IDs, and error events.
 *
 * Example usage:
 * ```
 * val jPlayer = JPlayer(context)
 * jPlayer.addOnEventListener(object : JPlayer.OnEventListener {
 *     // Implement methods to handle player events
 *     // ...
 * })
 * ```
 *
 * @param context The context in which the player is created.
 * @param attrs An attribute set.
 * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource that supplies default values for the view. Can be 0 to not look for defaults.
 */
class JPlayer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    /**
     * Initializes the WebView settings.
     *
     * This function enables JavaScript and initializes the WebView settings.
     * It also loads the default URL 'file:///android_asset/JPlayer.html' after initialization.
     */
    init {
        settings.javaScriptEnabled = true
        initialize()
    }

    /**
     * Enables or disables autoplay feature.
     *
     * This function allows enabling or disabling autoplay feature for media playback.
     * It sets the `mediaPlaybackRequiresUserGesture` setting based on the provided boolean value.
     *
     * @param boolean `true` to enable autoplay, `false` to disable autoplay.
     */
    fun enableAutoPlaying(boolean: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.mediaPlaybackRequiresUserGesture = !boolean
        }
    }

    /**
     * Initializes the player.
     *
     * This function loads the default URL 'file:///android_asset/JPlayer.html'.
     * It is called during the initialization of the JPlayer instance.
     */
    fun initialize() {
        loadUrl("file:///android_asset/JPlayer.html")
    }


    /**
     * Notifies the listener when the player is ready to play.
     *
     * This function is called when the YouTube player is ready to start playing videos.
     * If a listener is initialized, the `onPlayerReady` callback of the listener is invoked.
     */
    @JavascriptInterface
    fun onPlayerReady() {
        post { listener?.onPlayerReady() }
    }

    /**
     * Notifies the listener when the playback state of the player changes.
     *
     * This function is called when the playback state of the YouTube player changes.
     * If a listener is initialized, the `onPlaybackStateChange` callback of the listener is invoked with the new state.
     *
     * @param state The new playback state. See [JPlayer.Companion] for possible states.
     */
    @JavascriptInterface
    fun onPlaybackStateChange(state: Int) {
        post {
            playbackState = state
            listener?.onPlaybackStateChange(playbackState)
        }
    }

    /**
     * Play the video playback.
     */
    fun play() {
        loadUrl("javascript:player.playVideo();")
    }

    /**
     * Pauses the video playback.
     */
    fun pause() {
        loadUrl("javascript:player.pauseVideo();")
    }

    /**
     * Loads a video by its ID and starts playback.
     *
     * @param videoId The ID of the YouTube video to be loaded.
     */
    fun loadVideo(videoId: String) {
        loadUrl("javascript: player.loadVideoById('$videoId', 0);")
    }

    /**
     * Checks if the player is currently playing a video.
     *
     * @return `true` if the player is playing, `false` otherwise.
     */
    fun isPlaying(): Boolean {
        return playbackState == PLAYING
    }

    private var fullscreenButtonClickListener: FullscreenButtonClickListener? = null
    private var listener: OnEventListener? = null

    /**
     * Interface for handling various player events.
     */
    interface OnEventListener {
        fun onPlayerReady()
        fun onPlaybackStateChange(playerState: Int)
    }

    /**
     * Listener invoked when the fullscreen button is clicked. The implementation is responsible for
     * changing the UI layout.
     */
    interface FullscreenButtonClickListener {
        /**
         * Called when the fullscreen button is clicked.
         *
         * @param isFullScreen `true` if the video rendering surface should be fullscreen, `false` otherwise.
         */
        fun onFullscreenButtonClick(isFullScreen: Boolean)
    }

    /**
     * Adds a listener to handle player events.
     *
     * This function sets the listener to handle player events such as player readiness and playback state changes.
     * It also adds or removes the JavaScript interface based on the presence of the listener.
     *
     * @param listener The listener to be added, or null to remove the current listener.
     */
    fun addOnEventListener(listener: OnEventListener?) {
        this.listener = listener
        // Add or remove JavaScript interface based on the presence of the listener
        if (listener == null) removeJavascriptInterface(javaClass.simpleName)
        else addJavascriptInterface(this, javaClass.simpleName)
    }

    /**
     * Sets the [FullscreenButtonClickListener].
     *
     * This function sets the listener to be notified when the fullscreen button is clicked.
     * If the listener is not null, it creates a [JClient] instance to handle custom view events.
     * If the listener is null, it removes the current listener and hides the fullscreen button.
     *
     * @param listener The listener to be notified when the fullscreen button is clicked, or null to
     * remove the current listener and hide the fullscreen button.
     */
    fun setFullscreenButtonClickListener(listener: FullscreenButtonClickListener?) {
        this.fullscreenButtonClickListener = listener
        // Create a JClient instance if the listener is not null
        // This will handle custom view events related to fullscreen mode
        webChromeClient = if (listener == null) null
        else JClient(context as Activity)
    }

    companion object {
        /**
         * Constant representing the playback state when the video has ended.
         */
        const val ENDED = 0

        /**
         * Constant representing the playback state when the video is playing.
         */
        const val PLAYING = 1

        /**
         * Constant representing the playback state when the video is paused.
         */
        const val PAUSED = 2

        /**
         * Constant representing the playback state when the video is buffering.
         */
        const val BUFFERING = 3

        /**
         * Constant representing the playback state when the video is cued.
         */
        const val CUED = 5
    }

    // Current playback state
    /**
     * Represents the current playback state of the video.
     * Initialized with -1 to indicate no state.
     */
    private var playbackState: Int = -1

    /**
     * Inner class extending WebChromeClient for handling custom view events.
     *
     * This class is responsible for handling custom view events such as entering or exiting fullscreen mode.
     */
    private inner class JClient(val activity: Activity) : WebChromeClient() {
        // Fullscreen view
        var fullscreen: View? = null

        override fun onHideCustomView() {
            // Remove fullscreen view
            if (fullscreen != null) (activity.window.decorView as FrameLayout).removeView(fullscreen)
            // Notify listener
            post { fullscreenButtonClickListener?.onFullscreenButtonClick(false) }
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            // Show fullscreen view
            fullscreen = view
            (activity.window.decorView as FrameLayout).addView(
                fullscreen, FrameLayout.LayoutParams(-1, -1)
            )
            // Notify listener
            post { fullscreenButtonClickListener?.onFullscreenButtonClick(true) }
        }
    }

}
