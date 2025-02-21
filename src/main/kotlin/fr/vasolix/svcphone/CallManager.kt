package fr.vasolix.svcphone

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class CallManager {
    /**
     * Holds information about a call for a single player:
     * - who they're in a call with (`otherId`),
     * - whether it's accepted (`accepted`).
     */
    private class CallInfo(val otherId: UUID, var isAccepted: Boolean)

    // We'll map each player's UUID to their CallInfo
    private val calls: MutableMap<UUID, CallInfo> = HashMap()

    // Keep track of scheduled tasks for call timeouts
    private val callTimeoutTasks: MutableMap<UUID, Int> = HashMap()

    /**
     * Send a call request from `caller` to `target`. This is initially "not accepted."
     */
    fun sendCallRequest(caller: Player, target: Player, timeoutCallback: Runnable) {
        val callerId = caller.uniqueId
        val targetId = target.uniqueId

        // Mark both as having a pending call (accepted=false)
        calls[callerId] = CallInfo(targetId, false)
        calls[targetId] = CallInfo(callerId, false)

        // Get the plugin reference and check if it's null
        val plugin = Bukkit.getPluginManager().getPlugin("SimplePhoneCall")
        if (plugin == null) {
            Bukkit.getLogger().severe("SimplePhoneCall plugin not found!")
            return
        }

        // Schedule a 20-second timeout
        val taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(
            plugin,
            {
                // Check if the target still has a pending call from the caller
                val info = calls[targetId]
                if (info != null && info.otherId == callerId && !info.isAccepted) {
                    // The call was never accepted within 20s, end it
                    endCall(callerId, targetId)
                    timeoutCallback.run() // Only run if it's not null
                } else {
                    Bukkit.getLogger().info("Call accepted or no longer pending.")
                }
            },
            20L * 20 // 20 seconds in ticks
        )

        callTimeoutTasks[targetId] = taskId
    }

    /**
     * Accept the call for both sides (meaning it's now "active").
     */
    fun acceptCall(callerId: UUID, targetId: UUID) {
        // If either side has an entry, mark it accepted
        if (calls.containsKey(callerId)) {
            calls[callerId]!!.isAccepted = true
        }
        if (calls.containsKey(targetId)) {
            calls[targetId]!!.isAccepted = true
        }

        // Cancel any timeout tasks if they're still running
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
     * True if this player has *any* call entry (pending or accepted).
     */
    fun hasCall(playerId: UUID): Boolean {
        return calls.containsKey(playerId)
    }

    /**
     * True if this player has a call *and* it has been accepted.
     */
    fun hasActiveCall(playerId: UUID): Boolean {
        val info = calls[playerId]
        return (info != null && info.isAccepted)
    }

    /**
     * Returns the UUID of the other participant in the player's call (or null if none).
     */
    fun getOtherParticipant(playerId: UUID): UUID? {
        val info = calls[playerId]
        return info?.otherId
    }

    /**
     * End the call for both players (removes from map and cancels timeouts).
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
