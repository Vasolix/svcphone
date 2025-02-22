package fr.vasolix.svcphone

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class CallManager {
    /**
     * Contient les informations sur un appel pour un joueur :
     * - avec qui il est en appel (`otherId`),
     * - si l'appel a été accepté (`isAccepted`).
     */
    private class CallInfo(val otherId: UUID, var isAccepted: Boolean)

    // Associe chaque UUID de joueur à ses informations d'appel
    private val calls: MutableMap<UUID, CallInfo> = HashMap()

    // Suivi des tâches programmées pour l'expiration des appels
    private val callTimeoutTasks: MutableMap<UUID, Int> = HashMap()

    /**
     * Envoie une demande d'appel de `caller` à `target`. Cet appel est initialement "non accepté".
     */
    fun sendCallRequest(caller: Player, target: Player, timeoutCallback: Runnable) {
        val callerId = caller.uniqueId
        val targetId = target.uniqueId

        // Marquer les deux joueurs comme ayant un appel en attente (accepted=false)
        calls[callerId] = CallInfo(targetId, false)
        calls[targetId] = CallInfo(callerId, false)

        // Récupérer la référence du plugin et vérifier si elle est valide
        val plugin = Bukkit.getPluginManager().getPlugin("SimplePhoneCall")
        if (plugin == null) {
            Bukkit.getLogger().severe("Le plugin SimplePhoneCall est introuvable !")
            return
        }

        // Planifier un délai d'expiration de 20 secondes
        val taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(
            plugin,
            {
                // Vérifier si la cible a toujours un appel en attente de l'appelant
                val info = calls[targetId]
                if (info != null && info.otherId == callerId && !info.isAccepted) {
                    // L'appel n'a pas été accepté dans les 20 secondes, on l'annule
                    endCall(callerId, targetId)
                    timeoutCallback.run() // Exécuter l'action d'expiration si elle est définie
                } else {
                    Bukkit.getLogger().info("Appel accepté ou plus en attente.")
                }
            },
            20L * 20 // 20 secondes en ticks
        )

        callTimeoutTasks[targetId] = taskId
    }

    /**
     * Accepte l'appel pour les deux participants (il devient alors "actif").
     */
    fun acceptCall(callerId: UUID, targetId: UUID) {
        // Si l'une des deux entrées existe, marquer comme accepté
        if (calls.containsKey(callerId)) {
            calls[callerId]!!.isAccepted = true
        }
        if (calls.containsKey(targetId)) {
            calls[targetId]!!.isAccepted = true
        }

        // Annuler les tâches d'expiration si elles existent encore
        val taskA = callTimeoutTasks.remove(callerId)
        if (taskA != null) {
            Bukkit.getScheduler().cancelTask(taskA)
        }
        val taskB = callTimeoutTasks.remove(targetId)
        if (taskB != null) {
            Bukkit.getScheduler().cancelTask(taskB)
        }
    }

    /**
     * Retourne vrai si le joueur a *un appel en attente ou actif*.
     */
    fun hasCall(playerId: UUID): Boolean {
        return calls.containsKey(playerId)
    }

    /**
     * Retourne vrai si le joueur a *un appel actif* (accepté).
     */
    fun hasActiveCall(playerId: UUID): Boolean {
        val info = calls[playerId]
        return (info != null && info.isAccepted)
    }

    /**
     * Retourne l'UUID de l'autre participant de l'appel du joueur (ou null s'il n'en a pas).
     */
    fun getOtherParticipant(playerId: UUID): UUID? {
        val info = calls[playerId]
        return info?.otherId
    }

    /**
     * Termine l'appel pour les deux joueurs (supprime de la liste et annule les délais d'expiration).
     */
    fun endCall(a: UUID, b: UUID) {
        calls.remove(a)
        calls.remove(b)

        val taskA = callTimeoutTasks.remove(a)
        if (taskA != null) {
            Bukkit.getScheduler().cancelTask(taskA)
        }
        val taskB = callTimeoutTasks.remove(b)
        if (taskB != null) {
            Bukkit.getScheduler().cancelTask(taskB)
        }
    }
}
