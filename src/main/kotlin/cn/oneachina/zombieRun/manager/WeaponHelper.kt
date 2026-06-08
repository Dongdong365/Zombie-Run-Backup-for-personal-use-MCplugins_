package cn.oneachina.zombieRun.manager

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import kotlin.math.min

class WeaponHelper {

    private val available: Boolean
    private val weaponMechanicsClass: Class<*>?
    private val weaponMechanicsAPI: Class<*>?

    init {
        val wmPlugin = Bukkit.getPluginManager().getPlugin("WeaponMechanics")
        available = wmPlugin != null
        weaponMechanicsClass = if (available) runCatching {
            Class.forName("me.deecaad.weaponmechanics.WeaponMechanics")
        }.getOrNull() else null
        weaponMechanicsAPI = if (available) runCatching {
            Class.forName("me.deecaad.weaponmechanics.WeaponMechanicsAPI")
        }.getOrNull() else null
    }

    fun isAvailable(): Boolean = available

    fun loadWeaponTitles(): List<String> {
        if (!available) return emptyList()
        return runCatching {
            val getInstance = weaponMechanicsClass!!.getMethod("getInstance")
            val instance = getInstance.invoke(null)
            val weaponHandlerField = instance.javaClass.getMethod("getWeaponHandler").invoke(instance)
            val infoHandlerField = weaponHandlerField.javaClass.getMethod("getInfoHandler").invoke(weaponHandlerField)
            val sortedWeaponList = infoHandlerField.javaClass.getMethod("getSortedWeaponList").invoke(infoHandlerField) as List<*>
            sortedWeaponList.filterIsInstance<String>().filter { it.isNotBlank() }
        }.getOrElse { emptyList() }
    }

    fun giveOrDropWeapon(weaponTitle: String, player: Player): Boolean {
        if (!available) {
            player.sendMessage("§cWeaponMechanics 不可用")
            return false
        }
        return runCatching {
            // 方法 1: 直接使用命令（最简单）
            player.sendMessage("§a尝试通过命令发放: /wm give ${player.name} $weaponTitle")
            val success = Bukkit.getServer().dispatchCommand(Bukkit.getServer().consoleSender, "wm give ${player.name} $weaponTitle")
            if (success) {
                player.sendMessage("§a命令发放成功！")
                return@runCatching true
            }
            
            // 方法 2: 尝试反射调用
            player.sendMessage("§a尝试反射调用...")
            val getInstance = weaponMechanicsClass!!.getMethod("getInstance")
            val instance = getInstance.invoke(null)
            
            // 打印所有可用方法，帮助调试
            player.sendMessage("§a可用方法:")
            instance.javaClass.methods.forEach { m ->
                if (m.name.contains("give", ignoreCase = true)) {
                    player.sendMessage("  - ${m.name}(${m.parameterTypes.joinToString { it.simpleName }})")
                }
            }
            
            val weaponHandler = instance.javaClass.getMethod("getWeaponHandler").invoke(instance)
            val infoHandler = weaponHandler.javaClass.getMethod("getInfoHandler").invoke(weaponHandler)
            
            // 尝试不同的方法签名
            val result = try {
                // 尝试 1: giveOrDropWeapon(String, Player, int)
                val method1 = infoHandler.javaClass.getMethod("giveOrDropWeapon", String::class.java, Player::class.java, Int::class.javaPrimitiveType)
                method1.invoke(infoHandler, weaponTitle, player, 1)
                true
            } catch (e: NoSuchMethodException) {
                try {
                    // 尝试 2: giveOrDropWeapon(String, Player)
                    val method2 = infoHandler.javaClass.getMethod("giveOrDropWeapon", String::class.java, Player::class.java)
                    method2.invoke(infoHandler, weaponTitle, player)
                    true
                } catch (e2: NoSuchMethodException) {
                    try {
                        // 尝试 3: giveWeapon(String, Player)
                        val method3 = infoHandler.javaClass.getMethod("giveWeapon", String::class.java, Player::class.java)
                        method3.invoke(infoHandler, weaponTitle, player)
                        true
                    } catch (e3: NoSuchMethodException) {
                        player.sendMessage("§c未找到兼容的 WeaponMechanics API")
                        false
                    }
                }
            }
            
            result
        }.onFailure { e ->
            player.sendMessage("§c发放枪械错误: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
        }.getOrDefault(false)
    }

    fun getWeaponTitle(itemStack: ItemStack): String? {
        if (!available) return null
        return runCatching {
            val method = weaponMechanicsAPI!!.getMethod("getWeaponTitle", ItemStack::class.java)
            method.invoke(null, itemStack) as? String
        }.getOrNull()
    }

    fun getCurrentAmmo(itemStack: ItemStack): AmmoData? {
        if (!available) return null
        return runCatching {
            val method = weaponMechanicsAPI!!.getMethod("getCurrentAmmo", ItemStack::class.java)
            val ammo = method.invoke(null, itemStack) ?: return@runCatching null
            val ammoClass = ammo.javaClass
            val ammoTitleMethod = ammoClass.getMethod("getAmmoTitle")
            val ammoTitle = ammoTitleMethod.invoke(ammo) as? String ?: return@runCatching null
            AmmoData(ammoTitle, ammo)
        }.getOrNull()
    }

    fun generateAmmo(ammoTitle: String, isMagazine: Boolean): ItemStack? {
        if (!available) return null
        return runCatching {
            val method = weaponMechanicsAPI!!.getMethod("generateAmmo", String::class.java, Boolean::class.javaPrimitiveType)
            method.invoke(null, ammoTitle, isMagazine) as? ItemStack
        }.getOrNull()
    }

    data class AmmoData(val ammoTitle: String, val rawAmmo: Any)

    companion object {
        @Volatile
        private var instance: WeaponHelper? = null

        fun getInstance(): WeaponHelper {
            return instance ?: synchronized(this) {
                instance ?: WeaponHelper().also { instance = it }
            }
        }
    }
}
