package fr.vasolix.svcphone

import de.maxhenkel.voicechat.api.BukkitVoicechatService
import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatServerApi
import org.bukkit.plugin.java.JavaPlugin

class SimplePhoneCallPlugin : JavaPlugin() {

    override fun onEnable() {
        logger.info("SimplePhoneCall plugin enabled!")

        // Initialisation du CallManager
        val callManager = CallManager()

        val blockManager = BlockListManager(this)

        // Initialisation du RingtonePlayer
        val ringtonePlayer = RingtonePlayer(this)

        // Récupération du service BukkitVoicechat
        val service = server.servicesManager.load(BukkitVoicechatService::class.java)

        if (service != null) {
            // Enregistrement du plugin pour Simple Voice Chat
            service.registerPlugin(object : SimplePhoneCallVoicePlugin() {
                override fun initialize(api: VoicechatApi) {
                    logger.info("Voicechat API initialized!")

                    // Vérification et récupération de l'API serveur
                    val serverApi = api as? VoicechatServerApi
                    if (serverApi != null) {
                        logger.info("Registering commands...")
                        getCommand("call")?.setExecutor(CallCommand(callManager, ringtonePlayer, blockManager))
                        getCommand("answer")?.setExecutor(AnswerCommand(callManager, serverApi, ringtonePlayer))
                        getCommand("decline")?.setExecutor(DeclineCommand(callManager, ringtonePlayer))
                        getCommand("hangup")?.setExecutor(HangupCommand(callManager, serverApi))
                        getCommand("blocklist")?.setExecutor(BlockListCommand(blockManager))
                        getCommand("block")?.setExecutor(BlockCommand(blockManager))
                        getCommand("unblock")?.setExecutor(UnBlockCommand(blockManager))
                        logger.info("Commands registered successfully!")
                    } else {
                        logger.warning("Failed to cast VoicechatApi to VoicechatServerApi!")
                    }
                }

                override fun getPluginId(): String {
                    return "simplephonecall"
                }
            })

            logger.info("Successfully registered SimplePhoneCallVoicePlugin with Simple Voice Chat!")
        } else {
            logger.warning("Simple Voice Chat plugin not found! Make sure it's installed.")
            server.pluginManager.disablePlugin(this) // Désactive le plugin en toute sécurité
        }
    }

    override fun onDisable() {
        logger.info("SimplePhoneCall plugin disabled!")
    }
}
