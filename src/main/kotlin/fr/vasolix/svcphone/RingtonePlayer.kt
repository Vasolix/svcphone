package fr.vasolix.svcphone

import org.bukkit.Instrument
import org.bukkit.Note
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class RingtonePlayer(private val plugin: JavaPlugin) {

    private val melodyNotes = arrayOf(
        Note.natural(0, Note.Tone.E),   // E♭
        Note.natural(0, Note.Tone.B),   // B♭
        Note.natural(0, Note.Tone.G),   // G
        Note.natural(0, Note.Tone.B),   // B♭
        Note.natural(0, Note.Tone.E),   // E♭
        Note.natural(0, Note.Tone.B)    // B♭
    )

    private val delays = intArrayOf(
        11, // E♭ (noire)
        6,  // B♭ (croche)
        11, // G (noire)
        6,  // B♭ (croche)
        11, // E♭ (noire)
        11  // B♭ (noire)
    )

    private val activeRingtones = mutableMapOf<Player, BukkitRunnable>()

    fun playRingtone(target: Player) {
        if (activeRingtones.containsKey(target)) return // Sonnerie déjà en cours

        val ringtoneTask = object : BukkitRunnable() {
            private var index = 0
            private var elapsedTicks = 0
            private var repeatCount = 0

            override fun run() {
                // Arrêt après 30 secondes (600 ticks)
                if (repeatCount >= (30 * 20) / getTotalDelayTicks()) {
                    stopRingtone(target)
                    cancel()
                    return
                }

                // Jouer la note de la mélodie
                if (elapsedTicks == 0 && index < melodyNotes.size) {
                    target.playNote(target.location, Instrument.PIANO, melodyNotes[index])
                }

                elapsedTicks++
                if (elapsedTicks >= delays[index]) {
                    elapsedTicks = 0
                    index++
                }

                // Réinitialisation de la séquence si terminée
                if (index >= melodyNotes.size) {
                    index = 0
                    repeatCount++
                }
            }

            private fun getTotalDelayTicks(): Int = delays.sum()
        }

        activeRingtones[target] = ringtoneTask
        ringtoneTask.runTaskTimer(plugin, 0L, 1L)
    }

    fun stopRingtone(target: Player) {
        activeRingtones.remove(target)?.cancel()
    }
}
