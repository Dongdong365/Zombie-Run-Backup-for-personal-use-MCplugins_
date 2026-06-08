package cn.oneachina.zombieRun.manager

import cn.oneachina.zombieRun.ZombieRun
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class StaminaManager(private val plugin: ZombieRun) {

    data class PlayerStamina(
        private var _stamina: Double = 100.0,
        val maxStamina: Double = 100.0,
        var isSprinting: Boolean = false,
        var isMoving: Boolean = false,
        var staminastate: Int = 1,
        var sprintTicks: Int = 0,
        var jumpCount: Int = 0,
        var isExhausted: Boolean = false
    ) {
        var stamina: Double
            get() = _stamina
            set(value) {
                _stamina = value.coerceIn(0.0, maxStamina)
            }

        fun addStamina(amount: Double) {
            _stamina = (_stamina + amount).coerceIn(0.0, maxStamina)
        }

        fun deductStamina(amount: Double) {
            _stamina = (_stamina - amount).coerceAtLeast(0.0)
        }
    }

    private val playerStamina: ConcurrentHashMap<Player, PlayerStamina> = ConcurrentHashMap()
    private val staminaTask: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()
    private val actionBarTask: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()
    private val zombieHealthBarTask: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()

    fun init() {
        startStaminaRegenTask()
        startActionBarTask()
        startStaminaEffectsTask()
        startZombieHealthBarTask()
        plugin.logger.info("体力系统初始化完成")
    }

    fun addPlayer(player: Player) {
        if (!playerStamina.containsKey(player)) {
            playerStamina[player] = PlayerStamina()
        }
    }

    fun removePlayer(player: Player) {
        playerStamina.remove(player)
    }

    fun getStamina(player: Player): Double {
        return playerStamina[player]?.stamina ?: 0.0
    }

    fun getMaxStamina(player: Player): Double {
        return playerStamina[player]?.maxStamina ?: 100.0
    }

    fun setStamina(player: Player, stamina: Double) {
        playerStamina[player]?.stamina = stamina
    }

    fun addStamina(player: Player, amount: Double) {
        playerStamina[player]?.addStamina(amount)
    }

    fun deductStamina(player: Player, amount: Double) {
        playerStamina[player]?.deductStamina(amount)
    }

    fun isStaminaEmpty(player: Player): Boolean {
        return getStamina(player) <= 0.0
    }

    fun canSprintOrJump(player: Player): Boolean {
        val ps = playerStamina[player]
        return ps?.let { !it.isExhausted } ?: true
    }

    fun addJumpCount(player: Player) {
        playerStamina[player]?.let { it.jumpCount++ }
    }

    fun setMoving(player: Player, moving: Boolean) {
        playerStamina[player]?.isMoving = moving
    }

    fun setSprinting(player: Player, sprinting: Boolean) {
        playerStamina[player]?.isSprinting = sprinting
    }

    fun getStaminaState(player: Player): Int {
        return playerStamina[player]?.staminastate ?: 1
    }

    private fun startStaminaRegenTask() {
        val task = object : BukkitRunnable() {
            override fun run() {
                for ((player, ps) in playerStamina) {
                    if (plugin.gameManager.getGameStatus() != GameManager.GameStatus.RUNNING) {
                        continue
                    }

                    val team = plugin.gameManager.getPlayerTeam(player)
                    if (team != GameManager.Team.HUMAN) {
                        continue
                    }

                    if (ps.isExhausted) {
                        // 体力耗尽状态，只有当体力恢复到100时才能解除
                        if (ps.stamina >= 100.0) {
                            ps.isExhausted = false
                            player.sendMessage("§a体力已完全恢复，可以跑跳了！")
                        } else {
                            // 体力耗尽时快速恢复
                            var regen = 1.0
                            if (!ps.isMoving) {
                                regen += 1.0
                            }
                            ps.addStamina(regen)
                        }
                    } else {
                        if (ps.isSprinting) {
                            ps.sprintTicks += 1
                            if (ps.sprintTicks >= 2) {
                                ps.deductStamina(1.0)
                                ps.sprintTicks -= 2
                            }
                        } else {
                            ps.sprintTicks = 0
                            var regen = 0.5
                            if (!ps.isMoving) {
                                regen += 0.5
                            }
                            ps.addStamina(regen)
                        }

                        // 处理跳跃消耗
                        if (ps.jumpCount >= 2) {
                            ps.deductStamina(1.0)
                            ps.jumpCount = 0
                        }

                        // 检查是否体力耗尽
                        if (ps.stamina <= 0) {
                            ps.isExhausted = true
                            player.sendMessage("§c体力耗尽！请等待体力恢复到100才能跑跳。")
                        }
                    }

                    ps.staminastate = if (ps.stamina <= 0) 2 else 1
                    ps.isMoving = false
                }
            }
        }.runTaskTimer(plugin, 0L, 2L)
        staminaTask.add(task.taskId)
    }

    private fun startActionBarTask() {
        val task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
            for ((player, ps) in playerStamina) {
                if (plugin.gameManager.getGameStatus() != GameManager.GameStatus.RUNNING) {
                    continue
                }

                val team = plugin.gameManager.getPlayerTeam(player)
                if (team != GameManager.Team.HUMAN) {
                    continue
                }

                val staminaBar = buildStaminaBar(ps.stamina, ps.maxStamina, ps.staminastate)
                player.sendActionBar(staminaBar)
            }
        }, 0L, 2L)
        actionBarTask.add(task)
    }

    private fun startZombieHealthBarTask() {
        val task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
            for (player in Bukkit.getOnlinePlayers()) {
                val team = plugin.gameManager.getPlayerTeam(player)
                if (team == GameManager.Team.ZOMBIE || team == GameManager.Team.ZOMBIE_MAIN) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 40, 0, false, false))
                }
            }
        }, 0L, 20L)
        zombieHealthBarTask.add(task)
    }

    private fun buildStaminaBar(current: Double, max: Double, state: Int): Component {
        val percentage = current / max
        val totalBars = 20
        val filledBars = (percentage * totalBars).toInt().coerceIn(0, totalBars)
        val emptyBars = totalBars - filledBars

        val (filledColor, emptyColor) = if (state == 1) {
            Pair(NamedTextColor.GREEN, NamedTextColor.GRAY)
        } else {
            Pair(NamedTextColor.RED, NamedTextColor.GRAY)
        }

        val filled = "█".repeat(filledBars)
        val empty = "█".repeat(emptyBars)

        return Component.text("体力: ", NamedTextColor.GOLD)
            .append(Component.text(filled, filledColor))
            .append(Component.text(empty, emptyColor))
            .append(Component.text(" ${current.toInt()}/${max.toInt()}", NamedTextColor.WHITE))
    }

    fun applyZombieEffects(player: Player) {
        val team = plugin.gameManager.getPlayerTeam(player)
        val baseHealth = 20.0
        val maxHealth = when (team) {
            GameManager.Team.ZOMBIE -> baseHealth * plugin.configManager.getZombieHealthMultiplier()
            GameManager.Team.ZOMBIE_MAIN -> baseHealth * plugin.configManager.getAlphaZombieHealthMultiplier()
            else -> return
        }
        player.getAttribute(Attribute.MAX_HEALTH)?.baseValue = maxHealth
        player.health = maxHealth

        player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, Int.MAX_VALUE, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, Int.MAX_VALUE, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, Int.MAX_VALUE, 1, false, false))

        if (team == GameManager.Team.ZOMBIE_MAIN && plugin.configManager.getAlphaZombieParticleEnabled()) {
            val task = object : BukkitRunnable() {
                override fun run() {
                    if (!player.isOnline || plugin.gameManager.getPlayerTeam(player) != GameManager.Team.ZOMBIE_MAIN) {
                        cancel()
                        return
                    }
                    player.world.spawnParticle(Particle.DRAGON_BREATH, player.location.clone().add(0.0, 0.5, 0.0),
                        1, 0.3, 0.5, 0.3, 0.0f)
                }
            }.runTaskTimer(plugin, 0L, 10L)
        }
    }

    private fun startStaminaEffectsTask() {
        val task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
            for ((player, ps) in playerStamina) {
                if (plugin.gameManager.getGameStatus() != GameManager.GameStatus.RUNNING) continue
                if (plugin.gameManager.getPlayerTeam(player) != GameManager.Team.HUMAN) continue

                if (ps.staminastate == 2) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 0, false, false))
                    player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 40, 0, false, false))
                    player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false))
                } else {
                    player.removePotionEffect(PotionEffectType.SLOWNESS)
                    player.removePotionEffect(PotionEffectType.WEAKNESS)
                    player.removePotionEffect(PotionEffectType.GLOWING)
                }
            }
        }, 0L, 20L)
        staminaTask.add(task)
    }

    fun clear() {
        staminaTask.forEach { Bukkit.getScheduler().cancelTask(it) }
        actionBarTask.forEach { Bukkit.getScheduler().cancelTask(it) }
        zombieHealthBarTask.forEach { Bukkit.getScheduler().cancelTask(it) }
        staminaTask.clear()
        actionBarTask.clear()
        zombieHealthBarTask.clear()
        playerStamina.clear()
    }
}