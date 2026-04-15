package com.example.quizzy

import android.content.Context
import android.media.MediaPlayer

object MusicManager {

    private var mediaPlayer: MediaPlayer? = null

    private const val PREFS_NAME = "quizzy_settings"
    private const val KEY_MUSIC_VOLUME = "music_volume"
    private const val KEY_SFX_VOLUME = "sfx_volume"

    // START MUSIC
    fun startMusic(context: Context) {
        if (mediaPlayer == null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedVolume = prefs.getFloat(KEY_MUSIC_VOLUME, 0.2f)

            mediaPlayer = MediaPlayer.create(
                context.applicationContext,
                R.raw.background_music
            )

            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(savedVolume, savedVolume)
        }

        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    // ⏸️ PAUSE
    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    // ▶️ RESUME
    fun resumeMusic() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    // ⛔ STOP
    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // 🎚️ SET MUSIC VOLUME (USED BY SLIDER)
    fun setVolume(context: Context, volume: Float) {
        val safeVolume = volume.coerceIn(0f, 1f)

        // Save
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_MUSIC_VOLUME, safeVolume).apply()

        // Apply instantly
        mediaPlayer?.setVolume(safeVolume, safeVolume)
    }

    // 📥 GET SAVED MUSIC VOLUME
    fun getVolume(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_MUSIC_VOLUME, 0.2f)
    }

    // 🎚️ SET SFX VOLUME (FOR FUTURE SLIDER)
    fun setSFXVolume(context: Context, volume: Float) {
        val safeVolume = volume.coerceIn(0f, 1f)

        // Save
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_SFX_VOLUME, safeVolume).apply()
    }

    // 📥 GET SAVED SFX VOLUME
    fun getSFXVolume(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_SFX_VOLUME, 0.7f)
    }

    // 🔊 CLICK SOUND (SFX)
    fun playClickSound(context: Context) {
        val sound = MediaPlayer.create(context.applicationContext, R.raw.click_sound)

        // Retrieve the saved SFX volume logic
        val sfxVolume = getSFXVolume(context)
        sound.setVolume(sfxVolume, sfxVolume)

        sound.start()

        sound.setOnCompletionListener {
            it.release()
        }
    }
}