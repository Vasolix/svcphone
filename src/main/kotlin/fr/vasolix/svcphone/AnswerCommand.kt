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
            sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.")
            return true
        }

        val target: Player = sender
        val targetId: UUID = target.uniqueId

        // Vérifier si le joueur a un appel en attente ou actif
        if (!callManager.hasCall(targetId)) {
            target.sendMessage("Vous n'avez aucun appel entrant à accepter.")
            return true
        }

        // Vérifier si l'appel est déjà accepté
        if (callManager.hasActiveCall(targetId)) {
            target.sendMessage("Cet appel a déjà été accepté. Utilisez /hangup à la place.")
            return true
        }

        // Récupérer l'appelant
        val callerId: UUID? = callManager.getOtherParticipant(targetId)
        if (callerId == null) {
            target.sendMessage("Vous n'avez aucun appel entrant à accepter.")
            return true
        }

        val caller: Player? = target.server.getPlayer(callerId)
        if (caller == null || !caller.isOnline) {
            target.sendMessage("L'appelant n'est plus en ligne.")
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
            .setName("Appel privé : ${caller.name} & ${target.name}")
            .setPassword(password)
            .setPersistent(false)
            .setType(Group.Type.ISOLATED)
            .build()

        // Ajouter les joueurs au groupe vocal
        serverApi.getConnectionOf(caller.uniqueId)?.group = group
        serverApi.getConnectionOf(target.uniqueId)?.group = group

        // Notifier les joueurs
        caller.sendMessage("${target.name} a répondu à l'appel ! Vous êtes maintenant dans un groupe privé.")
        target.sendMessage("Vous avez répondu à l'appel avec ${caller.name} ! Vous êtes maintenant dans un groupe privé.")

        return true
    }
}
