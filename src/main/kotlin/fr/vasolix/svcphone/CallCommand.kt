package fr.vasolix.svcphone

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CallCommand(
    private val callManager: CallManager,
    private val ringtonePlayer: RingtonePlayer
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return true
        }

        val caller: Player = sender

        if (args.isEmpty()) {
            caller.sendMessage("Usage: /call <playername>")
            return true
        }

        val target: Player? = Bukkit.getPlayer(args[0])

        if (target == null || !target.isOnline) {
            caller.sendMessage("The player '${args[0]}' is not online.")
            return true
        }

        if (callManager.hasActiveCall(target.uniqueId)) {
            caller.sendMessage("That player is already receiving a call.")
            return true
        }

        if (callManager.hasActiveCall(caller.uniqueId)) {
            caller.sendMessage("You are already in a call!")
            return true
        }

        // Gérer la demande d'appel avec un timeout
        callManager.sendCallRequest(caller, target) {
            caller.sendMessage("${target.name} isn't available.")
            ringtonePlayer.stopRingtone(target) // Arrêter la sonnerie si l'appel n'est pas accepté
        }

        target.sendMessage("${caller.name} is calling you... type /answer to accept or /decline to reject.")
        caller.sendMessage("Calling ${target.name}...")

        try {
            // Jouer la sonnerie pour le destinataire
            ringtonePlayer.playRingtone(target)
        } catch (e: Exception) {
            caller.sendMessage("An error occurred while playing the ringtone.")
            e.printStackTrace()
        }

        return true
    }
}
