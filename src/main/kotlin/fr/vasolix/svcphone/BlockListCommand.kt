package fr.vasolix.svcphone

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BlockListCommand(private val blockListManager: BlockListManager) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.")
            return true
        }

        val blockedPlayers = blockListManager.getBlockedPlayers(sender.uniqueId)
        if (blockedPlayers.isEmpty()) {
            sender.sendMessage("Votre liste de blocage est vide.")
        } else {
            sender.sendMessage("Joueurs bloqués : ${blockedPlayers.joinToString { Bukkit.getOfflinePlayer(it).name ?: "Inconnu" }}")
        }
        return true
    }
}
