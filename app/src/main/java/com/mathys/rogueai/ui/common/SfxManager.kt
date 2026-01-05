package com.mathys.rogueai.ui.common

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/**
 * SfxManager
 *
 * Classe responsable de la gestion des sons du jeu :
 * - Effets sonores courts (SoundPool)
 * - Musique de fond en boucle (MediaPlayer)
 *
 * À instancier une seule fois (ex: au niveau du Game / ViewModel / Activity)
 * et à libérer explicitement pour éviter les fuites mémoire.
 */
class SfxManager(private val context: Context) {

    /** SoundPool utilisé pour les effets sonores courts (latence faible) */
    private var soundPool: SoundPool? = null

    /** MediaPlayer utilisé pour la musique de fond */
    private var backgroundMusic: MediaPlayer? = null

    /**
     * Map liant une clé logique (ex: PLAYER_JOINED)
     * à l'identifiant SoundPool correspondant
     */
    private val soundIds = mutableMapOf<String, Int>()

    /**
     * Clés des sons utilisables dans le jeu.
     * Elles correspondent aux fichiers présents dans res/raw.
     */
    companion object {
        const val PLAYER_JOINED = "player_joined"
        const val PLAYER_READY = "player_ready"
        const val GAME_START = "game_start"
        const val CORRECT_ACTION = "correct_action"
        const val WRONG_ACTION = "wrong_action"
        const val VICTORY = "victory"
        const val DEFEAT = "defeat"
    }

    /**
     * Initialisation du SoundPool avec des paramètres adaptés à un jeu :
     * - Usage GAME
     * - Sons de type "sonification"
     * - Nombre maximum de sons joués simultanément
     */
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

    /**
     * Charge tous les effets sonores depuis le dossier res/raw.
     *
     * Cette méthode doit être appelée une seule fois
     * (par exemple au lancement de la partie).
     */
    fun loadSounds() {
        try {
            // Association clé logique -> nom de ressource
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
                // Récupération dynamique de l'ID de la ressource
                val resId = context.resources
                    .getIdentifier(resourceName, "raw", context.packageName)

                // Chargement du son uniquement si la ressource existe
                if (resId != 0) {
                    soundPool?.let { pool ->
                        soundIds[key] = pool.load(context, resId, 1)
                    }
                }
            }
        } catch (e: Exception) {
            // Sécurité pour éviter un crash en cas d'erreur audio
            e.printStackTrace()
        }
    }

    /**
     * Joue un effet sonore précédemment chargé.
     *
     * @param soundName clé du son (ex: PLAYER_JOINED)
     * @param volume volume du son (0.0f à 1.0f)
     */
    fun playSound(soundName: String, volume: Float = 1f) {
        soundIds[soundName]?.let { soundId ->
            soundPool?.play(soundId, volume, volume, 1, 0, 1f)
        }
    }

    /**
     * Lance la musique de fond en boucle.
     *
     * Si une musique est déjà en cours, elle est arrêtée et recréée.
     */
    fun startBackgroundMusic(volume: Float = 0.3f) {
        try {
            val resId = context.resources
                .getIdentifier("background_music", "raw", context.packageName)

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

    /**
     * Arrête complètement la musique de fond
     * et libère les ressources associées.
     */
    fun stopBackgroundMusic() {
        backgroundMusic?.apply {
            stop()
            release()
        }
        backgroundMusic = null
    }

    /** Met la musique de fond en pause (ex: app en arrière-plan) */
    fun pauseBackgroundMusic() {
        backgroundMusic?.pause()
    }

    /** Reprend la musique de fond après une pause */
    fun resumeBackgroundMusic() {
        backgroundMusic?.start()
    }

    /**
     * Modifie le volume de la musique de fond en temps réel.
     */
    fun setMusicVolume(volume: Float) {
        backgroundMusic?.setVolume(volume, volume)
    }

    /**
     * Libère toutes les ressources audio.
     *
     * À appeler impérativement lors de la destruction
     * du jeu / de l'écran pour éviter les fuites mémoire.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        stopBackgroundMusic()
    }
}