package fr.vasolix.svcphone

import de.maxhenkel.voicechat.api.VoicechatServerApi
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class HangupCommand(
    private val callManager: CallManager,
    private val serverApi: VoicechatServerApi
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.")
            return true
        }

        val player: Player = sender
        val playerId: UUID = player.uniqueId

        // 1. Vérifier si le joueur est dans un appel (accepté ou en attente)
        if (!callManager.hasCall(playerId)) {
            player.sendMessage("Vous n'êtes dans aucun appel.")
            return true
        }

        // 2. Trouver l'autre participant
        val otherId: UUID? = callManager.getOtherParticipant(playerId)
        if (otherId == null) {
            player.sendMessage("Impossible de trouver l'autre participant.")
            return true
        }

        // 3. Notifier l'autre participant s'il est en ligne
        val otherPlayer: Player? = player.server.getPlayer(otherId)
        otherPlayer?.takeIf { it.isOnline }?.sendMessage("${player.name} a raccroché l'appel.")

        // 4. Terminer l'appel des deux côtés
        callManager.endCall(playerId, otherId)

        // 5. Retirer les joueurs du groupe vocal s'ils y sont
        serverApi.getConnectionOf(playerId)?.group = null
        serverApi.getConnectionOf(otherId)?.group = null

        player.sendMessage("Vous avez raccroché l'appel.")
        return true
    }
}
