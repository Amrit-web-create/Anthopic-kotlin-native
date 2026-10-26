package com.customapplock.studio.service

import com.customapplock.studio.domain.RelockMode
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/** In-memory unlock state. unlocked[pkg] = 0 while inside the app, otherwise the time we left it. */
@Singleton
class SessionManager @Inject constructor() {
    private val unlocked = ConcurrentHashMap<String, Long>()
    private val failures = ConcurrentHashMap<String, Int>()
    private val lockedUntil = ConcurrentHashMap<String, Long>()

    @Volatile var lockShowingFor: String? = null
    @Volatile var lockShowingSince: Long = 0L

    val masterUnlocked = MutableStateFlow(false)
    @Volatile private var masterLeftAt = 0L

    fun markUnlocked(pkg: String) { unlocked[pkg] = 0L }

    fun onLeave(pkg: String, mode: RelockMode) {
        if (!unlocked.containsKey(pkg)) return
        if (mode == RelockMode.IMMEDIATE) unlocked.remove(pkg) else unlocked[pkg] = System.currentTimeMillis()
    }

    fun isValid(pkg: String, mode: RelockMode, graceMs: Long): Boolean {
        val left = unlocked[pkg] ?: return false
        if (left == 0L) return true
        val ok = when (mode) {
            RelockMode.SCREEN_OFF -> true
            RelockMode.GRACE -> System.currentTimeMillis() - left <= graceMs
            RelockMode.IMMEDIATE -> false
        }
        if (ok) unlocked[pkg] = 0L else unlocked.remove(pkg)
        return ok
    }

    /** Relock everything right now. */
    fun clearAll() { unlocked.clear() }

    // ---- brute-force protection: every 5 wrong tries the lock freezes, and the freeze doubles ----
    fun remainingMs(pkg: String): Long {
        val until = lockedUntil[pkg] ?: return 0L
        val r = until - System.currentTimeMillis()
        return if (r > 0L) r else 0L
    }

    /** Returns the total number of failures so far. */
    fun recordFailure(pkg: String): Int {
        val n = (failures[pkg] ?: 0) + 1
        failures[pkg] = n
        if (n % 5 == 0) {
            val step = (n / 5 - 1).coerceAtMost(5)
            val duration = (30_000L shl step).coerceAtMost(900_000L)
            lockedUntil[pkg] = System.currentTimeMillis() + duration
        }
        return n
    }

    fun failuresOf(pkg: String): Int = failures[pkg] ?: 0

    fun clearFailures(pkg: String) {
        failures.remove(pkg)
        lockedUntil.remove(pkg)
    }

    // ---- master (in-app) gate ----
    fun setMasterUnlocked() { masterUnlocked.value = true; masterLeftAt = 0L }
    fun markMasterLeft() { if (masterUnlocked.value) masterLeftAt = System.currentTimeMillis() }
    fun checkMasterTimeout(timeoutMs: Long) {
        if (masterLeftAt != 0L && System.currentTimeMillis() - masterLeftAt > timeoutMs) {
            masterUnlocked.value = false
        }
        masterLeftAt = 0L
    }
}
