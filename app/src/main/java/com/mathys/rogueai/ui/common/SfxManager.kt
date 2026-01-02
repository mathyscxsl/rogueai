package com.mathys.rogueai.ui.common

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class SfxManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var backgroundMusic: MediaPlayer? = null
    private val soundIds = mutableMapOf<String, Int>()

    companion object {
        const val PLAYER_JOINED = "player_joined"
        const val PLAYER_READY = "player_ready"
        const val GAME_START = "game_start"
        const val CORRECT_ACTION = "correct_action"
        const val WRONG_ACTION = "wrong_action"
        const val VICTORY = "victory"
        const val DEFEAT = "defeat"
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    fun loadSounds() {
        try {
            val resourceMap = mapOf(
                PLAYER_JOINED to "player_joined",
                PLAYER_READY to "player_ready",
                GAME_START to "game_start",
                CORRECT_ACTION to "correct_action",
                WRONG_ACTION to "wrong_action",
                VICTORY to "victory",
                DEFEAT to "defeat"
            )

            resourceMap.forEach { (key, resourceName) ->
                val resId =
                    context.resources.getIdentifier(resourceName, "raw", context.packageName)
                if (resId != 0) {
                    soundPool?.let { pool ->
                        soundIds[key] = pool.load(context, resId, 1)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSound(soundName: String, volume: Float = 1f) {
        soundIds[soundName]?.let { soundId ->
            soundPool?.play(soundId, volume, volume, 1, 0, 1f)
        }
    }

    fun startBackgroundMusic(volume: Float = 0.3f) {
        try {
            val resId =
                context.resources.getIdentifier("background_music", "raw", context.packageName)
            if (resId != 0) {
                backgroundMusic?.release()
                backgroundMusic = MediaPlayer.create(context, resId).apply {
                    isLooping = true
                    setVolume(volume, volume)
                    start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopBackgroundMusic() {
        backgroundMusic?.apply {
            stop()
            release()
        }
        backgroundMusic = null
    }

    fun pauseBackgroundMusic() {
        backgroundMusic?.pause()
    }

    fun resumeBackgroundMusic() {
        backgroundMusic?.start()
    }

    fun setMusicVolume(volume: Float) {
        backgroundMusic?.setVolume(volume, volume)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        stopBackgroundMusic()
    }
}