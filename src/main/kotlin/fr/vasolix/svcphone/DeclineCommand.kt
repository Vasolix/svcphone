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
            sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.")
            return true
        }

        val player: Player = sender
        val playerId: UUID = player.uniqueId

        // 1. Vérifier si le joueur a un appel en attente ou accepté
        if (!callManager.hasCall(playerId)) {
            player.sendMessage("Vous n'avez aucun appel entrant à refuser.")
            return true
        }

        // 2. Vérifier si l'appel est déjà actif
        if (callManager.hasActiveCall(playerId)) {
            player.sendMessage("Cet appel a déjà été accepté. Utilisez /hangup pour raccrocher.")
            return true
        }

        // 3. L'appel est en attente, récupérer l'autre participant
        val otherId: UUID? = callManager.getOtherParticipant(playerId)
        if (otherId == null) {
            player.sendMessage("Vous n'avez aucun appel entrant à refuser.")
            return true
        }

        // 4. Notifier l'autre joueur s'il est en ligne
        val otherPlayer: Player? = player.server.getPlayer(otherId)
        otherPlayer?.takeIf { it.isOnline }?.sendMessage("${player.name} a refusé votre appel.")

        // 5. Arrêter la sonnerie du joueur qui refuse l'appel
        ringtonePlayer.stopRingtone(player)

        // 6. Terminer l'appel pour les deux participants
        callManager.endCall(playerId, otherId)

        player.sendMessage("Vous avez refusé l'appel.")
        return true
    }
}
