package fr.vasolix.svcphone

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.UUID

class BlockListManager(plugin: JavaPlugin) {

    private val blocklistFile: File = File(plugin.dataFolder, "blocklist.yml")
    private val config: YamlConfiguration = YamlConfiguration.loadConfiguration(blocklistFile)

    init {
        if (!blocklistFile.exists()) {
            plugin.saveResource("blocklist.yml", false)
        }
    }

    fun blockPlayer(blocker: UUID, blocked: UUID) {
        if (blocker == blocked) return
        val blockedList = config.getStringList(blocker.toString()).toMutableList()
        if (!blockedList.contains(blocked.toString())) {
            blockedList.add(blocked.toString())
            config.set(blocker.toString(), blockedList)
            save()
        }
    }

    fun unblockPlayer(blocker: UUID, blocked: UUID) {
        val blockedList = config.getStringList(blocker.toString()).toMutableList()
        if (blockedList.remove(blocked.toString())) {
            config.set(blocker.toString(), blockedList)
            save()
        }
    }

    fun isBlocked(blocker: UUID, blocked: UUID): Boolean {
        return config.getStringList(blocker.toString()).contains(blocked.toString())
    }

    fun getBlockedPlayers(blocker: UUID): List<UUID> {
        return config.getStringList(blocker.toString()).map { UUID.fromString(it) }
    }

    private fun save() {
        config.save(blocklistFile)
    }
}