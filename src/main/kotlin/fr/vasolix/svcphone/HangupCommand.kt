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
            sender.sendMessage("Only players can use this command.")
            return true
        }

        val player: Player = sender
        val playerId: UUID = player.uniqueId

        // 1. Vérifier si le joueur est dans un appel (accepté ou en attente)
        if (!callManager.hasCall(playerId)) {
            player.sendMessage("You are not in a call.")
            return true
        }

        // 2. Trouver l'autre participant
        val otherId: UUID? = callManager.getOtherParticipant(playerId)
        if (otherId == null) {
            player.sendMessage("Could not find the other participant.")
            return true
        }

        // 3. Notifier l'autre participant s'il est en ligne
        val otherPlayer: Player? = player.server.getPlayer(otherId)
        otherPlayer?.takeIf { it.isOnline }?.sendMessage("${player.name} has hung up the call.")

        // 4. Terminer l'appel des deux côtés
        callManager.endCall(playerId, otherId)

        // 5. Retirer les joueurs du groupe vocal s'ils y sont
        serverApi.getConnectionOf(playerId)?.group = null
        serverApi.getConnectionOf(otherId)?.group = null

        player.sendMessage("You hung up the call.")
        return true
    }
}
