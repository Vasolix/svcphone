package fr.vasolix.svcphone

import de.maxhenkel.voicechat.api.Group
import de.maxhenkel.voicechat.api.VoicechatServerApi
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

class AnswerCommand(
    private val callManager: CallManager,
    private val serverApi: VoicechatServerApi,
    private val ringtonePlayer: RingtonePlayer
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return true
        }

        val target: Player = sender
        val targetId: UUID = target.uniqueId

        // Vérifier si le joueur a un appel en attente ou actif
        if (!callManager.hasCall(targetId)) {
            target.sendMessage("You don't have any incoming calls to answer.")
            return true
        }

        // Vérifier si l'appel est déjà accepté
        if (callManager.hasActiveCall(targetId)) {
            target.sendMessage("This call has already been accepted. Use /hangup instead.")
            return true
        }

        // Récupérer l'appelant
        val callerId: UUID? = callManager.getOtherParticipant(targetId)
        if (callerId == null) {
            target.sendMessage("You don't have any incoming calls to answer.")
            return true
        }

        val caller: Player? = target.server.getPlayer(callerId)
        if (caller == null || !caller.isOnline) {
            target.sendMessage("The caller is no longer online.")
            callManager.endCall(targetId, callerId)
            return true
        }

        // Arrêter la sonnerie pour le destinataire
        ringtonePlayer.stopRingtone(target)

        // Accepter l'appel pour les deux joueurs
        callManager.acceptCall(callerId, targetId)

        // Créer un groupe vocal privé
        val password = UUID.randomUUID().toString().substring(0, 8)
        val group: Group = serverApi.groupBuilder()
            .setName("Private Call: ${caller.name} & ${target.name}")
            .setPassword(password)
            .setPersistent(false)
            .setType(Group.Type.ISOLATED)
            .build()

        // Ajouter les joueurs au groupe vocal
        serverApi.getConnectionOf(caller.uniqueId)?.group = group
        serverApi.getConnectionOf(target.uniqueId)?.group = group

        // Notifier les joueurs
        caller.sendMessage("${target.name} answered the call! You are now in a private group.")
        target.sendMessage("You answered the call with ${caller.name}! You are now in a private group.")

        return true
    }
}
