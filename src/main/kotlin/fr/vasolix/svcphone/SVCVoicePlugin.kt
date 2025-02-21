package fr.vasolix.svcphone

import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatPlugin

open class SVCVoicePlugin : VoicechatPlugin {

    private var voicechatApi: VoicechatApi? = null

    override fun getPluginId(): String {
        return "svcphone"
    }

    override fun initialize(api: VoicechatApi) {
        voicechatApi = api

        println("SVCPlugin initialisé avec l'API !")
    }

    fun getApi(): VoicechatApi? {
        return voicechatApi
    }

}