package fr.vasolix.svcphone

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UnBlockCommand(private val blockListManager: BlockListManager) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("Utilisation : /unblock <joueur>")
            return true
        }

        val target = Bukkit.getPlayer(args[0])
        if (target == null || !target.isOnline) {
            sender.sendMessage("Joueur introuvable.")
            return true
        }

        blockListManager.unblockPlayer(sender.uniqueId, target.uniqueId)
        sender.sendMessage("Vous avez débloqué ${target.name}.")
        return true
    }
}
