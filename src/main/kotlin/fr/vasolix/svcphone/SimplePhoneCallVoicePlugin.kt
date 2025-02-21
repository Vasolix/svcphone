package fr.vasolix.svcphone

import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatPlugin

open class SimplePhoneCallVoicePlugin : VoicechatPlugin {

    private var voicechatApi: VoicechatApi? = null

    override fun getPluginId(): String {
        return "simplephonecall"
    }

    override fun initialize(api: VoicechatApi) {
        voicechatApi = api

        println("SimplePhoneCallVoicePlugin initialized with API!")
    }

    fun getApi(): VoicechatApi? {
        return voicechatApi
    }

}