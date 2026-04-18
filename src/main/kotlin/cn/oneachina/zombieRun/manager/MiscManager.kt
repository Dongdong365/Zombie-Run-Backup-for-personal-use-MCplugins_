package cn.oneachina.zombieRun.manager

import cn.oneachina.zombieRun.ZombieRun
import me.deecaad.weaponmechanics.WeaponMechanics
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class MiscManager(private val plugin: ZombieRun) : Listener {

    private val playerCoins = ConcurrentHashMap<Player, Int>()
    private val playerKills = ConcurrentHashMap<Player, Int>()
    private val playerInfections = ConcurrentHashMap<Player, Int>()
    private val selectedWeapon = ConcurrentHashMap<Player, String>()
    private val lastHealth = ConcurrentHashMap<Player, Double>()

    fun getSelectableWeapons(): List<String> {
        return loadWeaponTitlesFromWeaponMechanics()
    }

    fun setSelectedWeapon(player: Player, weaponIndex: Int): Boolean {
        val weapons = getSelectableWeapons()
        if (weaponIndex !in 1..weapons.size) {
            return false
        }
        selectedWeapon[player] = weapons[weaponIndex - 1]
        return true
    }

    fun clearSelectedWeapon(player: Player) {
        selectedWeapon.remove(player)
    }

    fun getSelectedWeapon(player: Player): String? = selectedWeapon[player]

    fun giveRandomGunToAllHumans() {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (plugin.gameManager.getPlayerTeam(player) == GameManager.Team.HUMAN) {
                giveRandomGun(player)
            }
        }
    }

    fun giveRandomGun(player: Player) {
        player.inventory.clear()
        val weapons = getSelectableWeapons()
        if (weapons.isEmpty()) {
            player.sendMessage("§c未找到可用枪械配置。")
            return
        }

        val selected = selectedWeapon[player]
        val weaponTitle = if (selected != null && weapons.contains(selected)) {
            val price = getWeaponPrice(selected)
            if (takeCoins(player, price)) {
                player.sendMessage("§a购买成功！花费硬币: $price，剩余: ${getCoins(player)}")
                selected
            } else {
                player.sendMessage("§c硬币不足，已改为随机枪械。")
                weapons.random()
            }
        } else {
            weapons.random()
        }

        if (!giveWeaponDirectly(player, weaponTitle)) {
            player.sendMessage("§c发放枪械失败：$weaponTitle")
            return
        }

        val sword = ItemStack(Material.IRON_SWORD)
        val meta = sword.itemMeta
        meta?.addEnchant(Enchantment.KNOCKBACK, 1, true)
        meta?.displayName(Component.text("§c匕首"))
        sword.itemMeta = meta
        player.inventory.addItem(sword)

        if (weaponTitle.equals("blaze", true)) {
            player.inventory.setItem(9, ItemStack(Material.ARROW, 64))
            player.inventory.setItem(10, ItemStack(Material.ARROW, 64))
            player.inventory.setItem(11, ItemStack(Material.ARROW, 64))
            player.inventory.setItem(12, ItemStack(Material.ARROW, 64))
            player.inventory.setItem(13, ItemStack(Material.ARROW, 64))
        } else {
            player.inventory.setItem(9, ItemStack(Material.GUNPOWDER, 64))
            player.inventory.setItem(10, ItemStack(Material.GUNPOWDER, 64))
            player.inventory.setItem(11, ItemStack(Material.GUNPOWDER, 64))
            player.inventory.setItem(12, ItemStack(Material.GUNPOWDER, 64))
            player.inventory.setItem(13, ItemStack(Material.GUNPOWDER, 64))
        }
    }

    private fun getWeaponPrice(weaponTitle: String): Int {
        val config = plugin.configManager.getConfig()
        val defaultPrice = config.getInt("weapons.default-price", 600)
        val prices = config.getConfigurationSection("weapons.prices")?.getValues(false) ?: emptyMap()
        val value = prices[weaponTitle]
        val price = when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
        return price ?: defaultPrice
    }

    private fun loadWeaponTitlesFromWeaponMechanics(): List<String> {
        return runCatching {
            WeaponMechanics.getInstance()
                .weaponHandler
                .infoHandler
                .sortedWeaponList
                .filter { it.isNotBlank() }
        }.getOrElse { emptyList() }
    }

    private fun giveWeaponDirectly(player: Player, weaponTitle: String): Boolean {
        return runCatching {
            WeaponMechanics.getInstance()
                .weaponHandler
                .infoHandler
                .giveOrDropWeapon(weaponTitle, player, 1)
        }.getOrDefault(false)
    }

    fun giveStarterKit(player: Player) {
        giveRandomGun(player)
    }

    fun addCoins(player: Player, amount: Int) {
        val current = playerCoins.getOrDefault(player, 0)
        playerCoins[player] = current + amount
        player.sendMessage(Component.text("+ $amount 硬币!").color(NamedTextColor.GOLD))
    }

    fun getCoins(player: Player): Int = playerCoins.getOrDefault(player, 0)

    fun takeCoins(player: Player, amount: Int): Boolean {
        val current = getCoins(player)
        if (current < amount) return false
        playerCoins[player] = current - amount
        return true
    }

    fun addKill(player: Player) {
        playerKills[player] = playerKills.getOrDefault(player, 0) + 1
    }

    fun getKills(player: Player): Int = playerKills.getOrDefault(player, 0)

    fun addInfection(player: Player) {
        playerInfections[player] = playerInfections.getOrDefault(player, 0) + 1
    }

    fun getInfections(player: Player): Int = playerInfections.getOrDefault(player, 0)

    fun getAllKills(): Map<Player, Int> = playerKills.toMap()
    fun getAllInfections(): Map<Player, Int> = playerInfections.toMap()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        val victim = event.entity as? Player ?: return
        if (plugin.gameManager.getPlayerTeam(victim) != GameManager.Team.HUMAN) return

        if (event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            event.damage = 0.05
        }

        val causingEntity = event.damageSource.causingEntity
        if (causingEntity == null && event.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            lastHealth[victim] = victim.health
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                val before = lastHealth.remove(victim) ?: return@Runnable
                val after = victim.health
                val damage = before - after
                if (damage > 0) {
                    plugin.staminaManager.deductStamina(victim, 15.0 * damage)
                }
            }, 1L)
        }
    }

    @EventHandler
    fun onCombat(event: EntityDamageByEntityEvent) {
        val attacker = event.damager as? Player ?: return
        val victim = event.entity as? Player ?: return

        if (plugin.gameManager.getGameStatus() != GameManager.GameStatus.RUNNING) return

        val attackerTeam = plugin.gameManager.getPlayerTeam(attacker)
        val victimTeam = plugin.gameManager.getPlayerTeam(victim)

        if (attackerTeam == GameManager.Team.HUMAN &&
            (victimTeam == GameManager.Team.ZOMBIE || victimTeam == GameManager.Team.ZOMBIE_MAIN)) {

            victim.velocity = victim.velocity.add(attacker.location.direction.setY(-1.0).normalize().multiply(0.3))

            val damage = event.finalDamage * 2
            attacker.sendActionBar(Component.text("造成伤害: ${String.format("%.1f", damage)}").color(NamedTextColor.RED))
        }
    }

    fun teleportToLobby(player: Player) {
        plugin.respawnManager.teleportToWaitRespawn(player)
        plugin.gameManager.setPlayerTeam(player, GameManager.Team.SPECTATOR)
        player.sendMessage("§a你已传送回大厅")
    }

    fun clear() {
        playerCoins.clear()
        playerKills.clear()
        playerInfections.clear()
        selectedWeapon.clear()
        lastHealth.clear()
    }
}
