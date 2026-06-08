package cn.oneachina.zombieRun.task

import cn.oneachina.zombieRun.ZombieRun
import cn.oneachina.zombieRun.manager.GameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class WaitStartCountdownTask(
    private val plugin: ZombieRun,
    private val gameManager: GameManager,
    private var countdown: Int
) : BukkitRunnable() {

    override fun run() {
        if (gameManager.getGameStatus() != GameManager.GameStatus.WAITING) {
            gameManager.cancelWaitStartTask()
            cancel()
            return
        }

        val onlineCount = Bukkit.getOnlinePlayers().size
        val minPlayers = plugin.configManager.getMinPlayers()
        if (onlineCount < minPlayers) {
            gameManager.cancelWaitStartTask()
            cancel()
            return
        }

        if (countdown <= 0) {
            gameManager.cancelWaitStartTask()
            gameManager.forceStartGame()
            cancel()
            return
        }

        Bukkit.getOnlinePlayers().forEach { player ->
            player.showTitle(Title.title(
                Component.text("", NamedTextColor.GREEN),
                Component.text("§a游戏将在 §c$countdown §a秒后开始")
            ))
        }

        countdown--
    }
}