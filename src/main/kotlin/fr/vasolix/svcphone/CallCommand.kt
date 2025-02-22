package fr.vasolix.svcphone

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CallCommand(
    private val callManager: CallManager,
    private val ringtonePlayer: RingtonePlayer,
    private val blockListManager: BlockListManager
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.")
            return true
        }

        val caller: Player = sender

        if (args.isEmpty()) {
            caller.sendMessage("Utilisation : /call <nomdujoueur>")
            return true
        }

        val target: Player? = Bukkit.getPlayer(args[0])

        if (target == null || !target.isOnline) {
            caller.sendMessage("Le joueur '${args[0]}' n'est pas en ligne.")
            return true
        }

        if (blockListManager.isBlocked(caller.uniqueId, target.uniqueId)) {
            caller.sendMessage("Vous ne pouvez pas appeler ce joueur car vous l'avez bloqué.")
            return true
        }

        if (blockListManager.isBlocked(target.uniqueId, caller.uniqueId)) {
            caller.sendMessage("Ce joueur vous a bloqué. Vous ne pouvez pas l'appeler.")
            return true
        }

        if (callManager.hasActiveCall(target.uniqueId)) {
            caller.sendMessage("Ce joueur est déjà en train de recevoir un appel.")
            return true
        }

        if (callManager.hasActiveCall(caller.uniqueId)) {
            caller.sendMessage("Vous êtes déjà en communication !")
            return true
        }

        // Gérer la demande d'appel avec un timeout
        callManager.sendCallRequest(caller, target) {
            caller.sendMessage("${target.name} n'est pas disponible.")
            ringtonePlayer.stopRingtone(target) // Arrêter la sonnerie si l'appel n'est pas accepté
        }

        target.sendMessage("${caller.name} vous appelle... Tapez /answer pour accepter ou /decline pour refuser.")
        caller.sendMessage("Appel en cours vers ${target.name}...")

        try {
            // Jouer la sonnerie pour le destinataire
            ringtonePlayer.playRingtone(target)
        } catch (e: Exception) {
            caller.sendMessage("Une erreur est survenue lors de la lecture de la sonnerie.")
            e.printStackTrace()
        }

        return true
    }
}
