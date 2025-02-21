package fr.vasolix.svcphone

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class DeclineCommand(
    private val callManager: CallManager,
    private val ringtonePlayer: RingtonePlayer
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return true
        }

        val player: Player = sender
        val playerId: UUID = player.uniqueId

        // 1. Vérifier si le joueur a un appel en attente ou accepté
        if (!callManager.hasCall(playerId)) {
            player.sendMessage("You don't have any incoming calls to decline.")
            return true
        }

        // 2. Vérifier si l'appel est déjà actif
        if (callManager.hasActiveCall(playerId)) {
            player.sendMessage("That call has already been accepted. Use /hangup instead.")
            return true
        }

        // 3. L'appel est en attente, récupérer l'autre participant
        val otherId: UUID? = callManager.getOtherParticipant(playerId)
        if (otherId == null) {
            player.sendMessage("You don't have any incoming calls to decline.")
            return true
        }

        // 4. Notifier l'autre joueur s'il est en ligne
        val otherPlayer: Player? = player.server.getPlayer(otherId)
        otherPlayer?.takeIf { it.isOnline }?.sendMessage("${player.name} declined your call.")

        // 5. Arrêter la sonnerie du joueur qui refuse l'appel
        ringtonePlayer.stopRingtone(player)

        // 6. Terminer l'appel pour les deux participants
        callManager.endCall(playerId, otherId)

        player.sendMessage("You declined the call.")
        return true
    }
}
